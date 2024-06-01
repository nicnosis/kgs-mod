/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.sgf;

import java.lang.ref.WeakReference;
import java.util.Stack;
import org.igoweb.games.Clock;
import org.igoweb.go.Game;
import org.igoweb.go.GoClock;
import org.igoweb.go.Goban;
import org.igoweb.go.Loc;
import org.igoweb.go.sgf.Node;
import org.igoweb.go.sgf.Prop;
import org.igoweb.go.sgf.Tree;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;

public class GameUpdater {
    public final Tree tree;
    private final WeakReference<Game> gameRef;
    public final EventListener treeListener = this::treeEvent;
    private final Prop[] lastTimeProps = new Prop[2];
    private final Node[] lastTimeNodes = new Node[2];
    private Clock[] clocks;

    public GameUpdater(Tree newTree, Game game, Clock wClock, Clock bClock) {
        this.tree = newTree;
        this.gameRef = new WeakReference<Game>(game);
        newTree.addListener(this.treeListener);
        this.clocks = new Clock[2];
        this.clocks[1] = wClock;
        this.clocks[0] = bClock;
        this.updateClocks(game, this.playDown(game, newTree.root, newTree.getActiveNode()));
    }

    protected void treeEvent(Event event) {
        Game game = (Game)this.gameRef.get();
        if (game == null) {
            this.close();
            return;
        }
        if (event.type == 7) {
            this.handleNewActiveNode(this.tree.getNode((Integer)event.arg));
            return;
        }
        if (event.type != 0 && event.type != 1) {
            return;
        }
        Prop prop = (Prop)event.arg;
        if (!prop.isPlayed()) {
            return;
        }
        Node node = (Node)event.source;
        Node activeNode = this.tree.getActiveNode();
        if (event.type == 0 && prop.type == 0) {
            game.setRulesAndReset(prop.getRules());
            this.playDown(game, this.tree.root, activeNode);
            return;
        }
        if (node == activeNode) {
            if (event.type == 0) {
                this.updateClocks(game, this.play(game, prop, node));
                return;
            }
            if (prop.type == 18) {
                while ((node = node.parent) != null) {
                    Prop prevTimeLeft = node.findProp(18, prop.getColor());
                    if (prevTimeLeft == null || this.clocks[prevTimeLeft.getColor()] == null) continue;
                    this.setClock(this.clocks[prevTimeLeft.getColor()], prevTimeLeft);
                    return;
                }
                if (this.clocks[prop.getColor()] != null) {
                    this.clocks[prop.getColor()].reset();
                }
            } else {
                this.undoNode(node);
                game.undo();
                this.playDown(game, node, node);
                this.updateClocks(game, 0);
            }
            return;
        }
        if (!node.isOnActivePath()) {
            return;
        }
        while (node.parent != null) {
            node = node.parent;
            if (node != activeNode) continue;
            return;
        }
        game.reset();
        this.playDown(game, this.tree.root, activeNode);
        this.updateClocks(game, 3);
    }

    private void handleNewActiveNode(Node start) {
        Game game = (Game)this.gameRef.get();
        if (game == null) {
            this.close();
            return;
        }
        int numUndosNeeded = 0;
        Node activeNode = this.tree.getActiveNode();
        if (!start.isOnActivePath() && !game.hasUndoStack()) {
            game.reset();
            this.playDown(game, this.tree.root, activeNode);
            this.updateClocks(game, 3);
            return;
        }
        while (!start.isOnActivePath()) {
            ++numUndosNeeded;
            this.undoNode(start);
            start = start.parent;
        }
        if (start != activeNode) {
            Node node = activeNode;
            while (node != null) {
                if (node == start) {
                    game.undo(numUndosNeeded);
                    this.updateClocks(game, this.playDown(game, start.getActiveChild(), activeNode));
                    return;
                }
                node = node.parent;
            }
        }
        if (!game.hasUndoStack()) {
            game.reset();
            this.playDown(game, this.tree.root, activeNode);
            this.updateClocks(game, 3);
            return;
        }
        while (start != activeNode) {
            ++numUndosNeeded;
            this.undoNode(start);
            start = start.parent;
        }
        game.undo(numUndosNeeded);
        this.updateClocks(game, 0);
    }

