/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.igoweb.igoweb.client.CChannel;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.igoweb.shared.MsgTypesUp;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.igoweb.shared.User;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;
import org.igoweb.util.LongHashMap;
import org.igoweb.util.Multicaster;

public abstract class CArchive
extends CChannel {
    static final String ARCHIVE_CLASS_PREFIX = "F2:";
    static final String TAG_ARCHIVE_CLASS_PREFIX = "E-:";
    static final String ARCHIVE_CALLBACK_PREFIX = "F2a:";
    static final String TAG_ARCHIVE_CALLBACK_PREFIX = "E-a:";
    private static final int EVENT_BASE = 35;
    public static final int ARCHIVE_JOINED_EVENT = 35;
    public static final int GAME_ADDED_EVENT = 36;
    public static final int GAME_REMOVED_EVENT = 37;
    public static final int ARCHIVE_EVENT_LIMIT = 38;
    private final LongHashMap<GameSummary<?>> games = new LongHashMap();
    public final User owner;
    private final LongHashMap<String> tags;

    protected CArchive(DataInputStream in, Conn newConn, boolean isTagArchive) throws IOException {
        super(newConn, in.readInt());
        this.owner = newConn.getUser(in);
        LongHashMap longHashMap = this.tags = isTagArchive ? new LongHashMap() : null;
        while (in.available() > 0) {
            GameSummary<?> game = this.loadGameSummary(in);
            this.games.put(game.id, game);
            if (!isTagArchive) continue;
            this.tags.put(game.id, in.readUTF());
        }
        EventListener listener = (EventListener)newConn.objects.remove((isTagArchive ? TAG_ARCHIVE_CALLBACK_PREFIX : ARCHIVE_CALLBACK_PREFIX) + this.owner.canonName());
        if (listener != null) {
            this.addListener(listener);
        }
    }

    protected abstract GameSummary<?> loadGameSummary(DataInputStream var1) throws IOException;

    static CArchive get(Conn localConn, String name, boolean tag) {
        return (CArchive)localConn.objects.get((tag ? TAG_ARCHIVE_CLASS_PREFIX : ARCHIVE_CLASS_PREFIX) + name);
    }

    static void addListener(Conn localConn, String name, EventListener newListener, boolean tagArchive) {
        CArchive arc = CArchive.get(localConn, name = User.canonName(name), tagArchive);
        if (arc == null) {
            String key = (tagArchive ? TAG_ARCHIVE_CALLBACK_PREFIX : ARCHIVE_CALLBACK_PREFIX) + name;
            localConn.objects.put(key, Multicaster.add((EventListener)localConn.objects.get(key), newListener));
            TxMessage msg = new TxMessage(tagArchive ? MsgTypesUp.JOIN_TAG_ARCHIVE_REQUEST : MsgTypesUp.JOIN_ARCHIVE_REQUEST);
            msg.writeUTF(name);
            localConn.send(msg);
        } else {
            arc.addListener(newListener);
        }
    }

    public Collection<GameSummary<?>> getGames() {
        return Collections.unmodifiableCollection(this.games.values());
    }

    public LongHashMap<String> getTags() {
        if (this.tags == null) {
            throw new RuntimeException();
        }
        return this.tags;
    }

    public GameSummary<?> getGame(long dateStamp) {
        return this.games.get(dateStamp);
    }

    public String getTag(long gameId) {
        return this.tags.get(gameId);
    }

    @Override
    public void addListener(EventListener newListener) {
        super.addListener(newListener);
        Collection allGames = this.games.values();
        newListener.handleEvent(new Event(this, 35, allGames));
    }

    @Override
    public void removeListener(EventListener victim) {
        super.removeListener(victim);
        if (!this.hasListener()) {
            this.conn.send(this.buildMessage(MsgTypesUp.UNJOIN_REQUEST));
            this.conn.objects.remove(this.id);
            this.conn.objects.remove(this.getClassPrefix() + this.owner.canonName());
        }
    }

    @Override
    protected void handleMessage(MsgTypesDown msgType, DataInputStream in) throws IOException {
        switch (msgType) {
            case ARCHIVE_GAMES_CHANGED: {
                while (in.available() > 0) {
                    GameSummary<?> gsum = this.loadGameSummary(in);
                    this.games.put(gsum.id, gsum);
                    if (this.tags != null) {
                        this.tags.put(gsum.id, in.readUTF());
                    }
                    this.emit(36, gsum);
                }
                break;
            }
            case ARCHIVE_GAME_REMOVED: {
                GameSummary<?> gsum = this.games.remove(in.readLong());
                if (gsum == null) break;
                this.emit(37, gsum);
                break;
            }
            default: {
                super.handleMessage(msgType, in);
            }
        }
    }

    public boolean isTagArchive() {
        return this.tags != null;
    }

    public void adminClearTag(GameSummary<?> summary) {
        TxMessage msg = this.buildMessage(MsgTypesUp.ADMIN_CLEAR_TAG);
        msg.writeLong(summary.id);
        this.conn.send(msg);
    }

    public String getClassPrefix() {
        return this.tags == null ? ARCHIVE_CLASS_PREFIX : TAG_ARCHIVE_CLASS_PREFIX;
    }

    public String toString() {
        return this.getClass().getSimpleName() + '[' + this.owner + ']';
    }
}
