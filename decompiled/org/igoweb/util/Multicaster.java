/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import org.igoweb.util.Event;
import org.igoweb.util.EventListener;
import org.igoweb.util.LockOrder;

public class Multicaster
implements EventListener {
    public static final LockOrder LOCK_ORDER = new LockOrder(Multicaster.class);
    private static final int MINIMUM_SIZE = 5;
    private EventListener[] listeners = new EventListener[5];
    private static final EventListener[] EMPTY;
    private int size;
    private static final Class<?> klass;

    private Multicaster() {
    }

    @Override
    public void handleEvent(Event event) {
        EventListener[] localListeners = this.getListeners();
        for (int i = localListeners.length - 1; i >= 0; --i) {
            localListeners[i].handleEvent(event);
        }
    }

    static EventListener[] getListeners(EventListener el) {
        return el != null && el.getClass() == klass ? ((Multicaster)el).getListeners() : null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected EventListener[] getListeners() {
        assert (LockOrder.testAcquire(this));
        Multicaster multicaster = this;
        synchronized (multicaster) {
            if (this.size == 0) {
                return EMPTY;
            }
            EventListener[] result = new EventListener[this.size];
            System.arraycopy(this.listeners, 0, result, 0, this.size);
            return result;
        }
    }

    private void realloc(int newSize) {
        assert (Thread.holdsLock(this));
        if (newSize < 5) {
            newSize = 5;
        }
        EventListener[] newListeners = new EventListener[newSize];
        System.arraycopy(this.listeners, 0, newListeners, 0, this.size);
        this.listeners = newListeners;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void add(EventListener el) {
        if (el == null) {
            throw new IllegalArgumentException();
        }
        assert (LockOrder.testAcquire(this));
        Multicaster multicaster = this;
        synchronized (multicaster) {
            if (this.size == this.listeners.length) {
                this.realloc(this.size * 2 - 1);
            }
            this.listeners[this.size++] = el;
        }
    }

    public void remove(EventListener el) {
        this.remove(el, false);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private EventListener remove(EventListener el, boolean dropOnSingleton) {
        if (el == null) {
            throw new IllegalArgumentException();
        }
        assert (LockOrder.testAcquire(this));
        Multicaster multicaster = this;
        synchronized (multicaster) {
            for (int i = this.size - 1; i >= 0; --i) {
                if (this.listeners[i] != el) continue;
                this.listeners[i] = this.listeners[--this.size];
                this.listeners[this.size] = null;
                break;
            }
            if (this.size == 1 && dropOnSingleton) {
                return this.listeners[0];
            }
            if (this.size * 3 + 5 < this.listeners.length) {
                this.realloc(this.size * 2);
            }
            return this;
        }
    }

    public static EventListener add(EventListener oldListener, EventListener newListener) {
        if (newListener == null) {
            throw new IllegalArgumentException();
        }
        if (oldListener == null) {
            return newListener;
        }
        if (oldListener.getClass() == klass) {
            ((Multicaster)oldListener).add(newListener);
            return oldListener;
        }
        Multicaster mcast = new Multicaster();
        mcast.add(oldListener);
        mcast.add(newListener);
        return mcast;
    }

    public final boolean isEmpty() {
        return this.size == 0;
    }

    public static EventListener remove(EventListener oldListener, EventListener victim) {
        if (victim == null) {
            throw new IllegalArgumentException();
        }
        if (oldListener == null || oldListener == victim) {
            return null;
        }
        return oldListener.getClass() == klass ? ((Multicaster)oldListener).remove(victim, true) : oldListener;
    }

    static {
        LOCK_ORDER.orderFor(External.class);
        EMPTY = new EventListener[0];
        Multicaster m = new Multicaster();
        klass = m.getClass();
    }

    public static class External
    extends Multicaster {
    }
}
