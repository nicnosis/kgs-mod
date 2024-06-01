/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client.gtp;

private abstract class GtpClient.RoomWatch
implements Runnable {
    private GtpClient.RoomWatch() {
    }

    @Override
    public void run() {
        if (GtpClient.this.roomWatch == this) {
            GtpClient.this.roomWatch = null;
            if (GtpClient.this.subscribedRoom == null) {
                this.activate();
                return;
            }
        }
        this.deactivate();
    }

    public abstract void activate();

    public void deactivate() {
    }
}
