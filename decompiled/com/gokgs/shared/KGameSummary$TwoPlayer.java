/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.shared;

import com.gokgs.shared.KGameSummary;
import com.gokgs.shared.KRole;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import org.igoweb.go.Rules;
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.User;

public static class KGameSummary.TwoPlayer<UserT extends User>
extends KGameSummary<UserT> {
    private UserT black;
    private UserT white;
    private final GameType gameType;

    protected KGameSummary.TwoPlayer(DataInputStream in, GameType newGameType, GameSummary.StreamUserLoader<UserT> userLoader) throws IOException {
        super(in, newGameType.roleMask, userLoader);
        this.gameType = newGameType;
    }

    protected KGameSummary.TwoPlayer(ResultSet rs, GameType newGameType, GameSummary.DbUserLoader<UserT> userLoader) throws SQLException {
        super(rs, userLoader);
        this.gameType = newGameType;
    }

    public KGameSummary.TwoPlayer(long newId, GameType gt, Rules rules, boolean isPrivate, UserT newWhite, UserT newBlack, HashMap<String, Integer> revisionMap) throws GameSummary.RevisionLimitException {
        super(newId, rules.getSize(), rules.getHandicap(), rules.getKomi(), isPrivate, 16387);
        this.gameType = gt;
        this.black = newBlack;
        this.white = newWhite;
        this.setRevision(revisionMap);
    }

    @Override
    public GameType getGameType() {
        return this.gameType;
    }

    @Override
    public UserT getPlayer(Role role) {
        return (UserT)(role == KRole.BLACK ? this.black : (role == KRole.WHITE ? this.white : null));
    }

    @Override
    public Role getRole(String name) {
        return name.equals(((User)this.black).name) ? KRole.BLACK : (name.equals(((User)this.white).name) ? KRole.WHITE : null);
    }

    @Override
    protected void setPlayer(Role role, UserT player) {
        if (role == KRole.BLACK && this.black == null) {
            this.black = player;
        } else if (role == KRole.WHITE && this.white == null) {
            this.white = player;
        } else {
            throw new IllegalStateException("Got setPlayer(role=" + role + ", player=" + player + ") when players=" + this.white + "-" + this.black);
        }
    }

    @Override
    public boolean isFinished() {
        return this.getScore() != 16387;
    }

    @Override
    public String getLocalDesc(IBundle bundle) {
        return bundle.str(-1337055791, new Object[]{((User)this.white).getNameAndRank(bundle), ((User)this.black).getNameAndRank(bundle)});
    }
}
