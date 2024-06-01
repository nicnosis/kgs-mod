/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.DataInputStream;
import java.io.IOException;
import org.igoweb.igoweb.client.CChannel;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.igoweb.shared.MsgTypesUp;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.igoweb.shared.User;

public abstract class CPlayback
extends CChannel {
    private static final int EVENT_BASE = 30;
    public static final int SEEK_START_EVENT = 30;
    public static final int SEEK_COMPLETE_EVENT = 31;
    public static final int CPLAYBACK_EVENT_LIMIT = 32;
    public final GameSummary<User> gameSummary;
    public final long length;

    public CPlayback(Conn newConn, int newId, long newLength, GameSummary<User> summary) {
        super(newConn, newId);
        this.length = newLength;
        this.gameSummary = summary;
    }

    @Override
    protected void handleMessage(MsgTypesDown msgType, DataInputStream in) throws IOException {
        switch (msgType) {
            case PLAYBACK_DATA: {
                this.handleData(in);
                break;
            }
            case PLAYBACK_SEEK_START: {
                this.emit(30);
                break;
            }
            case PLAYBACK_SEEK_COMPLETE: {
                this.emit(31);
                break;
            }
            default: {
                super.handleMessage(msgType, in);
            }
        }
    }

    protected abstract void handleData(DataInputStream var1) throws IOException;

    public void sendSetPosition(float speed, long position) {
        TxMessage tx = this.buildMessage(MsgTypesUp.PLAYBACK_SET);
        tx.writeLong(position);
        tx.writeFloat(speed);
        this.conn.send(tx);
    }
}
