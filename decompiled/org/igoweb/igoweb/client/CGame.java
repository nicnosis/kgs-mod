/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import org.igoweb.games.Clock;
import org.igoweb.igoweb.client.CGameListEntry;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.ChatMode;
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.igoweb.shared.MsgTypesUp;
import org.igoweb.igoweb.shared.Proposal;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.igoweb.shared.User;
import org.igoweb.util.Defs;

public abstract class CGame
extends CGameListEntry {
    private static final int EVENT_BASE = 60;
    public static final int CHAT_MODE_CHANGED_EVENT = 60;
    public static final int UNDO_REQUESTED_EVENT = 61;
    public static final int REVIEW_STARTING_EVENT = 62;
    public static final int PROPOSAL_EVENT = 63;
    public static final int NOT_MEMBER_EVENT = 64;
    public static final int GAME_OVER_EVENT = 65;
    public static final int UNKNOWN_USER_EVENT = 66;
    public static final int PREP_PAUSE_OVER_EVENT = 67;
    public static final int PREP_PAUSE_ON_EVENT = 68;
    public static final int GAME_EVENT_LIMIT = 69;
    private int numObservers;
    private ChatMode chatMode = ChatMode.NORMAL;
    private Clock[] clocks;
    private CGame original;
    private GameSummary<User> gameSummary;
    private int prepPauseLength = -1;

    protected CGame(Conn newConn, GameType newGameType, int newId, DataInputStream in) throws IOException {
        super(newConn, newGameType, newId, in);
    }

    @Override
    protected void handleMessage(MsgTypesDown msgType, DataInputStream in) throws IOException {
        switch (msgType) {
            case SET_CHAT_MODE: {
                this.emit(60, (Object)ChatMode.values()[in.readByte()]);
                break;
            }
            case GAME_UPDATE: {
                this.readGameUpdate(in);
                break;
            }
            case GAME_UNDO_REQUEST: {
                this.emit(61, Role.get(in));
                break;
            }
            case GAME_TIME_EXPIRED: {
                this.conn.send(this.buildMessage(MsgTypesUp.GAME_TIME_EXPIRED));
                break;
            }
            case GAME_ALL_PLAYERS_GONE: {
                this.emit(14, Defs.getString(2031923646));
                break;
            }
            case GAME_SET_ROLES_UNKNOWN_USER: {
                this.emit(66, in.readUTF());
                break;
            }
            case GAME_SET_ROLES_NOT_MEMBER: {
                this.emit(64, in.readUTF());
                break;
            }
            case GAME_PREP_STATUS: {
                this.prepPauseLength = in.readByte();
                this.emit(this.prepPauseLength == 0 ? 67 : 68, this.prepPauseLength);
                break;
            }
            default: {
                super.handleMessage(msgType, in);
            }
        }
    }

    public int getAndClearPrepPauseState() {
        int result = this.prepPauseLength;
        this.prepPauseLength = -1;
        return result;
    }

    public int getNumObservers() {
        if (this.isJoined()) {
            int count = this.getMembers().size();
            for (int i = 0; i < Role.count(); ++i) {
                User u;
                Role role = Role.get(i);
                if (!this.gameType.isMainRole(role) || (u = this.getPlayer(Role.get(i))) == null || !this.getMembers().containsKey(u.name)) continue;
                --count;
            }
            return count;
        }
        return this.numObservers;
    }

    public void sendSaveRequest() {
        this.sendChangeFlagsRequest((short)512, true);
    }

    public void sendAudio(short audioType, byte[] data, int offset, int len) {
        TxMessage msg = this.buildMessage(MsgTypesUp.GAME_AUDIO);
        msg.writeShort(audioType);
        msg.write(data, offset, len);
        this.conn.send(msg);
    }

    public ChatMode getChatMode() {
        return this.chatMode;
    }

    public boolean hasChatAccess() {
        return this.chatMode != ChatMode.MODERATED || this.gameType.isMainRole(this.getRole());
    }

    public TxMessage buildSgfMessage() {
        return null;
    }

    public void sendSgfMessage(TxMessage tx) {
        this.conn.send(tx);
    }

    @Override
    protected void readFrom(DataInputStream in) throws IOException {
        super.readFrom(in);
        this.numObservers = in.readShort();
    }

    @Override
    protected void readUpdate(DataInputStream in) throws IOException {
        super.readUpdate(in);
        this.numObservers = in.readShort();
    }

    @Override
    protected void join(DataInputStream in) throws IOException {
        this.gameSummary = this.loadGameSummary(in);
        this.clocks = new Clock[Role.count()];
        for (int i = 0; i < Role.count(); ++i) {
            Role role = Role.get(i);
            this.clocks[i] = this.gameType.isRole(role) ? this.buildClock(role) : null;
        }
        this.readGameState(in);
        super.join(in);
        this.original = null;
        this.emit(40);
    }

    public Clock getClock(Role role) {
        return this.clocks[role.id];
    }

    @Override
    protected void unjoin() {
        this.clocks = null;
        super.unjoin();
    }

    protected abstract Clock buildClock(Role var1);

    @Override
    protected boolean readGameState(DataInputStream in) throws IOException {
        boolean change = super.readGameState(in);
        for (int i = 0; i < this.clocks.length; ++i) {
            if (!this.gameType.isTimed(Role.get(i))) continue;
            this.clocks[i].readState(in);
        }
        return change;
    }

    protected abstract void readGameUpdate(DataInputStream var1) throws IOException;

    public void sendGrantUndo() {
        this.conn.send(this.buildMessage(MsgTypesUp.GAME_UNDO_ACCEPT));
    }

    public void sendResign() {
        this.conn.send(this.buildMessage(MsgTypesUp.GAME_RESIGN));
    }

    public void sendUndoRequest() {
        this.conn.send(this.buildMessage(MsgTypesUp.GAME_UNDO_REQUEST));
    }

    public final void sendSetChatMode(ChatMode newChatMode) {
        this.sendSetChatMode(newChatMode, this.conn.getMe().name);
    }

    public void sendSetChatMode(ChatMode newChatMode, String moderator) {
        TxMessage msg = this.buildMessage(MsgTypesUp.SET_CHAT_MODE);
        msg.write(newChatMode.ordinal());
        if (newChatMode == ChatMode.MODERATED) {
            msg.writeUTF(User.canonName(moderator));
        }
        this.conn.send(msg);
    }

    public void sendAddTime(Role role, int ms) {
        TxMessage msg = this.buildMessage(MsgTypesUp.GAME_ADD_TIME);
        msg.write(role.id);
        msg.writeInt(ms);
        this.conn.send(msg);
    }

    public void sendReviewRequest() {
        this.conn.send(this.buildMessage(MsgTypesUp.GAME_START_REVIEW));
    }

    protected void setOriginal(CGame game) {
        if (game.isJoined()) {
            this.original = game;
            this.importChats(game);
            this.original.emit(62, this);
        }
    }

    public CGame getOriginalGame() {
        return this.original != null && this.original.isJoined() ? this.original : null;
    }

    public final void sendSetRoles() {
        this.sendSetRoles(new String[0], new Role[0]);
    }

    public final void sendSetRoles(String name, Role role) {
        this.sendSetRoles(new String[]{name}, new Role[]{role});
    }

    public void sendSetRoles(String[] names, Role[] roles) {
        TxMessage tx = this.buildMessage(MsgTypesUp.GAME_SET_ROLES);
        for (int i = 0; i < names.length; ++i) {
            tx.writeUTF(names[i]);
            tx.write(roles[i].id);
        }
        this.conn.send(tx);
    }

    public GameSummary<User> getGameSummary() {
        return this.gameSummary;
    }

    protected abstract GameSummary<User> loadGameSummary(DataInputStream var1) throws IOException;

    void emitProposal(Proposal<?, ?, ?> arg) {
        this.emit(63, arg);
    }

    public void sendAllowChat(String user, boolean allow) {
        TxMessage tx = this.buildMessage(MsgTypesUp.GAME_SET_ALLOW_CHAT);
        tx.writeBoolean(allow);
        tx.writeUTF(User.canonName(user));
        this.conn.send(tx);
    }

    public abstract int getMoveNum();

    @Override
    public HashMap<String, User> getAccessList() {
        HashMap<String, User> result = new HashMap<String, User>(super.getAccessList());
        for (int i = 0; i < Role.count(); ++i) {
            Role role = Role.get(i);
            if (!this.gameType.isMainRole(role)) continue;
            User user = this.gameSummary.getPlayer(role);
            result.put(user.name, user);
        }
        return result;
    }

    @Override
    public String getDescription(IBundle bundle) {
        return this.gameSummary == null ? super.getDescription(bundle) : this.gameSummary.getLocalDesc(bundle);
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[me=" + (this.conn == null ? "<null>" : this.conn.getMe().name) + "," + (this.gameSummary == null ? "<null>" : this.gameSummary) + "]";
    }
}
