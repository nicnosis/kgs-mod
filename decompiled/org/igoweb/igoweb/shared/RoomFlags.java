/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

public class RoomFlags {
    public static final int PRIVATE_BIT = 1;
    public static final int TOURN_ONLY_BIT = 2;
    public static final int GLOBAL_GAMES_ONLY_BIT = 4;
    public static final int ADMIN_ONLY_BITS = 2;
    public static final String PRIVATE_NAME = "private";
    public static final String TOURN_ONLY_NAME = "tournOnly";
    public static final String GLOBAL_GAMES_ONLY_NAME = "globalOnly";

    private RoomFlags() {
    }
}
