/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.PlayerContainer;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.igoweb.shared.User;

public abstract class Proposal<UserT extends User, UserRoleT extends UserRole<UserT>, PropT extends Proposal<UserT, UserRoleT, PropT>>
implements PlayerContainer<UserT> {
    private GameType gameType;
    private boolean isPrivate;
    private boolean isUploaded;
    private final ArrayList<UserRoleT> userRoles = new ArrayList();
    private final List<UserRoleT> constList = Collections.unmodifiableList(this.userRoles);

    public Proposal(GameType newGameType) {
        this.setGameType(newGameType);
    }

    public Proposal(DataInput in, UserDecoder<UserT> userDecoder) throws IOException, NoSuchUserException {
        this.gameType = GameType.get(in);
        byte flags = in.readByte();
        this.isPrivate = (flags & 1) != 0;
        this.isUploaded = (flags & 2) != 0;
        int numRoles = in.readByte();
        if (numRoles < 0) {
            throw new IOException();
        }
        if (userDecoder == null) {
            userDecoder = userIn -> {
                if (!userIn.readUTF().isEmpty()) {
                    throw new IOException();
                }
                return null;
            };
        }
        for (int i = 0; i < numRoles; ++i) {
            this.userRoles.add(this.readUserRole(in, userDecoder));
        }
    }

    public Proposal(PropT initial) {
        this.gameType = ((Proposal)initial).getGameType();
        this.isPrivate = ((Proposal)initial).isPrivate();
        for (UserRole userRole : ((Proposal)initial).getUserRoles()) {
            this.userRoles.add(this.cloneUserRole(userRole));
        }
    }

    protected abstract UserRoleT readUserRole(DataInput var1, UserDecoder<UserT> var2) throws IOException, NoSuchUserException;

    protected abstract void writeUserRole(TxMessage var1, UserRoleT var2);

    protected abstract UserRoleT cloneUserRole(UserRoleT var1);

    public final GameType getGameType() {
        return this.gameType;
    }

    public void setGameType(GameType newGameType) {
        this.gameType = newGameType;
    }

    public void writeTo(TxMessage tx) {
        tx.write(this.gameType.id);
        byte flags = 0;
        if (this.isPrivate) {
            flags = (byte)(flags | 1);
        }
        if (this.isUploaded) {
            flags = (byte)(flags | 2);
        }
        tx.write(flags);
        tx.write(this.userRoles.size());
        for (UserRole userRole : this.userRoles) {
            this.writeUserRole(tx, userRole);
        }
    }

    protected void writeUser(TxMessage tx, UserT user) {
        tx.writeUTF(user == null ? "" : ((User)user).canonName());
    }

    public boolean isPrivate() {
        return this.isPrivate;
    }

    public void setPrivate(boolean newIsPrivate) {
        this.isPrivate = newIsPrivate;
    }

    public boolean isUploaded() {
        return this.isUploaded;
    }

    public void setUploaded(boolean newIsUploaded) {
        this.isUploaded = newIsUploaded;
    }

    public void validate(PropT initial, UserT owner) throws IOException {
        if (this.gameType == null) {
            throw new IOException("Null game type?");
        }
        if (this.gameType.isTournament() || this.isPrivate && this.gameType.isRanked()) {
            throw new IOException("Invalid proposal: " + this);
        }
        boolean ownerPresent = false;
        int roles = 0;
        for (UserRole userRole : this.userRoles) {
            if (userRole.getUser() == owner) {
                ownerPresent = true;
            } else if (userRole.getUser() != null && initial == null) {
                throw new IOException("Invalid initial proposal; contains non-owner " + userRole.getUser() + ": " + this);
            }
            roles |= 1 << userRole.getRole().id;
        }
        if (!ownerPresent || roles != this.gameType.roleMask) {
            throw new IOException("Invalid proposal from " + owner + ": " + this);
        }
    }

    public boolean isComplete() {
        for (UserRole userRole : this.userRoles) {
            if (userRole.getUser() != null) continue;
            return false;
        }
        return true;
    }

    public UserRoleT getUserRole(int i) {
        return (UserRoleT)((UserRole)this.userRoles.get(i));
    }

    public List<UserRoleT> getUserRoles() {
        return this.constList;
    }

    protected List<UserRoleT> getMutableUserRoles() {
        return this.userRoles;
    }

    public Role getRole(UserT user) {
        UserRoleT ur = this.getUserRole(user);
        return ur == null ? null : ((UserRole)ur).getRole();
    }

    public UserRoleT getUserRole(UserT user) {
        for (UserRole ur : this.userRoles) {
            if (ur.getUser() != user) continue;
            return (UserRoleT)ur;
        }
        return null;
    }

    public UserRoleT getUserRole(Role role) {
        for (UserRole ur : this.userRoles) {
            if (ur.getRole() != role) continue;
            return (UserRoleT)ur;
        }
        return null;
    }

    @Override
    public UserT getPlayer(Role role) {
        UserRoleT ur = this.getUserRole(role);
        return ur == null ? null : (UserT)((UserRole)ur).getUser();
    }

    public abstract void addUserRole();

    public abstract void removeUserRole();

    public boolean isAddUserRoleAllowed() {
        return false;
    }

    public boolean isRemoveUserRoleAllowed() {
        return false;
    }

    public abstract TxMessage testRankedOk(int var1);

    public String toString() {
        StringBuilder sb = new StringBuilder("Proposal[");
        this.toString(sb);
        return sb.append(']').toString();
    }

    protected void toString(StringBuilder sb) {
        sb.append(this.gameType);
        if (this.isPrivate) {
            sb.append(",private");
        }
        for (UserRole ur : this.userRoles) {
            sb.append(",").append(ur);
        }
    }

    public byte[] toBytes() {
        TxMessage tx = new TxMessage();
        this.writeTo(tx);
        return tx.closeAndGetExactBytes();
    }

    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Proposal peer = (Proposal)obj;
        if (this.gameType != peer.gameType || this.isPrivate != peer.isPrivate || this.userRoles.size() != peer.userRoles.size()) {
            return false;
        }
        for (int i = 0; i < this.userRoles.size(); ++i) {
            if (((UserRole)this.userRoles.get(i)).equals(peer.userRoles.get(i))) continue;
            return false;
        }
        return true;
    }

    public int getCount(Role role) {
        int num = 0;
        for (UserRole userRole : this.userRoles) {
            if (userRole.getRole() != role) continue;
            ++num;
        }
        return num;
    }

    public static class NoSuchUserException
    extends Exception {
        public final String name;

        public NoSuchUserException(String newName) {
            this.name = newName;
        }
    }

    public static class UserRole<UserT extends User> {
        private UserT user;
        private Role role;

        protected UserRole(UserT newUser, Role newRole) {
            this.user = newUser;
            this.role = newRole;
        }

        public final UserT getUser() {
            return this.user;
        }

        public void setUser(UserT newUser) {
            this.user = newUser;
        }

        public final Role getRole() {
            return this.role;
        }

        public void setRole(Role newRole) {
            this.role = newRole;
        }

        public boolean equals(Object obj) {
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            UserRole peer = (UserRole)obj;
            return this.user == peer.user && this.role == peer.role;
        }

        public String toString() {
            return "UserRole[" + this.user + "," + this.role + "]";
        }
    }

    public static interface UserDecoder<UserT extends User> {
        public UserT getUser(DataInput var1) throws IOException, NoSuchUserException;
    }
}
