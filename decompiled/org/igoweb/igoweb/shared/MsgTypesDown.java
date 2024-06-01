/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.DataInputStream;
import java.io.IOException;
import org.igoweb.igoweb.shared.TxMessage;

public enum MsgTypesDown {
    HELLO(0),
    USER_UPDATE(1),
    LOGIN_FAILED_NO_SUCH_USER(2),
    LOGIN_FAILED_BAD_PASSWORD(3),
    LOGIN_FAILED_USER_ALREADY_EXISTS(4),
    RECONNECT(5),
    LOGIN_SUCCESS(6),
    ARCHIVE_JOIN(7),
    UNJOIN(-1),
    ARCHIVE_GAMES_CHANGED(-2),
    ARCHIVE_GAME_REMOVED(-3),
    CLOSE(-4),
    ARCHIVE_NONEXISTANT(8),
    ROOM_NAMES(9),
    PRIVATE_KEEP_OUT(10),
    JOIN(-11),
    USER_ADDED(-12),
    CHAT(-13),
    ANNOUNCE(-14),
    USER_REMOVED(-15),
    DETAILS_JOIN(16),
    DETAILS_UPDATE(-17),
    DETAILS_NONEXISTANT(11),
    AVATAR(12),
    DETAILS_RANK_GRAPH(-18),
    ROOM_CAT_COUNTERS(-19),
    ROOM_DESC(-20),
    ROOM_NAME_FLUSH(-21),
    ROOM_CREATED(22),
    CONVO_JOIN(23),
    CONVO_NO_SUCH_USER(24),
    MESSAGES(25),
    MESSAGE_CREATE_SUCCESS(26),
    MESSAGE_CREATE_NO_USER(27),
    MESSAGE_CREATE_FULL(28),
    MESSAGE_CREATE_CONNECTED(29),
    FRIEND_CHANGE_NO_USER(30),
    FRIEND_ADD_SUCCESS(31),
    FRIEND_REMOVE_SUCCESS(32),
    CHALLENGE_CREATED(33),
    CHALLENGE_NOT_CREATED(34),
    CHALLENGE_CANT_PLAY_RANKED(-35),
    IDLE_WARNING(36),
    GAME_LIST(-37),
    SYNC(38),
    CHALLENGE_PROPOSAL(-39),
    GAME_NOTIFY(40),
    GAME_STATE(-41),
    GAME_UPDATE(-42),
    GAME_UNDO_REQUEST(-43),
    MODERATED_CHAT(-44),
    SET_CHAT_MODE(-45),
    GAME_TIME_EXPIRED(-46),
    GAME_OVER(-47),
    GAME_REVIEW(48),
    LOAD_FAILED(49),
    CHANNEL_AUDIO(-50),
    PLAYBACK_ADD(51),
    PLAYBACK_SETUP(52),
    PLAYBACK_DATA(-53),
    GLOBAL_GAMES_JOIN(54),
    SUBSCRIPTION_UPDATE(55),
    CHALLENGE_FINAL(-56),
    ANNOUNCEMENT(57),
    SERVER_STATS(58),
    DELETE_ACCOUNT_ALREADY_GONE(59),
    DELETE_ACCOUNT_SUCCESS(60),
    CHANNEL_SUBSCRIBERS_ONLY(61),
    GAME_ALL_PLAYERS_GONE(-62),
    GAME_EDITOR_LEFT(-63),
    CHANNEL_NO_TALKING(-64),
    LOGIN_FAILED_KEEP_OUT(65),
    TOO_MANY_KEEP_OUTS(66),
    KEEP_OUT_SUCCESS(67),
    KEEP_OUT_LOGIN_NOT_FOUND(68),
    CLEAR_KEEP_OUT_SUCCESS(69),
    CLEAR_KEEP_OUT_FAILURE(70),
    CANT_PLAY_TWICE(71),
    ALREADY_IN_PLAYBACK(72),
    GAME_STARTED(73),
    REGISTER_SUCCESS(74),
    REGISTER_BAD_EMAIL(75),
    GAME_CONTAINER_REMOVE_GAME(-76),
    PLAYBACK_ERROR(77),
    ROOM_CREATE_TOO_MANY_ROOMS(78),
    ROOM_CREATE_NAME_TAKEN(79),
    ROOM_CHANNEL_INFO(80),
    CHANNEL_CHANGE_NO_SUCH_USER(-81),
    ACCESS_LIST(-82),
    PLAYBACK_DELETE(83),
    CHALLENGE_SUBMIT(-84),
    SUBSCRIPTION_LOW(85),
    ROOM_CAT_ROOM_GONE(-86),
    CONVO_NO_CHATS(-87),
    GAME_SET_ROLES_UNKNOWN_USER(-88),
    GAME_SET_ROLES_NOT_MEMBER(-89),
    CHALLENGE_DECLINE(-90),
    GAMELISTENTRY_PLAYER_REPLACED(-91),
    CHANNEL_AD(-92),
    AD(93),
    CHANNEL_CHAT_ALLOWED(-94),
    CHANNEL_ALREADY_JOINED(-95),
    TOURN_NOTIFY(96),
    GAME_PREP_STATUS(-96),
    PLAYBACK_SEEK_START(-97),
    PLAYBACK_SEEK_COMPLETE(-98),
    AUTOMATCH_PREFS(99),
    AUTOMATCH_STATUS(100),
    GAME_NAME_CHANGE(-101),
    FETCH_TAGS_RESULT(102),
    TAG_ARCHIVE_JOIN(103),
    TAG_ARCHIVE_NONEXISTANT(104),
    JOIN_COMPLETE(-105),
    ROOM_JOIN(-106),
    GAME_JOIN(-107),
    CHALLENGE_JOIN(-108),
    GAME_LOOP_WARNING(-254),
    LOGOUT(106);

    public static final int DETAILS_FORCED_NO_RANK = 1;
    public static final int DETAILS_EMAIL_WANTED = 2;
    public static final int DETAILS_EMAIL_PRIVATE = 4;
    public static final int DETAILS_RANK_WANTED = 8;
    public static final int MAX_FRAMES_PER_PACKET = 63;
    public final short id;

    private MsgTypesDown(int newId) {
        this.id = (short)newId;
    }

    public boolean isStatic() {
        return this.id >= 0;
    }

    public static enum GamePrep {
        END,
        TOURNAMENT,
        AUTOMATCH;


        public static GamePrep read(DataInputStream in) throws IOException {
            byte id = in.readByte();
            try {
                return GamePrep.values()[id];
            }
            catch (IndexOutOfBoundsException excep) {
                throw new IOException("Illegal GamePrep: " + id);
            }
        }

        public void writeTo(TxMessage tx) {
            tx.writeByte(this.ordinal());
        }
    }
}
