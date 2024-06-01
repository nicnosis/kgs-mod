/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.shared;

import java.util.HashMap;
import org.igoweb.go.sgf.Prop;

public class Score {
    public static final short MAX = 16383;
    public static final short B_BY_TIME = 16384;
    public static final short B_BY_RESIGN = 16385;
    public static final short NO_RESULT = 16386;
    public static final short UNFINISHED = 16387;
    public static final short B_BY_FORFEIT = 16388;
    public static final short UNKNOWN = 16389;
    public static final short W_BY_TIME = -16384;
    public static final short W_BY_RESIGN = -16385;
    public static final short W_BY_FORFEIT = -16388;
    private static final HashMap<String, Short> constants = Score.makeConstants();

    private Score() {
    }

    public static Prop toSgfProp(short val) {
        int iVal;
        float dVal = 0.0f;
        switch (val) {
            case 16389: {
                iVal = 5;
                break;
            }
            case 16387: {
                return null;
            }
            case 16386: {
                iVal = 4;
                break;
            }
            case 16385: {
                iVal = 1;
                dVal = -1.0f;
                break;
            }
            case -16385: {
                iVal = 1;
                dVal = 1.0f;
                break;
            }
            case 16388: {
                iVal = 3;
                dVal = -1.0f;
                break;
            }
            case -16388: {
                iVal = 3;
                dVal = 1.0f;
                break;
            }
            case 16384: {
                iVal = 2;
                dVal = -1.0f;
                break;
            }
            case -16384: {
                iVal = 2;
                dVal = 1.0f;
                break;
            }
            default: {
                iVal = 0;
                dVal = (float)val * -0.5f;
            }
        }
        return new Prop(25, iVal, dVal);
    }

    public static Prop toSgfProp(String sVal) {
        short val;
        Short valObj = constants.get(sVal);
        if (valObj == null) {
            float fVal = Float.parseFloat(sVal.substring(2));
            if (sVal.startsWith("W+")) {
                fVal = -fVal;
            } else if (!sVal.startsWith("B+")) {
                throw new IllegalArgumentException("Unrecognized score string " + sVal);
            }
            if (Math.abs(fVal) >= 8191.0f) {
                throw new IllegalArgumentException("Score too high: " + sVal);
            }
            val = (short)(fVal * 2.0f);
        } else {
            val = valObj;
        }
        return Score.toSgfProp(val);
    }

    public static String toString(short val) {
        String base = "B+";
        if (val < 0) {
            base = "W+";
            val = -val;
        }
        switch (val) {
            case 16389: {
                return "UNKNOWN";
            }
            case 0: {
                return "JIGO";
            }
            case 16387: {
                return "UNFINISHED";
            }
            case 16386: {
                return "NO_RESULT";
            }
            case 16385: {
                return base + "RESIGN";
            }
            case 16388: {
                return base + "FORFEIT";
            }
            case 16384: {
                return base + "TIME";
            }
        }
        return base + (float)val * 0.5f;
    }

    private static HashMap<String, Short> makeConstants() {
        HashMap<String, Short> result = new HashMap<String, Short>();
        result.put("0", (short)0);
        result.put("JIGO", (short)0);
        result.put("UNKNOWN", (short)16389);
        result.put("UNFINISHED", (short)16387);
        result.put("NO_RESULT", (short)16386);
        result.put("B+RESIGN", (short)16385);
        result.put("W+RESIGN", (short)-16385);
        result.put("B+FORFEIT", (short)16388);
        result.put("W+FORFEIT", (short)-16388);
        result.put("B+TIME", (short)16384);
        result.put("W+TIME", (short)-16384);
        return result;
    }
}
