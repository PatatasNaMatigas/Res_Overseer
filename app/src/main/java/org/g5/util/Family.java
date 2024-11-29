package org.g5.util;

import java.util.ArrayList;

public class Family<P, C> {

    private P parent;
    private ArrayList<C> children = new ArrayList<>(3);

    public void setParent(P parent) {
        this.parent = parent;
    }

    public P getParent() {
        return parent;
    }

    public void addChild(C child) {
        children.add(child);
    }

    public ArrayList<C> getChildren() {
        return children;
    }
}