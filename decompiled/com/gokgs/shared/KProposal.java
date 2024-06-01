/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.shared;

import com.gokgs.shared.KGameType;
import com.gokgs.shared.KRole;
import java.io.DataInput;
import java.io.IOException;
import java.util.List;
import org.igoweb.go.Rules;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.igoweb.shared.Proposal;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.igoweb.shared.User;

public class KProposal<UserT extends User, PropT extends KProposal<UserT, PropT>>
extends Proposal<UserT, KUserRole<UserT>, PropT> {
    public static final int MAX_SIMUL_SIZE = 101;
    public static final int FIELD_SIZE = 0;
    public static final int FIELD_HANDICAP = 1;
    public static final int FIELD_KOMI = 2;
    public static final int FIELD_TIME = 3;
    public static final int RANKED_TOO_SMALL = 0;
    public static final int RANKED_TOO_BIG = 1;
    public static final int RANKED_LONGER = 2;
    public static final int MAX_HANDICAP = 9;
    public static final float MAX_KOMI = 100.0f;
    public static final int MAX_RANKED_HANDICAP = 6;
    public static final float MAX_RANKED_KOMI = 10.0f;
    private static final byte NIGIRI_BIT = 1;
    byte flags;
    private Rules rules;
    private static Role[] nigiriFlips;

    public KProposal(GameType gameType) {
        super(gameType);
        this.flags = gameType == KGameType.TEACHING || gameType == KGameType.DEMONSTRATION || gameType == KGameType.SIMUL ? (byte)0 : 1;
        this.rules = new Rules();
        this.rules.setTimeSystem(2);
    }

    public KProposal(DataInput in, Proposal.UserDecoder<UserT> userDecoder) throws IOException, Proposal.NoSuchUserException {
        super(in, userDecoder);
        this.flags = in.readByte();
        this.rules = new Rules(in);
        if (this.isUseNigiri()) {
            this.rules.setHandicap(0);
            this.rules.setKomi(this.rules.getDefaultKomi());
        }
    }

    public KProposal(PropT initial) {
        super(initial);
        this.flags = ((KProposal)initial).flags;
        this.rules = new Rules(((KProposal)initial).getRules());
    }

    @Override
    public void removeUserRole() {
        List userRoles = this.getMutableUserRoles();
        if (!this.isRemoveUserRoleAllowed()) {
            throw new IllegalStateException(this.toString());
        }
        for (int i = userRoles.size() - 1; i > 1; --i) {
            if (((KUserRole)userRoles.get(i)).getUser() != null) continue;
            userRoles.remove(i);
            return;
        }
        userRoles.remove(userRoles.size() - 1);
    }

    @Override
    public void addUserRole() {
        List userRoles = this.getMutableUserRoles();
        if (!this.isAddUserRoleAllowed()) {
            throw new IllegalStateException(this.toString());
        }
        userRoles.add(new KUserRole<Object>(null, KRole.BLACK, 0, 0.0f));
    }

    @Override
    public KUserRole<UserT> cloneUserRole(KUserRole<UserT> src) {
        return new KUserRole(src.getUser(), src.getRole(), ((KUserRole)src).handicap, ((KUserRole)src).komi);
    }

    @Override
    protected KUserRole<UserT> readUserRole(DataInput in, Proposal.UserDecoder<UserT> userDecoder) throws IOException, Proposal.NoSuchUserException {
        UserT user = userDecoder.getUser(in);
        Role role = Role.get(in);
        byte handicap = 0;
        float komi = 0.0f;
        if (this.getGameType() == KGameType.SIMUL && role == KRole.BLACK) {
            handicap = in.readByte();
            komi = (float)in.readShort() * 0.5f;
        }
        return new KUserRole<UserT>(user, role, handicap, komi);
    }

    @Override
    protected void writeUserRole(TxMessage tx, KUserRole<UserT> userRole) {
        this.writeUser(tx, userRole.getUser());
        tx.write(userRole.getRole().id);
        if (this.getGameType() == KGameType.SIMUL && userRole.getRole() == KRole.BLACK) {
            tx.write(((KUserRole)userRole).handicap);
            tx.writeShort((short)(((KUserRole)userRole).komi * 2.0f));
        }
    }

    public void setUseNigiri(boolean useNigiri) {
        GameType gt = this.getGameType();
        if (gt == KGameType.SIMUL || gt == KGameType.DEMONSTRATION) {
            useNigiri = false;
        }
        this.flags = useNigiri ? (byte)(this.flags | 1) : (byte)(this.flags & 0xFFFFFFFE);
    }

    public boolean isUseNigiri() {
        return (this.flags & 1) != 0;
    }

    public void setRules(Rules newRules) {
        this.rules = new Rules(newRules);
    }

    public Rules getRules() {
        return this.rules;
    }

    @Override
    public void writeTo(TxMessage out) {
        super.writeTo(out);
        try {
            out.write(this.flags);
            this.rules.writeTo(out);
        }
        catch (IOException excep) {
            throw new RuntimeException(excep);
        }
    }

    @Override
    public void validate(PropT initial, UserT owner) throws IOException {
        super.validate(initial, owner);
        switch (this.getGameType().id) {
            case 1: {
                this.setUseNigiri(false);
                if (initial != null || this.getUserRoles().size() != 1) break;
                return;
            }
            case 2: 
            case 3: {
                break;
            }
            case 6: {
                if (initial != null && ((Proposal)initial).getGameType() != KGameType.RENGO || this.getUserRoles().size() != 4) break;
                return;
            }
            case 5: {
                this.setUseNigiri(false);
                if (initial != null && ((Proposal)initial).getGameType() != KGameType.SIMUL || this.getUserRoles().size() < 3 || this.getCount(KRole.WHITE) != 1 || this.getRole(owner) != KRole.WHITE) break;
                return;
            }
            default: {
                if (this.getGameType() == KGameType.RANKED) {
                    for (int i = 0; i < 2; ++i) {
                        Object user = ((KUserRole)this.getUserRole(i)).getUser();
                        if (user == null || KProposal.canPlayRanked(user)) continue;
                        this.setGameType(KGameType.FREE);
                    }
                }
                if (initial != null && ((Proposal)initial).getGameType() != KGameType.TEACHING && ((Proposal)initial).getGameType() != KGameType.FREE && ((Proposal)initial).getGameType() != KGameType.RANKED || this.getUserRoles().size() != 2) break;
                return;
            }
        }
        throw new IOException("Invalid proposal from " + owner + " with initial of " + initial + ": " + this);
    }

    public static boolean canPlayRanked(User u) {
        return u.isRankWanted() && u.getRank() <= 39;
    }

    @Override
    public TxMessage testRankedOk(int chanId) {
        int limit;
        int tooBigFlag;
        int field;
        if (this.rules.getSize() != 19) {
            field = 0;
            tooBigFlag = this.rules.getSize() > 19 ? 1 : 0;
            limit = 19;
        } else if (this.rules.getHandicap() > 6) {
            field = 1;
            tooBigFlag = 1;
            limit = 6;
        } else if (this.rules.getKomi() > 10.0f) {
            field = 2;
            tooBigFlag = 1;
            limit = 20;
        } else if (this.rules.getKomi() < -10.0f) {
            field = 2;
            tooBigFlag = 0;
            limit = -20;
        } else if (this.rules.estimateTimePerPlayer() <= 450) {
            field = 3;
            tooBigFlag = 2;
            limit = 0;
        } else {
            return null;
        }
        TxMessage tx = new TxMessage(MsgTypesDown.CHALLENGE_CANT_PLAY_RANKED);
        tx.writeInt(chanId);
        tx.write(field);
        tx.write(tooBigFlag);
        tx.writeShort((short)limit);
        this.setGameType(KGameType.FREE);
        return tx;
    }

    public void flipNigiri() {
        if (!this.isUseNigiri()) {
            return;
        }
        for (KUserRole userRole : this.getUserRoles()) {
            Role newRole = nigiriFlips[userRole.getRole().id];
            if (newRole == null) {
                throw new IllegalArgumentException("Flip nigiri error: " + this);
            }
            userRole.setRole(newRole);
        }
    }

    public void computeHandicapAndKomi(UserT user) {
        int rank2;
        KUserRole ur2;
        if (user == null && this.getUserRoles().size() != 2) {
            return;
        }
        KUserRole ur1 = (KUserRole)this.getUserRole(KRole.WHITE);
        KUserRole kUserRole = ur2 = user == null ? (KUserRole)this.getUserRole(KRole.BLACK) : (KUserRole)this.getUserRole(user);
        if (ur1 == null || ur2 == null) {
            throw new IllegalStateException("Can't find users for " + user + " in proposal " + this);
        }
        Object pl1 = ur1.getUser();
        Object pl2 = ur2.getUser();
        int rank1 = ((User)pl1).isRankWanted() ? ((User)pl1).getRank() : 0;
        int n = rank2 = ((User)pl2).isRankWanted() ? ((User)pl2).getRank() : 0;
        if ((rank1 == rank2 || rank1 == 0 || rank2 == 0) && this.getGameType() != KGameType.SIMUL) {
            this.setUseNigiri(this.getGameType() != KGameType.TEACHING);
            this.rules.setHandicap(0);
            this.rules.setKomi(this.rules.getDefaultKomi());
            return;
        }
        this.setUseNigiri(false);
        if (this.getGameType() == KGameType.SIMUL) {
            if (rank1 <= rank2) {
                ur2.handicap = 0;
                ur2.komi = this.rules.getDefaultKomi();
            } else {
                this.rules.setHandicapAndKomi(rank1, rank2);
                ur2.handicap = this.rules.getHandicap();
                ur2.komi = this.rules.getKomi();
            }
        } else if (rank2 > rank1) {
            ur1.setRole(KRole.BLACK);
            ur2.setRole(KRole.WHITE);
            this.rules.setHandicapAndKomi(rank2, rank1);
        } else {
            this.rules.setHandicapAndKomi(rank1, rank2);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        KProposal peer = (KProposal)obj;
        return this.flags == peer.flags && this.rules.equals(peer.rules);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("KProposal[");
        super.toString(sb);
        if (this.isUseNigiri()) {
            sb.append(",nigiri");
        }
        return sb.append(',').append(this.rules).append(']').toString();
    }

    @Override
    public void setGameType(GameType gameType) {
        GameType prevGameType = this.getGameType();
        if (prevGameType == gameType) {
            return;
        }
        List userRoles = this.getMutableUserRoles();
        if (prevGameType == KGameType.SIMUL || prevGameType == KGameType.DEMONSTRATION || prevGameType == KGameType.TEACHING) {
            this.flags = (byte)(this.flags | 1);
        }
        switch (gameType.id) {
            case 1: {
                this.flags = (byte)(this.flags & 0xFFFFFFFE);
                if (userRoles.size() < 1) {
                    userRoles.add(new KUserRole<Object>(null, KRole.OWNER));
                }
                ((KUserRole)userRoles.get(0)).setRole(KRole.OWNER);
                userRoles.subList(1, userRoles.size()).clear();
                break;
            }
            case 6: {
                if (prevGameType == KGameType.SIMUL) {
                    userRoles.subList(2, userRoles.size()).clear();
                }
                if (userRoles.size() == 2) {
                    userRoles.add(1, new KUserRole<Object>(null, ((KUserRole)userRoles.get(0)).getRole() == KRole.BLACK ? KRole.BLACK_2 : KRole.WHITE_2));
                    userRoles.add(new KUserRole<Object>(null, ((KUserRole)userRoles.get(2)).getRole() == KRole.BLACK ? KRole.BLACK_2 : KRole.WHITE_2));
                    break;
                }
                while (userRoles.size() < 4) {
                    userRoles.add(new KUserRole<Object>(null, KRole.WHITE));
                }
                if (userRoles.size() > 4) {
                    userRoles.subList(4, userRoles.size()).clear();
                }
                ((KUserRole)userRoles.get(0)).setRole(KRole.WHITE);
                ((KUserRole)userRoles.get(1)).setRole(KRole.WHITE_2);
                ((KUserRole)userRoles.get(2)).setRole(KRole.BLACK);
                ((KUserRole)userRoles.get(3)).setRole(KRole.BLACK_2);
                break;
            }
            case 5: {
                this.flags = (byte)(this.flags & 0xFFFFFFFE);
                if (userRoles.isEmpty()) {
                    userRoles.add(new KUserRole<Object>(null, KRole.WHITE));
                }
                while (userRoles.size() < 3) {
                    userRoles.add(new KUserRole<Object>(null, KRole.BLACK));
                }
                ((KUserRole)userRoles.get(0)).setRole(KRole.WHITE);
                for (int i = 1; i < userRoles.size(); ++i) {
                    ((KUserRole)userRoles.get(i)).setRole(KRole.BLACK);
                }
                break;
            }
            case 4: {
                this.flags = (byte)(this.flags & 0xFFFFFFFE);
            }
            case 7: 
            case 8: 
            case 9: {
                if (prevGameType == KGameType.SIMUL) {
                    for (int i = userRoles.size() - 1; i > 0 && userRoles.size() > 2; --i) {
                        if (((KUserRole)userRoles.get(i)).getUser() != null) continue;
                        userRoles.remove(i);
                    }
                    userRoles.subList(2, userRoles.size()).clear();
                } else if (prevGameType == KGameType.RENGO) {
                    userRoles.remove(3);
                    userRoles.remove(1);
                }
                if (userRoles.isEmpty()) {
                    userRoles.add(new KUserRole<Object>(null, KRole.WHITE));
                }
                if (userRoles.size() != 1) break;
                ((KUserRole)userRoles.get(0)).setRole(KRole.WHITE);
                userRoles.add(new KUserRole<Object>(null, KRole.BLACK));
                break;
            }
            default: {
                throw new IllegalArgumentException(gameType.toString());
            }
        }
        super.setGameType(gameType);
    }

    @Override
    public boolean isAddUserRoleAllowed() {
        return this.getGameType() == KGameType.SIMUL && this.getMutableUserRoles().size() < 101;
    }

    @Override
    public boolean isRemoveUserRoleAllowed() {
        return this.getGameType() == KGameType.SIMUL && this.getMutableUserRoles().size() > 3;
    }

    static {
        KGameType.DEMONSTRATION.toString();
        nigiriFlips = new Role[6];
        KProposal.nigiriFlips[KRole.WHITE.id] = KRole.BLACK;
        KProposal.nigiriFlips[KRole.BLACK.id] = KRole.WHITE;
        KProposal.nigiriFlips[KRole.WHITE_2.id] = KRole.BLACK_2;
        KProposal.nigiriFlips[KRole.BLACK_2.id] = KRole.WHITE_2;
    }

    public static class KUserRole<UserT extends User>
    extends Proposal.UserRole<UserT> {
        private int handicap;
        private float komi;

        protected KUserRole(UserT user, Role role) {
            this(user, role, 0, 0.0f);
        }

        protected KUserRole(UserT user, Role role, int newHandicap, float newKomi) {
            super(user, role);
            this.handicap = newHandicap;
            this.komi = newKomi;
        }

        public final int getHandicap() {
            return this.handicap;
        }

        public void setHandicap(int newHandicap) {
            this.handicap = newHandicap;
        }

        public final float getKomi() {
            return this.komi;
        }

        public void setKomi(float newKomi) {
            this.komi = newKomi;
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            KUserRole peer = (KUserRole)obj;
            return this.handicap == peer.handicap && this.komi == peer.komi;
        }

        @Override
        public String toString() {
            return "KUserRole[" + this.getUser() + "," + this.getRole() + ",hc=" + this.handicap + ",komi=" + this.komi + "]";
        }
    }
}
