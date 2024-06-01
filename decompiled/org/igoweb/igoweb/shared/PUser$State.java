/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.util.HashMap;
import java.util.Locale;

public static enum PUser.State {
    GUEST(true, false, false),
    PENDING(true, true, false),
    ACTIVE(false, true, false),
    ABORTED(true, false, true),
    CLOSED(false, false, true);

    public final String sqlName;
    public final boolean guest;
    public final boolean active;
    public final boolean deleted;
    private static HashMap<String, PUser.State> nameToState;

    public boolean canBecome(PUser.State newState) {
        switch (this) {
            case GUEST: {
                return newState == PENDING || newState == ABORTED;
            }
            case PENDING: {
                return newState == ACTIVE || newState == ABORTED;
            }
            case ACTIVE: {
                return newState == CLOSED;
            }
        }
        return false;
    }

    private PUser.State(boolean guest, boolean active, boolean deleted) {
        this.guest = guest;
        this.active = active;
        this.deleted = deleted;
        this.sqlName = this.toString().toLowerCase(Locale.US);
    }

    public static PUser.State get(String sqlName) {
        PUser.State result;
        if (nameToState == null) {
            PUser.State.initNameToState();
        }
        if ((result = nameToState.get(sqlName)) == null) {
            throw new IllegalArgumentException("Invalid state sql name: " + sqlName);
        }
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void initNameToState() {
        Class<PUser.State> clazz = PUser.State.class;
        synchronized (PUser.State.class) {
            if (nameToState == null) {
                HashMap<String, PUser.State> map = new HashMap<String, PUser.State>();
                for (PUser.State state : PUser.State.values()) {
                    map.put(state.sqlName, state);
                }
                nameToState = map;
            }
            // ** MonitorExit[var0] (shouldn't be in output)
            return;
        }
    }
}
