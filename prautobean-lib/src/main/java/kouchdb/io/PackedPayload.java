package kouchdb.io;

import kouchdb.ann.ProtoNumber;
import kouchdb.ann.ProtoOrigin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

/**
 * takes autobean-like interface. writes {non-optional,optional}{booelan,other}as bitset, then length-prefixed bytes.  Lists are length-prefixed payloads
 *
 * @param <ProtoMessage>
 * @param <ClazProto>
 */
public class PackedPayload<ProtoMessage, ClazProto extends Class<ProtoMessage>> {
    /**
     * a nil holder
     */
    public static final byte[] EMPTY = new byte[0];
    public static final HashSet<Class> VIEWCLASSES = new HashSet<Class>(asList(long.class, double.class, int.class, float.class, short.class, byte.class));
    public static final Map<Class, Integer> VIEWSIZES = new HashMap<Class, Integer>() {{
        put(long.class, 8);
        put(double.class, 8);
        put(int.class, 4);
        put(float.class, 4);
        put(short.class, 2);
        put(byte.class, 1);
        put(boolean.class, 1);
    }};


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
            put(boolean.class, byteBuffer -> (int) byteBuffer.get() > 0);
        }

    };

    static Map<Class, PackedPayload> codeSmell = new LinkedHashMap<>();
    /**
     * non-optional booleans.   always present.
     */
    public List<Method> bool = new ArrayList<>();
    /**
     * optional bools.  may be null==false.
     */
    public List<Method> optbool = new ArrayList<>();
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
     *
     * @param theAutoBeanClass an-autobean like generated protobuf proxy interface
     */
    public PackedPayload(ClazProto theAutoBeanClass) {
        AtomicInteger nc = new AtomicInteger(0);
        asList(theAutoBeanClass.getDeclaredMethods()).forEach(method -> {
            if (null == method.getAnnotation(ProtoNumber.class)) return;
            kouchdb.ann.Optional annotation = method.getAnnotation(kouchdb.ann.Optional.class);
            boolean b2 = annotation == null;
            boolean b3 = method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class;
            List<Method> l = b2 ? b3 ? bool : nonOpt : b3 ? optbool : opt;
            l.add(method);
        });
        bitsetLen = bool.size()
                + optbool.size() + opt.size();
        BitSet bitSet = new BitSet(bitsetLen);
        bitSet.set(bitsetLen-1);

        bitsetBytes = bitSet.toByteArray().length;//bitset probably gets this right.

        init(theAutoBeanClass);
    }

    private static int putLeaf(ByteBuffer buffer, Class encodingType, Object data) {
        int fixup = buffer.position();
        if (VIEWSETTER.containsKey(encodingType)) VIEWSETTER.get(encodingType).accept(buffer, data);
        else if (List.class.isAssignableFrom(encodingType)) {
            buffer.getInt();
            for (Object o : (List) data) {
                putLeaf(buffer, o.getClass(), o);
            }
            buffer.putInt(fixup, buffer.position() - fixup);
        } else if (null != encodingType.getAnnotation(ProtoOrigin.class)) {
            try {
                PackedPayload packedPayload = codeSmell.get(encodingType);
                int put = packedPayload.put(data, buffer);//has own fixup
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        int value = buffer.position() - fixup;
        return value;
    }

    private static boolean isPackedObjectType(Class<?> encodingType) {
        return String.class == encodingType || byte[].class == encodingType || List.class.isAssignableFrom(encodingType) || null != encodingType.getAnnotation(ProtoOrigin.class);
    }

    private void init(ClazProto theAutoBean) {
        codeSmell.put(theAutoBean, this);
    }

    public int put(ProtoMessage p, ByteBuffer b) throws InvocationTargetException, IllegalAccessException {
        int fixup = b.position();
        BitSet bitSet1 = new BitSet(bitsetLen);
        int bitset = 0;
        for (Method method : bool) {
            try {
                if (Boolean.TRUE.equals(method.invoke(p)))
                    bitSet1.set(bitset++);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        for (Method method : optbool) {
            try {
                if (Boolean.TRUE.equals(method.invoke(p)))
                    bitSet1.set(bitset++);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        for (Method method : opt) {
            try {
                if (null != method.invoke(p))
                    bitSet1.set(bitset++);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        int position = b.position();
        for (Method method1 : nonOpt) {
            Class returnType1 = method1.getReturnType();
            Object invoke1 = method1.invoke(p);
            putLeaf(b, returnType1, invoke1);
        }


        for (Method method : opt) {
            Class returnType = method.getReturnType();
            Object data = method.invoke(p);
            if (data != null) putLeaf(b, returnType, data);
        }
        int i = b.position() - fixup;
        b.putInt(fixup, i);
        return i;
    }

    public ProtoMessage get(ClazProto proxyClass, ByteBuffer in) throws InvocationTargetException, IllegalAccessException {
        int fixup = in.getInt();
        int hold = in.limit();
        int position = in.position();
        BitSet bitSet = BitSet.valueOf((ByteBuffer) in.limit(position + bitsetBytes));
        byte[] bytes = new byte[in.remaining()];
        in.limit(hold);

        int skip = bool.size() + optbool.size();

        Map<Method, Integer> offsets = new LinkedHashMap<>();
        Map<Method, Object> values = new LinkedHashMap<>();

        Consumer<Method> harvest = method -> {
            offsets.put(method, in.position());
            Class<?> encodingType = method.getReturnType();

            Object r;
            if (VIEWCLASSES.contains(encodingType)) {
                r = VIEWGETTER.get(encodingType).apply(in);
                values.put(method, r);
            } else if (isPackedObjectType(encodingType)) {
                int position1 = in.position();
                offsets.put(method, position1);
                int offset = in.getInt();
                in.position(in.position() + offset);
            }
        };
        nonOpt.forEach(harvest);

        for (int i = 0, optSize = opt.size(); i < optSize; i++) {
            Method method = opt.get(i);
            if (bitSet.get(skip + i))
                harvest.accept(method);
        }
        Object o = Proxy.newProxyInstance(proxyClass.getClassLoader(), new Class[]{proxyClass}, (proxy, method, args) -> {
            Object r;
            if (values.containsKey(method)) {
                r= values.get(method);
            }
            r = descend(in, bitSet, offsets, method);

            values.put(method, r);
            return r;
        });
        return (ProtoMessage) o;
    }

    private Object descend(ByteBuffer b, BitSet bitSet, Map<Method, Integer> offsets, Method method) {
        Object r = null;
        if (bool.contains(method)) {
            r = bitSet.get(bool.indexOf(method));
        } else if (optbool.contains(method)) r = bitSet.get(optbool.indexOf(method) + bool.size());
        else {
            Integer integer = offsets.remove(method);
            if (null != integer) {
                b.position(integer);
                int size = b.getInt();
                Class encodingType = method.getReturnType();
                ByteBuffer slice = (ByteBuffer) b.slice().limit(size);
                if (VIEWGETTER.containsKey(encodingType))
                    r = VIEWGETTER.get(encodingType).apply(b);
                else if (List.class.isAssignableFrom(encodingType)) {
                    Class type = (Class) ((ParameterizedType) encodingType.getGenericInterfaces()[0]).getActualTypeArguments()[0];
                    if (VIEWCLASSES.contains(type)) {
                        r = new ReadOnlyBBList(type, size, b);
                    } else {
                        List arrayList = (List) (r = new ArrayList());
                        while (slice.hasRemaining()) arrayList.add(descend(slice, bitSet, offsets, method));
                    }
                }
            }
        }
        return r;
    }
}
