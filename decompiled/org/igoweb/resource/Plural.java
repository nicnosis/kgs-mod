/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.resource;

import java.util.Locale;

public class Plural {
    public static final int RUSSIAN_1 = 0;
    public static final int RUSSIAN_234 = 1;
    public static final int RUSSIAN_OTHER = 2;
    public static final int POLISH_1 = 1;
    public static final int POLISH_234 = 2;
    public static final int POLISH_OTHER = 5;
    public static final int CZECH_1 = 1;
    public static final int CZECH_234 = 2;
    public static final int CZECH_5 = 5;
    public static final int CZECH_FRACTION = 6;
    public final double val;

    public Plural(double val) {
        this.val = val;
    }

    public int getCategory(Locale locale) {
        return Plural.getCategory(this.val, locale);
    }

    public static int getCategory(int value) {
        return Plural.getCategory(value, Locale.getDefault());
    }

    public static int getCategory(int value, Locale locale) {
        String lang = locale.getLanguage();
        if (lang.equals("ru")) {
            if (value < 0) {
                value = -value;
            }
            int ones = value % 10;
            int tens = value / 10 % 10;
            if (tens == 1) {
                return 2;
            }
            if (ones == 1) {
                return 0;
            }
            if (ones >= 2 && ones <= 4) {
                return 1;
            }
            return 2;
        }
        if (lang.equals("pl")) {
            if (value < 0) {
                value = -value;
            }
            if (value == 1) {
                return 1;
            }
            return (value %= 10) >= 2 && value <= 4 ? 2 : 5;
        }
        if (lang.equals("cs") || lang.equals("sl")) {
            if (value < 0) {
                value = -value;
            }
            switch (value) {
                case 1: {
                    return 1;
                }
                case 2: 
                case 3: 
                case 4: {
                    return 2;
                }
            }
            return 5;
        }
        return value == 1 ? 1 : 2;
    }

    public static int getCategory(double value) {
        return Plural.getCategory(value, Locale.getDefault());
    }

    public static int getCategory(double value, Locale locale) {
        String lang = locale.getLanguage();
        if (lang.equals("ru")) {
            if (value < 0.0) {
                value = -value;
            }
            return value > 0.0 && value < 1.0 ? 1 : Plural.getCategory((int)Math.floor(value), locale);
        }
        if (lang.equals("pl")) {
            return Math.floor(value) == value ? Plural.getCategory((int)value, locale) : 2;
        }
        if (lang.equals("cs")) {
            double floor = Math.floor(value);
            return value == floor ? Plural.getCategory((int)floor, locale) : 6;
        }
        return value == 1.0 ? 1 : 2;
    }
}
