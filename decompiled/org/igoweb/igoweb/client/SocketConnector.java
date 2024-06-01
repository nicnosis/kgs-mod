/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.igoweb.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.AccessControlException;
import org.igoweb.igoweb.Config;
import org.igoweb.igoweb.client.Connector;
import org.igoweb.util.Defs;

public class SocketConnector
extends Connector {
    private String host;
    private int port;
    private OutputStream out;
    private InputStream in;
    private Socket sock;
    private String hostPort;

    public SocketConnector() {
        this(Config.get("defaultHost"), Config.getInt("defaultPort"));
    }

    public SocketConnector(String newHost, int newPort) {
        this.host = newHost;
        this.port = newPort;
        this.hostPort = this.host + ":" + this.port;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public String connect() {
        SocketConnector socketConnector = this;
        synchronized (socketConnector) {
            try {
                this.sock = new Socket(this.host, this.port);
                this.sock.setKeepAlive(false);
                this.createStreams(this.sock);
                return null;
            }
            catch (UnknownHostException e) {
                return Defs.getString(2031923665, this.hostPort);
            }
            catch (ConnectException e) {
                return Defs.getString(2031923678, this.hostPort);
            }
            catch (NoRouteToHostException e) {
                return Defs.getString(2031923668, this.hostPort);
            }
            catch (AccessControlException excep) {
                return Defs.getString(2031923691);
            }
            catch (IOException e) {
                Object[] args = new Object[]{e.getMessage(), this.hostPort};
                if (args[0] == null) {
                    args[0] = e.toString();
                }
                this.disconnect();
                return Defs.getString(2031923653, args);
            }
        }
    }

    protected void createStreams(Socket newSock) throws IOException {
        this.out = newSock.getOutputStream();
        this.in = newSock.getInputStream();
    }

    @Override
    public void cutoff() {
        try {
            if (this.sock != null) {
                this.sock.shutdownOutput();
            }
        }
        catch (IOException excep) {
            super.cutoff();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void disconnect() {
        SocketConnector socketConnector = this;
        synchronized (socketConnector) {
            if (this.sock != null) {
                try {
                    this.sock.close();
                }
                catch (Exception e) {
                    System.err.println("Exception " + e + " while closing.");
                }
                this.sock = null;
                this.in = null;
                this.out = null;
            }
        }
    }

    @Override
    public InputStream getInputStream() {
        return this.in;
    }

    @Override
    public OutputStream getOutputStream() {
        return this.out;
    }

    @Override
    public Connector cloneParams() {
        SocketConnector copy = (SocketConnector)super.cloneParams();
        copy.host = this.host;
        copy.port = this.port;
        copy.hostPort = this.hostPort;
        return copy;
    }
}
