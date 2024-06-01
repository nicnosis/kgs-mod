/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client;

import com.gokgs.client.KCProposal;
import com.gokgs.shared.KGameType;
import java.io.DataInputStream;
import java.io.IOException;
import org.igoweb.igoweb.client.CChallenge;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.Proposal;
import org.igoweb.igoweb.shared.User;
import org.igoweb.util.Defs;

public class KCChallenge
extends CChallenge<KCProposal> {
    public KCChallenge(Conn newConn, int newId, DataInputStream in) throws IOException {
        super(newConn, newId, in);
    }

    @Override
    protected KCProposal readProposal(DataInputStream in, Proposal.UserDecoder<User> userDecoder) throws IOException {
        try {
            return new KCProposal(in, userDecoder);
        }
        catch (Proposal.NoSuchUserException excep) {
            throw new RuntimeException(excep);
        }
    }

    @Override
    protected KCProposal copyProposal(KCProposal src) {
        return new KCProposal(src);
    }

    @Override
    protected String getNonRankedDescription(DataInputStream in) throws IOException {
        Object[] args = new Object[3];
        byte type = in.readByte();
        args[0] = (int)type;
        args[1] = (int)in.readByte();
        args[2] = type == 2 ? (Number)Float.valueOf((float)in.readShort() * 0.5f) : (Number)Integer.valueOf(in.readShort());
        return Defs.getString(-1772731645, args);
    }

    @Override
    protected KCProposal createExpectedProposal(KCProposal initial, User user) {
        KCProposal result = super.createExpectedProposal(initial, user);
        if (user != null && initial.getUserRole(user) == null && result.getGameType() != KGameType.RENGO) {
            result.computeHandicapAndKomi(user);
        }
        return result;
    }
}
