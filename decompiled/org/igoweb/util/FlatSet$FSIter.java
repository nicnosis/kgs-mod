/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.util.Iterator;
import org.igoweb.util.FlatSet;

private class FlatSet.FSIter<TT>
implements Iterator<TT> {
    private int i = 0;
    private int iterEditCount = FlatSet.access$100(FlatSet.this);

    private FlatSet.FSIter() {
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
