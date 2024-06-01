/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.TreeSet;
import org.igoweb.igoweb.client.CChannel;
import org.igoweb.igoweb.client.CGame;
import org.igoweb.igoweb.client.CGameContainer;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.igoweb.shared.MsgTypesUp;
import org.igoweb.igoweb.shared.Proposal;
import org.igoweb.igoweb.shared.RoomCategories;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.igoweb.shared.User;
import org.igoweb.util.Defs;
import org.igoweb.util.EventListener;

public class CRoom
extends CGameContainer
implements Comparable<Object> {
    public static final int NAME_MAX_LEN = 50;
    public static final int DESC_MAX_LEN = 1000;
    private static final int EVENT_BASE = 74;
    static final String CHALLENGE_PREFIX = "g:";
    public static final int COUNTS_CHANGED_EVENT = 74;
    public static final int GAME_IN_ROOM_EVENT = 75;
    public static final int ROOM_GONE_EVENT = 76;
    public static final int NAME_FLUSHED_EVENT = 77;
    public static final int NAME_AND_FLAGS_CHANGED_EVENT = 78;
    public static final int CATEGORY_CHANGED_EVENT = 79;
    public static final int DESC_CHANGED_EVENT = 80;
    public static final int OWNERS_CHANGED_EVENT = 81;
    public static final int DESC_AND_OWNERS_INVALID_EVENT = 82;
    public static final int ROOM_EVENT_LIMIT = 83;
    private int numUsers = 0;
    private int numGames = 0;
    private String name;
    private RoomCategories category;
    private int flags;
    private static short nextGameReqId = 0;
    private String desc;
    private TreeSet<User> owners;

    public CRoom(Conn newConn, int newId, RoomCategories newCategory, CGameContainer.GameReader gameReader) {
        super(newConn, newId, gameReader);
        this.category = newCategory;
    }

    public void setCounts(int newNumUsers, int newNumGames) {
        if (this.numUsers != newNumUsers || this.numGames != newNumGames) {
            this.numUsers = newNumUsers;
            this.numGames = newNumGames;
            if (!this.isJoined()) {
                this.emit(74);
            }
        }
    }

    public final String getName() {
        if (this.category == RoomCategories.SPECIAL) {
            return Defs.getString(-669080757);
        }
        return this.name == null ? "" : this.name;
    }

    @Override
    public int compareTo(Object o) {
        return this.getName().compareToIgnoreCase(((CRoom)o).getName());
    }

    public boolean isTournOnly() {
        return (this.flags & 2) != 0;
    }

    @Override
    public boolean isPrivate() {
        return (this.flags & 1) != 0;
    }

    public boolean isGlobalGamesOnly() {
        return (this.flags & 4) != 0;
    }

    public RoomCategories getCategory() {
        return this.category;
    }

    public final boolean isAutomatch() {
        return this.category == RoomCategories.SPECIAL;
    }

    @Override
    public void handleMessage(MsgTypesDown msgType, DataInputStream in) throws IOException {
        switch (msgType) {
            case ROOM_DESC: {
                User user;
                String oldDesc = this.desc;
                TreeSet<User> oldOwners = this.owners;
                in.readByte();
                this.desc = in.readUTF();
                this.owners = new TreeSet();
                while (in.available() > 0 && (user = this.conn.getUser(in)) != null) {
                    this.owners.add(user);
                }
                if (!this.desc.equals(oldDesc)) {
                    if (this.members != null) {
                        this.appendChat(new RoomDesc(this.desc));
                    }
                    this.emit(80, this.desc);
                }
                if (this.owners.equals(oldOwners)) break;
                this.emit(81);
                break;
            }
            case ROOM_NAME_FLUSH: {
                if (this.isJoined()) break;
                this.name = null;
                this.emit(77);
                break;
            }
            default: {
                super.handleMessage(msgType, in);
            }
        }
    }

    void updateName(DataInputStream in) throws IOException {
        this.name = in.readUTF();
        this.flags = in.readShort();
        this.emit(78);
    }

    public void requestLoadGame(long gameId, boolean isPrivate) {
        TxMessage msg = this.buildMessage(MsgTypesUp.ROOM_LOAD_GAME);
        msg.writeBoolean(isPrivate);
        msg.writeLong(gameId);
        this.conn.send(msg);
    }

    public void sendNewGameRequest(Proposal<?, ?, ?> prop, String notes, boolean isGlobal, EventListener listener) {
        TxMessage tx = this.buildMessage(MsgTypesUp.CHALLENGE_CREATE);
        nextGameReqId = (short)(nextGameReqId + 1);
        tx.writeShort(nextGameReqId);
        tx.writeBoolean(isGlobal);
        tx.writeUTF(notes);
        prop.writeTo(tx);
        this.conn.send(tx);
        tx.writeUTF(notes);
        if (listener == null) {
            throw new IllegalArgumentException();
        }
        this.conn.objects.put(CHALLENGE_PREFIX + nextGameReqId, listener);
    }

    public String getDesc() {
        if (this.desc == null) {
            this.conn.send(this.buildMessage(MsgTypesUp.ROOM_DESC_REQUEST));
            this.desc = "";
        }
        return this.desc;
    }

    public boolean isPermanent() {
        return this.category != RoomCategories.TEMPORARY;
    }

    public TreeSet<User> getOwners() {
        return this.owners == null ? new TreeSet<User>() : this.owners;
    }

    public boolean canPlayRanked() {
        return (this.flags & 3) == 0;
    }

    void setCategory(RoomCategories newCategory) {
        if (newCategory != this.category) {
            this.category = newCategory;
            this.emit(79);
        }
    }

    public boolean isInfoKnown() {
        return !this.name.isEmpty();
    }

    @Override
    public String toString() {
        return "Room[id=" + this.id + ", user=" + this.conn.getMe().name + ", name=" + this.name + "]";
    }

    public int getNumUsers() {
        return this.isJoined() ? this.getMembers().size() : this.numUsers;
    }

    public int getNumGames() {
        return this.isJoined() ? this.getGames().size() : this.numGames;
    }

    public boolean fetchName() {
        if (this.name == null) {
            this.conn.send(this.buildMessage(MsgTypesUp.ROOM_NAMES_REQUEST));
            this.name = "";
            return false;
        }
        return true;
    }

    boolean isNameCurrent() {
        return this.name != null;
    }

    public void sendAddOwner(String newOwner) {
        TxMessage tx = this.buildMessage(MsgTypesUp.ROOM_ADD_OWNER);
        tx.writeUTF(newOwner);
        this.conn.send(tx);
    }

    public void sendRemoveOwner(String prevOwner) {
        TxMessage tx = this.buildMessage(MsgTypesUp.ROOM_REMOVE_OWNER);
        tx.writeUTF(prevOwner);
        this.conn.send(tx);
    }

    public void sendChanges(String newName, int newCategory, boolean tournOnly, boolean isPrivate, boolean isGlobalGamesOnly, String newDesc) {
        TxMessage tx = this.buildMessage(MsgTypesUp.ROOM_EDIT);
        tx.writeUTF(newName);
        tx.write(newCategory);
        short roomFlags = 0;
        if (isPrivate) {
            roomFlags = (short)(roomFlags | 1);
        }
        if (tournOnly) {
            roomFlags = (short)(roomFlags | 2);
        }
        if (isGlobalGamesOnly) {
            roomFlags = (short)(roomFlags | 4);
        }
        tx.writeShort(roomFlags);
        tx.writeUTF(newDesc);
        this.conn.send(tx);
        if (!this.isJoined()) {
            this.name = newName;
            this.desc = newDesc;
        }
    }

    @Override
    protected void join(DataInputStream in) throws IOException {
        in.readByte();
        this.conn.subscribedRooms.add(this);
        super.join(in);
        if (this.desc == null) {
            this.getDesc();
        } else {
            this.appendChat(new RoomDesc(this.desc));
        }
    }

    @Override
    protected void unjoin() {
        this.conn.subscribedRooms.remove(this);
        super.unjoin();
    }

    @Override
    protected void close() {
        this.conn.subscribedRooms.remove(this);
        super.close();
    }

    @Override
    public String getDescription(IBundle bundle) {
        return this.name == null ? "-" : this.name;
    }

    public void sendCloneGameRequest(CGame game) {
        TxMessage tx = this.buildMessage(MsgTypesUp.ROOM_CLONE_GAME);
        tx.writeInt(game.id);
        this.conn.send(tx);
    }

    @Override
    public boolean isOwner(User user) {
        return this.owners.contains(user);
    }

    public static class RoomDesc
    extends CChannel.Chat {
        public RoomDesc(String newText) {
            super(null, newText, false, false);
        }

        @Override
        public boolean isFromPeer() {
            return false;
        }
    }
}
