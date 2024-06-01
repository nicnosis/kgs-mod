/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.gtp;

import org.igoweb.go.gtp.GtpException;

public class Command {
    public static final String LIST_COMMANDS = "list_commands";
    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String BOARDSIZE = "boardsize";
    public static final String KOMI = "komi";
    public static final String TIME_SETTINGS = "time_settings";
    public static final String KGS_TIME_SETTINGS = "kgs-time_settings";
    public static final String PLACE_FREE_HANDICAP = "place_free_handicap";
    public static final String SET_FREE_HANDICAP = "set_free_handicap";
    public static final String PLAY = "play";
    public static final String GENMOVE = "genmove";
    public static final String KGS_GENMOVE_CLEANUP = "kgs-genmove_cleanup";
    public static final String CLEAR_BOARD = "clear_board";
    public static final String TIME_LEFT = "time_left";
    public static final String FINAL_STATUS_LIST = "final_status_list";
    public static final String UNDO = "undo";
    public static final String QUIT = "quit";
    public static final String CHAT = "kgs-chat";
    public static final String GAME_OVER = "kgs-game_over";
    public static final String KGS_RULES = "kgs-rules";
    public final String text;
    private boolean mustSucceed;

    public Command(String text) {
        this(text, false);
    }

    public Command(String text, boolean mustSucceed) {
        this.text = text;
        this.mustSucceed = mustSucceed;
    }

    public void responseReceived(String resp, boolean success) throws GtpException {
        if (this.mustSucceed && !success) {
            throw new GtpException("Command was required to succeed, but got an error response: " + this.text);
        }
    }
}
