/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import org.igoweb.igoweb.shared.MsgTypesDown;

static class CDetailsChannel.1 {
    static final /* synthetic */ int[] $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown;

    static {
        $SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown = new int[MsgTypesDown.values().length];
        try {
            CDetailsChannel.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.DETAILS_UPDATE.ordinal()] = 1;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
        try {
            CDetailsChannel.1.$SwitchMap$org$igoweb$igoweb$shared$MsgTypesDown[MsgTypesDown.DETAILS_RANK_GRAPH.ordinal()] = 2;
        }
        catch (NoSuchFieldError noSuchFieldError) {
            // empty catch block
        }
    }
}
