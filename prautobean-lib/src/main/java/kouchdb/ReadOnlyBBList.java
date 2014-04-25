package kouchdb;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

/**
* Created by jim on 4/24/14.
*/
class ReadOnlyBBList implements List {
    private final Class type;
    private final int size;
    private final ByteBuffer b;
    Function<ByteBuffer, Object> byteBufferObjectFunction;
    Integer per;

    private Integer vsize;

    public ReadOnlyBBList(Class type, int size, ByteBuffer b) {
        this.type = type;
        this.size = size;
        this.b = b;
        byteBufferObjectFunction = PackedPayload.VIEWGETTER.get(type);
        per = PackedPayload.VIEWSIZES.get(type);
        vsize = PackedPayload.VIEWSIZES.get(type);
    }

    @Override
    public int size() {
        return size / per;
    }

    @Override
    public boolean isEmpty() {
        return size > per;
    }

    @Override
    public boolean contains(Object o) {
        b.rewind();
        while (b.hasRemaining()) {
            Object apply = byteBufferObjectFunction.apply(b);
            if (apply.equals(o)) return true;
        }

        return false;
    }

    @Override
    public Iterator iterator() {
        ByteBuffer ib = (ByteBuffer) b.duplicate().rewind();
        return new Iterator() {
            @Override
            public boolean hasNext() {
                return ib.hasRemaining();
            }

            @Override
            public Object next() {
                return byteBufferObjectFunction.apply(ib);
            }
        };
    }

    @Override
    public Object[] toArray() {

        Object[] objects = new Object[size];
        ByteBuffer ib = (ByteBuffer) b.duplicate().rewind();
        int c = 0;
        while (ib.hasRemaining()) objects[c++] = byteBufferObjectFunction.apply(ib);
        return objects;

    }

    @Override
    public boolean add(Object o) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean addAll(Collection c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection c) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public Object get(int index) {
        return byteBufferObjectFunction.apply((ByteBuffer) b.duplicate().position(index * vsize));
    }

    @Override
    public Object set(int index, Object element) {
        return null;
    }

    @Override
    public void add(int index, Object element) {

    }

    @Override
    public Object remove(int index) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        contains(o);
        return b.position()/per;
    }

    @Override
    public int lastIndexOf(Object o) {

        return 0;
    }

    @Override
    public ListIterator listIterator() {
        return null;
    }

    @Override
    public ListIterator listIterator(int index) {
        return null;
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        return null;
    }

    @Override
    public boolean retainAll(Collection c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection c) {
        return false;
    }

    @Override
    public boolean containsAll(Collection c) {
        return false;
    }

    @Override
    public Object[] toArray(Object[] a) {
        return toArray();
    }
}
