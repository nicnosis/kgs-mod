/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import org.igoweb.igoweb.shared.User;

public class Friend<U extends User> {
    public final U user;
    public final String notes;

    public Friend(U user, String notes) {
        this.user = user;
        this.notes = notes;
    }

    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Friend peer = (Friend)o;
        return this.user == peer.user && this.notes.equals(peer.notes);
    }
}
