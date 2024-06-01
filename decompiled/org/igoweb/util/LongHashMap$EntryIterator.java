/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import org.igoweb.util.LongHashMap;

private class LongHashMap.EntryIterator
implements Iterator<Map.Entry<Long, V>> {
    int bucket;
    LongHashMap.LongEntry<V> prevEntry;
    LongHashMap.LongEntry<V> entry;
    int expectedModCount;

    public LongHashMap.EntryIterator() {
        this.expectedModCount = LongHashMap.this.modCount;
        this.entry = null;
        this.bucket = 0;
        while (this.bucket < LongHashMap.this.entries.length) {
            if (LongHashMap.this.entries[this.bucket] != null) {
                this.entry = LongHashMap.this.entries[this.bucket];
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
    public LongHashMap.LongEntry<V> next() {
        if (LongHashMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
        }
        this.prevEntry = this.entry;
        if (this.entry != null) {
            this.entry = this.entry.next;
            while (this.entry == null && ++this.bucket < LongHashMap.this.entries.length) {
                this.entry = LongHashMap.this.entries[this.bucket];
            }
        }
        return this.prevEntry;
    }

    @Override
    public void remove() {
        if (this.prevEntry == null) {
            throw new IllegalStateException();
        }
        if (LongHashMap.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
        }
        LongHashMap.this.remove(this.prevEntry.key);
        this.prevEntry = null;
        this.expectedModCount = LongHashMap.this.modCount;
    }
}
