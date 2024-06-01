/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

public static enum CConvo.State {
    PEER_GONE(false),
    PEER_DISCONNECTED(false),
    CHATS_BLOCKED(false),
    PEER_IN_TOURN_NO_CHAT(false),
    PEER_IN_TOURN(true),
    PEER_PLAYING(true),
    PEER_SLEEPING(true),
    NORMAL(true);

    private final boolean chatsOk;

    private CConvo.State(boolean newChatsOk) {
        this.chatsOk = newChatsOk;
    }

    public boolean isChatsOk() {
        return this.chatsOk;
    }
}
