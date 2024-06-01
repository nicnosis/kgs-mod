/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.util.HashMap;

public class AuthLevel {
    public static final int NORMAL = 0;
    public static final int ROBOT_RANKED = 1;
    public static final int TEACHER = 2;
    public static final int JR_ADMIN = 3;
    public static final int SR_ADMIN = 4;
    public static final int SUPER_ADMIN = 5;
    public static final int LIMIT = 6;
    private static final String[] sqlNames = new String[]{"normal", "robot_ranked", "teacher", "jr_admin", "sr_admin", "super_admin"};
    private static final HashMap<String, Integer> types = new HashMap();

    private AuthLevel() {
    }

    public static String toSql(int authLevel) {
        return sqlNames[authLevel];
    }

    public static int fromSql(String sqlLabel) {
        return types.get(sqlLabel);
    }

    public static String toString(int authLevel) {
        return "AuthLevel[" + AuthLevel.toSql(authLevel) + ']';
    }

    static {
        for (int i = 0; i < 6; ++i) {
            types.put(sqlNames[i], i);
        }
    }
}
