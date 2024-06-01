/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.gtp;

import java.util.HashSet;
import java.util.Iterator;
import org.igoweb.go.Chain;
import org.igoweb.go.Game;
import org.igoweb.go.Loc;
import org.igoweb.go.Rules;
import org.igoweb.go.gtp.Command;
import org.igoweb.go.gtp.GtpException;
import org.igoweb.go.gtp.Protocol;
import org.igoweb.go.sgf.GameUpdater;
import org.igoweb.go.sgf.Node;
import org.igoweb.go.sgf.Prop;
import org.igoweb.go.sgf.Tree;
import org.igoweb.util.Emitter;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;
import org.igoweb.util.LockOrder;

public abstract class GtpGame
extends Emitter {
    public static final int EVENT_BASE = 10;
    private static final String[] RULES_NAMES = "japanese chinese aga new_zealand".split(" ");
    public static final int RESTART_EVENT = 10;
    public static final int ABORT_COMMAND_EVENT = 11;
    public static final int TOO_FEW_HANDICAPS_FROM_ENGINE_EVENT = 12;
    public static final int TOO_FEW_HANDICAPS_FROM_TREE_EVENT = 13;
    public static final int PASS_HANDICAP_EVENT = 14;
    public static final int RESPONSE_ABORTED_EVENT = 15;
    public static final int CLEANUP_FAILED_EVENT = 16;
    public static final int DEAD_STONES_SUBMITTED_EVENT = 17;
    public static final int QUIT_EVENT = 18;
    public static final int UNDO_AFTER_SUBMIT_EVENT = 19;
    public static final int EVENT_LIMIT = 21;
    private final int myColor;
    private Game game;
    private Protocol protocol;
    private Tree tree;
    private Object lock;
    protected static final int STATE_IDLE = 0;
    protected static final int STATE_GENMOVE_SENT = 1;
    protected static final int STATE_MOVE_SUBMITTED = 2;
    protected static final int STATE_FINAL_STATUS_SENT = 3;
    protected static final int STATE_DONE = 4;
    private int state = 0;
    private boolean resolvingDispute = false;
    private boolean doneResolving = false;
    private int numAbortedRequests = 0;
    private int numUndosRequested = 0;
    private int numAbortedUndos = 0;
    private StringBuilder handicapMessage;
    private int numHandicapsInMessage;
    private Rules rules;
    private HashSet<Chain> engineDeadChains = null;
    private HashSet<Chain> treeDeadChains = new HashSet();
    private final Command undoCommand = new Command("undo"){

        @Override
        public void responseReceived(String resp, boolean success) throws GtpException {
            GtpGame.this.undoResponseReceived(success);
        }
    };
    private boolean hcapChosenByMe = false;
    private EventListener treeListener = new EventListener(){

        @Override
        public void handleEvent(Event event) {
            GtpGame.this.treeEvent(event);
        }
    };

    public GtpGame(Tree tree, Protocol protocol, int myColor) {
        this.lock = protocol.getLock();
        this.tree = tree;
        this.protocol = protocol;
        this.myColor = myColor;
        this.rules = tree.root.findProp(0).getRules();
        this.game = new Game(this.rules);
        if (protocol.isCommandSupported("kgs-rules")) {
            protocol.send(new Command("kgs-rules " + RULES_NAMES[this.rules.getType()]));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void treeReady() {
        assert (LockOrder.testAcquire(this.lock));
        Object object = this.lock;
        synchronized (object) {
            new GameUpdater(this.tree, this.game, null, null);
            this.tree.addListener(this.treeListener);
            this.syncEngineState(false);
        }
    }

    private void syncEngineState(boolean clearRequired) {
        if (this.state == 2) {
            this.emit(19, "genmove");
            this.state = 0;
        } else if (this.state == 1) {
            this.emit(11, "genmove");
            ++this.numAbortedRequests;
            this.state = 0;
        }
        this.protocol.send(new Command("boardsize " + this.rules.getSize(), true));
        if (this.protocol.isCommandSupported("clear_board") || clearRequired) {
            this.protocol.send(new Command("clear_board", true));
        }
        this.protocol.send(new Command("komi " + this.rules.getKomi()));
        String timeSettings = this.protocol.buildTimeSettings(this.rules);
        if (timeSettings != null) {
            this.protocol.send(new Command(timeSettings, true));
        }
        if (this.rules.getHandicap() > 0) {
            this.handicapMessage = new StringBuilder("set_free_handicap");
            this.numHandicapsInMessage = 0;
            this.hcapChosenByMe = false;
            if (!this.rules.isFixedHandicap() && this.myColor == 0 && this.tree.getActiveNode() == this.tree.root) {
                this.hcapChosenByMe = true;
                this.protocol.send(new Command("place_free_handicap " + this.rules.getHandicap()){

                    @Override
                    public void responseReceived(String resp, boolean success) throws GtpException {
                        GtpGame.this.handicapPlacementReceived(resp, success);
                    }
                });
            }
        } else {
            this.handicapMessage = null;
        }
        for (Node node = this.tree.root; node != null; node = node.getActiveChild()) {
            this.processNode(node);
            if (node == this.tree.getActiveNode()) break;
        }
    }

    private void treeEvent(Event event) {
        switch (event.type) {
            case 7: {
                this.handleNewActiveNode(this.tree.getNode(((Number)event.arg).intValue()));
                break;
            }
            case 0: {
                if (event.source != this.tree.getActiveNode()) break;
                this.sgfPropAdded((Prop)event.arg);
                break;
            }
            case 1: {
                if (event.source != this.tree.getActiveNode()) break;
                this.sgfPropRemoved((Prop)event.arg);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleNewActiveNode(Node prevActive) {
        assert (LockOrder.testAcquire(this.lock));
        Object object = this.lock;
        synchronized (object) {
            this.treeDeadChains.clear();
            Node newActive = this.tree.getActiveNode();
            while (!prevActive.isOnActivePath()) {
                if (!this.doUndo(prevActive)) {
                    return;
                }
                prevActive = prevActive.parent;
            }
            boolean newIsChildOfCur = true;
            Node tmp = newActive;
            while (tmp != prevActive) {
                if (tmp == null) {
                    newIsChildOfCur = false;
                    break;
                }
                tmp = tmp.parent;
            }
            if (newIsChildOfCur) {
                while (prevActive != newActive) {
                    prevActive = prevActive.getActiveChild();
                    this.processNode(prevActive);
                }
            } else {
                while (prevActive != newActive) {
                    if (!this.doUndo(prevActive)) {
                        return;
                    }
                    prevActive = prevActive.parent;
                }
            }
        }
    }

    private boolean doUndo(Node node) {
        Prop moveProp;
        if (this.state == 3 || this.state == 4) {
            if (this.state == 3) {
                ++this.numAbortedRequests;
            }
            this.engineDeadChains = null;
            this.state = 0;
            if (this.protocol.isCommandSupported("kgs-genmove_cleanup") && this.rules.getType() != 0) {
                this.resolvingDispute = true;
            }
        }
        if ((moveProp = node.findProp(14)) == null) {
            return true;
        }
        if (this.handicapMessage != null || !this.protocol.isCommandSupported("undo")) {
            this.emit(10, !this.protocol.isCommandSupported("undo") ? "Undo not supported by engine" : "Undo not possible during handicap setup");
            if (moveProp.getLoc() != Loc.PASS || this.protocol.isCommandSupported("clear_board")) {
                this.syncEngineState(true);
            }
            return false;
        }
        if (this.state == 1) {
            this.emit(11, "genmove");
            this.protocol.send(this.undoCommand);
            this.state = 0;
            ++this.numAbortedRequests;
            ++this.numUndosRequested;
        } else if (this.state == 2) {
            this.emit(19, "genmove");
            this.protocol.send(this.undoCommand);
            ++this.numUndosRequested;
            this.state = 0;
        }
        this.protocol.send(this.undoCommand);
        ++this.numUndosRequested;
        return true;
    }

    private void processNode(Node node) {
        Iterator<Prop> iter = node.iterator();
        while (iter.hasNext()) {
            this.sgfPropAdded(iter.next());
        }
    }

    protected void sgfPropRemoved(Prop param) {
        if (param.type == 23) {
            this.treeDeadChains.remove(this.game.getChain(param.getLoc()));
        }
    }

    protected void sgfPropAdded(Prop param) {
        switch (param.type) {
            case 14: 
            case 17: {
                if (this.handicapMessage != null) {
                    if (param.getColor() == 0) {
                        if (param.getLoc() != Loc.PASS) {
                            this.handicapMessage.append(' ').append(Protocol.toGtpVertex(param.getLoc(), this.game.size));
                        }
                    } else {
                        this.emit(13);
                        this.numHandicapsInMessage = this.rules.getHandicap();
                    }
                    if (++this.numHandicapsInMessage >= this.rules.getHandicap()) {
                        if (!this.hcapChosenByMe) {
                            this.protocol.send(new Command(this.handicapMessage.toString(), true));
                        }
                        this.handicapMessage = null;
                    }
                    if (param.getColor() == 0) {
                        return;
                    }
                }
                if (param.type != 14) {
                    throw new IllegalStateException("Got an AB/AW/AE when expecting moves");
                }
                if (this.state == 2) {
                    if (param.getColor() != this.myColor) {
                        throw new IllegalStateException("Got a parameter of my opponent's color when in state MOVE_SUBMITTED");
                    }
                    this.state = 0;
                    break;
                }
                this.protocol.send(new Command("play " + Protocol.toGtpColor(param.getColor()) + " " + Protocol.toGtpVertex(param.getLoc(), this.rules.getSize()), true));
                break;
            }
            case 18: {
                if (param.getColor() != this.myColor) break;
                if (this.rules.getTimeSystem() == 2 && !this.protocol.isCommandSupported("kgs-time_settings")) {
                    this.protocol.send(new Command("time_left " + Protocol.toGtpColor(param.getColor()) + " " + (int)param.getFloat() + " 0"));
                    break;
                }
                this.protocol.send(new Command("time_left " + Protocol.toGtpColor(param.getColor()) + " " + (int)param.getFloat() + " " + param.getInt()));
                break;
            }
            case 23: {
                this.treeDeadChains.add(this.game.getChain(param.getLoc()));
                break;
            }
        }
    }

    protected void deadListReceived(String deadList) throws GtpException {
        if (this.numAbortedRequests > 0) {
            this.emit(15, "final_status_list");
            --this.numAbortedRequests;
            return;
        }
        if (deadList == null) {
            this.state = 4;
            this.emit(17, Boolean.FALSE);
            return;
        }
        HashSet<Chain> cleanupFailedSet = this.resolvingDispute ? new HashSet<Chain>() : null;
        this.engineDeadChains = new HashSet();
        for (String vertex : deadList.split(" |\n")) {
            if (vertex.length() <= 0) continue;
            Chain group = this.game.getChain(Protocol.parseGtpVertex(vertex, this.game.size));
            if (group == null) {
                throw new GtpException("Engine reported dead stone at \"" + vertex + "\", no stone there!");
            }
            if (cleanupFailedSet != null && group.color != this.myColor && cleanupFailedSet.add(group)) {
                this.emit(16, group);
                continue;
            }
            this.engineDeadChains.add(group);
        }
        this.state = 4;
        this.syncDeadChains(true);
        this.emit(17, Boolean.TRUE);
    }

    protected abstract void submitMarkDead(Loc var1, boolean var2);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void testSendRequest() {
        assert (LockOrder.testAcquire(this.lock));
        Object object = this.lock;
        synchronized (object) {
            if (this.state != 0 || this.handicapMessage != null) {
                return;
            }
            if (this.game.isPlayOver()) {
                this.state = 3;
                this.protocol.send(new Command("final_status_list dead"){

                    @Override
                    public void responseReceived(String resp, boolean success) throws GtpException {
                        GtpGame.this.deadListReceived(success ? resp : null);
                    }
                });
            } else if (this.isGenmoveNeeded()) {
                this.state = 1;
                this.protocol.send(new Command((this.resolvingDispute ? "kgs-genmove_cleanup" : "genmove") + " " + Protocol.toGtpColor(this.myColor)){

                    @Override
                    public void responseReceived(String resp, boolean success) throws GtpException {
                        GtpGame.this.moveReceived(resp, success);
                    }
                });
            }
        }
    }

    protected boolean isGenmoveNeeded() {
        return this.game.getWhoseMove() == this.myColor;
    }

    private void moveReceived(String resp, boolean success) throws GtpException {
        if (this.numAbortedRequests > 0) {
            this.emit(15, "genmove");
            --this.numAbortedRequests;
            return;
        }
        assert (this.state == 1);
        if (!success) {
            this.state = 0;
            throw new GtpException("Got error response \"" + resp + "\" while expecting a move.");
        }
        if (resp.equals("resign")) {
            this.state = 0;
            this.submitResign();
        } else {
            Loc loc = Protocol.parseGtpVertex(resp, this.game.size);
            if (this.game.isLegal(loc) != 0) {
                throw new GtpException("Move \"" + resp + "\" from engine is not a legal move" + (this.game.isLegal(loc) == 2 ? " (ko rule violation)." : "."));
            }
            this.state = 2;
            if (loc == Loc.PASS && this.resolvingDispute) {
                this.doneResolving = true;
            }
            this.submitMove(loc);
        }
    }

    protected abstract void submitResign();

    private void handicapPlacementReceived(String resp, boolean success) throws GtpException {
        if (!success) {
            throw new GtpException("Got error \"" + resp + "\" from engine when expecting free handicap placement.");
        }
        String[] moves = resp.split(" ");
        HashSet<Loc> allMoves = new HashSet<Loc>();
        for (int i = 0; i < moves.length; ++i) {
            Loc loc = Protocol.parseGtpVertex(moves[i], this.game.size);
            if (loc == Loc.PASS) {
                this.emit(14);
                continue;
            }
            if (!allMoves.add(loc)) {
                throw new IllegalArgumentException("Move \"" + resp + "\" from engine appears twice in handicap list!");
            }
            this.submitMove(loc);
        }
        if (moves.length != this.rules.getHandicap()) {
            this.emit(12);
            this.submitMove(Loc.PASS);
        }
    }

    protected abstract void submitMove(Loc var1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void closeGame() {
        assert (LockOrder.testAcquire(this.lock));
        Object object = this.lock;
        synchronized (object) {
            this.tree.removeListener(this.treeListener);
            if (this.protocol.isCommandSupported("kgs-game_over")) {
                this.protocol.send(new Command("kgs-game_over"));
            }
            if (!this.protocol.isCommandSupported("clear_board")) {
                this.emit(18);
                this.protocol.send(new Command("quit", true));
            }
        }
    }

    private void undoResponseReceived(boolean success) throws GtpException {
        --this.numUndosRequested;
        if (this.numAbortedUndos > 0) {
            this.emit(15, "undo");
            --this.numAbortedUndos;
            return;
        }
        if (!success) {
            if (!this.protocol.isCommandSupported("clear_board")) {
                throw new GtpException("Undo failed and clear board is not available - aborting");
            }
            this.emit(10, "Undo failed");
            this.numAbortedUndos = this.numUndosRequested;
            if (this.state == 3 || this.state == 1) {
                this.emit(11, this.state == 3 ? "final_status_list" : "genmove");
                ++this.numAbortedRequests;
                this.engineDeadChains = null;
                this.state = 0;
            } else if (this.state == 4) {
                this.engineDeadChains = null;
                this.state = 0;
            }
            this.syncEngineState(true);
        }
    }

    protected boolean syncDeadChains(boolean exact) {
        boolean changes = false;
        for (Chain group : this.engineDeadChains) {
            if (this.treeDeadChains.contains(group) || !exact && group.color == this.myColor) continue;
            changes = true;
            this.submitMarkDead(group.getMember(), true);
        }
        for (Chain group : this.treeDeadChains) {
            if (this.engineDeadChains.contains(group) || !exact && group.color != this.myColor) continue;
            changes = true;
            this.submitMarkDead(group.getMember(), false);
        }
        return changes;
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    protected int getState() {
        return this.state;
    }

    public int getMyColor() {
        return this.myColor;
    }

    protected Object getLock() {
        return this.lock;
    }

    public Game getGame() {
        return this.game;
    }

    public Rules getRules() {
        return this.rules;
    }

    public boolean isResolvingDispute() {
        return this.resolvingDispute;
    }

    public boolean isDoneResolving() {
        return this.doneResolving;
    }
}
