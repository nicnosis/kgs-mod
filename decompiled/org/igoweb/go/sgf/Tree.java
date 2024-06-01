/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.sgf;

import java.util.ArrayList;
import java.util.Collection;
import org.igoweb.go.Loc;
import org.igoweb.go.sgf.Node;
import org.igoweb.go.sgf.Prop;
import org.igoweb.go.sgf.SgfEvent;
import org.igoweb.util.EventListener;
import org.igoweb.util.IntHashMap;
import org.igoweb.util.Multicaster;

public class Tree {
    public final Node root;
    private Node activeNode;
    private int lastNodeId;
    private IntHashMap<Node> nodeIdToNode;
    private final Multicaster nodeRelay;

    public Tree() {
        this.activeNode = this.root = new Node(null, -1, 0);
        this.nodeIdToNode = new IntHashMap();
        this.nodeRelay = new Multicaster.External();
        this.nodeIdToNode.put(0, this.root);
    }

    public Tree(Tree original) {
        this();
        for (SgfEvent sgfEvent : original.asEventList()) {
            this.apply(sgfEvent);
        }
    }

    public void setActiveNode(Node newActiveNode) {
        if (newActiveNode != this.activeNode) {
            Node prevActiveNode = this.activeNode;
            this.activeNode = newActiveNode;
            prevActiveNode.setActive(false, null);
            newActiveNode.setActive(true, prevActiveNode);
        }
    }

    public final Node.NodeIterator nodes() {
        return new Node.NodeIterator(this.root);
    }

    public Node addNode() {
        Node newNode = this.addNode(this.activeNode);
        this.setActiveNode(newNode);
        return newNode;
    }

    public Node addNode(Node parent) {
        return this.addNode(parent, -1);
    }

    public Node addNode(Node parent, int insertionOrder, int newNodeId) {
        if (newNodeId < 0 || this.nodeIdToNode.containsKey(newNodeId)) {
            throw new IllegalArgumentException();
        }
        this.lastNodeId = newNodeId - 1;
        return this.addNode(parent, insertionOrder);
    }

    public Node addNode(Node parent, int insertionOrder) {
        int id = this.lastNodeId + 1 & Integer.MAX_VALUE;
        while (this.nodeIdToNode.containsKey(id)) {
            ++id;
        }
        this.lastNodeId = id;
        Node newNode = new Node(parent, insertionOrder, id);
        if (!this.nodeRelay.isEmpty()) {
            newNode.addListener(this.nodeRelay);
        }
        this.nodeIdToNode.put(id, newNode);
        parent.emitChildAdded(new int[]{id, parent.children().indexOf(newNode)});
        return newNode;
    }

    public Node getActiveNode() {
        return this.activeNode;
    }

    public Node getNode(int id) {
        return this.nodeIdToNode.get(id);
    }

    public Node findNodeContainingMove(Loc loc, boolean forwardFirst) {
        Node result = null;
        if (forwardFirst) {
            result = this.findMove(loc, true);
        }
        if (result == null) {
            result = this.findMove(loc, false);
        }
        return result;
    }

    private Node findMove(Loc loc, boolean searchForward) {
        Node node = this.activeNode;
        while (node != null) {
            Prop param = node.findProp(14);
            if (param != null && param.getLoc().equals(loc)) {
                return node;
            }
            node = searchForward ? node.getActiveChild() : node.parent;
        }
        return null;
    }

    public final boolean add(Prop param) {
        return this.activeNode.add(param, false);
    }

    public final boolean add(Prop param, boolean appendText) {
        return this.activeNode.add(param, appendText);
    }

    public void delete(Node node) {
        Node activeParent = this.activeNode;
        while (activeParent != null && activeParent != node) {
            activeParent = activeParent.parent;
        }
        if (activeParent == node) {
            this.setActiveNode(activeParent.parent);
        }
        Node.NodeIterator victims = new Node.NodeIterator(node);
        while (victims.hasNext()) {
            this.nodeIdToNode.remove((Object)victims.nextNode().id);
        }
        node.cutFromParent();
        victims = new Node.NodeIterator(node);
        while (victims.hasNext()) {
            victims.nextNode().removeListener(this.nodeRelay);
        }
    }

    public Node insertCopy(Node node) {
        Node newNode = this.insertCopy(node, this.activeNode);
        this.setActiveNode(newNode);
        return newNode;
    }

