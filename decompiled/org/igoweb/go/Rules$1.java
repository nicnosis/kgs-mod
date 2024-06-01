/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import org.igoweb.games.AbsoluteClock;
import org.igoweb.util.Defs;

class Rules.1
extends AbsoluteClock {
    Rules.1(long limit) {
        super(limit);
    }

    @Override
    protected String format(long time) {
        int intTime = (int)(time / 1000L);
        return Defs.getString(-1337055797, new Object[]{intTime / 60, intTime % 60, 0});
    }
}
