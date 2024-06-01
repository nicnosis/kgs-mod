/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.DataInput;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.util.DbConn;
import org.igoweb.util.DbConnFactory;
import org.igoweb.util.LockOrder;

public class Subscription {
    public static final LockOrder LOCK_ORDER = new LockOrder(Subscription.class);
    private int id;
    public static final long FIRST_SUB_LEAD_TIME = TimeUnit.DAYS.toMillis(7L);
    public static final long ALWAYS_SEND_LIMIT = TimeUnit.DAYS.toMillis(35L);
    public static final long DAY_END = TimeUnit.DAYS.toMillis(1L) - 1L;
    public final long start;
    public final long end;
    private static final Subscription[] prototype = new Subscription[0];

    public Subscription(long newStart, long newEnd) {
        this.start = newStart;
        this.end = newEnd;
        this.id = -1;
    }

    private Subscription(long newStart, long newEnd, int newId) {
        this.start = newStart;
        this.end = newEnd;
        this.id = newId;
    }

    public static Subscription[] load(DbConnFactory dbf, int userId) {
        try (DbConn db = dbf.getDbConn();){
            Subscription[] subscriptionArray = Subscription.load(db, userId);
            return subscriptionArray;
        }
    }

    public static Subscription[] load(DbConn db, int userId) throws SQLException {
        ArrayList<Subscription> tmpSubs = null;
        PreparedStatement ps = db.get("SELECT * FROM subscriptions WHERE account_id = ? ORDER BY start");
        ps.setInt(1, userId);
        try (ResultSet rs = ps.executeQuery();){
            long leadTime = FIRST_SUB_LEAD_TIME;
            while (rs.next()) {
                if (tmpSubs == null) {
                    tmpSubs = new ArrayList<Subscription>();
                }
                tmpSubs.add(new Subscription(rs.getDate("start").getTime() - leadTime, rs.getDate("end").getTime() + DAY_END, rs.getInt("id")));
                leadTime = 0L;
            }
            Subscription[] subscriptionArray = tmpSubs == null ? null : tmpSubs.toArray(prototype);
            return subscriptionArray;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void updateEnd(DbConn db, long newEnd) throws SQLException {
        assert (LockOrder.testAcquire(this));
        Subscription subscription = this;
        synchronized (subscription) {
            PreparedStatement ps;
            if (this.id == -1) {
                throw new IllegalStateException("No ID: " + this);
            }
            if (newEnd <= this.start) {
                ps = db.get("DELETE FROM subscriptions  WHERE id = ?");
                ps.setInt(1, this.id);
            } else {
                ps = db.get("UPDATE subscriptions  SET end = ?  WHERE id = ?");
                ps.setDate(1, new java.sql.Date(newEnd));
                ps.setInt(2, this.id);
            }
            ps.executeUpdate();
        }
    }

    public int store(DbConnFactory factory, int userId) {
        try (DbConn db = factory.getDbConn();){
            int n = this.store(db, userId);
            return n;
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public int store(DbConn db, int userId) throws SQLException {
        assert (LockOrder.testAcquire(this));
        Subscription subscription = this;
        synchronized (subscription) {
            if (this.id != -1) {
                throw new RuntimeException("Subscription already inserted: " + this);
            }
            PreparedStatement ps = db.get("INSERT INTO subscriptions (account_id, start, end)  VALUES (?, ?, ?)", true);
            ps.setInt(1, userId);
            ps.setDate(2, new java.sql.Date(this.start));
            ps.setDate(3, new java.sql.Date(this.end));
            ps.executeUpdate();
            if (ps.getUpdateCount() != 1) {
                throw new RuntimeException("Update count = " + ps.getUpdateCount());
            }
            try (ResultSet rs = ps.getGeneratedKeys();){
                rs.next();
                int n = this.id = rs.getInt(1);
                return n;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getSubscriptionId() {
        assert (LockOrder.testAcquire(this));
        Subscription subscription = this;
        synchronized (subscription) {
            return this.id;
        }
    }

    public Date getStartDate() {
        return new Date(this.start);
    }

    public Instant getStartInstant() {
        return Instant.ofEpochMilli(this.start);
    }

    public Date getEndDate() {
        return new Date(this.end);
    }

    public Instant getEndInstant() {
        return Instant.ofEpochMilli(this.end);
    }

    public boolean isActive() {
        return this.isActive(System.currentTimeMillis());
    }

    public boolean isActive(long when) {
        return when >= this.start && when <= this.end;
    }

    public static boolean isSubscribed(long time, Subscription[] subscriptions) {
        if (subscriptions == null || subscriptions.length == 0) {
            return false;
        }
        for (int i = subscriptions.length - 1; i >= 0; --i) {
            if (subscriptions[i].start > time || subscriptions[i].end < time) continue;
            return true;
        }
        return false;
    }

    public static void write(TxMessage tx, Subscription[] subscriptions) {
        if (subscriptions == null) {
            tx.write(0);
            return;
        }
        if (subscriptions.length > 255) {
            throw new IllegalArgumentException();
        }
        tx.write(subscriptions.length);
        for (Subscription sub : subscriptions) {
            tx.writeLong(sub.start);
            tx.writeLong(sub.end);
        }
    }

    public static Subscription[] read(DataInput in) throws IOException {
        int num = in.readByte() & 0xFF;
        if (num == 0) {
            return null;
        }
        Subscription[] result = new Subscription[num];
        for (int i = 0; i < num; ++i) {
            long start = in.readLong();
            result[i] = new Subscription(start, in.readLong());
        }
        return result;
    }

    public static long getExpiration(Subscription[] subs) {
        if (subs == null) {
            return 0L;
        }
        long date = 0L;
        for (int i = subs.length - 1; i >= 0; --i) {
            if (subs[i].end <= date) continue;
            date = subs[i].end;
        }
        return date;
    }

    public static String toString(Subscription[] subscriptions) {
        StringBuilder sb = new StringBuilder("Subscription[](");
        if (subscriptions == null) {
            sb.append("null");
        } else {
            for (Subscription sub : subscriptions) {
                sb.append(new Date(sub.start)).append("..").append(new Date(sub.end)).append(", ");
            }
        }
        return sb.append(']').toString();
    }

    public String toString() {
        return "Subscription[" + this.id + ": " + new java.sql.Date(this.start) + ".." + new java.sql.Date(this.end) + "]";
    }

    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Subscription peer = (Subscription)o;
        return this.start == peer.start && this.end == peer.end;
    }
}
