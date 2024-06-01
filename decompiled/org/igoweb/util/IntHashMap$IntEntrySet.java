/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import org.igoweb.util.IntHashMap;

private class IntHashMap.IntEntrySet
extends AbstractSet<Map.Entry<Integer, V>> {
    @Override
    public Iterator<Map.Entry<Integer, V>> iterator() {
        return new IntHashMap.EntryIterator(IntHashMap.this);
    }

    @Override
    public int size() {
        return IntHashMap.this.size;
    }

    @Override
    public void clear() {
        IntHashMap.this.clear();
    }
}
