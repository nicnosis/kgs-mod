/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import org.igoweb.igoweb.shared.AuthLevel;
import org.igoweb.igoweb.shared.User;
import org.igoweb.util.DbConn;

public class PUser
extends User {
    public static final int STATE_CHANGED_EVENT = 20;
    public static final int PUSER_EVENT_LIMIT = 21;
    public static final String SQL_COLUMNS_NEEDED = "name, id, state,auth_level, rank, rank_confident, avatar, helpful, tourn_winner, long_lived";
    public final int id;
    private State state;

    public PUser(ResultSet rs) throws SQLException {
        super(rs.getString("name"), 0);
        this.id = rs.getInt("id");
        this.state = State.get(rs.getString("state"));
        super.setGuest(this.state.guest);
        if (this.state.deleted) {
            throw new IllegalStateException("Loaded user " + this.name + "(id=" + this.id + ") in deleted state " + (Object)((Object)this.state));
        }
        this.setAuthLevel(AuthLevel.fromSql(rs.getString("auth_level")));
        this.setRank(rs.getInt("rank"));
        boolean rankWanted = !rs.wasNull();
        this.setRankWanted(rankWanted);
        this.setRankConfident(rankWanted && rs.getBoolean("rank_confident"));
        this.setAvatar(rs.getBoolean("avatar"));
        this.setHelpful(rs.getBoolean("helpful"));
        int winner = rs.getInt("tourn_winner");
        this.setTournWinner((winner & 1) != 0);
        this.setTournRunnerUp((winner & 2) != 0);
        this.setLongLived(rs.getBoolean("long_lived"));
    }

    protected PUser(String name, int id) {
        this(name, id, State.GUEST);
    }

    public PUser(String name, int id, State state) {
        super(name);
        this.id = id;
        this.state = state;
        super.setGuest(state.guest);
        if (state.deleted) {
            super.delete();
        }
    }

    public PUser(User user, int id) {
        super(user.name, user.getFlags());
        this.id = id;
        this.state = super.isGuest() ? State.GUEST : (super.isDeleted() ? State.CLOSED : State.ACTIVE);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static PUser load(DbConn db, String name) throws SQLException {
        try (ResultSet rs = null;){
            PreparedStatement ps = db.get("SELECT name, id, state,auth_level, rank, rank_confident, avatar, helpful, tourn_winner, long_lived  FROM accounts, accounts_active  WHERE canon_name = ? AND state = 'active'    AND id = accounts_active.account_id");
            ps.setString(1, PUser.canonName(name));
            rs = ps.executeQuery();
            PUser pUser = rs.next() ? new PUser(rs) : null;
            return pUser;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static PUser load(DbConn db, int id) throws SQLException {
        try (ResultSet rs = null;){
            PreparedStatement ps = db.get("SELECT name, id, state,auth_level, rank, rank_confident, avatar, helpful, tourn_winner, long_lived  FROM accounts, accounts_active  WHERE id = ? AND accounts_active.account_id = id");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            PUser pUser = rs.next() ? new PUser(rs) : null;
            return pUser;
        }
    }

    @Override
    public boolean setGuest(boolean guest) {
        throw new UnsupportedOperationException("Use setState instead");
    }

    public boolean setState(State newState) {
        if (this.state != newState) {
            if (!this.state.canBecome(newState)) {
                throw new IllegalStateException("Transition from " + (Object)((Object)this.state) + " to " + (Object)((Object)newState) + " not allowed.");
            }
            this.state = newState;
            super.setGuest(newState.guest);
            if (newState.deleted) {
                super.delete();
            }
            this.emit(20);
            return true;
        }
        return false;
    }

    public State getState() {
        return this.state;
    }

    public static RankState getRankState(String state) {
        switch (state.charAt(0)) {
            case 'n': {
                return RankState.NONE;
            }
            case 'c': {
                return RankState.CONFIDENT;
            }
            case 'u': {
                return RankState.UNCERTAIN;
            }
        }
        throw new IllegalArgumentException(state);
    }

    public static void setLongLivedInDb(DbConn db, int userId, boolean value) throws SQLException {
        PreparedStatement ps = db.get("UPDATE accounts_active  SET long_lived = ?  WHERE account_id = ?");
        ps.setBoolean(1, value);
        ps.setInt(2, userId);
        ps.executeUpdate();
    }

    public static enum RankState {
        NONE,
        CONFIDENT,
        UNCERTAIN;

    }

    public static enum State {
        GUEST(true, false, false),
        PENDING(true, true, false),
        ACTIVE(false, true, false),
        ABORTED(true, false, true),
        CLOSED(false, false, true);

        public final String sqlName;
        public final boolean guest;
        public final boolean active;
        public final boolean deleted;
        private static HashMap<String, State> nameToState;

        public boolean canBecome(State newState) {
            switch (this) {
                case GUEST: {
                    return newState == PENDING || newState == ABORTED;
                }
                case PENDING: {
                    return newState == ACTIVE || newState == ABORTED;
                }
                case ACTIVE: {
                    return newState == CLOSED;
                }
            }
            return false;
        }

        private State(boolean guest, boolean active, boolean deleted) {
            this.guest = guest;
            this.active = active;
            this.deleted = deleted;
            this.sqlName = this.toString().toLowerCase(Locale.US);
        }

        public static State get(String sqlName) {
            State result;
            if (nameToState == null) {
                State.initNameToState();
            }
            if ((result = nameToState.get(sqlName)) == null) {
                throw new IllegalArgumentException("Invalid state sql name: " + sqlName);
            }
            return result;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private static void initNameToState() {
            Class<State> clazz = State.class;
            synchronized (State.class) {
                if (nameToState == null) {
                    HashMap<String, State> map = new HashMap<String, State>();
                    for (State state : State.values()) {
                        map.put(state.sqlName, state);
                    }
                    nameToState = map;
                }
                // ** MonitorExit[var0] (shouldn't be in output)
                return;
            }
        }
    }
}
