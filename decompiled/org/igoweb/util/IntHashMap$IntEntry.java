/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.util.Map;

private static class IntHashMap.IntEntry<V>
implements Map.Entry<Integer, V> {
    IntHashMap.IntEntry<V> next;
    int key;
    V value;

    private IntHashMap.IntEntry(int key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Integer getKey() {
        return new Integer(this.key);
    }

    @Override
    public V getValue() {
        return this.value;
    }

    @Override
    public V setValue(V value) {
        V oldVal = this.value;
        this.value = value;
        return oldVal;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Map.Entry)) {
            return false;
        }
        Map.Entry peer = (Map.Entry)o;
        return new Integer(this.key).equals(peer.getKey()) && (this.value == null ? peer.getValue() == null : this.value.equals(peer.getValue()));
    }
}
