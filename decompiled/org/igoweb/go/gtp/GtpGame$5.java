/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.gtp;

import org.igoweb.go.gtp.Command;
import org.igoweb.go.gtp.GtpException;

class GtpGame.5
extends Command {
    GtpGame.5(String text) {
        super(text);
    }

    @Override
    public void responseReceived(String resp, boolean success) throws GtpException {
        GtpGame.this.moveReceived(resp, success);
    }
}
