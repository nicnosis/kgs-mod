/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.games;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import org.igoweb.util.Emitter;

public abstract class Clock
extends Emitter {
    public static final int STARTED_CHANGED_EVENT = 0;
    public static final int PAUSED_CHANGED_EVENT = 1;
    public static final int SET_EVENT = 2;
    public static final int LOADED_EVENT = 3;
    protected static final int STARTED_BIT = 1;
    protected static final int PAUSED_BIT = 2;
    private long startTime;
    private boolean started = false;
    private boolean paused = false;
    protected long timeLeft;

    public final boolean setStarted(boolean val) {
        return this.setStarted(val, 0L);
    }

    public boolean setStarted(boolean val, long now) {
        if (val == this.started) {
            return false;
        }
        if (val) {
            this.startTime = now == 0L ? System.currentTimeMillis() : now;
        } else {
            this.update(now, true);
        }
        this.started = val;
        this.emit(0);
        return true;
    }

    public final boolean isStarted() {
        return this.started;
    }

    protected final void update(boolean stopping) {
        this.update(0L, stopping);
    }

    protected final void update(long now, boolean stopping) {
        this.doUpdate(now == 0L ? System.currentTimeMillis() : now, stopping);
    }

    public long getElapsedTime(long now) {
        return now - this.startTime;
    }

    protected void doUpdate(long now, boolean stopping) {
        if (!this.paused && this.started) {
            this.timeLeft -= now - this.startTime;
            this.startTime = now;
            if (this.timeLeft < 0L) {
                this.timeLeft = 0L;
            }
        }
    }

    public final void setSecs(double time) {
        this.setMs((long)(time * 1000.0));
    }

    public void setMs(long time) {
        this.setMs(time, false);
    }

    protected void setMs(long time, boolean forceChange) {
        if (forceChange || this.timeLeft != time) {
            if (time < 0L) {
                time = 0L;
            }
            this.timeLeft = time;
            this.emit(2);
        }
    }

    public void addMs(long ms) {
        this.update(false);
        this.timeLeft += ms;
    }

    public final String getCurrentReadout() {
        return this.getCurrentReadout(0L);
    }

    public abstract String getCurrentReadout(long var1);

    public boolean isExpired(long now) {
        if (this.timeLeft == 0L) {
            return true;
        }
        this.update(now, false);
        return this.timeLeft == 0L;
    }

    public final boolean isPaused() {
        return this.paused;
    }

    public boolean setPaused(boolean newPauseValue) {
        if (newPauseValue == this.paused) {
            return false;
        }
        if (this.started) {
            if (newPauseValue) {
                this.update(false);
            } else {
                this.startTime = System.currentTimeMillis();
            }
        }
        this.paused = newPauseValue;
        this.emit(1);
        return true;
    }

    public final long getPeriodMsLeft() {
        return this.getPeriodMsLeft(0L);
    }

    public long getPeriodMsLeft(long now) {
        return this.getTotalMsLeft(now);
    }

    public long getTotalMsLeft(long now) {
        if (now >= 0L) {
            this.update(now, false);
        }
        return this.timeLeft;
    }

    public boolean isFinalPeriod() {
        return true;
    }

    public abstract void reset();

    public abstract Collection<String> getSampleReadouts();

    public void writeState(DataOutput out) throws IOException {
        this.update(false);
        int flags = 0;
        if (this.paused) {
            flags |= 2;
        }
        if (this.started) {
            flags |= 1;
        }
        out.writeByte(flags);
        out.writeInt((int)this.timeLeft);
    }

    public void readState(DataInput in) throws IOException {
        byte flags = in.readByte();
        this.paused = (flags & 2) != 0;
        this.started = (flags & 1) != 0;
        this.timeLeft = in.readInt();
        if (this.timeLeft < 0L) {
            throw new IOException("timeLeft of " + this.timeLeft + " is invalid.");
        }
        this.startTime = System.currentTimeMillis();
        this.emit(3);
    }

    public abstract Object getState();

    public abstract void setState(Object var1);

    public String toString() {
        String klass = this.getClass().getName();
        return klass.substring(klass.lastIndexOf(46) + 1) + (this.started ? "[started," : "[") + (this.paused ? "paused," : "") + "timeLeft=" + (double)this.timeLeft / 1000.0 + "]";
    }
}
