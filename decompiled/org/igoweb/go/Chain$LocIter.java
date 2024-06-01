/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import java.util.Iterator;
import org.igoweb.go.Loc;

private static class Chain.LocIter
implements Iterator<Loc> {
    final int limit;
    final Loc[] locs;
    int current;

    public Chain.LocIter(Loc[] locs, int numLocs) {
        this.locs = locs;
        this.limit = numLocs;
        this.current = 0;
    }

    @Override
    public boolean hasNext() {
        return this.current < this.limit;
    }

    @Override
    public Loc next() {
        return this.locs[this.current++];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
