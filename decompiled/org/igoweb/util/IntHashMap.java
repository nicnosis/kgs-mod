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

public class IntHashMap<V>
extends AbstractMap<Integer, V> {
    private static final int DEFAULT_CAPACITY = 7;
    private int size;
    private int growSize;
    volatile transient int modCount;
    private IntEntry<V>[] entries;

    public IntHashMap() {
        this(7);
    }

    public IntHashMap(int startingCapacity) {
        startingCapacity = (int)((double)startingCapacity / 0.75 + 0.9999) | 1;
        this.entries = new IntEntry[startingCapacity];
        this.growSize = (int)((double)startingCapacity * 0.75);
    }

    public IntHashMap(Map<Integer, ? extends V> initMap) {
        this(initMap.size());
        this.putAll(initMap);
    }

    public boolean containsKey(int key) {
        IntEntry<V> bucket = this.entries[(key & Integer.MAX_VALUE) % this.entries.length];
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
        return this.containsKey((Integer)key);
    }

    @Override
    public V get(Object key) {
        return this.get((Integer)key);
    }

    public V get(int key) {
        IntEntry<V> bucket = this.entries[(key & Integer.MAX_VALUE) % this.entries.length];
        while (bucket != null) {
            if (bucket.key == key) {
                return bucket.value;
            }
            bucket = bucket.next;
        }
        return null;
    }

    @Override
    public V put(Integer key, V value) {
        return this.put((int)key, value);
    }

    @Override
    public V put(int key, V value) {
        int bucket = (key & Integer.MAX_VALUE) % this.entries.length;
        IntEntry<V> entry = this.entries[bucket];
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
            bucket = (key & Integer.MAX_VALUE) % this.entries.length;
        }
        IntEntry newEntry = new IntEntry(key, value);
        newEntry.next = this.entries[bucket];
        this.entries[bucket] = newEntry;
        ++this.size;
        return null;
    }

    @Override
    public V remove(Object key) {
        return this.remove((Integer)key);
    }

    public V remove(int key) {
        int bucket = (key & Integer.MAX_VALUE) % this.entries.length;
        IntEntry<V> prevEntry = null;
        IntEntry<V> entry = this.entries[bucket];
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
    public Set<Map.Entry<Integer, V>> entrySet() {
        return new IntEntrySet();
    }

    private void growCapacity() {
        int newSize = this.entries.length * 2 + 1;
        IntEntry[] newEntries = new IntEntry[newSize];
        for (int bucket = 0; bucket < this.entries.length; ++bucket) {
            IntEntry<V> entry = this.entries[bucket];
            while (entry != null) {
                IntEntry nextEntry = entry.next;
                int newBucket = (entry.key & Integer.MAX_VALUE) % newSize;
                entry.next = newEntries[newBucket];
                newEntries[newBucket] = entry;
                entry = nextEntry;
            }
        }
        this.entries = newEntries;
        this.growSize = (int)((double)newSize * 0.75);
    }

    private class EntryIterator
    implements Iterator<Map.Entry<Integer, V>> {
        int bucket;
        IntEntry<V> prevEntry;
        IntEntry<V> entry;
        int expectedModCount;

        public EntryIterator() {
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
        public IntEntry<V> next() {
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

    private class IntEntrySet
    extends AbstractSet<Map.Entry<Integer, V>> {
        @Override
        public Iterator<Map.Entry<Integer, V>> iterator() {
            return new EntryIterator();
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

    private static class IntEntry<V>
    implements Map.Entry<Integer, V> {
        IntEntry<V> next;
        int key;
        V value;

        private IntEntry(int key, V value) {
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
}
