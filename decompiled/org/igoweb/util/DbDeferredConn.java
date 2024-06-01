/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.igoweb.util.ThreadPool;

public class DbDeferredConn {
    private final Connection conn;
    private final ThreadPool threadPool;
    private boolean isActive;
    private final LinkedList<PreparedStatement> statements = new LinkedList();
    private final Logger logger;

    DbDeferredConn(Connection newConn, ThreadPool newThreadPool, Logger newLogger) {
        this.conn = newConn;
        this.threadPool = newThreadPool;
        this.logger = newLogger;
    }

    public PreparedStatement get(String query) throws SQLException {
        return this.conn.prepareStatement(query);
    }

    public synchronized void execute(PreparedStatement statement) {
        this.statements.add(statement);
        if (!this.isActive) {
            this.isActive = true;
            this.threadPool.execute(this::work);
        }
    }

    public void execute(String sql) throws SQLException {
        this.execute(this.conn.prepareStatement(sql));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void work() {
        while (true) {
            PreparedStatement statement;
            DbDeferredConn dbDeferredConn = this;
            synchronized (dbDeferredConn) {
                if (this.statements.isEmpty()) {
                    this.isActive = false;
                    return;
                }
                statement = this.statements.removeFirst();
            }
            try {
                statement.execute();
                statement.close();
            }
            catch (SQLException excep) {
                throw new RuntimeException("Error executing " + statement, excep);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean destroy(boolean wait) {
        try {
            DbDeferredConn dbDeferredConn = this;
            synchronized (dbDeferredConn) {
                boolean logged = false;
                while (this.isActive) {
                    if (!wait) {
                        return false;
                    }
                    if (!logged) {
                        this.logger.info("DbDeferredConn.destroy: Waiting for statements to finish");
                        logged = true;
                    }
                    this.wait();
                }
            }
            this.conn.close();
            return true;
        }
        catch (InterruptedException excep) {
            throw new RuntimeException("Interrupted while waiting for statements to finish", excep);
        }
        catch (SQLException excep) {
            throw new RuntimeException("Error closing deferred DB connection", excep);
        }
    }
}
