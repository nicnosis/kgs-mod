/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.DataInputStream;
import java.io.IOException;

public enum ChatMode {
    NORMAL,
    QUIET,
    MODERATED;


    public static ChatMode read(DataInputStream in) throws IOException {
        byte value = in.readByte();
        try {
            return ChatMode.values()[value];
        }
        catch (ArrayIndexOutOfBoundsException excep) {
            throw new IOException("Invalid chat mode: " + value);
        }
    }
}
