/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.sgf.movie;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.igoweb.go.sgf.SgfEvent;

public class MovieEvent
extends SgfEvent {
    private static final int BASE = 10;
    public static final int START = 10;
    public static final int INITIAL_TREE_READY = 11;
    public static final int FILE_OVER = 12;
    public static final int POINTER_MOVED = 13;
    public static final int TIMESTAMP = 14;
    public static final int PING = 15;
    public static final int SPEEX_BASE = 16;
    public static final int SPEEX_FPP = 16;
    public static final int SPEEX_MUTE_CHANGED = 17;
    public static final int SPEEX_DATA = 18;
    public static final int SPEEX_LIMIT = 19;
    public static final String[] MOVIE_TYPE_NAMES = MovieEvent.buildMovieTypeNames();

    public MovieEvent(int newType, Object newArg) {
        super(-1, newType, newArg);
    }

    public static SgfEvent readMovieEvent(DataInput in, int newType, int activeNodeId) throws IOException {
        if (newType < 10) {
            return SgfEvent.readFrom(in, newType, activeNodeId);
        }
        switch (newType) {
            case 10: 
            case 11: 
            case 12: 
            case 15: {
                return new MovieEvent(newType, null);
            }
            case 13: {
                float[] fArray;
                float val1 = in.readFloat();
                if (Float.isNaN(val1)) {
                    fArray = null;
                } else {
                    float[] fArray2 = new float[2];
                    fArray2[0] = val1;
                    fArray = fArray2;
                    fArray2[1] = in.readFloat();
                }
                return new MovieEvent(newType, fArray);
            }
            case 14: 
            case 16: {
                return new MovieEvent(newType, (Object)in.readInt());
            }
            case 17: {
                return new MovieEvent(newType, in.readBoolean());
            }
            case 18: {
                byte[] data = new byte[in.readShort()];
                in.readFully(data);
                return new MovieEvent(newType, data);
            }
        }
        throw new IOException(Integer.toString(newType));
    }

    @Override
    public void writeTo(DataOutput out) throws IOException {
        if (this.type < 10) {
            super.writeTo(out);
            return;
        }
        out.write(this.type);
        switch (this.type) {
            case 10: 
            case 11: 
            case 12: 
            case 15: {
                break;
            }
            case 13: {
                float[] args = (float[])this.arg;
                if (args == null) {
                    out.writeFloat(Float.NaN);
                    break;
                }
                out.writeFloat(args[0]);
                out.writeFloat(args[1]);
                break;
            }
            case 14: 
            case 16: {
                out.writeInt((Integer)this.arg);
                break;
            }
            case 17: {
                out.writeBoolean((Boolean)this.arg);
                break;
            }
            case 18: {
                byte[] data = (byte[])this.arg;
                out.writeShort((short)data.length);
                out.write(data);
                break;
            }
            default: {
                throw new RuntimeException();
            }
        }
    }

    private static String[] buildMovieTypeNames() {
        String[] result = new String[19];
        System.arraycopy(TYPE_NAMES, 0, result, 0, TYPE_NAMES.length);
        result[10] = "START";
        result[11] = "INITIAL_TREE_READY";
        result[12] = "FILE_OVER";
        result[13] = "POINTER_MOVED";
        result[14] = "TIMESTAMP";
        result[15] = "PING";
        result[16] = "SPEEX_FPP";
        result[17] = "SPEEX_MUTE_CHANGED";
        result[18] = "SPEEX_DATA";
        return result;
    }
}
