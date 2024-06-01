/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.util.Comparator;
import org.igoweb.igoweb.shared.User;

public static class User.Comparator
implements Comparator<User> {
    public static final int SORT_BY_NAME = 0;
    public static final int SORT_BY_RANK = 1;
    private int type;

    public User.Comparator() {
        this(0);
    }

    public User.Comparator(int sortType) {
        this.type = sortType;
    }

    public void setSortType(int newType) {
        this.type = newType;
    }

    public int getSortType() {
        return this.type;
    }

    @Override
    public int compare(User u1, User u2) {
        int r2;
        int r1;
        if (this.type == 1 && (r1 = u1.getRankSortValue()) != (r2 = u2.getRankSortValue())) {
            return r2 - r1;
        }
        return u1.name.compareToIgnoreCase(u2.name);
    }
}
