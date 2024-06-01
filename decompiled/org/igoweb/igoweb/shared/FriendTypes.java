/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

public class FriendTypes {
    public static final int BUDDY = 0;
    public static final int CENSORED = 1;
    public static final int FAN = 2;
    public static final int ADMIN_TRACK = 3;
    public static final int NONADMIN_COUNT = 3;
    public static final int COUNT = 4;
    private static final String[] sqlNames = new String[]{"buddy", "censored", "fan", "admin_track"};

    private FriendTypes() {
    }

    public static int get(String sqlName) {
        switch (sqlName.charAt(0)) {
            case 'b': {
                if (!sqlName.equals("buddy")) break;
                return 0;
            }
            case 'c': {
                if (!sqlName.equals("censored")) break;
                return 1;
            }
            case 'f': {
                if (!sqlName.equals("fan")) break;
                return 2;
            }
            case 'a': {
                if (!sqlName.equals("admin_track")) break;
                return 3;
            }
        }
        throw new IllegalArgumentException("No such friends type: " + sqlName);
    }

    public static String getSqlName(int type) {
        return sqlNames[type];
    }
}
