/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class Connector {
    public abstract String connect();

    public abstract void disconnect();

    public abstract InputStream getInputStream();

    public abstract OutputStream getOutputStream();

    public Connector cloneParams() {
        try {
            return (Connector)this.getClass().newInstance();
        }
        catch (Exception excep) {
            throw new RuntimeException("Error " + excep);
        }
    }

    public void cutoff() {
    }
}
