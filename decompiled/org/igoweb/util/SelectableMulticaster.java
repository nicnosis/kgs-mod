/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.util.LinkedList;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;
import org.igoweb.util.IntHashMap;
import org.igoweb.util.Multicaster;
import org.igoweb.util.ThreadPool;

public class SelectableMulticaster
extends IntHashMap<EventListener>
implements EventListener,
Runnable {
    private EventListener allListener;
    private EventListener deferredAllListener;
    private ThreadPool executor;
    private LinkedList<DeferBundle> queuedDeferredEvents;

    public SelectableMulticaster() {
    }

    public SelectableMulticaster(ThreadPool threadPool) {
        this.executor = threadPool;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void handleEvent(Event event) {
        EventListener localSpecificListener;
        EventListener localAllListener;
        SelectableMulticaster selectableMulticaster = this;
        synchronized (selectableMulticaster) {
            localAllListener = this.allListener;
            localSpecificListener = (EventListener)this.get(event.type * 2);
            EventListener deferredSpecific = (EventListener)this.get(event.type * 2 + 1);
            if (deferredSpecific != null || this.deferredAllListener != null) {
                if (this.queuedDeferredEvents == null) {
                    this.queuedDeferredEvents = new LinkedList();
                    this.executor.execute(this);
                }
                this.queuedDeferredEvents.add(new DeferBundle(event, deferredSpecific, this.deferredAllListener));
            }
        }
        if (localAllListener != null) {
            localAllListener.handleEvent(event);
        }
        if (localSpecificListener != null) {
            localSpecificListener.handleEvent(event);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        while (true) {
            DeferBundle db;
            SelectableMulticaster selectableMulticaster = this;
            synchronized (selectableMulticaster) {
                if (this.queuedDeferredEvents.isEmpty()) {
                    this.queuedDeferredEvents = null;
                    return;
                }
                db = this.queuedDeferredEvents.removeFirst();
            }
            db.emitEvent();
        }
    }

    public static EventListener add(EventListener oldListener, EventListener newListener) {
        if (oldListener == null) {
            return newListener;
        }
        if (oldListener instanceof SelectableMulticaster) {
            ((SelectableMulticaster)oldListener).add(newListener);
            return oldListener;
        }
        return Multicaster.add(oldListener, newListener);
    }

    public synchronized void add(EventListener newListener) {
        this.allListener = Multicaster.add(this.allListener, newListener);
    }

    public static EventListener addDeferred(EventListener oldListener, EventListener newListener, ThreadPool exec) {
        if (oldListener == null) {
            SelectableMulticaster selMulti = new SelectableMulticaster(exec);
            selMulti.deferredAllListener = newListener;
            return selMulti;
        }
        if (oldListener instanceof SelectableMulticaster) {
            SelectableMulticaster selMulti = (SelectableMulticaster)oldListener;
            selMulti.executor = exec;
            selMulti.addDeferred(newListener);
            return selMulti;
        }
        SelectableMulticaster selMulti = new SelectableMulticaster(exec);
        selMulti.allListener = oldListener;
        selMulti.deferredAllListener = newListener;
        return selMulti;
    }

    public synchronized void addDeferred(EventListener newListener) {
        this.deferredAllListener = Multicaster.add(this.deferredAllListener, newListener);
    }

    public static EventListener add(int id, EventListener oldListener, EventListener newListener) {
        if (oldListener == null || !(oldListener instanceof SelectableMulticaster)) {
            SelectableMulticaster selMulti = new SelectableMulticaster();
            selMulti.allListener = oldListener;
            selMulti.put(id *= 2, newListener);
            return selMulti;
        }
        ((SelectableMulticaster)oldListener).add(id, newListener);
        return oldListener;
    }

    public synchronized void add(int id, EventListener newListener) {
        EventListener oldListener = (EventListener)this.get(id *= 2);
        newListener = Multicaster.add(oldListener, newListener);
        if (newListener != oldListener) {
            this.put(id, newListener);
        }
    }

    public static EventListener addDeferred(int id, EventListener oldListener, EventListener newListener, ThreadPool exec) {
        if (oldListener == null || !(oldListener instanceof SelectableMulticaster)) {
            id = id * 2 + 1;
            SelectableMulticaster selMulti = new SelectableMulticaster(exec);
            selMulti.allListener = oldListener;
            selMulti.put(id, newListener);
            return selMulti;
        }
        ((SelectableMulticaster)oldListener).addDeferred(id, newListener);
        return oldListener;
    }

    public synchronized void addDeferred(int id, EventListener newListener) {
        EventListener oldListener = (EventListener)this.get(id = id * 2 + 1);
        newListener = Multicaster.add(oldListener, newListener);
        if (newListener != oldListener) {
            this.put(id, newListener);
        }
    }

    public static EventListener remove(EventListener oldListener, EventListener victim) {
        if (oldListener == null || oldListener == victim) {
            return null;
        }
        if (oldListener instanceof SelectableMulticaster) {
            ((SelectableMulticaster)oldListener).remove(victim);
            return oldListener;
        }
        return Multicaster.remove(oldListener, victim);
    }

    public synchronized void remove(EventListener victim) {
        this.allListener = Multicaster.remove(this.allListener, victim);
    }

    public static EventListener removeDeferred(EventListener oldListener, EventListener victim) {
        if (oldListener == null || !(oldListener instanceof SelectableMulticaster)) {
            return oldListener;
        }
        return ((SelectableMulticaster)oldListener).removeDeferred(victim);
    }

    public synchronized EventListener removeDeferred(EventListener victim) {
        this.deferredAllListener = Multicaster.remove(this.deferredAllListener, victim);
        return this.deferredAllListener == null && this.isEmpty() ? this.allListener : this;
    }

    public static EventListener remove(int id, EventListener oldListener, EventListener victim) {
        return SelectableMulticaster.doRemove(id * 2, oldListener, victim);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static EventListener doRemove(int id, EventListener oldListener, EventListener victim) {
        SelectableMulticaster selMulti;
        if (oldListener == null || !(oldListener instanceof SelectableMulticaster)) {
            return oldListener;
        }
        SelectableMulticaster selectableMulticaster = selMulti = (SelectableMulticaster)oldListener;
        synchronized (selectableMulticaster) {
            oldListener = (EventListener)selMulti.get(id);
            EventListener replacement = Multicaster.remove(oldListener, victim);
            if (oldListener != replacement) {
                if (replacement == null) {
                    selMulti.remove(id);
                    if (selMulti.isEmpty() && selMulti.deferredAllListener == null) {
                        return selMulti.allListener;
                    }
                } else {
                    selMulti.put(id, replacement);
                }
            }
        }
        return selMulti;
    }

    public synchronized void remove(int id, EventListener victim) {
        EventListener oldListener = (EventListener)this.get(id *= 2);
        if (oldListener != (victim = Multicaster.remove(oldListener, victim))) {
            if (victim == null) {
                this.remove(id);
            } else {
                this.put(id, victim);
            }
        }
    }

    public synchronized void removeDeferred(int id, EventListener victim) {
        EventListener oldListener = (EventListener)this.get(id = id * 2 + 1);
        if (oldListener != (victim = Multicaster.remove(oldListener, victim))) {
            if (victim == null) {
                this.remove(id);
            } else {
                this.put(id, victim);
            }
        }
    }

    public static EventListener removeDeferred(int id, EventListener oldListener, EventListener victim) {
        return SelectableMulticaster.doRemove(id * 2 + 1, oldListener, victim);
    }

    private static class DeferBundle {
        public final Event event;
        public final EventListener[] globalListeners;
        public final EventListener gsListener;
        public final EventListener[] perTypeListeners;
        public final EventListener ptsListener;

        public DeferBundle(Event event, EventListener globalListener, EventListener perTypeListener) {
            this.event = event;
            this.globalListeners = Multicaster.getListeners(globalListener);
            this.gsListener = this.globalListeners == null ? globalListener : null;
            this.perTypeListeners = Multicaster.getListeners(perTypeListener);
            this.ptsListener = this.perTypeListeners == null ? perTypeListener : null;
        }

        public void emitEvent() {
            int i;
            if (this.globalListeners != null) {
                for (i = this.globalListeners.length - 1; i >= 0; --i) {
                    this.globalListeners[i].handleEvent(this.event);
                }
            }
            if (this.gsListener != null) {
                this.gsListener.handleEvent(this.event);
            }
            if (this.perTypeListeners != null) {
                for (i = this.perTypeListeners.length - 1; i >= 0; --i) {
                    this.perTypeListeners[i].handleEvent(this.event);
                }
            }
            if (this.ptsListener != null) {
                this.ptsListener.handleEvent(this.event);
            }
        }
    }
}
