/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.sgf;

import java.util.Iterator;
import org.igoweb.go.sgf.Node;
import org.igoweb.go.sgf.Prop;

private class Node.PropIter
implements Iterator<Prop> {
    private int i = -1;
    private int origSize;

    public Node.PropIter() {
        this.origSize = Node.this.numProps;
    }

    @Override
    public Prop next() {
        if (Node.this.numProps != this.origSize) {
            throw new RuntimeException();
        }
        return Node.this.props[++this.i];
    }

    @Override
    public boolean hasNext() {
        if (Node.this.numProps != this.origSize) {
            throw new RuntimeException();
        }
        return this.i + 1 < this.origSize;
    }

    @Override
    public void remove() {
        if (Node.this.numProps != this.origSize) {
            throw new RuntimeException();
        }
        Prop victim = Node.this.props[this.i];
        for (int j = this.i + 1; j < this.origSize; ++j) {
            ((Node)Node.this).props[j - 1] = Node.this.props[j];
        }
        --this.origSize;
        --Node.this.numProps;
        --this.i;
        Node.this.emit(1, victim);
        if (victim.type == 17 && Node.this.remove(new Prop(16, victim.getLoc()))) {
            --this.i;
            --this.origSize;
        }
    }
}
