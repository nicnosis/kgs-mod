/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

private static class CacheMap.SoftKeyRef<Key, Value>
extends SoftReference<Value> {
    public final Key key;

    public CacheMap.SoftKeyRef(Key newKey, Value value, ReferenceQueue<Value> q) {
        super(value, q);
        this.key = newKey;
    }
}
