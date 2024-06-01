/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.games;

import org.igoweb.resource.ResEntry;
import org.igoweb.resource.Resource;

public class GameRes
extends Resource {
    private static final int BASE = -900134593;
    public static final int NN_NN = -900134593;
    private static final ResEntry[] contents = new ResEntry[]{new ResEntry("nn:nn", -900134593, "{0}:{1,number,00}", "This is the system for showing a time amount less than an hour.", new Object[][]{{new Integer(5), new Integer(15)}, {new Integer(0), new Integer(2)}})};

    @Override
    public String propFilePath() {
        return "org/igoweb/games/res/Res";
    }

    public String toString() {
        return "Game Resources";
    }

    @Override
    public ResEntry[] getContents() {
        return contents;
    }
}
