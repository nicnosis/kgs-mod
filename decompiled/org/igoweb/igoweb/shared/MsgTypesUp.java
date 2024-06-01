/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

public enum MsgTypesUp {
    LOGIN(0, false),
    PING(1, false),
    UNJOIN_REQUEST(2, true),
    JOIN_ARCHIVE_REQUEST(3, false),
    CHAT(4, true),
    ANNOUNCE(5, true),
    ROOM_NAMES_REQUEST(6, false),
    DETAILS_JOIN_REQUEST(7, false),
    AVATAR_REQUEST(8, false),
    DETAILS_CHANGE(9, true),
    SET_PASSWORD(10, false),
    DETAILS_RANK_GRAPH_REQUEST(11, true),
    ROOM_DESC_REQUEST(13, true),
    JOIN_REQUEST(14, true),
    CREATE_ROOM_REQUEST(15, false),
    ROOM_EDIT(16, true),
    CHANNEL_ADD_ACCESS(17, true),
    CHANNEL_REMOVE_ACCESS(18, true),
    ROOM_ADD_OWNER(19, true),
    ROOM_REMOVE_OWNER(20, true),
    ACCESS_LIST_REQUEST(21, true),
    CONVO_REQUEST(22, false),
    MESSAGE_CREATE(23, false),
    MESSAGE_DELETE(24, false),
    FRIEND_ADD(25, false),
    FRIEND_REMOVE(26, false),
    CHALLENGE_CREATE(27, true),
    SYNC_REQUEST(28, false),
    CHALLENGE_PROPOSAL(29, true),
    GAME_MOVE(30, true),
    GAME_RESIGN(31, true),
    GAME_UNDO_REQUEST(32, true),
    GAME_UNDO_ACCEPT(33, true),
    MODERATED_COMMENT(34, true),
    SET_CHAT_MODE(35, true),
    GAME_TIME_EXPIRED(36, true),
    GAME_ADD_TIME(37, true),
    WAKE_UP(38, false),
    GAME_SET_ROLES(39, true),
    GAME_START_REVIEW(40, true),
    CHALLENGE_RETRY(41, true),
    GAME_LIST_ENTRY_SET_FLAGS(42, true),
    GAME_AUDIO(43, true),
    START_PLAYBACK(44, false),
    REQUEST_PLAYBACK_LIST(45, false),
    PLAYBACK_SET(46, true),
    GLOBAL_LIST_JOIN_REQUEST(47, false),
    UPLOAD_AVATAR(48, false),
    GAME_SET_ALLOW_CHAT(49, true),
    ANNOUNCEMENT(50, false),
    REQUEST_SERVER_STATS(51, false),
    DELETE_ACCOUNT(52, false),
    KEEP_OUT_REQUEST(53, false),
    CLEAR_KEEP_OUT(54, false),
    JOIN_GAME_BY_ID(55, false),
    ROOM_LOAD_GAME(56, true),
    SHUTDOWN(57, false),
    CHANNEL_DELETE(58, true),
    REGISTER(59, false),
    ROOM_CLONE_GAME(60, true),
    CHALLENGE_ACCEPT(61, true),
    CHALLENGE_SUBMIT(62, true),
    CONVO_NO_CHATS(63, true),
    CHALLENGE_DECLINE(64, true),
    AUTOMATCH_CREATE(65, false),
    AUTOMATCH_CANCEL(66, false),
    AUTOMATCH_SET_PREFS(67, false),
    IDLE_ON(68, false),
    TAG_GAME(69, false),
    FETCH_TAGS(70, false),
    JOIN_TAG_ARCHIVE_REQUEST(71, false),
    ADMIN_CLEAR_TAG(72, true),
    ANNOUNCE_TO_PLAYERS(73, true),
    LOGOUT(74, false),
    GAME_MARK_LIFE(255, true),
    GAME_SCORING_DONE(254, true),
    KGS_SGF_CHANGE(253, true),
    GAME_SET_SCORES(252, true);

    public final short id;
    public final boolean channel;
    public static final short FRIEND_NOTES_MAX_LENGTH = 50;
    public static final int KEEP_OUT_REASON_MAX_LENGTH = 500;
    public static final int MAX_TAG_LEN = 50;
    public static final int CHAT_MAX_LEN = 1000;

    private MsgTypesUp(int newId, boolean newChannel) {
        this.id = (short)newId;
        this.channel = newChannel;
    }

    public static class ChallengeValues {
        public static final int NOTES_MAX_LEN = 80;
    }

    public static class DetailsValues {
        public static final int MAX_NAME_LEN = 50;
        public static final int MAX_EMAIL_LEN = 70;
        public static final int MAX_INFO_LEN = 1500;
    }

    public static class UserMessageValues {
        public static final int MAX_LEN = 1000;
        public static final int MAX_COUNT = 10;
    }

    public static class RoomValues {
        public static final int NAME_MAX_LEN = 50;
        public static final int DESC_MAX_LEN = 1000;
    }

    public static class LoginValues {
        public static final int PASSWORD_LEN = 256;
        public static final int COMPRESSION_ABANDONED_MINOR_VERSION = 5;
        public static final int COMPRESSION_ABANDONED_BUGFIX_VERSION = 22;
    }
}
