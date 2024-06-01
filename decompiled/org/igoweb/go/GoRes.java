/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import org.igoweb.games.GameRes;
import org.igoweb.resource.AndroidRes;
import org.igoweb.resource.ResEntry;
import org.igoweb.resource.Resource;
import org.igoweb.util.UtilRes;

public class GoRes
extends Resource {
    public static final int BASE = -1337055800;
    public static final int COLOR_MOVE = -1337055800;
    public static final int RULE_SETS = -1337055799;
    public static final int NN_NN_FOR_N = -1337055798;
    public static final int NN_NN_ROUND_N = -1337055797;
    public static final int PASS = -1337055796;
    public static final int RANK_FORMATTER = -1337055795;
    public static final int UNDO = -1337055794;
    public static final int NO_TIME_LIMIT = -1337055793;
    public static final int TIME_FORMATTER = -1337055792;
    public static final int GAME_TITLE_FORMATTER = -1337055791;
    private static final ResEntry[] contents = new ResEntry[]{new ResEntry("color move", -1337055800, "{0,choice,0#B|1#W} {1}", "", new Object[][]{{new Integer(0), "A5"}, {new Integer(1), "C14"}}), new ResEntry("nn:nn/n", -1337055798, "{0}:{1,number,00}/{2}", "This is the system for showing a time amount in minutes and seconds, followed by the number of moves left in this time period.", new Object[][]{{new Integer(5), new Integer(15), new Integer(25)}, {new Integer(0), new Integer(2), new Integer(3)}}), new ResEntry("nn:nn (n)", -1337055797, "{0}:{1,number,00} {2,choice,1#SD|2#({2})}", "This is the system for showing a time amount in minutes and seconds, followed by the byo-yomi period. Note that if argument 2 is \"1\" then the letter \"SD\" is shown, indicating that you are in sudden death (it is your last byo-yomi period).", new Object[][]{{new Integer(0), new Integer(25), new Integer(3)}, {new Integer(0), new Integer(30), new Integer(1)}}), new ResEntry("Pass", -1337055796, "Pass", "Button label.", AndroidRes.ONLY), new ResEntry("rankFormatter2", -1337055795, "{0,choice,0#|1#{1}k|2#{1}d|3#{1}p}{2,choice,0#?|1#}", "The code to turn a rank integer into a localized rank. Note that this returns a \"-\" (meaning the user chooses not to have a rank) if the rank is -1 and \"?\" (meaning unknown rank) if the rank is 0. The first argument is the type of rank (0=none, 1=kyu, 2=dan, 3=pro), and the second argument is the rank value.", new Object[][]{{new Integer(0), new Integer(0), new Integer(1)}, {new Integer(1), new Integer(5), new Integer(0)}, {new Integer(1), new Integer(5), new Integer(1)}, {new Integer(2), new Integer(4), new Integer(0)}, {new Integer(2), new Integer(4), new Integer(1)}, {new Integer(3), new Integer(3)}}), new ResEntry("Undo", -1337055794, "Undo"), new ResEntry("No time limit", -1337055793, "No time limit", "Describes a game where there is no limit on how much time players can use."), new ResEntry("TimeFormatter", -1337055792, "{1,choice,0#{2}|1#{1}:{2,number,00}}:{3,number,00}{0,choice,1# (absolute)|2#+{6}\u00d7{4}:{5,number,00} (byo-yomi)|3#+{4}:{5,number,00}/{6} (Canadian)}", "Formatting a time system. Argument 0 is system (1=absolute, 2=byo-yomi, 3=canadian). Arguments 1/2/3 are hours/minutes/seconds for main time. Arguments 4/5 are minutes/seconds for byo-yomi time. Argument 6 is the number of byo-yomi periods or stones.", new Object[][]{{new Integer(1), new Integer(2), new Integer(30), new Integer(0), new Integer(0), new Integer(0), new Integer(0)}, {new Integer(2), new Integer(1), new Integer(30), new Integer(0), new Integer(0), new Integer(30), new Integer(5)}, {new Integer(3), new Integer(0), new Integer(45), new Integer(0), new Integer(10), new Integer(0), new Integer(25)}, {new Integer(1), new Integer(0), new Integer(5), new Integer(0), new Integer(0), new Integer(0), new Integer(0)}}), new ResEntry("gameTitleFormatter", -1337055791, "{0} vs. {1}", "This is the default title of a game - white is arg 0, black is arg 1.", new Object[][]{{"rimas", "wms"}, {"wms [2k]", "glue [5k]"}}), new ResEntry("RuleSets", -1337055799, "Japanese|Chinese|AGA|New Zealand", "This should be a list of the four supported rule sets, separated by \"|\" characters. Make sure that you keep them in the same order as the English text is!")};

    @Override
    public String propFilePath() {
        return "org/igoweb/go/res/Res";
    }

    public String toString() {
        return "Go Resources";
    }

    @Override
    public Resource[] getChildren() {
        return new Resource[]{new UtilRes(), new GameRes()};
    }

    @Override
    public ResEntry[] getContents() {
        return contents;
    }
}
