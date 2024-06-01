/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.shared;

import org.igoweb.igoweb.shared.Role;

public class KRole
extends Role {
    public static final int WHITE_ID = 2;
    public static final KRole WHITE = new KRole(2, "white", 1);
    public static final int WHITE_2_ID = 3;
    public static final KRole WHITE_2 = new KRole(3, "white_2", 1);
    public static final int BLACK_ID = 4;
    public static final KRole BLACK = new KRole(4, "black", 0);
    public static final int BLACK_2_ID = 5;
    public static final KRole BLACK_2 = new KRole(5, "black_2", 0);
    public static final int COUNT = 6;

    private KRole(int newId, String newSql, int newTeam) {
        super(newId, newSql, newTeam);
    }

    public static Role forColor(int color) {
        if (color == 0) {
            return BLACK;
        }
        if (color == 1) {
            return WHITE;
        }
        throw new IllegalArgumentException();
    }

    public static KRole opponent(Role role) {
        if (role == BLACK || role == BLACK_2) {
            return WHITE;
        }
        if (role == WHITE || role == WHITE_2) {
            return BLACK;
        }
        throw new IllegalArgumentException(role.toString());
    }
}
