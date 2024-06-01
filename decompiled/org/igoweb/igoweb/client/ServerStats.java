/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.DataInput;
import java.io.IOException;
import org.igoweb.util.Emitter;

public class ServerStats
extends Emitter {
    public static final int LOGINS = 0;
    public static final int ACCOUNTS = 1;
    public static final int ROOMS = 2;
    public static final int GAMES = 3;
    public static final int RESOURCE_COUNT = 4;
    public final short versionMajor;
    public final short versionMinor;
    public final short versionBugfix;
    public final long startupTime;
    public final long bytesIn;
    public final long messagesIn;
    public final long bytesOut;
    public final long messagesOut;
    public final int pingTime;
    private int[] resources = new int[8];

    ServerStats(DataInput in, long newPingTime) throws IOException {
        this.pingTime = (int)newPingTime;
        this.versionMajor = in.readShort();
        this.versionMinor = in.readShort();
        this.versionBugfix = in.readShort();
        this.startupTime = in.readLong();
        this.bytesIn = in.readLong();
        this.messagesIn = in.readLong();
        this.bytesOut = in.readLong();
        in.readLong();
        this.messagesOut = in.readLong();
        for (int i = 0; i < 8; ++i) {
            this.resources[i] = in.readInt();
        }
    }

    public int getCurrent(int resource) {
        return this.resources[resource * 2];
    }

    public int getMax(int resource) {
        return this.resources[resource * 2 + 1];
    }
}
