/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.util.Comparator;
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.User;

public static class GameSummary.Comparator
implements Comparator<GameSummary<?>> {
    public static final int SORT_BY_DATE = 0;
    public static final int SORT_BY_NAME = 1;
    private int sortType;
    private final String ignoredName;

    public GameSummary.Comparator() {
        this(null);
    }

    public GameSummary.Comparator(User ignoredUser) {
        this(ignoredUser, 0);
    }

    public GameSummary.Comparator(User ignoredUser, int newSortType) {
        this.ignoredName = ignoredUser == null ? "" : ignoredUser.name;
        this.sortType = newSortType;
    }

    @Override
    public int compare(GameSummary<?> r1, GameSummary<?> r2) {
        int cmp;
        int sortByDateResult;
        int n = r1.id == r2.id ? 0 : (sortByDateResult = r1.id > r2.id ? -1 : 1);
        if (this.sortType == 0) {
            return sortByDateResult;
        }
        String n1 = null;
        String n2 = null;
        GameType gt1 = r1.getGameType();
        GameType gt2 = r2.getGameType();
        for (int i = 0; i < Role.count(); ++i) {
            String name;
            Role role = Role.get(i);
            if (n1 == null && gt1.isRole(role) && !(name = ((User)r1.getPlayer((Role)role)).name).equals(this.ignoredName)) {
                n1 = name;
            }
            if (n2 != null || !gt2.isRole(role) || (name = ((User)r2.getPlayer((Role)role)).name).equals(this.ignoredName)) continue;
            n2 = name;
        }
        if (n1 == null) {
            n1 = this.ignoredName;
        }
        if (n2 == null) {
            n2 = this.ignoredName;
        }
        return (cmp = n1.compareToIgnoreCase(n2)) == 0 ? sortByDateResult : cmp;
    }

    public void setSortType(int newType) {
        this.sortType = newType;
    }

    public int getSortType() {
        return this.sortType;
    }
}
