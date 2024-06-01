/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client.gtp;

import com.gokgs.client.KCGame;
import com.gokgs.client.gtp.Options;
import com.gokgs.shared.KRole;
import java.util.LinkedList;
import java.util.logging.Logger;
import org.igoweb.go.Go;
import org.igoweb.go.Loc;
import org.igoweb.go.gtp.GtpException;
import org.igoweb.go.gtp.GtpGame;
import org.igoweb.go.gtp.Protocol;
import org.igoweb.go.sgf.Prop;
import org.igoweb.igoweb.client.Client;
import org.igoweb.igoweb.shared.GameAction;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.User;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;
import org.igoweb.util.LockOrder;

public class ClientGtpGame
extends GtpGame {
    private static final long OPPONENT_GONE_TIMEOUT = 300000L;
    private Client client;
    private KCGame cGame;
    private boolean opponentIsPresent = true;
    private Options options;
    private Logger logger;
    private Thread scoringFinisherThread = null;
    private long lastDeadStoneChange;
    private LinkedList<Loc> pendingMoves = new LinkedList();
    private EventListener cGameListener = this::gameEvent;

    public ClientGtpGame(Client newClient, KCGame newCGame, Protocol protocol, Options newOptions) {
        super(newCGame.getSgfTree(), protocol, newCGame.getRole().team);
        this.client = newClient;
        this.cGame = newCGame;
        this.options = newOptions;
        this.logger = newOptions.logger;
        newCGame.addListener(this.cGameListener);
        int oppColor = Go.opponent(newCGame.getRole().team);
        User opponent = newCGame.getPlayer(KRole.forColor(oppColor));
        this.logger.fine("Starting game as " + (newCGame.getRole() == KRole.BLACK ? "black" : "white") + " against " + opponent.name);
        if (protocol.getEngineName() != null) {
            newCGame.sendChat("GTP Engine for " + newClient.getMe().name + " (" + (newCGame.getRole() == KRole.WHITE ? "white" : "black") + "): " + protocol.getEngineName() + (protocol.getEngineVersion() != null ? " version " + protocol.getEngineVersion() : ""));
        }
        this.treeDoneLoading();
        this.addListener(event -> {
            if (event.type == 17) {
                this.deadStonesSubmitted((Boolean)event.arg);
            }
        });
    }

    private void deadStonesSubmitted(boolean success) {
        if (!success && (this.cGame.gameType.isRanked() || this.cGame.gameType.isTournament())) {
            throw new IllegalArgumentException("Command final_status_list failed in a tournament/ranked game. This is not permitted - please disable ranked games until engine is fixed.");
        }
        this.gameEvent(new Event(this.cGame, 40));
    }

    private void treeDoneLoading() {
        this.treeReady();
        if (!this.cGame.getMembers().containsKey(this.cGame.getPlayer((Role)KRole.forColor((int)Go.opponent((int)this.cGame.getRole().team))).name)) {
            this.startOpponentTimer();
        }
    }

    private void gameEvent(Event event) {
        switch (event.type) {
            case 65: {
                Object[] argArray = (Object[])event.arg;
                Short scoreSummary = (Short)argArray[2];
                this.logger.fine("Game over; result = " + IBundle.get().formatScore(scoreSummary.shortValue()));
                break;
            }
            case 20: {
                User who = (User)event.arg;
                if (who.equals(this.client.getMe()) || this.cGame.getRole(who.name) == null) break;
                this.startOpponentTimer();
                break;
            }
            case 19: {
                User who = (User)event.arg;
                if (who.equals(this.client.getMe()) || this.cGame.getRole(who.name) == null) break;
                this.opponentIsPresent = true;
                this.getLock().notifyAll();
                break;
            }
            case 61: {
                if (this.options.allowUndo && !this.cGame.gameType.isRanked() && !this.cGame.gameType.isTournament() && (this.getProtocol().isCommandSupported("undo") || this.getProtocol().isCommandSupported("clear_board"))) {
                    this.logger.finer("Undo requested, request will be granted");
                    this.cGame.sendGrantUndo();
                    break;
                }
                this.logger.finer("Undo requested, cannot be granted: " + (this.cGame.gameType.isRanked() || this.cGame.gameType.isTournament() ? "Game is rated/tournament" : "Engine supports neither \"undo\" nor \"clear_board\""));
                break;
            }
            case 40: {
                if (this.cGame.getAction() == GameAction.MOVE || this.cGame.getAction() == GameAction.SCORE) {
                    if (!this.pendingMoves.isEmpty()) {
                        if (this.cGame.getAction() == GameAction.MOVE) {
                            this.submitMove(this.pendingMoves.remove());
                            return;
                        }
                        this.logger.warning("Entered scoring when pending moves queued; they will be dropped.");
                        this.pendingMoves.clear();
                    }
                    this.testSendRequest();
                }
                if (this.cGame.getAction() == GameAction.SCORE && !this.cGame.isDoneSent(this.cGame.getRole().team)) {
                    if (!this.cGame.gameType.isRanked() && !this.cGame.gameType.isTournament() && this.getState() != 3) {
                        this.logger.finest("Telling server that current living/dead stones are OK.");
                        this.cGame.sendDone();
                    } else if (this.scoringFinisherThread == null) {
                        this.scoringFinisherThread = new Thread(this::finishScoring);
                        this.lastDeadStoneChange = System.currentTimeMillis();
                        this.scoringFinisherThread.start();
                    }
                } else if (this.cGame.isOver()) {
                    this.logger.fine("Game is over and scored (final result = " + IBundle.get().formatScore(this.cGame.getScore()) + "), leaving game.");
                    this.closeGame();
                }
                if (this.scoringFinisherThread == null || !this.cGame.isOver()) break;
                this.scoringFinisherThread = null;
                this.getLock().notifyAll();
                break;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void waitForOpponentReturn() {
        if (this.cGame.gameType.isTournament()) {
            return;
        }
        long deadline = System.currentTimeMillis() + 300000L;
        try {
            assert (LockOrder.testAcquire(this.getLock()));
            Object object = this.getLock();
            synchronized (object) {
                while (!this.opponentIsPresent) {
                    long timeout = deadline - System.currentTimeMillis();
                    if (timeout <= 0L) {
                        this.logger.warning("Opponent has not returned. Leaving game.");
                        this.closeGame();
                        return;
                    }
                    assert (LockOrder.testWait(this.getLock()));
                    this.getLock().wait(timeout);
                }
                this.logger.fine("Opponent has returned.");
            }
        }
        catch (Throwable thrown) {
            this.emit(2, thrown);
        }
    }

    @Override
    protected void closeGame() {
        if (this.scoringFinisherThread != null) {
            this.scoringFinisherThread = null;
            this.getLock().notifyAll();
        }
        this.cGame.sendUnjoinRequest();
        this.cGame.removeListener(this.cGameListener);
        super.closeGame();
    }

    private void startOpponentTimer() {
        this.opponentIsPresent = false;
        this.logger.warning("Opponent has left game. Will give them 5 minutes to return.");
        new Thread(this::waitForOpponentReturn, "Opponent waiter").start();
    }

    @Override
    protected void submitMove(Loc moveLoc) {
        if (this.cGame.getAction() == GameAction.MOVE) {
            this.logger.finest("Submitting move " + Protocol.toGtpVertex(moveLoc, this.getGame().size) + " to server");
            this.cGame.sendMove(moveLoc);
        } else {
            this.pendingMoves.add(moveLoc);
        }
    }

    @Override
    protected void submitResign() {
        this.logger.fine("Submitting resignation to server");
        this.cGame.sendResign();
    }

    @Override
    protected void submitMarkDead(Loc loc, boolean isDead) {
        this.logger.finest("Submitting stone status to server: Location = " + Protocol.toGtpVertex(loc, this.getGame().size) + ", dead = " + isDead);
        this.cGame.sendMarkDead(loc, isDead);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void finishScoring() {
        int numCorrections = 0;
        assert (LockOrder.testAcquire(this.getLock()));
        Object object = this.getLock();
        synchronized (object) {
            try {
                while (true) {
                    if (this.scoringFinisherThread != Thread.currentThread()) {
                        return;
                    }
                    if (this.getState() != 4) {
                        this.scoringFinisherThread = null;
                        return;
                    }
                    long sleepTime = this.lastDeadStoneChange + 5000L - System.currentTimeMillis();
                    if (sleepTime > 0L) {
                        assert (LockOrder.testWait(this.getLock()));
                        this.getLock().wait(sleepTime);
                        continue;
                    }
                    this.lastDeadStoneChange = System.currentTimeMillis();
                    if (this.syncDeadChains(false)) {
                        if (this.cGame.gameType.isTournament() && !this.isDoneResolving()) {
                            if (this.getProtocol().isCommandSupported("kgs-genmove_cleanup")) {
                                this.cGame.sendChat("Disagreement over tournament scoring. Switching to cleanup mode.");
                                this.logger.info("Disagreement over tournament scoring. Switching to cleanup mode.");
                                this.cGame.sendUndoRequest();
                                continue;
                            }
                            this.logger.warning("Disagreement over tournament scoring. We cannot perform cleanup (kgs-genmove_cleanup not supported). Accepting opponent's dead stone list.");
                            this.cGame.sendDone();
                            continue;
                        }
                        if (++numCorrections != 2) continue;
                        this.cGame.sendChat(this.getRules().getType() != 0 && this.getProtocol().isCommandSupported("kgs-genmove_cleanup") ? this.options.cleanupHintMessage : this.options.cantDisagreeMessage);
                        continue;
                    }
                    if (!this.cGame.isJoined() || this.cGame.isDoneSent(this.cGame.getRole().team)) continue;
                    this.cGame.sendDone();
                }
            }
            catch (Throwable thrown) {
                this.emit(2, thrown);
            }
        }
    }

    @Override
    protected void sgfPropRemoved(Prop param) {
        if (param.type == 23) {
            this.lastDeadStoneChange = System.currentTimeMillis();
        }
        super.sgfPropRemoved(param);
    }

    @Override
    protected void sgfPropAdded(Prop param) {
        if (param.type == 23) {
            this.lastDeadStoneChange = System.currentTimeMillis();
        }
        super.sgfPropAdded(param);
    }

    @Override
    protected void deadListReceived(String deadList) throws GtpException {
        if (deadList == null) {
            deadList = "";
        }
        if (this.cGame.gameType.isTournament()) {
            if (!this.getProtocol().isCommandSupported("kgs-genmove_cleanup")) {
                this.logger.warning("Warning: Program does not support the kgs-genmove_cleanup command. It will be assumed that all dead stones have already been removed.");
                deadList = "";
            } else if (this.isDoneResolving()) {
                this.logger.info("Cleanup mode has ended by passes. It will be assumed that all dead stones have already been removed.");
                deadList = "";
            }
        }
        super.deadListReceived(deadList);
    }

    @Override
    protected boolean isGenmoveNeeded() {
        return this.cGame.getAction() == GameAction.MOVE;
    }
}
