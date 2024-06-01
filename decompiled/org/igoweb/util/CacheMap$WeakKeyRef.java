/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

private static class CacheMap.WeakKeyRef<Key, Value>
extends WeakReference<Value> {
    public final Key key;

    public CacheMap.WeakKeyRef(Key newKey, Value value, ReferenceQueue<Value> q) {
        super(value, q);
        this.key = newKey;
    }
}
