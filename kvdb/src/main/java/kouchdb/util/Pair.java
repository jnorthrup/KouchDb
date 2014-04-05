package kouchdb.util;

/**
 * Simple pair class.
 *
 * @param <A> any type
 * @param <B> any type
 */
public class Pair<A, B> {
    private final A a;
    private final B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (this != o) {
            if (o instanceof Pair) {

                Pair pair = (Pair) o;

                return !((null != a ? !a.equals(pair.a) : null != pair.a) || (null != b ? !b.equals(pair.b)
                        : null != pair.b));

            }
            return false;
        }
        return true;
    }

}
