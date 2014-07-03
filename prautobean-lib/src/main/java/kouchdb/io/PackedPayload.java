package kouchdb.io;

import kouchdb.ann.Optional;
import kouchdb.ann.ProtoNumber;
import kouchdb.ann.ProtoOrigin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

/**
 * takes autobean-like interface. writes {non-optional,optional}{booelan,other}as bitset, then length-prefixed bytes.  Lists are length-prefixed payloads
 *
 * @param <ProtoMessage>
 */
public class PackedPayload<ProtoMessage> {
    /**
     * a nil holder
     */
    public static final byte[] EMPTY = new byte[0];
    public static final Set<Class<?>> VIEWCLASSES = new HashSet<Class<?>>(asList(long.class, double.class, int.class, float.class, short.class, byte.class));
    public static final Map<Class, Integer> VIEWSIZES = new HashMap<Class, Integer>() {{
        put(long.class, 8);
        put(double.class, 8);
        put(int.class, 4);
        put(float.class, 4);
        put(short.class, 2);
        put(byte.class, 1);
        put(boolean.class, 1);
    }};

    /**
     * lambdas that set values to a stream
     */

    public static final Map<Class, BiConsumer<ByteBuffer, Object>> VIEWSETTER = new HashMap<Class, BiConsumer<ByteBuffer, Object>>() {{
        put(long.class, (byteBuffer, o) -> byteBuffer.putLong((long) o));
        put(double.class, (byteBuffer, o) -> byteBuffer.putDouble((double) o));
        put(int.class, (byteBuffer, o) -> byteBuffer.putInt((int) o));
        put(float.class, (byteBuffer, o) -> byteBuffer.putFloat((float) o));
        put(short.class, (byteBuffer, o) -> byteBuffer.putShort((short) o));
        put(byte.class, (byteBuffer, o) -> byteBuffer.put((byte) o));

        put(String.class, (byteBuffer, o) -> {
            byte[] o1 = o.toString().getBytes(UTF_8);
            int begin = byteBuffer.position();
            byteBuffer.position(begin + 5);
            byteBuffer.put(o1);
            writeSize(byteBuffer, begin, o1.length);
        });
        put(boolean.class, (byteBuffer, o) -> byteBuffer.put((byte) ((boolean) o ? 1 : 0)));
    }};

    /**
     * methods of ByteBuffer that grab from the stream
     */
    public static final Map<Class, Function<ByteBuffer, Object>> VIEWGETTER = new HashMap<Class, Function<ByteBuffer, Object>>() {
        {
            put(long.class, ByteBuffer::getLong);
            put(double.class, ByteBuffer::getDouble);
            put(int.class, ByteBuffer::getInt);
            put(float.class, ByteBuffer::getFloat);
            put(short.class, ByteBuffer::getShort);
            put(byte.class, ByteBuffer::get);


            put(String.class, byteBuffer -> {
                int anInt = readSize(byteBuffer);
                byte[] bytes = new byte[anInt];
                byteBuffer.get(bytes);
                return new String(bytes, UTF_8);
            });
            put(boolean.class, byteBuffer -> 0 < byteBuffer.get());
        }
    };
    public static final Comparator<Method> METHOD_COMPARATOR = (o1, o2) -> o1.getAnnotation(ProtoNumber.class).value() - o2.getAnnotation(ProtoNumber.class).value();

    static Map<Class, PackedPayload> codeSmell = new HashMap<>();
    /**
     * non-optional booleans.   always present.
     */
    public Collection<Method> bool = new TreeSet<>(METHOD_COMPARATOR);
    /**
     * optional variables that aren't bools. exist as part of the bitset above plus as n-byte values or ints to hold byte[] strings/blobs
     */
    public Collection<Method> opt = new TreeSet<>(METHOD_COMPARATOR);
    /**
     * always present before opt but after bitset.
     */
    public Collection<Method> nonOpt = new TreeSet<>(METHOD_COMPARATOR);
    /**
     * number of bits padded to 8, a constant per protobuf
     */
    private int bitsetLen;
    /**
     * too lazy/distrustful to bother with alignment/8
     */
    private int bitsetBytes;


