/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.lang.ref.WeakReference;

private static class LockOrder.WeakHashRef
extends WeakReference<Object> {
    private final int objectHashCode;

    public LockOrder.WeakHashRef(Object o, boolean inRefQ) {
        super(o, inRefQ ? refQ : null);
        this.objectHashCode = System.identityHashCode(o);
    }

    public int hashCode() {
        return this.objectHashCode;
    }

    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        LockOrder.WeakHashRef peer = (LockOrder.WeakHashRef)o;
        return this.objectHashCode == peer.objectHashCode && this.get() == peer.get();
    }
}
