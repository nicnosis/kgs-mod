/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import org.igoweb.util.LongHashMap;

private class LongHashMap.LongEntrySet
extends AbstractSet<Map.Entry<Long, V>> {
    @Override
    public Iterator<Map.Entry<Long, V>> iterator() {
        return new LongHashMap.EntryIterator(LongHashMap.this);
    }

    @Override
    public int size() {
        return LongHashMap.this.size;
    }

    @Override
    public void clear() {
        LongHashMap.this.clear();
    }
}
