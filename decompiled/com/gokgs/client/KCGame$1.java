/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client;

import org.igoweb.igoweb.shared.MsgTypesDown;

static class KCGame.1 {
    static final /* synthetic */ int[] $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown;

    static {
        $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown = new int[MsgTypesDown.values().length];
        try {
            KCGame.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.GAME_LOOP_WARNING.ordinal()] = 1;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            KCGame.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.GAME_OVER.ordinal()] = 2;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            KCGame.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.GAME_EDITOR_LEFT.ordinal()] = 3;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
    }
}
