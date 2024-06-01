/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.util.HashMap;
import java.util.Locale;
import org.igoweb.util.Bundle;

public static abstract class Bundle.Multi<BundleT extends Bundle>
extends HashMap<String, BundleT> {
    public Bundle.Multi(String resName, String localeList) {
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
