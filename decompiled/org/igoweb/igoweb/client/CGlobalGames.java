/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.DataInputStream;
import java.io.IOException;
import org.igoweb.igoweb.client.CGameContainer;
import org.igoweb.igoweb.client.CGameListEntry;
import org.igoweb.igoweb.client.CRoom;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.GameContainers;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;
import org.igoweb.util.IntHashMap;

public class CGlobalGames
extends CGameContainer
implements EventListener {
    public final GameContainers type;
    private IntHashMap<Object> listenRooms = new IntHashMap();

    public CGlobalGames(Conn newConn, int chanId, CGameContainer.GameReader gameReader, DataInputStream in) throws IOException {
        super(newConn, chanId, gameReader);
        this.type = GameContainers.read(in);
        this.join(in);
    }

    @Override
    protected boolean addGame(CGameListEntry game) {
        Object chan;
        if (!super.addGame(game)) {
            return false;
        }
        int roomId = game.roomId;
        if (this.listenRooms.put(roomId, (Object)this) == null && (chan = this.conn.objects.get(roomId)) != null && chan instanceof CRoom) {
            CRoom room = (CRoom)chan;
            room.addListener(this);
            room.fetchName();
        }
        return true;
    }

    @Override
    protected boolean remove(CGameListEntry game) {
        if (!super.remove(game)) {
            return false;
        }
        int roomId = game.roomId;
        for (CGameListEntry entry : this.getGames().values()) {
            if (entry.roomId != roomId) continue;
            return true;
        }
        this.listenRooms.remove(roomId);
        Object chan = this.conn.objects.get(roomId);
        if (chan != null && chan instanceof CRoom) {
            ((CRoom)chan).removeListener(this);
        }
        return true;
    }

    @Override
    public void handleEvent(Event event) {
        if (event.type == 78) {
            int roomId = ((CRoom)event.source).id;
            for (CGameListEntry game : this.getGames().values()) {
                if (game.roomId != roomId) continue;
                this.emit(59, game);
            }
        } else if (event.type == 77) {
            ((CRoom)event.source).fetchName();
        }
    }

    @Override
    public String toString() {
        return "CGlobalGames[" + (Object)((Object)this.type) + "]";
    }
}
