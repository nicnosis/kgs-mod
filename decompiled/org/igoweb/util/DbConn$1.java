/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import org.igoweb.util.DbConn;

static class DbConn.1 {
    static final /* synthetic */ int[] $SwitchMap$org$igoweb$util$DbConn$State;

    static {
        $SwitchMap$org$igoweb$util$DbConn$State = new int[DbConn.State.values().length];
        try {
            DbConn.1.$SwitchMap$org$igoweb$util$DbConn$State[DbConn.State.DYING.ordinal()] = 1;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            DbConn.1.$SwitchMap$org$igoweb$util$DbConn$State[DbConn.State.READY.ordinal()] = 2;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
    }
}
