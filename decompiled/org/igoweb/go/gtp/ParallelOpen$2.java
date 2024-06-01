/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.gtp;

class ParallelOpen.2
implements Runnable {
    final /* synthetic */ String val$outFile;

    ParallelOpen.2(String string) {
        this.val$outFile = string;
    }

    @Override
    public void run() {
        ParallelOpen.this.openOutput(this.val$outFile);
    }
}
