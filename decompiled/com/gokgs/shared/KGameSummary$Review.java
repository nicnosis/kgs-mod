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
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.User;

static class KGameSummary.Review<UserT extends User>
extends KGameSummary<UserT> {
    private UserT owner;
    private UserT black;
    private UserT white;

    protected KGameSummary.Review(DataInputStream in, GameSummary.StreamUserLoader<UserT> userLoader) throws IOException {
        super(in, KGameType.REVIEW.roleMask, userLoader);
    }

    protected KGameSummary.Review(ResultSet rs, GameSummary.DbUserLoader<UserT> userLoader) throws SQLException {
        super(rs, userLoader);
    }

    protected KGameSummary.Review(long newId, KGameSummary<UserT> original, boolean isPrivate, UserT newOwner, HashMap<String, Integer> revisionMap) throws GameSummary.RevisionLimitException {
        super(newId, original.getBoardSize(), original.getHandicap(), original.getKomi(), isPrivate, 16387);
        this.owner = newOwner;
        this.black = original.getPlayer(KRole.BLACK);
        if (this.black == null) {
            throw new IllegalArgumentException("Cannot turn " + original + " into a review");
        }
        this.white = original.getPlayer(KRole.WHITE);
        if (this.white == null) {
            throw new IllegalArgumentException("Cannot turn " + original + " into a review");
        }
        this.setRevision(revisionMap);
    }

    @Override
    public GameType getGameType() {
        return KGameType.REVIEW;
    }

    @Override
    public UserT getPlayer(Role role) {
        switch (role.id) {
            case 1: {
                return this.owner;
            }
            case 2: {
                return this.white;
            }
            case 4: {
                return this.black;
            }
        }
        return null;
    }

    @Override
    public Role getRole(String name) {
        if (name.equals(((User)this.owner).name)) {
            return KRole.OWNER;
        }
        if (name.equals(((User)this.white).name)) {
            return KRole.WHITE;
        }
        if (name.equals(((User)this.black).name)) {
            return KRole.BLACK;
        }
        return null;
    }

    @Override
    protected void setPlayer(Role role, UserT player) {
        if (role == KRole.OWNER && this.owner == null) {
            this.owner = player;
        } else if (role == KRole.BLACK && this.black == null) {
            this.black = player;
        } else if (role == KRole.WHITE && this.white == null) {
            this.white = player;
        } else {
            throw new IllegalStateException("Got setPlayer(role=" + role + ", player=" + player + ") when players=" + this.owner + " (" + this.white + "-" + this.black + ")");
        }
    }

    @Override
    public String getLocalDesc(IBundle bundle) {
        return bundle.str(-669080763, new Object[]{((User)this.owner).getNameAndRank(bundle), ((User)this.white).getNameAndRank(bundle), ((User)this.black).getNameAndRank(bundle)});
    }

    @Override
    public boolean isFinished() {
        return true;
    }
}
