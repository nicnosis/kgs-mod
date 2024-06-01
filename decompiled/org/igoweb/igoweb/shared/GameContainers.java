/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.DataInputStream;
import java.io.IOException;

public enum GameContainers {
    CHALLENGES,
    ACTIVES,
    FANS;


    public static GameContainers read(DataInputStream in) throws IOException {
        byte type = in.readByte();
        try {
            return GameContainers.values()[type];
        }
        catch (IndexOutOfBoundsException excep) {
            throw new IOException("Invalid GameContainer: " + type);
        }
    }
}
