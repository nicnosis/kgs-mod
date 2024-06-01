/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import org.igoweb.igoweb.shared.GameAction;
import org.igoweb.igoweb.shared.User;

public static class CGameListEntry.UserAction {
    public final User user;
    public final GameAction action;

    public CGameListEntry.UserAction(User newUser, GameAction newAction) {
        this.user = newUser;
        this.action = newAction;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof CGameListEntry.UserAction)) {
            return false;
        }
        CGameListEntry.UserAction peer = (CGameListEntry.UserAction)o;
        return peer.user == this.user && peer.action == this.action;
    }

    public String toString() {
        return "UserAction[" + this.user + "=" + this.action + "]";
    }
}
