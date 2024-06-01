/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.igoweb.util.DbConn;
import org.igoweb.util.DbDeferredConn;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;
import org.igoweb.util.LockOrder;
import org.igoweb.util.ThreadPool;

public class DbConnFactory {
    public static final LockOrder LOCK_ORDER = new LockOrder(DbConnFactory.class);
    public static final int MAX_FREE_CONNS = 5;
    private final String url;
    private final String user;
    private final String password;
    private ArrayList<DbConn> dbConns = new ArrayList();
    private DbDeferredConn dbDeferredConn = null;
    private ThreadPool threadPool;
    private ThreadPool.DelayedTask task;
    private boolean connIdle = true;
    private boolean deferredConnIdle = true;
    private final boolean customThreadPool;
    private int connErrors = 0;
    private Logger logger;
    private final EventListener closeListener = this::handleDbConnEvent;

    public DbConnFactory(String dbName, String newUser, String newPassword) {
        this("localhost", dbName, newUser, newPassword, null);
    }

    public DbConnFactory(String dbHostName, String dbName, String newUser, String newPassword) {
        this(dbHostName, dbName, newUser, newPassword, null, null);
    }

    public DbConnFactory(String dbHostName, String dbName, String newUser, String newPassword, ThreadPool threads) {
        this(dbHostName, dbName, newUser, newPassword, threads, null);
    }

    public DbConnFactory(String dbHostName, String dbName, String newUser, String newPassword, ThreadPool newThreadPool, Logger newLogger) {
        this.customThreadPool = newThreadPool == null;
        this.threadPool = newThreadPool;
        this.url = "jdbc:mysql://" + dbHostName + "/" + dbName;
        this.user = newUser;
        this.password = newPassword;
        this.logger = newLogger;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        }
        catch (Exception excep) {
            throw new RuntimeException("Error while trying to install MySQL JDBC driver", excep);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void close() {
        assert (LockOrder.testAcquire(this));
        DbConnFactory dbConnFactory = this;
        synchronized (dbConnFactory) {
            if (this.dbDeferredConn != null) {
                this.dbDeferredConn.destroy(true);
                this.dbDeferredConn = null;
            }
            while (this.task != null) {
                this.cleanOldConns();
            }
            this.dbConns = null;
            this.notifyAll();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DbConn getDbConn() {
        DbConn result;
        assert (LockOrder.testAcquire(this));
        DbConnFactory dbConnFactory = this;
        synchronized (dbConnFactory) {
            if (this.dbConns == null) {
                throw new IllegalStateException("Connection factory has already closed");
            }
            if (this.dbConns.isEmpty()) {
                this.dbConns.add(new DbConn(this.buildDbConnection(), this.closeListener));
            }
            result = this.dbConns.remove(this.dbConns.size() - 1);
        }
        result.setReady();
        return result;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DbDeferredConn getDbDeferredConn() {
        assert (LockOrder.testAcquire(this));
        DbConnFactory dbConnFactory = this;
        synchronized (dbConnFactory) {
            if (this.dbConns == null) {
                throw new IllegalStateException("Connection factory has already closed");
            }
            this.deferredConnIdle = false;
            if (this.dbDeferredConn == null) {
                this.dbDeferredConn = new DbDeferredConn(this.buildDbConnection(), this.threadPool, this.logger);
            }
            return this.dbDeferredConn;
        }
    }

    private synchronized Connection buildDbConnection() {
        while (true) {
            try {
                return DriverManager.getConnection(this.url, this.user, this.password);
            }
            catch (SQLException excep) {
                if (this.logger == null) continue;
                this.logger.log(Level.WARNING, "Error creating DB Conn, url=" + this.url + ", user=" + this.user, excep);
                if (++this.connErrors <= 4) continue;
                this.dbConns = null;
                throw new RuntimeException("Too many failures connecting to DB", excep);
            }
            break;
        }
    }

    private void handleDbConnEvent(Event event) {
        if (event.type == 0) {
            this.closeConn((DbConn)event.source);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void closeConn(DbConn conn) {
        assert (LockOrder.testAcquire(this));
        DbConnFactory dbConnFactory = this;
        synchronized (dbConnFactory) {
            int poolSize;
            int n = poolSize = this.dbConns == null ? 5 : this.dbConns.size();
            if (poolSize < 5) {
                if (poolSize == 0) {
                    this.connIdle = false;
                    if (this.task == null) {
                        if (this.threadPool == null) {
                            this.threadPool = new ThreadPool(1);
                        }
                        this.task = this.threadPool.scheduleAtFixedRate(this::cleanOldConns, 60000L, 60000L);
                    }
                }
                this.dbConns.add(conn);
                this.notifyAll();
                conn = null;
            }
        }
        if (conn != null) {
            conn.destroy();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void cleanOldConns() {
        DbConn deadConn = null;
        assert (LockOrder.testAcquire(this));
        DbConnFactory dbConnFactory = this;
        synchronized (dbConnFactory) {
            if (this.deferredConnIdle) {
                if (this.dbDeferredConn != null && this.dbDeferredConn.destroy(false)) {
                    this.dbDeferredConn = null;
                }
            } else {
                this.deferredConnIdle = true;
            }
            if (this.connIdle) {
                if (this.dbConns.isEmpty()) {
                    if (this.dbDeferredConn == null) {
                        this.task.cancel();
                        this.task = null;
                        if (this.customThreadPool) {
                            this.threadPool.shutdown();
                            this.threadPool = null;
                        }
                    }
                } else {
                    deadConn = this.dbConns.remove(0);
                }
            } else {
                this.connIdle = true;
            }
        }
        if (deadConn != null) {
            deadConn.destroy();
        }
    }

    public void setLogger(Logger newLogger) {
        this.logger = newLogger;
    }

    static {
        assert (LOCK_ORDER.addInnerOrder(ThreadPool.LOCK_ORDER));
    }
}
