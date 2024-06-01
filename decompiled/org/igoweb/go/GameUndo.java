/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import java.util.ArrayList;
import java.util.Iterator;
import org.igoweb.go.GobanSnapshot;
import org.igoweb.go.Loc;
import org.igoweb.go.Undo;

class GameUndo
extends Undo {
    public final int whoseMove;
    public final int moveNum;
    public final Loc lastMove;
    private ArrayList<GobanSnapshot> snapshots = null;
    public final int whoMadeLastMove;
    public final int numPasses;
    private static final Iterator<GobanSnapshot> EMPTY_SNAPSHOT_ITERATOR = new ArrayList().iterator();

    public GameUndo(int[] caps, Loc koLoc, int koColor, int whoseMove, int moveNum, int whoMadeLastMove, Loc lastMove, int numPasses) {
        super(caps, koLoc, koColor);
        this.whoseMove = whoseMove;
        this.moveNum = moveNum;
        this.whoMadeLastMove = whoMadeLastMove;
        this.lastMove = lastMove;
        this.numPasses = numPasses;
    }

    public Iterator<GobanSnapshot> getSnapshots() {
        return this.snapshots == null ? EMPTY_SNAPSHOT_ITERATOR : this.snapshots.iterator();
    }

    public void addSnapshot(GobanSnapshot snapshot) {
        if (this.snapshots == null) {
            this.snapshots = new ArrayList(1);
        }
        this.snapshots.add(snapshot);
    }

    @Override
    public void combine(Undo successor) {
        this.combine((GameUndo)successor);
    }

    public void combine(GameUndo successor) {
        super.combine(successor);
        if (this.snapshots == null) {
            this.snapshots = successor.snapshots;
        } else if (successor.snapshots != null) {
            this.snapshots.addAll(successor.snapshots);
        }
    }
}
