/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.resource;

public class AndroidRes {
    public static final AndroidRes NO = new AndroidRes("NO");
    public static final AndroidRes YES = new AndroidRes("YES");
    public static final AndroidRes ONLY = new AndroidRes("ONLY");
    public final String name;

    private AndroidRes(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
