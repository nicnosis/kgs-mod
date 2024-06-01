/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.User;

public class PlaybackInfo {
    public final long id;
    public final boolean subscribersOnly;
    public final GameSummary<User> gameSummary;

    public PlaybackInfo(long id, boolean subscribersOnly, GameSummary<User> gameSummary) {
        this.id = id;
        this.subscribersOnly = subscribersOnly;
        this.gameSummary = gameSummary;
    }

    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == this.getClass() && ((PlaybackInfo)obj).id == this.id;
    }
}
