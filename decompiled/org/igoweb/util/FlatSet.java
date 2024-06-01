/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

public class FlatSet<T>
extends AbstractSet<T>
implements Serializable {
    private Object[] elements;
    private int size;
    private int editCount;

    public FlatSet(Collection<? extends T> collection) {
        this.elements = new Object[collection.size() + 5];
        this.addAll(collection);
    }

    public FlatSet() {
        this.elements = new Object[5];
    }

    public FlatSet(int count) {
        this.elements = new Object[count];
    }

    @Override
    public Iterator<T> iterator() {
        return new FSIter();
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
    public boolean contains(Object o) {
        for (int i = 0; i < this.size; ++i) {
            if (!o.equals(this.elements[i])) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean add(T o) {
        if (this.contains(o)) {
            return false;
        }
        ++this.editCount;
        if (this.elements.length == this.size) {
            Object[] newEl = new Object[this.size * 2];
            System.arraycopy(this.elements, 0, newEl, 0, this.size);
            this.elements = newEl;
        }
        this.elements[this.size++] = o;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        for (int i = 0; i < this.size; ++i) {
            if (!o.equals(this.elements[i])) continue;
            ++this.editCount;
            this.elements[i] = this.elements[--this.size];
            this.elements[this.size] = null;
            return true;
        }
        return false;
    }

    private class FSIter<TT>
    implements Iterator<TT> {
        private int i = 0;
        private int iterEditCount = FlatSet.access$100(FlatSet.this);

        private FSIter() {
        }

        @Override
        public boolean hasNext() {
            if (FlatSet.this.editCount != this.iterEditCount) {
                throw new IllegalStateException();
            }
            return this.i < FlatSet.this.size;
        }

        @Override
        public TT next() {
            if (FlatSet.this.editCount != this.iterEditCount) {
                throw new IllegalStateException();
            }
            return (TT)FlatSet.this.elements[this.i++];
        }

        @Override
        public void remove() {
            if (FlatSet.this.editCount != this.iterEditCount) {
                throw new IllegalStateException();
            }
            ((FlatSet)FlatSet.this).elements[this.i--] = FlatSet.this.elements[--FlatSet.this.size];
            ((FlatSet)FlatSet.this).elements[((FlatSet)FlatSet.this).size] = null;
            FlatSet.this.editCount = ++this.iterEditCount;
        }
    }
}
