/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import org.igoweb.igoweb.shared.MsgTypesDown;

static class CArchive.1 {
    static final /* synthetic */ int[] $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown;

    static {
        $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown = new int[MsgTypesDown.values().length];
        try {
            CArchive.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.ARCHIVE_GAMES_CHANGED.ordinal()] = 1;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            CArchive.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.ARCHIVE_GAME_REMOVED.ordinal()] = 2;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
    }
}
