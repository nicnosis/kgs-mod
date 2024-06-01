/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.User;

public interface PlayerContainer<UserT extends User> {
    public UserT getPlayer(Role var1);
}