    /**
     * this initializes the invariants that hold a serialized message.
     * <p/>
     * <p/>
     * [---len---][---bitset[-bools-][-optbools-]][---nonopt---][---opt---]
     *
     * @param theAutoBeanClass an-autobean like generated protobuf proxy interface
     */
    public PackedPayload(Class<ProtoMessage> theAutoBeanClass) {
        AtomicInteger nc = new AtomicInteger(0);
        asList(theAutoBeanClass.getDeclaredMethods()).forEach(method -> {
            if (method.isAnnotationPresent(ProtoNumber.class)) {
                Collection<Method> l = boolean.class == method.getReturnType() || Boolean.class == method.getReturnType() ? bool : method.isAnnotationPresent(Optional.class) ? opt : nonOpt;
                l.add(method);
            }
        });

        bitsetLen = bool.size() + opt.size();
        BitSet bitSet = new BitSet(bitsetLen);
        bitSet.set(bitsetLen);
        bitsetBytes = bitSet.toByteArray().length;
        init(theAutoBeanClass);
    }

    private static void skim(ByteBuffer in, Map<Method, Object> values, Map<Method, Object> offsets, Method method, int position) {
        Class<?> returnType = method.getReturnType();
        if (VIEWGETTER.containsKey(returnType)) {
            values.put(method, VIEWGETTER.get(returnType).apply(in));
        } else if (returnType.isEnum()) {
            values.put(method, returnType.getEnumConstants()[in.getShort()]);
        } else {
            offsets.put(method, position);
            int anInt = readSize(in);
            in.position( in.position() + anInt);
        }
    }

    private void init(Class theAutoBean) {
        codeSmell.putIfAbsent(theAutoBean, this);
    }

    public <C extends Class<ProtoMessage>> ProtoMessage get(C c, ByteBuffer in) {
        long size = readSize(in);
        byte[] bytes = new byte[bitsetBytes];
        in.get(bytes);
        BitSet bitSet = BitSet.valueOf(bytes);

        Map<Method, Object> values = new TreeMap<>(METHOD_COMPARATOR);
        Map<Method, Object> offsets = new TreeMap<>(METHOD_COMPARATOR);

        AtomicInteger c1 = new AtomicInteger(0);
        bool.forEach(method -> values.put(method, bitSet.get(c1.getAndIncrement())));

        nonOpt.forEach(method -> skim(in, values, offsets, method, in.position()));
        //handle opt
        c1.set(0);

        opt.forEach(method -> {
            if (bitSet.get(bool.size() + c1.getAndIncrement()))
                skim(in, values, offsets, method, in.position());
            else
                values.put(method, null);
        });

        return (ProtoMessage) Proxy.newProxyInstance(c.getClassLoader(),
                new Class[]{c},
                (proxy, method, args) ->
                values.computeIfAbsent(method, k -> offsets.computeIfPresent(k, (k1, v) -> {
                    in.position((Integer) v);
                    int size1 = readSize(in);
                    int fin = in.position() + size1;

                    Class returnType = method.getReturnType();
                    Object r = null;
                    if (returnType.isAnnotationPresent(ProtoOrigin.class))
                        r = codeSmell.computeIfAbsent(returnType, PackedPayload::new).get(returnType, in);
                    else if (returnType.isAssignableFrom(List.class)) {
                        //enums lack generic type parms. not sure why
                        ParameterizedType genericReturnType = (ParameterizedType) method.getGenericReturnType();
                        Class aClass = (Class) genericReturnType.getActualTypeArguments()[0];
                        System.err.println("");

                        if (VIEWSIZES.containsKey(aClass)) {
                            r = new ReadOnlyBBList(aClass, VIEWSIZES.get(aClass), (ByteBuffer) in.slice().limit(size1));
                            in.position(fin);
                        } else {
                            List<Object> objects = new ArrayList<>();
                            r = objects;
                            if (aClass.isEnum())
                                while (in.position() < fin)
                                    objects.add(aClass.getEnumConstants()[in.getShort()]);
                            else {
                                PackedPayload packedPayload = codeSmell.computeIfAbsent(aClass, PackedPayload::new);
                                while (in.position() < fin) {
                                    Object o = packedPayload.get(aClass, in);
                                    objects.add(o);
                                }
                            }
                        }
                    }
                    return r;
                })));

    }


