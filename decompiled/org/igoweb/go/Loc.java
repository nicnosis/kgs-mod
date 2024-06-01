/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import java.util.Iterator;
import org.igoweb.util.Defs;

public class Loc
implements Comparable<Loc> {
    public static final String xLabels = "A B C D E F G H J K L M N O P Q R S T U V W X Y Z AA BB CC DD EE FF GG HH JJ KK LL MM NN";
    public static final String yLabels = "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38";
    public static final int MAX_SIZE = 38;
    private static final Loc[] prebuilt = new Loc[1444];
    public final int x;
    public final int y;
    private static final int[] vectors;
    public static final Loc PASS;

    private Loc(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    public static Loc get(int xIn, int yIn) {
        if (xIn < 0 || yIn < 0 || xIn >= 38 || yIn >= 38) {
            throw new IllegalArgumentException();
        }
        return prebuilt[xIn + yIn * 38];
    }

    public static Loc get(String coord, int size) {
        if (coord.equals("PASS")) {
            return PASS;
        }
        char x = coord.charAt(0);
        if (x >= 'A' && x <= 'Z') {
            x = (char)(x + 32);
        }
        if (x >= 'i') {
            x = (char)(x - '\u0001');
        }
        return Loc.get(x - 97, size - Integer.parseInt(coord.substring(1)));
    }

    @Override
    public int compareTo(Loc peer) {
        if (this.y == peer.y) {
            return this.x - peer.x;
        }
        return this.y - peer.y;
    }

    public boolean equals(Object obj) {
        return this == obj;
    }

    public Iterator<Loc> neighbors(int boardSize) {
        if (this.x < 0 || this.x > boardSize - 1 || this.y > boardSize - 1) {
            throw new IllegalArgumentException();
        }
        return new Neighbors(boardSize);
    }

    public String toString() {
        if (this == PASS) {
            return "Loc[pass]";
        }
        return "Loc[" + this.x + "," + this.y + "]";
    }

    public String toCoords(int boardSize) {
        if (this.x < 0) {
            return Defs.getString(-1337055796);
        }
        char xChar = (char)(this.x + 97);
        if (xChar >= 'i') {
            xChar = (char)(xChar + '\u0001');
        }
        StringBuilder result = new StringBuilder();
        if (xChar > 'z') {
            if ((xChar = (char)(xChar - 26)) >= 'i') {
                xChar = (char)(xChar + '\u0001');
            }
            result.append(xChar).append(xChar);
        } else {
            result.append(xChar);
        }
        return result.append(boardSize - this.y).toString();
    }

    static {
        for (int i = 0; i < prebuilt.length; ++i) {
            Loc.prebuilt[i] = new Loc(i % 38, i / 38);
        }
        vectors = new int[]{-38, -1, 1, 38};
        PASS = new Loc(-1, -1);
    }

    private class Neighbors
    implements Iterator<Loc> {
        private int i = 0;
        private final int validMask;

        public Neighbors(int boardSize) {
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
}
