/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

public class Event {
    public final Object source;
    public final int type;
    public final Object arg;
    private boolean consumed;

    public Event(Object source, int type) {
        this(source, type, null);
    }

    public Event(Object source, int type, Object arg) {
        this.source = source;
        this.type = type;
        this.arg = arg;
    }

    public final void consume() {
        this.consumed = true;
    }

    public final boolean isConsumed() {
        return this.consumed;
    }

    public int hashCode() {
        int result = this.type;
        if (this.source != null) {
            result = result * -1640524983 + this.source.hashCode();
        }
        if (this.arg != null) {
            result = result * -1640524983 + this.arg.hashCode();
        }
        return result;
    }

    public boolean equals(Object obj) {
        if (obj != null && obj.getClass().equals(this.getClass())) {
            Event peer = (Event)obj;
            return this.type == peer.type && this.source.equals(peer.source) && (this.arg == null ? peer.arg == null : this.arg.equals(peer.arg));
        }
        return false;
    }

    public String toString() {
        String cname = this.getClass().getName();
        return cname.substring(cname.lastIndexOf(46) + 1) + "[type=" + this.type + ", src=" + this.source + ", arg=" + this.arg + ']';
    }
}
