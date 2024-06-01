/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import org.igoweb.util.IntHashMap;

public class Bundle
extends IntHashMap<String> {
    public final Locale locale;

    public Bundle(String resLoc, Locale newLocale, boolean intern) {
        this.locale = newLocale;
        String[] variants = new String[]{"_" + newLocale.getLanguage() + '_' + newLocale.getCountry(), "_" + newLocale.getLanguage(), ""};
        try {
            resLoc = resLoc.replace('.', '/');
            if (resLoc.charAt(0) != '/') {
                resLoc = "/" + resLoc;
            }
            for (int variant = 0; variant < 3; ++variant) {
                InputStream rawIn = this.getClass().getResourceAsStream(resLoc + variants[variant] + ".stringTable");
                if (rawIn == null) continue;
                DataInputStream in = new DataInputStream(new BufferedInputStream(rawIn));
                while (true) {
                    int val = in.readInt();
                    String str = in.readUTF();
                    if (intern) {
                        str = str.intern();
                    }
                    super.put(val, str);
                }
            }
            throw new RuntimeException("Resource bundle " + resLoc + ", locale " + newLocale + " not found");
        }
        catch (EOFException variant) {
        }
        catch (IOException excep) {
            throw new RuntimeException("Error during resource I/O: " + excep);
        }
    }

    public String str(int id) {
        return (String)this.get(id);
    }

    public final String str(int id, int arg) {
        return this.str(id, new Object[]{arg});
    }

    public final String str(int id, float arg) {
        return this.str(id, new Object[]{Float.valueOf(arg)});
    }

    public String str(int id, Object[] args) {
        MessageFormat mf = new MessageFormat((String)this.get(id));
        mf.setLocale(this.locale);
        return mf.format(args);
    }

    public String str(int id, Object arg) {
        return this.str(id, new Object[]{arg});
    }

    @Override
    public String put(int key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String remove(int key) {
        throw new UnsupportedOperationException();
    }

    public Locale getLocale() {
        return this.locale;
    }

    public static class StdMulti
    extends Multi<Bundle> {
        public StdMulti(String resName, String localeList) {
            super(resName, localeList);
        }

        @Override
        public Bundle buildBundle(String resName, Locale bundleLocale) {
            return new Bundle(resName, bundleLocale, true);
        }
    }

    public static abstract class Multi<BundleT extends Bundle>
    extends HashMap<String, BundleT> {
        public Multi(String resName, String localeList) {
            for (String group : localeList.split(" ")) {
                if (group.isEmpty() || group.charAt(0) == '!') continue;
                String lName = group.split("/")[0];
                String[] localeParts = lName.split("_");
                Locale multiLocale = null;
                if (localeParts.length >= 2) {
                    multiLocale = new Locale(localeParts[0], localeParts[1]);
                } else if (localeParts.length >= 1) {
                    multiLocale = new Locale(localeParts[0]);
                }
                if (multiLocale == null) continue;
                super.put(lName, this.buildBundle(resName, multiLocale));
            }
        }

        protected abstract BundleT buildBundle(String var1, Locale var2);
    }
}
