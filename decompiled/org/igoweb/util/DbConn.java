/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;

public class DbConn {
    static final int CLOSE_EVENT = 0;
    static final int DESTROY_EVENT = 1;
    private int maxStatements = 3;
    private final LinkedHashMap<String, PreparedStatement> statements = new LinkedHashMap(10, 0.75f, true);
    private final Connection conn;
    private final EventListener closeListener;
    private int eventCode = 0;
    private Statement stmt = null;
    private boolean doesStmtCacheResults;
    private State state = State.IDLE;

    DbConn(Connection conn, EventListener closeListener) {
        this.conn = conn;
        this.closeListener = closeListener;
    }

    public PreparedStatement get(String query, boolean getGeneratedKeys) throws SQLException {
        if (this.state != State.READY) {
            throw new IllegalStateException();
        }
        PreparedStatement result = this.statements.get(query);
        if (result == null) {
            result = this.conn.prepareStatement(query, getGeneratedKeys ? 1 : 2);
            this.statements.put(query, result);
        }
        return result;
    }

    public final PreparedStatement get(String query) throws SQLException {
        return this.get(query, false);
    }

    public void close() {
        this.close(null);
    }

    void setReady() {
        if (this.state != State.IDLE) {
            throw new IllegalStateException();
        }
        this.state = State.READY;
    }

    public void close(ResultSet rs) {
        block11: {
            try {
                if (this.statements.size() > this.maxStatements) {
                    Iterator<PreparedStatement> iter = this.statements.values().iterator();
                    while (this.statements.size() > this.maxStatements) {
                        iter.next().close();
                        iter.remove();
                    }
                }
                switch (this.state) {
                    case DYING: {
                        this.state = State.DEAD;
                        break;
                    }
                    case READY: {
                        this.state = State.IDLE;
                        break;
                    }
                    default: {
                        throw new IllegalStateException("State was " + (Object)((Object)this.state));
                    }
                }
                if (this.stmt != null) {
                    this.stmt.close();
                    this.stmt = null;
                }
                if (rs != null) {
                    rs.close();
                }
                this.closeListener.handleEvent(new Event(this, this.eventCode));
                if (this.eventCode == 1) {
                    this.conn.close();
                }
            }
            catch (SQLException excep) {
                if (this.eventCode == 1) break block11;
                throw new RuntimeException("Error while closing DbConn", excep);
            }
        }
    }

    public void error() {
        this.eventCode = 1;
        this.state = State.DYING;
    }

    void destroy() {
        if (this.state != State.IDLE) {
            throw new IllegalStateException();
        }
        this.state = State.DEAD;
        try {
            for (PreparedStatement ps : this.statements.values()) {
                ps.close();
            }
            this.conn.close();
        }
        catch (SQLException excep) {
            throw new RuntimeException("Error closing DbConn", excep);
        }
    }

    public boolean execute(String exec) throws SQLException {
        if (this.state != State.READY) {
            throw new IllegalStateException();
        }
        if (this.stmt == null) {
            this.stmt = this.conn.createStatement();
        }
        return this.stmt.execute(exec);
    }

    public final ResultSet executeQuery(String exec) throws SQLException {
        return this.executeQuery(exec, true);
    }

    public ResultSet executeQuery(String exec, boolean cachedResult) throws SQLException {
        if (this.state != State.READY) {
            throw new IllegalStateException();
        }
        if (this.stmt != null && cachedResult != this.doesStmtCacheResults) {
            this.stmt.close();
            this.stmt = null;
        }
        if (this.stmt == null) {
            if (cachedResult) {
                this.stmt = this.conn.createStatement();
            } else {
                this.stmt = this.conn.createStatement(1003, 1007);
                this.stmt.setFetchSize(Integer.MIN_VALUE);
            }
            this.doesStmtCacheResults = cachedResult;
        }
        return this.stmt.executeQuery(exec);
    }

    public static String stripSupplementaries(String src) {
        if (src == null) {
            return src;
        }
        int srcLen = src.length();
        for (int i = 0; i < srcLen; ++i) {
            char c = src.charAt(i);
            if (!Character.isHighSurrogate(c) && !Character.isLowSurrogate(c)) continue;
            StringBuilder dest = new StringBuilder(src.substring(0, i));
            for (int j = i + 2; j < srcLen; ++j) {
                c = src.charAt(j);
                if (Character.isHighSurrogate(c) || Character.isLowSurrogate(c)) continue;
                dest.append(c);
            }
            return dest.toString();
        }
        return src;
    }

    static enum State {
        IDLE,
        READY,
        DYING,
        DEAD;

    }
}
