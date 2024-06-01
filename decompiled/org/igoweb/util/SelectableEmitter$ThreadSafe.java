/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import org.igoweb.util.EventListener;
import org.igoweb.util.LockOrder;
import org.igoweb.util.SelectableEmitter;

public static class SelectableEmitter.ThreadSafe
extends SelectableEmitter {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addListener(EventListener newListener) {
        assert (LockOrder.testAcquire(this));
        SelectableEmitter.ThreadSafe threadSafe = this;
        synchronized (threadSafe) {
            super.addListener(newListener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addListener(int type, EventListener newListener) {
        assert (LockOrder.testAcquire(this));
        SelectableEmitter.ThreadSafe threadSafe = this;
        synchronized (threadSafe) {
            super.addListener(type, newListener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeListener(EventListener victim) {
        assert (LockOrder.testAcquire(this));
        SelectableEmitter.ThreadSafe threadSafe = this;
        synchronized (threadSafe) {
            super.removeListener(victim);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeListener(int type, EventListener victim) {
        assert (LockOrder.testAcquire(this));
        SelectableEmitter.ThreadSafe threadSafe = this;
        synchronized (threadSafe) {
            super.removeListener(type, victim);
        }
    }
}
