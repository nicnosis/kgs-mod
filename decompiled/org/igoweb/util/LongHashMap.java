/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LongHashMap<V>
extends AbstractMap<Long, V> {
    private static final int DEFAULT_CAPACITY = 7;
    private int size;
    private int growSize;
    volatile transient int modCount;
    private LongEntry<V>[] entries;

    public LongHashMap() {
        this(7);
    }

    public LongHashMap(int startingCapacity) {
        startingCapacity = (int)((double)startingCapacity / 0.75 + 0.9999) | 1;
        this.entries = new LongEntry[startingCapacity];
        this.growSize = (int)((double)startingCapacity * 0.75);
    }

    public LongHashMap(Map<Long, ? extends V> initMap) {
        this(initMap.size());
        this.putAll(initMap);
    }

    public boolean containsKey(long key) {
        LongEntry<V> bucket = this.entries[(int)((key & Long.MAX_VALUE) % (long)this.entries.length)];
        while (bucket != null) {
            if (bucket.key == key) {
                return true;
            }
            bucket = bucket.next;
        }
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return this.containsKey((Long)key);
    }

    @Override
    public V get(Object key) {
        return this.get((Long)key);
    }

    public V get(long key) {
        LongEntry<V> bucket = this.entries[(int)((key & Long.MAX_VALUE) % (long)this.entries.length)];
        while (bucket != null) {
            if (bucket.key == key) {
                return bucket.value;
            }
            bucket = bucket.next;
        }
        return null;
    }

    public V get(long key, V def) {
        V result = this.get(key);
        return result == null ? def : result;
    }

    @Override
    public V put(Long key, V value) {
        return this.put((long)key, value);
    }

    @Override
    public V put(long key, V value) {
        int bucket = (int)((key & Long.MAX_VALUE) % (long)this.entries.length);
        LongEntry<V> entry = this.entries[bucket];
        while (entry != null) {
            if (entry.key == key) {
                Object result = entry.value;
                entry.value = value;
                return result;
            }
            entry = entry.next;
        }
        ++this.modCount;
        if (this.size == this.growSize) {
            this.growCapacity();
            bucket = (int)((key & Long.MAX_VALUE) % (long)this.entries.length);
        }
        LongEntry newEntry = new LongEntry(key, value);
        newEntry.next = this.entries[bucket];
        this.entries[bucket] = newEntry;
        ++this.size;
        return null;
    }

    @Override
    public V remove(Object key) {
        return this.remove((Long)key);
    }

    public V remove(long key) {
        int bucket = (int)((key & Long.MAX_VALUE) % (long)this.entries.length);
        LongEntry<V> prevEntry = null;
        LongEntry<V> entry = this.entries[bucket];
        while (entry != null) {
            if (entry.key == key) {
                if (prevEntry == null) {
                    this.entries[bucket] = entry.next;
                } else {
                    prevEntry.next = entry.next;
                }
                --this.size;
                ++this.modCount;
                return entry.value;
            }
            prevEntry = entry;
            entry = entry.next;
        }
        return null;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public void clear() {
        ++this.modCount;
        Arrays.fill(this.entries, null);
        this.size = 0;
    }

    @Override
    public Set<Map.Entry<Long, V>> entrySet() {
        return new LongEntrySet();
    }

    private void growCapacity() {
        int newSize = this.entries.length * 2 + 1;
        LongEntry[] newEntries = new LongEntry[newSize];
        for (int bucket = 0; bucket < this.entries.length; ++bucket) {
            LongEntry<V> entry = this.entries[bucket];
            while (entry != null) {
                LongEntry nextEntry = entry.next;
                int newBucket = (int)((entry.key & Long.MAX_VALUE) % (long)newSize);
                entry.next = newEntries[newBucket];
                newEntries[newBucket] = entry;
                entry = nextEntry;
            }
        }
        this.entries = newEntries;
        this.growSize = (int)((double)newSize * 0.75);
    }

    private class EntryIterator
    implements Iterator<Map.Entry<Long, V>> {
        int bucket;
        LongEntry<V> prevEntry;
        LongEntry<V> entry;
        int expectedModCount;

        public EntryIterator() {
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
        public LongEntry<V> next() {
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

    private class LongEntrySet
    extends AbstractSet<Map.Entry<Long, V>> {
        @Override
        public Iterator<Map.Entry<Long, V>> iterator() {
            return new EntryIterator();
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

    private static class LongEntry<V>
    implements Map.Entry<Long, V> {
        LongEntry<V> next;
        long key;
        V value;

        private LongEntry(long key, V value) {
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
}
