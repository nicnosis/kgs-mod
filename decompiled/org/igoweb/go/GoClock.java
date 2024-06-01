/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.igoweb.games.Clock;
import org.igoweb.util.Defs;

public class GoClock
extends Clock {
    private final long mainTime;
    private final long byTime;
    private final int aux;
    private int stonesLeft;
    private final boolean canadian;

    public GoClock(long newMainTime, long newByTime, int newAux, boolean newCanadian) {
        this.mainTime = newMainTime;
        this.byTime = newByTime;
        this.aux = newAux;
        this.canadian = newCanadian;
        this.reset();
    }

    public final void setSecs(double time, int newAux) {
        this.setMs((long)(time * 1000.0), newAux);
    }

    public void setMs(long time, int auxVal) {
        boolean forceChange = false;
        if (this.canadian) {
            if (this.stonesLeft != auxVal) {
                forceChange = true;
            }
            this.stonesLeft = auxVal;
            if (auxVal == 0) {
                time += this.byTime;
            }
            if (time != this.timeLeft) {
                forceChange = false;
            }
        } else {
            time += this.byTime * (long)(auxVal == 0 ? this.aux : auxVal - 1);
        }
        super.setMs(time, forceChange);
    }

    @Override
    public String getCurrentReadout(long now) {
        return this.format(this.getPeriodMsLeft(now), this.getAuxValue());
    }

    @Override
    public long getPeriodMsLeft(long now) {
        long time = this.getTotalMsLeft(now);
        if (time == 0L) {
            return 0L;
        }
        if (this.canadian) {
            if (this.stonesLeft == 0) {
                time -= this.byTime;
            }
        } else if (time <= (long)this.aux * this.byTime) {
            if ((time %= this.byTime) == 0L) {
                time = this.byTime;
            }
        } else {
            time -= (long)this.aux * this.byTime;
        }
        return time;
    }

    private String format(long curTimeLeft, int byVal) {
        int format = byVal == 0 ? -900134593 : (this.canadian ? -1337055798 : -1337055797);
        int itime = (int)((curTimeLeft + 999L) / 1000L);
        return Defs.getString(format, new Object[]{itime / 60, itime % 60, byVal});
    }

    @Override
    public Collection<String> getSampleReadouts() {
        ArrayList<String> result = new ArrayList<String>();
        result.add(this.format(this.mainTime, 0));
        result.add(this.format(this.byTime, this.aux));
        if (!this.canadian) {
            result.add(this.format(this.byTime, 0));
        }
        return result;
    }

    @Override
    protected void doUpdate(long now, boolean stopping) {
        super.doUpdate(now, stopping);
        if (this.canadian) {
            if (this.stonesLeft == 0 && this.timeLeft < this.byTime) {
                this.stonesLeft = this.aux;
            }
            if (stopping && this.stonesLeft > 0) {
                --this.stonesLeft;
                if (this.stonesLeft == 0 && this.timeLeft > 0L) {
                    this.stonesLeft = this.aux;
                    this.timeLeft = this.byTime;
                }
            }
        } else if (stopping && this.timeLeft < this.byTime * (long)this.aux) {
            this.timeLeft = (this.timeLeft + this.byTime - 1L) / this.byTime * this.byTime;
        }
    }

    public int getAuxValue() {
        if (this.canadian) {
            return this.stonesLeft;
        }
        long time = this.timeLeft;
        return time > (long)this.aux * this.byTime ? 0 : (int)((time + this.byTime - 1L) / this.byTime);
    }

    @Override
    public void reset() {
        int oldStonesLeft = this.stonesLeft;
        this.stonesLeft = this.mainTime == 0L ? this.aux : 0;
        super.setMs(this.canadian ? this.mainTime + this.byTime : this.mainTime + this.byTime * (long)this.aux, this.canadian && this.stonesLeft != oldStonesLeft);
    }

    @Override
    public void writeState(DataOutput out) throws IOException {
        super.writeState(out);
        if (this.canadian) {
            out.writeByte(this.stonesLeft);
        }
    }

    @Override
    public void readState(DataInput in) throws IOException {
        super.readState(in);
        if (this.canadian) {
            this.stonesLeft = in.readByte() & 0xFF;
        }
    }

    @Override
    public void setMs(long newVal) {
        throw new RuntimeException();
    }

    @Override
    public boolean isFinalPeriod() {
        return this.canadian ? this.stonesLeft != 0 : this.getTotalMsLeft(0L) <= (long)this.aux * this.byTime;
    }

    @Override
    public Object getState() {
        long[] res = new long[]{this.canadian ? this.getPeriodMsLeft() : this.getTotalMsLeft(0L), this.stonesLeft};
        return res;
    }

    @Override
    public void setState(Object obj) {
        this.update(false);
        long[] state = (long[])obj;
        this.setMs(state[0], this.canadian ? (int)state[1] : 1);
    }

    public long getByTime() {
        return this.byTime;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + (this.canadian ? "Canadian" : "Byo-yomi") + ", main=" + this.mainTime + ", byTime=" + this.byTime + ", aux=" + this.aux + ", timeLeft=" + (this.timeLeft + 999L) / 1000L + ", stonesLeft=" + this.stonesLeft + "]";
    }
}
