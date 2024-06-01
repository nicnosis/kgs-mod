/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import org.igoweb.util.IntHashMap;

private class IntHashMap.EntryIterator
implements Iterator<Map.Entry<Integer, V>> {
    int bucket;
    IntHashMap.IntEntry<V> prevEntry;
    IntHashMap.IntEntry<V> entry;
    int expectedModCount;

    public IntHashMap.EntryIterator() {
        this.expectedModCount = IntHashMap.this.modCount;
        this.entry = null;
        this.bucket = 0;
        while (this.bucket < IntHashMap.this.entries.length) {
            if (IntHashMap.this.entries[this.bucket] != null) {
                this.entry = IntHashMap.this.entries[this.bucket];
                return;
            }
            ++this.bucket;
        }
    }

    @Override
    public boolean hasNext() {
        return this.entry != null;
    }

    @Override
    public IntHashMap.IntEntry<V> next() {
        if (IntHashMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
        }
        this.prevEntry = this.entry;
        if (this.entry != null) {
            this.entry = this.entry.next;
            while (this.entry == null && ++this.bucket < IntHashMap.this.entries.length) {
                this.entry = IntHashMap.this.entries[this.bucket];
            }
        }
        return this.prevEntry;
    }

    @Override
    public void remove() {
        if (this.prevEntry == null) {
            throw new IllegalStateException();
        }
        if (IntHashMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
        }
        IntHashMap.this.remove(this.prevEntry.key);
        this.prevEntry = null;
        this.expectedModCount = IntHashMap.this.modCount;
    }
}
