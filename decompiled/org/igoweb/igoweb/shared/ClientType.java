/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.DataInput;
import java.io.IOException;
import java.util.Locale;

public enum ClientType {
    JAVAWS,
    STANDALONE,
    APPLET,
    GTP,
    ROOM_MANAGER,
    UNIT_TEST,
    STRESS_TEST,
    ANDROID,
    HTML,
    JSON,
    EXTRA_1,
    EXTRA_2,
    EXTRA_3;

    public final String sqlName = this.toString().toLowerCase(Locale.US);

    public static ClientType get(DataInput in) throws IOException {
        short val = in.readShort();
        try {
            return ClientType.values()[val];
        }
        catch (IndexOutOfBoundsException excep) {
            throw new IOException("Bogus client type " + val);
        }
    }
}
