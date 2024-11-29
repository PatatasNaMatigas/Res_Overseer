package org.g5.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class TriMap<K, V1, V2> {

    private ArrayList<Family<K, Pair<V1, V2>>> triMap;
    private HashMap<K, Integer> keyID;

    public TriMap() {
        triMap = new ArrayList<>(20);
        keyID = new HashMap<>(20);
    }

    public TriMap(int initialSize) {
        triMap = new ArrayList<>(initialSize);
        keyID = new HashMap<>(initialSize);
    }

    public void newEntry(K key, V1 value1, V2 value2) {
        if (keyID.containsKey(key)) {
            addValue(key, value1, value2);
            return;
        }

        Pair<V1, V2> pair = new Pair<>();
        pair.setPair(value1, value2);
        Family<K, Pair<V1, V2>> childParent = new Family<>();
        childParent.setParent(key);
        childParent.addChild(pair);
        triMap.add(childParent);
        keyID.put(key, keyID.size());
    }

    private void addValue(K key, V1 value1, V2 value2) {
        Pair<V1, V2> pair = new Pair<>();
        pair.setPair(value1, value2);

        ArrayList<Pair<V1, V2>> children =
                triMap.get(keyID.get(key)).getChildren();
        if (!children.contains(pair)) {
            triMap.get(keyID.get(key)).addChild(pair);
        }
    }

    public Family<K, Pair<V1, V2>> getEntry(K key) {
        return triMap.get(keyID.get(key));
    }

    public List<K> getKeys() {
        ArrayList<K> keys = new ArrayList<>(triMap.size());

        for (int i = 0; i < triMap.size(); i++) {
            keys.add(triMap.get(i).getParent());
        }

        return Collections.unmodifiableList(keys);
    }

    public int size() {
        return triMap.size();
    }

    public boolean isEmpty() {
        return triMap.isEmpty();
    }

    public void clear() {
        triMap.clear();
        keyID.clear();
    }

    public static <A, B, C> int hash(A a, B b, C c) {
        return Objects.hash(a, b, c);
    }
}