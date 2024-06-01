/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.gtp;

import org.igoweb.go.gtp.Command;

class Protocol.2
extends Command {
    Protocol.2(String text) {
        super(text);
    }

    @Override
    public void responseReceived(String resp, boolean success) {
        Protocol.this.commandListReceived(success ? resp.split("[\n ]") : null);
    }
}
