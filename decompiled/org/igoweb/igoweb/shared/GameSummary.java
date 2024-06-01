/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.DataInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.igoweb.shared.PUser;
import org.igoweb.igoweb.shared.PlayerContainer;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.igoweb.shared.User;
import org.igoweb.util.DbConn;
import org.igoweb.util.DbConnFactory;
import org.igoweb.util.Emitter;
import org.igoweb.util.Event;
import org.igoweb.util.LockOrder;

public abstract class GameSummary<UserT extends User>
extends Emitter
implements PlayerContainer<UserT> {
    public static final LockOrder LOCK_ORDER = new LockOrder("GameSummary");
    public static final StreamUserLoader<User> DEFAULT_STREAM_USER_LOADER;
    public static final DbUserLoader<PUser> DEFAULT_DB_USER_LOADER;
    private static final int EVENT_BASE = 0;
    public static final int SCORE_CHANGED_EVENT = 0;
    public static final int SAVED_CHANGED_EVENT = 1;
    public static final int DELETED_EVENT = 2;
    public static final int IN_PLAY_CHANGED_EVENT = 3;
    public static final int PRIVATE_CHANGED_EVENT = 4;
    public static final int ESCAPER_CHANGED_EVENT = 5;
    public static final int EVENT_LIMIT = 6;
    private static final int PRIVATE_BIT = 1;
    private static final int IN_PLAY_BIT = 2;
    private static final int SAVED_BIT = 8;
    private static final int DELETED_BIT = 16;
    public static final String SQL_COLUMNS = "games.id, game_type, flags, revision, games.score,games_players.role, games_players.rank, games_players.rank_state,games_players.account_id";
    public final long id;
    private static final TimeZone UTC;
    private int score;
    private int flags;
    private int revision;
    private static String[] playerInsertions;

    protected GameSummary(ResultSet rs, DbUserLoader<UserT> userLoader) throws SQLException {
        this.id = rs.getLong("games.id");
        this.flags = 8;
        String flagsCol = rs.getString("flags");
        if (flagsCol.contains("private")) {
            this.flags |= 1;
        }
        this.revision = rs.getInt("revision");
        this.score = rs.getInt("games.score");
        this.readAuxColumns(rs);
        this.setPlayer(Role.get(rs.getString("role")), userLoader.loadUser(rs));
        while (rs.next()) {
            if (rs.getLong("games.id") != this.id) {
                return;
            }
            this.setPlayer(Role.get(rs.getString("role")), userLoader.loadUser(rs));
        }
        return;
    }

    protected abstract void readAuxColumns(ResultSet var1) throws SQLException;

    protected GameSummary(long date, boolean isPrivate, int newScore) {
        this.id = date;
        if (isPrivate) {
            this.flags |= 1;
        }
        this.score = newScore;
        this.revision = -1;
    }

    public int getRevision() {
        return this.revision;
    }

    protected void setRevision(HashMap<String, Integer> usedFiles) throws RevisionLimitException {
        if (this.revision >= 0) {
            throw new IllegalStateException();
        }
        StringBuilder sb = new StringBuilder();
        this.appendPlayerNames(sb);
        String nameKey = sb.toString();
        Integer objVal = usedFiles.get(nameKey);
        int n = this.revision = objVal == null ? 0 : objVal + 1;
        if (this.revision > Short.MAX_VALUE) {
            throw new RevisionLimitException();
        }
        usedFiles.put(nameKey, this.revision);
    }

    public void syncRevisionMap(HashMap<String, Integer> usedFiles) {
        if (this.revision < 0) {
            throw new IllegalStateException();
        }
        StringBuilder sb = new StringBuilder();
        this.appendPlayerNames(sb);
        String nameKey = sb.toString();
        Integer objVal = usedFiles.get(nameKey);
        if (objVal == null || objVal < this.revision) {
            usedFiles.put(nameKey, this.revision);
        }
    }

    protected GameSummary(DataInputStream in, int roleMask, StreamUserLoader<UserT> loader) throws IOException {
        this.id = in.readLong();
        this.score = in.readShort();
        this.revision = in.readShort();
        this.flags = in.readByte();
        int bit = 1;
        int i = 0;
        while (bit <= roleMask) {
            if ((roleMask & bit) != 0) {
                this.setPlayer(Role.get(i), loader.loadUser(in));
            }
            bit += bit;
            ++i;
        }
    }

    protected abstract void setPlayer(Role var1, UserT var2);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeTo(TxMessage tx) {
        assert (LockOrder.testAcquire(this));
        GameSummary gameSummary = this;
        synchronized (gameSummary) {
            tx.write(this.getGameType().id);
            tx.writeLong(this.id);
            tx.writeShort((short)this.score);
            tx.writeShort((short)this.revision);
            tx.write(this.flags);
            int roleMask = this.getGameType().roleMask;
            int bit = 1;
            int i = 0;
            while (bit <= roleMask) {
                if ((roleMask & bit) != 0) {
                    ((User)this.getPlayer(Role.get(i))).writeTo(tx);
                }
                bit += bit;
                ++i;
            }
        }
    }

    public boolean equals(Object obj) {
        return obj != null && obj.getClass().equals(this.getClass()) && this.id == ((GameSummary)obj).id;
    }

    public int hashCode() {
        return (int)this.id * -1640524983 + (int)(this.id >> 32);
    }

    public int getScore() {
        return this.score;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected boolean setScore(int newScore) {
        assert (LockOrder.testAcquire(this));
        GameSummary gameSummary = this;
        synchronized (gameSummary) {
            if (newScore != this.score) {
                this.score = newScore;
                this.emit(0);
                return true;
            }
        }
        return false;
    }

    public boolean isFinished() {
        return true;
    }

    public abstract GameType getGameType();

    @Override
    public abstract UserT getPlayer(Role var1);

    public String getFileName() {
        GregorianCalendar gc = new GregorianCalendar(UTC, Locale.US);
        gc.setTime(new Date(this.id));
        StringBuilder sb = new StringBuilder();
        sb.append(gc.get(1)).append('/').append(gc.get(2) - 0 + 1).append('/').append(gc.get(5)).append('/');
        this.appendPlayerNames(sb);
        if (this.revision > 0) {
            sb.append('-').append(this.revision + 1);
        }
        return sb.append(this.getExtension()).toString();
    }

    private void appendPlayerNames(StringBuilder sb) {
        GameType gt = this.getGameType();
        boolean first = true;
        for (int i = 0; i < Role.count(); ++i) {
            Role role = Role.get(i);
            if (!gt.isMainRole(role)) continue;
            if (!first) {
                sb.append('-');
            }
            first = false;
            sb.append(((User)this.getPlayer((Role)role)).name);
        }
    }

    protected abstract String getExtension();

    public final String getLocalDesc() {
        return this.getLocalDesc(IBundle.get());
    }

    public abstract String getLocalDesc(IBundle var1);

    public Role getRole(String name) {
        int roleMask = this.getGameType().roleMask;
        int i = 0;
        while (1 << i <= roleMask) {
            if ((roleMask & 1 << i) != 0) {
                Role role = Role.get(i);
                if (((User)this.getPlayer((Role)role)).name.equals(name)) {
                    return role;
                }
            }
            ++i;
        }
        return null;
    }

    public abstract Role getWinner();

    public boolean isDownloadable() {
        return (this.flags & 9) == 8;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeToDb(DbConnFactory factory) {
        assert (LockOrder.testAcquire(this));
        GameSummary gameSummary = this;
        synchronized (gameSummary) {
            if (this.revision < 0) {
                throw new IllegalStateException();
            }
            if ((this.flags & 0x18) != 0) {
                throw new IllegalStateException("Flags = 0x" + Integer.toString(this.flags, 16));
            }
            try (DbConn db = factory.getDbConn();){
                int i;
                PreparedStatement ps = db.get("INSERT INTO games (    id, game_type, flags, revision, score " + this.getAuxColumns() + ")  VALUES (?, ?, ?, ?, ?" + this.getAuxFields() + ")");
                ps.setLong(1, this.id);
                ps.setString(2, this.getGameType().sql);
                ps.setString(3, this.getFlagsAsSql());
                ps.setInt(4, this.revision);
                ps.setInt(5, this.score);
                this.writeAuxColumns(ps, 6);
                ps.executeUpdate();
                int numPlayers = 0;
                for (i = 0; i < Role.count(); ++i) {
                    if (this.getPlayer(Role.get(i)) == null) continue;
                    ++numPlayers;
                }
                if (playerInsertions == null) {
                    GameSummary.setPlayerInsertions();
                }
                ps = db.get(playerInsertions[numPlayers]);
                i = 0;
                while (numPlayers > 0) {
                    Role role = Role.get(i);
                    PUser user = (PUser)this.getPlayer(role);
                    if (user != null) {
                        ps.setLong(--numPlayers * 6 + 1, this.id);
                        ps.setInt(numPlayers * 6 + 2, user.id);
                        if (user.isRankWanted()) {
                            ps.setInt(numPlayers * 6 + 3, user.getRank());
                            ps.setString(numPlayers * 6 + 4, user.isRankConfident() ? "confident" : "uncertain");
                        } else {
                            ps.setInt(numPlayers * 6 + 3, 0);
                            ps.setString(numPlayers * 6 + 4, "none");
                        }
                        ps.setString(numPlayers * 6 + 5, role.sql);
                        ps.setBoolean(numPlayers * 6 + 6, this.getGameType().isMainRole(role));
                    }
                    ++i;
                }
                ps.executeUpdate();
            }
            this.flags |= 8;
            this.emit(1);
        }
    }

    protected abstract String getAuxColumns();

    protected abstract String getAuxFields();

    protected abstract void writeAuxColumns(PreparedStatement var1, int var2) throws SQLException;

    protected final String getFlagsAsSql() {
        StringBuilder sb = new StringBuilder();
        this.getFlagsAsSql(sb);
        int len = sb.length();
        return len == 0 ? "" : sb.substring(0, len - 1);
    }

    protected void getFlagsAsSql(StringBuilder sb) {
        if ((this.flags & 1) != 0) {
            sb.append("private,");
        }
    }

    private static void setPlayerInsertions() {
        String[] tmpPlayerInsertions = new String[Role.count() + 1];
        for (int i = 1; i < Role.count() + 1; ++i) {
            StringBuilder sb = new StringBuilder("INSERT INTO games_players    (game_id, account_id, rank, rank_state, role, main_role)  VALUES ");
            for (int j = 0; j < i; ++j) {
                if (j > 0) {
                    sb.append(',');
                }
                sb.append("(?, ?, ?, ?, ?, ?)");
            }
            tmpPlayerInsertions[i] = sb.toString();
        }
        playerInsertions = tmpPlayerInsertions;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean setFlag(DbConnFactory factory, int mask, boolean value) {
        assert (LockOrder.testAcquire(this));
        GameSummary gameSummary = this;
        synchronized (gameSummary) {
            int newFlags;
            int n = newFlags = value ? this.flags | mask : this.flags & ~mask;
            if (newFlags == this.flags) {
                return false;
            }
            this.flags = newFlags;
            this.writeFlagsToDb(factory);
        }
        return true;
    }

    protected void writeFlagsToDb(DbConnFactory factory) {
        assert (Thread.holdsLock(this));
        if ((this.flags & 0x18) != 8) {
            return;
        }
        try (DbConn db = factory.getDbConn();){
            PreparedStatement ps = db.get("UPDATE games SET flags = ? WHERE id = ?");
            ps.setString(1, this.getFlagsAsSql());
            ps.setLong(2, this.id);
            ps.executeUpdate();
        }
    }

    public final boolean setPrivate(DbConnFactory factory, boolean isPrivate) {
        if (this.setFlag(factory, 1, isPrivate)) {
            this.emit(4);
            return true;
        }
        return false;
    }

    public abstract boolean isEscaper(Role var1);

    public abstract void setEscaper(DbConnFactory var1, Role var2);

    public abstract void setForfeit(DbConnFactory var1, Role var2);

    public final boolean isPrivate() {
        return (this.flags & 1) != 0;
    }

    public final boolean isInPlay() {
        return (this.flags & 2) != 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean setInPlay(boolean inPlay) {
        assert (LockOrder.testAcquire(this));
        GameSummary gameSummary = this;
        synchronized (gameSummary) {
            int oldFlags = this.flags;
            int n = this.flags = inPlay ? oldFlags | 2 : oldFlags & 0xFFFFFFFD;
            if (oldFlags != this.flags) {
                this.emit(3);
                return true;
            }
        }
        return false;
    }

    public final boolean isSaved() {
        return (this.flags & 8) != 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void delete() {
        assert (LockOrder.testAcquire(this));
        GameSummary gameSummary = this;
        synchronized (gameSummary) {
            if ((this.flags & 0x10) != 0) {
                return;
            }
            if ((this.flags & 8) != 0) {
                throw new IllegalStateException("Can't delete saved summaries");
            }
            this.flags |= 0x10;
            this.emit(2);
        }
    }

    public final boolean isDeleted() {
        return (this.flags & 0x10) != 0;
    }

    public long getId() {
        return this.id;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("GameSummary[");
        sb.append(this.id).append(", ").append(this.getGameType());
        for (Role role : Role.values()) {
            if (!this.getGameType().isRole(role)) continue;
            sb.append(", ").append(role).append('=').append(this.getPlayer(role));
        }
        return sb.append(']').toString();
    }

    private static User defaultStreamUserLoader(DataInputStream in) throws IOException {
        String name = in.readUTF();
        return new User(name, in.readInt());
    }

    static {
        assert (LOCK_ORDER.addInnerOrder(DbConnFactory.LOCK_ORDER));
        DEFAULT_STREAM_USER_LOADER = GameSummary::defaultStreamUserLoader;
        DEFAULT_DB_USER_LOADER = new DefaultDbUserLoader();
        UTC = TimeZone.getTimeZone("UTC");
    }

    public static class EscaperEvent
    extends Event {
        public final boolean newVal;

        public EscaperEvent(Object newSource, Role role, boolean newNewVal) {
            super(newSource, 5, role);
            this.newVal = newNewVal;
        }
    }

    private static class DefaultDbUserLoader
    implements DbUserLoader<PUser> {
        private DefaultDbUserLoader() {
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

    public static interface DbUserLoader<UserT extends User> {
        public UserT loadUser(ResultSet var1) throws SQLException;
    }

    public static interface StreamUserLoader<UserT extends User> {
        public UserT loadUser(DataInputStream var1) throws IOException;
    }

    public static class RevisionLimitException
    extends Exception {
    }

    public static class Comparator
    implements java.util.Comparator<GameSummary<?>> {
        public static final int SORT_BY_DATE = 0;
        public static final int SORT_BY_NAME = 1;
        private int sortType;
        private final String ignoredName;

        public Comparator() {
            this(null);
        }

        public Comparator(User ignoredUser) {
            this(ignoredUser, 0);
        }

        public Comparator(User ignoredUser, int newSortType) {
            this.ignoredName = ignoredUser == null ? "" : ignoredUser.name;
            this.sortType = newSortType;
        }

        @Override
        public int compare(GameSummary<?> r1, GameSummary<?> r2) {
            int cmp;
            int sortByDateResult;
            int n = r1.id == r2.id ? 0 : (sortByDateResult = r1.id > r2.id ? -1 : 1);
            if (this.sortType == 0) {
                return sortByDateResult;
            }
            String n1 = null;
            String n2 = null;
            GameType gt1 = r1.getGameType();
            GameType gt2 = r2.getGameType();
            for (int i = 0; i < Role.count(); ++i) {
                String name;
                Role role = Role.get(i);
                if (n1 == null && gt1.isRole(role) && !(name = ((User)r1.getPlayer((Role)role)).name).equals(this.ignoredName)) {
                    n1 = name;
                }
                if (n2 != null || !gt2.isRole(role) || (name = ((User)r2.getPlayer((Role)role)).name).equals(this.ignoredName)) continue;
                n2 = name;
            }
            if (n1 == null) {
                n1 = this.ignoredName;
            }
            if (n2 == null) {
                n2 = this.ignoredName;
            }
            return (cmp = n1.compareToIgnoreCase(n2)) == 0 ? sortByDateResult : cmp;
        }

        public void setSortType(int newType) {
            this.sortType = newType;
        }

        public int getSortType() {
            return this.sortType;
        }
    }
}
