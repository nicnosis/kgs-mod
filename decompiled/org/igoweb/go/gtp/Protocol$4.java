/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.gtp;

import org.igoweb.go.gtp.Command;

class Protocol.4
extends Command {
    final /* synthetic */ Object val$lock;

    Protocol.4(String text, Object object) {
        this.val$lock = object;
        super(text);
    }

    @Override
    public void responseReceived(String resp, boolean success) {
        Protocol.this.ready = true;
        this.val$lock.notifyAll();
        if (success) {
            Protocol.this.engineVersion = resp;
        }
    }
}
