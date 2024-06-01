/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import org.igoweb.igoweb.client.CGameListEntry;
import org.igoweb.igoweb.client.CRoom;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;

private class Client.TournNotifyHandler
implements EventListener {
    private CGameListEntry game;
    private boolean hasStarted;

    public Client.TournNotifyHandler(CRoom room, CGameListEntry newGame, boolean newHasStarted) {
        if (room == null || newGame == null) {
            return;
        }
        this.game = newGame;
        this.hasStarted = newHasStarted;
        if (room.fetchName()) {
            this.handleEvent(new Event(room, 78));
        } else {
            room.addListener(this);
        }
    }

    @Override
    public void handleEvent(Event event) {
        if (event.type == 78) {
            CRoom room = (CRoom)event.source;
            room.removeListener(this);
            Client.this.emit(109, new Object[]{room, this.game, this.hasStarted});
        }
    }
}
