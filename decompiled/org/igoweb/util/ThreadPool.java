/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.util.LinkedList;
import org.igoweb.util.LockOrder;

public class ThreadPool {
    public static final LockOrder LOCK_ORDER = new LockOrder(DelayedTask.class);
    private static final LockOrder DQ_LOCK_ORDER = new LockOrder("threadPool-delayQueue");
    private static final LockOrder WQ_LOCK_ORDER = new LockOrder(ThreadPool.class);
    private LinkedList<Runnable> workQueue = new LinkedList();
    private DelayedTask[] delays = new DelayedTask[16];
    private Object delayQueueLock = new Object();
    private int numDelays = 0;
    private boolean closed = false;
    private int waitingThreads = 0;

    public ThreadPool(int numThreads) {
        assert (DQ_LOCK_ORDER.orderFor(this.delayQueueLock));
        Runnable launcher = this::waitForWork;
        if (numThreads < 1) {
            throw new IllegalArgumentException("Threads " + numThreads);
        }
        for (int i = 0; i < numThreads; ++i) {
            Thread thread = new Thread(launcher, "ThreadPool-" + i);
            thread.setDaemon(true);
            thread.start();
        }
        Thread thread = new Thread(this::manageDelayQueue, "ThreadPool-delayQueue");
        thread.setDaemon(true);
        thread.start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void shutdown() {
        assert (LockOrder.testAcquire(this.delayQueueLock));
        Object object = this.delayQueueLock;
        synchronized (object) {
            assert (LockOrder.testAcquire(this));
            ThreadPool threadPool = this;
            synchronized (threadPool) {
                this.closed = true;
                this.notifyAll();
                this.delayQueueLock.notifyAll();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void waitForWork() {
        try {
            while (true) {
                Runnable work;
                assert (LockOrder.testAcquire(this));
                ThreadPool threadPool = this;
                synchronized (threadPool) {
                    assert (LockOrder.testWait(this));
                    while (this.workQueue.isEmpty()) {
                        if (this.closed) {
                            return;
                        }
                        ++this.waitingThreads;
                        this.wait();
                        --this.waitingThreads;
                    }
                    work = this.workQueue.removeFirst();
                    if (!this.workQueue.isEmpty()) {
                        this.notify();
                    }
                }
                work.run();
            }
        }
        catch (InterruptedException excep) {
            throw new RuntimeException(excep);
        }
    }

    public final void execute(Runnable runnable) {
        this.execute(runnable, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void execute(Runnable runnable, boolean forced) {
        assert (LockOrder.testAcquire(this));
        ThreadPool threadPool = this;
        synchronized (threadPool) {
            if (!this.closed) {
                if (forced && this.waitingThreads <= this.workQueue.size()) {
                    Thread thread = new Thread(runnable, "ThreadPool-temp");
                    thread.setDaemon(true);
                    thread.start();
                } else {
                    this.workQueue.add(runnable);
                    this.notify();
                }
            }
        }
    }

    /*
     * Loose catch block
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void manageDelayQueue() {
        try {
            assert (LockOrder.testAcquire(this.delayQueueLock));
            Object object = this.delayQueueLock;
            synchronized (object) {
                while (true) {
                    assert (LockOrder.testWait(this.delayQueueLock));
                    while (this.numDelays == 0) {
                        if (this.closed) {
                            // MONITOREXIT @DISABLED, blocks:[0, 6, 9, 10, 14] lbl11 : MonitorExitStatement: MONITOREXIT : var1_1
                            this.shutdown();
                            return;
                        }
                        this.delayQueueLock.wait();
                    }
                    long now = System.currentTimeMillis();
                    if (now >= this.delays[1].when) {
                        this.execute(this.delays[1]);
                        this.removeDelay(1);
                        continue;
                    }
                    if (this.closed) {
                        this.delays = null;
                        // MONITOREXIT @DISABLED, blocks:[0, 6, 9, 12] lbl23 : MonitorExitStatement: MONITOREXIT : var1_1
                        this.shutdown();
                        return;
                    }
                    assert (LockOrder.testWait(this.delayQueueLock));
                    this.delayQueueLock.wait(this.delays[1].when - now);
                    continue;
                    break;
                }
                catch (InterruptedException excep) {
                    throw new RuntimeException(excep);
                }
            }
        }
        catch (Throwable throwable) {
            this.shutdown();
            throw throwable;
        }
    }

    private void removeDelay(int offset) {
        if (this.delays == null) {
            return;
        }
        this.delays[offset].inQueue = false;
        DelayedTask delay = this.delays[this.numDelays];
        this.delays[this.numDelays--] = null;
        if (offset <= this.numDelays) {
            if (offset > 1 && delay.when < this.delays[offset >> 1].when) {
                int newOffset;
                while (this.delays[newOffset = offset >> 1].when > delay.when) {
                    this.delays[offset] = this.delays[newOffset];
                    this.delays[offset].offset = offset;
                    offset = newOffset;
                    if (offset > 1) continue;
                }
                this.delays[offset] = delay;
                delay.offset = offset;
                return;
            }
            while (true) {
                if (offset * 2 <= this.numDelays && this.delays[offset * 2].when < delay.when) {
                    if (offset * 2 + 1 <= this.numDelays && this.delays[offset * 2 + 1].when < this.delays[offset * 2].when) {
                        this.delays[offset] = this.delays[offset * 2 + 1];
                        this.delays[offset].offset = offset;
                        offset = offset * 2 + 1;
                        continue;
                    }
                    this.delays[offset] = this.delays[offset * 2];
                    this.delays[offset].offset = offset;
                    offset *= 2;
                    continue;
                }
                if (offset * 2 + 1 > this.numDelays || this.delays[offset * 2 + 1].when >= delay.when) break;
                this.delays[offset] = this.delays[offset * 2 + 1];
                this.delays[offset].offset = offset;
                offset = offset * 2 + 1;
            }
            this.delays[offset] = delay;
            delay.offset = offset;
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void add(DelayedTask delay) {
        assert (LockOrder.testAcquire(this.delayQueueLock));
        Object object = this.delayQueueLock;
        synchronized (object) {
            int newOffset;
            if (this.closed) {
                return;
            }
            delay.inQueue = true;
            int offset = ++this.numDelays;
            if (offset >= this.delays.length) {
                DelayedTask[] newDelays = new DelayedTask[this.delays.length * 2];
                System.arraycopy(this.delays, 0, newDelays, 0, this.delays.length);
                this.delays = newDelays;
            }
            while (offset > 1 && this.delays[newOffset = offset >> 1].when > delay.when) {
                this.delays[offset] = this.delays[newOffset];
                this.delays[offset].offset = offset;
                offset = newOffset;
            }
            this.delays[offset] = delay;
            delay.offset = offset;
            if (offset == 1) {
                this.delayQueueLock.notify();
            }
        }
    }

    public DelayedTask schedule(Runnable runnable, long delay) {
        DelayedTask task = new DelayedTask(System.currentTimeMillis() + delay, 0L, runnable, true);
        this.add(task);
        return task;
    }

    public DelayedTask scheduleAt(Runnable runnable, long when) {
        DelayedTask task = new DelayedTask(when, 0L, runnable, true);
        this.add(task);
        return task;
    }

    public DelayedTask scheduleAtFixedRate(Runnable runnable, long initialDelay, long period) {
        DelayedTask task = new DelayedTask(System.currentTimeMillis() + initialDelay, period, runnable, true);
        this.add(task);
        return task;
    }

    public DelayedTask scheduleWithFixedDelay(Runnable runnable, long initialDelay, long period) {
        DelayedTask task = new DelayedTask(System.currentTimeMillis() + initialDelay, period, runnable, false);
        this.add(task);
        return task;
    }

    static {
        LOCK_ORDER.addInnerOrder(DQ_LOCK_ORDER);
        DQ_LOCK_ORDER.addInnerOrder(WQ_LOCK_ORDER);
    }

    public class DelayedTask
    implements Runnable {
        private long when;
        private final long period;
        private Runnable task;
        private boolean fixedRate;
        private int offset;
        private boolean inQueue;

        private DelayedTask(long newWhen, long newPeriod, Runnable newTask, boolean newFixedRate) {
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
            DelayedTask delayedTask = this;
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
            DelayedTask delayedTask = this;
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
    }
}
