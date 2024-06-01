/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import org.igoweb.igoweb.shared.MsgTypesDown;

static class CRoom.1 {
    static final /* synthetic */ int[] $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown;

    static {
        $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown = new int[MsgTypesDown.values().length];
        try {
            CRoom.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.ROOM_DESC.ordinal()] = 1;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            CRoom.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.ROOM_NAME_FLUSH.ordinal()] = 2;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
    }
}
