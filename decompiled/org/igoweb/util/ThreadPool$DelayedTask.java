/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import org.igoweb.util.LockOrder;

public class ThreadPool.DelayedTask
implements Runnable {
    private long when;
    private final long period;
    private Runnable task;
    private boolean fixedRate;
    private int offset;
    private boolean inQueue;

    private ThreadPool.DelayedTask(long newWhen, long newPeriod, Runnable newTask, boolean newFixedRate) {
        this.when = newWhen;
        this.period = newPeriod;
        this.task = newTask;
        this.fixedRate = newFixedRate;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean cancel() {
        assert (LockOrder.testAcquire(this));
        ThreadPool.DelayedTask delayedTask = this;
        synchronized (delayedTask) {
            if (this.task == null) {
                return false;
            }
            this.task = null;
            assert (LockOrder.testAcquire(ThreadPool.this.delayQueueLock));
            Object object = ThreadPool.this.delayQueueLock;
            synchronized (object) {
                if (this.inQueue) {
                    ThreadPool.this.removeDelay(this.offset);
                }
            }
            return true;
        }
    }

    public boolean isPending() {
        return this.task != null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        Runnable localTask;
        assert (LockOrder.testAcquire(this));
        ThreadPool.DelayedTask delayedTask = this;
        synchronized (delayedTask) {
            localTask = this.task;
            if (this.period == 0L) {
                this.task = null;
            }
        }
        if (localTask != null) {
            localTask.run();
        }
        if (this.period > 0L) {
            delayedTask = this;
            synchronized (delayedTask) {
                if (this.task == null) {
                    return;
                }
                this.when = (this.fixedRate ? this.when : System.currentTimeMillis()) + this.period;
                ThreadPool.this.add(this);
            }
        }
    }

    public String toString() {
        return "DelayedTask[when=" + this.when + ", period=" + this.period + ", fixedRate=" + this.fixedRate + ", task=" + this.task + "]";
    }

    static /* synthetic */ long access$000(ThreadPool.DelayedTask x0) {
        return x0.when;
    }

    static /* synthetic */ boolean access$102(ThreadPool.DelayedTask x0, boolean x1) {
        x0.inQueue = x1;
        return x0.inQueue;
    }

    static /* synthetic */ int access$202(ThreadPool.DelayedTask x0, int x1) {
        x0.offset = x1;
        return x0.offset;
    }
}
