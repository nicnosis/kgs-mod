/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.sgf;

import org.igoweb.go.sgf.Prop;

public class Node.IllegalNodeError
extends RuntimeException {
    public final Prop prop;

    public Node.IllegalNodeError(Prop prop) {
        super("Cannot add " + prop + " to non-root node.");
        this.prop = prop;
    }
}
