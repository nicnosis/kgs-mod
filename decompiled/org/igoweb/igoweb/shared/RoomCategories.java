/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.DataInputStream;
import java.io.IOException;
import org.igoweb.igoweb.shared.TxMessage;

public enum RoomCategories {
    MAIN,
    TEMPORARY,
    CLUBS,
    LESSONS,
    TOURNAMENT,
    FRIENDLY,
    NATIONAL,
    SPECIAL;


    public void write(TxMessage out) {
        out.writeByte(this == SPECIAL ? this.ordinal() + 1 : this.ordinal());
    }

    public boolean isVisible() {
        return this != SPECIAL;
    }

    public static RoomCategories read(DataInputStream in) throws IOException {
        byte id = in.readByte();
        if (id < 0 || id >= SPECIAL.ordinal()) {
            if (id == SPECIAL.ordinal() + 1) {
                return SPECIAL;
            }
            throw new IOException("Invalid room category: " + id);
        }
        return RoomCategories.values()[id];
    }
}
