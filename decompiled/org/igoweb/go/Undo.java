/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.igoweb.go.Chain;
import org.igoweb.go.Loc;

public class Undo {
    public final Loc koLoc;
    public final int koColor;
    private HashSet<Chain> deadChains = null;
    private HashSet<Chain> newChains = null;
    private final int[] caps = new int[2];
    private static final Iterator<Chain> EMPTY_ITERATOR = new ArrayList().iterator();

    public Undo(int[] caps, Loc koLoc, int koColor) {
        this.koLoc = koLoc;
        this.koColor = koColor;
        this.caps[0] = caps[0];
        this.caps[1] = caps[1];
    }

    public final int getCaps(int color) {
        return this.caps[color];
    }

    public void addToDeadSet(Chain group) {
        if (this.newChains == null || !this.newChains.remove(group)) {
            if (this.deadChains == null) {
                this.deadChains = new HashSet();
            }
            this.deadChains.add(group);
        }
    }

    public void addToNewSet(Chain group) {
        if (this.deadChains == null || !this.deadChains.remove(group)) {
            if (this.newChains == null) {
                this.newChains = new HashSet();
            }
            this.newChains.add(group);
        }
    }

    public Iterator<Chain> getNewChains() {
        return this.getChainIterator(this.newChains);
    }

    public Iterator<Chain> getDeadChains() {
        return this.getChainIterator(this.deadChains);
    }

    private final Iterator<Chain> getChainIterator(Collection<Chain> collection) {
        return collection == null ? EMPTY_ITERATOR : collection.iterator();
    }

    public void combine(Undo successor) {
        if (this.deadChains == null) {
            this.deadChains = new HashSet();
        }
        if (this.newChains == null) {
            this.newChains = new HashSet();
        }
        if (successor.deadChains != null) {
            for (Chain group : successor.deadChains) {
                if (this.newChains.remove(group)) continue;
                this.deadChains.add(group);
            }
        }
        if (successor.newChains != null) {
            for (Chain group : successor.newChains) {
                if (this.deadChains.remove(group)) continue;
                this.newChains.add(group);
            }
        }
    }
}
