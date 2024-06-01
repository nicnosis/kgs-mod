/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.util.LinkedList;
import org.igoweb.igoweb.client.ConnMutex;
import org.igoweb.util.LockOrder;

public class LockConnMutex
implements ConnMutex {
    private final LinkedList<Runnable> work = new LinkedList();
    public static final LockOrder LOCK_ORDER = new LockOrder(LockConnMutex.class);
    private static final LockOrder WORK_LOCK_ORDER = new LockOrder("LockConnMutex.work");
    private boolean closed;
    private boolean started;
    public final Object lock;

    public LockConnMutex() {
        this.lock = this;
        assert (WORK_LOCK_ORDER.orderFor(this.work));
    }

    public LockConnMutex(Object newLock) {
        this.lock = newLock;
        assert (WORK_LOCK_ORDER.orderFor(this.work));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void go() {
        assert (LockOrder.testAcquire(this.work));
        LinkedList<Runnable> linkedList = this.work;
        synchronized (linkedList) {
            if (this.closed || this.started) {
                throw new IllegalStateException("closed=" + this.closed + ", started=" + this.started);
            }
            this.started = true;
            new Thread(this::dispatcher, "igoweb client LockConnMutex dispatcher").start();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void runSynchronized(Runnable runnable) {
        assert (LockOrder.testAcquire(this.work));
        LinkedList<Runnable> linkedList = this.work;
        synchronized (linkedList) {
            this.work.add(runnable);
            this.work.notify();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    private void dispatcher() {
        try {
            while (true) {
                assert (LockOrder.testAcquire(this.work));
                Object object = this.work;
                // MONITORENTER : object
                while (this.work.isEmpty()) {
                    if (this.closed) {
                        // MONITOREXIT : object
                        return;
                    }
                    assert (LockOrder.testWait(this.work));
                    this.work.wait();
                }
                Runnable runnable = this.work.removeFirst();
                // MONITOREXIT : object
                assert (LockOrder.testAcquire(this.lock));
                object = this.lock;
                // MONITORENTER : object
                runnable.run();
                // MONITOREXIT : object
            }
        }
        catch (InterruptedException excep) {
            throw new RuntimeException();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() {
        assert (LockOrder.testAcquire(this.work));
        LinkedList<Runnable> linkedList = this.work;
        synchronized (linkedList) {
            this.closed = true;
            this.work.notify();
        }
    }

    static {
        LOCK_ORDER.addInnerOrder(WORK_LOCK_ORDER);
    }
}
