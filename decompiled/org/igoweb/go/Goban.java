/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import org.igoweb.go.Chain;
import org.igoweb.go.Go;
import org.igoweb.go.Loc;
import org.igoweb.go.Undo;
import org.igoweb.util.Emitter;

public class Goban
extends Emitter {
    public static final int CHANGE_STONE_EVENT = 0;
    public static final int CHANGE_CAPTURES_EVENT = 1;
    public static final int MAX_GOBAN_EVENT = 1;
    public static final int MIN_SIZE = 2;
    public static final int MAX_SIZE = 38;
    private final int[][] stones;
    private static final long[][][] hashCodes = Goban.computeHashCodes();
    private final Chain[][] groups;
    public final int size;
    private long hashCode = 0L;
    protected int[] captures = new int[2];
    private Loc koLoc = null;
    private int koColor;
    private final Loc[] allLocs;

    public Goban(int newSize) {
        this.size = newSize;
        this.stones = new int[newSize][newSize];
        this.groups = new Chain[newSize][newSize];
        this.allLocs = new Loc[newSize * newSize];
        this.clearBoard();
        for (int j = 0; j < newSize; ++j) {
            for (int i = 0; i < newSize; ++i) {
                this.allLocs[i + j * newSize] = Loc.get(i, j);
            }
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder("Goban[\n");
        this.printLetters(s);
        String[] pics = new String[]{"# ", "O ", ". "};
        for (int i = 0; i < this.size; ++i) {
            if (this.size - i < 10) {
                s.append(' ');
            }
            s.append(this.size - i).append("  ");
            for (int j = 0; j < this.size; ++j) {
                s.append(pics[this.stones[j][i]]);
            }
            if (this.size - i < 10) {
                s.append(' ');
            }
            s.append("  ").append(this.size - i).append('\n');
        }
        this.printLetters(s);
        s.append("Captures: W=").append(this.captures[1]).append(", B=").append(this.captures[0]);
        if (this.koLoc != null) {
            s.append(", ko=").append(this.koLoc);
        }
        return s.append(']').toString();
    }

    private void printLetters(StringBuilder s) {
        s.append("    ");
        for (int j = 1; j <= this.size; ++j) {
            char c = (char)(97 + j - 1);
            if (c >= 'i') {
                c = (char)(c + '\u0001');
            }
            s.append(c).append(' ');
        }
        s.append('\n');
    }

    protected boolean addStone(Loc loc, int color, boolean countCaptures) {
        return this.addStone(loc, color, countCaptures, true, null);
    }

    public boolean addStone(Loc loc, int color, boolean countCaptures, Undo undo) {
        return this.addStone(loc, color, countCaptures, true, undo);
    }

    protected boolean addStone(Loc loc, int color, boolean countCaptures, boolean notifyListeners, Undo undo) {
        if (loc.x < 0 || loc.x >= this.size || loc.y < 0 || loc.y >= this.size || color != 0 && color != 1) {
            throw new RuntimeException();
        }
        if (this.getColor(loc) != 2) {
            return false;
        }
        this.koLoc = null;
        int oppColor = Go.opponent(color);
        Chain lg = new Chain(color, loc);
        if (undo != null) {
            undo.addToNewSet(lg);
        }
        this.set(loc, color, lg, notifyListeners);
        boolean reportCapturesChanged = false;
        Iterator<Loc> neighbors = loc.neighbors(this.size);
        while (neighbors.hasNext()) {
            Chain ng;
            Loc n = neighbors.next();
            if (this.getColor(n) == 2) {
                lg.addLib(n);
                continue;
            }
            if (this.getColor(n) == color) {
                ng = this.getChain(n);
                if (ng == lg) continue;
                ng.rmLib(loc);
                Chain newLg = new Chain(lg, ng);
                if (undo != null) {
                    undo.addToDeadSet(lg);
                    undo.addToDeadSet(ng);
                    undo.addToNewSet(newLg);
                }
                lg = newLg;
                for (Loc aLg : lg) {
                    this.set(aLg, lg);
                }
                continue;
            }
            if (this.getColor(n) != oppColor) continue;
            ng = this.getChain(n);
            ng.rmLib(loc);
            if (ng.countLiberties() != 0) continue;
            if (ng.size() == 1) {
                this.koLoc = ng.getMember();
                this.koColor = oppColor;
            }
            if (undo != null) {
                undo.addToDeadSet(ng);
            }
            if (countCaptures) {
                reportCapturesChanged = notifyListeners;
                int n2 = color;
                this.captures[n2] = this.captures[n2] + ng.size();
            }
            this.eraseChain(ng, notifyListeners);
        }
        if (lg.countLiberties() == 0) {
            this.eraseChain(lg, notifyListeners);
            if (undo != null) {
                undo.addToDeadSet(lg);
            }
            if (countCaptures) {
                reportCapturesChanged = notifyListeners;
                int n = oppColor;
                this.captures[n] = this.captures[n] + lg.size();
            }
        }
        if (this.koLoc != null && (this.getChain(loc).countLiberties() != 1 || this.getChain(loc).size() != 1)) {
            this.koLoc = null;
        }
        if (reportCapturesChanged) {
            this.emit(1);
        }
        return true;
    }

    private void addChain(Chain g, boolean notifyListeners) {
        g.clearLibs();
        for (Loc aG : g) {
            this.set(aG, g.color, g, notifyListeners);
        }
        for (Loc member : g) {
            Iterator<Loc> mns = member.neighbors(this.size);
            while (mns.hasNext()) {
                Loc mn = mns.next();
                if (this.getColor(mn) == 2) {
                    g.addLib(mn);
                    continue;
                }
                Chain ng = this.getChain(mn);
                if (ng == null || ng == g) continue;
                ng.rmLib(member);
            }
        }
    }

    private void eraseChain(Chain g, boolean notifyListeners) {
        g.clearLibs();
        for (Loc member : g) {
            this.set(member, 2, null, notifyListeners);
            Iterator<Loc> mns = member.neighbors(this.size);
            while (mns.hasNext()) {
                Loc mn = mns.next();
                Chain ng = this.getChain(mn);
                if (ng == null || ng.equals(g)) continue;
                ng.addLib(member);
            }
        }
    }

    public void undo(Undo undo) {
        this.undo(undo, true);
    }

    protected void undo(Undo undo, boolean notifyListeners) {
        this.koLoc = undo.koLoc;
        this.koColor = undo.koColor;
        if (this.captures[0] != undo.getCaps(0) || this.captures[1] != undo.getCaps(1)) {
            this.captures[0] = undo.getCaps(0);
            this.captures[1] = undo.getCaps(1);
            if (notifyListeners) {
                this.emit(1);
            }
        }
        Iterator<Chain> groupIter = undo.getNewChains();
        while (groupIter.hasNext()) {
            this.eraseChain(groupIter.next(), notifyListeners);
        }
        groupIter = undo.getDeadChains();
        while (groupIter.hasNext()) {
            this.addChain(groupIter.next(), notifyListeners);
        }
    }

    public final int getColor(Loc loc) {
        return this.stones[loc.x][loc.y];
    }

    public final int getColor(int x, int y) {
        return this.stones[x][y];
    }

    public final Chain getChain(Loc loc) {
        return this.groups[loc.x][loc.y];
    }

    private void set(Loc loc, Chain group) {
        this.groups[loc.x][loc.y] = group;
    }

    private void set(Loc loc, int color, Chain group, boolean notifyListeners) {
        this.groups[loc.x][loc.y] = group;
        if (color != this.stones[loc.x][loc.y]) {
            this.hashCode ^= hashCodes[loc.x][loc.y][color] ^ hashCodes[loc.x][loc.y][this.stones[loc.x][loc.y]];
            this.stones[loc.x][loc.y] = color;
            if (notifyListeners) {
                this.emit(0, loc);
            }
        }
    }

    public final int caps(int color) {
        return this.captures[color];
    }

    public ArrayList<Chain> getChains() {
        ArrayList<Chain> grpList = new ArrayList<Chain>();
        for (Loc allLoc : this.allLocs) {
            Chain g = this.getChain(allLoc);
            if (g == null || g.getMember() != allLoc) continue;
            grpList.add(g);
        }
        return grpList;
    }

    public Iterator<Loc> allLocs() {
        return Arrays.asList(this.allLocs).iterator();
    }

    public List<Loc> getAllLocs() {
        return Collections.unmodifiableList(Arrays.asList(this.allLocs));
    }

    public void reset() {
        this.clearBoard();
    }

    private void clearBoard() {
        if (this.captures[0] != 0 || this.captures[1] != 0) {
            this.captures[0] = 0;
            this.captures[1] = 0;
            this.emit(1);
        }
        for (int i = 0; i < this.size; ++i) {
            for (int j = 0; j < this.size; ++j) {
                if (this.stones[i][j] == 2) continue;
                this.stones[i][j] = 2;
                this.groups[i][j] = null;
                this.emit(0, Loc.get(i, j));
            }
        }
        this.hashCode = 0L;
    }

    public int hashCode() {
        return (int)this.hashCode;
    }

    public long longHashCode() {
        long result = this.hashCode;
        if (this.koLoc != null) {
            result ^= (long)((this.koLoc.x << 8) + this.koLoc.y << 16);
        }
        return result;
    }

    public boolean isSimpleKo(Loc loc, int color) {
        return this.koLoc == loc && this.koColor == color;
    }

    public Loc getSimpleKoLoc() {
        return this.koLoc;
    }

    public int getSimpleKoColor() {
        return this.koColor;
    }

    private static long[][][] computeHashCodes() {
        Random r = new Random(5L);
        long[][][] result = new long[38][38][3];
        for (int i = 0; i < 38; ++i) {
            for (int j = 0; j < 38; ++j) {
                result[i][j][0] = r.nextLong();
                result[i][j][1] = r.nextLong();
                result[i][j][2] = 0L;
            }
        }
        return result;
    }

    public int[] getBoardMap() {
        int[] result = new int[(this.size * this.size + 15) / 16];
        int index = 0;
        int shift = 0;
        int bits = 0;
        for (Loc allLoc : this.allLocs) {
            int color = this.getColor(allLoc);
            bits |= color << shift;
            if ((shift += 2) != 32) continue;
            result[index++] = bits;
            shift = 0;
            bits = 0;
        }
        if (shift != 0) {
            result[index] = bits;
        }
        return result;
    }

    public void removeStone(Loc loc) {
        this.removeStone(loc, null);
    }

    public void removeStone(Loc loc, Undo undo) {
        if (loc.x < 0 || loc.x >= this.size || loc.y < 0 || loc.y >= this.size) {
            throw new RuntimeException();
        }
        if (this.getColor(loc) == 2) {
            return;
        }
        Chain targetChain = this.getChain(loc);
        if (undo != null) {
            undo.addToDeadSet(targetChain);
        }
        this.eraseChain(targetChain, false);
        for (Loc targetStone : targetChain) {
            if (targetStone.equals(loc)) continue;
            this.addStone(targetStone, targetChain.color, true, false, undo);
        }
        this.emit(0, loc);
    }

    public void clearSimpleKo() {
        this.koLoc = null;
    }

    public HashSet<Loc> floodFill(Loc seed, int border, HashSet<Loc> deadLocs) {
        HashSet<Loc> foundLocs = new HashSet<Loc>();
        Stack<Loc> todo = new Stack<Loc>();
        if ((1 << this.getColor(seed) & border) == 0) {
            foundLocs.add(seed);
            todo.push(seed);
        }
        while (!todo.empty()) {
            Loc loc = (Loc)todo.pop();
            Iterator<Loc> neighbors = loc.neighbors(this.size);
            while (neighbors.hasNext()) {
                Loc neighbor = neighbors.next();
                int color = deadLocs != null && deadLocs.contains(neighbor) ? 2 : this.getColor(neighbor);
                if ((1 << color & border) != 0 || !foundLocs.add(neighbor)) continue;
                todo.push(neighbor);
            }
        }
        return foundLocs;
    }

    public static int[] hoshiLines(int boardSize) {
        if (boardSize < 9) {
            return new int[0];
        }
        int[] result = new int[2 + (boardSize & 1)];
        result[0] = boardSize == 9 ? 2 : 3;
        result[1] = boardSize - 1 - result[0];
        if (result.length == 3) {
            result[2] = boardSize / 2;
        }
        return result;
    }

    public void toArray(byte[] out) {
        int i = 0;
        int lim = this.size;
        for (int y = 0; y < lim; ++y) {
            for (int x = 0; x < lim; ++x) {
                out[i++] = (byte)this.stones[x][y];
            }
        }
    }
}
