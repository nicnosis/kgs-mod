/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import org.igoweb.util.Emitter;
import org.igoweb.util.EventListener;
import org.igoweb.util.LockOrder;

public static class Emitter.ThreadSafe
extends Emitter {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addListener(EventListener newListener) {
        assert (LockOrder.testAcquire(this));
        Emitter.ThreadSafe threadSafe = this;
        synchronized (threadSafe) {
            super.addListener(newListener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void removeListener(EventListener victim) {
        assert (LockOrder.testAcquire(this));
        Emitter.ThreadSafe threadSafe = this;
        synchronized (threadSafe) {
            super.removeListener(victim);
        }
    }
}
