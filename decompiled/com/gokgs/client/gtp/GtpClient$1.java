/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client.gtp;

import com.gokgs.client.gtp.GtpClient;

class GtpClient.1
extends GtpClient.RoomWatch {
    GtpClient.1() {
        super(GtpClient.this, null);
    }

    @Override
    public void activate() {
        GtpClient.this.lookForRoom();
    }

    @Override
    public void deactivate() {
        GtpClient.this.allRooms = null;
    }
}
