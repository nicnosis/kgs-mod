/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import org.igoweb.igoweb.shared.MsgTypesDown;

static class CGameListEntry.1 {
    static final /* synthetic */ int[] $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown;

    static {
        $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown = new int[MsgTypesDown.values().length];
        try {
            CGameListEntry.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.GAME_STATE.ordinal()] = 1;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            CGameListEntry.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.GAMELISTENTRY_PLAYER_REPLACED.ordinal()] = 2;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            CGameListEntry.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.GAME_NAME_CHANGE.ordinal()] = 3;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
    }
}
