/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.shared;

import com.gokgs.shared.KGameSummary;
import com.gokgs.shared.KGameType;
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

public static class KGameSummary.Demo<UserT extends User>
extends KGameSummary<UserT> {
    private UserT owner;

    protected KGameSummary.Demo(ResultSet rs, GameSummary.DbUserLoader<UserT> userLoader) throws SQLException {
        super(rs, userLoader);
    }

    protected KGameSummary.Demo(DataInputStream in, GameSummary.StreamUserLoader<UserT> userLoader) throws IOException {
        super(in, KGameType.DEMONSTRATION.roleMask, userLoader);
    }

    public KGameSummary.Demo(long newId, Rules rules, boolean isPrivate, UserT newOwner, HashMap<String, Integer> revisionMap) throws GameSummary.RevisionLimitException {
        super(newId, rules.getSize(), rules.getHandicap(), rules.getKomi(), isPrivate, 16387);
        this.owner = newOwner;
        this.setRevision(revisionMap);
    }

    protected KGameSummary.Demo(long newId, KGameSummary<UserT> original, boolean isPrivate, UserT newOwner, HashMap<String, Integer> revisionMap) throws GameSummary.RevisionLimitException {
        super(newId, original.getBoardSize(), original.getHandicap(), original.getKomi(), isPrivate, 16387);
        this.owner = newOwner;
        this.setRevision(revisionMap);
    }

    @Override
    public GameType getGameType() {
        return KGameType.DEMONSTRATION;
    }

    @Override
    public UserT getPlayer(Role role) {
        return role == KRole.OWNER ? (UserT)this.owner : null;
    }

    @Override
    public Role getRole(String name) {
        return name.equals(((User)this.owner).name) ? KRole.OWNER : null;
    }

    @Override
    protected void setPlayer(Role role, UserT player) {
        if (role != KRole.OWNER || this.owner != null) {
            throw new IllegalStateException("Got setPlayer(role=" + role + ", player=" + player + ") when owner=" + this.owner);
        }
        this.owner = player;
    }

    @Override
    public String getLocalDesc(IBundle bundle) {
        return ((User)this.owner).getNameAndRank(bundle);
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
