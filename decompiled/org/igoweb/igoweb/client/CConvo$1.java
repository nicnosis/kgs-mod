/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import org.igoweb.igoweb.shared.MsgTypesDown;

static class CConvo.1 {
    static final /* synthetic */ int[] $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown;

    static {
        $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown = new int[MsgTypesDown.values().length];
        try {
            CConvo.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.CONVO_NO_CHATS.ordinal()] = 1;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            CConvo.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.CHAT.ordinal()] = 2;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
    }
}
