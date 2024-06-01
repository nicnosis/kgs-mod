/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import org.igoweb.util.Bundle;
import org.igoweb.util.Defs;

public abstract class Go {
    public static final int BLACK = 0;
    public static final int WHITE = 1;
    public static final int EMPTY = 2;
    public static final int COLOR_LIMIT = 3;
    public static final int RANK_NONE = 0;
    public static final int RANK_MIN_KYU = 1;
    public static final int RANK_MAX_KYU = 30;
    public static final int RANK_MAX_AMA = 39;
    public static final int RANK_MAX_PRO = 48;

    public static final int opponent(int color) {
        if (color < 0 || color > 1) {
            throw new RuntimeException("Can't take opponent of " + color);
        }
        return color ^ 1;
    }

    public static String formatRank(int rank) {
        return Go.formatRank(Defs.getBundle(), rank, true, true);
    }

    public static String formatRank(int rank, boolean wanted, boolean conf) {
        return Go.formatRank(Defs.getBundle(), rank, wanted, conf);
    }

    public static String formatRank(Bundle bundle, int rank, boolean rankWanted, boolean rankConfident) {
        int type;
        if (!rankWanted) {
            return "-";
        }
        if (rank == 0) {
            type = 0;
            rankConfident = false;
        } else if (rank <= 30) {
            type = 1;
            rank = 31 - rank;
        } else if (rank <= 39) {
            type = 2;
            rank -= 30;
        } else {
            type = 3;
            rank -= 39;
        }
        return bundle.str(-1337055795, new Object[]{new Integer(type), new Integer(rank), new Integer(rankConfident ? 1 : 0)});
    }
}
