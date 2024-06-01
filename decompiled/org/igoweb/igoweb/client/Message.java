/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import org.igoweb.igoweb.shared.User;

public class Message {
    public static final int MAX_LENGTH = 1000;
    public final long sendDate;
    public final User sender;
    public final String text;

    Message(long sendDate, User sender, String text) {
        this.sendDate = sendDate;
        this.sender = sender;
        this.text = text;
    }
}
