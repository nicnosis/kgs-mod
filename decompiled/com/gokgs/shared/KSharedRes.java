/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.shared;

import org.igoweb.go.sgf.SgfRes;
import org.igoweb.igoweb.shared.SharedRes;
import org.igoweb.resource.ResEntry;
import org.igoweb.resource.Resource;

public class KSharedRes
extends Resource {
    public static final int RENGO_REVIEW_GAME_TITLE = 696435397;
    public static final int RENGO_GAME_TITLE = 877402151;
    public static final int GAME_TYPE_BASE = -1388380729;
    public static final int GAME_TYPE_DEMONSTRATION = -1388380728;
    public static final int GAME_TYPE_REVIEW = -1388380727;
    public static final int GAME_TYPE_RENGO_REVIEW = -1388380726;
    public static final int GAME_TYPE_TEACHING = -1388380725;
    public static final int GAME_TYPE_SIMUL = -1388380724;
    public static final int GAME_TYPE_RENGO = -1388380723;
    public static final int GAME_TYPE_FREE = -1388380722;
    public static final int GAME_TYPE_RANKED = -1388380721;
    public static final int GAME_TYPE_TOURNAMENT = -1388380720;
    public static final int GAME_TYPE_CHALLENGE = -1388380729;
    private static final ResEntry[] contents = new ResEntry[]{new ResEntry("gtDemo", -1388380728, "Demonstration", "A type of game. This kind of game has one person editing SGF data."), new ResEntry("gtReview", -1388380727, "Review", "A type of game. This kind of game has one person reviewing a game that was played before on the server."), new ResEntry("gtRReview", -1388380726, "Rengo Review", "A type of game. This kind of game has one person reviewing a rengo game."), new ResEntry("gtTeaching", -1388380725, "Teaching", "A type of game. In this type, the players can play each other, but white can interrupt the game and edit it whenever they want."), new ResEntry("gtSimul", -1388380724, "Simul", "A game type. In this type, one player plays against a lot of other players at once, each on a different board."), new ResEntry("gtRengo", -1388380723, "Rengo", "A game where two players are on each side, and they take turns playing."), new ResEntry("gtFree", -1388380722, "Free", "A game type. In free games, it is a normal game, but the result will not affect your rank."), new ResEntry("gtRanked", -1388380721, "Ranked", "A game type. In ranked games, the result will affect your rank."), new ResEntry("gtTournament", -1388380720, "Tournament", "A game type. Tournament games are playing as part of a tournament."), new ResEntry("{0}, {1} vs. {2}, {3}", 877402151, "{0}, {1} vs. {2}, {3}", "This message describes a rengo game. The first names are the white players, the next two are the black players.", new Object[][]{{"dfan", "owl", "glue", "Admin"}, {"dick", "harry", "Ella", "Lucy"}}), new ResEntry("{0} ({1}, {2} vs. {3}, {4})", 696435397, "{0} ({1}, {2} vs. {3}, {4})", "This message describes a reviewed rengo game. The first name is the reviewer; the next two are the white players players; then come the two black players.", new Object[][]{{"wms", "dfan", "owl", "glue", "Admin"}, {"tom", "dick", "harry", "Ella", "Lucy"}}), new ResEntry("Challenge", -1388380729, "challenge", "The name for a game that is being set up - the games in the lists that aren't started yet, and appear in bold.")};

    @Override
    public String propFilePath() {
        return "com/gokgs/shared/res/Res";
    }

    public String toString() {
        return "KGS Shared Resources";
    }

    @Override
    public ResEntry[] getContents() {
        return contents;
    }

    @Override
    public Resource[] getChildren() {
        return new Resource[]{new SgfRes(), new SharedRes()};
    }
}
