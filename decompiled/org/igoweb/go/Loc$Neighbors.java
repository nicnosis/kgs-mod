/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import java.util.Iterator;
import org.igoweb.go.Loc;

private class Loc.Neighbors
implements Iterator<Loc> {
    private int i = 0;
    private final int validMask;

    public Loc.Neighbors(int boardSize) {
        int vm = 0;
        if (Loc.this.y > 0) {
            vm |= 1;
        }
        if (Loc.this.x > 0) {
            vm |= 2;
        }
        if (Loc.this.x < boardSize - 1) {
            vm |= 4;
        }
        if (Loc.this.y < boardSize - 1) {
            vm |= 8;
        }
        this.validMask = vm;
    }

    @Override
    public boolean hasNext() {
        return 1 << this.i <= this.validMask;
    }

    @Override
    public Loc next() {
        while ((this.validMask & 1 << this.i) == 0) {
            if (++this.i <= 4) continue;
            throw new IllegalStateException();
        }
        return prebuilt[Loc.this.x + Loc.this.y * 38 + vectors[this.i++]];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
