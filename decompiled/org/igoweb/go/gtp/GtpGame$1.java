/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.gtp;

import org.igoweb.go.gtp.Command;
import org.igoweb.go.gtp.GtpException;

class GtpGame.1
extends Command {
    GtpGame.1(String text) {
        super(text);
    }

    @Override
    public void responseReceived(String resp, boolean success) throws GtpException {
        GtpGame.this.undoResponseReceived(success);
    }
}
