/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.sgf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.StringTokenizer;
import org.igoweb.go.Loc;
import org.igoweb.go.Rules;

public class Prop
implements Comparable<Prop> {
    public static final int RULES = 0;
    public static final int GAMENAME = 1;
    public static final int PLAYERNAME = 2;
    public static final int PLAYERRANK = 3;
    public static final int DATE = 4;
    public static final int COPYRIGHT = 5;
    public static final int GAMECOMMENT = 6;
    public static final int EVENT = 7;
    public static final int ROUND = 8;
    public static final int PLACE = 9;
    public static final int PLAYERTEAM = 10;
    public static final int SOURCE = 11;
    public static final int ANNOTATOR = 12;
    public static final int TRANSCRIBER = 13;
    public static final int MOVE = 14;
    public static final int CIRCLE = 15;
    public static final int PHANTOMCLEAR = 16;
    public static final int ADDSTONE = 17;
    public static final int TIMELEFT = 18;
    public static final int LABEL = 19;
    public static final int TRIANGLE = 20;
    public static final int SQUARE = 21;
    public static final int TERRITORY = 22;
    public static final int DEAD = 23;
    public static final int COMMENT = 24;
    public static final int RESULT = 25;
    public static final int SETWHOSEMOVE = 26;
    public static final int MOVENUMBER = 27;
    public static final int NODENAME = 28;
    public static final int UNKNOWN = 29;
    public static final int CROSS = 30;
    public static final int ARROW = 31;
    public static final int LINE = 32;
    public static final int NUM_PROP_TYPES = 33;
    public static final short LOC_FLAG = 8;
    public static final short TEXT_FLAG = 1;
    public static final short COLOR_FLAG = 32;
    public static final short INT_FLAG = 128;
    public static final short FLOAT_FLAG = 256;
    public static final short LOC2_FLAG = 512;
    public static final short PASS_OK_FLAG = 1024;
    public static final short SYNTHETIC_FLAG = 2048;
    public static final short SETUP_FLAG = 8192;
    public static final short ROOT_FLAG = 2;
    private static final short DATA_MASK = 937;
    public static final short MARK_FLAG = 16;
    public static final short PLAYED_FLAG = 64;
    public static final short ANNOTATION_FLAG = 4;
    public static final short AUX_FLAG = 4096;
    private static final short IN_USE_FLAG = Short.MIN_VALUE;
    public static final int RESULT_SCORED = 0;
    public static final int RESULT_RESIGN = 1;
    public static final int RESULT_TIME = 2;
    public static final int RESULT_FORFEIT = 3;
    public static final int RESULT_NO_RESULT = 4;
    public static final int RESULT_UNKNOWN = 5;
    public static final int RESULT_MAX_IVAL = 6;
    private static short[] typeInfo = new short[0];
    public final int type;
    private final int color;
    private final int iVal;
    private final float fVal;
    private final Loc loc;
    private final Object aux;
    private static final Object dummyAux;
    public static final int DEFAULT_COLOR = 0;
    public static final int DEFAULT_IVAL = 0;
    public static final float DEFAULT_DVAL = 0.0f;

    public final boolean isMark() {
        return (typeInfo[this.type] & 0x10) != 0;
    }

    public final boolean isRoot() {
        return (typeInfo[this.type] & 2) != 0;
    }

    public final boolean isPlayed() {
        return (typeInfo[this.type] & 0x40) != 0;
    }

    public static boolean isMark(int typeIn) {
        return (typeInfo[typeIn] & 0x10) != 0;
    }

    public static boolean passOk(int typeIn) {
        return (typeInfo[typeIn] & 0x400) != 0;
    }

    public Prop(int newType, int newColor, Loc newLoc, int newIVal, float newFVal, Object newAux) {
        this(newType, newColor, newLoc, newAux, newIVal, newFVal);
        short info = typeInfo[newType];
        if ((info & 0x20) == 0 && newColor != 0 || (info & 8) == 0 != (newLoc == null) || (info & 1) != 0 && !(newAux instanceof String) || (info & 0x200) != 0 && !(newAux instanceof Loc) || newType == 0 && !(newAux instanceof Rules) || (info & 0x1201) == 0 != (newAux == null) || (info & 0x80) == 0 && newIVal != 0 || (info & 0x100) == 0 && newFVal != 0.0f) {
            throw new RuntimeException("Bad generalized param contructor, type " + newType);
        }
    }

    private Prop(int newType, int newColor, Loc newLoc, Object newAux, int newIVal, float newFVal) {
        this.type = newType;
        this.color = newColor;
        this.loc = newLoc == null ? Loc.PASS : newLoc;
        this.aux = newAux == null ? dummyAux : newAux;
        this.iVal = newIVal;
        this.fVal = newFVal;
        this.validate();
    }

    private void validate() {
        if (this.color < 0 || this.color >= 3) {
            throw new IllegalArgumentException("Illegal color " + this.color);
        }
        switch (this.type) {
            case 25: {
                if (this.iVal >= 0 && this.iVal < 6) break;
                throw new IllegalArgumentException("Illegal iVal " + this.iVal + " for a result property");
            }
            case 2: 
            case 3: 
            case 10: 
            case 14: 
            case 18: 
            case 22: {
                if (this.color != 2) break;
                throw new IllegalArgumentException("Color EMPTY is not allowed in property " + this.type);
            }
        }
    }

    public Prop(int newType, int colorOrInt, Loc newLoc) {
        this(newType, Prop.hasColor(newType) ? colorOrInt : 0, newLoc, null, Prop.hasColor(newType) ? 0 : colorOrInt, 0.0f);
        int dataFlags = typeInfo[newType] & 0x3A9;
        if (dataFlags != 40 && dataFlags != 136) {
            throw this.illegalArgs(newType, "int,Loc");
        }
        if (newLoc == Loc.PASS && !Prop.passOk(newType)) {
            throw new IllegalArgumentException("Invalid \"pass\" in param type " + newType);
        }
    }

    public Prop(int newType, Rules rules) {
        this(newType, 0, null, rules, 0, 0.0f);
        if (newType != 0) {
            throw this.illegalArgs(newType, "Rules");
        }
    }

    public Prop(int newType, int colorOrIval, String text) {
        this(newType, Prop.hasColor(newType) ? colorOrIval : 0, null, text, Prop.hasColor(newType) ? 0 : colorOrIval, 0.0f);
        int dataFlags = typeInfo[newType] & 0x3A9;
        if (dataFlags != 33 && dataFlags != 129) {
            throw this.illegalArgs(newType, "int,String");
        }
    }

    public Prop(int newType, String text) {
        this(newType, 0, null, text, 0, 0.0f);
        if ((typeInfo[newType] & 0x3A9) != 1) {
            throw this.illegalArgs(newType, "String");
        }
    }

    public Prop(int newType, Loc newLoc, String text) {
        this(newType, 0, newLoc, text, 0, 0.0f);
        if ((typeInfo[newType] & 0x3A9) != 9) {
            throw this.illegalArgs(newType, "Loc,String");
        }
        if (newLoc == Loc.PASS && !Prop.passOk(newType)) {
            throw new RuntimeException("Invalid \"pass\" in param type " + newType);
        }
    }

    public Prop(int newType, int newColor, int newIVal) {
        this(newType, newColor, null, null, newIVal, 0.0f);
        if ((typeInfo[newType] & 0x3A9) != 160) {
            throw this.illegalArgs(newType, "int color,int");
        }
    }

    public Prop(int newType, Loc newLoc) {
        this(newType, 0, newLoc, null, 0, 0.0f);
        if ((typeInfo[newType] & 0x3A9) != 8) {
            throw this.illegalArgs(newType, "Loc");
        }
        if (newLoc == Loc.PASS && !Prop.passOk(newType)) {
            throw new RuntimeException("Invalid \"pass\" in param type " + newType);
        }
    }

    public Prop(int newType, int newColor, int newIVal, float newFVal) {
        this(newType, newColor, null, null, newIVal, newFVal);
        if ((typeInfo[newType] & 0x3A9) != 416) {
            throw this.illegalArgs(newType, "int,float");
        }
    }

    public Prop(int newType, int colorOrIval, float newFVal) {
        this(newType, Prop.hasColor(newType) ? colorOrIval : 0, null, null, Prop.hasColor(newType) ? 0 : colorOrIval, newFVal);
        int dataFlags = typeInfo[newType] & 0x3A9;
        if (dataFlags != 288 && dataFlags != 384) {
            throw this.illegalArgs(newType, "int,float");
        }
    }

    public Prop(int newType, int colorOrIval) {
        this(newType, Prop.hasColor(newType) ? colorOrIval : 0, null, null, Prop.hasColor(newType) ? 0 : colorOrIval, 0.0f);
        int dataFlags = typeInfo[newType] & 0x3A9;
        if (dataFlags != 32 && dataFlags != 128) {
            throw this.illegalArgs(newType, "int");
        }
    }

    public Prop(int newType, Loc loc1, Loc loc2) {
        this(newType, 0, loc1, loc2, 0, 0.0f);
        if ((typeInfo[newType] & 0x3A9) != 520) {
            throw this.illegalArgs(newType, "loc,loc");
        }
        if (!(loc1 != Loc.PASS && loc2 != Loc.PASS || Prop.passOk(newType))) {
            throw new IllegalArgumentException("Invalid \"pass\" in param type " + newType);
        }
    }

    public Prop(Prop init, String appendedText) {
        this(init.type, init.color, init.loc, init.getText() + appendedText, init.iVal, init.fVal);
    }

    public Prop(int newType, DataInput in) throws IOException {
        try {
            byte y;
            byte x;
            this.type = newType;
            short flags = typeInfo[newType];
            Object tmpAux = dummyAux;
            if ((flags & 1) != 0) {
                tmpAux = in.readUTF();
            }
            if ((flags & 8) != 0) {
                x = in.readByte();
                y = in.readByte();
                if (x == -1) {
                    if ((flags & 0x400) == 0) {
                        throw new IOException("Pass now allowed in type " + newType);
                    }
                    this.loc = Loc.PASS;
                } else {
                    if (x < 0 || x >= 38 || y < 0 || y >= 38) {
                        throw new IOException("Out of bounds loc: x=" + x + ", y=" + y);
                    }
                    this.loc = Loc.get(x, (int)y);
                }
            } else {
                this.loc = Loc.PASS;
            }
            this.color = (flags & 0x20) == 0 ? (byte)0 : in.readByte();
            this.iVal = (flags & 0x80) == 0 ? 0 : in.readInt();
            float f = this.fVal = (flags & 0x100) == 0 ? 0.0f : in.readFloat();
            if ((flags & 0x200) != 0) {
                x = in.readByte();
                y = in.readByte();
                if (x == -1) {
                    if ((flags & 0x400) == 0) {
                        throw new IOException();
                    }
                    tmpAux = Loc.PASS;
                } else {
                    tmpAux = Loc.get(x, (int)y);
                }
            }
            if (newType == 0) {
                tmpAux = new Rules(in);
            }
            this.aux = tmpAux;
            this.validate();
        }
        catch (ArrayIndexOutOfBoundsException | IllegalArgumentException excep) {
            throw new IOException(excep);
        }
    }

    public final boolean hasColor() {
        return (typeInfo[this.type] & 0x20) != 0;
    }

    public static boolean hasColor(int typeIn) {
        return (typeInfo[typeIn] & 0x20) != 0;
    }

    public int getColor() {
        if (!this.hasColor()) {
            throw new RuntimeException();
        }
        return this.color;
    }

    public final boolean hasInt() {
        return (typeInfo[this.type] & 0x80) != 0;
    }

    public static boolean hasInt(int typeIn) {
        return (typeInfo[typeIn] & 0x80) != 0;
    }

    public int getInt() {
        if (!this.hasInt()) {
            throw new RuntimeException();
        }
        return this.iVal;
    }

    public final boolean hasFloat() {
        return (typeInfo[this.type] & 0x100) != 0;
    }

    public static boolean hasFloat(int typeIn) {
        return (typeInfo[typeIn] & 0x100) != 0;
    }

    public float getFloat() {
        if (!this.hasFloat()) {
            throw new RuntimeException();
        }
        return this.fVal;
    }

    public Rules getRules() {
        if (this.type != 0) {
            throw new RuntimeException();
        }
        return (Rules)this.aux;
    }

    public boolean conflictsWith(Prop peer) {
        if (Prop.isMark(this.type) && Prop.isMark(peer.type)) {
            return this.loc.equals(peer.loc);
        }
        if (this.type == 14 && peer.isSetup() || this.isSetup() && peer.type == 14) {
            return true;
        }
        if (this.type != peer.type || this.type == 29) {
            return false;
        }
        if (this.type == 26) {
            return true;
        }
        if (this.type == 22 || this.type == 17 || this.type == 16) {
            return this.loc.equals(peer.loc);
        }
        return this.color == peer.color && this.loc.equals(peer.loc) && (!this.hasLoc2() || this.aux.equals(peer.aux));
    }

    public String toString() {
        StringBuilder sBuf = new StringBuilder();
        sBuf.append("Prop[type=").append(this.type);
        if (this.hasColor()) {
            sBuf.append(",color=").append("BWE".charAt(this.color));
        }
        if (this.hasLoc()) {
            sBuf.append(',').append(this.loc);
        }
        if (this.aux != dummyAux) {
            sBuf.append(',').append(this.aux);
        }
        if (this.hasInt()) {
            sBuf.append(",int=").append(this.iVal);
        }
        if (this.hasFloat()) {
            sBuf.append(",float=").append(this.fVal);
        }
        return sBuf.append(']').toString();
    }

    public final boolean isAnnotation() {
        return (typeInfo[this.type] & 4) != 0;
    }

    public final boolean isSetup() {
        return (typeInfo[this.type] & 0x2000) != 0;
    }

    public final boolean hasLoc() {
        return (typeInfo[this.type] & 8) != 0;
    }

    public static boolean hasLoc(int typeIn) {
        return (typeInfo[typeIn] & 8) != 0;
    }

    public final Loc getLoc() {
        if (this.loc == null) {
            throw new RuntimeException();
        }
        return this.loc;
    }

    public final boolean hasLoc2() {
        return (typeInfo[this.type] & 0x200) != 0;
    }

    public static boolean hasLoc2(int typeIn) {
        return (typeInfo[typeIn] & 0x200) != 0;
    }

    public final Loc getLoc2() {
        if (!this.hasLoc2()) {
            throw new RuntimeException();
        }
        return (Loc)this.aux;
    }

    public final boolean hasText() {
        return (typeInfo[this.type] & 1) != 0;
    }

    public static boolean hasText(int typeIn) {
        return (typeInfo[typeIn] & 1) != 0;
    }

    public final String getText() {
        if (!this.hasText()) {
            throw new RuntimeException();
        }
        return (String)this.aux;
    }

    public final boolean isSynthetic() {
        return (typeInfo[this.type] & 0x800) != 0;
    }

    @Override
    public int compareTo(Prop peer) {
        int val = this.type - peer.type;
        if (val != 0) {
            return val;
        }
        if (this.hasColor() && (val = peer.color - this.color) != 0) {
            return val;
        }
        val = this.loc.compareTo(peer.loc);
        if (val != 0) {
            return val;
        }
        if (this.aux instanceof Loc) {
            val = ((Loc)this.aux).compareTo((Loc)peer.aux);
        } else if (this.aux instanceof String) {
            val = ((String)this.aux).compareTo((String)peer.aux);
        }
        if (val != 0) {
            return val;
        }
        val = this.iVal - peer.iVal;
        if (val != 0) {
            return val;
        }
        if (this.fVal != peer.fVal) {
            return this.fVal < peer.fVal ? -1 : 1;
        }
        return 0;
    }

    public int hashCode() {
        int hash = this.aux.hashCode();
        hash = hash * -1640524983 + this.type;
        hash = hash * -1640524983 + this.iVal;
        hash = hash * -1640524983 + this.color;
        hash = hash * -1640524983 + Float.floatToIntBits(this.fVal);
        hash = hash * -1640524983 + this.loc.hashCode();
        return hash;
    }

    public boolean equals(Object obj) {
        if (obj == null || !obj.getClass().equals(this.getClass())) {
            return false;
        }
        Prop peer = (Prop)obj;
        return this.type == peer.type && this.color == peer.color && this.loc == peer.loc && this.iVal == peer.iVal && this.aux.equals(peer.aux) && this.fVal == peer.fVal;
    }

    public static void installProp(int paramId, int flags) {
        if (typeInfo.length < paramId + 1) {
            short[] newTypeInfo = new short[paramId + 10];
            System.arraycopy(typeInfo, 0, newTypeInfo, 0, typeInfo.length);
            typeInfo = newTypeInfo;
        }
        if (typeInfo[paramId] != 0) {
            throw new RuntimeException();
        }
        Prop.typeInfo[paramId] = (short)(flags | Short.MIN_VALUE);
    }

    private IllegalArgumentException illegalArgs(int propType, String args) {
        return new IllegalArgumentException("Illegal SGF paramater arguments (type=" + propType + "," + args + ").");
    }

    public void writeTo(DataOutput out) throws IOException {
        out.write(this.type);
        short flags = typeInfo[this.type];
        if ((flags & 1) != 0) {
            out.writeUTF((String)this.aux);
        }
        if ((flags & 8) != 0) {
            out.write(this.loc.x);
            out.write(this.loc.y);
        }
        if ((flags & 0x20) != 0) {
            out.write(this.color);
        }
        if ((flags & 0x80) != 0) {
            out.writeInt(this.iVal);
        }
        if ((flags & 0x100) != 0) {
            out.writeFloat(this.fVal);
        }
        if ((flags & 0x200) != 0) {
            Loc loc2 = (Loc)this.aux;
            out.write(loc2.x);
            out.write(loc2.y);
        }
        if (this.type == 0) {
            ((Rules)this.aux).writeTo(out);
        }
    }

    static {
        int paramId = 0;
        StringTokenizer paramAttributes = new StringTokenizer("4166 3 35 162 3 3 3 3 3 3 35 3 3 3 1128 28 2120 8300 480 29 28 28 60 12 5 386 8292 192 5 1 28 540 540");
        while (paramAttributes.hasMoreElements()) {
            Prop.installProp(paramId++, Integer.parseInt(paramAttributes.nextToken()));
        }
        dummyAux = new Object();
    }
}
