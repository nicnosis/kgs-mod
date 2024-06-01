/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.DataInputStream;
import java.io.IOException;
import org.igoweb.igoweb.client.CChannel;
import org.igoweb.igoweb.client.CRoom;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.igoweb.shared.MsgTypesUp;
import org.igoweb.igoweb.shared.RoomCategories;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.util.EventListener;
import org.igoweb.util.IntHashMap;

public class CRoomCatChannel
extends CChannel {
    private boolean joinWanted = false;
    public final RoomCategories category;
    private static final int EVENT_BASE = 83;
    public static final int ROOM_ADDED_EVENT = 83;
    public static final int ROOM_REMOVED_EVENT = 84;
    public static final int ROOM_CAT_EVENT_LIMIT = 85;
    private IntHashMap<CRoom> rooms;

    public CRoomCatChannel(Conn newConn, int newId, RoomCategories newCategory) {
        super(newConn, newId);
        this.category = newCategory;
        newConn.objects.put(newId, this);
    }

    @Override
    protected void handleMessage(MsgTypesDown msgType, DataInputStream in) throws IOException {
        switch (msgType) {
            case ROOM_CAT_COUNTERS: {
                if (!this.isJoined()) {
                    if (this.joinWanted) {
                        this.join(null);
                    } else {
                        super.sendUnjoinRequest();
                        return;
                    }
                }
                TxMessage tx = null;
                while (in.available() > 0) {
                    Object rawRoom = this.conn.objects.get(in.readInt());
                    short numUsers = in.readShort();
                    short numGames = in.readShort();
                    in.readByte();
                    if (rawRoom == null || !(rawRoom instanceof CRoom)) continue;
                    CRoom room = (CRoom)rawRoom;
                    if (!room.isNameCurrent()) {
                        if (tx == null) {
                            tx = new TxMessage(MsgTypesUp.ROOM_NAMES_REQUEST);
                        }
                        tx.writeInt(room.id);
                    }
                    room.setCounts(numUsers, numGames);
                    if (this.rooms.put(room.id, room) != null) continue;
                    this.emit(83, room);
                }
                if (tx == null) break;
                this.conn.send(tx);
                break;
            }
            case ROOM_CAT_ROOM_GONE: {
                Object rawRoom = this.conn.objects.get(in.readInt());
                if (rawRoom == null || !(rawRoom instanceof CRoom)) break;
                CRoom room = (CRoom)rawRoom;
                if (this.rooms == null || this.rooms.remove(room.id) == null) break;
                this.emit(84, room);
                break;
            }
            default: {
                super.handleMessage(msgType, in);
            }
        }
    }

    @Override
    protected void unjoin() {
        super.unjoin();
        this.rooms = null;
        if (this.joinWanted) {
            super.sendJoinRequest();
            this.rooms = new IntHashMap();
        }
    }

    @Override
    public void addListener(EventListener listener) {
        if (!this.hasListener()) {
            this.joinWanted = true;
            if (!this.isJoined()) {
                super.sendJoinRequest();
                this.rooms = new IntHashMap();
            }
        }
        super.addListener(listener);
        for (CRoom room : this.rooms.values()) {
            this.emit(83, room);
        }
    }

    @Override
    public void removeListener(EventListener listener) {
        super.removeListener(listener);
        if (!this.hasListener()) {
            this.joinWanted = false;
            if (this.isJoined()) {
                super.sendUnjoinRequest();
            }
        }
    }

    public IntHashMap<CRoom> getRooms() {
        return this.rooms == null ? new IntHashMap<CRoom>() : this.rooms;
    }

    @Override
    public void sendJoinRequest() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendUnjoinRequest() {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return "CRoomCatChannel[" + (Object)((Object)this.category) + "]";
    }
}
