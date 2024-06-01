/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import org.igoweb.igoweb.shared.PUser;

static class PUser.1 {
    static final /* synthetic */ int[] $SwitchMap$org$igoweb$igoweb$shared$PUser$State;

    static {
        $SwitchMap$org$igoweb$igoweb$shared$PUser$State = new int[PUser.State.values().length];
        try {
            PUser.1.$SwitchMap$org$igoweb$igoweb$shared$PUser$State[PUser.State.GUEST.ordinal()] = 1;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            PUser.1.$SwitchMap$org$igoweb$igoweb$shared$PUser$State[PUser.State.PENDING.ordinal()] = 2;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            PUser.1.$SwitchMap$org$igoweb$igoweb$shared$PUser$State[PUser.State.ACTIVE.ordinal()] = 3;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
    }
}
