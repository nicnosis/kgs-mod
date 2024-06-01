/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.DataInputStream;
import java.io.IOException;
import org.igoweb.igoweb.client.CGameListEntry;

public static interface CGameContainer.GameReader {
    public CGameListEntry readGame(DataInputStream var1) throws IOException;
}
