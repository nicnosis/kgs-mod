/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import org.igoweb.games.AbsoluteClock;
import org.igoweb.games.Clock;
import org.igoweb.games.NullClock;
import org.igoweb.go.GoClock;
import org.igoweb.util.Bundle;
import org.igoweb.util.Defs;
import org.igoweb.util.Emitter;

public class Rules
extends Emitter
implements Serializable {
    public static final int MAX_EST_TIME_PER_PLAYER = 7200;
    public static final int MIN_SIZE = 2;
    public static final int MAX_SIZE = 38;
    public static final int MAX_TIME = 2147483;
    public static final int MAX_BY_PERIODS = 255;
    public static final int MAX_BY_STONES = 255;
    public static final int JAPANESE = 0;
    public static final int CHINESE = 1;
    public static final int AGA = 2;
    public static final int NEW_ZEALAND = 3;
    public static final int NUM_TYPES = 4;
    public static final int TIME_NONE = 0;
    public static final int TIME_ABSOLUTE = 1;
    public static final int TIME_BYOYOMI = 2;
    public static final int TIME_CANADIAN = 3;
    public static final int NUM_TIME_SYSTEMS = 4;
    public static final int KO_SIMPLE = 0;
    public static final int KO_SAMEBOARD = 1;
    public static final int KO_SAMESTATE = 2;
    private int type = 0;
    private int size = 19;
    private int handicap = 0;
    private float komi = 0.0f;
    private int timeSystem = 0;
    private int mainTime = 1800;
    private int byTime = 30;
    private int canByTime = 600;
    private int byPeriods = 5;
    private int byStones = 25;
    public static final int EVENT_BASE = 0;
    public static final int TYPE_CHANGED_EVENT = 0;
    public static final int SIZE_CHANGED_EVENT = 1;
    public static final int HANDICAP_CHANGED_EVENT = 2;
    public static final int KOMI_CHANGED_EVENT = 3;
    public static final int TIME_SYSTEM_CHANGED_EVENT = 4;
    public static final int MAIN_TIME_CHANGED_EVENT = 5;
    public static final int BYO_YOMI_TIME_CHANGED_EVENT = 6;
    public static final int BYO_YOMI_PERIODS_CHANGED_EVENT = 7;
    public static final int BYO_YOMI_STONES_CHANGED_EVENT = 8;
    public static final int EVENT_LIMIT = 9;
    public static final int MAX_ULTRA_BLITZ_TIME = 450;
    public static final int MAX_BLITZ_TIME = 900;

    public Rules() {
    }

    public Rules(Rules original) {
        this.setType(original.type);
        this.setSize(original.size);
        this.setHandicap(original.handicap);
        this.setKomi(original.komi);
        this.setTimeSystem(original.timeSystem);
        if (original.getMainTime() >= 0) {
            this.setMainTime(original.getMainTime());
        }
        if (original.getByoYomiTime() >= 0) {
            this.setByoYomiTime(original.getByoYomiTime());
        }
        if (original.getByoYomiPeriods() >= 0) {
            this.setByoYomiPeriods(original.getByoYomiPeriods());
        }
        if (original.getByoYomiStones() >= 0) {
            this.setByoYomiStones(original.getByoYomiStones());
        }
    }

    public Rules(int newSize) {
        this.setSize(newSize);
    }

    public Rules(DataInput in) throws IOException {
        try {
            this.setType(in.readByte());
            this.setSize(in.readByte());
            this.setHandicap(in.readByte());
            this.setKomi((float)in.readShort() * 0.5f);
            this.setTimeSystem(in.readByte());
            if (this.timeSystem != 0) {
                this.setMainTime(in.readInt());
                if (this.timeSystem != 1) {
                    this.setByoYomiTime(in.readInt());
                    int auxTime = in.readByte() & 0xFF;
                    if (this.timeSystem == 2) {
                        this.setByoYomiPeriods(auxTime);
                    } else {
                        this.setByoYomiStones(auxTime);
                    }
                }
            }
        }
        catch (IllegalArgumentException excep) {
            throw new IOException(excep);
        }
    }

    public void copyTo(Rules dest) {
        dest.type = this.type;
        dest.size = this.size;
        dest.timeSystem = this.timeSystem;
        dest.byPeriods = this.byPeriods;
        dest.byTime = this.byTime;
        dest.byStones = this.byStones;
        dest.komi = this.komi;
        dest.handicap = this.handicap;
        dest.canByTime = this.canByTime;
        dest.mainTime = this.mainTime;
    }

    public final boolean isSuicideLegal() {
        return this.type == 3;
    }

    public final boolean scoreSeki() {
        return this.type != 0;
    }

    public final boolean isScoreCaptures() {
        return this.type == 0;
    }

    public final boolean isScoreLivingStones() {
        return this.type != 0;
    }

    public final boolean isFixedHandicap() {
        return this.type == 0 || this.type == 2;
    }

    public final int getKoType() {
        if (this.type == 0) {
            return 0;
        }
        if (this.type == 1) {
            return 1;
        }
        return 2;
    }

    public int getHandicapComp() {
        if (this.type == 2 && this.handicap > 0) {
            return this.handicap - 1;
        }
        if (this.type == 1) {
            return this.handicap;
        }
        return 0;
    }

    public boolean isBlackFirst() {
        return this.handicap == 0 || !this.isFixedHandicap();
    }

    public float getDefaultKomi() {
        switch (this.type) {
            case 1: {
                return 7.5f;
            }
            case 0: {
                return 6.5f;
            }
            case 3: {
                return 7.0f;
            }
            case 2: {
                return 7.5f;
            }
        }
        throw new IllegalArgumentException(Integer.toString(this.type));
    }

    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(this.getClass())) {
            return false;
        }
        Rules peer = (Rules)obj;
        return this.type == peer.type && this.size == peer.size && this.handicap == peer.handicap && this.komi == peer.komi && this.timeSystem == peer.timeSystem && (this.timeSystem == 0 || this.mainTime == peer.mainTime) && (this.timeSystem != 2 || this.byTime == peer.byTime && this.byPeriods == peer.byPeriods) && (this.timeSystem != 3 || this.canByTime == peer.canByTime && this.byStones == peer.byStones);
    }

    public void setSize(int newSize) {
        int oldSize = this.size;
        if (oldSize != newSize) {
            if (newSize < 2 || newSize > 38) {
                throw new IllegalArgumentException("Illegal board size " + newSize);
            }
            this.size = newSize;
            this.emit(1);
        }
    }

    public final int getSize() {
        return this.size;
    }

    public void setType(int newType) {
        int oldType = this.type;
        if (oldType != newType) {
            if (newType < 0 || newType >= 4) {
                throw new IllegalArgumentException("Illegal rules type " + newType);
            }
            this.type = newType;
            this.emit(0);
        }
    }

    public final int getType() {
        return this.type;
    }

    public void setHandicap(int newHandicap) {
        int oldHandicap = this.handicap;
        if (oldHandicap != newHandicap) {
            if (newHandicap < 0 || newHandicap == 1) {
                throw new IllegalArgumentException("Illegal handicap " + newHandicap);
            }
            this.handicap = newHandicap;
            this.emit(2);
        }
    }

    public final int getHandicap() {
        return this.handicap;
    }

    public void setKomi(float newKomi) {
        float oldKomi = this.komi;
        if (oldKomi != newKomi) {
            this.komi = newKomi;
            this.emit(3);
        }
    }

    public final float getKomi() {
        return this.komi;
    }

    public void setTimeSystem(int newSystem) {
        int oldSystem = this.timeSystem;
        if (oldSystem != newSystem) {
            if (newSystem < 0 || newSystem >= 4) {
                throw new IllegalArgumentException("Illegal time system " + newSystem);
            }
            int oldMainTime = this.getMainTime();
            int oldByoYomiTime = this.getByoYomiTime();
            int oldByoYomiPeriods = this.getByoYomiPeriods();
            int oldByoYomiStones = this.getByoYomiStones();
            this.timeSystem = newSystem;
            this.emit(4);
            if (oldMainTime != this.getMainTime()) {
                this.emit(5);
            }
            if (oldByoYomiTime != this.getByoYomiTime()) {
                this.emit(6);
            }
            if (oldByoYomiPeriods != this.getByoYomiPeriods()) {
                this.emit(7);
            }
            if (oldByoYomiStones != this.getByoYomiStones()) {
                this.emit(8);
            }
        }
    }

    public final int getTimeSystem() {
        return this.timeSystem;
    }

    public void setMainTime(int newMainTime) {
        if (newMainTime == -1) {
            this.setTimeSystem(0);
            return;
        }
        this.checkOverflow(newMainTime);
        if (this.timeSystem == 2) {
            this.checkOverflow(newMainTime + this.byTime * this.byPeriods);
        }
        if (this.mainTime == newMainTime) {
            return;
        }
        this.mainTime = newMainTime;
        if (this.timeSystem == 0) {
            this.setTimeSystem(1);
        } else {
            this.emit(5);
        }
    }

    public int getMainTime() {
        return this.timeSystem == 0 ? -1 : this.mainTime;
    }

    public void setByoYomiTime(int newByTime) {
        if (newByTime == -1) {
            if (this.timeSystem > 1) {
                this.setTimeSystem(1);
            }
            return;
        }
        this.checkOverflow(newByTime);
        if (this.timeSystem != 3) {
            this.checkOverflow(this.mainTime + newByTime * this.byStones);
        }
        if (this.timeSystem <= 1) {
            this.byTime = newByTime;
            this.canByTime = newByTime;
            this.setTimeSystem(2);
        } else {
            int oldByTime;
            int n = oldByTime = this.timeSystem == 2 ? this.byTime : this.canByTime;
            if (oldByTime != newByTime) {
                if (this.timeSystem == 2) {
                    this.byTime = newByTime;
                } else {
                    this.canByTime = newByTime;
                }
                this.emit(6);
            }
        }
    }

    public int getByoYomiTime() {
        if (this.timeSystem == 2) {
            return this.byTime;
        }
        if (this.timeSystem == 3) {
            return this.canByTime;
        }
        return -1;
    }

    public void setByoYomiPeriods(int newByPeriods) {
        if (newByPeriods == -1) {
            if (this.timeSystem == 2) {
                this.setTimeSystem(1);
            }
            return;
        }
        if (newByPeriods <= 0 || newByPeriods > 255) {
            throw new IllegalArgumentException("Illegal number of byo-yomi periods " + newByPeriods);
        }
        if (this.timeSystem == 2) {
            this.checkOverflow(this.mainTime + this.byTime * newByPeriods);
            int oldVal = this.byPeriods;
            if (oldVal != newByPeriods) {
                this.byPeriods = newByPeriods;
                this.emit(7);
            }
        } else {
            this.byPeriods = newByPeriods;
            this.setTimeSystem(2);
        }
    }

    public int getByoYomiPeriods() {
        return this.timeSystem == 2 ? this.byPeriods : -1;
    }

    public void setByoYomiStones(int newByStones) {
        if (newByStones == -1) {
            if (this.timeSystem == 3) {
                this.setTimeSystem(1);
            }
            return;
        }
        if (newByStones <= 0 || newByStones > 255) {
            throw new IllegalArgumentException("Illegal number of byo-yomi stones " + newByStones);
        }
        this.setTimeSystem(3);
        int oldVal = this.byStones;
        if (oldVal != newByStones) {
            this.byStones = newByStones;
            this.emit(8);
        }
    }

    public int getByoYomiStones() {
        return this.timeSystem == 3 ? this.byStones : -1;
    }

    public void setHandicapAndKomi(int wRank, int bRank) {
        if (bRank < 1 || wRank < 1) {
            this.setHandicap(0);
            this.setKomi(this.getDefaultKomi());
        } else {
            int rankDiff = wRank - bRank;
            if (rankDiff == 0) {
                this.setHandicap(0);
                this.setKomi(this.getDefaultKomi());
            } else if (rankDiff > 0) {
                int hcap;
                if (wRank > 39) {
                    rankDiff = 1;
                }
                if ((hcap = rankDiff) > 0 && (hcap *= this.size * this.size / 361) == 0) {
                    hcap = 1;
                }
                if (hcap > 9) {
                    hcap = 9;
                }
                this.setHandicap(hcap > 1 ? hcap : 0);
                this.setKomi(this.type == 3 ? 0.0f : 0.5f);
            } else {
                if (bRank > 39) {
                    rankDiff = -1;
                }
                this.setHandicap(0);
                if (rankDiff < -9) {
                    rankDiff = -9;
                }
                this.setKomi(((float)rankDiff + 0.5f) * 2.0f * this.getDefaultKomi());
            }
        }
    }

    public int hashCode() {
        return this.hashCode(0);
    }

    public int hashCode(int val) {
        val = val * -1640524983 + this.type;
        val = val * -1640524983 + this.size;
        val = val * -1640524983 + this.handicap;
        val = val * -1640524983 + Float.floatToRawIntBits(this.komi);
        val = val * -1640524983 + this.timeSystem;
        if (this.timeSystem != 0) {
            val = val * -1640524983 + this.mainTime;
            if (this.timeSystem == 2) {
                val = val * -1640524983 + this.byTime;
                val = val * -1640524983 + this.byPeriods;
            } else if (this.timeSystem == 3) {
                val = val * -1640524983 + this.canByTime;
                val = val * -1640524983 + this.byStones;
            }
        }
        return val;
    }

    public static String getTypeName(int typeIn) {
        switch (typeIn) {
            case 0: {
                return "japanese";
            }
            case 1: {
                return "chinese";
            }
            case 2: {
                return "aga";
            }
            case 3: {
                return "new_zealand";
            }
        }
        throw new IllegalArgumentException("Illlegal rules type: " + typeIn);
    }

    public static int getTypeFromName(String name) {
        if (name.equals("japanese")) {
            return 0;
        }
        if (name.equals("chinese")) {
            return 1;
        }
        if (name.equals("aga")) {
            return 2;
        }
        if (name.equals("new_zealand")) {
            return 3;
        }
        throw new IllegalArgumentException("Illlegal rules type: " + name);
    }

    public static String getTimeSystemName(int timeSystemIn) {
        switch (timeSystemIn) {
            case 0: {
                return "none";
            }
            case 1: {
                return "absolute";
            }
            case 2: {
                return "byo_yomi";
            }
            case 3: {
                return "canadian";
            }
        }
        throw new IllegalArgumentException("Illlegal time system: " + timeSystemIn);
    }

    public static int getTimeSystemFromName(String name) {
        if (name.equals("none")) {
            return 0;
        }
        if (name.equals("absolute")) {
            return 1;
        }
        if (name.equals("byo_yomi")) {
            return 2;
        }
        if (name.equals("canadian")) {
            return 3;
        }
        throw new IllegalArgumentException("Illegal time system: " + name);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName());
        sb.append('[');
        this.toString(sb);
        return sb.append(']').toString();
    }

    protected void toString(StringBuilder sb) {
        sb.append("size=").append(this.size).append(", type=").append(this.type).append(", handicap=").append(this.handicap).append(", komi=").append(this.komi).append(", timeSystem=").append(this.timeSystem);
        if (this.timeSystem != 0) {
            sb.append(", mainTime=").append(this.mainTime);
            if (this.timeSystem == 2) {
                sb.append(", byoYomiTime=").append(this.byTime).append(", byoYomiPeriods=").append(this.byPeriods);
            } else if (this.timeSystem == 3) {
                sb.append(", byoYomiTime=").append(this.canByTime).append(", byoYomiStones=").append(this.byStones);
            }
        }
    }

    public void writeTo(DataOutput out) throws IOException {
        out.write(this.type);
        out.write(this.size);
        out.write(this.handicap);
        out.writeShort((short)(this.komi * 2.0f));
        out.write(this.timeSystem);
        if (this.timeSystem != 0) {
            out.writeInt(this.mainTime);
            if (this.timeSystem == 2) {
                out.writeInt(this.byTime);
                out.write(this.byPeriods);
            } else if (this.timeSystem == 3) {
                out.writeInt(this.canByTime);
                out.write(this.byStones);
            }
        }
    }

    public int estimateTimePerPlayer() {
        int result;
        int movesPerGame = this.size * this.size * 7 / 20;
        switch (this.timeSystem) {
            case 0: {
                result = 7200;
                break;
            }
            case 1: {
                result = this.mainTime;
                break;
            }
            case 2: {
                result = this.mainTime + this.byPeriods * this.byTime + (this.byTime - 1) * movesPerGame / 2;
                break;
            }
            case 3: {
                int effectiveByStones = Math.min(this.byStones, movesPerGame);
                double timeUsed = Math.log(effectiveByStones) * 0.1 + 0.5;
                if (timeUsed > 1.0) {
                    timeUsed = 1.0;
                }
                result = this.mainTime + (int)((double)(movesPerGame * this.canByTime) * timeUsed / (double)effectiveByStones);
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
        if (result > 7200) {
            result = 7200;
        }
        return result;
    }

    public String getTimeDescription() {
        return this.getTimeDescription(Defs.getBundle());
    }

    public String getTimeDescription(Bundle bundle) {
        if (this.timeSystem == 0) {
            return bundle.str(-1337055793);
        }
        Object[] args = new Object[7];
        args[0] = this.timeSystem;
        args[1] = this.mainTime / 3600;
        args[2] = this.mainTime / 60 % 60;
        args[3] = this.mainTime % 60;
        if (this.timeSystem == 2) {
            args[4] = this.byTime / 60;
            args[5] = this.byTime % 60;
            args[6] = this.byPeriods;
        } else {
            args[4] = this.canByTime / 60;
            args[5] = this.canByTime % 60;
            args[6] = this.byStones;
        }
        return bundle.str(-1337055792, args);
    }

    public Clock buildClock() {
        switch (this.timeSystem) {
            case 0: {
                return new NullClock();
            }
            case 1: {
                return new AbsoluteClock((long)this.mainTime * 1000L){

                    @Override
                    protected String format(long time) {
                        int intTime = (int)(time / 1000L);
                        return Defs.getString(-1337055797, new Object[]{intTime / 60, intTime % 60, 0});
                    }
                };
            }
            case 2: {
                return new GoClock((long)this.mainTime * 1000L, (long)this.byTime * 1000L, this.byPeriods, false);
            }
            case 3: {
                return new GoClock((long)this.mainTime * 1000L, (long)this.canByTime * 1000L, this.byStones, true);
            }
        }
        throw new IllegalStateException("Unknown time system: " + this.timeSystem);
    }

    private void checkOverflow(int time) {
        if (time < 0 || time > 2147483) {
            throw new IllegalArgumentException("Illegal time: " + time);
        }
    }
}
