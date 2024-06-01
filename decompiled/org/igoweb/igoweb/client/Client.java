/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import org.igoweb.igoweb.Config;
import org.igoweb.igoweb.client.CArchive;
import org.igoweb.igoweb.client.CChallenge;
import org.igoweb.igoweb.client.CChannel;
import org.igoweb.igoweb.client.CConvo;
import org.igoweb.igoweb.client.CDetailsChannel;
import org.igoweb.igoweb.client.CGame;
import org.igoweb.igoweb.client.CGameContainer;
import org.igoweb.igoweb.client.CGameListEntry;
import org.igoweb.igoweb.client.CGlobalGames;
import org.igoweb.igoweb.client.CPlayback;
import org.igoweb.igoweb.client.CRoom;
import org.igoweb.igoweb.client.CRoomCatChannel;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.client.ConnMutex;
import org.igoweb.igoweb.client.Connector;
import org.igoweb.igoweb.client.FriendsGroup;
import org.igoweb.igoweb.client.Message;
import org.igoweb.igoweb.client.PlaybackInfo;
import org.igoweb.igoweb.client.ServerStats;
import org.igoweb.igoweb.shared.ClientType;
import org.igoweb.igoweb.shared.GameContainers;
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.igoweb.shared.MsgTypeUtil;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.igoweb.shared.MsgTypesUp;
import org.igoweb.igoweb.shared.RoomCategories;
import org.igoweb.igoweb.shared.Subscription;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.igoweb.shared.User;
import org.igoweb.resource.Plural;
import org.igoweb.util.Defs;
import org.igoweb.util.Emitter;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;
import org.igoweb.util.LongHashMap;
import org.igoweb.util.Multicaster;
import org.igoweb.util.RsaCrypto;

