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
import java.util.ArrayList;
import java.util.HashMap;
import org.igoweb.go.Rules;
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.User;

public static class KGameSummary.Rengo<UserT extends User>
extends KGameSummary<UserT> {
    private ArrayList<UserT> users;

    protected KGameSummary.Rengo(ResultSet rs, GameSummary.DbUserLoader<UserT> userLoader) throws SQLException {
        super(rs, userLoader);
    }

    public KGameSummary.Rengo(long newId, Rules rules, boolean isPrivate, UserT white1, UserT white2, UserT black1, UserT black2, HashMap<String, Integer> revisionMap) throws GameSummary.RevisionLimitException {
        super(newId, rules.getSize(), rules.getHandicap(), rules.getKomi(), isPrivate, 16387);
        this.setPlayer(KRole.WHITE, white1);
        this.setPlayer(KRole.WHITE_2, white2);
        this.setPlayer(KRole.BLACK, black1);
        this.setPlayer(KRole.BLACK_2, black2);
        this.setRevision(revisionMap);
    }

    protected KGameSummary.Rengo(DataInputStream in, GameSummary.StreamUserLoader<UserT> userLoader) throws IOException {
        super(in, KGameType.RENGO.roleMask, userLoader);
    }

    @Override
    public GameType getGameType() {
        return KGameType.RENGO;
    }

    @Override
    public UserT getPlayer(Role role) {
        return (UserT)((User)this.users.get(role.id));
    }

    @Override
    public void setPlayer(Role role, UserT player) {
        if (this.users == null) {
            this.users = new ArrayList(6);
            while (this.users.size() < 6) {
                this.users.add(null);
            }
        }
        if (this.users.get(role.id) != null) {
            throw new IllegalStateException("Got setPlayer(role=" + role + ") when role already filled!");
        }
        this.users.set(role.id, player);
    }

    @Override
    public boolean isFinished() {
        return this.getScore() != 16387;
    }

    @Override
    public String getLocalDesc(IBundle bundle) {
        return bundle.str(877402151, new Object[]{((User)this.getPlayer(KRole.WHITE)).getNameAndRank(bundle), ((User)this.getPlayer(KRole.WHITE_2)).getNameAndRank(bundle), ((User)this.getPlayer(KRole.BLACK)).getNameAndRank(bundle), ((User)this.getPlayer(KRole.BLACK_2)).getNameAndRank(bundle)});
    }
}
