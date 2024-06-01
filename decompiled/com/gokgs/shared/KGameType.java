/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.shared;

import com.gokgs.shared.KRole;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.Role;

public class KGameType
extends GameType {
    public static final int DEMONSTRATION_ID = 1;
    public static final KGameType DEMONSTRATION = new KGameType(1, 10, 2, 2, 0, "demonstration", KRole.OWNER);
    public static final int REVIEW_ID = 2;
    public static final KGameType REVIEW = new KGameType(2, 10, 2, 22, 0, "review", KRole.OWNER);
    public static final int RENGO_REVIEW_ID = 3;
    public static final KGameType RENGO_REVIEW = new KGameType(3, 10, 2, 62, 0, "rengo_review", KRole.OWNER);
    public static final int TEACHING_ID = 4;
    public static final KGameType TEACHING = new KGameType(4, 122, 20, 20, 20, "teaching", KRole.WHITE);
    public static final int SIMUL_ID = 5;
    public static final KGameType SIMUL = new KGameType(5, 248, 20, 20, 20, "simul", KRole.WHITE);
    public static final int RENGO_ID = 6;
    public static final KGameType RENGO = new KGameType(6, 248, 60, 60, 20, "rengo", KRole.WHITE);
    public static final int FREE_ID = 7;
    public static final KGameType FREE = new KGameType(7, 248, 20, 20, 20, "free", KRole.WHITE);
    public static final int RANKED_ID = 8;
    public static final KGameType RANKED = new KGameType(8, 244, 20, 20, 20, "ranked", KRole.WHITE);
    public static final int TOURNAMENT_ID = 9;
    public static final KGameType TOURNAMENT = new KGameType(9, 249, 20, 20, 20, "tournament", KRole.WHITE);
    public static final int PLAYBACK_ID = 10;
    public static final KGameType PLAYBACK = new KGameType(10, 8, 0, 2, 0, "playback", null);
    public static final int COUNT = 11;

    private KGameType(int newId, int flags, int mainRoleMask, int newRoleMask, int timedMask, String newSql, Role ownerRole) {
        super(newId, flags, mainRoleMask, newRoleMask, timedMask, newSql, ownerRole);
    }

    @Override
    public GameType getFreeType() {
        return this == RANKED ? FREE : null;
    }
}
