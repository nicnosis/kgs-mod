/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import org.igoweb.igoweb.shared.MsgTypesDown;

static class CGameContainer.1 {
    static final /* synthetic */ int[] $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown;

    static {
        $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown = new int[MsgTypesDown.values().length];
        try {
            CGameContainer.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.GAME_LIST.ordinal()] = 1;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            CGameContainer.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.GAME_CONTAINER_REMOVE_GAME.ordinal()] = 2;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
    }
}
