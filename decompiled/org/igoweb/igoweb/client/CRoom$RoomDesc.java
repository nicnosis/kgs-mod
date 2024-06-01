/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import org.igoweb.igoweb.client.CChannel;

public static class CRoom.RoomDesc
extends CChannel.Chat {
    public CRoom.RoomDesc(String newText) {
        super(null, newText, false, false);
    }

    @Override
    public boolean isFromPeer() {
        return false;
    }
}