    public void put(ProtoMessage proto, ByteBuffer out) {
        int begin = out.position();
        out.position(begin + 5);
        int fixup = out.position();
        BitSet bitSet = new BitSet(bitsetLen);
        if (0 < bitsetLen) bitSet.set(bitsetLen - 1);
        AtomicInteger c = new AtomicInteger(0);
        bool.forEach(method -> {
            try {
                bitSet.set(c.getAndIncrement(), Boolean.TRUE.equals(method.invoke(proto)));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });

        out.put(bitSet.toByteArray());
        nonOpt.forEach(method -> {
            try {
                Object invoke = method.invoke(proto);
                writeElement(out, invoke, method, null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        c.set(0);
        opt.forEach(method -> {
            try {

                Object invoke = method.invoke(proto);
                boolean b = null != invoke;
                bitSet.set(c.getAndIncrement() + bool.size(), b);
                if (b) writeElement(out, invoke, method, null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

        });
        ((ByteBuffer) out.duplicate().position(fixup)).put(bitSet.toByteArray());
        long size = out.position() - fixup;
        writeSize(out, begin, size);
        out.put(bitSet.toByteArray());
    }

    private void writeElement(ByteBuffer out, Object value, Method method, Class forcedClaz) {

        Class<?> returnType = null == forcedClaz ? null == method ? value.getClass() : method.getReturnType() : forcedClaz;
        if (VIEWSETTER.containsKey(returnType)) {

            VIEWSETTER.get(returnType).accept(out, value);

        } else if (returnType.isEnum()) {
            int ordinal = 0;
            try {
                ordinal = ((Enum) value).ordinal();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
            }
            out.putShort((short) ordinal);
        } else if (returnType.isAnnotationPresent(ProtoOrigin.class)) {
            PackedPayload packedPayload = codeSmell.computeIfAbsent(returnType, aClass -> new PackedPayload(returnType));
            packedPayload.put(value, out);

        } else if (returnType.isAssignableFrom(List.class)) {
            int begin = out.position();
            out.position(begin + 5);
            int content = out.position();
            Class genericReturnType = (Class) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];

            List list = (List) value;
            if (VIEWSETTER.containsKey(genericReturnType)) {
                BiConsumer<ByteBuffer, Object> byteBufferObjectBiConsumer = VIEWSETTER.get(genericReturnType);
                list.forEach(o -> byteBufferObjectBiConsumer.accept(out, o));
            } else if (genericReturnType.isEnum()) list.forEach(o -> {
                int ordinal = ((Enum) o).ordinal();
                out.putShort((short) (ordinal & 0xffff));
            });
            else if (genericReturnType.isAnnotationPresent(ProtoOrigin.class))
                list.forEach(o -> writeElement(out, o, null, genericReturnType));

            writeSize(out, begin, out.position() - content);
        }
    }


    public static final <T, C extends Class<T>> PackedPayload<T> create(C c) {
        return codeSmell.computeIfAbsent(c, PackedPayload::new);
    }

    /**
     * given an output bytebuffer with 5 bytes headroom in front:
     * <p/>
     * if size is less than 255 we write a byte and move the page in place to have a 1 byte size
     * <p/>
     * otherwise we encode 0xff followed by the int32 size.
     *
     * @param out   bytebuffer with data starting at begin+5
     * @param begin the starting mark
     * @param size  the payload actual size used in the out buf.
     */
    public static void writeSize(ByteBuffer out, int begin, long size) {
        ByteBuffer writeBuf = (ByteBuffer) out.duplicate().position(begin);
        if (255 > size) {
            writeBuf.put((byte) (size & 0xff));
            writeBuf.put((ByteBuffer) out.duplicate().flip().position(begin + 5));
            out.position(writeBuf.position());
        } else writeBuf.put((byte) 0xff).putInt((int) (size & 0xffff_ffffL));
    }

    public static int readSize(ByteBuffer in) {
        int sanityCheck = in.remaining();
        long size = in.get() & 0xff;
        sanityCheck--;
        if (0xff == size) {
            size = in.getInt() & 0xffff_ffffL;
            sanityCheck -= 4;
        }
        assert sanityCheck >= size:"!-- "+in+"/avail:"+sanityCheck+"\treports a size\t"+size ;

        return (int) size;
    }
}
    