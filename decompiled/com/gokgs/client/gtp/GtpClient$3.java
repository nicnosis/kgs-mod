/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client.gtp;

import com.gokgs.client.gtp.GtpClient;

class GtpClient.3
extends GtpClient.RoomWatch {
    GtpClient.3() {
        super(GtpClient.this, null);
    }

    @Override
    public void activate() {
        GtpClient.this.allRoomsSeen();
    }
}
