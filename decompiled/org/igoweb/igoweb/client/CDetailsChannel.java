/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import org.igoweb.igoweb.client.CChannel;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.igoweb.shared.MsgTypesUp;
import org.igoweb.igoweb.shared.Subscription;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.igoweb.shared.User;

public class CDetailsChannel
extends CChannel {
    static final String CLASS_PREFIX = "F3:";
    static final String ORIG_NAME_KEY = "F3a:";
    public static final int MAX_NAME_LEN = 50;
    public static final int MAX_EMAIL_LEN = 70;
    public static final int MAX_INFO_LEN = 1500;
    private static final int EVENT_BASE = 32;
    public static final int CHANGE_EVENT = 32;
    public static final int REOPEN_EVENT = 33;
    public static final int RANK_GRAPH_EVENT = 34;
    public static final int DETAILS_EVENT_LIMIT = 35;
    public final User owner;
    private boolean forcedNoRank;
    private boolean emailWanted;
    private boolean emailPrivate;
    private Date lastOn;
    private Date registrationStart;
    private String personalName;
    private String personalEmail;
    private String personalInfo;
    private Locale locale;
    private Subscription[] subscriptions;
    private short[] rankGraph;
    private boolean rankGraphRequested;

    CDetailsChannel(Conn newConn, DataInputStream in) throws IOException {
        super(newConn, in.readInt());
        this.owner = newConn.getUser(in);
        this.update(in);
        newConn.objects.put(CLASS_PREFIX + this.owner.canonName(), this);
    }

    @Override
    protected void handleMessage(MsgTypesDown msgType, DataInputStream in) throws IOException {
        switch (msgType) {
            case DETAILS_UPDATE: {
                this.update(in);
                break;
            }
            case DETAILS_RANK_GRAPH: {
                this.rankGraph = new short[in.available() / 2];
                for (int i = 0; i < this.rankGraph.length; ++i) {
                    this.rankGraph[i] = in.readShort();
                }
                this.emit(34, this.rankGraph);
                break;
            }
            default: {
                super.handleMessage(msgType, in);
            }
        }
    }

    void update(DataInputStream in) throws IOException {
        int flags = in.readInt();
        this.forcedNoRank = (flags & 1) != 0;
        this.emailWanted = (flags & 2) != 0;
        this.emailPrivate = (flags & 4) != 0;
        this.lastOn = new Date(in.readLong());
        this.registrationStart = new Date(in.readLong());
        this.personalName = in.readUTF();
        this.personalEmail = this.emailPrivate ? null : in.readUTF();
        this.personalInfo = in.readUTF();
        String localeStr = in.readUTF();
        Locale locale = this.locale = localeStr.length() == 5 ? new Locale(localeStr.substring(0, 2), localeStr.substring(3, 5)) : new Locale(localeStr, "");
        if (in.available() > 0) {
            if (this.emailPrivate) {
                this.personalEmail = in.readUTF();
            }
            this.subscriptions = Subscription.read(in);
        }
        this.emit(32);
    }

    public boolean isMe() {
        return this.owner == this.conn.getMe();
    }

    public boolean isForcedNoRank() {
        return this.forcedNoRank;
    }

    public boolean isEmailWanted() {
        return this.emailWanted;
    }

    public boolean isEmailPrivate() {
        return this.emailPrivate;
    }

    public Date getLastOn() {
        return this.lastOn;
    }

    public Date getRegistrationStart() {
        return this.registrationStart;
    }

    public String getPersonalName() {
        return this.personalName;
    }

    public String getPersonalEmail() {
        return this.personalEmail;
    }

    public String getPersonalInfo() {
        return this.personalInfo;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public Subscription[] getSubscriptions() {
        return this.subscriptions;
    }

    public boolean isDataChanged(String newPersonalName, boolean newRankWanted, String newPersonalEmail, boolean newEmailPrivate, boolean newEmailWanted, String newPersonalInfo, boolean newForcedNoRank, int newAuthLevel) {
        return !newPersonalName.equals(this.personalName) || newRankWanted != this.owner.isRankWanted() || !newPersonalEmail.equals(this.personalEmail) || newEmailPrivate != this.emailPrivate || newEmailWanted != this.emailWanted || !newPersonalInfo.equals(this.personalInfo) || newForcedNoRank != this.forcedNoRank || newAuthLevel != this.owner.getAuthLevel();
    }

    public void sendChanges(String newPersonalName, boolean newRankWanted, String newPersonalEmail, boolean newEmailPrivate, boolean newEmailWanted, String newPersonalInfo, boolean newForcedNoRank, int newAuthLevel) {
        if (!this.isDataChanged(newPersonalName, newRankWanted, newPersonalEmail, newEmailPrivate, this.emailWanted, newPersonalInfo, newForcedNoRank, newAuthLevel)) {
            return;
        }
        TxMessage tx = this.buildMessage(MsgTypesUp.DETAILS_CHANGE);
        tx.writeUTF(newPersonalName);
        tx.writeUTF(newPersonalEmail);
        tx.writeUTF(newPersonalInfo);
        int flags = 0;
        if (newRankWanted) {
            flags |= 8;
        }
        if (newEmailPrivate) {
            flags |= 4;
        }
        if (newEmailWanted) {
            flags |= 2;
        }
        if (newForcedNoRank) {
            flags |= 1;
        }
        tx.write(flags);
        tx.write(newAuthLevel);
        this.conn.send(tx);
    }

    public final void reopen() {
        this.emit(33);
    }

    @Override
    public void unjoin() {
        this.conn.objects.remove(CLASS_PREFIX + this.owner.canonName());
    }

    public short[] requestRankGraphData() {
        if (!this.rankGraphRequested) {
            this.rankGraphRequested = true;
            this.conn.send(this.buildMessage(MsgTypesUp.DETAILS_RANK_GRAPH_REQUEST));
        }
        return this.rankGraph;
    }

    public String toString() {
        return "CDetailsChannel[" + this.owner + ", forcedNoRank=" + this.forcedNoRank + ", emailWanted=" + this.emailWanted + ", emailPrivate=" + this.emailPrivate + ", lastOn=" + this.lastOn + ", registrationStart=" + this.registrationStart + ", personalName=" + this.personalName + ", personalEmail=" + this.personalEmail + ", personalInfo=" + this.personalInfo + ", locale=" + this.locale + "]";
    }
}
