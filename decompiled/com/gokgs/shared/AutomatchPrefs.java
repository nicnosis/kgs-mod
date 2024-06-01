/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.shared;

public class AutomatchPrefs {
    public static final short MAX_HANDICAP_MASK = 31;
    public static final int FREE_OK = 32;
    public static final int RANKED_OK = 64;
    public static final int ROBOT_OK = 128;
    public static final int HUMAN_OK = 256;
    public static final int BLITZ_OK = 512;
    public static final int MEDIUM_OK = 1024;
    public static final int UNRANKED_OK = 2048;
    public static final int FAST_OK = 4096;
    public static final int ESTIMATED_RANK_SHIFT = 26;
    public static final int ESTIMATED_RANK_MASK = 63;
    public static final int GAME_TYPE_MASK = 96;
    public static final int OPPONENT_MASK = 384;
    public static final int SPEED_MASK = 5632;
    public static final int DEFAULT = 335551974;
    public static final int MIN_ESTIMATED_RANK = 1;
    public static final int MAX_ESTIMATED_RANK = 30;

    public static int getEstRank(int prefs) {
        return prefs >> 26 & 0x3F;
    }

    public static int setEstRank(int prefs, int estRank) {
        return prefs & 0x3FFFFFF | estRank << 26;
    }
}
