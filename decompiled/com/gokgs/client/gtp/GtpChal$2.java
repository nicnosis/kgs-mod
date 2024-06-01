/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client.gtp;

import com.gokgs.client.KCProposal;
import org.igoweb.go.gtp.Command;

class GtpChal.2
extends Command {
    final /* synthetic */ KCProposal val$prop;

    GtpChal.2(String text, KCProposal kCProposal) {
        this.val$prop = kCProposal;
        super(text);
    }

    @Override
    public void responseReceived(String resp, boolean timeSuccess) {
        GtpChal.this.receivedTimeResponse(timeSuccess, this.val$prop);
    }
}
