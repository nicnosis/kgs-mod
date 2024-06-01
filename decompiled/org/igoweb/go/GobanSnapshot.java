/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import java.util.Arrays;
import org.igoweb.go.Goban;
import org.igoweb.go.Rules;

public class GobanSnapshot {
    private final int[] stones;
    private final int capDiff;
    private final int hashCode;
    private final int whoseMove;

    public GobanSnapshot(Goban g, Rules rules, int newWhoseMove) {
        this.stones = g.getBoardMap();
        this.hashCode = g.hashCode();
        this.whoseMove = rules.getKoType() == 1 ? 2 : newWhoseMove;
        this.capDiff = rules.getType() == 0 ? g.caps(1) - g.caps(0) : 0;
    }

    public int hashCode() {
        return this.hashCode + this.whoseMove + (this.capDiff << 8);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !obj.getClass().equals(this.getClass())) {
            return false;
        }
        GobanSnapshot peer = (GobanSnapshot)obj;
        return this.whoseMove == peer.whoseMove && this.capDiff == peer.capDiff && Arrays.equals(this.stones, peer.stones);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("GobanSnapshot[wm=");
        sb.append(this.whoseMove);
        for (int stone : this.stones) {
            sb.append(',').append(stone);
        }
        return sb.append(']').toString();
    }
}
