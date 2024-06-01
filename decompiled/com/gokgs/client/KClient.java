/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client;

import com.gokgs.client.KCArchive;
import com.gokgs.client.KCChallenge;
import com.gokgs.client.KCGame;
import com.gokgs.client.KCPlayback;
import com.gokgs.shared.KGameSummary;
import com.gokgs.shared.KGameType;
import java.io.DataInputStream;
import java.io.IOException;
import org.igoweb.igoweb.client.CArchive;
import org.igoweb.igoweb.client.CGameListEntry;
import org.igoweb.igoweb.client.CPlayback;
import org.igoweb.igoweb.client.Client;
import org.igoweb.igoweb.client.ConnMutex;
import org.igoweb.igoweb.client.Connector;
import org.igoweb.igoweb.shared.ClientType;
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.User;

public class KClient
extends Client {
    public KClient(String userName, String password, ClientType clientType, long clientId, ConnMutex connMutex, Connector connector) {
        super(userName, password, clientType, clientId, connMutex, connector);
    }

    @Override
    protected CArchive loadArchive(DataInputStream in, boolean isTagArchive) throws IOException {
        return new KCArchive(in, this.conn, isTagArchive);
    }

    @Override
    public CGameListEntry loadGame(DataInputStream in, int gameId, GameType gameType) throws IOException {
        if (gameType == GameType.CHALLENGE) {
            return new KCChallenge(this.conn, gameId, in);
        }
        return new KCGame(this.conn, gameType, gameId, in);
    }

    @Override
    protected GameSummary<User> loadGameSummary(DataInputStream in) throws IOException {
        return KGameSummary.load(in);
    }

    @Override
    protected CPlayback buildPlayback(int channelId, long length, GameSummary<User> gameSummary) {
        return new KCPlayback(this.conn, channelId, length, gameSummary);
    }

    static {
        KGameType.DEMONSTRATION.toString();
    }
}
