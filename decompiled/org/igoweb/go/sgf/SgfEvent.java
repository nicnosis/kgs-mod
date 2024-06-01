/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.sgf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.igoweb.go.sgf.Prop;
import org.igoweb.util.Event;

public class SgfEvent
extends Event {
    public static final int PROP_ADDED = 0;
    public static final int PROP_REMOVED = 1;
    public static final int PROP_CHANGED = 2;
    public static final int APPEARANCE_CHANGED = 3;
    public static final int CHILDREN_REORDERED = 4;
    public static final int CHILD_ADDED = 5;
    public static final int DELETED = 6;
    public static final int ACTIVATED = 7;
    public static final int PROP_GROUP_ADDED = 8;
    public static final int PROP_GROUP_REMOVED = 9;
    public static final String[] TYPE_NAMES = new String[]{"PROP_ADDED", "PROP_REMOVED", "PROP_CHANGED", "APPEARANCE_CHANGED", "CHILDREN_REORDERED", "CHILD_ADDED", "DELETED", "ACTIVATED", "PROP_GROUP_ADDED", "PROP_GROUP_REMOVED"};
    public static final int EVENT_LIMIT = 10;
    public final int srcId;

    public SgfEvent(int newSrcId, int newType) {
        this(null, newSrcId, newType, null);
    }

    public SgfEvent(int newSrcId, int newType, Object newArg) {
        this(null, newSrcId, newType, newArg);
    }

    public SgfEvent(int newSrcId, int newType, Collection<Prop> newArg) {
        this(null, newSrcId, newType, newArg);
    }

    public SgfEvent(Object src, int newSrcId, int newType, Object newArg) {
        super(src, newType, newArg);
        this.srcId = newSrcId;
    }

    public static SgfEvent readFrom(DataInput in, int newType, int activeNodeId) throws IOException {
        int srcId = in.readInt();
        Object arg = null;
        switch (newType) {
            case 0: 
            case 1: 
            case 2: {
                arg = new Prop((int)in.readByte(), in);
                break;
            }
            case 4: {
                int[] ids = new int[in.readShort()];
                arg = ids;
                for (int i = 0; i < ids.length; ++i) {
                    ids[i] = in.readInt();
                }
                break;
            }
            case 5: {
                int[] args = new int[2];
                arg = args;
                args[0] = in.readInt();
                args[1] = in.readShort();
                break;
            }
            case 6: {
                break;
            }
            case 7: {
                arg = activeNodeId;
                break;
            }
            case 8: 
            case 9: {
                byte propType;
                ArrayList<Prop> props = new ArrayList<Prop>();
                arg = props;
                while ((propType = in.readByte()) != -1) {
                    props.add(new Prop((int)propType, in));
                }
                break;
            }
            default: {
                throw new IOException("Invalid event code " + newType);
            }
        }
        return new SgfEvent(null, srcId, newType, arg);
    }

    public boolean isStructural() {
        return this.type < 10 && this.type != 3;
    }

    public void writeTo(DataOutput out) throws IOException {
        out.write(this.type);
        out.writeInt(this.srcId);
        switch (this.type) {
            case 0: 
            case 1: 
            case 2: {
                ((Prop)this.arg).writeTo(out);
                break;
            }
            case 4: {
                int[] childrenIds = (int[])this.arg;
                out.writeShort(childrenIds.length);
                for (int childrenId : childrenIds) {
                    out.writeInt(childrenId);
                }
                break;
            }
            case 5: {
                int[] args = (int[])this.arg;
                out.writeInt(args[0]);
                out.writeShort(args[1]);
                break;
            }
            case 6: 
            case 7: {
                break;
            }
            case 8: 
            case 9: {
                for (Object o : (Collection)this.arg) {
                    ((Prop)o).writeTo(out);
                }
                out.write(-1);
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "SgfEvent[" + this.type + ", node=" + this.srcId + ", " + this.arg + "]";
    }
}
