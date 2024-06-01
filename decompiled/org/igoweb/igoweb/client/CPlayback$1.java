/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import org.igoweb.igoweb.shared.MsgTypesDown;

static class CPlayback.1 {
    static final /* synthetic */ int[] $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown;

    static {
        $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown = new int[MsgTypesDown.values().length];
        try {
            CPlayback.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.PLAYBACK_DATA.ordinal()] = 1;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            CPlayback.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.PLAYBACK_SEEK_START.ordinal()] = 2;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            CPlayback.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.PLAYBACK_SEEK_COMPLETE.ordinal()] = 3;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
    }
}
