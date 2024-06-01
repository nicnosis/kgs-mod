/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.games;

import java.util.Collection;
import java.util.Collections;
import org.igoweb.games.Clock;

public class NullClock
extends Clock {
    @Override
    protected void doUpdate(long now, boolean stopping) {
    }

    @Override
    public void setMs(long time) {
    }

    @Override
    public void addMs(long ms) {
    }

    @Override
    public String getCurrentReadout(long time) {
        return "-";
    }

    @Override
    public boolean isExpired(long now) {
        return false;
    }

    @Override
    public long getTotalMsLeft(long now) {
        return Long.MAX_VALUE;
    }

    @Override
    public void reset() {
    }

    @Override
    public Collection<String> getSampleReadouts() {
        return Collections.singleton("-");
    }

    @Override
    public Object getState() {
        return this;
    }

    @Override
    public void setState(Object state) {
        if (state != this) {
            throw new IllegalArgumentException(state.toString());
        }
    }
}
