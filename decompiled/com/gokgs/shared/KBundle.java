/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.shared;

import com.gokgs.shared.KGameType;
import com.gokgs.shared.KRole;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.igoweb.go.Go;
import org.igoweb.go.sgf.Prop;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.igoweb.shared.Role;

public class KBundle
extends IBundle {
    private static final Pattern RANK_PARSER = Pattern.compile("(\\p{Digit}+)(k|d|p)");
    public static final int DONESCORING = 33;
    public static final int NUM_KGS_PROPS = 34;

    public KBundle(String resLoc, Locale newLocale, boolean intern) {
        super(resLoc, newLocale, intern);
    }

    @Override
    public String formatRank(int rank, boolean rankWanted, boolean confident) {
        return Go.formatRank(this, rank, rankWanted, confident);
    }

    @Override
    public String formatScore(int scoreVal) {
        int format;
        Object[] args = new Object[]{scoreVal > 0 ? 0 : 1, Math.abs((double)scoreVal * 0.5)};
        switch (scoreVal) {
            case 16386: {
                format = -669080771;
                break;
            }
            case 0: {
                format = -669080766;
                break;
            }
            case -16384: 
            case 16384: {
                format = -669080767;
                break;
            }
            case -16385: 
            case 16385: {
                format = -669080769;
                break;
            }
            case -16388: 
            case 16388: {
                format = -669080768;
                break;
            }
            case 16389: {
                format = -669080765;
                break;
            }
            case 16387: {
                format = -669080764;
                break;
            }
            default: {
                format = -669080770;
            }
        }
        return this.str(format, args);
    }

    @Override
    public String formatRankGraphValue(int value) {
        int rank = value / 100 + 30 + 1;
        return rank <= 0 || rank > 39 ? "" : this.formatRank(rank);
    }

    @Override
    public int getRankGraphGranularity() {
        return 100;
    }

    @Override
    public short getRankGraphMinimumValue() {
        return -3100;
    }

    @Override
    public short getRankGraphMaximumValue() {
        return 1900;
    }

    @Override
    public String getGameTypeDescription(GameType gameType) {
        return this.str(-1388380729 + gameType.id);
    }

    @Override
    public int getDefaultAutomatchPrefs() {
        return 335551974;
    }

    @Override
    public int parseRank(String rankStr) {
        Matcher m = RANK_PARSER.matcher(rankStr.toLowerCase(Locale.US));
        if (!m.matches()) {
            throw new IllegalArgumentException("Cannot decode rank \"" + rankStr + "\"");
        }
        int val = Integer.parseInt(m.group(1));
        if (val < 1) {
            throw new IllegalArgumentException("Cannot decode rank \"" + rankStr + "\"");
        }
        char type = m.group(2).charAt(0);
        switch (type) {
            case 'k': {
                val = 31 - val;
                if (val >= 1) break;
                throw new IllegalArgumentException("Cannot decode rank \"" + rankStr + "\"");
            }
            case 'd': {
                if ((val += 30) <= 39) break;
                throw new IllegalArgumentException("Cannot decode rank \"" + rankStr + "\"");
            }
            case 'p': {
                if ((val += 39) <= 48) break;
                throw new IllegalArgumentException("Cannot decode rank \"" + rankStr + "\"");
            }
        }
        return val;
    }

    @Override
    public IBundle.Conflict checkConflict(GameType currentGameType, Role currentRole, GameType newGameType, Role newRole) {
        if (currentGameType == KGameType.SIMUL && newGameType == KGameType.SIMUL && currentRole == KRole.WHITE && newRole == KRole.WHITE) {
            return null;
        }
        return super.checkConflict(currentGameType, currentRole, newGameType, newRole);
    }

    static {
        Prop.installProp(33, 130);
    }
}
