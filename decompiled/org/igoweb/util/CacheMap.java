/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class CacheMap<Key, Value> {
    private final boolean isWeak;
    private final HashMap<Key, Reference<Value>> subMap = new HashMap();
    private final ReferenceQueue<Value> refQ = new ReferenceQueue();

    public CacheMap() {
        this.isWeak = false;
    }

    public CacheMap(boolean newIsWeak) {
        this.isWeak = newIsWeak;
    }

    public void clear() {
        this.subMap.clear();
    }

    public Value get(Object key) {
        Reference<Value> ref = this.subMap.get(key);
        return ref == null ? null : (Value)ref.get();
    }

    public Value put(Key key, Value value) {
        this.cleanup();
        Reference ref = this.isWeak ? new WeakKeyRef<Key, Value>(key, value, this.refQ) : new SoftKeyRef<Key, Value>(key, value, this.refQ);
        ref = this.subMap.put(key, ref);
        return ref == null ? null : (Value)ref.get();
    }

    public Value remove(Key key) {
        this.cleanup();
        Reference<Value> ref = this.subMap.remove(key);
        return ref == null ? null : (Value)ref.get();
    }

    private void cleanup() {
        Reference<Value> deadRef;
        while ((deadRef = this.refQ.poll()) != null) {
            Object k = this.isWeak ? ((WeakKeyRef)deadRef).key : ((SoftKeyRef)deadRef).key;
            if (this.subMap.get(k) != deadRef) continue;
            this.subMap.remove(k);
        }
        return;
    }

    private static class SoftKeyRef<Key, Value>
    extends SoftReference<Value> {
        public final Key key;

        public SoftKeyRef(Key newKey, Value value, ReferenceQueue<Value> q) {
            super(value, q);
            this.key = newKey;
        }
    }

    private static class WeakKeyRef<Key, Value>
    extends WeakReference<Value> {
        public final Key key;

        public WeakKeyRef(Key newKey, Value value, ReferenceQueue<Value> q) {
            super(value, q);
            this.key = newKey;
        }
    }
}
