/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import org.igoweb.util.Event;
import org.igoweb.util.EventListener;
import org.igoweb.util.LockOrder;
import org.igoweb.util.SelectableMulticaster;

public class SelectableEmitter {
    protected volatile transient EventListener listener = null;

    public void addListener(EventListener newListener) {
        this.listener = SelectableMulticaster.add(this.listener, newListener);
    }

    public void addListener(int eventType, EventListener newListener) {
        this.listener = SelectableMulticaster.add(eventType, this.listener, newListener);
    }

    public void removeListener(EventListener victim) {
        this.listener = SelectableMulticaster.remove(this.listener, victim);
    }

    public void removeListener(int eventType, EventListener victim) {
        this.listener = SelectableMulticaster.remove(eventType, this.listener, victim);
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

    protected Event buildEvent(int type, Object arg) {
        return new Event(this, type, arg);
    }

    protected void emit(Event event) {
        EventListener localListener = this.listener;
        if (localListener != null) {
            this.listener.handleEvent(event);
        }
    }

    public boolean hasListener() {
        return this.listener != null;
    }

    public static class ThreadSafe
    extends SelectableEmitter {
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
        public void addListener(int type, EventListener newListener) {
            assert (LockOrder.testAcquire(this));
            ThreadSafe threadSafe = this;
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
            ThreadSafe threadSafe = this;
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
            ThreadSafe threadSafe = this;
            synchronized (threadSafe) {
                super.removeListener(type, victim);
            }
        }
    }
}
