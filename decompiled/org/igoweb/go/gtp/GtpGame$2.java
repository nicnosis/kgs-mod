/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.gtp;

import org.igoweb.util.Event;
import org.igoweb.util.EventListener;

class GtpGame.2
implements EventListener {
    GtpGame.2() {
    }

    @Override
    public void handleEvent(Event event) {
        GtpGame.this.treeEvent(event);
    }
}
