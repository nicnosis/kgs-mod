/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.resource;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.igoweb.resource.AndroidRes;
import org.igoweb.resource.Plural;

public class ResEntry {
    public final String key;
    public final int index;
    public final String original;
    public final String comment;
    public final AndroidRes android;
    private final Object[][] sampleArgs;

    public ResEntry(String key, int index, String original) {
        this(key, index, original, "", (Object[][])null, AndroidRes.NO);
    }

    public ResEntry(String key, int index, String original, String comment) {
        this(key, index, original, comment, (Object[][])null, AndroidRes.NO);
    }

    public ResEntry(String key, int index, String original, String comment, AndroidRes android) {
        this(key, index, original, comment, (Object[][])null, android);
    }

    public ResEntry(String key, int index, String original, String comment, Object[] sample) {
        this(key, index, original, comment, ResEntry.makeSampleArgs(sample), AndroidRes.NO);
    }

    public ResEntry(String key, int index, String original, String comment, Object[] sample, AndroidRes android) {
        this(key, index, original, comment, ResEntry.makeSampleArgs(sample), android);
    }

    public ResEntry(String key, int index, String original, String comment, Object[][] sampleArgs) {
        this(key, index, original, comment, sampleArgs, AndroidRes.NO);
    }

    public ResEntry(String key, int index, String original, String comment, Object[][] sampleArgs, AndroidRes android) {
        this.key = key;
        this.index = index;
        this.original = original;
        this.comment = comment;
        this.sampleArgs = sampleArgs;
        this.android = android;
        if (android == null) {
            throw new IllegalArgumentException("AndroidRes must be set.");
        }
    }

    public ResEntry repackage(String pkgName) {
        return new ResEntry(pkgName + ':' + this.key, this.index, this.original, this.comment, this.sampleArgs);
    }

    public int getNumSamples() {
        return this.sampleArgs == null ? 0 : this.sampleArgs.length;
    }

    public String getFormatted(int sampleNum, Locale locale) {
        return this.getFormatted(this.original, sampleNum, locale);
    }

    public String getFormatted(String text, int sampleNum, Locale locale) {
        Object[] tempArgs = this.sampleArgs[sampleNum];
        Object[] args = new Object[tempArgs.length];
        for (int i = 0; i < args.length; ++i) {
            args[i] = tempArgs[i] instanceof Plural ? new Integer(((Plural)tempArgs[i]).getCategory(locale)) : tempArgs[i];
        }
        if (args.length == 1 && args[0] instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat(text, locale);
            return sdf.format((Date)args[0]);
        }
        MessageFormat format = new MessageFormat(text);
        format.setLocale(locale);
        return format.format(args);
    }

    public String isValid(String translation, Locale locale) {
        if (this.sampleArgs == null) {
            return null;
        }
        try {
            for (int i = 0; i < this.sampleArgs.length; ++i) {
                this.getFormatted(translation, i, locale);
            }
        }
        catch (Exception excep) {
            return "The substitutions generated an error";
        }
        if (this.sampleArgs[0].length == 1 && this.sampleArgs[0][0] instanceof Date) {
            return null;
        }
        int braceIndex = -1;
        while ((braceIndex = this.original.indexOf(123, braceIndex + 1)) != -1) {
            int argNum = this.original.charAt(braceIndex + 1) - 48;
            if (translation.indexOf("{" + argNum) != -1) continue;
            return "The argument \"{" + argNum + "}\" is not used in the translation";
        }
        return this.testQuotes(translation);
    }

    private String testQuotes(String text) {
        boolean quoted = false;
        boolean prevWasQuote = false;
        StringBuilder substring = null;
        int braceDepth = 0;
        block5: for (int i = 0; i < text.length(); ++i) {
            switch (text.charAt(i)) {
                case '{': {
                    prevWasQuote = false;
                    if (substring == null) {
                        if (quoted) {
                            return "You have an argument that won't expand because of the ' characters";
                        }
                        substring = new StringBuilder();
                        ++braceDepth;
                        continue block5;
                    }
                    if (quoted) {
                        substring.append('{');
                        continue block5;
                    }
                    ++braceDepth;
                    continue block5;
                }
                case '\'': {
                    if (!(substring != null || prevWasQuote || text.length() != i + 1 && text.charAt(i + 1) == '\'')) {
                        return "You must double up quotes in replacement strings";
                    }
                    if (prevWasQuote) {
                        prevWasQuote = false;
                        if (substring != null) {
                            substring.append('\'');
                        }
                    } else {
                        prevWasQuote = true;
                    }
                    quoted = !quoted;
                    continue block5;
                }
                case '}': {
                    prevWasQuote = false;
                    if (substring == null) {
                        if (!quoted) continue block5;
                        return "You have quotes around a } character";
                    }
                    if (quoted) {
                        substring.append('}');
                        continue block5;
                    }
                    if (--braceDepth == 0) {
                        String err = this.testQuotes(substring.toString());
                        if (err != null) {
                            return err;
                        }
                        substring = null;
                        continue block5;
                    }
                    if (braceDepth >= 0) continue block5;
                    return "Too many \"}\" characters";
                }
                default: {
                    prevWasQuote = false;
                    if (substring == null) continue block5;
                    substring.append(text.charAt(i));
                }
            }
        }
        if (braceDepth != 0) {
            return "Missing \"}\"";
        }
        if (quoted) {
            return "Missing closing quote";
        }
        return null;
    }

    public String toString() {
        String className = this.getClass().getName();
        return this.toString(className.substring(className.lastIndexOf(46) + 1) + "[", "]");
    }

    protected String toString(String head, String tail) {
        return head + "key=" + this.key + ", args=" + this.sampleArgs + tail;
    }

    private static Object[][] makeSampleArgs(Object[] samples) {
        if (!(samples[0] instanceof Date)) {
            return new Object[][]{samples};
        }
        Object[][] sampleArgs = new Object[samples.length][];
        for (int i = 0; i < samples.length; ++i) {
            sampleArgs[i] = new Object[1];
            sampleArgs[i][0] = samples[i];
        }
        return sampleArgs;
    }

    protected Object[][] getSampleArgs() {
        return this.sampleArgs;
    }
}
