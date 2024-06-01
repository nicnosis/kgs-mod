/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client;

import java.io.DataInputStream;
import java.io.IOException;
import org.igoweb.go.sgf.movie.MovieEvent;
import org.igoweb.igoweb.client.CPlayback;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.User;

public class KCPlayback
extends CPlayback {
    private static final int EVENT_BASE = 135;
    public static final int PLAYBACK_EVENT = 135;
    public static final int KCPLAYBACK_EVENT_LIMIT = 136;

    public KCPlayback(Conn conn, int id, long length, GameSummary<User> summary) {
        super(conn, id, length, summary);
    }

    @Override
    protected void handleData(DataInputStream in) throws IOException {
        while (in.available() > 0) {
            this.emit(135, MovieEvent.readMovieEvent(in, in.readByte(), 0));
        }
    }
}
