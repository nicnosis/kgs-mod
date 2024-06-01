/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.igoweb.igoweb.shared.User;

public static interface GameSummary.DbUserLoader<UserT extends User> {
    public UserT loadUser(ResultSet var1) throws SQLException;
}
