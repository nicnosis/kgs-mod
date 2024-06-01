/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import java.util.Iterator;
import org.igoweb.go.Goban;
import org.igoweb.go.Loc;

public class ScoringGoban
extends Goban {
    public static final int DEAD = 0;
    public static final int FAKE_EYE = 1;
    public static final int CAN_SEE_BLACK = 2;
    public static final int CAN_SEE_WHITE = 3;
    public static final int IS_EYE = 4;
    public static final int IS_CONNECTED = 5;
    public static final int FAKE_EYE_CHECKED = 6;
    public static final int FAKE_EYE_POSSIBLE = 7;
    public static final int SEKI_CHECKED = 8;
    public static final int ALREADY_SEEN = 9;
    private final short[][] marks;

    public static final int canSee(int color) {
        return 2 + color - 0;
    }

    public ScoringGoban(Goban oldGoban) {
        super(oldGoban.size);
        this.marks = new short[oldGoban.size + 2][oldGoban.size + 2];
        Iterator<Loc> locs = oldGoban.allLocs();
        while (locs.hasNext()) {
            Loc loc = locs.next();
            int color = oldGoban.getColor(loc);
            if (color == 1 || color == 0) {
                this.addStone(loc, color, true, null);
            }
            this.captures[1] = oldGoban.caps(1);
            this.captures[0] = oldGoban.caps(0);
            this.marks[loc.x + 1][loc.y + 1] = 0;
        }
    }

    public final boolean get(Loc loc, int flag) {
        try {
            return (this.marks[loc.x][loc.y] & 1 << flag) != 0;
        }
        catch (ArrayIndexOutOfBoundsException excep) {
            return false;
        }
    }

    public final void set(Loc loc, int flag, boolean value) {
        if (value) {
            short[] sArray = this.marks[loc.x];
            int n = loc.y;
            sArray[n] = (short)(sArray[n] | 1 << flag);
        } else {
            short[] sArray = this.marks[loc.x];
            int n = loc.y;
            sArray[n] = (short)(sArray[n] & ~(1 << flag));
        }
    }

    public void setAll(int flag, boolean value) {
        int localSize = this.size + 1;
        short mask = (short)(1 << flag);
        if (!value) {
            mask = ~mask;
        }
        for (int i = 0; i < localSize; ++i) {
            for (int j = 0; j < localSize; ++j) {
                if (value) {
                    short[] sArray = this.marks[i];
                    int n = j;
                    sArray[n] = (short)(sArray[n] | mask);
                    continue;
                }
                short[] sArray = this.marks[i];
                int n = j;
                sArray[n] = (short)(sArray[n] & mask);
            }
        }
    }

    public final int finalColor(Loc loc) {
        if (this.get(loc, 0)) {
            return 2;
        }
        return this.getColor(loc);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Flags: alreadySeen,sekiChecked,\n       fakeEyePossible,fakeEyeChecked,isConnected,isEye\n       canSeeW,canSeeB,fakeEye,dead\n");
        for (int x = 0; x < this.size; ++x) {
            buf.append("+-----");
        }
        buf.append("+\n");
        for (int y = 0; y < this.size; ++y) {
            int x;
            for (x = 0; x < this.size; ++x) {
                buf.append('|');
                buf.append("#O.X".charAt(this.getColor(Loc.get(x, y))));
                buf.append(' ');
                String intp = Integer.toHexString(this.marks[x][y]);
                if (intp.length() < 3) {
                    buf.append('0');
                }
                if (intp.length() < 2) {
                    buf.append('0');
                }
                buf.append(intp);
            }
            buf.append("|\n");
            for (x = 0; x < this.size; ++x) {
                buf.append("+-----");
            }
            buf.append("+\n");
        }
        return buf.toString();
    }
}
