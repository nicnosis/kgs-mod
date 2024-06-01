/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.gtp;

import org.igoweb.go.gtp.Command;

class Protocol.3
extends Command {
    Protocol.3(String text) {
        super(text);
    }

    @Override
    public void responseReceived(String resp, boolean success) {
        if (success) {
            Protocol.this.engineName = resp;
        }
    }
}
