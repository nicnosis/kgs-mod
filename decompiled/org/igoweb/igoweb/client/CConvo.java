/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import org.igoweb.igoweb.client.CChannel;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.igoweb.shared.MsgTypesUp;
import org.igoweb.igoweb.shared.User;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;

public class CConvo
extends CChannel
implements EventListener {
    static final String CLASS_PREFIX = "F1:";
    static final String CALLBACK_PREFIX = "F1a:";
    static final String ORIG_NAME_PREFIX = "F1b:";
    private static final int EVENT_BASE = 85;
    public static final int CONVO_STATE_CHANGED_EVENT = 85;
    public static final int CONVO_EVENT_LIMIT = 86;
    public final User peer;
    private boolean chatsBlocked;
    private State state;
    public final boolean isRequested;
    private boolean listenerAdded;

    CConvo(Conn newConn, int chanId, DataInputStream in, boolean newIsRequested) throws IOException {
        super(newConn, chanId);
        this.peer = newConn.getUser(in);
        this.isRequested = newIsRequested;
        newConn.objects.put(CLASS_PREFIX + this.peer.canonName(), this);
        this.members = new HashMap();
        this.members.put(this.peer.name, this.peer);
        User me = newConn.getMe();
        this.members.put(me.name, me);
        this.handleEvent(null);
        if (!this.listenerAdded) {
            this.peer.addListener(this);
            this.listenerAdded = true;
        }
    }

    @Override
    protected void close() {
        Object obj = this.conn.objects.remove(CLASS_PREFIX + this.peer.canonName());
        if (obj != this) {
            this.conn.objects.put(CLASS_PREFIX + this.peer.canonName(), obj);
        }
        if (this.listenerAdded) {
            this.peer.removeListener(this);
            this.listenerAdded = false;
        }
        super.close();
    }

    @Override
    protected void unjoin() {
        super.unjoin();
        this.close();
    }

    @Override
    protected void join(DataInputStream in) throws IOException {
        this.handleEvent(null);
        super.join(in);
        if (!this.listenerAdded) {
            this.peer.addListener(this);
            this.listenerAdded = true;
        }
    }

    @Override
    public void handleMessage(MsgTypesDown msgType, DataInputStream in) throws IOException {
        switch (msgType) {
            case CONVO_NO_CHATS: {
                if (this.chatsBlocked) break;
                this.chatsBlocked = true;
                this.handleEvent(null);
                break;
            }
            case CHAT: {
                if (this.chatsBlocked) {
                    this.chatsBlocked = false;
                    this.handleEvent(null);
                }
            }
            default: {
                super.handleMessage(msgType, in);
            }
        }
    }

    public static CConvo get(Conn srcConn, String user) {
        return (CConvo)srcConn.objects.get(CLASS_PREFIX + User.canonName(user));
    }

    public void sendNoChats() {
        this.conn.send(this.buildMessage(MsgTypesUp.CONVO_NO_CHATS));
    }

    @Override
    public boolean isCensored(String name) {
        return false;
    }

    @Override
    public void handleEvent(Event event) {
        State oldState;
        State newState = this.peer.isDeleted() ? State.PEER_GONE : (!this.peer.isConnected() ? State.PEER_DISCONNECTED : (this.chatsBlocked ? State.CHATS_BLOCKED : (this.peer.isInTourn() ? (this.conn.getMe().getAuthLevel() >= 3 ? State.PEER_IN_TOURN : State.PEER_IN_TOURN_NO_CHAT) : (this.peer.isPlaying() ? State.PEER_PLAYING : (this.peer.isSleeping() ? State.PEER_SLEEPING : State.NORMAL)))));
        if (newState != (oldState = this.state)) {
            this.state = newState;
            this.emit(85, (Object)oldState);
        }
    }

    public State getState() {
        return this.state;
    }

    @Override
    public boolean isChatsOk() {
        return this.state.isChatsOk();
    }

    public String toString() {
        return "CConvo[me=" + this.conn.getMe().name + ", peer=" + this.peer.name + ", requested=" + this.isRequested + "]";
    }

    public static enum State {
        PEER_GONE(false),
        PEER_DISCONNECTED(false),
        CHATS_BLOCKED(false),
        PEER_IN_TOURN_NO_CHAT(false),
        PEER_IN_TOURN(true),
        PEER_PLAYING(true),
        PEER_SLEEPING(true),
        NORMAL(true);

        private final boolean chatsOk;

        private State(boolean newChatsOk) {
            this.chatsOk = newChatsOk;
        }

        public boolean isChatsOk() {
            return this.chatsOk;
        }
    }
}
