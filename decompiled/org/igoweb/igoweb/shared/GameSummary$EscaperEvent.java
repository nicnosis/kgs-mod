/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import org.igoweb.igoweb.shared.Role;
import org.igoweb.util.Event;

public static class GameSummary.EscaperEvent
extends Event {
    public final boolean newVal;

    public GameSummary.EscaperEvent(Object newSource, Role role, boolean newNewVal) {
        super(newSource, 5, role);
        this.newVal = newNewVal;
    }
}
