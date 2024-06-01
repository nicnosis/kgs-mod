/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import org.igoweb.igoweb.shared.MsgTypesDown;

static class Client.1 {
    static final /* synthetic */ int[] $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown;

    static {
        $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown = new int[MsgTypesDown.values().length];
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.HELLO.ordinal()] = 1;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.USER_UPDATE.ordinal()] = 2;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.LOGIN_FAILED_NO_SUCH_USER.ordinal()] = 3;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.LOGIN_FAILED_BAD_PASSWORD.ordinal()] = 4;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.LOGIN_SUCCESS.ordinal()] = 5;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.LOGIN_FAILED_USER_ALREADY_EXISTS.ordinal()] = 6;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.LOGIN_FAILED_KEEP_OUT.ordinal()] = 7;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.RECONNECT.ordinal()] = 8;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.DETAILS_JOIN.ordinal()] = 9;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.DETAILS_NONEXISTANT.ordinal()] = 10;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.ROOM_NAMES.ordinal()] = 11;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.ROOM_CREATED.ordinal()] = 12;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.ROOM_CREATE_TOO_MANY_ROOMS.ordinal()] = 13;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.ROOM_CREATE_NAME_TAKEN.ordinal()] = 14;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.ROOM_CHANNEL_INFO.ordinal()] = 15;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.CONVO_JOIN.ordinal()] = 16;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.CONVO_NO_SUCH_USER.ordinal()] = 17;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.MESSAGES.ordinal()] = 18;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.MESSAGE_CREATE_SUCCESS.ordinal()] = 19;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.MESSAGE_CREATE_NO_USER.ordinal()] = 20;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.MESSAGE_CREATE_FULL.ordinal()] = 21;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.MESSAGE_CREATE_CONNECTED.ordinal()] = 22;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.FRIEND_CHANGE_NO_USER.ordinal()] = 23;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.FRIEND_ADD_SUCCESS.ordinal()] = 24;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.FRIEND_REMOVE_SUCCESS.ordinal()] = 25;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.CHALLENGE_CREATED.ordinal()] = 26;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.CHALLENGE_NOT_CREATED.ordinal()] = 27;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.ANNOUNCEMENT.ordinal()] = 28;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.SERVER_STATS.ordinal()] = 29;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.DELETE_ACCOUNT_ALREADY_GONE.ordinal()] = 30;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.KEEP_OUT_SUCCESS.ordinal()] = 31;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.KEEP_OUT_LOGIN_NOT_FOUND.ordinal()] = 32;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.DELETE_ACCOUNT_SUCCESS.ordinal()] = 33;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.IDLE_WARNING.ordinal()] = 34;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.CANT_PLAY_TWICE.ordinal()] = 35;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.LOAD_FAILED.ordinal()] = 36;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.GAME_STARTED.ordinal()] = 37;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.ARCHIVE_JOIN.ordinal()] = 38;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.TAG_ARCHIVE_JOIN.ordinal()] = 39;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.ARCHIVE_NONEXISTANT.ordinal()] = 40;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.TAG_ARCHIVE_NONEXISTANT.ordinal()] = 41;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.AVATAR.ordinal()] = 42;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.CLEAR_KEEP_OUT_SUCCESS.ordinal()] = 43;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.CLEAR_KEEP_OUT_FAILURE.ordinal()] = 44;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.TOO_MANY_KEEP_OUTS.ordinal()] = 45;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.SYNC.ordinal()] = 46;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.GAME_NOTIFY.ordinal()] = 47;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.GAME_REVIEW.ordinal()] = 48;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.PLAYBACK_ADD.ordinal()] = 49;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.PLAYBACK_DELETE.ordinal()] = 50;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.ALREADY_IN_PLAYBACK.ordinal()] = 51;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.PLAYBACK_ERROR.ordinal()] = 52;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.PLAYBACK_SETUP.ordinal()] = 53;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.GLOBAL_GAMES_JOIN.ordinal()] = 54;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.SUBSCRIPTION_UPDATE.ordinal()] = 55;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.REGISTER_SUCCESS.ordinal()] = 56;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.REGISTER_BAD_EMAIL.ordinal()] = 57;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.PRIVATE_KEEP_OUT.ordinal()] = 58;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.CHANNEL_SUBSCRIBERS_ONLY.ordinal()] = 59;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.SUBSCRIPTION_LOW.ordinal()] = 60;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.AD.ordinal()] = 61;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.TOURN_NOTIFY.ordinal()] = 62;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.AUTOMATCH_STATUS.ordinal()] = 63;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.AUTOMATCH_PREFS.ordinal()] = 64;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            Client.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.FETCH_TAGS_RESULT.ordinal()] = 65;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
    }
}
