/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.DataInput;
import java.io.IOException;
import java.util.HashMap;
import org.igoweb.igoweb.shared.Role;

public class GameType {
    public final int id;
    private final int mainRoleMask;
    public final int roleMask;
    private final int flags;
    private final int timedMask;
    protected static final int IS_TOURNAMENT_BIT = 1;
    protected static final int IS_EDITABLE_BIT = 2;
    protected static final int IS_RANKED_BIT = 4;
    protected static final int IS_PRIVATE_OK_BIT = 8;
    protected static final int IS_ACTIVE_LIST_BIT = 16;
    protected static final int IS_CONFLICT_BIT = 32;
    protected static final int IS_SCORABLE_BIT = 64;
    protected static final int IS_KIBITZ_BLOCKED_BIT = 128;
    public final Role owner;
    public final String sql;
    private static final HashMap<String, GameType> sqlToGameType = new HashMap();
    private static GameType[] idToType = new GameType[0];
    public static final int CHALLENGE_ID = 0;
    public static final GameType CHALLENGE = new GameType(0, 32, 1, 1, 0, "challenge", Role.CHALLENGE_CREATOR);

    public static GameType get(String srcSql) {
        GameType result = sqlToGameType.get(srcSql);
        if (result == null) {
            throw new IllegalArgumentException("Unknown game type " + srcSql);
        }
        return result;
    }

    protected GameType(int newId, int newFlags, int newMainRoleMask, int newRoleMask, int newTimedMask, String newSql, Role newOwner) {
        this.id = newId;
        this.flags = newFlags;
        this.mainRoleMask = newMainRoleMask;
        this.roleMask = newRoleMask;
        this.timedMask = newTimedMask;
        this.owner = newOwner;
        if (newId >= idToType.length) {
            GameType[] newIdToType = new GameType[newId + 1];
            System.arraycopy(idToType, 0, newIdToType, 0, idToType.length);
            idToType = newIdToType;
        }
        if (idToType[newId] != null) {
            throw new IllegalArgumentException();
        }
        GameType.idToType[newId] = this;
        this.sql = newSql;
        if (newSql != null && sqlToGameType.put(newSql, this) != null) {
            throw new IllegalArgumentException("Entered game type " + newSql + "twice");
        }
    }

    public static GameType get(int srcId) {
        return idToType[srcId];
    }

    public static GameType get(DataInput in) throws IOException {
        try {
            return idToType[in.readByte()];
        }
        catch (ArrayIndexOutOfBoundsException excep) {
            IOException excep2 = new IOException("Got invalid game type, count is " + idToType.length);
            excep2.initCause(excep);
            throw excep2;
        }
    }

    public final boolean isTournament() {
        return (this.flags & 1) != 0;
    }

    public final boolean isEditable() {
        return (this.flags & 2) != 0;
    }

    public final boolean isRanked() {
        return (this.flags & 4) != 0;
    }

    public final boolean isPrivateOk() {
        return (this.flags & 8) != 0;
    }

    public final boolean isKibitzBlocked() {
        return (this.flags & 0x80) != 0;
    }

    public final boolean isMainRole(Role role) {
        return role != null && (this.mainRoleMask & 1 << role.id) != 0;
    }

    public final boolean isRole(Role role) {
        return role != null && (this.roleMask & 1 << role.id) != 0;
    }

    public final boolean isTimed(Role role) {
        return (this.timedMask & 1 << role.id) != 0;
    }

    public final boolean isOwner(Role role) {
        return this.owner != null && this.owner == role;
    }

    public final boolean isOnActiveList() {
        return (this.flags & 0x10) != 0;
    }

    public static GameType[] values() {
        return idToType;
    }

    public static int count() {
        return idToType.length;
    }

    public String toString() {
        String name = this.getClass().getName();
        name = name.substring(name.lastIndexOf(".") + 1);
        return name + "[" + this.sql + "]";
    }

    public GameType getFreeType() {
        return null;
    }

    public boolean isConflict() {
        return (this.flags & 0x20) != 0;
    }

    public boolean isScorable() {
        return (this.flags & 0x40) != 0;
    }
}
