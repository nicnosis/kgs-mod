/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client.gtp;

import org.igoweb.go.gtp.Command;

class GtpConvo.1
extends Command {
    GtpConvo.1(String text) {
        super(text);
    }

    @Override
    public void responseReceived(String resp, boolean success) {
        GtpConvo.this.handleChatResponse(resp, success);
    }
}
