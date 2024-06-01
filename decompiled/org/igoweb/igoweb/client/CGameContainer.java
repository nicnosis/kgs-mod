/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.DataInputStream;
import java.io.IOException;
import org.igoweb.igoweb.client.CChannel;
import org.igoweb.igoweb.client.CGameListEntry;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;
import org.igoweb.util.IntHashMap;

public class CGameContainer
extends CChannel {
    private static final int EVENT_BASE = 57;
    public static final int GAME_ADDED_EVENT = 57;
    public static final int GAME_REMOVED_EVENT = 58;
    public static final int GAME_CHANGED_EVENT = 59;
    public static final int GAME_CONTAINER_EVENT_LIMIT = 60;
    private final GameReader gameReader;
    private IntHashMap<CGameListEntry> games;
    private static final CGameListEntry[] referenceArray = new CGameListEntry[0];
    private final EventListener gameListener = this::handleGameEvent;

    public CGameContainer(Conn newConn, int newId, GameReader newGameReader) {
        super(newConn, newId);
        this.gameReader = newGameReader;
    }

    private void handleGameEvent(Event event) {
        if (this.games != null) {
            CGameListEntry src = (CGameListEntry)event.source;
            switch (event.type) {
                case 22: {
                    this.remove(src);
                    break;
                }
            }
        }
    }

    public IntHashMap<CGameListEntry> getGames() {
        return this.games;
    }

    public String toString() {
        return "GameContainer[" + this.id + ']';
    }

    @Override
    public void handleMessage(MsgTypesDown msgType, DataInputStream in) throws IOException {
        switch (msgType) {
            case GAME_LIST: {
                this.readGameList(in);
                break;
            }
            case GAME_CONTAINER_REMOVE_GAME: {
                if (this.games == null) break;
                while (in.available() > 0) {
                    CGameListEntry victim = this.games.get(in.readInt());
                    if (victim == null) continue;
                    this.remove(victim);
                }
                break;
            }
            default: {
                super.handleMessage(msgType, in);
            }
        }
    }

    protected boolean remove(CGameListEntry game) {
        if (this.games != null) {
            CGameListEntry removed = this.games.remove(game.id);
            if (removed == game) {
                this.emit(58, game);
                game.changeContainerCount(-1);
                game.removeListener(this.gameListener);
                return true;
            }
            System.err.println("Tried to remove " + game + " from " + this + ", got " + removed + " instead!");
            this.games.put(game.id, removed);
        }
        return false;
    }

    @Override
    protected void join(DataInputStream in) throws IOException {
        this.games = new IntHashMap();
        this.readGameList(in);
        super.join(in);
    }

    @Override
    protected void unjoin() {
        if (this.games != null) {
            for (CGameListEntry entry : this.games.values().toArray(referenceArray)) {
                this.remove(entry);
            }
        }
        this.games = null;
        super.unjoin();
    }

    private void readGameList(DataInputStream in) throws IOException {
        while (in.available() > 0) {
            CGameListEntry game = this.gameReader.readGame(in);
            if (game == null) {
                return;
            }
            if (this.games == null) continue;
            this.addGame(game);
        }
    }

    protected boolean addGame(CGameListEntry game) {
        CGameListEntry removed = this.games.put(game.id, game);
        if (removed == null) {
            game.changeContainerCount(1);
            game.addListener(this.gameListener);
            this.emit(57, game);
            return true;
        }
        if (removed == game) {
            this.emit(59, game);
            return false;
        }
        System.err.println("Added " + game + " to " + this + ", and it replaced " + removed);
        this.emit(58, removed);
        removed.changeContainerCount(-1);
        removed.removeListener(this.gameListener);
        game.changeContainerCount(1);
        game.addListener(this.gameListener);
        this.emit(57, game);
        return true;
    }

    public static interface GameReader {
        public CGameListEntry readGame(DataInputStream var1) throws IOException;
    }
}
