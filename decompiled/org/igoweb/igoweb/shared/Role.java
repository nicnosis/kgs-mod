/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.DataInput;
import java.io.IOException;
import java.util.HashMap;

public class Role {
    private static Role[] idToRole = new Role[0];
    private static final HashMap<String, Role> sqlToRole = new HashMap();
    public static final int CHALLENGE_CREATOR_ID = 0;
    public static final Role CHALLENGE_CREATOR = new Role(0, "challengeCreator", -1);
    public static final int OWNER_ID = 1;
    public static final Role OWNER = new Role(1, "owner", -1);
    protected static final int ROLE_ID_LIMIT = 2;
    public final int id;
    public final int team;
    public final String sql;

    public static Role get(String sqlIn) {
        Role result = sqlToRole.get(sqlIn);
        if (result == null) {
            throw new IllegalArgumentException("Unknown role " + sqlIn);
        }
        return result;
    }

    protected Role(int newId, String newSql, int newTeam) {
        this.id = newId;
        this.team = newTeam;
        if (newId >= idToRole.length) {
            Role[] newIdToRole = new Role[newId + 1];
            System.arraycopy(idToRole, 0, newIdToRole, 0, idToRole.length);
            idToRole = newIdToRole;
        }
        if (idToRole[newId] != null) {
            throw new IllegalArgumentException();
        }
        Role.idToRole[newId] = this;
        this.sql = newSql;
        if (newSql != null && sqlToRole.put(newSql, this) != null) {
            throw new IllegalArgumentException("Entered role " + newSql + "twice");
        }
    }

    public static Role get(int idIn) {
        return idToRole[idIn];
    }

    public static Role get(DataInput in) throws IOException {
        byte id = in.readByte();
        try {
            return idToRole[id];
        }
        catch (ArrayIndexOutOfBoundsException excep) {
            throw new IOException("Got invalid role: " + id, excep);
        }
    }

    public static Role[] values() {
        return idToRole;
    }

    public static int count() {
        return idToRole.length;
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[" + this.sql + "]";
    }
}
