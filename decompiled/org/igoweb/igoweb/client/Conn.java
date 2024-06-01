/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.igoweb.igoweb.client.ConnMutex;
import org.igoweb.igoweb.client.Connector;
import org.igoweb.igoweb.client.FriendsGroup;
import org.igoweb.igoweb.shared.MsgTypesUp;
import org.igoweb.igoweb.shared.Proposal;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.igoweb.shared.User;
import org.igoweb.util.CacheMap;
import org.igoweb.util.Defs;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;

public class Conn
implements Proposal.UserDecoder<User> {
    private short lastSyncId = 0;
    private User me;
    final ConnMutex mutex;
    public final HashMap<Object, Object> objects = new HashMap();
    public final FriendsGroup[] friendsGroups = new FriendsGroup[4];
    private final CacheMap<String, User> users = new CacheMap(true);
    public final HashSet<Object> subscribedRooms = new HashSet();
    private final Connector connector;
    private String error = null;
    private final EventListener listener;
    private boolean pingerActive = false;
    private DataInputStream in;
    private OutputStream out;
    private boolean closing = false;
    private static final int EVENT_BASE = 8;
    public static final int MESSAGE_IN_EVENT = 8;
    public static final int CLOSED_EVENT = 9;
    public static final int INTERNAL_ERROR_EVENT = 10;
    static final int EVENT_LIMIT = 12;
    private static final long PING_PERIOD = 70000L;
    private boolean errorShown = false;
    private long lastOutboundMessage = 0L;
    private boolean closedEmitted;
    private final Logger logger;

    public Conn(Connector newConnector, EventListener newListener, ConnMutex newMutex, Logger newLogger) {
        this.connector = newConnector;
        this.listener = newListener;
        this.mutex = newMutex;
        this.logger = newLogger;
        for (int i = 0; i < 4; ++i) {
            this.friendsGroups[i] = new FriendsGroup();
        }
    }

    public void go() {
        this.mutex.go();
        new Thread(this::waitForMessages, "igoweb client conn reader").start();
    }

    public synchronized void close() {
        if (!this.closing) {
            if (this.logger != null) {
                this.logger.fine("close()");
            }
            this.closing = true;
            this.connector.cutoff();
            this.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void send(TxMessage msg) {
        Conn conn = this;
        synchronized (conn) {
            if (this.error == null && this.out != null) {
                if (this.lastOutboundMessage == 0L) {
                    this.notifyAll();
                }
                this.lastOutboundMessage = System.currentTimeMillis();
                try {
                    msg.writeTo(this.out);
                    this.out.flush();
                }
                catch (IOException excep) {
                    this.error = Defs.getString(2031923652, excep.getMessage());
                    this.close();
                }
                if (!this.pingerActive) {
                    this.pingerActive = true;
                    new Thread(this::doPings, "igoweb client pinger").start();
                }
            }
        }
    }

    public void emit(Event event) {
        this.mutex.runSynchronized(() -> this.doEmit(event));
    }

    private void doEmit(Event event) {
        try {
            if (event.type == 9) {
                this.handleCloseEmitted();
            }
            this.listener.handleEvent(event);
        }
        catch (Throwable fatalExcep) {
            if (this.logger != null) {
                this.logger.log(Level.WARNING, "Exception handling conn event", fatalExcep);
            }
            if (!this.errorShown) {
                this.errorShown = true;
                this.listener.handleEvent(new Event(this, 10, fatalExcep));
            }
            this.close();
        }
    }

    protected void handleCloseEmitted() {
        this.closedEmitted = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void waitForMessages() {
        try {
            this.error = this.connector.connect();
            if (this.logger != null) {
                this.logger.info("Connection result: " + this.error);
            }
            if (this.error == null) {
                this.in = new DataInputStream(new BufferedInputStream(this.connector.getInputStream()));
                this.out = new DataOutputStream(this.connector.getOutputStream());
                this.in.readInt();
                while (!this.closing) {
                    int len = this.in.readShort();
                    if (len == Short.MAX_VALUE) {
                        len = this.in.readInt();
                    }
                    if (len < 0) {
                        throw new IOException("Invalid message length: " + len + " (0x" + Integer.toString(len & 0xFFFF, 16) + ")");
                    }
                    byte[] buf = new byte[len];
                    this.in.readFully(buf);
                    this.emit(new Event(this, 8, new DataInputStream(new ByteArrayInputStream(buf))));
                }
            }
        }
        catch (IOException excep) {
            boolean expectedEof;
            boolean bl = expectedEof = this.closing && excep instanceof EOFException;
            if (this.logger != null) {
                this.logger.log(expectedEof ? Level.FINE : Level.WARNING, "Got IOException for " + this.me, excep);
            }
            excep.printStackTrace();
            Conn conn = this;
            synchronized (conn) {
                if (this.error == null && !expectedEof) {
                    this.error = excep.getMessage() == null || excep instanceof EOFException ? Defs.getString(2031923680) : Defs.getString(2031923652, excep.getMessage());
                }
                this.close();
            }
        }
        catch (Throwable thrown) {
            if (this.logger != null) {
                this.logger.log(Level.WARNING, "Unexpected error", thrown);
            }
            if (this.error == null) {
                this.error = "Unexpected error: " + thrown;
            }
            throw thrown;
        }
        finally {
            Conn excep = this;
            synchronized (excep) {
                if (this.in != null) {
                    try {
                        this.in.close();
                        this.out.close();
                    }
                    catch (IOException iOException) {}
                    this.in = null;
                    this.out = null;
                    this.notifyAll();
                }
            }
            this.emit(new Event(this, 9, this.error));
            this.mutex.close();
        }
    }

    public void setMe(User newMe) {
        this.me = newMe;
    }

    public User getMe() {
        return this.me;
    }

    public Connector getConnector() {
        return this.connector.cloneParams();
    }

    private void doPings() {
        try {
            Conn conn = this;
            synchronized (conn) {
                while (true) {
                    if (this.closing || this.out == null) {
                        return;
                    }
                    long now = System.currentTimeMillis();
                    long sleepTime = this.lastOutboundMessage + 70000L - now;
                    if (sleepTime <= 0L) {
                        this.send(new TxMessage(MsgTypesUp.PING));
                        this.lastOutboundMessage = now;
                        sleepTime = 70000L;
                    }
                    this.wait(sleepTime);
                }
            }
        }
        catch (Throwable thrown) {
            this.emit(new Event(this, 10, thrown));
            return;
        }
    }

    public void sendSync(Runnable runnable) {
        TxMessage sync = new TxMessage(MsgTypesUp.SYNC_REQUEST);
        this.lastSyncId = (short)(this.lastSyncId + 1);
        sync.writeShort(this.lastSyncId);
        this.send(sync);
        if (this.objects.put("sync:" + this.lastSyncId, runnable) != null) {
            throw new IllegalArgumentException();
        }
    }

    public void performSyncCallback(int id) {
        ((Runnable)this.objects.remove("sync:" + id)).run();
    }

    @Override
    public User getUser(DataInput userIn) throws IOException {
        String name = userIn.readUTF();
        if (name.isEmpty()) {
            return null;
        }
        int flags = userIn.readInt();
        User user = this.users.get(name);
        if (user == null) {
            user = new User(name, flags);
            this.users.put(name, user);
        } else {
            user.setFlags(flags);
        }
        return user;
    }

    public ConnMutex getMutex() {
        return this.mutex;
    }

    public boolean isClosed() {
        return this.closedEmitted;
    }

    public User getCachedUser(String name) {
        return this.users.get(name);
    }
}