    public Node insertCopy(Node newNode, Node parent) {
        Node peer = this.addNode(parent);
        for (Prop aNewNode : newNode) {
            peer.add(aNewNode);
        }
        for (Node props : newNode.children()) {
            this.insertCopy(props, peer);
        }
        return peer;
    }

    public void addListener(EventListener newListener) {
        if (this.nodeRelay.isEmpty()) {
            Node.NodeIterator nodes = this.nodes();
            while (nodes.hasNext()) {
                nodes.nextNode().addListener(this.nodeRelay);
            }
        }
        this.nodeRelay.add(newListener);
    }

    public void removeListener(EventListener victim) {
        this.nodeRelay.remove(victim);
        if (this.nodeRelay.isEmpty()) {
            Node.NodeIterator nodes = this.nodes();
            while (nodes.hasNext()) {
                nodes.nextNode().removeListener(this.nodeRelay);
            }
        }
    }

    public void apply(SgfEvent event) {
        Node src = this.getNode(event.srcId);
        switch (event.type) {
            case 0: {
                src.add((Prop)event.arg);
                break;
            }
            case 1: {
                src.remove((Prop)event.arg);
                break;
            }
            case 2: {
                src.add((Prop)event.arg, true);
                break;
            }
            case 4: {
                int[] args = (int[])event.arg;
                ArrayList<Node> children = new ArrayList<Node>();
                for (int arg : args) {
                    children.add(this.getNode(arg));
                }
                src.reorderChildren(children);
                break;
            }
            case 5: {
                int[] args = (int[])event.arg;
                this.addNode(src, args[1], args[0]);
                break;
            }
            case 6: {
                this.delete(src);
                break;
            }
            case 7: {
                this.setActiveNode(src);
                break;
            }
            case 8: {
                for (Object o : (Collection)event.arg) {
                    src.add((Prop)o);
                }
                break;
            }
            case 9: {
                for (Object o : (Collection)event.arg) {
                    src.remove((Prop)o);
                }
                break;
            }
        }
    }

    public ArrayList<SgfEvent> asEventList() {
        ArrayList<SgfEvent> result = new ArrayList<SgfEvent>();
        Node.NodeIterator nodes = this.nodes();
        while (nodes.hasNext()) {
            Node node = nodes.nextNode();
            if (node.parent != null) {
                result.add(new SgfEvent(node.parent, node.parent.id, 5, new int[]{node.id, node.parent.children().indexOf(node)}));
            }
            if (node.isEmpty()) continue;
            ArrayList<Prop> props = new ArrayList<Prop>(node.size());
            for (Prop aNode : node) {
                props.add(aNode);
            }
            result.add(new SgfEvent(node, node.id, 8, props));
        }
        result.add(new SgfEvent(this.getActiveNode(), this.getActiveNode().id, 7, 0));
        return result;
    }

    public void addHandicapStones(int size, int handicap) {
        if (handicap >= size * size) {
            handicap = size * size - 1;
        }
        if (handicap > 9) {
            handicap = 9;
        }
        int lo = 3;
        if (size <= 10) {
            lo = 2;
            if (size <= 8) {
                lo = 1;
                if (size <= 4) {
                    lo = 0;
                }
            }
        }
        int mid = size / 2;
        int hi = size - lo - 1;
        this.add(new Prop(17, 0, Loc.get(hi, lo)));
        this.add(new Prop(17, 0, Loc.get(lo, hi)));
        if (handicap >= 3) {
            this.add(new Prop(17, 0, Loc.get(hi, hi)));
        }
        if (handicap >= 4) {
            this.add(new Prop(17, 0, Loc.get(lo, lo)));
            if ((handicap & 1) == 1) {
                this.add(new Prop(17, 0, Loc.get(mid, mid)));
            }
        }
        if (handicap >= 6) {
            this.add(new Prop(17, 0, Loc.get(lo, mid)));
            this.add(new Prop(17, 0, Loc.get(hi, mid)));
        }
        if (handicap >= 8) {
            this.add(new Prop(17, 0, Loc.get(mid, lo)));
            this.add(new Prop(17, 0, Loc.get(mid, hi)));
        }
    }

    public int size() {
        return this.nodeIdToNode.size();
    }
}
