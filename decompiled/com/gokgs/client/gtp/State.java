/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client.gtp;

import com.gokgs.client.KCChallenge;
import com.gokgs.client.KCProposal;
import com.gokgs.client.gtp.Options;
import java.util.LinkedList;
import org.igoweb.go.gtp.Protocol;
import org.igoweb.igoweb.client.CArchive;
import org.igoweb.igoweb.client.Client;
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.User;

public class State {
    private static final Object KEY = new Object();
    public final Client client;
    public final Protocol protocol;
    public final Options options;
    public final LinkedList<KCProposal> proposals = new LinkedList();
    private CArchive archive;
    public KCChallenge myChal;

    private State(Client newClient, Protocol newProtocol, Options newOptions) {
        this.client = newClient;
        this.protocol = newProtocol;
        this.options = newOptions;
    }

    public static void create(Client newClient, Protocol newProtocol, Options newOptions) {
        newClient.objects.put(KEY, new State(newClient, newProtocol, newOptions));
    }

    public static State get(Client clientRef) {
        return (State)clientRef.objects.get(KEY);
    }

    public boolean canPlayRanked() {
        return this.canPlayRanked(false);
    }

    public boolean canPlayRanked(boolean verbose) {
        if (verbose) {
            if (this.client.getMe().getAuthLevel() != 1) {
                this.options.logger.fine("Auth level is not ROBOT_RANKED, can only play free games");
            }
            if (!this.protocol.isCommandSupported("final_status_list")) {
                this.options.logger.fine("Engine does not support final_status_list, can only play free games");
            }
        }
        return this.client.getMe().getAuthLevel() == 1 && this.protocol.isCommandSupported("final_status_list");
    }

    public void setArchive(CArchive newArchive) {
        this.archive = newArchive;
    }

    public boolean isEscaper(User user) {
        for (GameSummary<?> game : this.archive.getGames()) {
            if (!game.getGameType().isRanked() || game.getScore() != 16387 || !game.getGameType().isMainRole(game.getRole(user.name))) continue;
            return true;
        }
        return false;
    }
}
