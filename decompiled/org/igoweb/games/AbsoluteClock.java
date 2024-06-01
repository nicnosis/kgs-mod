/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.games;

import java.util.Collection;
import java.util.Collections;
import org.igoweb.games.Clock;
import org.igoweb.util.Defs;

public class AbsoluteClock
extends Clock {
    private final long limit;

    public AbsoluteClock(long limit) {
        this.limit = limit;
        this.reset();
    }

    @Override
    public String getCurrentReadout(long time) {
        return this.format(this.getTotalMsLeft(time));
    }

    protected String format(long time) {
        int intTime = (int)(time / 1000L);
        return Defs.getString(-900134593, new Object[]{new Integer(intTime / 60), new Integer(intTime % 60)});
    }

    @Override
    public void reset() {
        this.setMs(this.limit);
    }

    @Override
    public Collection<String> getSampleReadouts() {
        return Collections.singleton(this.format(this.limit));
    }

    @Override
    public Object getState() {
        return new Long(this.getPeriodMsLeft());
    }

    @Override
    public void setState(Object obj) {
        this.update(false);
        this.setMs((Long)obj);
    }
}
