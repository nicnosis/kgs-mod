/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.util.Locale;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.util.SelectableEmitter;

public class User
extends SelectableEmitter
implements Comparable<User> {
    public static final int MAX_NAME_LEN = 10;
    public static final int EVENT_BASE = 0;
    public static final int FLAGS_CHANGED_EVENT = 0;
    public static final int AUTH_LEVEL_CHANGED_EVENT = 1;
    public static final int GUEST_CHANGED_EVENT = 2;
    public static final int CONNECTED_CHANGED_EVENT = 3;
    public static final int DELETED_CHANGED_EVENT = 4;
    public static final int ROBOT_CHANGED_EVENT = 5;
    public static final int RANK_WANTED_CHANGED_EVENT = 6;
    public static final int RANK_CONFIDENT_CHANGED_EVENT = 7;
    public static final int AVATAR_CHANGED_EVENT = 8;
    public static final int SUBSCRIBED_CHANGED_EVENT = 9;
    public static final int HELPFUL_CHANGED_EVENT = 10;
    public static final int TOURN_WINNER_CHANGED_EVENT = 11;
    public static final int TOURN_RUNNER_UP_CHANGED_EVENT = 12;
    public static final int PLAYING_CHANGED_EVENT = 13;
    public static final int IN_TOURN_CHANGED_EVENT = 14;
    public static final int RANK_CHANGED_EVENT = 15;
    public static final int SLEEPING_CHANGED_EVENT = 16;
    public static final int LOW_BANDWIDTH_CHANGED_EVENT = 17;
    public static final int LONG_LIVED_CHANGED_EVENT = 18;
    public static final int MEIJIN_CHANGED_EVENT = 19;
    public static final int USER_EVENT_LIMIT = 20;
    private static final int AUTH_LEVEL_MASK = 7;
    private static final int GUEST_BIT = 8;
    private static final int CONNECTED_BIT = 16;
    private static final int DELETED_BIT = 32;
    private static final int SLEEPING_BIT = 64;
    private static final int ROBOT_BIT = 128;
    private static final int RANK_WANTED_BIT = 256;
    private static final int RANK_CONFIDENT_BIT = 512;
    private static final int AVATAR_BIT = 1024;
    private static final int SUBSCRIBED_BIT = 2048;
    private static final int HELPFUL_BIT = 4096;
    private static final int TOURN_WINNER_BIT = 8192;
    private static final int TOURN_RUNNER_UP_BIT = 16384;
    private static final int PLAYING_BIT = 32768;
    private static final int IN_TOURN_BIT = 65536;
    private static final int LOW_BANDWIDTH_BIT = 131072;
    private static final int LONG_LIVED_BIT = 262144;
    private static final int MEIJIN_BIT = 524288;
    private static final int UNUSED_30 = 0x40000000;
    private static final int UNUSED_31 = Integer.MIN_VALUE;
    public static final int MAX_RANK = 1023;
    private static final int RANK_SHIFT = 20;
    private static final int RANK_MASK = 0x3FF00000;
    private static final int NAME_AND_RANK_MASK = 1072694024;
    public final String name;
    private String nameAndRank;
    private volatile int flags;

    public User(String newName, int newFlags) {
        this.name = newName;
        this.flags = newFlags;
    }

    public User(String newName) {
        this.name = newName;
        this.flags = 4360;
    }

    public boolean setFlags(int newFlags) {
        return this.set(-1, newFlags, 0);
    }

    public final int getAuthLevel() {
        return this.flags & 7;
    }

    public final boolean isGuest() {
        return (this.flags & 8) != 0;
    }

    public final boolean isConnected() {
        return (this.flags & 0x10) != 0;
    }

    public final boolean isDeleted() {
        return (this.flags & 0x20) != 0;
    }

    public final boolean isSleeping() {
        return (this.flags & 0x40) != 0;
    }

    public boolean isRobot() {
        return (this.flags & 0x80) != 0;
    }

    public final boolean isRankWanted() {
        return (this.flags & 0x100) != 0;
    }

    public final boolean isRankConfident() {
        return (this.flags & 0x200) != 0;
    }

    public final boolean isAvatar() {
        return (this.flags & 0x400) != 0;
    }

    public final boolean isTournWinner() {
        return (this.flags & 0x2000) != 0;
    }

    public final boolean isTournRunnerUp() {
        return (this.flags & 0x4000) != 0;
    }

    public final boolean isPlaying() {
        return (this.flags & 0x8000) != 0;
    }

    public final boolean isInTourn() {
        return (this.flags & 0x10000) != 0;
    }

    public final boolean isSubscribed() {
        return (this.flags & 0x800) != 0;
    }

    public final boolean isLowBandwidth() {
        return (this.flags & 0x20000) != 0;
    }

    public final boolean isLongLived() {
        return (this.flags & 0x40000) != 0;
    }

    public final boolean isMeijin() {
        return (this.flags & 0x80000) != 0;
    }

    public int getRank() {
        return (this.flags & 0x100) == 0 ? 0 : this.flags >> 20 & 0x3FF;
    }

    public boolean canPlayRanked() {
        return (this.flags & 0x108) == 256;
    }

    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        User peer = (User)obj;
        return this.name.equals(peer.name) && this.flags == peer.flags;
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[name=" + this.name + ", " + Integer.toString(this.flags, 16) + ']';
    }

    public int getRankSortValue() {
        if ((this.flags & 0x100) == 0) {
            return 0;
        }
        int val = this.getRank();
        if ((this.flags & 0x200) != 0) {
            val += 1024;
        }
        return val;
    }

    public static boolean nameValid(String testName) {
        if (testName.length() < 1 || testName.length() > 10) {
            return false;
        }
        for (int i = 0; i < testName.length(); ++i) {
            char c = testName.charAt(i);
            if ((i != 0 || c < '0' || c > '9') && (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9')) continue;
            return false;
        }
        return true;
    }

    public static long passwordCompute(String password) {
        long result = 0L;
        for (int i = 0; i < password.length(); ++i) {
            result = result * 1055L + (long)password.charAt(i);
        }
        return result;
    }

    public boolean isHelpful() {
        return (this.flags & 0x1000) != 0;
    }

    public String canonName() {
        return User.canonName(this.name);
    }

    public static String canonName(String nameIn) {
        return nameIn.toLowerCase(Locale.ENGLISH);
    }

    public String getName() {
        return this.name;
    }

    public final String getNameAndRank() {
        return this.getNameAndRank(IBundle.get());
    }

    public String getNameAndRank(IBundle bundle) {
        if (this.nameAndRank == null) {
            this.nameAndRank = this.formatNameAndRank(bundle);
        }
        return this.nameAndRank;
    }

    protected String formatNameAndRank(IBundle bundle) {
        if (this.isGuest()) {
            return this.name;
        }
        return User.formatNameAndRank(this.name, this.getRank(), this.isRankWanted(), this.isRankConfident(), bundle);
    }

    public static String formatNameAndRank(String nameIn, int rank, boolean rankWanted, boolean confident, IBundle bundle) {
        return bundle.str(-669080772, new Object[]{nameIn, bundle.formatRank(rank, rankWanted, confident)});
    }

    public boolean setAuthLevel(int newAuthLevel) {
        if (newAuthLevel < 0 || newAuthLevel >= 6) {
            throw new IllegalArgumentException("Bad auth level: " + newAuthLevel);
        }
        return this.set(7, newAuthLevel, 1);
    }

    public boolean setGuest(boolean guest) {
        return this.set(8, guest ? 8 : 0, 2);
    }

    public boolean setConnected(boolean connected) {
        return this.set(16, connected ? 16 : 0, 3);
    }

    public boolean delete() {
        return this.set(32, 32, 4);
    }

    public final boolean setRobot(boolean robot) {
        return this.set(128, robot ? 128 : 0, 5);
    }

    public final boolean setSleeping(boolean sleeping) {
        return this.set(64, sleeping ? 64 : 0, 16);
    }

    public boolean setRankWanted(boolean rankWanted) {
        if (!this.set(256, rankWanted ? 256 : 0, 6)) {
            return false;
        }
        if ((this.flags & 0x3FF00000) != 0) {
            this.emit(15);
        }
        return true;
    }

    public final boolean setRankConfident(boolean rankConfident) {
        return this.set(512, rankConfident ? 512 : 0, 7);
    }

    public boolean setAvatar(boolean avatar) {
        return this.set(1024, avatar ? 1024 : 0, 8);
    }

    public boolean setSubscribed(boolean subscribed) {
        return this.set(2048, subscribed ? 2048 : 0, 9);
    }

    public boolean setHelpful(boolean helpful) {
        return this.set(4096, helpful ? 4096 : 0, 10);
    }

    public boolean setTournWinner(boolean tournWinner) {
        return this.set(8192, tournWinner ? 8192 : 0, 11);
    }

    public boolean setTournRunnerUp(boolean tournRunnerUp) {
        return this.set(16384, tournRunnerUp ? 16384 : 0, 12);
    }

    public boolean setPlaying(boolean playing) {
        return this.set(32768, playing ? 32768 : 0, 13);
    }

    public boolean setInTourn(boolean inTourn) {
        return this.set(65536, inTourn ? 65536 : 0, 14);
    }

    public boolean setLowBandwidth(boolean lowBandwidth) {
        return this.set(131072, lowBandwidth ? 131072 : 0, 17);
    }

    public boolean setLongLived(boolean longLived) {
        return this.set(262144, longLived ? 262144 : 0, 18);
    }

    public boolean setMeijin(boolean meijin) {
        return this.set(524288, meijin ? 524288 : 0, 19);
    }

    public boolean setRank(int newRank) {
        if (newRank > 1023 || newRank < 0) {
            throw new IllegalArgumentException("Bad rank: " + newRank);
        }
        return this.set(0x3FF00000, newRank << 20, (this.flags & 0x100) == 0 ? -1 : 15);
    }

    protected boolean set(int mask, int newVal, int eventType) {
        int oldFlags = this.flags;
        int newFlags = oldFlags & ~mask | newVal;
        if (oldFlags == newFlags) {
            return false;
        }
        this.flags = newFlags;
        if (((oldFlags ^ newFlags) & 0x3FF00308) != 0) {
            this.nameAndRank = null;
        }
        if (eventType != -1) {
            this.emit(eventType);
        }
        return true;
    }

    public void emitFlagsChanged() {
        this.emit(0);
    }

    public void writeTo(TxMessage msg) {
        msg.writeUTF(this.name);
        msg.writeInt(this.flags);
    }

    public final int getFlags() {
        return this.flags;
    }

    @Override
    public int compareTo(User peer) {
        return this.name.compareToIgnoreCase(peer.name);
    }

    public static class Comparator
    implements java.util.Comparator<User> {
        public static final int SORT_BY_NAME = 0;
        public static final int SORT_BY_RANK = 1;
        private int type;

        public Comparator() {
            this(0);
        }

        public Comparator(int sortType) {
            this.type = sortType;
        }

        public void setSortType(int newType) {
            this.type = newType;
        }

        public int getSortType() {
            return this.type;
        }

        @Override
        public int compare(User u1, User u2) {
            int r2;
            int r1;
            if (this.type == 1 && (r1 = u1.getRankSortValue()) != (r2 = u2.getRankSortValue())) {
                return r2 - r1;
            }
            return u1.name.compareToIgnoreCase(u2.name);
        }
    }
}
