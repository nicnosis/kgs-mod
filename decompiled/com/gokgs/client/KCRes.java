/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client;

import com.gokgs.shared.KSharedRes;
import org.igoweb.go.GoRes;
import org.igoweb.igoweb.client.ClientRes;
import org.igoweb.resource.ResEntry;
import org.igoweb.resource.Resource;

public class KCRes
extends Resource {
    public static final int BASE = -1772731645;
    public static final int CANT_PLAY_RANKED = -1772731645;
    public static final int REPEAT_POS_WARNING = -1772731644;
    public static final int DESC_1_P = -1772731643;
    public static final int DESC_2_P = -1772731642;
    public static final int NAMED_GAME_TITLE = -1772731641;
    public static final int MOVE_N_CMT = -1772731640;
    public static final int GT_BASE = -266865813;
    public static final int GT_DEMO = -266865812;
    public static final int GT_REVIEW = -266865811;
    public static final int GT_RENGO_REVIEW = -266865810;
    public static final int GT_TEACH = -266865809;
    public static final int GT_SIMUL = -266865808;
    public static final int GT_RENGO = -266865807;
    public static final int GT_FREE = -266865806;
    public static final int GT_RANKED = -266865805;
    public static final int GT_TOURN = -266865804;
    private static final ResEntry[] contents = new ResEntry[]{new ResEntry("RankType0", -266865812, "D", "This should be a one-character code for a demonstration game."), new ResEntry("RankTypeRev", -266865811, "D", "This should be a one-letter code for a review game. In English this is \"D\" just like Demonstration games, because \"R\" is taken by ranked games, so you can make it the same as demo games or you can pick a different letter."), new ResEntry("RankTypeRengoRev", -266865810, "D", "This should be a one-letter code for a rengo review game. In English this is \"D\" just like Demonstration & normal review games, so you can make it the same as demo games or you can pick a different letter."), new ResEntry("RankType1", -266865809, "T", "This should be a one-character code for a teaching game."), new ResEntry("RankTypeSimul", -266865808, "S", "This should be a one-character code for a simul game."), new ResEntry("RankTypeRengo", -266865807, "2", "This should be a one-character code for a rengo game. The english is \"2\" to indicate 2 players per side, but you can pick something else if it fits better."), new ResEntry("RankType2", -266865806, "F", "This should be a one-character code for a free (non-ranked) game."), new ResEntry("RankType3", -266865805, "R", "This should be a one-character code for a ranked game."), new ResEntry("RankType4", -266865804, "*", "This should be a one-character code for a tournament game."), new ResEntry("cantPlayRanked", -1772731645, "This game cannot be played as a ranked game. The {0,choice,0#size|1#handicap|2#komi|3#time|4#?} must be {1,choice,0#at least '{2}'|1#at most '{2}'|2#longer} if you want to play a ranked game.", "An error message when users try to set up a game with the wrong parameters. Be very careful with the quotes!", new Object[][]{{new Integer(0), new Integer(0), new Integer(19)}, {new Integer(1), new Integer(1), new Integer(6)}, {new Integer(2), new Integer(0), new Float(-10.5f)}, {new Integer(3), new Integer(2), new Integer(0)}}), new ResEntry("RepeatPosWarning", -1772731644, "This is the second time that the game has reached this position. If it repeats this position a third time, then it will be assumed that neither player wants to stop this cycle and the game will be ended with no result."), new ResEntry("Desc1p", -1772731643, "{0,choice,0#Black|1#White} ({1})", "A single player. This will get put into other messages.", new Object[][]{{new Integer(0), "wms"}, {new Integer(1), "glue"}}), new ResEntry("Desc2p", -1772731642, "{0,choice,0#Black|1#White} ({1} and {2})", "A rengo team. This will get put into other messages.", new Object[][]{{new Integer(0), "wms", "Admin"}, {new Integer(1), "glue", "voldemort"}}), new ResEntry("namedGameTitle", -1772731641, "{0} ({1})", "Title of a game that has a name. {0} is the game owner, {1} is the title.", new Object[][]{{"wms", "Review of pro game"}}), new ResEntry("Move n", -1772731640, "{0,choice,-1#Game Over|0#Game Start|1#Move {0}}", "", new Object[][]{{new Integer(-1)}, {new Integer(0)}, {new Integer(1)}, {new Integer(53)}})};

    @Override
    public String propFilePath() {
        return "com/gokgs/client/res/Res";
    }

    public String toString() {
        return "KGS Client Resources";
    }

    @Override
    public Resource[] getChildren() {
        return new Resource[]{new ClientRes(), new GoRes(), new KSharedRes()};
    }

    @Override
    public ResEntry[] getContents() {
        return contents;
    }
}
