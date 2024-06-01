/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.util.Collection;
import java.util.HashMap;
import org.igoweb.igoweb.shared.Friend;
import org.igoweb.igoweb.shared.User;
import org.igoweb.util.Emitter;

public class FriendsGroup
extends Emitter {
    public static final int NOTES_MAX_LENGTH = 50;
    private static final int EVENT_BASE = 6;
    public static final int USER_ADDED_EVENT = 6;
    public static final int USER_REMOVED_EVENT = 7;
    public static final int EVENT_LIMIT = 8;
    private final HashMap<String, Friend<User>> users = new HashMap();

    FriendsGroup() {
    }

    void add(User u, String notes) {
        Friend<User> f = new Friend<User>(u, notes);
        if (!f.equals(this.users.put(u.name, f))) {
            this.emit(6, u);
            u.emitFlagsChanged();
        }
    }

    void remove(User u) {
        if (this.users.remove(u.name) != null) {
            this.emit(7, u);
            u.emitFlagsChanged();
        }
    }

    public boolean contains(String name) {
        return this.users.containsKey(name);
    }

    public boolean contains(User user) {
        return this.users.containsKey(user.name);
    }

    public Collection<Friend<User>> getUsers() {
        return this.users.values();
    }

    public String getNotes(User u) {
        Friend<User> f = this.users.get(u.name);
        return f == null ? null : f.notes;
    }
}
