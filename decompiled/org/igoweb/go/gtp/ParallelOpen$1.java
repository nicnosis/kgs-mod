/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.gtp;

class ParallelOpen.1
implements Runnable {
    final /* synthetic */ String val$inFile;

    ParallelOpen.1(String string) {
        this.val$inFile = string;
    }

    @Override
    public void run() {
        ParallelOpen.this.openInput(this.val$inFile);
    }
}
