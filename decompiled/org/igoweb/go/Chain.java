/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Iterator;
import org.igoweb.go.Loc;

public class Chain
extends AbstractCollection<Loc> {
    public final int color;
    private int numLibs;
    private final Loc[] members;
    private final Loc[] libs;
    private final int hashCode;

    public Chain(int color, Loc loc) {
        this.color = color;
        this.numLibs = 0;
        this.members = new Loc[1];
        this.members[0] = loc;
        this.libs = new Loc[4];
        this.hashCode = color * -1640524983 + loc.hashCode();
    }

    public Chain(Chain a, Chain b) {
        int cmpResult;
        if (a.color != b.color) {
            throw new IllegalArgumentException("Can't merge groups of different color.");
        }
        int hashResult = this.color = a.color;
        this.members = new Loc[a.size() + b.size()];
        int i = this.members.length;
        int ai = a.size() - 1;
        int bi = b.size() - 1;
        while (true) {
            Loc loc;
            if ((cmpResult = a.members[ai].compareTo(b.members[bi])) == 0) {
                throw new IllegalArgumentException("Can't merge groups that contain the same stone.");
            }
            if (cmpResult > 0) {
                loc = a.members[ai];
                this.members[--i] = loc;
                hashResult = hashResult * -1640524983 + loc.hashCode();
                if (ai == 0) {
                    System.arraycopy(b.members, 0, this.members, 0, bi + 1);
                    break;
                }
                --ai;
                continue;
            }
            loc = b.members[bi];
            this.members[--i] = loc;
            hashResult = hashResult * -1640524983 + loc.hashCode();
            if (bi == 0) {
                System.arraycopy(a.members, 0, this.members, 0, ai + 1);
                break;
            }
            --bi;
        }
        while (i > 0) {
            hashResult = hashResult * -1640524983 + this.members[--i].hashCode();
        }
        this.hashCode = hashResult;
        this.libs = new Loc[this.members.length * 2 + 2];
        this.numLibs = 0;
        ai = 0;
        bi = 0;
        while (true) {
            if (ai >= a.numLibs) {
                System.arraycopy(b.libs, bi, this.libs, this.numLibs, b.numLibs - bi);
                this.numLibs += b.numLibs - bi;
                return;
            }
            if (bi >= b.numLibs) {
                System.arraycopy(a.libs, ai, this.libs, this.numLibs, a.numLibs - ai);
                this.numLibs += a.numLibs - ai;
                return;
            }
            cmpResult = a.libs[ai].compareTo(b.libs[bi]);
            if (cmpResult < 0) {
                this.libs[this.numLibs++] = a.libs[ai++];
                continue;
            }
            if (cmpResult == 0) {
                this.libs[this.numLibs++] = a.libs[ai++];
                ++bi;
                continue;
            }
            this.libs[this.numLibs++] = b.libs[bi++];
        }
    }

    public Chain(Chain a) {
        this.color = a.color;
        this.numLibs = a.numLibs;
        this.members = new Loc[a.members.length];
        System.arraycopy(a.members, 0, this.members, 0, this.members.length);
        this.libs = new Loc[a.libs.length];
        System.arraycopy(a.libs, 0, this.libs, 0, a.libs.length);
        this.hashCode = a.hashCode;
    }

    public void addLib(Loc loc) {
        int lo = 0;
        int hi = this.numLibs;
        while (lo != hi) {
            int med = lo + hi >> 1;
            int cmpResult = this.libs[med].compareTo(loc);
            if (cmpResult == 0) {
                return;
            }
            if (cmpResult < 0) {
                lo = med + 1;
                continue;
            }
            hi = med;
        }
        if (lo < this.numLibs) {
            if (this.libs[lo].equals(loc)) {
                return;
            }
            System.arraycopy(this.libs, lo, this.libs, lo + 1, this.numLibs - lo);
        }
        this.libs[lo] = loc;
        ++this.numLibs;
    }

    @Override
    public String toString() {
        int i;
        StringBuilder sb = new StringBuilder(this.color == 0 ? "Chain[B, " : "Chain[W, ");
        for (i = 0; i < this.members.length; ++i) {
            sb.append(this.members[i]).append(',');
        }
        sb.append(" libs=");
        for (i = 0; i < this.numLibs; ++i) {
            sb.append(this.libs[i]);
            if (i + 1 >= this.numLibs) continue;
            sb.append(',');
        }
        return sb.append(']').toString();
    }

    public final void clearLibs() {
        this.numLibs = 0;
    }

    public void rmLib(Loc loc) {
        int lo = 0;
        int hi = this.numLibs;
        while (lo != hi) {
            int med = lo + hi >> 1;
            int cmpResult = this.libs[med].compareTo(loc);
            if (cmpResult == 0) {
                lo = med;
                break;
            }
            if (cmpResult < 0) {
                lo = med + 1;
                continue;
            }
            hi = med;
        }
        if (lo < this.numLibs && this.libs[lo].equals(loc)) {
            --this.numLibs;
            System.arraycopy(this.libs, lo + 1, this.libs, lo, this.numLibs - lo);
        }
    }

    public final int countLiberties() {
        return this.numLibs;
    }

    public final Loc getMember() {
        return this.members[0];
    }

    @Override
    public final Iterator<Loc> iterator() {
        return new LocIter(this.members, this.members.length);
    }

    public final Loc getLiberty() {
        if (this.numLibs == 0) {
            return null;
        }
        return this.libs[0];
    }

    public final Iterator<Loc> libertyIterator() {
        return new LocIter(this.libs, this.numLibs);
    }

    @Override
    public final int hashCode() {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Chain peer = (Chain)obj;
        return peer.color == this.color && Arrays.equals(this.members, peer.members);
    }

    @Override
    public final int size() {
        return this.members.length;
    }

    private static class LocIter
    implements Iterator<Loc> {
        final int limit;
        final Loc[] locs;
        int current;

        public LocIter(Loc[] locs, int numLocs) {
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
}