    private int playDown(Game game, Node start, Node end) {
        int clocksChanged = 0;
        while (true) {
            game.makeUndoBreakpoint();
            for (Prop aStart : start) {
                clocksChanged |= this.play(game, aStart, start);
            }
            if (start == end) break;
            start = start.getActiveChild();
        }
        return clocksChanged;
    }

    protected int play(Game game, Prop prop, Node node) {
        switch (prop.type) {
            case 0: {
                int result = 0;
                if (this.lastTimeProps[0] != null) {
                    result |= 1;
                }
                if (this.lastTimeProps[1] != null) {
                    result |= 2;
                }
                this.lastTimeProps[0] = null;
                this.lastTimeProps[1] = null;
                this.lastTimeNodes[0] = null;
                this.lastTimeNodes[1] = null;
                return result;
            }
            case 14: {
                game.move(prop.getColor(), prop.getLoc());
                break;
            }
            case 16: {
                game.removeStone(prop.getLoc());
                break;
            }
            case 17: {
                Loc loc = prop.getLoc();
                if (prop.getColor() == 2) {
                    game.removeStone(loc);
                    break;
                }
                if (game.getColor(loc) != 2) {
                    throw new RuntimeException("Adding stone when already there?");
                }
                game.addStone(loc, prop.getColor(), false);
                break;
            }
            case 18: {
                int color = prop.getColor();
                this.lastTimeProps[color] = prop;
                this.lastTimeNodes[color] = node;
                return 1 << color;
            }
            case 26: {
                game.setWhoseMove(prop.getColor());
                break;
            }
        }
        return 0;
    }

    public static Goban buildBoard(Tree srcTree, Node node) {
        Goban goban = new Goban(srcTree.root.findProp(0).getRules().getSize());
        Stack<Node> nodeStack = new Stack<Node>();
        do {
            nodeStack.push(node);
        } while ((node = node.parent) != null);
        while (!nodeStack.isEmpty()) {
            for (Prop prop : (Node)nodeStack.pop()) {
                switch (prop.type) {
                    case 14: {
                        if (prop.getLoc() == Loc.PASS) break;
                        goban.addStone(prop.getLoc(), prop.getColor(), false, null);
                        break;
                    }
                    case 17: {
                        Loc loc = prop.getLoc();
                        if (prop.getColor() == 2) {
                            goban.removeStone(loc);
                            break;
                        }
                        if (goban.getColor(loc) != 2) {
                            goban.removeStone(loc);
                        }
                        goban.addStone(loc, prop.getColor(), false, null);
                        break;
                    }
                    case 16: {
                        goban.removeStone(prop.getLoc());
                        break;
                    }
                }
            }
        }
        return goban;
    }

    private void close() {
        this.tree.removeListener(this.treeListener);
    }

    public final Game getGame() {
        return (Game)this.gameRef.get();
    }

    private void undoNode(Node node) {
        if (node == this.lastTimeNodes[0]) {
            this.lastTimeNodes[0] = null;
        }
        if (node == this.lastTimeNodes[1]) {
            this.lastTimeNodes[1] = null;
        }
    }

    private void updateClocks(Game game, int change) {
        for (int color = 0; color <= 1; ++color) {
            if (this.clocks[color] == null) continue;
            if (this.lastTimeNodes[color] == null && this.lastTimeProps[color] != null) {
                change |= 1 << color;
                Node node = this.tree.getActiveNode();
                while (node != null) {
                    Prop prop = node.findProp(18, color);
                    if (prop != null) {
                        this.lastTimeProps[color] = prop;
                        this.lastTimeNodes[color] = node;
                        this.setClock(this.clocks[color], prop);
                        break;
                    }
                    node = node.parent;
                }
            }
            if ((change & 1 << color) == 0) continue;
            Prop prop = this.lastTimeProps[color];
            if (prop == null) {
                this.clocks[color].reset();
                continue;
            }
            this.setClock(this.clocks[color], prop);
        }
    }

    private void setClock(Clock clock, Prop prop) {
        if (clock instanceof GoClock) {
            ((GoClock)clock).setSecs(prop.getFloat(), prop.getInt());
        } else {
            clock.setSecs(prop.getFloat());
        }
    }

    public Clock getClock(int color) {
        return this.clocks[color];
    }

    public void setClock(int color, Clock clock) {
        this.clocks[color] = clock;
    }
}
