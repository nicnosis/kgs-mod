/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import org.igoweb.igoweb.shared.User;

public static class CChannel.Chat {
    public final User user;
    public final boolean announcement;
    public final boolean moderated;
    public final String text;

    public CChannel.Chat(User newUser, String newText, boolean newAnnouncement, boolean newModerated) {
        this.user = newUser;
        this.announcement = newAnnouncement;
        this.moderated = newModerated;
        this.text = newText;
    }

    public boolean isFromPeer() {
        return true;
    }

    public String toString() {
        return "CChannel.Chat[User=" + this.user + ",len=" + this.text.length() + (this.announcement ? ",annc" : "") + (this.moderated ? ",moderated" : "") + "]";
    }
}
