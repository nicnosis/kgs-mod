/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.igoweb.igoweb.client.CGame;
import org.igoweb.igoweb.client.CGameListEntry;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.GameAction;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.igoweb.shared.MsgTypesUp;
import org.igoweb.igoweb.shared.Proposal;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.igoweb.shared.User;

public abstract class CChallenge<PropT extends Proposal<User, ? extends Proposal.UserRole<User>, ?>>
extends CGameListEntry {
    public static final MsgTypesUp PROPOSAL = MsgTypesUp.CHALLENGE_PROPOSAL;
    public static final MsgTypesUp ACCEPT = MsgTypesUp.CHALLENGE_ACCEPT;
    public static final MsgTypesUp SUBMIT = MsgTypesUp.CHALLENGE_SUBMIT;
    public static final int NOTES_MAX_LEN = 80;
    private static final int EVENT_BASE = 69;
    public static final int PROPOSAL_EVENT = 69;
    public static final int FINAL_PROPOSAL_EVENT = 70;
    public static final int CANT_PLAY_RANKED_EVENT = 71;
    public static final int DECLINED_EVENT = 72;
    public static final int SUBMIT_RECEIVED_EVENT = 73;
    public static final int CHALLENGE_EVENT_LIMIT = 74;
    private PropT initialProposal;
    private PropT currentProposal;
    private PropT defaultSentProposal;
    private HashMap<String, PropT> sentProposals = new HashMap();
    private HashMap<String, PropT> submissions;
    private final ArrayList<User> submitters = new ArrayList();

    public CChallenge(Conn newConn, int newId, DataInputStream in) throws IOException {
        super(newConn, GameType.CHALLENGE, newId, in);
        if (this.getRole() == Role.CHALLENGE_CREATOR) {
            this.submissions = new HashMap();
        }
    }

    @Override
    protected void readFrom(DataInputStream in) throws IOException {
        super.readFrom(in);
        this.initialProposal = this.readProposal(in, this.conn);
    }

    @Override
    protected void handleMessage(MsgTypesDown msgType, DataInputStream in) throws IOException {
        switch (msgType) {
            case CHALLENGE_PROPOSAL: {
                this.currentProposal = this.readProposal(in, this.conn);
                if (this.getAction() == GameAction.CHALLENGE_ACCEPT && ((Proposal)this.currentProposal).equals(this.defaultSentProposal)) {
                    this.sendProposal(ACCEPT, this.currentProposal);
                }
                this.emit(69, this.currentProposal);
                break;
            }
            case CHALLENGE_FINAL: {
                CGame game = (CGame)this.conn.objects.get(in.readInt());
                PropT proposal = this.readProposal(in, this.conn);
                this.emit(70, proposal);
                if (game == null) break;
                game.emitProposal((Proposal<?, ?, ?>)proposal);
                break;
            }
            case CHALLENGE_CANT_PLAY_RANKED: {
                this.emit(71, this.getNonRankedDescription(in));
                break;
            }
            case CHALLENGE_SUBMIT: {
                User src = this.conn.getUser(in);
                if (this.isCensored(src)) break;
                this.submissions.put(src.name, this.readProposal(in, this.conn));
                this.submitters.add(src);
                this.emit(73, src);
                break;
            }
            case CHALLENGE_DECLINE: {
                Iterator<CGameListEntry.UserAction> iter = this.getActions().iterator();
                while (iter.hasNext()) {
                    if (iter.next().user != this.conn.getMe()) continue;
                    iter.remove();
                }
                this.emit(72);
                break;
            }
            default: {
                super.handleMessage(msgType, in);
            }
        }
    }

    public PropT getInitialProposal() {
        return this.initialProposal;
    }

    public boolean sendProposal(MsgTypesUp msgType, PropT proposal) {
        TxMessage tx;
        this.defaultSentProposal = proposal;
        if (((Proposal)proposal).isComplete()) {
            User opp = this.getOpponent(proposal);
            if (opp != null) {
                this.sentProposals.put(opp.name, proposal);
            }
        } else if (msgType != SUBMIT) {
            throw new IllegalArgumentException("Trying to send incomplete proposal: " + proposal);
        }
        TxMessage txMessage = tx = ((Proposal)proposal).getGameType().isRanked() ? ((Proposal)proposal).testRankedOk(0) : null;
        if (tx == null) {
            tx = this.buildMessage(msgType);
            ((Proposal)proposal).writeTo(tx);
            this.conn.send(tx);
            return true;
        }
        try {
            this.handleMessage(MsgTypesDown.CHALLENGE_CANT_PLAY_RANKED, new DataInputStream(new ByteArrayInputStream(tx.closeAndGetBytes(), 8, tx.size() - 8)));
        }
        catch (IOException excep) {
            throw new RuntimeException("Error decoding our can't play ranked message?", excep);
        }
        return false;
    }

    public void sendRetry() {
        this.conn.send(this.buildMessage(MsgTypesUp.CHALLENGE_RETRY));
    }

    protected abstract PropT readProposal(DataInputStream var1, Proposal.UserDecoder<User> var2) throws IOException;

    protected abstract PropT copyProposal(PropT var1);

    protected String getNonRankedDescription(DataInputStream in) throws IOException {
        return null;
    }

    public PropT getProposal() {
        if (this.getAction() == GameAction.CHALLENGE_SUBMITTED) {
            return this.defaultSentProposal;
        }
        return this.currentProposal == null ? this.initialProposal : this.currentProposal;
    }

    public PropT getCurrentProposal() {
        return this.currentProposal;
    }

    public void setUserProposal(String name, PropT proposal) {
        if (this.submissions != null) {
            this.submissions.put(name, proposal);
        }
        this.sentProposals.put(name, proposal);
    }

    public PropT getSubmission(String name) {
        return (PropT)((Proposal)this.submissions.get(name));
    }

    @Override
    protected void rmUser(User user) {
        if (this.submissions != null) {
            this.submissions.remove(user.name);
        }
        this.submitters.remove(user);
        super.rmUser(user);
    }

    public List<User> getSubmitters() {
        return this.submitters;
    }

    public void sendDecline(User user) {
        TxMessage tx = this.buildMessage(MsgTypesUp.CHALLENGE_DECLINE);
        tx.writeUTF(user.canonName());
        this.conn.send(tx);
    }

    public PropT getExpectedProposal(PropT prop) {
        return this.getExpectedProposal(this.getOpponent(prop));
    }

    public PropT getExpectedProposal(User user) {
        if (this.submissions == null || user == null) {
            return this.createExpectedProposal(this.defaultSentProposal == null ? this.initialProposal : this.defaultSentProposal, user);
        }
        Proposal prop = (Proposal)this.sentProposals.get(user.name);
        return (PropT)(prop == null ? this.createExpectedProposal(this.initialProposal, user) : prop);
    }

    protected PropT createExpectedProposal(PropT initial, User user) {
        PropT result = this.copyProposal(initial);
        if (user == null) {
            return result;
        }
        Proposal.UserRole<Object> toModify = null;
        for (int i = 0; i < ((Proposal)result).getUserRoles().size(); ++i) {
            Object ur = ((Proposal)result).getUserRole((int)i);
            Object prevUser = ((Proposal.UserRole)ur).getUser();
            if (prevUser == user) {
                return result;
            }
            if (toModify != null || prevUser != null) continue;
            toModify = ur;
        }
        if (toModify != null) {
            toModify.setUser(user);
        }
        return result;
    }

    private User getOpponent(PropT prop) {
        User defaultUser = null;
        User me = this.conn.getMe();
        for (int i = ((Proposal)prop).getUserRoles().size() - 1; i >= 0; --i) {
            Object opp = ((Proposal.UserRole)((Proposal)prop).getUserRole((int)i)).getUser();
            if (opp == me || opp == null) continue;
            if (defaultUser != null) {
                return null;
            }
            defaultUser = (User)opp;
        }
        return defaultUser;
    }

    public String toString() {
        return "CChallenge[" + this.currentProposal + "]";
    }
}
