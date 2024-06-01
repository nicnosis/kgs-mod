/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client;

import com.gokgs.shared.KGameSummary;
import java.io.DataInputStream;
import java.io.IOException;
import org.igoweb.igoweb.client.CArchive;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.GameSummary;

public class KCArchive
extends CArchive {
    public KCArchive(DataInputStream in, Conn conn, boolean isTagArchive) throws IOException {
        super(in, conn, isTagArchive);
    }

    @Override
    protected GameSummary<?> loadGameSummary(DataInputStream in) throws IOException {
        return KGameSummary.load(in);
    }
}
