/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.User;

public static class Proposal.UserRole<UserT extends User> {
    private UserT user;
    private Role role;

    protected Proposal.UserRole(UserT newUser, Role newRole) {
        this.user = newUser;
        this.role = newRole;
    }

    public final UserT getUser() {
        return this.user;
    }

    public void setUser(UserT newUser) {
        this.user = newUser;
    }

    public final Role getRole() {
        return this.role;
    }

    public void setRole(Role newRole) {
        this.role = newRole;
    }

    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Proposal.UserRole peer = (Proposal.UserRole)obj;
        return this.user == peer.user && this.role == peer.role;
    }

    public String toString() {
        return "UserRole[" + this.user + "," + this.role + "]";
    }
}
