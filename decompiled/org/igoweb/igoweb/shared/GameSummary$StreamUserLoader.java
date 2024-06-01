/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.DataInputStream;
import java.io.IOException;
import org.igoweb.igoweb.shared.User;

public static interface GameSummary.StreamUserLoader<UserT extends User> {
    public UserT loadUser(DataInputStream var1) throws IOException;
}
