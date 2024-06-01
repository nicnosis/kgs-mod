/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client.gtp;

import com.gokgs.client.gtp.GtpClient;

class GtpClient.4
extends GtpClient.RoomWatch {
    GtpClient.4() {
        super(GtpClient.this, null);
    }

    @Override
    public void activate() {
        GtpClient.this.logger.severe("Client cannot find room to join. Logging out.");
        GtpClient.this.client.logout();
    }
}
