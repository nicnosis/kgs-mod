/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.util.Date;
import org.igoweb.resource.ResEntry;
import org.igoweb.resource.Resource;

public class SharedRes
extends Resource {
    private static final int BASE = -669080772;
    public static final int USER_FORMATTER = -669080772;
    public static final int NR = -669080771;
    public static final int PLAYER_PLUS = -669080770;
    public static final int PLAYER_PLUS_RES = -669080769;
    public static final int PLAYER_PLUS_FORF = -669080768;
    public static final int PLAYER_PLUS_TIME = -669080767;
    public static final int JIGO = -669080766;
    public static final int UNKNOWN = -669080765;
    public static final int UNFINISHED = -669080764;
    public static final int EDIT_GAME_TITLE = -669080763;
    public static final int NXN_HN = -669080762;
    public static final int N_N_N = -669080761;
    public static final int GRAPH_TITLE = -669080760;
    public static final int NO_GRAPH = -669080759;
    public static final int MAIL_CHAR_SET = -669080758;
    public static final int AUTOMATCH = -669080757;
    private static final ResEntry[] contents = new ResEntry[]{new ResEntry("userFormatter2", -669080772, "{0} [{1}]", "To format a user. The first argument is the name; the second is the rank; and the third will be 0 if the rank is unknown, 1 otherwise.", new Object[][]{{"wms", "5k"}, {"owl", "4d?"}}), new ResEntry("N.R.", -669080771, "N.R.", "This stands for \"No Result\"."), new ResEntry("player+nn", -669080770, "{0,choice,0#B+{1}|1#W+{1}}", "The result of a game when it was played until the very end (that is, until a score is known instead of a resign).", new Object[][]{{0, 1.5}, {1, 4.0}}), new ResEntry("player+Res.", -669080769, "{0,choice,0#B|1#W}+Res.", "The result of a game when it was won by resignation.", new Object[][]{{0}, {1}}), new ResEntry("player+Forf.", -669080768, "{0,choice,0#B|1#W}+Forf.", "The result of a game when it was won by forfeit (for example, if the opponent didn't show up for the game).", new Object[][]{{0}, {1}}), new ResEntry("player+Time", -669080767, "{0,choice,0#B|1#W}+Time", "The result of a game when one player ran out of time and lost.", new Object[][]{{0}, {1}}), new ResEntry("Jigo", -669080766, "Jigo", "This means \"Tie Game\". It's a Japanese term often used in English language go results."), new ResEntry("Unknown", -669080765, "Unknown", "The score description when we do not know who won."), new ResEntry("Unfinished", -669080764, "Unfinished", "The final score when the game was never finished."), new ResEntry("{0} ({1} vs. {2})", -669080763, "{0} ({1} vs. {2})", "This message describes a reviewed game. The first name is the reviewer; the next two are the players (white, then black).", new Object[][]{{"wms", "dfan", "owl"}, {"tom", "dick", "harry"}}), new ResEntry("nxn Hn", -669080762, "{0}\u00d7{0}{1,choice,0# |1# H{1}}", "This is a board size and (optional) handicap. Don't get sloppy and use \"x\" for \"times\"! Use a real \"\u00d7\" instead please.", new Object[][]{{19, 0}, {9, 3}}), new ResEntry("n.n.n", -669080761, "{0}.{1}.{2}", "This is the version number of the server.", new Object[][]{{1, 0, 0}, {0, 7, 10}}), new ResEntry("graphTitle", -669080760, "KGS Rank Graph for {0} ({1,date} through {2,date})", "The title of a graph of a user's rank.", new Object[]{"wms", new Date(), new Date()}), new ResEntry("noGraph", -669080759, "Sorry, no rank graph available for {0}", "What you see if you look at a graph that is not available."), new ResEntry("mailCharSet", -669080758, "ISO-8859-1", "This must be the proper character set for email in this language. ISO-8859-1 will cover most European langauges; for Asian languages, you will have to pick correctly."), new ResEntry("Automatch", -669080757, "Automatch", "The name of the fake room where automatch games are held.")};

    @Override
    public String propFilePath() {
        return "org/igoweb/igoweb/shared/res/Res";
    }

    public String toString() {
        return "Igoweb Shared Resources";
    }

    @Override
    public ResEntry[] getContents() {
        return contents;
    }

    @Override
    public Resource[] getChildren() {
        return new Resource[0];
    }
}
