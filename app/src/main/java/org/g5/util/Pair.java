package org.g5.util;

public class Pair<V1, V2> {

    V1 value1;
    V2 value2;

    public Pair() {}

    public Pair(V1 value1, V2 value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public void setPair(V1 value1, V2 value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public V1 getValue1() {
        return value1;
    }

    public V2 getValue2() {
        return value2;
    }

    public boolean bothEmpty() {
        return value1 == null || value2 == null;
    }
}
