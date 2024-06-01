/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.shared;

import org.igoweb.igoweb.shared.Proposal;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.User;

public static class KProposal.KUserRole<UserT extends User>
extends Proposal.UserRole<UserT> {
    private int handicap;
    private float komi;

    protected KProposal.KUserRole(UserT user, Role role) {
        this(user, role, 0, 0.0f);
    }

    protected KProposal.KUserRole(UserT user, Role role, int newHandicap, float newKomi) {
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
        KProposal.KUserRole peer = (KProposal.KUserRole)obj;
        return this.handicap == peer.handicap && this.komi == peer.komi;
    }

    @Override
    public String toString() {
        return "KUserRole[" + this.getUser() + "," + this.getRole() + ",hc=" + this.handicap + ",komi=" + this.komi + "]";
    }

    static /* synthetic */ int access$000(KProposal.KUserRole x0) {
        return x0.handicap;
    }

    static /* synthetic */ float access$100(KProposal.KUserRole x0) {
        return x0.komi;
    }

    static /* synthetic */ int access$002(KProposal.KUserRole x0, int x1) {
        x0.handicap = x1;
        return x0.handicap;
    }

    static /* synthetic */ float access$102(KProposal.KUserRole x0, float x1) {
        x0.komi = x1;
        return x0.komi;
    }
}
