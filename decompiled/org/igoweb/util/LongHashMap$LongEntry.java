/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.util.Map;

private static class LongHashMap.LongEntry<V>
implements Map.Entry<Long, V> {
    LongHashMap.LongEntry<V> next;
    long key;
    V value;

    private LongHashMap.LongEntry(long key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Long getKey() {
        return new Long(this.key);
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
        return new Long(this.key).equals(peer.getKey()) && (this.value == null ? peer.getValue() == null : this.value.equals(peer.getValue()));
    }
}
