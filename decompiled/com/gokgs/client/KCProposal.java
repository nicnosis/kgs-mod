/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client;

import com.gokgs.shared.KProposal;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.Proposal;
import org.igoweb.igoweb.shared.User;

public class KCProposal
extends KProposal<User, KCProposal> {
    public KCProposal(GameType gameType) {
        super(gameType);
    }

    public KCProposal(KCProposal prop) {
        super(prop);
    }

    public KCProposal(DataInputStream in, Proposal.UserDecoder<User> userDecoder) throws IOException, Proposal.NoSuchUserException {
        super(in, userDecoder);
    }

    public KCProposal(byte[] bytes, Proposal.UserDecoder<User> userDecoder) throws IOException, Proposal.NoSuchUserException {
        this(new DataInputStream(new ByteArrayInputStream(bytes)), userDecoder);
    }
}
