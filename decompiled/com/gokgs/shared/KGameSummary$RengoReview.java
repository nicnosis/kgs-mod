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
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.User;

static class KGameSummary.RengoReview<UserT extends User>
extends KGameSummary<UserT> {
    private ArrayList<UserT> users;

    protected KGameSummary.RengoReview(DataInputStream in, GameSummary.StreamUserLoader<UserT> userLoader) throws IOException {
        super(in, KGameType.RENGO_REVIEW.roleMask, userLoader);
    }

    protected KGameSummary.RengoReview(ResultSet rs, GameSummary.DbUserLoader<UserT> userLoader) throws SQLException {
        super(rs, userLoader);
    }

    protected KGameSummary.RengoReview(long newId, KGameSummary<UserT> original, boolean isPrivate, UserT owner, HashMap<String, Integer> revisionMap) throws GameSummary.RevisionLimitException {
        super(newId, original.getBoardSize(), original.getHandicap(), original.getKomi(), isPrivate, 16387);
        this.setPlayer(KRole.OWNER, owner);
        for (int i = Role.count() - 1; i >= 0; --i) {
            Role role;
            if (i == 1 || !KGameType.RENGO_REVIEW.isRole(role = Role.get(i))) continue;
            this.setPlayer(role, original.getPlayer(role));
        }
        this.setRevision(revisionMap);
    }

    @Override
    public GameType getGameType() {
        return KGameType.RENGO_REVIEW;
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
        if (player == null) {
            throw new IllegalArgumentException("Cannot set a null player for role " + role);
        }
        if (this.users.get(role.id) != null) {
            throw new IllegalStateException("Got setPlayer(role=" + role + ") when role already filled!");
        }
        this.users.set(role.id, player);
    }

    @Override
    public String getLocalDesc(IBundle bundle) {
        return bundle.str(696435397, new Object[]{((User)this.getPlayer(Role.OWNER)).getNameAndRank(bundle), ((User)this.getPlayer(KRole.WHITE)).getNameAndRank(bundle), ((User)this.getPlayer(KRole.WHITE_2)).getNameAndRank(bundle), ((User)this.getPlayer(KRole.BLACK)).getNameAndRank(bundle), ((User)this.getPlayer(KRole.BLACK_2)).getNameAndRank(bundle)});
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
