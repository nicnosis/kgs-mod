/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.DataInput;
import java.io.IOException;

public class GameAction {
    public static final int MOVE_ID = 0;
    public static final GameAction MOVE = new GameAction(0, "MOVE");
    public static final int EDIT_ID = 1;
    public static final GameAction EDIT = new GameAction(1, "EDIT");
    public static final int SCORE_ID = 2;
    public static final GameAction SCORE = new GameAction(2, "SCORE");
    public static final int CHALLENGE_CREATE_ID = 3;
    public static final GameAction CHALLENGE_CREATE = new GameAction(3, "CHALLENGE_CREATE");
    public static final int CHALLENGE_SETUP_ID = 4;
    public static final GameAction CHALLENGE_SETUP = new GameAction(4, "CHALLENGE_SETUP");
    public static final int CHALLENGE_WAIT_ID = 5;
    public static final GameAction CHALLENGE_WAIT = new GameAction(5, "CHALLENGE_WAIT");
    public static final int CHALLENGE_ACCEPT_ID = 6;
    public static final GameAction CHALLENGE_ACCEPT = new GameAction(6, "CHALLENGE_ACCEPT");
    public static final int CHALLENGE_SUBMITTED_ID = 7;
    public static final GameAction CHALLENGE_SUBMITTED = new GameAction(7, "CHALLENGE_SUBMITTED");
    public static final int EDIT_DELAY_ID = 8;
    public static final GameAction EDIT_DELAY = new GameAction(8, "EDIT_DELAY");
    public final int id;
    public final String name;
    private static final GameAction[] idToAction = new GameAction[]{MOVE, EDIT, SCORE, CHALLENGE_CREATE, CHALLENGE_SETUP, CHALLENGE_WAIT, CHALLENGE_ACCEPT, CHALLENGE_SUBMITTED};

    private GameAction(int newId, String newName) {
        this.id = newId;
        this.name = newName;
    }

    public static GameAction get(int inputId) {
        return idToAction[inputId];
    }

    public static GameAction get(DataInput in) throws IOException {
        byte id = in.readByte();
        if (id < 0 || id >= idToAction.length) {
            throw new IOException("Invalid action ID: " + id);
        }
        return idToAction[id];
    }

    public String toString() {
        return "GameAction[" + this.name + "]";
    }
}
