/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.shared;

import org.igoweb.igoweb.Config;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.util.Bundle;

public static abstract class IBundle.IMulti
extends Bundle.Multi<IBundle> {
    public IBundle.IMulti(String resName) {
        super(resName, Config.get("localeList"));
    }
}
