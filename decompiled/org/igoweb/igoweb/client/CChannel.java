/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.igoweb.shared.MsgTypesUp;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.igoweb.shared.User;
import org.igoweb.util.Defs;
import org.igoweb.util.Emitter;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;

public abstract class CChannel
extends Emitter {
    public final int id;
    protected final Conn conn;
    private static final int MAX_CHATS_KEPT = 100;
    public static final int CHAT_MAX_LEN = 1000;
    private static final int EVENT_BASE = 12;
    private final LinkedList<Chat> chats = new LinkedList();
    private int numChatsSeen;
    private boolean newChats;
    public static final int INFORMATION_MSG_EVENT = 12;
    public static final int WARNING_MSG_EVENT = 13;
    public static final int ERROR_MSG_EVENT = 14;
    public static final int JOINED_EVENT = 15;
    public static final int UNJOINED_EVENT = 16;
    public static final int CHAT_EVENT = 17;
    public static final int USER_ADDED_EVENT = 19;
    public static final int USER_REMOVED_EVENT = 20;
    public static final int NEW_CHATS_FLAG_CHANGED_EVENT = 21;
    public static final int CLOSED_EVENT = 22;
    public static final int ACCESS_LIST_EVENT = 23;
    public static final int AUDIO_EVENT = 24;
    public static final int CHANGE_INVALID_USER_EVENT = 25;
    public static final int AD_EVENT = 26;
    public static final int CHAT_ALLOWED_EVENT = 27;
    public static final int ALREADY_JOINED_EVENT = 28;
    public static final int JOIN_COMPLETE_EVENT = 29;
    static final int CHANNEL_EVENT_LIMIT = 30;
    protected HashMap<String, User> members = null;
    private HashMap<String, User> accessList;

    protected CChannel(Conn newConn, int newId) {
        this.id = newId;
        this.conn = newConn;
        newConn.objects.put(newId, this);
    }

    protected TxMessage buildMessage(MsgTypesUp type) {
        TxMessage msg = new TxMessage(type);
        msg.writeInt(this.id);
        return msg;
    }

    protected void handleMessage(MsgTypesDown msgType, DataInputStream in) throws IOException {
        switch (msgType) {
            case CHAT: 
            case ANNOUNCE: 
            case MODERATED_CHAT: {
                User who = this.conn.getUser(in);
                if (msgType != MsgTypesDown.ANNOUNCE && this.isCensored(who)) break;
                Chat chat = new Chat(who, in.readUTF(), msgType == MsgTypesDown.ANNOUNCE, msgType == MsgTypesDown.MODERATED_CHAT);
                this.appendChat(chat);
                break;
            }
            case USER_ADDED: {
                this.addUser(this.conn.getUser(in));
                break;
            }
            case USER_REMOVED: {
                this.rmUser(this.conn.getUser(in));
                break;
            }
            case UNJOIN: {
                this.unjoin();
                break;
            }
            case CLOSE: {
                this.close();
                break;
            }
            case JOIN: 
            case ROOM_JOIN: 
            case GAME_JOIN: 
            case CHALLENGE_JOIN: {
                this.join(in);
                break;
            }
            case ACCESS_LIST: {
                this.accessList = new HashMap();
                while (in.available() > 0) {
                    User u = this.conn.getUser(in);
                    this.accessList.put(u.name, u);
                }
                this.emit(23, this.accessList.values());
                break;
            }
            case CHANNEL_AUDIO: {
                this.emit(24, in);
                break;
            }
            case CHANNEL_NO_TALKING: {
                this.emit(14, Defs.getString(2031923649));
                break;
            }
            case CHANNEL_CHANGE_NO_SUCH_USER: {
                this.emit(25, in.readUTF());
                break;
            }
            case CHANNEL_AD: {
                Object[] args = new Object[]{in.readByte(), in};
                this.emit(26, args);
                break;
            }
            case CHANNEL_CHAT_ALLOWED: {
                this.emit(27);
                break;
            }
            case CHANNEL_ALREADY_JOINED: {
                this.emit(28);
                break;
            }
            case JOIN_COMPLETE: {
                this.emit(29);
                break;
            }
            default: {
                System.err.println("Unknown message type " + (Object)((Object)msgType) + " for channel " + this);
            }
        }
    }

    protected void appendChat(Chat chat) {
        this.chats.add(chat);
        if (this.chats.size() > 100) {
            this.chats.removeFirst();
        }
        ++this.numChatsSeen;
        Event event = new Event(this, 17, chat);
        this.emit(event);
        if (!event.isConsumed() && !this.newChats && chat.isFromPeer()) {
            this.newChats = true;
            this.emit(21, true);
        }
    }

    public final boolean isCensored(User user) {
        return user != null && user.getAuthLevel() < 3 && this.isCensored(user.name);
    }

    public boolean isCensored(String userName) {
        return this.conn.friendsGroups[1].contains(userName);
    }

    protected void unjoin() {
        this.members = null;
        this.chats.clear();
        this.emit(16);
    }

    public boolean hasNewChats() {
        return this.newChats;
    }

    public boolean isChatsOk() {
        return true;
    }

    public int forwardChats(EventListener listener, int startPoint) {
        for (Chat chat : this.chats.subList(Math.max(0, this.chats.size() + startPoint - this.numChatsSeen), this.chats.size())) {
            Event event = new Event(this, 17, chat);
            listener.handleEvent(event);
            if (!this.newChats || !event.isConsumed()) continue;
            this.newChats = false;
            this.emit(21);
        }
        return this.numChatsSeen;
    }

    public void sendUnjoinRequest() {
        this.conn.send(this.buildMessage(MsgTypesUp.UNJOIN_REQUEST));
    }

    public final void sendChat(String text) {
        this.sendChat(text, false, false);
    }

    public final void sendChat(String text, boolean isAnnouncement) {
        this.sendChat(text, isAnnouncement, false);
    }

    public void sendChat(String text, boolean isAnnounce, boolean toAll) {
        if (text.length() > 1000) {
            text = text.substring(0, 1000);
        }
        TxMessage tx = this.buildMessage(isAnnounce ? (toAll ? MsgTypesUp.ANNOUNCE_TO_PLAYERS : MsgTypesUp.ANNOUNCE) : MsgTypesUp.CHAT);
        tx.writeUTF(text.replace('\n', ' '));
        this.conn.send(tx);
    }

    public HashMap<String, User> getMembers() {
        return this.members;
    }

    protected void join(DataInputStream in) throws IOException {
        this.members = new HashMap();
        if (in != null && in.available() > 0) {
            User user;
            while ((user = this.conn.getUser(in)) != null) {
                this.addUser(user);
            }
        }
        this.emit(15);
    }

    public void importChats(CChannel peer) {
        this.chats.clear();
        this.chats.addAll(peer.chats);
    }

    protected void addUser(User user) {
        if (this.members != null && this.members.put(user.name, user) == null) {
            this.emit(19, user);
        }
    }

    protected void rmUser(User user) {
        if (this.members != null && this.members.remove(user.name) != null) {
            this.emit(20, user);
        }
    }

    public boolean isJoined() {
        return this.members != null;
    }

    protected void close() {
        if (this.isJoined()) {
            this.members = null;
            this.chats.clear();
            this.emit(16);
        }
        this.conn.objects.remove(this.id);
        this.emit(22);
    }

    public boolean isClosed() {
        return this.conn.objects.get(this.id) != this;
    }

    public void sendJoinRequest() {
        this.conn.send(this.buildMessage(MsgTypesUp.JOIN_REQUEST));
    }

    public HashMap<String, User> getAccessList() {
        if (this.accessList == null && this.isPrivate()) {
            this.conn.send(this.buildMessage(MsgTypesUp.ACCESS_LIST_REQUEST));
            return new HashMap<String, User>();
        }
        return this.accessList;
    }

    protected boolean isPrivate() {
        return false;
    }

    public void sendDeleteRequest() {
        this.conn.send(this.buildMessage(MsgTypesUp.CHANNEL_DELETE));
    }

    public final String getDescription() {
        return this.getDescription(IBundle.get());
    }

    public String getDescription(IBundle bundle) {
        return "";
    }

    public void sendModeratedComment(String src, String comment) {
        TxMessage tx = this.buildMessage(MsgTypesUp.MODERATED_COMMENT);
        tx.writeUTF(User.canonName(src));
        tx.writeUTF(comment);
        this.conn.send(tx);
    }

    public final boolean isOwner() {
        return this.isOwner(this.conn.getMe());
    }

    public boolean isOwner(User who) {
        return false;
    }

    public void sendAddAccess(String name) {
        TxMessage tx = this.buildMessage(MsgTypesUp.CHANNEL_ADD_ACCESS);
        tx.writeUTF(name);
        this.conn.send(tx);
    }

    public void sendRemoveAccess(String name) {
        TxMessage tx = this.buildMessage(MsgTypesUp.CHANNEL_REMOVE_ACCESS);
        tx.writeUTF(name);
        this.conn.send(tx);
    }

    public void rejoin() {
        if (this.isJoined()) {
            this.emit(28);
        }
    }

    public static class Chat {
        public final User user;
        public final boolean announcement;
        public final boolean moderated;
        public final String text;

        public Chat(User newUser, String newText, boolean newAnnouncement, boolean newModerated) {
            this.user = newUser;
            this.announcement = newAnnouncement;
            this.moderated = newModerated;
            this.text = newText;
        }

        public boolean isFromPeer() {
            return true;
        }

        public String toString() {
            return "CChannel.Chat[User=" + this.user + ",len=" + this.text.length() + (this.announcement ? ",annc" : "") + (this.moderated ? ",moderated" : "") + "]";
        }
    }
}
