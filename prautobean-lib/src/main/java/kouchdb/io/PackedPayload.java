package kouchdb.io;

import kouchdb.ann.ProtoNumber;
import kouchdb.ann.ProtoOrigin;

import java.lang.reflect.*;
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
        put(long.class, (byteBuffer, o) -> {
            byteBuffer.putLong((long) o);
        });
        put(double.class, (byteBuffer, o) -> {
            byteBuffer.putDouble((double) o);
        });
        put(int.class, (byteBuffer, o) -> {
            byteBuffer.putInt((int) o);
        });
        put(float.class, (byteBuffer, o) -> {
            byteBuffer.putFloat((float) o);
        });
        put(short.class, (byteBuffer, o) -> {
            byteBuffer.putShort((short) o);
        });
        put(byte.class, (byteBuffer, o) -> {
            byteBuffer.put((byte) o);
        });
        put(byte[].class, (byteBuffer, o) -> {
            byte[] o1 = (byte[]) o;
            byteBuffer.putInt(o1.length);
            byteBuffer.put(o1);
        });
        put(String.class, (byteBuffer, o) -> {
            byte[] o1 = o.toString().getBytes(UTF_8);
            byteBuffer.putInt(o1.length);
            byteBuffer.put(o1);
        });
        put(boolean.class, (byteBuffer, o) -> {
            byteBuffer.put((byte) ((boolean) o ? 1 : 0));
        });
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
            put(byte[].class, byteBuffer -> {
                int anInt = byteBuffer.getInt();
                byte[] bytes = new byte[anInt];
                byteBuffer.get(bytes);
                return bytes;
            });
            put(String.class, byteBuffer -> {
                int anInt = byteBuffer.getInt();
                byte[] bytes = new byte[anInt];
                byteBuffer.get(bytes);
                return new String(bytes, UTF_8);
            });
            put(boolean.class, byteBuffer -> byteBuffer.get() > 0);
        }
    };
    public static final Comparator<Method> METHOD_COMPARATOR = (o1, o2) -> {
        return (o1.getAnnotation(ProtoNumber.class).value()) - o2.getAnnotation(ProtoNumber.class).value();
    };

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
     * <p>
     * <p>
     * [---len---][---bitset[-bools-][-optbools-]][---nonopt---][---opt---]
     *
     * @param theAutoBeanClass an-autobean like generated protobuf proxy interface
     */
    public PackedPayload(Class<ProtoMessage> theAutoBeanClass) {
        AtomicInteger nc = new AtomicInteger(0);
        asList(theAutoBeanClass.getDeclaredMethods()).forEach(method -> {
            if (null == method.getAnnotation(ProtoNumber.class)) return;
            kouchdb.ann.Optional annotation = method.getAnnotation(kouchdb.ann.Optional.class);
            boolean b2 = annotation == null;
            boolean b3 = method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class;
            Collection<Method> l = b2 ? b3 ? bool : nonOpt : b3 ? bool : opt;
            l.add(method);
        });

        bitsetLen = bool.size() + opt.size();
        BitSet bitSet = new BitSet(bitsetLen);
        bitSet.set(bitsetLen);
        bitsetBytes = bitSet.toByteArray().length;
        init(theAutoBeanClass);
    }

    private static void skim(ByteBuffer in, Map<Method, Object> values, Map<Method, Object> offsets, Method method) {
        Class<?> returnType = method.getReturnType();
        if (VIEWGETTER.containsKey(returnType)) {
            values.put(method, VIEWGETTER.get(returnType).apply(in));
        } else if (returnType.isEnum()) {
            values.put(method, returnType.getEnumConstants()[in.getShort()]);
        } else {
            offsets.put(method, in.position());
            int anInt = in.getInt();
            in.position(in.position() + anInt);
        }
    }

    private void init(Class theAutoBean) {
        codeSmell.put(theAutoBean, this);
    }

    public ProtoMessage get(Class c, ByteBuffer in) {
        int fixup = in.getInt();
        byte[] bytes = new byte[bitsetBytes];
        in.get(bytes);
        BitSet bitSet = BitSet.valueOf(bytes);

        Map<Method, Object> values = new TreeMap<>(METHOD_COMPARATOR);
        Map<Method, Object> offsets = new TreeMap<>(METHOD_COMPARATOR);

        AtomicInteger c1 = new AtomicInteger(0);
        bool.forEach(method -> {
            values.put(method, bitSet.get(c1.getAndIncrement()));
        });

        nonOpt.forEach(method -> skim(in, values, offsets, method));
        //handle opt
        c1.set(0);

        opt.forEach(method -> {
            if (bitSet.get(bool.size() + (c1.getAndIncrement()))) skim(in, values, offsets, method);
            else
                values.put(method, null);
        });

        return (ProtoMessage) Proxy.newProxyInstance(c.getClassLoader(), new Class[]{c}, (proxy, method, args) ->
                values.computeIfAbsent(method, k ->
                        offsets.computeIfPresent(k, (k1, v) -> {
                            in.position((Integer) v);
                            int len = in.getInt();
                            ByteBuffer slice = (ByteBuffer) in.slice().limit(len);

                            Class returnType = method.getReturnType();
                            Object r = null;
                            if (returnType.isAnnotationPresent(ProtoOrigin.class))
                                r = codeSmell.computeIfAbsent(returnType, PackedPayload::new).get(returnType, in);
                            else if (returnType.isAssignableFrom(List.class)) {
                                GenericDeclaration genericDeclaration = (GenericDeclaration) returnType.getTypeParameters()[0];
                                if (VIEWCLASSES.contains(genericDeclaration))
                                    r = new ReadOnlyBBList((Class) genericDeclaration, VIEWSIZES.get(genericDeclaration), slice);

                                else {
                                    ArrayList<Object> objects = new ArrayList<>();
                                    r = objects;
                                    Class genericDeclaration1 = (Class) genericDeclaration;
                                    if (genericDeclaration1.isEnum())
                                        while (slice.hasRemaining())
                                            objects.add(genericDeclaration1.getEnumConstants()[slice.getShort()]);
                                    else {
                                        PackedPayload packedPayload = codeSmell.computeIfAbsent(genericDeclaration1, PackedPayload::new);
                                        while (slice.hasRemaining()) {
                                            Object o = packedPayload.get(genericDeclaration1, in);
                                            objects.add(o);
                                        }
                                    }
                                }
                            }
                            return r;
                        })));
    }

    public void put(ProtoMessage proto, ByteBuffer out) {
        int fixup = out.position();
        out.putInt(-1);
        BitSet bitSet = new BitSet(bitsetLen);
        bitSet.set(bitsetLen - 1);
        AtomicInteger c = new AtomicInteger(0);
        bool.forEach(method -> {
            try {
                bitSet.set(c.getAndIncrement(), Boolean.TRUE.equals(method.invoke(proto)));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });

        byte[] src = bitSet.toByteArray();
        out.put(src);
        nonOpt.forEach(method -> {
            try {
                Class<?> returnType = method.getReturnType();
                Object invoke = method.invoke(proto);
                writeElement(out, invoke, method, null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        c.set(0);
        opt.forEach(method -> {
            try {
                Class<?> returnType = method.getReturnType();
                Object invoke = method.invoke(proto);
                boolean b = null != invoke;
                bitSet.set(c.getAndIncrement() + bool.size(), b);
                if (b) writeElement(out, invoke, method, null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

        });
        int i = out.position() - fixup;
        ByteBuffer backtrack = (ByteBuffer) out.duplicate().position(fixup);
        byte[] src1 = bitSet.toByteArray();
        backtrack.putInt(i).put(src1);


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
            int fixupIndex = out.position();
            out.putInt(-1);
            Class genericReturnType = (Class) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];

            List list = (List) value;
            if (VIEWSETTER.containsKey(genericReturnType))
                list.forEach(o -> VIEWSETTER.get(genericReturnType).accept(out, o));
            else if ((genericReturnType).isEnum()) {
                list.forEach(o -> {
                    out.putShort((short) ((Enum) o).ordinal());
                });
            } else if (genericReturnType.isAnnotationPresent(ProtoOrigin.class)) list.forEach(o -> {
                writeElement(out, o, null, genericReturnType);
            });
            int i = out.position() - fixupIndex;
            out.putInt(fixupIndex, i);
        }
    }
}
    