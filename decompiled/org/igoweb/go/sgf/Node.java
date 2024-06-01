/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.sgf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import org.igoweb.go.Game;
import org.igoweb.go.Loc;
import org.igoweb.go.Scorer;
import org.igoweb.go.sgf.Prop;
import org.igoweb.go.sgf.SgfEvent;
import org.igoweb.util.Emitter;
import org.igoweb.util.Event;

public class Node
extends Emitter
implements Iterable<Prop> {
    public final Node parent;
    public final int id;
    private static final int DEFAULT_PROPS_SIZE = 4;
    private Prop[] props;
    private int numProps;
    private ArrayList<Node> children;
    private Node activeChild;
    private int moveNum = 0;
    private boolean isOnActivePath;
    private boolean isActive;
    private static final List<Node> NO_CHILDREN = Collections.unmodifiableList(new ArrayList());

    Node(Node parent, int insertionOrder, int id) {
        this.parent = parent;
        this.id = id;
        this.props = new Prop[4];
        if (parent == null) {
            this.isOnActivePath = true;
            this.isActive = true;
        } else {
            this.isActive = false;
            ArrayList<Node> parentChildren = parent.children;
            if (parentChildren == null) {
                if (parent.activeChild == null) {
                    insertionOrder = 0;
                } else {
                    parentChildren = new ArrayList();
                    parent.children = parentChildren;
                    if (insertionOrder == 0) {
                        parentChildren.add(this);
                        parentChildren.add(parent.activeChild);
                    } else {
                        insertionOrder = 1;
                        parentChildren.add(parent.activeChild);
                        parentChildren.add(this);
                    }
                }
            } else if (insertionOrder < 0) {
                insertionOrder = parentChildren.size();
                parentChildren.add(this);
            } else {
                parentChildren.add(insertionOrder, this);
            }
            this.moveNum = parent.moveNum;
            if (parent.activeChild == null) {
                parent.activeChild = this;
                this.isOnActivePath = parent.isOnActivePath;
            } else {
                this.isOnActivePath = false;
            }
        }
    }

    public final boolean add(Prop prop) {
        return this.add(prop, false, false);
    }

    public final boolean add(Prop prop, boolean appendText) {
        return this.add(prop, appendText, false);
    }

    public boolean add(Prop prop, boolean appendText, boolean skipOnConflict) {
        if (this.parent != null && prop.isRoot()) {
            throw new IllegalNodeError(prop);
        }
        if (prop.type == 17 && prop.getColor() != 2) {
            this.add(new Prop(16, prop.getLoc()), appendText, skipOnConflict);
        }
        int insertionPoint = -1;
        for (int i = 0; i < this.numProps; ++i) {
            Prop oldProp = this.props[i];
            if (prop.conflictsWith(oldProp)) {
                if (appendText && prop.type == oldProp.type && prop.hasText()) {
                    this.props[i] = new Prop(oldProp, prop.getText());
                    this.emit(2, prop);
                    return true;
                }
                if (prop.equals(oldProp) || skipOnConflict) {
                    return false;
                }
                if (i < --this.numProps) {
                    System.arraycopy(this.props, i + 1, this.props, i, this.numProps - i);
                }
                this.emit(1, oldProp);
                --i;
                continue;
            }
            if (insertionPoint != -1 || prop.compareTo(oldProp) >= 0) continue;
            insertionPoint = i;
        }
        if (prop.type == 14) {
            this.changeMoveNum(1);
        }
        if (this.props.length == this.numProps) {
            Prop[] newProps = new Prop[this.numProps * 2];
            System.arraycopy(this.props, 0, newProps, 0, this.numProps);
            this.props = newProps;
        }
        if (insertionPoint == -1) {
            this.props[this.numProps++] = prop;
        } else {
            System.arraycopy(this.props, insertionPoint, this.props, insertionPoint + 1, this.numProps - insertionPoint);
            this.props[insertionPoint] = prop;
            ++this.numProps;
        }
        this.emit(0, prop);
        return true;
    }

    public boolean contains(Prop p) {
        for (int i = 0; i < this.numProps; ++i) {
            if (!this.props[i].equals(p)) continue;
            return true;
        }
        return false;
    }

    public final Node getActiveChild() {
        return this.activeChild;
    }

    void setActive(boolean isActive, Node prevActive) {
        if (this.isActive != isActive) {
            this.isActive = isActive;
            if (isActive) {
                Node node = this;
                while (!node.isOnActivePath) {
                    node.parent.setActiveChild(node);
                    node = node.parent;
                }
            }
            this.emit(3);
            if (isActive) {
                this.emit(7, new Integer(prevActive.id));
            }
        }
    }

    public final boolean isActive() {
        return this.isActive;
    }

    public List<Node> children() {
        return this.children == null ? (this.activeChild == null ? NO_CHILDREN : Collections.singletonList(this.activeChild)) : this.children;
    }

    public final Node getChild(int childNum) {
        return this.children == null ? this.activeChild : this.children.get(childNum);
    }

    public int indexOfChild(Node child) {
        if (this.children == null) {
            return this.activeChild == child ? 0 : -1;
        }
        return this.children.indexOf(child);
    }

    public final int countChildren() {
        return this.children == null ? (this.activeChild == null ? 0 : 1) : this.children.size();
    }

    private void changeMoveNum(int delta) {
        NodeIterator iter = new NodeIterator(this);
        while (iter.hasNext()) {
            Node node = iter.nextNode();
            node.moveNum += delta;
            node.emit(3);
        }
    }

    public int size() {
        return this.numProps;
    }

    public boolean isEmpty() {
        return this.numProps == 0;
    }

    @Override
    public final Iterator<Prop> iterator() {
        return new PropIter();
    }

    public final int getMoveNum() {
        return this.moveNum;
    }

    public Prop findProp(int propType) {
        for (int i = 0; i < this.numProps; ++i) {
            if (this.props[i].type != propType) continue;
            return this.props[i];
        }
        return null;
    }

    public Prop findProp(int propType, int color) {
        for (int i = 0; i < this.numProps; ++i) {
            if (this.props[i].type != propType || this.props[i].getColor() != color) continue;
            return this.props[i];
        }
        return null;
    }

    public Prop findProp(int propType, Loc loc) {
        for (int i = 0; i < this.numProps; ++i) {
            if (this.props[i].type != propType || this.props[i].getLoc() != loc) continue;
            return this.props[i];
        }
        return null;
    }

    public boolean remove(Prop prop) {
        for (int i = 0; i < this.numProps; ++i) {
            if (!this.props[i].equals(prop)) continue;
            while (++i < this.numProps) {
                this.props[i - 1] = this.props[i];
            }
            --this.numProps;
            this.emit(1, prop);
            return true;
        }
        return false;
    }

    public boolean hasConflicts(Prop prop) {
        for (int i = 0; i < this.numProps; ++i) {
            if (!prop.conflictsWith(this.props[i])) continue;
            return true;
        }
        return false;
    }

    public boolean removeConflicts(Prop prop) {
        Prop[] removals = null;
        int numRemovals = 0;
        for (int i = 0; i < this.numProps; ++i) {
            if (prop.conflictsWith(this.props[i])) {
                if (removals == null) {
                    removals = new Prop[this.numProps];
                }
                removals[numRemovals++] = this.props[i];
                continue;
            }
            if (numRemovals <= 0) continue;
            this.props[i - numRemovals] = this.props[i];
        }
        while (--numRemovals >= 0) {
            this.emit(1, removals[numRemovals]);
        }
        return removals != null;
    }

    public boolean removeType(int propType) {
        Prop oldProp = this.findProp(propType);
        if (oldProp != null) {
            this.remove(oldProp);
            return true;
        }
        return false;
    }

    public void setActiveChild(Node newActiveChild) {
        if (newActiveChild != this.activeChild) {
            Node node;
            if (this.isOnActivePath) {
                node = this.activeChild;
                while (node != null) {
                    node.isOnActivePath = false;
                    node.isActive = false;
                    node.emit(3);
                    node = node.activeChild;
                }
            }
            this.activeChild = newActiveChild;
            if (this.isOnActivePath) {
                node = this.activeChild;
                while (node != null) {
                    node.isOnActivePath = true;
                    node.emit(3);
                    node = node.activeChild;
                }
            }
        }
    }

    public final boolean isOnActivePath() {
        return this.isOnActivePath;
    }

    public void moveActiveChild(int distance) {
        if (distance != -1 && distance != 1) {
            throw new IllegalArgumentException("Distance must be 1 or -1");
        }
        int acIndex = this.getActiveChildIndex();
        int peerIndex = acIndex + distance;
        if (peerIndex < 0 || peerIndex > this.countChildren()) {
            throw new IllegalArgumentException("Can't move a node that far");
        }
        this.children.set(acIndex, this.children.get(peerIndex));
        this.children.set(peerIndex, this.activeChild);
        int[] childIds = new int[this.children.size()];
        for (int i = 0; i < childIds.length; ++i) {
            childIds[i] = this.getChild((int)i).id;
        }
        this.emit(4, childIds);
    }

    public void reorderChildren(ArrayList<Node> newList) {
        if (newList.size() != this.countChildren()) {
            throw new IllegalArgumentException();
        }
        boolean change = false;
        for (int i = 0; i < newList.size(); ++i) {
            Node child = newList.get(i);
            if (this.indexOfChild(child) == -1 || newList.indexOf(child) != i) {
                throw new IllegalArgumentException();
            }
            if (change || this.children().get(i) == child) continue;
            change = true;
        }
        if (change) {
            this.children.clear();
            this.children.addAll(newList);
            int[] childIds = new int[this.children.size()];
            for (int i = 0; i < childIds.length; ++i) {
                childIds[i] = this.getChild((int)i).id;
            }
            this.emit(4, childIds);
        }
    }

    public Node findNextChild(int direction) {
        if (this.children == null) {
            return null;
        }
        int newActive = this.getActiveChildIndex() + direction;
        if (newActive < 0) {
            newActive = this.children.size() - 1;
        } else if (newActive == this.children.size()) {
            newActive = 0;
        }
        return this.children.get(newActive);
    }

    private int getActiveChildIndex() {
        return this.children == null ? 0 : this.children.indexOf(this.activeChild);
    }

    void cutFromParent() {
        ArrayList<Node> pc = this.parent.children;
        if (this.parent.activeChild == this) {
            if (pc == null) {
                this.parent.activeChild = null;
                this.emit(6);
                return;
            }
            this.parent.setActiveChild(this.parent.findNextChild(1));
        }
        pc.remove(this);
        if (pc.size() == 1) {
            this.parent.activeChild = pc.get(0);
            this.parent.children = null;
        }
        this.emit(6);
    }

    public boolean isAnnotated() {
        Loc circleLoc = null;
        Loc moveLoc = null;
        for (int i = 0; i < this.numProps; ++i) {
            Prop childMove;
            int charNum;
            Prop prop = this.props[i];
            if (prop.type == 14) {
                moveLoc = prop.getLoc();
                continue;
            }
            if (!prop.isAnnotation()) continue;
            if (prop.type == 15 && circleLoc == null) {
                circleLoc = prop.getLoc();
                continue;
            }
            if (prop.type == 19 && prop.getText().length() == 1 && (charNum = prop.getText().charAt(0) - 65) >= 0 && charNum < this.countChildren() && (childMove = this.getChild(charNum).findProp(14)) != null && childMove.getLoc() == prop.getLoc()) continue;
            return true;
        }
        return circleLoc != null && moveLoc != circleLoc;
    }

    public void score(Game game, Collection<Prop> addedProps, Collection<Prop> removedProps) {
        Prop p;
        if (addedProps == null) {
            addedProps = new ArrayList<Prop>();
        }
        if (removedProps == null) {
            removedProps = new ArrayList<Prop>();
        }
        HashSet<Loc> currentTerritory = this.addNeededDeadMarks(game, addedProps, removedProps);
        for (Prop p2 : removedProps) {
            if (p2.type != 23) continue;
            currentTerritory.remove(p2.getLoc());
        }
        for (Prop p2 : addedProps) {
            if (p2.type != 23) continue;
            currentTerritory.add(p2.getLoc());
        }
        Scorer scorer = new Scorer(game, currentTerritory);
        HashSet<Prop> territoryProps = new HashSet<Prop>();
        for (int i = 0; i < this.numProps; ++i) {
            p = this.props[i];
            if (p.type != 22) continue;
            territoryProps.add(p);
        }
        Iterator<Loc> locs = scorer.getTerritoryLocs(0).iterator();
        while (locs.hasNext()) {
            p = new Prop(22, 0, locs.next());
            if (territoryProps.remove(p)) continue;
            addedProps.add(p);
        }
        locs = scorer.getTerritoryLocs(1).iterator();
        while (locs.hasNext()) {
            p = new Prop(22, 1, locs.next());
            if (territoryProps.remove(p)) continue;
            addedProps.add(p);
        }
        removedProps.addAll(territoryProps);
    }

    private HashSet<Loc> addNeededDeadMarks(Game game, Collection<Prop> newDeadMarks, Collection<Prop> keepAliveMarks) {
        HashSet<Loc> notDeadLocs = new HashSet<Loc>();
        for (Prop p : keepAliveMarks) {
            if (p.type != 23) continue;
            notDeadLocs.add(p.getLoc());
        }
        HashSet<Loc> deadLocs = new HashSet<Loc>();
        HashSet<Loc> territoryOnDead = new HashSet<Loc>();
        for (int i = 0; i < this.numProps; ++i) {
            int boardColor;
            Loc loc;
            Prop prop = this.props[i];
            if (prop.type == 23) {
                loc = prop.getLoc();
                if (!notDeadLocs.contains(loc)) {
                    deadLocs.add(loc);
                }
                territoryOnDead.remove(loc);
                continue;
            }
            if (prop.type != 22 || (boardColor = game.getColor(loc = prop.getLoc())) != 0 && boardColor != 1 || notDeadLocs.contains(loc)) continue;
            deadLocs.add(loc);
            territoryOnDead.add(loc);
        }
        Iterator iter = territoryOnDead.iterator();
        while (iter.hasNext()) {
            newDeadMarks.add(new Prop(23, (Loc)iter.next()));
        }
        return deadLocs;
    }

    @Override
    protected Event buildEvent(int type, Object arg) {
        return new SgfEvent(this, this.id, type, arg);
    }

    void emitChildAdded(int[] args) {
        super.emit(5, args);
    }

    public String toString() {
        return "Node[id=" + this.id + "]";
    }

    private class PropIter
    implements Iterator<Prop> {
        private int i = -1;
        private int origSize;

        public PropIter() {
            this.origSize = Node.this.numProps;
        }

        @Override
        public Prop next() {
            if (Node.this.numProps != this.origSize) {
                throw new RuntimeException();
            }
            return Node.this.props[++this.i];
        }

        @Override
        public boolean hasNext() {
            if (Node.this.numProps != this.origSize) {
                throw new RuntimeException();
            }
            return this.i + 1 < this.origSize;
        }

        @Override
        public void remove() {
            if (Node.this.numProps != this.origSize) {
                throw new RuntimeException();
            }
            Prop victim = Node.this.props[this.i];
            for (int j = this.i + 1; j < this.origSize; ++j) {
                ((Node)Node.this).props[j - 1] = Node.this.props[j];
            }
            --this.origSize;
            --Node.this.numProps;
            --this.i;
            Node.this.emit(1, victim);
            if (victim.type == 17 && Node.this.remove(new Prop(16, victim.getLoc()))) {
                --this.i;
                --this.origSize;
            }
        }
    }

    public class IllegalNodeError
    extends RuntimeException {
        public final Prop prop;

        public IllegalNodeError(Prop prop) {
            super("Cannot add " + prop + " to non-root node.");
            this.prop = prop;
        }
    }

    public static class NodeIterator
    extends Stack<Node>
    implements Iterator<Node> {
        public NodeIterator(Node root) {
            this.push(root);
        }

        @Override
        public boolean hasNext() {
            return !this.isEmpty();
        }

        @Override
        public Node next() {
            return this.nextNode();
        }

        public Node nextNode() {
            Node result = (Node)this.pop();
            for (int i = result.countChildren() - 1; i >= 0; --i) {
                this.push(result.getChild(i));
            }
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
