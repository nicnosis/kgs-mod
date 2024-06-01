/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.util;

import java.util.Locale;
import org.igoweb.util.Bundle;

public static class Bundle.StdMulti
extends Bundle.Multi<Bundle> {
    public Bundle.StdMulti(String resName, String localeList) {
        super(resName, localeList);
    }

    @Override
    public Bundle buildBundle(String resName, Locale bundleLocale) {
        return new Bundle(resName, bundleLocale, true);
    }
}
