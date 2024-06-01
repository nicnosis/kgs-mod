/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.DataInputStream;
import java.io.IOException;
import org.igoweb.igoweb.shared.TxMessage;

public static enum MsgTypesDown.GamePrep {
    END,
    TOURNAMENT,
    AUTOMATCH;


    public static MsgTypesDown.GamePrep read(DataInputStream in) throws IOException {
        byte id = in.readByte();
        try {
            return MsgTypesDown.GamePrep.values()[id];
        }
        catch (IndexOutOfBoundsException excep) {
            throw new IOException("Illegal GamePrep: " + id);
        }
    }

    public void writeTo(TxMessage tx) {
        tx.writeByte(this.ordinal());
    }
}
