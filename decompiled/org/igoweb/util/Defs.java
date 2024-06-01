/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Locale;
import org.igoweb.util.Bundle;

public abstract class Defs {
    public static final int HASH_MULTIPLIER = -1640524983;
    private static Bundle bundle;

    public static Bundle getBundle() {
        return bundle;
    }

    public static String getString(int id) {
        return bundle.str(id);
    }

    public static String getString(int id, Object[] args) {
        return bundle.str(id, args);
    }

    public static String getString(int id, double arg) {
        return bundle.str(id, arg);
    }

    public static String getString(int id, Object arg) {
        return bundle.str(id, arg);
    }

    public static void setBundle(Bundle res) {
        bundle = res;
    }

    public static void loadBundle(String resLoc) {
        Defs.setBundle(new Bundle(resLoc, Locale.getDefault(), false));
    }

    public static String formatTime(int numSecs) {
        return bundle.str(-24406068, new Object[]{numSecs / 3600, numSecs / 60 % 60, numSecs % 60});
    }

    public static int parseTime(String time) throws ParseException {
        try {
            Object[] times = new MessageFormat(bundle.str(-24406067)).parse(time);
            return ((Number)times[0]).intValue() * 3600 + ((Number)times[1]).intValue() * 60 + ((Number)times[2]).intValue();
        }
        catch (ParseException exception) {
            Object[] times = new MessageFormat(bundle.str(-24406066)).parse(time);
            return ((Number)times[0]).intValue() * 60 + ((Number)times[1]).intValue();
        }
    }

    public static String getPkgName(Class<?> klass) {
        String pkgName = klass.getName();
        return pkgName.substring(0, pkgName.lastIndexOf(46));
    }
}
