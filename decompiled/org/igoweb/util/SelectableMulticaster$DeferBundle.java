/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import org.igoweb.util.Event;
import org.igoweb.util.EventListener;
import org.igoweb.util.Multicaster;

private static class SelectableMulticaster.DeferBundle {
    public final Event event;
    public final EventListener[] globalListeners;
    public final EventListener gsListener;
    public final EventListener[] perTypeListeners;
    public final EventListener ptsListener;

    public SelectableMulticaster.DeferBundle(Event event, EventListener globalListener, EventListener perTypeListener) {
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
