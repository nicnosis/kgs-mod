/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import org.igoweb.igoweb.shared.MsgTypesDown;

static class CRoomCatChannel.1 {
    static final /* synthetic */ int[] $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown;

    static {
        $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown = new int[MsgTypesDown.values().length];
        try {
            CRoomCatChannel.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.ROOM_CAT_COUNTERS.ordinal()] = 1;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            CRoomCatChannel.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.ROOM_CAT_ROOM_GONE.ordinal()] = 2;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
    }
}
