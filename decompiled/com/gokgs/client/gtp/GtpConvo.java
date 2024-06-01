/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client.gtp;

import com.gokgs.client.gtp.Options;
import java.util.Locale;
import org.igoweb.go.gtp.Command;
import org.igoweb.go.gtp.Protocol;
import org.igoweb.igoweb.client.CChannel;
import org.igoweb.util.Event;

public class GtpConvo {
    private final CChannel chan;
    private final Protocol protocol;
    private final Options options;
    private final Type type;
    private final String peer;

    public GtpConvo(CChannel newChan, Protocol newProtocol, Options newOptions, Type newType, String newPeer) {
        this.chan = newChan;
        this.protocol = newProtocol;
        this.type = newType;
        this.peer = newPeer;
        this.options = newOptions;
        if (newProtocol.isCommandSupported("kgs-chat")) {
            newChan.addListener(this::handleConvoEvent);
        } else if (newType == Type.PRIVATE) {
            this.handleChatResponse(null, false);
        }
    }

    private void handleConvoEvent(Event event) {
        switch (event.type) {
            case 17: {
                this.handleChat((CChannel.Chat)event.arg);
            }
        }
    }

    private void handleChat(CChannel.Chat chat) {
        if (!chat.user.name.equals(this.peer)) {
            return;
        }
        StringBuilder cmd = new StringBuilder("kgs-chat");
        cmd.append(' ').append(this.type.toString().toLowerCase(Locale.US)).append(' ').append(chat.user.name).append(' ');
        String text = chat.text;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c < ' ') continue;
            cmd.append(c);
        }
        this.protocol.send(new Command(cmd.toString()){

            @Override
            public void responseReceived(String resp, boolean success) {
                GtpConvo.this.handleChatResponse(resp, success);
            }
        });
    }

    private void handleChatResponse(String resp, boolean success) {
        if (!success) {
            resp = this.options.convoAnswer;
        }
        if ((resp = resp.trim()).length() > 1000) {
            resp = resp.substring(0, 1000).trim();
        }
        this.chan.sendChat(resp);
        if (this.type == Type.PRIVATE) {
            this.chan.sendUnjoinRequest();
        }
    }

    public static enum Type {
        PRIVATE,
        GAME;

    }
}
