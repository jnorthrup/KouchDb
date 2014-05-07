package kouchdb.io;

import kouchdb.ann.ProtoNumber;
import kouchdb.ann.ProtoOrigin;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
    public static final HashSet<Class<?>> VIEWCLASSES = new HashSet<Class<?>>(asList(long.class, double.class, int.class, float.class, short.class, byte.class));
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

    static Map<Class, PackedPayload> codeSmell = new LinkedHashMap<>();
    /**
     * non-optional booleans.   always present.
     */
    public List<Method> bool = new ArrayList<>();
    /**
     * optional variables that aren't bools. exist as part of the bitset above plus as n-byte values or ints to hold byte[] strings/blobs
     */
    public List<Method> opt = new ArrayList<>();
    /**
     * always present before opt but after bitset.
     */
    public List<Method> nonOpt = new ArrayList<>();
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
            List<Method> l = b2 ? b3 ? bool : nonOpt : b3 ? bool : opt;
            l.add(method);
        });

        bitsetLen = bool.size() + opt.size();
        BitSet bitSet = new BitSet(bitsetLen);
        bitSet.set(bitsetLen - 1);
        bitsetBytes = bitSet.toByteArray().length;
        init(theAutoBeanClass);
    }

    private static void skim(ByteBuffer in, Map values, Map offsets, Method method) {
        Class<?> returnType = method.getReturnType();
        if (VIEWCLASSES.contains(returnType)) {
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

        Map values = new LinkedHashMap<>();
        Map offsets = new LinkedHashMap<>();
        for (int i1 = 0; i1 < bool.size(); i1++) {
            Method method = bool.get(i1);
            values.put(method, bitSet.get(i1));
        }
        nonOpt.forEach(method -> skim(in, values, offsets, method));
        //handle opt
        for (int i = 0; i < opt.size(); i++) {
            Method method = opt.get(i);
            if (bitSet.get(bool.size() + i)) {
                skim(in, values, offsets, method);
            } else values.put(method, null);
        }
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

        for (int size = bool.size(), i = 0; i < size; i++) {
            Method method = bool.get(i);
            try {
                bitSet.set(i, Boolean.TRUE.equals(method.invoke(proto)));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0, nonOptSize = nonOpt.size(); i < nonOptSize; i++) {
            try {
                Method method = nonOpt.get(i);
                Class<?> returnType = method.getReturnType();
                Object invoke = method.invoke(proto);
                writeElement(out, returnType, invoke);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0, optSize = opt.size(); i < optSize; i++) {
            if(bitSet.get(bool.size()+i)) {
                try {
                    Method method = nonOpt.get(i);
                    Class<?> returnType = method.getReturnType();
                    Object invoke = method.invoke(proto);
                    writeElement(out, returnType, invoke);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

            }
        }
        final int i = out.position() - fixup;
        out.putInt(fixup,i);
    }

    private void writeElement(ByteBuffer out, Class<?> returnType, Object invoke) {
        if (VIEWCLASSES.contains(returnType)) {

            VIEWSETTER.get(returnType).accept(out, invoke);

        } else if (returnType.isEnum()) {
            int ordinal = 0;
            try {
                ordinal = ((Enum) invoke).ordinal();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } finally {
            }
            out.putShort((short) ordinal);
        } else if (returnType.isAnnotationPresent(ProtoOrigin.class)) {
            PackedPayload packedPayload = codeSmell.computeIfAbsent(returnType, aClass -> new PackedPayload(returnType));
            packedPayload.put(invoke, out);

        } else if (returnType.isAssignableFrom(List.class)) {
            int fixupIndex = out.position();
            out.putInt(-1);
            GenericDeclaration x = (GenericDeclaration) returnType.getTypeParameters()[0];
            List list = (List) invoke;
            if (VIEWCLASSES.contains(x)) list.forEach(o -> VIEWSETTER.get(x).accept(out, o));
            else if (((Class) x).isEnum()) {
                list.forEach(o -> {
                    out.putShort((short) ((Enum) o).ordinal());
                });
            } else if (x.isAnnotationPresent(ProtoOrigin.class)) {
                list.forEach(o -> {
                    writeElement(out, (Class<?>) x, o);
                });
            }
            int i = out.position() - fixupIndex;
            out.putInt(fixupIndex,i);
        }
    }
}
    