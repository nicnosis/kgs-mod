/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.util.Locale;
import org.igoweb.igoweb.Config;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.util.Bundle;
import org.igoweb.util.Defs;

public abstract class IBundle
extends Bundle {
    public static IBundle get() {
        return (IBundle)Defs.getBundle();
    }

    protected IBundle(String resLoc, Locale locale, boolean intern) {
        super(resLoc, locale, intern);
    }

    public final String formatRank(int rank) {
        return this.formatRank(rank, true, true);
    }

    public abstract String formatRank(int var1, boolean var2, boolean var3);

    public abstract String formatScore(int var1);

    public String formatRankGraphValue(int value) {
        throw new UnsupportedOperationException();
    }

    public int getRankGraphGranularity() {
        throw new UnsupportedOperationException();
    }

    public short getRankGraphMinimumValue() {
        throw new UnsupportedOperationException();
    }

    public short getRankGraphMaximumValue() {
        throw new UnsupportedOperationException();
    }

    public abstract int parseRank(String var1);

    public abstract String getGameTypeDescription(GameType var1);

    public abstract int getDefaultAutomatchPrefs();

    public Conflict checkConflict(GameType currentGameType, Role currentRole, GameType newGameType, Role newRole) {
        if (currentGameType == GameType.CHALLENGE) {
            if (newGameType == GameType.CHALLENGE) {
                return currentRole == Role.CHALLENGE_CREATOR && newRole == Role.CHALLENGE_CREATOR ? Conflict.REFUSE_NEW : null;
            }
            return newGameType.isMainRole(newRole) ? Conflict.LEAVE_CURRENT : null;
        }
        if (!currentGameType.isConflict() || !currentGameType.isMainRole(currentRole)) {
            return null;
        }
        if (newGameType == GameType.CHALLENGE) {
            return Conflict.REFUSE_NEW;
        }
        if (!newGameType.isConflict() || !newGameType.isMainRole(newRole)) {
            return null;
        }
        return newGameType.isTournament() && !currentGameType.isTournament() ? Conflict.LEAVE_CURRENT : Conflict.REFUSE_NEW;
    }

    public static abstract class IMulti
    extends Bundle.Multi<IBundle> {
        public IMulti(String resName) {
            super(resName, Config.get("localeList"));
        }
    }

    public static enum Conflict {
        LEAVE_CURRENT,
        REFUSE_NEW;

    }
}
