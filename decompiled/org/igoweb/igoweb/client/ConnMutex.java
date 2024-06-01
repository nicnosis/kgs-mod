/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

public interface ConnMutex {
    public void go();

    public void runSynchronized(Runnable var1);

    public void close();
}
