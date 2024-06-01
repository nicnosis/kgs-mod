/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.sgf;

import java.util.Iterator;
import java.util.Stack;
import org.igoweb.go.sgf.Node;

public static class Node.NodeIterator
extends Stack<Node>
implements Iterator<Node> {
    public Node.NodeIterator(Node root) {
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
