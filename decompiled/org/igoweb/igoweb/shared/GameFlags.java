/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

public class GameFlags {
    public static final int OVER_ID = 0;
    public static final short OVER_BIT = 1;
    public static final String OVER_NAME = "over";
    public static final int ADJOURNED_ID = 1;
    public static final short ADJOURNED_BIT = 2;
    public static final String ADJOURNED_NAME = "adjourned";
    public static final int PRIVATE_ID = 2;
    public static final short PRIVATE_BIT = 4;
    public static final String PRIVATE_NAME = "private";
    public static final int SUBSCRIBERS_ONLY_ID = 3;
    public static final short SUBSCRIBERS_ONLY_BIT = 8;
    public static final String SUBSCRIBERS_NAME = "subscribers";
    public static final int EVENT_ID = 4;
    public static final short EVENT_BIT = 16;
    public static final String EVENT_NAME = "event";
    public static final int UPLOADED_ID = 5;
    public static final short UPLOADED_BIT = 32;
    public static final String UPLOADED_NAME = "uploaded";
    public static final int AUDIO_ID = 6;
    public static final short AUDIO_BIT = 64;
    public static final String AUDIO_NAME = "audio";
    public static final int PAUSED_ID = 7;
    public static final short PAUSED_BIT = 128;
    public static final String PAUSED_NAME = "paused";
    public static final int NAMED_ID = 8;
    public static final short NAMED_BIT = 256;
    public static final String NAMED_NAME = "named";
    public static final int SAVED_ID = 9;
    public static final short SAVED_BIT = 512;
    public static final String SAVED_NAME = "saved";
    public static final int GLOBAL_ID = 10;
    public static final short GLOBAL_BIT = 1024;
    public static final String GLOBAL_NAME = "global";
    public static final int RECORDED_ID = 11;
    public static final short RECORDED_BIT = 2048;
    public static final String RECORDED_NAME = "recorded";

    private GameFlags() {
    }

    public static String toString(short flags) {
        StringBuilder out = new StringBuilder("GameFlags[");
        if ((flags & 1) != 0) {
            out.append("OVER,");
        }
        if ((flags & 2) != 0) {
            out.append("ADJOURNED,");
        }
        if ((flags & 4) != 0) {
            out.append("PRIVATE,");
        }
        if ((flags & 8) != 0) {
            out.append("SUBSCRIBERS_ONLY,");
        }
        if ((flags & 0x10) != 0) {
            out.append("EVENT,");
        }
        if ((flags & 0x20) != 0) {
            out.append("UPLOADED,");
        }
        if ((flags & 0x40) != 0) {
            out.append("AUDIO,");
        }
        if ((flags & 0x80) != 0) {
            out.append("PAUSED,");
        }
        if ((flags & 0x100) != 0) {
            out.append("NAMED,");
        }
        if ((flags & 0x200) != 0) {
            out.append("SAVED,");
        }
        if ((flags & 0x400) != 0) {
            out.append("GLOBAL,");
        }
        if ((flags & 0x800) != 0) {
            out.append("RECORDED,");
        }
        if ((flags & 0xFFFFF000) != 0) {
            out.append("unknown(0x").append(Integer.toString(flags & 0xFFFF, 16)).append("),");
        }
        if (flags != 0) {
            out.setLength(out.length() - 1);
        }
        return out.append(']').toString();
    }
}
