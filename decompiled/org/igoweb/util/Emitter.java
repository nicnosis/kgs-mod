/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import org.igoweb.util.Event;
import org.igoweb.util.EventListener;
import org.igoweb.util.LockOrder;
import org.igoweb.util.Multicaster;

public class Emitter {
    private volatile transient EventListener listener = null;

    public void addListener(EventListener newListener) {
        this.listener = Multicaster.add(this.listener, newListener);
    }

    public void removeListener(EventListener victim) {
        this.listener = Multicaster.remove(this.listener, victim);
    }

    protected final void emit(int type) {
        this.emit(type, null);
    }

    protected void emit(int type, Object arg) {
        EventListener localListener = this.listener;
        if (localListener != null) {
            localListener.handleEvent(this.buildEvent(type, arg));
        }
    }

    protected void emit(Event event) {
        EventListener localListener = this.listener;
        if (localListener != null) {
            localListener.handleEvent(event);
        }
    }

    protected Event buildEvent(int type, Object arg) {
        return new Event(this, type, arg);
    }

    public boolean hasListener() {
        return this.listener != null;
    }

    protected EventListener getListener() {
        return this.listener;
    }

    public static class ThreadSafe
    extends Emitter {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void addListener(EventListener newListener) {
            assert (LockOrder.testAcquire(this));
            ThreadSafe threadSafe = this;
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
            ThreadSafe threadSafe = this;
            synchronized (threadSafe) {
                super.removeListener(victim);
            }
        }
    }
}
