package kouchdb.io;

import kouchdb.ann.Optional;
import kouchdb.ann.ProtoNumber;
import kouchdb.ann.ProtoOrigin;

import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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
            byte[] bytes = o.toString().getBytes(UTF_8);
            int begin = byteBuffer.position();
            reposition(byteBuffer, begin + 5);
            byteBuffer.put(bytes);
            writeSize(byteBuffer, begin, bytes.length);
        });
        put(boolean.class, (byteBuffer, o) -> byteBuffer.put((byte) (Boolean.TRUE.equals(o) ? 1 : 0)));
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
            put(boolean.class, byteBuffer -> 0 != byteBuffer.get());
        }
    };
    public static final Comparator<Method> METHOD_COMPARATOR = (o1, o2) -> o1.getAnnotation(ProtoNumber.class).value() - o2.getAnnotation(ProtoNumber.class).value();

    static Map<Class, PackedPayload> codeSmell = new HashMap<>();
    /**
     * non-optional booleans.   always present.
     */
    public Collection<Method> boolPlaceHolders = new TreeSet<>(METHOD_COMPARATOR);
    /**
     * optional variables that aren't bools. exist as part of the bitset above plus as n-byte values or ints to hold byte[] strings/blobs
     */
    public Collection<Method> optPlaceHolders = new TreeSet<>(METHOD_COMPARATOR);
    /**
     * always present before optPlaceHolders but after bitset.
     */
    public Collection<Method> nonOptPlaceHolders = new TreeSet<>(METHOD_COMPARATOR);
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
     * [---len---][---bitset[-bools-][-optbools-]][---nonopt---][---optPlaceHolders---]
     *
     * @param theAutoBeanClass an-autobean like generated protobuf proxy interface
     */
    public PackedPayload(Class<ProtoMessage> theAutoBeanClass) {
        AtomicInteger nc = new AtomicInteger(0);
        asList(theAutoBeanClass.getDeclaredMethods()).forEach(method -> {
            if (method.isAnnotationPresent(ProtoNumber.class)) {
                Collection<Method> l = boolean.class == method.getReturnType() || Boolean.class == method.getReturnType() ? boolPlaceHolders : method.isAnnotationPresent(Optional.class) ? optPlaceHolders : nonOptPlaceHolders;
                l.add(method);
            }
        });

        bitsetLen = boolPlaceHolders.size() + optPlaceHolders.size();
        BitSet bitSet = new BitSet(bitsetLen);
        bitSet.set(bitsetLen);
        bitsetBytes = bitSet.toByteArray().length;
        init(theAutoBeanClass);
    }

    static void skim(ByteBuffer in, Map<Method, Object> values, Map<Method, Object> offsets, Method method, int position) {
        Class<?> returnType = method.getReturnType();
        if (VIEWGETTER.containsKey(returnType)) {
            skimValue(VIEWGETTER.get(returnType).apply(in), values, method);
        } else if (returnType.isEnum()) {
            skimValue(returnType.getEnumConstants()[in.getShort()], values, method);
        } else {
            skimOver(in, offsets, method, position);
        }
    }

    static void skimValue(Object value, Map<Method, Object> values, Method method) {
        values.put(method, value);
    }

    static void skimOver(ByteBuffer in, Map<Method, Object> offsets, Method method, int position) {
        skimValue(position, offsets, method);
        int size = readSize(in, String.valueOf(method));
        reposition(in, position + size, "skim " + method);
    }

    private void init(Class theAutoBean) {
        codeSmell.putIfAbsent(theAutoBean, this);
    }

    public <C extends Class<ProtoMessage>> ProtoMessage get(C c, ByteBuffer in___) {

        int begin1 = in___.position();

        long size = readSize(in___);
        byte[] bytes = new byte[bitsetBytes];
        in___.get(bytes);
        BitSet bitSet = BitSet.valueOf(bytes);

        Map<Method, Object> values = new TreeMap<>(METHOD_COMPARATOR);
        Map<Method, Object> offsets = new TreeMap<>(METHOD_COMPARATOR);

        AtomicInteger c1 = new AtomicInteger(0);
        boolPlaceHolders.forEach(method -> values.put(method, bitSet.get(c1.getAndIncrement())));

        nonOptPlaceHolders.forEach(method -> {
            skim(in___, values, offsets, method, in___.position());
        });
        //handle optPlaceHolders
        c1.set(0);

        optPlaceHolders.forEach(new Consumer<Method>() {
            @Override
            public void accept(Method method) {
                if (bitSet.get(boolPlaceHolders.size() + c1.getAndIncrement())) {

                    skim(in___, values, offsets, method, in___.position());


                } else
                    skimValue(null, values, method);
            }
        });

        return (ProtoMessage) Proxy.newProxyInstance(c.getClassLoader(), new Class[]{c}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return values.computeIfAbsent(method, new Function<Method, Object>() {
                    @Override
                    public Object apply(Method k) {
                        return offsets.computeIfPresent(k, new BiFunction<Method, Object, Object>() {
                            @Override
                            public Object apply(Method k1, Object v) {
                                reposition(in___, (Integer) v, "method: " +
                                        k1 + " start");

                                Class returnType = method.getReturnType();
                                Object r = null;
                                if (returnType.isAnnotationPresent(ProtoOrigin.class))
                                    r = readComplex(returnType, in___);
                                else if (returnType.isAssignableFrom(List.class)) {
                                    r = readList(method, in___);
                                }
                                return r;
                            }
                        });
                    }
                });
            }
        });

    }

    static Object readComplex(Class returnType, ByteBuffer in___) {
        return codeSmell.computeIfAbsent(returnType, PackedPayload::new).get(returnType, in___);
    }


    static Object readList(Method method, ByteBuffer in___) {
        ParameterizedType genericReturnType = (ParameterizedType) method.getGenericReturnType();
        Class aClass = (Class) genericReturnType.getActualTypeArguments()[0];
        int listSize = readSize(in___);
        int fin = in___.position() + listSize;

        Object r;//enums lack generic type parms. not sure why
        if (VIEWSIZES.containsKey(aClass)) {
            r = new ReadOnlyBBList(aClass, VIEWSIZES.get(aClass), (ByteBuffer) in___.slice().limit(listSize));
            reposition(in___, fin, "primitive list close.");
        } else {
            Collection objects = (Collection) (r = new ArrayList());
            if (aClass.isEnum())
                while (in___.position() < fin) {
                    System.err.println(":>e " + in___.position());
                    short aShort = in___.getShort();
                    Object[] enumConstants = aClass.getEnumConstants();
                    Object e = enumConstants[aShort];
                    System.err.println(":>e " + e);
                    objects.add(e);
                }
            else {
                PackedPayload packedPayload = codeSmell.computeIfAbsent(aClass, PackedPayload::new);
                while (in___.position() < fin) {
                    Object o = packedPayload.get(aClass, in___);
                    objects.add(o);
                }
            }
        }
        return r;
    }


    public void put(ProtoMessage proto, ByteBuffer out) {
        int begin = out.position();
        reposition(out, begin + 5);
        int fixup = out.position();
        BitSet bitSet = new BitSet(bitsetLen);
        if (0 < bitsetLen) bitSet.set(bitsetLen - 1);
        AtomicInteger c = new AtomicInteger(0);
        boolPlaceHolders.forEach(method -> {
            try {
                bitSet.set(c.getAndIncrement(), Boolean.TRUE.equals(method.invoke(proto)));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });


        out.put(bitSet.toByteArray());
        nonOptPlaceHolders.forEach(method -> {
            try {

                Object value = method.invoke(proto);
                writeElement(out, value, method, null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        c.set(0);
        optPlaceHolders.forEach(method -> {
            try {

                Object value = method.invoke(proto);
                boolean b = null != value;
                bitSet.set(c.getAndIncrement() + boolPlaceHolders.size(), b);
                if (b) {
                    writeElement(out, value, method, null);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

        });
        reposition(out.duplicate(), fixup, "writing bitset:" + bitSet.toString()).put(bitSet.toByteArray());
        writeSize(out, begin, (long) (out.position() - fixup));
    }

    static void writeElement(ByteBuffer out, Object value, Method method, Class forcedClaz) {

        Class<?> returnType = null == forcedClaz ? null == method ? value.getClass() : method.getReturnType() : forcedClaz;

        if (VIEWSETTER.containsKey(returnType))
            writeSimple(out, value, returnType);
        else if (returnType.isEnum()) {
            writeEnum(out, (Enum) value);
        } else if (returnType.isAnnotationPresent(ProtoOrigin.class))
            writeComplex(out, value, returnType);
        else if (returnType.isAssignableFrom(List.class))
            writeList(out, (List) value, ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0]);
        else throw new Error("non-matching type data found here.");
    }

    static void writeComplex(ByteBuffer out, Object value, Class<?> returnType) {
        PackedPayload packedPayload = codeSmell.computeIfAbsent(returnType, aClass -> new PackedPayload(returnType));
        packedPayload.put(value, out);
    }

    static void writeEnum(ByteBuffer out, Enum value) {
        int ordinal = 0;
        try {
            ordinal = value.ordinal();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
        }
        out.putShort((short) ordinal);
    }

    static void writeSimple(ByteBuffer out, Object value, Class<?> returnType) {
        VIEWSETTER.get(returnType).accept(out, value);
    }

    static void writeList(ByteBuffer out, List valueToWrite, Type type) {
        int begin = out.position();
        reposition(out, begin + 5, "writeList fixup");
        int fixup = out
                .position();
        Class genericReturnType = (Class) type;

        if (VIEWSETTER.containsKey(genericReturnType)) {
            writeSimpleList(out, valueToWrite, genericReturnType);
        } else if (genericReturnType.isEnum()) {
            writeEnumList(out, valueToWrite);
        } else if (genericReturnType.isAnnotationPresent(ProtoOrigin.class))
            writeComplexList(out, valueToWrite, genericReturnType);
        writeSize(out, begin, out.position() - fixup);
    }

    static void writeComplexList(ByteBuffer out, List valueToWrite, Class genericReturnType) {
        valueToWrite.forEach(o -> writeElement(out, o, null, genericReturnType));
    }

    static void writeEnumList(ByteBuffer out, List valueToWrite) {
        valueToWrite.forEach(o -> {
            int ordinal = ((Enum) o).ordinal();
            out.putShort((short) (ordinal & 0xffff));
        });
    }

    private static void writeSimpleList(ByteBuffer out, List valueToWrite, Class genericReturnType) {
        valueToWrite.forEach(o -> VIEWSETTER.get(genericReturnType).accept(out, o));
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
    static ByteBuffer writeSize(ByteBuffer out, int begin, long size) {
        ByteBuffer writeBuf = reposition(out.duplicate(), begin);
        if (0xff > size) {
            writeBuf.put((byte) (size & 0xff));
            ByteBuffer copyInPlace = reposition((ByteBuffer) out.duplicate().flip(), begin + 5);
            writeBuf.put(copyInPlace);
            reposition(out, writeBuf.position(), "trimsize");
        } else
            writeBuf.put((byte) 0xff)
                    .putInt((int) (size & 0xffff_ffffL));
        System.err.println("--| " + size);
        return out;
    }

    public static int readSize(ByteBuffer in, String... audit) {
        int sanityCheck = in.remaining();
        long size = in.get() & 0xff;
        sanityCheck--;
        if (0xff == size) {
            size = in.getInt() & 0xffff_ffff;
            sanityCheck -= 4;
        }
        if (audit.length > 0) System.err.println("<?> " + size + " :\t" + audit[0]);
        assert sanityCheck >= size : "" + in + " asking for size " + size;
        System.err.println("|-- " + size);

        return (int) size;
    }

    static ByteBuffer reposition(ByteBuffer in, int newpos, String... audit) {
        System.err.println(":<>  " + in + ":\t" + in.position() + ':' + newpos + (audit.length > 0 ? '\t' + audit[0] : ""));
        return (ByteBuffer) in.position(newpos);
    }
}
    