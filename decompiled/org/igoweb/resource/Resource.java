/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.resource;

import java.util.Locale;
import org.igoweb.resource.ResEntry;

public abstract class Resource {
    public Resource[] getChildren() {
        return new Resource[0];
    }

    public abstract String propFilePath();

    public String propFilePath(Locale locale) {
        return this.propFilePath(locale.toString());
    }

    public String propFilePath(String localeName) {
        return this.propFilePath(localeName, "properties");
    }

    public String propFilePath(String localeName, String extension) {
        String primaryPath = this.propFilePath();
        if (primaryPath == null) {
            return null;
        }
        if (localeName.length() == 0) {
            return primaryPath + '.' + extension;
        }
        return primaryPath + '_' + localeName + '.' + extension;
    }

    public abstract ResEntry[] getContents();

    public String getKeyPrefix() {
        String className = this.getClass().getName();
        return className.substring(0, className.lastIndexOf(46));
    }

    public String getAuxEntries() {
        return null;
    }
}
