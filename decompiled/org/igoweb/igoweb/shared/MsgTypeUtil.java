/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import java.io.IOException;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.igoweb.shared.MsgTypesUp;
import org.igoweb.util.IntHashMap;

public class MsgTypeUtil {
    private static IntHashMap<MsgTypesDown> downMsgTypes = MsgTypeUtil.buildDownMsgTypes();
    private static IntHashMap<MsgTypesUp> upMsgTypes = MsgTypeUtil.buildUpMsgTypes();

    public static MsgTypesDown getDownFromId(short id) throws IOException {
        MsgTypesDown result = downMsgTypes.get(id);
        if (result == null) {
            throw new IOException("Unknown message type ID " + id);
        }
        return result;
    }

    private static IntHashMap<MsgTypesDown> buildDownMsgTypes() {
        IntHashMap<MsgTypesDown> result = new IntHashMap<MsgTypesDown>();
        for (MsgTypesDown type : MsgTypesDown.values()) {
            if (result.containsKey(type.id)) {
                throw new RuntimeException("Two message types on ID " + type.id + ": " + (Object)((Object)result.get(type.id)) + ", " + (Object)((Object)type));
            }
            result.put(type.id, type);
        }
        return result;
    }

    public static MsgTypesUp getUpFromId(short id) throws IOException {
        MsgTypesUp result = upMsgTypes.get(id);
        if (result == null) {
            throw new IOException("Unknown message type ID " + id);
        }
        return result;
    }

    private static IntHashMap<MsgTypesUp> buildUpMsgTypes() {
        IntHashMap<MsgTypesUp> result = new IntHashMap<MsgTypesUp>();
        for (MsgTypesUp type : MsgTypesUp.values()) {
            if (result.containsKey(type.id)) {
                throw new RuntimeException("Two message types on ID " + type.id + ": " + (Object)((Object)result.get(type.id)) + ", " + (Object)((Object)type));
            }
            result.put(type.id, type);
        }
        return result;
    }
}