public abstract class Client
extends Emitter
implements CGameContainer.GameReader {
    private static final int EVENT_BASE = 86;
    public static final int LOGIN_SUCCESS_EVENT = 86;
    public static final int DISCONNECT_EVENT = 87;
    public static final int NEW_USER_DETAILS_EVENT = 88;
    public static final int NO_SUCH_ARCHIVE_EVENT = 89;
    public static final int GOT_ROOM_EVENT = 90;
    public static final int ANNOUNCE_EVENT = 91;
    public static final int ALL_ROOMS_IN_EVENT = 92;
    public static final int KEEPER_ERROR_MSG_EVENT = 93;
    public static final int NEW_CONVO_EVENT = 94;
    public static final int LONG_IDLE_WARNING_EVENT = 95;
    public static final int GAME_CREATED_EVENT = 96;
    public static final int CANT_PLAY_TWICE_EVENT = 97;
    public static final int CANT_CREATE_GAME_EVENT = 98;
    public static final int AVATAR_DATA_EVENT = 99;
    public static final int MESSAGES_WAITING_EVENT = 100;
    public static final int LEAVE_MESSAGE_SUCCESS_EVENT = 101;
    public static final int LEAVE_MESSAGE_NO_USER_EVENT = 102;
    public static final int LEAVE_MESSAGE_DEST_LOGGED_IN_EVENT = 103;
    public static final int GAME_LOADED_EVENT = 104;
    public static final int INTERNAL_ERROR_EVENT = 105;
    public static final int NEW_CLIENT_ID_EVENT = 106;
    public static final int TOURN_NOTIFY_EVENT = 109;
    public static final int MINOR_OUT_OF_DATE_EVENT = 110;
    public static final int MAJOR_OUT_OF_DATE_EVENT = 111;
    public static final int PLAYBACK_LIST_CHANGED_EVENT = 112;
    public static final int SUBSCRIPTION_CHANGED_EVENT = 113;
    public static final int USER_GROUP_CHANGED = 114;
    public static final int ROOM_CREATE_CALLBACK_EVENT = 115;
    public static final int CONVO_NO_SUCH_USER_EVENT = 116;
    public static final int LEAVE_MESSAGE_FULL_MAILBOX_EVENT = 117;
    public static final int CHALLENGE_CREATED_EVENT = 118;
    public static final int CHALLENGE_NOT_CREATED_EVENT = 119;
    public static final int PLAYBACK_CREATED_EVENT = 120;
    public static final int GLOBAL_GAME_LIST_JOINED_EVENT = 121;
    public static final int SERVER_STATS_EVENT = 122;
    public static final int REGISTER_SUCCESS_EVENT = 123;
    public static final int REGISTER_BAD_EMAIL_EVENT = 124;
    public static final int PRIVATE_KEEP_OUT_EVENT = 127;
    public static final int FRIEND_CHANGE_FAILED_EVENT = 128;
    public static final int AUTOMATCH_STATUS_CHANGED_EVENT = 129;
    public static final int AUTOMATCH_PREFS_EVENT = 130;
    public static final int FETCH_TAGS_NO_SUCH_USER_EVENT = 131;
    public static final int FETCH_TAGS_SUCCESS_EVENT = 132;
    public static final int REQUEST_USER_FAILED_EVENT = 133;
    public static final int SUBSCRIBERS_ONLY_EVENT = 134;
    public static final int EVENT_LIMIT = 135;
    public static final int MAX_KEEP_OUT_SECS = 172800;
    public static final int KEEP_OUT_REASON_MAX_LENGTH = 500;
    public static final int MAX_TAG_LENGTH = 50;
    public final HashMap<String, String> loginProps = new HashMap();
    private short serverBugfixVersion;
    private String userName;
    private String password;
    private final TreeMap<Long, PlaybackInfo> playbackList = new TreeMap();
    private final HashSet<Long> playbacksRequested = new HashSet();
    public final HashMap<Object, Object> objects = new HashMap();
    private boolean closeExpected = false;
    protected final Conn conn;
    private final ClientType clientType;
    private long clientId;
    private EnumMap<RoomCategories, CRoomCatChannel> roomCats;
    private short callbackKey;
    private static final String ROOM_CREATE_CALLBACK_PREFIX = "c1:";
    private static final String LEAVE_MESSAGE_PREFIX = "l:";
    private static final String FRIEND_CHANGE_PREFIX = "a:";
    private Subscription[] subscriptions;
    private EventListener ssListener;
    private long ssSendTime;
    protected int automatchPrefs = IBundle.get().getDefaultAutomatchPrefs();
    protected boolean automatchRunning = false;
    private LongHashMap<String> myTags;

    public Client(String newUserName, String newPassword, ClientType newClientType, long newClientId, ConnMutex connMutex, Connector connector) {
        this.userName = newUserName;
        this.password = newPassword;
        this.clientId = newClientId;
        this.clientType = newClientType;
        this.conn = this.buildConn(connector, this::connEventIn, connMutex);
    }

    protected Conn buildConn(Connector connector, EventListener listener, ConnMutex mutex) {
        return new Conn(connector, listener, mutex, null);
    }

    public void go() {
        this.conn.go();
    }

    private void connEventIn(Event event) {
        try {
            switch (event.type) {
                case 8: {
                    DataInputStream in = (DataInputStream)event.arg;
                    MsgTypesDown msgType = MsgTypeUtil.getDownFromId(in.readShort());
                    if (msgType.isStatic()) {
                        this.staticMsg(msgType, in);
                        break;
                    }
                    int chanId = in.readInt();
                    CChannel chan = (CChannel)this.conn.objects.get(chanId);
                    if (chan == null) {
                        System.err.println((Object)((Object)msgType) + ": channel " + chanId + " not found");
                        break;
                    }
                    chan.handleMessage(msgType, in);
                    break;
                }
                case 9: {
                    this.emit(87, this.closeExpected ? null : event.arg);
                    break;
                }
                case 10: {
                    this.emit(105, event.arg);
                    break;
                }
                default: {
                    throw new RuntimeException();
                }
            }
        }
        catch (IOException excep) {
            throw new RuntimeException(excep);
        }
    }

    private void sendLogin(long passwordXor) {
        TxMessage msg = new TxMessage(MsgTypesUp.LOGIN);
        msg.writeShort(this.clientType.ordinal());
        msg.writeShort(Short.parseShort(Config.get("version.bugfix")));
        String beta = Config.get("version.beta");
        msg.writeShort(beta == null ? -1 : (int)Short.parseShort(beta));
        msg.writeUTF(Defs.getString(2031923656));
        msg.writeLong(this.clientId);
        msg.writeUTF(this.userName);
        if (this.password == null) {
            msg.writeBoolean(false);
        } else {
            msg.writeBoolean(true);
            msg.write(Client.rsaEncrypt(User.passwordCompute(this.password) ^ passwordXor), 0, 256);
        }
        StringTokenizer toks = new StringTokenizer("java.version java.vendor os.name os.arch os.version");
        while (toks.hasMoreElements()) {
            String prop = null;
            try {
                String tok = toks.nextToken();
                prop = this.loginProps.containsKey(tok) ? this.loginProps.get(tok) : System.getProperty(tok);
            }
            catch (AccessControlException accessControlException) {
                // empty catch block
            }
            msg.writeUTF(prop == null ? "" : prop);
        }
        this.conn.send(msg);
    }

    public static byte[] rsaEncrypt(long value) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteOut);
        try {
            dataOut.writeLong(value);
            return RsaCrypto.encrypt(new BigInteger(Config.get("public"), 16), new BigInteger(Config.get("modulus"), 16), byteOut.toByteArray(), 256);
        }
        catch (IOException excep) {
            throw new RuntimeException(excep);
        }
    }

    private void staticMsg(MsgTypesDown msgType, DataInputStream msg) throws IOException {
        switch (msgType) {
            case HELLO: {
                short major = msg.readShort();
                short minor = msg.readShort();
                this.serverBugfixVersion = msg.readShort();
                if (major != Config.getInt("version.major") || minor != Config.getInt("version.minor")) {
                    this.emit(111);
                    this.loginFailed(null);
                    break;
                }
                this.sendLogin(msg.readLong());
                break;
            }
            case USER_UPDATE: {
                this.conn.getUser(msg);
                break;
            }
            case LOGIN_FAILED_NO_SUCH_USER: {
                this.loginFailed(2031923660);
                break;
            }
            case LOGIN_FAILED_BAD_PASSWORD: {
                this.loginFailed(2031923659);
                break;
            }
            case LOGIN_SUCCESS: {
                this.handleLoginSuccess(msg);
                if (this.serverBugfixVersion <= Config.getInt("version.bugfix")) break;
                this.emit(110);
                break;
            }
            case LOGIN_FAILED_USER_ALREADY_EXISTS: {
                this.loginFailed(2031923661);
                break;
            }
            case LOGIN_FAILED_KEEP_OUT: {
                this.loginFailed(Defs.getString(2031923655, msg.readUTF()));
                break;
            }
            case RECONNECT: {
                this.loginFailed(2031923662);
                break;
            }
            case DETAILS_JOIN: {
                CDetailsChannel details = new CDetailsChannel(this.conn, msg);
                this.conn.objects.put(details.id, details);
                this.emit(88, details);
                this.conn.objects.remove("F3a:" + details.owner.canonName());
                break;
            }
            case DETAILS_NONEXISTANT: {
                String requestName = msg.readUTF();
                this.emit(133, requestName);
                this.emit(14, Defs.getString(2031923657, this.conn.objects.remove("F3a:" + requestName)));
                break;
            }
            case ROOM_NAMES: {
                while (msg.available() > 0) {
                    Object rawRoom = this.conn.objects.get(msg.readInt());
                    if (rawRoom != null && rawRoom instanceof CRoom) {
                        ((CRoom)rawRoom).updateName(msg);
                        continue;
                    }
                    msg.readUTF();
                    msg.readShort();
                }
                break;
            }
            case ROOM_CREATED: 
            case ROOM_CREATE_TOO_MANY_ROOMS: 
            case ROOM_CREATE_NAME_TAKEN: {
                ((EventListener)this.conn.objects.remove(ROOM_CREATE_CALLBACK_PREFIX + msg.readShort())).handleEvent(new Event(this, 115, msgType == MsgTypesDown.ROOM_CREATED ? null : Boolean.valueOf(msgType == MsgTypesDown.ROOM_CREATE_NAME_TAKEN)));
                break;
            }
            case ROOM_CHANNEL_INFO: {
                this.readRoomChanInfo(msg);
                break;
            }
            case CONVO_JOIN: 
            case CONVO_NO_SUCH_USER: {
                int chanId = msgType == MsgTypesDown.CONVO_JOIN ? msg.readInt() : 0;
                short key = msg.readShort();
                String origName = (String)this.conn.objects.remove("F1b:" + key);
                Event event = msgType == MsgTypesDown.CONVO_JOIN ? this.buildEvent(94, new CConvo(this.conn, chanId, msg, key != 0)) : this.buildEvent(116, origName);
                EventListener convoListener = (EventListener)this.conn.objects.remove("F1a:" + key);
                this.emit(event);
                if (convoListener == null) break;
                convoListener.handleEvent(event);
                break;
            }
            case MESSAGES: {
                ArrayList<Message> userMessages = new ArrayList<Message>();
                while (msg.available() > 0) {
                    long sendDate = msg.readLong();
                    String name = msg.readUTF();
                    User sender = new User(name, msg.readInt());
                    userMessages.add(new Message(sendDate, sender, msg.readUTF()));
                }
                this.emit(100, userMessages);
                break;
            }
            case MESSAGE_CREATE_SUCCESS: {
                this.leaveMessageResult(101, msg);
                break;
            }
            case MESSAGE_CREATE_NO_USER: {
                this.leaveMessageResult(102, msg);
                break;
            }
            case MESSAGE_CREATE_FULL: {
                this.leaveMessageResult(117, msg);
                break;
            }
            case MESSAGE_CREATE_CONNECTED: {
                this.leaveMessageResult(103, msg);
                break;
            }
            case FRIEND_CHANGE_NO_USER: {
                String name = (String)this.conn.objects.remove(FRIEND_CHANGE_PREFIX + msg.readShort());
                this.emit(128, name);
                break;
            }
            case FRIEND_ADD_SUCCESS: 
            case FRIEND_REMOVE_SUCCESS: {
                this.conn.objects.remove(FRIEND_CHANGE_PREFIX + msg.readShort());
                FriendsGroup group = this.getFriendsGroup(msg.readByte());
                if (msgType == MsgTypesDown.FRIEND_ADD_SUCCESS) {
                    group.add(this.conn.getUser(msg), msg.readUTF());
                    break;
                }
                group.remove(this.conn.getUser(msg));
                break;
            }
            case CHALLENGE_CREATED: {
                short callbackKeyRcv = msg.readShort();
                CChallenge chal = (CChallenge)this.readGame(msg);
                EventListener listener = (EventListener)this.conn.objects.remove("g:" + callbackKeyRcv);
                listener.handleEvent(new Event(this, 118, chal));
                break;
            }
            case CHALLENGE_NOT_CREATED: {
                ((EventListener)this.conn.objects.remove("g:" + msg.readShort())).handleEvent(new Event(this, 119, msg.available() > 0 ? Long.valueOf(msg.readLong()) : null));
                break;
            }
            case ANNOUNCEMENT: {
                this.emit(91, msg.readUTF());
                break;
            }
            case SERVER_STATS: {
                if (this.ssListener == null) break;
                this.ssListener.handleEvent(new Event(this, 122, new ServerStats(msg, System.currentTimeMillis() - this.ssSendTime)));
                this.ssListener = null;
                break;
            }
            case DELETE_ACCOUNT_ALREADY_GONE: {
                this.emit(12, Defs.getString(2031923641, msg.readUTF()));
                break;
            }
            case KEEP_OUT_SUCCESS: {
                this.emit(12, Defs.getString(2031923686, msg.readUTF()));
                break;
            }
            case KEEP_OUT_LOGIN_NOT_FOUND: {
                this.emit(14, Defs.getString(2031923682, msg.readUTF()));
                break;
            }
            case DELETE_ACCOUNT_SUCCESS: {
                this.emit(12, Defs.getString(2031923669, msg.readUTF()));
                break;
            }
            case IDLE_WARNING: {
                this.emit(95);
                break;
            }
            case CANT_PLAY_TWICE: {
                EventListener listener = (EventListener)this.conn.objects.remove("g:" + msg.readShort());
                if (listener == null) {
                    this.emit(97);
                    break;
                }
                listener.handleEvent(new Event(this, 97));
                break;
            }
            case LOAD_FAILED: {
                this.emit(14, Defs.getString(2031923645));
                break;
            }
            case GAME_STARTED: {
                this.emit(104, this.loadGameSummary(msg));
                break;
            }
            case ARCHIVE_JOIN: 
            case TAG_ARCHIVE_JOIN: {
                CArchive arc = this.loadArchive(msg, msgType == MsgTypesDown.TAG_ARCHIVE_JOIN);
                this.conn.objects.put(arc.id, arc);
                this.conn.objects.put(arc.getClassPrefix() + arc.owner.canonName(), arc);
                break;
            }
            case ARCHIVE_NONEXISTANT: 
            case TAG_ARCHIVE_NONEXISTANT: {
                EventListener callback = (EventListener)this.conn.objects.remove((msgType == MsgTypesDown.ARCHIVE_NONEXISTANT ? "F2a:" : "E-a:") + msg.readUTF());
                if (callback == null) break;
                callback.handleEvent(new Event(this, 89));
                break;
            }
            case AVATAR: {
                String name = User.canonName(msg.readUTF());
                EventListener listener = (EventListener)this.conn.objects.remove("pic:" + name);
                byte[] data = null;
                if (msg.available() > 0) {
                    data = new byte[msg.available()];
                    msg.readFully(data);
                }
                listener.handleEvent(new Event(this, 99, data));
                break;
            }
            case CLEAR_KEEP_OUT_SUCCESS: {
                this.emit(12, Defs.getString(2031923671, msg.readUTF()));
                break;
            }
            case CLEAR_KEEP_OUT_FAILURE: {
                this.emit(14, Defs.getString(2031923672));
                break;
            }
            case TOO_MANY_KEEP_OUTS: {
                this.emit(14, Defs.getString(2031923664));
                break;
            }
            case SYNC: {
                this.conn.performSyncCallback(msg.readShort());
                break;
            }
            case GAME_NOTIFY: {
                this.readGame(msg);
                break;
            }
            case GAME_REVIEW: {
                CGame original = (CGame)this.conn.objects.get(msg.readInt());
                CGame review = (CGame)this.readGame(msg);
                if (original == null) {
                    System.err.println("Can't find parent, but told of review!");
                    break;
                }
                review.setOriginal(original);
                break;
            }
            case PLAYBACK_ADD: {
                while (msg.available() > 0) {
                    long pbId = msg.readLong();
                    boolean subsOnly = msg.readBoolean();
                    this.playbackList.put(pbId, new PlaybackInfo(pbId, subsOnly, this.loadGameSummary(msg)));
                }
                this.emit(112, this.playbackList);
                break;
            }
            case PLAYBACK_DELETE: {
                while (msg.available() > 0) {
                    this.playbackList.remove(msg.readLong());
                }
                this.emit(112, this.playbackList);
                break;
            }
            case ALREADY_IN_PLAYBACK: {
                this.emit(13, Defs.getString(2031923638));
                break;
            }
            case PLAYBACK_ERROR: {
                this.emit(13, Defs.getString(2031923634));
                break;
            }
            case PLAYBACK_SETUP: {
                int chanId = msg.readInt();
                long len = msg.readLong();
                this.emit(120, this.buildPlayback(chanId, len, this.loadGameSummary(msg)));
                break;
            }
            case GLOBAL_GAMES_JOIN: {
                this.emit(121, new CGlobalGames(this.conn, msg.readInt(), this, msg));
                break;
            }
            case SUBSCRIPTION_UPDATE: {
                this.subscriptions = Subscription.read(msg);
                this.emit(113);
                break;
            }
            case REGISTER_SUCCESS: {
                this.emit(123);
                break;
            }
            case REGISTER_BAD_EMAIL: {
                this.emit(124, msg.readUTF());
                break;
            }
            case PRIVATE_KEEP_OUT: 
            case CHANNEL_SUBSCRIBERS_ONLY: {
                this.emit(msgType == MsgTypesDown.PRIVATE_KEEP_OUT ? 127 : 134, this.conn.objects.get(msg.readInt()));
                break;
            }
            case SUBSCRIPTION_LOW: {
                int days = msg.readByte() + 1;
                this.emit(13, Defs.getString(2031923635, new Object[]{days, Config.get("webHost"), Plural.getCategory(days)}));
                break;
            }
            case AD: {
                Object[] args = new Object[]{msg.readByte(), msg};
                this.emit(26, args);
                break;
            }
            case TOURN_NOTIFY: {
                int gameId = msg.readInt();
                int roomId = msg.readInt();
                new TournNotifyHandler((CRoom)this.conn.objects.get(roomId), (CGameListEntry)this.conn.objects.get(gameId), msg.readBoolean());
                break;
            }
            case AUTOMATCH_STATUS: {
                this.automatchRunning = msg.readBoolean();
                this.emit(129);
                break;
            }
            case AUTOMATCH_PREFS: {
                int newPrefs = msg.readInt();
                if (newPrefs == this.automatchPrefs) break;
                this.automatchPrefs = newPrefs;
                this.emit(130);
                break;
            }
            case FETCH_TAGS_RESULT: {
                this.myTags = new LongHashMap();
                while (msg.available() > 0) {
                    long gameId = msg.readLong();
                    this.myTags.put(gameId, msg.readUTF());
                }
                this.emit(132);
                break;
            }
        }
    }

    private void leaveMessageResult(int eventCode, DataInputStream msg) throws IOException {
        EventListener callback = (EventListener)this.conn.objects.remove(LEAVE_MESSAGE_PREFIX + msg.readShort());
        if (callback != null) {
            callback.handleEvent(new Event(this, eventCode));
        }
    }

    public User getMe() {
        return this.conn.getMe();
    }

    public void logout() {
        this.closeExpected = true;
        this.conn.close();
    }

    public void requestUserDetails(String peer) {
        String name = User.canonName(peer);
        String key = "F3a:" + name;
        if (this.conn.objects.containsKey(key)) {
            return;
        }
        CDetailsChannel details = (CDetailsChannel)this.conn.objects.get("F3:" + name);
        if (details == null) {
            this.conn.objects.put(key, peer);
            TxMessage msg = new TxMessage(MsgTypesUp.DETAILS_JOIN_REQUEST);
            msg.writeUTF(name);
            this.conn.send(msg);
        } else {
            details.reopen();
        }
    }

    public void requestArchive(String peer, EventListener listener) {
        CArchive.addListener(this.conn, peer, listener, false);
    }

    public void requestTagArchive(String peer, EventListener listener) {
        CArchive.addListener(this.conn, peer, listener, true);
    }

    public void sendKeepOut(String victim, int timeout, String reason) {
        TxMessage msg = new TxMessage(MsgTypesUp.KEEP_OUT_REQUEST);
        msg.writeUTF(User.canonName(victim));
        msg.writeInt(timeout);
        msg.writeUTF(reason == null ? "" : reason);
        this.conn.send(msg);
    }

    public void sendDeleteAccountRequest(String victim) {
        TxMessage msg = new TxMessage(MsgTypesUp.DELETE_ACCOUNT);
        msg.writeUTF(User.canonName(victim));
        this.conn.send(msg);
    }

    public void sendShutdownRequest(int timeout) {
        TxMessage msg = new TxMessage(MsgTypesUp.SHUTDOWN);
        msg.writeInt(timeout);
        this.conn.send(msg);
    }

    public void sendCreateRoomRequest(String name, String desc, int category, boolean isTournOnly, boolean isPrivate, boolean isGlobalGamesOnly, EventListener callback) {
        TxMessage tx = new TxMessage(MsgTypesUp.CREATE_ROOM_REQUEST);
        if (this.callbackKey == -1) {
            this.callbackKey = (short)(this.callbackKey + 1);
        }
        this.callbackKey = (short)(this.callbackKey + 1);
        tx.writeShort(this.callbackKey);
        this.conn.objects.put(ROOM_CREATE_CALLBACK_PREFIX + this.callbackKey, callback);
        tx.writeUTF(name);
        tx.writeUTF(desc);
        tx.write(category);
        int flags = 0;
        if (isTournOnly) {
            flags = (short)(flags | 2);
        }
        if (isPrivate) {
            flags = (short)(flags | 1);
        }
        if (isGlobalGamesOnly) {
            flags = (short)(flags | 4);
        }
        tx.writeShort(flags);
        this.conn.send(tx);
    }

    public void sendAnnouncement(String announcement) {
        TxMessage msg = new TxMessage(MsgTypesUp.ANNOUNCEMENT);
        msg.writeUTF(announcement);
        this.conn.send(msg);
    }

    public void createConvo(String peer, EventListener callback) {
        if (User.canonName(peer).equals(this.getMe().canonName())) {
            return;
        }
        CConvo result = this.getConvo(peer);
        if (result != null) {
            return;
        }
        TxMessage tx = new TxMessage(MsgTypesUp.CONVO_REQUEST);
        this.callbackKey = (short)(this.callbackKey + 1);
        if (this.callbackKey == 0) {
            this.callbackKey = 1;
        }
        tx.writeShort(this.callbackKey);
        tx.writeUTF(User.canonName(peer));
        this.conn.send(tx);
        if (callback != null) {
            String cbkey = "F1a:" + this.callbackKey;
            this.conn.objects.put(cbkey, Multicaster.add((EventListener)this.conn.objects.get(cbkey), callback));
        }
        this.conn.objects.put("F1b:" + this.callbackKey, peer);
    }

    public CConvo getConvo(String peer) {
        return CConvo.get(this.conn, peer);
    }

    public FriendsGroup getFriendsGroup(int id) {
        return this.conn.friendsGroups[id];
    }

    public void sendSync(Runnable runnable) {
        this.conn.sendSync(runnable);
    }

    public void addFriend(int type, String friendName, String notes) {
        TxMessage tx = new TxMessage(MsgTypesUp.FRIEND_ADD);
        this.callbackKey = (short)(this.callbackKey + 1);
        if (this.callbackKey == 0) {
            this.callbackKey = 1;
        }
        tx.writeShort(this.callbackKey);
        this.conn.objects.put(FRIEND_CHANGE_PREFIX + this.callbackKey, friendName);
        tx.write(type);
        tx.writeUTF(User.canonName(friendName));
        tx.writeUTF(notes);
        this.conn.send(tx);
    }

    public void removeFriend(int type, String friendName) {
        TxMessage tx = new TxMessage(MsgTypesUp.FRIEND_REMOVE);
        this.callbackKey = (short)(this.callbackKey + 1);
        if (this.callbackKey == 0) {
            this.callbackKey = 1;
        }
        tx.writeShort(this.callbackKey);
        tx.write(type);
        tx.writeUTF(User.canonName(friendName));
        this.conn.send(tx);
    }

    public void requestServerStats(EventListener callback) {
        if (this.ssListener == null) {
            this.conn.send(new TxMessage(MsgTypesUp.REQUEST_SERVER_STATS));
            this.ssSendTime = System.currentTimeMillis();
        }
        this.ssListener = Multicaster.add(this.ssListener, callback);
    }

    public void sendJoinRequest(long gameId) {
        TxMessage msg = new TxMessage(MsgTypesUp.JOIN_GAME_BY_ID);
        msg.writeLong(gameId);
        this.conn.send(msg);
    }

    public Collection<Object> getSubscribedRooms() {
        return this.conn.subscribedRooms;
    }

    public void sendKeepAlive() {
        this.conn.send(new TxMessage(MsgTypesUp.WAKE_UP));
    }

    public void uploadAvatar(byte[] data, int dataLen) {
        TxMessage msg = new TxMessage(MsgTypesUp.UPLOAD_AVATAR);
        msg.write(data, 0, dataLen);
        this.conn.send(msg);
    }

    public void requestAvatarData(String name, EventListener listener) {
        name = User.canonName(name);
        String key = "pic:" + name;
        EventListener oldListener = (EventListener)this.conn.objects.get(key);
        if (oldListener == null) {
            TxMessage msg = new TxMessage(MsgTypesUp.AVATAR_REQUEST);
            msg.writeUTF(name);
            this.conn.send(msg);
        } else {
            listener = Multicaster.add(oldListener, listener);
        }
        this.conn.objects.put(key, listener);
    }

    private void loginFailed(int msgId) {
        this.loginFailed(Defs.getString(msgId));
    }

    private void loginFailed(String message) {
        if (message != null) {
            this.emit(93, message);
        }
        System.err.println("Client.loginFailed: closeExpected = true");
        this.closeExpected = true;
        this.conn.close();
    }

    public void leaveMessage(String dest, String text, EventListener callback) {
        this.callbackKey = (short)(this.callbackKey + 1);
        this.conn.objects.put(LEAVE_MESSAGE_PREFIX + this.callbackKey, callback);
        TxMessage tx = new TxMessage(MsgTypesUp.MESSAGE_CREATE);
        tx.writeShort(this.callbackKey);
        tx.writeUTF(User.canonName(dest));
        tx.writeUTF(text);
        this.conn.send(tx);
    }

    public void deleteMessage(Message message) {
        TxMessage tx = new TxMessage(MsgTypesUp.MESSAGE_DELETE);
        tx.writeLong(message.sendDate);
        tx.writeUTF(User.canonName(message.sender.name));
        this.conn.send(tx);
    }

    public void deleteMessages() {
        this.conn.send(new TxMessage(MsgTypesUp.MESSAGE_DELETE));
    }

    public void sendClearKeepOut(String name) {
        TxMessage msg = new TxMessage(MsgTypesUp.CLEAR_KEEP_OUT);
        msg.writeUTF(User.canonName(name));
        this.conn.send(msg);
    }

    public CRoom getRoom(CGameListEntry game) {
        return (CRoom)this.conn.objects.get(game.roomId);
    }

    public Connector getConnector() {
        return this.conn.getConnector();
    }

    public void sendStartPlayback(long playbackId) {
        TxMessage msg = new TxMessage(MsgTypesUp.START_PLAYBACK);
        msg.writeLong(playbackId);
        this.conn.send(msg);
    }

    public ConnMutex getConnMutex() {
        return this.conn.mutex;
    }

    public void sendPlaybackListReq(long start, long end) {
        if (this.playbacksRequested.add(start)) {
            TxMessage msg = new TxMessage(MsgTypesUp.REQUEST_PLAYBACK_LIST);
            msg.writeLong(start);
            msg.writeLong(end);
            this.conn.send(msg);
        }
    }

    public TreeMap<Long, PlaybackInfo> getPlaybackList() {
        return this.playbackList;
    }

    public void sendSetPassword(String who, String newPassword) {
        TxMessage tx = new TxMessage(MsgTypesUp.SET_PASSWORD);
        tx.writeUTF(User.canonName(who));
        tx.write(Client.rsaEncrypt(User.passwordCompute(newPassword)), 0, 256);
        this.conn.send(tx);
    }

    protected abstract CArchive loadArchive(DataInputStream var1, boolean var2) throws IOException;

    public CRoomCatChannel getRoomCategory(RoomCategories cat) {
        return this.roomCats.get((Object)cat);
    }

    private void handleLoginSuccess(DataInputStream in) throws IOException {
        int type;
        User me = this.conn.getUser(in);
        this.conn.setMe(me);
        long newId = in.readLong();
        if (newId != this.clientId) {
            this.clientId = newId;
            this.emit(106, newId);
        }
        while ((type = in.read()) != 255) {
            User friend = this.conn.getUser(in);
            if (type >= 4) continue;
            this.conn.friendsGroups[type].add(friend, in.readUTF());
        }
        this.subscriptions = Subscription.read(in);
        int numCategories = in.readByte();
        this.roomCats = new EnumMap(RoomCategories.class);
        for (int i = 0; i < numCategories; ++i) {
            this.roomCats.put(RoomCategories.values()[i], new CRoomCatChannel(this.conn, in.readInt(), RoomCategories.values()[i]));
        }
        while (in.available() > 0 && this.readRoomChanInfo(in)) {
        }
        this.emit(86);
        if (me.isGuest()) {
            this.emit(12, Defs.getString(2031923651));
        }
    }

    private boolean readRoomChanInfo(DataInputStream in) throws IOException {
        int chanId = in.readInt();
        if (chanId == 0) {
            return false;
        }
        RoomCategories category = RoomCategories.read(in);
        CRoom room = (CRoom)this.conn.objects.get(chanId);
        if (room == null) {
            room = new CRoom(this.conn, chanId, category, this);
            this.conn.objects.put(room.id, room);
            this.emit(90, room);
        } else {
            room.setCategory(category);
        }
        return true;
    }

    @Override
    public CGameListEntry readGame(DataInputStream in) throws IOException {
        byte typeId = in.readByte();
        if (typeId == -1) {
            return null;
        }
        GameType gameType = GameType.get(typeId);
        int id = in.readInt();
        CGameListEntry currentGame = (CGameListEntry)this.conn.objects.get(id);
        if (currentGame == null) {
            currentGame = this.loadGame(in, id, gameType);
            this.emit(96, currentGame);
        } else {
            in.readInt();
            currentGame.readFrom(in);
        }
        return currentGame;
    }

    protected abstract CGameListEntry loadGame(DataInputStream var1, int var2, GameType var3) throws IOException;

    protected abstract GameSummary<User> loadGameSummary(DataInputStream var1) throws IOException;

    protected abstract CPlayback buildPlayback(int var1, long var2, GameSummary<User> var4);

    public Subscription[] getSubscriptions() {
        return this.subscriptions;
    }

    public void sendJoinGameContainer(GameContainers gcType) {
        TxMessage tx = new TxMessage(MsgTypesUp.GLOBAL_LIST_JOIN_REQUEST);
        tx.write(gcType.ordinal());
        this.conn.send(tx);
    }

    public void sendRegister(String email, String name, String info, boolean privateEmail, boolean wantsEmail) {
        TxMessage tx = new TxMessage(MsgTypesUp.REGISTER);
        tx.writeUTF(email);
        tx.writeUTF(name == null ? "" : name);
        tx.writeUTF(info == null ? "" : info);
        tx.writeBoolean(privateEmail);
        tx.writeBoolean(wantsEmail);
        this.conn.send(tx);
    }

    public String toString() {
        return "Client[" + this.getMe() + "]";
    }

    public Conn getConn() {
        return this.conn;
    }

    public boolean isClosed() {
        return this.conn.isClosed();
    }

    public void sendAutomatchCancel() {
        this.conn.send(new TxMessage(MsgTypesUp.AUTOMATCH_CANCEL));
    }

    public void sendAutomatchCreate(int prefs) {
        this.automatchPrefs = prefs;
        TxMessage tx = new TxMessage(MsgTypesUp.AUTOMATCH_CREATE);
        tx.writeInt(prefs);
        this.conn.send(tx);
    }

    public void sendSetAutomatchPrefs(int prefs) {
        this.automatchPrefs = prefs;
        TxMessage tx = new TxMessage(MsgTypesUp.AUTOMATCH_SET_PREFS);
        tx.writeInt(prefs);
        this.conn.send(tx);
    }

    public boolean isAutomatchRunning() {
        return this.automatchRunning;
    }

    public int getAutomatchPrefs() {
        return this.automatchPrefs;
    }

    public void setTag(long gameId, String tag) {
        TxMessage msg = new TxMessage(MsgTypesUp.TAG_GAME);
        msg.writeLong(gameId);
        if (tag == null) {
            tag = "";
        } else if (tag.length() > 50) {
            tag = tag.substring(0, 50);
        }
        if (this.myTags == null || !tag.equals(this.myTags.get(gameId, ""))) {
            msg.writeUTF(tag);
            this.conn.send(msg);
            if (this.myTags != null) {
                if (tag.isEmpty()) {
                    this.myTags.remove(gameId);
                } else {
                    this.myTags.put(gameId, tag);
                }
            }
        }
    }

    public String getTag(long gameId) {
        return this.myTags == null ? null : this.myTags.get(gameId);
    }

    public void getMyTags() {
        this.conn.send(new TxMessage(MsgTypesUp.FETCH_TAGS));
    }

    public boolean isGameTagsLoaded() {
        return this.myTags != null;
    }

    public CChannel getChannel(int id) {
        return (CChannel)this.conn.objects.get(id);
    }

    public final User getCachedUser(String name) {
        return this.conn.getCachedUser(name);
    }

    private class TournNotifyHandler
    implements EventListener {
        private CGameListEntry game;
        private boolean hasStarted;

        public TournNotifyHandler(CRoom room, CGameListEntry newGame, boolean newHasStarted) {
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
}
