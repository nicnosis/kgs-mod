/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.DataInputStream;
import java.io.IOException;
import org.igoweb.igoweb.client.CChannel;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.GameAction;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.igoweb.shared.MsgTypesUp;
import org.igoweb.igoweb.shared.PlayerContainer;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.igoweb.shared.User;
import org.igoweb.util.FlatSet;

public abstract class CGameListEntry
extends CChannel
implements PlayerContainer<User> {
    private static final int EVENT_BASE = 38;
    public static final int RENDER_CHANGED_EVENT = 38;
    public static final int NAME_CHANGED_EVENT = 39;
    public static final int GAME_STATE_CHANGED_EVENT = 40;
    private static final int FLAG_BASE = 41;
    public static final int OVER_CHANGED_EVENT = 41;
    public static final int ADJOURNED_CHANGED_EVENT = 42;
    public static final int PRIVATE_CHANGED_EVENT = 43;
    public static final int SUBSCRIBERS_ONLY_CHANGED_EVENT = 44;
    public static final int EVENT_CHANGED_EVENT = 45;
    public static final int UPLOADED_CHANGED_EVENT = 46;
    public static final int AUDIO_CHANGED_EVENT = 47;
    public static final int PAUSED_CHANGED_EVENT = 48;
    public static final int SAVED_CHANGED_EVENT = 50;
    public static final int RECORDED_CHANGED_EVENT = 52;
    public static final int GAME_LIST_ENTRY_EVENT_LIMIT = 57;
    public final GameType gameType;
    public final int roomId;
    private String name;
    private int numContainers;
    private short flags;
    private final User[] roleToUser = new User[Role.count()];
    private Role myRole;
    private FlatSet<UserAction> actions;

    protected CGameListEntry(Conn newConn, GameType newGameType, int newId, DataInputStream in) throws IOException {
        super(newConn, newId);
        this.gameType = newGameType;
        this.roomId = in.readInt();
        this.readFrom(in);
    }

    protected void readFrom(DataInputStream in) throws IOException {
        byte roleId;
        while ((roleId = in.readByte()) != -1) {
            if (roleId < 0 || roleId >= Role.count()) {
                throw new IOException("Invalid role ID: " + roleId);
            }
            this.roleToUser[roleId] = this.conn.getUser(in);
            if (this.roleToUser[roleId] != this.conn.getMe()) continue;
            this.myRole = Role.get(roleId);
        }
        for (int i = 0; i < this.roleToUser.length; ++i) {
            if (this.roleToUser[i] != null == this.gameType.isRole(Role.get(i))) continue;
            throw new RuntimeException(this.toString() + i);
        }
        this.setFlags(in.readShort());
        String oldName = this.name;
        String string = this.name = (this.flags & 0x100) != 0 ? in.readUTF() : null;
        if (oldName == null ? this.name != null : !oldName.equals(this.name)) {
            this.emit(39, this.name);
        }
    }

    protected void readUpdate(DataInputStream in) throws IOException {
        this.setFlags(in.readShort());
    }

    protected boolean readGameState(DataInputStream in) throws IOException {
        this.setFlags(in.readShort());
        FlatSet<UserAction> oldActions = this.actions;
        this.actions = new FlatSet();
        byte action;
        while ((action = in.readByte()) != -1) {
            this.actions.add(new UserAction(this.conn.getUser(in), GameAction.get(action)));
        }
        return !this.actions.equals(oldActions);
    }

    public String getName() {
        return this.name;
    }

    private void setFlags(short newFlags) {
        int oldFlags = this.flags;
        this.flags = newFlags;
        oldFlags ^= newFlags;
        for (int i = 0; i < 16; ++i) {
            if ((oldFlags & 1) != 0) {
                this.emit(41 + i);
            }
            oldFlags >>= 1;
        }
    }

    public void changeContainerCount(int delta) {
        this.numContainers += delta;
        if (this.numContainers < 0) {
            throw new IllegalStateException();
        }
        if (this.numContainers == 0 && !this.isJoined()) {
            this.conn.objects.remove(this.id);
        }
    }

    @Override
    public User getPlayer(Role role) {
        return this.roleToUser[role.id];
    }

    public Role getRole() {
        return this.myRole;
    }

    public Role getRole(String peer) {
        peer = User.canonName(peer);
        for (int i = 0; i < this.roleToUser.length; ++i) {
            if (this.roleToUser[i] == null || !this.roleToUser[i].canonName().equals(peer)) continue;
            return Role.get(i);
        }
        return null;
    }

    public Role getRole(User user) {
        for (int i = 0; i < this.roleToUser.length; ++i) {
            if (this.roleToUser[i] != user) continue;
            return Role.get(i);
        }
        return null;
    }

    @Override
    protected void handleMessage(MsgTypesDown msgType, DataInputStream in) throws IOException {
        switch (msgType) {
            case GAME_STATE: {
                if (this.isJoined()) {
                    if (!this.readGameState(in)) break;
                    this.emit(40);
                    break;
                }
                System.err.println("GAME_STATE from unjoined game: " + this);
                break;
            }
            case GAMELISTENTRY_PLAYER_REPLACED: {
                int roleId = in.read();
                this.roleToUser[roleId] = this.conn.getUser(in);
                if (this.myRole != null && this.myRole.id == roleId) {
                    this.myRole = null;
                }
                if (this.roleToUser[roleId] != this.conn.getMe()) break;
                this.myRole = Role.get(roleId);
                break;
            }
            case GAME_NAME_CHANGE: {
                String newName;
                String string = newName = in.available() == 0 ? null : in.readUTF();
                if (!(newName == null ? this.name != null : !newName.equals(this.name))) break;
                this.name = newName;
                this.emit(39, newName);
                break;
            }
            default: {
                super.handleMessage(msgType, in);
            }
        }
    }

    public boolean isOver() {
        return (this.flags & 1) != 0;
    }

    public boolean isAdjourned() {
        return (this.flags & 2) != 0;
    }

    @Override
    public boolean isPrivate() {
        return (this.flags & 4) != 0;
    }

    public boolean isSubscribersOnly() {
        return (this.flags & 8) != 0;
    }

    public boolean isEvent() {
        return (this.flags & 0x10) != 0;
    }

    public boolean isRecorded() {
        return (this.flags & 0x800) != 0;
    }

    public boolean isUploaded() {
        return (this.flags & 0x20) != 0;
    }

    public boolean isAudio() {
        return (this.flags & 0x40) != 0;
    }

    public boolean isSaved() {
        return (this.flags & 0x200) != 0;
    }

    public boolean isGlobal() {
        return (this.flags & 0x400) != 0;
    }

    public boolean isPaused() {
        return (this.flags & 0x80) != 0;
    }

    public void sendSetEvent(boolean eventFlag) {
        this.sendChangeFlagsRequest((short)16, eventFlag);
    }

    public void sendSetRecorded(boolean recordedFlag) {
        this.sendChangeFlagsRequest((short)2048, recordedFlag);
    }

    public void sendSetSubscribersOnly(boolean subscribersFlag) {
        this.sendChangeFlagsRequest((short)8, subscribersFlag);
    }

    public void sendSetAudio(boolean isAudio) {
        this.sendChangeFlagsRequest((short)64, isAudio);
    }

    protected final void sendChangeFlagsRequest(short mask, boolean set) {
        short dest;
        short s = dest = set ? mask : (short)0;
        if ((this.flags & mask) != dest) {
            TxMessage tx = this.buildMessage(MsgTypesUp.GAME_LIST_ENTRY_SET_FLAGS);
            tx.writeShort(mask);
            tx.writeShort(dest);
            this.conn.send(tx);
        }
    }

    @Override
    protected void unjoin() {
        this.actions = null;
        super.unjoin();
    }

    public final GameAction getAction() {
        return this.getAction(this.conn.getMe().name);
    }

    public GameAction getAction(String who) {
        if (this.actions != null) {
            for (UserAction ua : this.actions) {
                if (!ua.user.canonName().equals(User.canonName(who))) continue;
                return ua.action;
            }
        }
        return null;
    }

    public User getUser(GameAction action) {
        if (this.actions != null) {
            for (UserAction ua : this.actions) {
                if (ua.action != action) continue;
                return ua.user;
            }
        }
        return null;
    }

    public FlatSet<UserAction> getActions() {
        return this.actions;
    }

    @Override
    public boolean isOwner(User user) {
        return this.gameType.isOwner(this.getRole(user));
    }

    @Override
    public String getDescription(IBundle bundle) {
        String ownerName = this.getPlayer((Role)this.gameType.owner).name;
        return this.name == null || this.name.isEmpty() ? bundle.str(2031923642, ownerName) : bundle.str(2031923643, new Object[]{ownerName, this.name});
    }

    public static class UserAction {
        public final User user;
        public final GameAction action;

        public UserAction(User newUser, GameAction newAction) {
            this.user = newUser;
            this.action = newAction;
        }

        public boolean equals(Object o) {
            if (o == null || !(o instanceof UserAction)) {
                return false;
            }
            UserAction peer = (UserAction)o;
            return peer.user == this.user && peer.action == this.action;
        }

        public String toString() {
            return "UserAction[" + this.user + "=" + this.action + "]";
        }
    }
}
