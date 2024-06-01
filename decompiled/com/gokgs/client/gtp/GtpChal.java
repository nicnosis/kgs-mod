/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client.gtp;

import com.gokgs.client.KCChallenge;
import com.gokgs.client.KCProposal;
import com.gokgs.client.gtp.State;
import com.gokgs.shared.KGameType;
import com.gokgs.shared.KProposal;
import com.gokgs.shared.KRole;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.igoweb.go.Rules;
import org.igoweb.go.gtp.Command;
import org.igoweb.go.gtp.Protocol;
import org.igoweb.igoweb.client.CChallenge;
import org.igoweb.igoweb.client.Client;
import org.igoweb.igoweb.shared.GameAction;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.User;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;

public class GtpChal
implements EventListener {
    private final Client client;
    private final KCChallenge chal;
    private boolean busy;
    private final Logger logger;
    private boolean propDeferred = false;

    public GtpChal(Client newClient, KCChallenge newChal) {
        this.client = newClient;
        this.chal = newChal;
        this.logger = State.get((Client)newClient).options.logger;
        newChal.addListener(this);
    }

    @Override
    public void handleEvent(Event event) {
        State state = State.get(this.client);
        switch (event.type) {
            case 15: {
                this.logger.finer("Joined challenge");
                state.myChal = this.chal;
                this.performAction();
                break;
            }
            case 73: {
                User opponent = (User)event.arg;
                if (state.options.opponent != null && !opponent.name.equalsIgnoreCase(state.options.opponent)) {
                    this.logger.fine("Got proposal from \"" + opponent.name + "\", want \"" + state.options.opponent + "\".");
                    return;
                }
                KCProposal prop = (KCProposal)this.chal.getSubmission(opponent.name);
                if (prop.getGameType().isRanked() && state.isEscaper(opponent)) {
                    this.logger.fine("Got ranked proposal from \"" + opponent.name + "\", we have an unfinished game vs. him, so I won't play.");
                    this.chal.sendDecline(opponent);
                    return;
                }
                Protocol protocol = state.protocol;
                if (prop.getRules().getHandicap() > 0 && (!protocol.isCommandSupported("set_free_handicap") || prop.getRole(this.client.getMe()) == KRole.BLACK && !protocol.isCommandSupported("place_free_handicap"))) {
                    this.logger.finer("Player asked for handicap, engine does not support it. Setting handicap to 0.");
                    prop.getRules().setHandicap(0);
                }
                if (state.options.forceKomi && prop.getRules().getKomi() != state.options.rules.getKomi()) {
                    this.logger.finer("Player asked for komi " + prop.getRules().getKomi() + ", we require komi " + state.options.rules.getKomi() + ". Setting it to our value.");
                    prop.getRules().setKomi(state.options.rules.getKomi());
                }
                this.logger.finer("Got challenge from \"" + opponent.name + "\", testing engine response.");
                this.testProposal(prop);
                break;
            }
            case 40: {
                this.performAction();
                break;
            }
            case 69: {
                this.performAction();
                break;
            }
            case 70: {
                break;
            }
            case 16: {
                if (state.myChal != this.chal) break;
                state.myChal = null;
                state.proposals.clear();
            }
        }
    }

    private void testProposal(KCProposal prop) {
        LinkedList<KCProposal> proposals = State.get((Client)this.client).proposals;
        boolean runTest = proposals.isEmpty();
        proposals.add(prop);
        if (runTest) {
            this.sendBoardSize();
        }
    }

    private void sendBoardSize() {
        if (this.chal.getAction() == null || this.chal.getAction() == GameAction.CHALLENGE_WAIT) {
            this.logger.finer("Waiting for prior proposal to resolve before processing next proposal");
            this.propDeferred = true;
            return;
        }
        State state = State.get(this.client);
        final KCProposal prop = state.proposals.getFirst();
        state.protocol.send(new Command("boardsize " + prop.getRules().getSize()){

            @Override
            public void responseReceived(String resp, boolean success) {
                GtpChal.this.receivedBoardSizeResponse(success, prop);
            }
        });
    }

    private void receivedBoardSizeResponse(boolean success, final KCProposal prop) {
        State state = State.get(this.client);
        if (state.proposals.isEmpty() || state.proposals.getFirst() != prop) {
            this.logger.finer("Proposal is obsolete. Ignoring it.");
            return;
        }
        if (success) {
            this.logger.finest("Board size " + prop.getRules().getSize() + " is acceptable");
        } else {
            this.logger.finer("Board size " + prop.getRules().getSize() + " is not acceptable, changing it to " + state.options.rules.getSize());
            prop.getRules().setSize(state.options.rules.getSize());
            state.protocol.send(new Command("boardsize " + prop.getRules().getSize(), true));
        }
        String timeSettings = state.protocol.buildTimeSettings(prop.getRules());
        if (timeSettings == null) {
            this.logger.finest("Engine doesn't know time_settings command - assuming that time settings are OK.");
            this.receivedTimeResponse(true, prop);
        } else {
            state.protocol.send(new Command(timeSettings){

                @Override
                public void responseReceived(String resp, boolean timeSuccess) {
                    GtpChal.this.receivedTimeResponse(timeSuccess, prop);
                }
            });
        }
    }

    private void receivedTimeResponse(boolean success, KCProposal prop) {
        State state = State.get(this.client);
        LinkedList<KCProposal> proposals = state.proposals;
        if (proposals.isEmpty() || proposals.getFirst() != prop) {
            this.logger.finer("Proposal is obsolete. Ignoring it.");
            return;
        }
        Rules rules = prop.getRules();
        if (success) {
            this.logger.finest("Time system is acceptable");
        } else {
            this.logger.finer("Time system is not acceptable, changing it to default");
            rules.setTimeSystem(state.options.rules.getTimeSystem());
            if (rules.getMainTime() >= 0) {
                rules.setMainTime(state.options.rules.getMainTime());
            }
            if (rules.getByoYomiTime() >= 0) {
                rules.setByoYomiTime(state.options.rules.getByoYomiTime());
            }
            if (rules.getByoYomiPeriods() >= 0) {
                rules.setByoYomiPeriods(state.options.rules.getByoYomiPeriods());
            }
            if (rules.getByoYomiStones() >= 0) {
                rules.setByoYomiPeriods(state.options.rules.getByoYomiStones());
            }
        }
        boolean rankedOk = state.canPlayRanked();
        if (rankedOk) {
            Rules compare = new Rules(rules);
            compare.setKomi(state.options.rules.getKomi());
            compare.setHandicap(state.options.rules.getHandicap());
            rankedOk = state.options.rules.equals(compare);
            if (!rankedOk) {
                this.logger.finer("Challenge does not match my preferences, switching game to free");
            }
        }
        if (!rankedOk) {
            prop.setGameType(KGameType.FREE);
        }
        this.logger.finest("Sending challenge back to server");
        if (this.chal.getRole() == Role.CHALLENGE_CREATOR) {
            prop.testRankedOk(0);
            this.chal.sendProposal(CChallenge.PROPOSAL, prop);
        } else {
            this.chal.sendProposal(CChallenge.ACCEPT, prop);
        }
        proposals.removeFirst();
        if (!proposals.isEmpty()) {
            this.sendBoardSize();
        }
    }

    private void performAction() {
        GameAction action = this.chal.getAction();
        if (this.propDeferred && action != null && action != GameAction.CHALLENGE_WAIT) {
            this.logger.finer("Action is valid, can now test next proposal.");
            this.propDeferred = false;
            this.sendBoardSize();
        }
        State state = State.get(this.client);
        KCProposal prop = (KCProposal)this.chal.getProposal();
        if (!this.busy && state.myChal == this.chal) {
            if (action == null) {
                ((KProposal.KUserRole)prop.getUserRole(true)).setUser(this.client.getMe());
                prop.computeHandicapAndKomi(null);
                this.chal.sendProposal(CChallenge.SUBMIT, prop);
                this.sync();
            } else if (action == GameAction.CHALLENGE_ACCEPT) {
                this.testProposal((KCProposal)this.chal.getProposal());
                this.sync();
            }
        }
    }

    private void sync() {
        this.busy = true;
        this.client.sendSync(() -> {
            this.busy = false;
            this.performAction();
        });
    }
}
