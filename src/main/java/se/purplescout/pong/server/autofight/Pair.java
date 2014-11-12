package se.purplescout.pong.server.autofight;

import java.util.Objects;

class Pair<A, B> {

    final A a;
    final B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash += Objects.hashCode(this.a);
        hash += Objects.hashCode(this.b);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair<?, ?> other = (Pair<?, ?>) obj;

        if (other.a == this.a && other.b == this.b) {
            return true;
        }

        if (other.a == this.b && other.b == this.a) {
            return true;
        }

        return false;
    }
}
