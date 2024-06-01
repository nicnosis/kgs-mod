/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client.gtp;

import com.gokgs.client.gtp.GtpClient;
import org.igoweb.igoweb.client.CRoom;

class GtpClient.2
extends GtpClient.RoomWatch {
    final /* synthetic */ CRoom val$room;

    GtpClient.2(CRoom cRoom) {
        this.val$room = cRoom;
        super(GtpClient.this, null);
    }

    @Override
    public void activate() {
        GtpClient.this.logger.warning("Couldn't join room " + this.val$room.getName());
        GtpClient.this.client.logout();
    }
}
