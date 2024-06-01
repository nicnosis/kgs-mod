/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.shared;

import com.gokgs.shared.KGameType;
import com.gokgs.shared.KRole;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.igoweb.go.Rules;
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.igoweb.shared.PUser;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.igoweb.shared.User;
import org.igoweb.util.DbConn;
import org.igoweb.util.DbConnFactory;
import org.igoweb.util.LockOrder;

public abstract class KGameSummary<UserT extends User>
extends GameSummary<UserT> {
    public static final String K_SQL_COLUMNS = "games.id, game_type, flags, revision, games.score,games_players.role, games_players.rank, games_players.rank_state,games_players.account_id, board_size, handicap, komi2";
    private int boardSize;
    private int handicap;
    private int escaperBits;
    private float komi;
    static final /* synthetic */ boolean $assertionsDisabled;

    protected KGameSummary(ResultSet rs, GameSummary.DbUserLoader<UserT> userLoader) throws SQLException {
        super(rs, userLoader);
    }

    protected KGameSummary(long newId, int newBoardSize, int newHandicap, float newKomi, boolean isPrivate, int scoreVal) {
        super(newId, isPrivate, scoreVal);
        this.boardSize = newBoardSize;
        this.handicap = newHandicap;
        this.komi = newKomi;
    }

    protected KGameSummary(DataInputStream in, int roleMask, GameSummary.StreamUserLoader<UserT> userLoader) throws IOException {
        super(in, roleMask, userLoader);
        this.boardSize = in.read();
        this.handicap = in.read();
        this.komi = (float)in.readShort() * 0.5f;
    }

    @Override
    protected void readAuxColumns(ResultSet rs) throws SQLException {
        this.boardSize = rs.getInt("board_size");
        this.handicap = rs.getInt("handicap");
        this.komi = (float)rs.getInt("komi2") * 0.5f;
        String flags = rs.getString("flags");
        if (flags.contains("escaper_b")) {
            this.escaperBits |= 1;
        }
        if (flags.contains("escaper_w")) {
            this.escaperBits |= 2;
        }
    }

    public static <UserT extends User> KGameSummary<UserT> load(ResultSet rs, GameSummary.DbUserLoader<UserT> userLoader) throws SQLException {
        KGameSummary sum;
        GameType gameType = GameType.get(rs.getString("game_type"));
        switch (gameType.id) {
            case 1: {
                sum = new Demo<UserT>(rs, userLoader);
                break;
            }
            case 2: {
                sum = new Review<UserT>(rs, userLoader);
                break;
            }
            case 3: {
                sum = new RengoReview<UserT>(rs, userLoader);
                break;
            }
            case 6: {
                sum = new Rengo<UserT>(rs, userLoader);
                break;
            }
            default: {
                sum = new TwoPlayer<UserT>(rs, gameType, userLoader);
            }
        }
        for (int i = 0; i < 6; ++i) {
            Role role = Role.get(i);
            if (!sum.getGameType().isRole(role) || sum.getPlayer(role) != null) continue;
            throw new RuntimeException("Game " + sum + " did not get role " + role + " set.");
        }
        return sum;
    }

    public static KGameSummary<User> load(DataInputStream in) throws IOException {
        return KGameSummary.load(in, DEFAULT_STREAM_USER_LOADER);
    }

    public static <U extends User> KGameSummary<U> load(DataInputStream in, GameSummary.StreamUserLoader<U> userLoader) throws IOException {
        byte gameTypeId = in.readByte();
        switch (gameTypeId) {
            case 1: {
                return new Demo<U>(in, userLoader);
            }
            case 2: {
                return new Review<U>(in, userLoader);
            }
            case 6: {
                return new Rengo<U>(in, userLoader);
            }
            case 3: {
                return new RengoReview<U>(in, userLoader);
            }
        }
        return new TwoPlayer<U>(in, GameType.get(gameTypeId), userLoader);
    }

    public static KGameSummary<PUser> buildReview(long newId, KGameSummary<PUser> original, boolean isPrivate, PUser owner, HashMap<String, Integer> revisionMap) throws GameSummary.RevisionLimitException {
        switch (original.getGameType().id) {
            case 3: 
            case 6: {
                return new RengoReview<PUser>(newId, original, isPrivate, owner, revisionMap);
            }
            case 1: {
                return new Demo<PUser>(newId, original, isPrivate, owner, revisionMap);
            }
        }
        return new Review<PUser>(newId, original, isPrivate, owner, revisionMap);
    }

    public int getBoardSize() {
        return this.boardSize;
    }

    public int getHandicap() {
        return this.handicap;
    }

    public float getKomi() {
        return this.komi;
    }

    @Override
    public void writeTo(TxMessage tx) {
        super.writeTo(tx);
        tx.write(this.boardSize);
        tx.write(this.handicap);
        tx.writeShort((short)(this.komi * 2.0f));
    }

    @Override
    public Role getWinner() {
        int score = this.getScore();
        if (score == 16386 || score == 16387 || score == 16389 || score == 0) {
            return null;
        }
        return score > 0 ? KRole.BLACK : KRole.WHITE;
    }

    @Override
    public String getExtension() {
        return ".sgf";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean setScore(DbConnFactory factory, int newScore) {
        if (!$assertionsDisabled && !LockOrder.testAcquire(this)) {
            throw new AssertionError();
        }
        KGameSummary kGameSummary = this;
        synchronized (kGameSummary) {
            if (super.setScore(newScore)) {
                if (this.escaperBits != 0 && newScore != 16387 && newScore != 16388 && newScore != -16388) {
                    this.setEscaper(factory, null);
                }
                try (DbConn db = factory.getDbConn();){
                    PreparedStatement ps = db.get("UPDATE games SET score = ? WHERE id = ?");
                    ps.setShort(1, (short)newScore);
                    ps.setLong(2, this.id);
                    ps.executeUpdate();
                }
                return true;
            }
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void setEscaper(DbConnFactory factory, Role role) {
        if (!$assertionsDisabled && !LockOrder.testAcquire(this)) {
            throw new AssertionError();
        }
        KGameSummary kGameSummary = this;
        synchronized (kGameSummary) {
            int newBits;
            if (this.getScore() != 16387 || !this.getGameType().isRanked() || !this.isSaved()) {
                role = null;
            }
            int n = role == KRole.BLACK ? 1 : (newBits = role == KRole.WHITE ? 2 : 0);
            if (newBits != this.escaperBits) {
                if (this.escaperBits != 0) {
                    this.emit(new GameSummary.EscaperEvent((Object)this, this.escaperBits == 1 ? KRole.BLACK : KRole.WHITE, false));
                }
                this.escaperBits = newBits;
                this.writeFlagsToDb(factory);
                if (role != null) {
                    this.emit(new GameSummary.EscaperEvent((Object)this, role, true));
                }
            }
        }
    }

    @Override
    public boolean isEscaper(Role role) {
        return role == KRole.WHITE ? (this.escaperBits & 2) != 0 : (role == KRole.BLACK ? (this.escaperBits & 1) != 0 : false);
    }

    @Override
    public void setForfeit(DbConnFactory factory, Role role) {
        int score;
        if (role == KRole.WHITE) {
            score = 16388;
        } else if (role == KRole.BLACK) {
            score = -16388;
        } else {
            throw new IllegalArgumentException("Unknown role " + role);
        }
        this.setScore(factory, score);
    }

    @Override
    protected void getFlagsAsSql(StringBuilder sb) {
        super.getFlagsAsSql(sb);
        if ((this.escaperBits & 1) != 0) {
            sb.append("escaper_b,");
        }
        if ((this.escaperBits & 2) != 0) {
            sb.append("escaper_w,");
        }
    }

    @Override
    protected String getAuxColumns() {
        return ", board_size, handicap, komi2";
    }

    @Override
    protected String getAuxFields() {
        return ", ?, ?, ?";
    }

    @Override
    protected void writeAuxColumns(PreparedStatement ps, int startingIndex) throws SQLException {
        ps.setInt(startingIndex, this.boardSize);
        ps.setInt(startingIndex + 1, this.handicap);
        ps.setInt(startingIndex + 2, (int)((double)this.komi * 2.0));
    }

    static {
        boolean bl = $assertionsDisabled = !KGameSummary.class.desiredAssertionStatus();
        if (KGameType.RANKED.id < 0) {
            throw new RuntimeException();
        }
        if (!$assertionsDisabled && !LOCK_ORDER.orderFor(Demo.class)) {
            throw new AssertionError();
        }
        if (!$assertionsDisabled && !LOCK_ORDER.orderFor(TwoPlayer.class)) {
            throw new AssertionError();
        }
        if (!$assertionsDisabled && !LOCK_ORDER.orderFor(Review.class)) {
            throw new AssertionError();
        }
        if (!$assertionsDisabled && !LOCK_ORDER.orderFor(Rengo.class)) {
            throw new AssertionError();
        }
        if (!$assertionsDisabled && !LOCK_ORDER.orderFor(RengoReview.class)) {
            throw new AssertionError();
        }
    }

    static class RengoReview<UserT extends User>
    extends KGameSummary<UserT> {
        private ArrayList<UserT> users;

        protected RengoReview(DataInputStream in, GameSummary.StreamUserLoader<UserT> userLoader) throws IOException {
            super(in, KGameType.RENGO_REVIEW.roleMask, userLoader);
        }

        protected RengoReview(ResultSet rs, GameSummary.DbUserLoader<UserT> userLoader) throws SQLException {
            super(rs, userLoader);
        }

        protected RengoReview(long newId, KGameSummary<UserT> original, boolean isPrivate, UserT owner, HashMap<String, Integer> revisionMap) throws GameSummary.RevisionLimitException {
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

    public static class Rengo<UserT extends User>
    extends KGameSummary<UserT> {
        private ArrayList<UserT> users;

        protected Rengo(ResultSet rs, GameSummary.DbUserLoader<UserT> userLoader) throws SQLException {
            super(rs, userLoader);
        }

        public Rengo(long newId, Rules rules, boolean isPrivate, UserT white1, UserT white2, UserT black1, UserT black2, HashMap<String, Integer> revisionMap) throws GameSummary.RevisionLimitException {
            super(newId, rules.getSize(), rules.getHandicap(), rules.getKomi(), isPrivate, 16387);
            this.setPlayer(KRole.WHITE, white1);
            this.setPlayer(KRole.WHITE_2, white2);
            this.setPlayer(KRole.BLACK, black1);
            this.setPlayer(KRole.BLACK_2, black2);
            this.setRevision(revisionMap);
        }

        protected Rengo(DataInputStream in, GameSummary.StreamUserLoader<UserT> userLoader) throws IOException {
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

    static class Review<UserT extends User>
    extends KGameSummary<UserT> {
        private UserT owner;
        private UserT black;
        private UserT white;

        protected Review(DataInputStream in, GameSummary.StreamUserLoader<UserT> userLoader) throws IOException {
            super(in, KGameType.REVIEW.roleMask, userLoader);
        }

        protected Review(ResultSet rs, GameSummary.DbUserLoader<UserT> userLoader) throws SQLException {
            super(rs, userLoader);
        }

        protected Review(long newId, KGameSummary<UserT> original, boolean isPrivate, UserT newOwner, HashMap<String, Integer> revisionMap) throws GameSummary.RevisionLimitException {
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

    public static class TwoPlayer<UserT extends User>
    extends KGameSummary<UserT> {
        private UserT black;
        private UserT white;
        private final GameType gameType;

        protected TwoPlayer(DataInputStream in, GameType newGameType, GameSummary.StreamUserLoader<UserT> userLoader) throws IOException {
            super(in, newGameType.roleMask, userLoader);
            this.gameType = newGameType;
        }

        protected TwoPlayer(ResultSet rs, GameType newGameType, GameSummary.DbUserLoader<UserT> userLoader) throws SQLException {
            super(rs, userLoader);
            this.gameType = newGameType;
        }

        public TwoPlayer(long newId, GameType gt, Rules rules, boolean isPrivate, UserT newWhite, UserT newBlack, HashMap<String, Integer> revisionMap) throws GameSummary.RevisionLimitException {
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

    public static class Demo<UserT extends User>
    extends KGameSummary<UserT> {
        private UserT owner;

        protected Demo(ResultSet rs, GameSummary.DbUserLoader<UserT> userLoader) throws SQLException {
            super(rs, userLoader);
        }

        protected Demo(DataInputStream in, GameSummary.StreamUserLoader<UserT> userLoader) throws IOException {
            super(in, KGameType.DEMONSTRATION.roleMask, userLoader);
        }

        public Demo(long newId, Rules rules, boolean isPrivate, UserT newOwner, HashMap<String, Integer> revisionMap) throws GameSummary.RevisionLimitException {
            super(newId, rules.getSize(), rules.getHandicap(), rules.getKomi(), isPrivate, 16387);
            this.owner = newOwner;
            this.setRevision(revisionMap);
        }

        protected Demo(long newId, KGameSummary<UserT> original, boolean isPrivate, UserT newOwner, HashMap<String, Integer> revisionMap) throws GameSummary.RevisionLimitException {
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
}
