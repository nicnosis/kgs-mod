/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.PUser;

private static class GameSummary.DefaultDbUserLoader
implements GameSummary.DbUserLoader<PUser> {
    private GameSummary.DefaultDbUserLoader() {
    }

    @Override
    public PUser loadUser(ResultSet rs) throws SQLException {
        PUser user = new PUser(rs.getString("name"), rs.getInt("games_players.account_id"), PUser.State.get(rs.getString("accounts.state")));
        PUser.RankState rankState = PUser.getRankState(rs.getString("games_players.rank_state"));
        if (rankState == PUser.RankState.NONE) {
            user.setRankWanted(false);
        } else {
            user.setRankWanted(true);
            user.setRank(rs.getInt("games_players.rank"));
            user.setRankConfident(rankState == PUser.RankState.CONFIDENT);
        }
        return user;
    }
}
