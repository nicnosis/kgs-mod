/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

public static class Proposal.NoSuchUserException
extends Exception {
    public final String name;

    public Proposal.NoSuchUserException(String newName) {
        this.name = newName;
    }
}
