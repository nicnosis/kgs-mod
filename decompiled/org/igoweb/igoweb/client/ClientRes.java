/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.EOFException;
import java.net.NoRouteToHostException;
import org.igoweb.igoweb.shared.RoomCategories;
import org.igoweb.igoweb.shared.SharedRes;
import org.igoweb.resource.Plural;
import org.igoweb.resource.ResEntry;
import org.igoweb.resource.Resource;

public class ClientRes
extends Resource {
    public static final int BASE = 2031923633;
    public static final int ALLOW_INFORM = 2031923633;
    public static final int PLAYBACK_ERROR = 2031923634;
    public static final int SUBSCRIPTION_LOW = 2031923635;
    public static final int CANT_ALLOW_NONUSERS = 2031923636;
    public static final int ALREADY_IN_PLAYBACK = 2031923638;
    public static final int GAME_RESTARTED = 2031923639;
    public static final int CANT_PLAY_TWICE = 2031923640;
    public static final int DEL_ACCT_ALREADY_GONE = 2031923641;
    public static final int OWNERS_GAME = 2031923642;
    public static final int OWNERS_NAMED_GAME = 2031923643;
    public static final int NO_GAME_NO_RESOURCE = 2031923644;
    public static final int BAD_GAME = 2031923645;
    public static final int ALL_PLAYERS_LEFT = 2031923646;
    public static final int CHAL_REJECTED = 2031923647;
    public static final int CHAL_USER_LEFT = 2031923648;
    public static final int NO_TALKING = 2031923649;
    public static final int PRIVATE_KEEP_OUT = 2031923650;
    public static final int GUEST_LOGIN_GREETING = 2031923651;
    public static final int IO_ERR_AFTER_CONN = 2031923652;
    public static final int IO_ERR_ON_CONN = 2031923653;
    public static final int YOU_ARE_OUT_OF_TIME = 2031923654;
    public static final int KEEP_OUT = 2031923655;
    public static final int LOCALE = 2031923656;
    public static final int REQUEST_USER_FAILED = 2031923657;
    public static final int SUBSCRIBERS_ONLY_GAME_2 = 2031923658;
    public static final int BAD_PASSWORD = 2031923659;
    public static final int NO_SUCH_USER = 2031923660;
    public static final int USER_ALREADY_EXISTS = 2031923661;
    public static final int RECONNECT_FORCE_OUT = 2031923662;
    public static final int TOO_LONG_IDLE = 2031923663;
    public static final int TOO_MANY_KEEP_OUTS = 2031923664;
    public static final int NO_HOST = 2031923665;
    public static final int NO_RESULT_DESC = 2031923666;
    public static final int SIGNED_UP_TOO_LATE = 2031923667;
    public static final int NO_ROUTE = 2031923668;
    public static final int DEL_ACCT_SUCCESS = 2031923669;
    public static final int NO_RANKED_IN_PRIVATE = 2031923670;
    public static final int KEEP_OUT_CLEARED = 2031923671;
    public static final int KEEP_OUT_NOT_CLEARED = 2031923672;
    public static final int PLAYER_RESIGNED = 2031923673;
    public static final int PRIVATE_CHANNEL = 2031923674;
    public static final int GAME_IS_OVER = 2031923675;
    public static final int SCORE_SUMMARY_TIE = 2031923676;
    public static final int SCORE_RESULT = 2031923677;
    public static final int SERVER_DOWN = 2031923678;
    public static final int TEACHER_LEFT = 2031923679;
    public static final int UNEXPECTED_CLOSE = 2031923680;
    public static final int DONE_WAS_OVERRIDDEN = 2031923681;
    public static final int FORCE_OUT_ALREADY_OUT = 2031923682;
    public static final int REGISTER_SUCCESS = 2031923683;
    public static final int UPDATE_USER_GONE = 2031923684;
    public static final int SCORE_SUMMARY_WIN = 2031923685;
    public static final int FORCE_OUT_SUCCESS = 2031923686;
    public static final int RANK_PRIVATE = 2031923687;
    public static final int RANK_SUBSCRIBERS_ONLY = 2031923688;
    public static final int ALMOST_TOO_LONG = 2031923689;
    public static final int MV_N = 2031923690;
    public static final int MUST_ALLOW_NETWORK = 2031923691;
    public static final int ROOM_CATEGORY_BASE = -897758079;
    public static final int MAIN_CATEGORY = -897758079 + RoomCategories.MAIN.ordinal();
    public static final int CLUBS_CATEGORY = -897758079 + RoomCategories.CLUBS.ordinal();
    public static final int LESSONS_CATEGORY = -897758079 + RoomCategories.LESSONS.ordinal();
    public static final int TOURNAMENT_CATEGORY = -897758079 + RoomCategories.TOURNAMENT.ordinal();
    public static final int FRIENDLY_CATEGORY = -897758079 + RoomCategories.FRIENDLY.ordinal();
    public static final int TEMPORARY_CATEGORY = -897758079 + RoomCategories.TEMPORARY.ordinal();
    public static final int NATIONAL_CATEGORY = -897758079 + RoomCategories.NATIONAL.ordinal();
    private static final ResEntry[] contents = new ResEntry[]{new ResEntry("allowInform", 2031923633, "User \"{0}\" has given you access to their game. You may join and make comments.", "A message when somebody gives you access to a private game.", new Object[][]{{"wms"}, {"tromp"}}), new ResEntry("allowResultnu", 2031923636, "Sorry, user \"{0}\" does not seem to exist and cannot be allowed into your game.", "A message sent when a teacher tries to let a nonexistant account into a private game or tries to let a guest make comments in a lecture game.", new Object[][]{{"wms"}, {"tromp"}}), new ResEntry("DoneWasOverridden", 2031923681, "After you pressed \"Done\", you or your opponent clicked to mark stones dead. You must press \"Done\" again to end the game."), new ResEntry("GameRestarted4", 2031923639, "Your game \"{0}\" has been loaded on the server. Do you want to join the game so you can finish it? (If you are already playing in another game, you will have to close it before you can join another.)", "", new Object[][]{{"wms vs. tromp"}, {"glue vs. owl"}}), new ResEntry("GoErrorpt", 2031923640, "Sorry, you are already playing in one game, so you can't start playing in another."), new ResEntry("GoErrorngnr", 2031923644, "Sorry, the server is out of boards! Please wait a few minutes and try to start a game again."), new ResEntry("GoErrorbg", 2031923645, "Sorry, the game you tried to load was not correctly saved. This is probably caused by the server crashing. It cannot be recovered."), new ResEntry("allPlayersLeft", 2031923646, "Sorry, all players in this game have left. Nobody will be allowed to edit it until a player returns."), new ResEntry("GoErrorcr", 2031923647, "Your challenge has been declined."), new ResEntry("GoErrorul", 2031923648, "Sorry, user \"{0}\" has left the game you are starting before you could challenge them. You will have to play against somebody else.", "", new Object[]{"wms"}), new ResEntry("GoErrornt", 2031923649, "Sorry, this is a lecture game. Only authorized players are allowed to make comments until the teacher chooses to allow public input."), new ResEntry("GoErrorpko", 2031923650, "Sorry, this game is a private lesson. You will not be allowed to observe it."), new ResEntry("GuestLoginGreeting2", 2031923651, "Welcome to the KGS Go Server! As a guest you will have all of the abilities of a full user, but your account and your games will not be saved. Please register this account to make it permanent! To register, select the \"Register\" option of the \"User\" menu. It is completely free of charge."), new ResEntry("IoErrAfterConn", 2031923652, "Error \"{0}\" while communicating with server. You must log in again.", "", new Object[]{new EOFException()}), new ResEntry("IoErrOnConn", 2031923653, "Error \"{0}\" while connecting to machine \"{1}\".", "", new Object[]{new NoRouteToHostException(), "www.gokgs..com"}), new ResEntry("KeepOut", 2031923655, "Sorry, you cannot log in because this account or internet address is temporarily blocked. The reason for blocking is \"{0}\".", "A message that appears if you try to log in an an admin has banned you. It may happen to \"nice\" people if they share an internet address with a troublemaker, so don't be too unpleasant!", new Object[][]{{"I just don't like you"}, {"You are a jerk and you swear too much"}}), new ResEntry("Locale", 2031923656, "en_US", "This should be your standard locale that you are creating here."), new ResEntry("LR4", 2031923659, "Your password does not match the user name that you gave. Please re-type your password and try again. Make sure that caps lock is off! Your password is case-sensitive."), new ResEntry("LR5", 2031923660, "The user name you gave does not exist. Please check to make sure that you typed it correctly."), new ResEntry("LR6", 2031923661, "The user name you gave is already in use, so you can not use it as your guest login name. Please choose a different user name and try again."), new ResEntry("LR24", 2031923662, "You have been disconnected because another user has logged in with your account from another computer."), new ResEntry("LRtl", 2031923663, "You have been logged out because you had been idle for too long. You must log in again."), new ResEntry("NoHost", 2031923665, "Machine \"{0}\" could not be found.", "", new Object[]{"www.gokgs.com"}), new ResEntry("NoResultDesc", 2031923666, "The players are repeating the same board position and neither is willing to play differently. The game will never end, and has no result."), new ResEntry("NoRoute", 2031923668, "Your system cannot connect to \"{0}\". There is probably either a network problem or you are behind a firewall that is preventing you from connecting out."), new ResEntry("PlayerResigned", 2031923673, "{1} has resigned. {0} has won the game.", "", new Object[]{"rimas", "wms"}), new ResEntry("privChan", 2031923674, "Sorry, \"{0}\" is private. You cannot enter.", "An error message when a room or game is private and you tried to join it.", new Object[][]{{"wms [3k] vs. sensei [5p]"}, {"The Private Clubroom"}}), new ResEntry("ScoreResult2", 2031923677, "{8,choice,0#Black|1#White}: {1} territory {2,choice,0#|1#+ 1 capture |2#+ {2} captures }{3,choice,0#|1#+ 1 stone |2#+ {3} stones }{4,choice,0#|0.5#+ {4} dame }{5,choice,-0.1#- {6} komi |0#|0.1#+ {5} komi }{7,choice,0#|1#+ {7} handicap compensation }= {0}", "Here's a string saying the final score. In order, the arguments are:\n  arg(0) = Total score for the player\n  arg(1) = Territory\n  arg(2) = Captures for the player\n  arg(3) = Living stones for the player\n  arg(4) = Dame points for the player\n  arg(5) = Komi points for the player\n  arg(6) = Negative of the komi for the player\n  arg(7) = Handicap compensation for the player (as in AGA rules)\n  arg(8) = Color of the player (0=black, 1=white)\nMake sure that zero values don't appear (other than territory) and the komi is shown cleanly, as in \"- 2.5\" instead of \"+ -2.5\" when it is a negative komi.\n\nAll of the arguments are duplicated as arguments 9..16, but this time they are the pluralization categories of the numbers; see the README for more information on these", new Object[][]{{15.5, 8, 2, 0, 0, 5.5, -5.5, 0, 1, new Plural(15.5), new Plural(8.0), new Plural(2.0), new Plural(0.0), new Plural(0.0), new Plural(5.5), new Plural(-5.5), new Plural(0.0), new Plural(1.0)}, {65.0, 18, 0, 43, 2.5, -0.5, 0.5, 2, 1, new Plural(65.0), new Plural(18.0), new Plural(0.0), new Plural(43.0), new Plural(2.5), new Plural(-0.5), new Plural(0.5), new Plural(2.0), new Plural(1.0)}, {40.0, 35, 5, 0, 0, 0, 0, 2, 0, new Plural(40.0), new Plural(35.0), new Plural(5.0), new Plural(0.0), new Plural(0.0), new Plural(0.0), new Plural(0.0), new Plural(2.0), new Plural(0.0)}}), new ResEntry("ServerDown", 2031923678, "The system \"{0}\" is refusing connections. The server is probably down right now. Please try again later."), new ResEntry("TeacherLeft", 2031923679, "{0} has left this game. Editing control has been returned to you.", "", new Object[]{"wms"}), new ResEntry("UnexpectedClose", 2031923680, "The connection to the server has been closed unexpectedly. You must log in again."), new ResEntry("USrs2", 2031923683, "Thank you for registering at the KGS Go Server! Your account is now permanent. You should receive the password for this account in your email very soon. Be sure to use it right away! If you don't use your password within 24 hours, your account will be deleted."), new ResEntry("USug", 2031923684, "There is no user \"{0}\". Update failed.", "", new Object[]{"wms"}), new ResEntry("USfos", 2031923686, "User \"{0}\" has been forced off of the server.", "", new Object[]{"wms"}), new ResEntry("USfoao2", 2031923682, "No user named \"{0}\" has logged in recently, so your block could not take effect.", "", new Object[]{"wms"}), new ResEntry("USdas", 2031923669, "User account \"{0}\" has been successfully deleted.", "", new Object[]{"wms"}), new ResEntry("USdaag", 2031923641, "No user account named \"{0}\" exists, so it could not be deleted.", "", new Object[]{"wms"}), new ResEntry("USruf", 2031923657, "There is no user account named \"{0}\" on this system.", "", new Object[]{"wms"}), new ResEntry("YouAreOutOfTime2", 2031923654, "{1} has run out of time. {0} has won.", "", new Object[][]{{"wms", "Ella"}, {"Superman", "Batman"}}), new ResEntry("categoryMain", MAIN_CATEGORY, "Main", "The name of the room category that includes all the main per-laungage rooms."), new ResEntry("categoryClubs", CLUBS_CATEGORY, "Clubs", "The name of the room category that includes all the clubs that meet on KGS."), new ResEntry("categoryLessons", LESSONS_CATEGORY, "Lessons", "The name of the room category that includes all the rooms for teachers."), new ResEntry("categoryTournament", TOURNAMENT_CATEGORY, "Tournaments", "The name of the room category that includes all the rooms for tournaents."), new ResEntry("categoryFriendly", FRIENDLY_CATEGORY, "Social", "The name of the room category that includes all rooms that are for groups of friends to meet."), new ResEntry("categoryTemporary", TEMPORARY_CATEGORY, "New Rooms", "The name of the category for rooms that users have just created."), new ResEntry("categoryNational", NATIONAL_CATEGORY, "National", "The name of the category for rooms that go are for people from a specific country."), new ResEntry("noRankedInPrivate", 2031923670, "Sorry, you can't play a ranked game in a private room. This game will be free.", "A message when you try to play ranked in a private room."), new ResEntry("keepOutCleared", 2031923671, "User {0} is no longer blocked from logging in.", "A message an admin gets after clearing a keep out.", new Object[][]{{"wms"}, {"glue"}}), new ResEntry("keepOutNotCleared", 2031923672, "Sorry, no block matching that user exists.", "A message when an admin tries to clear a block against a user, and the user is not blocked."), new ResEntry("tooManyKeepOuts", 2031923664, "Sorry, you have blocked too many users recently. Please wait a few minutes then try again.", "A message to assitants when the've been using their block power too much."), new ResEntry("plusOnlyGame2", 2031923658, "This game is for KGS Plus members only. For information about membership, please visit http://{0}/kgsPlus.jsp", "A warning message you get when a non-subscriber tries to enter a subscribers-only game.", new Object[][]{{"www.gokgs.com"}, {"beta.gokgs.com"}}), new ResEntry("alreadyInPlayback", 2031923638, "You are already playing back a game! You must close your current playback game before you can start a new one."), new ResEntry("signedUpTooLate", 2031923667, "Sorry, you signed up for KGS Plus too late to watch this lecture! You can watch any lectures that started after you signed up, or in the week before you signed up."), new ResEntry("playbackError", 2031923634, "There was an error loading or playing the playback that you have selected. Please try again later."), new ResEntry("subscriptionLow", 2031923635, "Your KGS Plus subscription will expire in under {0} {0,choice,1#day|2#days}! If you want to continue with your subscription, you should visit http://{1}/ right away and extend your subscription.", "{0} is the number of days left (always at least 1), {1} is the host of the URL to resubscribe, and {2} is the plural code of {1}.", new Object[][]{{1, "www.gokgs.com", new Plural(1.0)}, {2, "www.gokgs.com", new Plural(2.0)}, {5, "www.goserver.com", new Plural(3.0)}, {7, "www.foo.org", new Plural(7.0)}}), new ResEntry("ownersGame", 2031923642, "{0}''s game", "Short description of a game", new Object[][]{{"wms"}, {"glue"}}), new ResEntry("ownersNamedGame", 2031923643, "{0}''s game: {1}", "Short description of a game, includes the owner and the name of the game.", new Object[][]{{"wms", "+/- 3 handicap stones please"}, {"glue", "Tournament Replay"}}), new ResEntry("GameIsOver", 2031923675, "{0} has ended. The final score is:", "A notice tha the game has ended. The argument is the name of the game. In the examples, the name of the game is in English - don't worry, in actual use it will be translated properly.", new Object[][]{{"wms vs. glue"}, {"patrickb vs. bazill"}}), new ResEntry("ScoreSummaryTie", 2031923676, "The game is a tie."), new ResEntry("ScoreSummaryWin2", 2031923685, "{0} has won by {1,choice,0.9#{1} points|1#1 point|1.1#{1} points}.", "Summary of the scores. Argument 0 is the color of the winner, 1 is the score difference, 2 is the pluralization code of 1. See the documentation for more on the pluralization code.", new Object[][]{{0, 0.5, new Plural(0.5)}, {1, 1.0, new Plural(1.0)}, {0, 8.0, new Plural(8.0)}}), new ResEntry("RankTypePrivate", 2031923687, "P", "This should be a one-character code for a private game."), new ResEntry("RankTypeSubscribers2", 2031923688, "+", "This should be a one-character code for a subscribers-only game."), new ResEntry("almostTooLong", 2031923689, "It has been a long time since you used this client. In 10 minutes, the server will assume that you are gone and log you out. If you want to stay logged in, then just press the \"OK\" button in this window to let the server know that you want to stay."), new ResEntry("Mv n", 2031923690, "Mv {0}", "The \"Mv\" is short for \"move\", as in \"move number\".", new Object[]{5}), new ResEntry("mustAllowNetwork", 2031923691, "You must allow connection to KGS in order to play on the server.")};

    @Override
    public String propFilePath() {
        return "org/igoweb/igoweb/client/res/Res";
    }

    public String toString() {
        return "Client Resources";
    }

    @Override
    public ResEntry[] getContents() {
        return contents;
    }

    @Override
    public Resource[] getChildren() {
        return new Resource[]{new SharedRes()};
    }
}
