/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.gtp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.igoweb.util.LockOrder;

public class ParallelOpen {
    private InputStream in = System.in;
    private OutputStream out = System.out;
    private boolean inDone = false;
    private boolean outDone = false;
    private Throwable error = null;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ParallelOpen(final String inFile, final String outFile) {
        assert (LockOrder.testAcquire(this));
        ParallelOpen parallelOpen = this;
        synchronized (parallelOpen) {
            if (inFile == null) {
                this.inDone = true;
            } else {
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        ParallelOpen.this.openInput(inFile);
                    }
                }, "infile opener").start();
            }
            if (outFile == null) {
                this.outDone = true;
            } else {
                new Thread(new Runnable(){

                    @Override
                    public void run() {
                        ParallelOpen.this.openOutput(outFile);
                    }
                }, "outfile opener").start();
            }
            assert (LockOrder.testWait(this));
            try {
                while (!this.inDone || !this.outDone) {
                    this.wait();
                }
            }
            catch (InterruptedException excep) {
                this.error = excep;
            }
            if (this.error != null) {
                throw new RuntimeException("Error creating pipes", this.error);
            }
        }
    }

    public InputStream getIn() {
        return this.in;
    }

    public OutputStream getOut() {
        return this.out;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void openInput(String inFile) {
        try {
            this.in = new BufferedInputStream(new FileInputStream(inFile));
            assert (LockOrder.testAcquire(this));
            ParallelOpen parallelOpen = this;
            synchronized (parallelOpen) {
                this.inDone = true;
                this.notify();
            }
        }
        catch (IOException excep) {
            assert (LockOrder.testAcquire(this));
            ParallelOpen parallelOpen = this;
            synchronized (parallelOpen) {
                this.error = excep;
                this.inDone = true;
                this.notify();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void openOutput(String outFile) {
        try {
            this.out = new BufferedOutputStream(new FileOutputStream(outFile));
            assert (LockOrder.testAcquire(this));
            ParallelOpen parallelOpen = this;
            synchronized (parallelOpen) {
                this.outDone = true;
                this.notify();
            }
        }
        catch (IOException excep) {
            assert (LockOrder.testAcquire(this));
            ParallelOpen parallelOpen = this;
            synchronized (parallelOpen) {
                this.error = excep;
                this.outDone = true;
                this.notify();
            }
        }
    }
}
