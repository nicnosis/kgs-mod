/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go.gtp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.igoweb.go.Loc;
import org.igoweb.go.Rules;
import org.igoweb.go.gtp.Command;
import org.igoweb.go.gtp.GtpException;
import org.igoweb.util.Emitter;
import org.igoweb.util.LockOrder;

public class Protocol
extends Emitter {
    public static final int EVENT_BASE = 0;
    public static final int EOF_EVENT = 0;
    public static final int IDLE_EVENT = 1;
    public static final int FATAL_ERROR_EVENT = 2;
    public static final int COMMAND_SENT_EVENT = 3;
    public static final int QUEUED_COMMAND_SENT_EVENT = 4;
    public static final int COMMAND_QUEUED_EVENT = 5;
    public static final int SUCCESSFUL_RESPONSE_EVENT = 6;
    public static final int ERROR_RESPONSE_EVENT = 7;
    public static final int MALFORMED_RESPONSE_EVENT = 8;
    public static final int UNEXPECTED_RESPONSE_EVENT = 9;
    public static final int EVENT_LIMIT = 10;
    private static final Pattern VERTEX_PATTERN = Pattern.compile("([a-hj-zA-HJ-Z])(\\d\\d?)");
    public static final int MAX_BOARD_SIZE = 25;
    private InputStreamReader in;
    private OutputStreamWriter out;
    private Object lock;
    private HashSet<String> engineCommands;
    private String engineName;
    private String engineVersion;
    private boolean ready;
    private boolean readerDead = false;
    private LinkedList<Command> cmdList = new LinkedList();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Protocol(InputStream in, OutputStream out, final Object lock) {
        try {
            this.in = new InputStreamReader(in, "UTF-8");
            this.out = new OutputStreamWriter(out, "UTF-8");
        }
        catch (UnsupportedEncodingException excep) {
            throw new RuntimeException("Cannot create reader/writer; UTF-8 not known?", excep);
        }
        this.lock = lock;
        new Thread(new Runnable(){

            @Override
            public void run() {
                Protocol.this.workerThread();
            }
        }, "GTP Reader").start();
        assert (LockOrder.testAcquire(lock));
        Object object = lock;
        synchronized (object) {
            this.send(new Command("list_commands"){

                @Override
                public void responseReceived(String resp, boolean success) {
                    Protocol.this.commandListReceived(success ? resp.split("[\n ]") : null);
                }
            });
            this.send(new Command("name"){

                @Override
                public void responseReceived(String resp, boolean success) {
                    if (success) {
                        Protocol.this.engineName = resp;
                    }
                }
            });
            this.send(new Command("version"){

                @Override
                public void responseReceived(String resp, boolean success) {
                    Protocol.this.ready = true;
                    lock.notifyAll();
                    if (success) {
                        Protocol.this.engineVersion = resp;
                    }
                }
            });
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitForReady() {
        assert (LockOrder.testAcquire(this.lock));
        Object object = this.lock;
        synchronized (object) {
            try {
                assert (LockOrder.testWait(this.lock));
                while (!this.ready && !this.readerDead) {
                    this.lock.wait();
                }
            }
            catch (InterruptedException excep) {
                throw new RuntimeException("Interrupted while waiting for engine data", excep);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void send(Command command) {
        assert (LockOrder.testAcquire(this.lock));
        Object object = this.lock;
        synchronized (object) {
            if (this.readerDead) {
                throw new IllegalStateException("Reader thread is dead, command will not complete");
            }
            if (this.cmdList.isEmpty()) {
                try {
                    this.out.write(command.text);
                    this.out.write(10);
                    this.out.flush();
                    this.emit(3, command.text);
                }
                catch (IOException excep) {
                    this.emit(2, excep);
                }
            } else {
                this.emit(5, command.text);
            }
            this.cmdList.add(command);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void workerThread() {
        Object object;
        try {
            while (true) {
                String resp;
                if ((resp = this.getResponse()) == null) {
                    assert (LockOrder.testAcquire(this.lock));
                    object = this.lock;
                    synchronized (object) {
                        this.emit(0);
                        return;
                    }
                }
                this.processResponse(resp);
                continue;
                break;
            }
        }
        catch (Throwable thrown) {
            assert (LockOrder.testAcquire(this.lock));
            Object object2 = this.lock;
            synchronized (object2) {
                this.emit(2, thrown);
                return;
            }
        }
        finally {
            assert (LockOrder.testAcquire(this.lock));
            object = this.lock;
            synchronized (object) {
                this.readerDead = true;
                this.lock.notifyAll();
            }
        }
    }

    private String getResponse() throws IOException {
        boolean comment = false;
        boolean hasWs = true;
        boolean wasCr = false;
        StringBuilder cmdBuf = new StringBuilder();
        block6: while (true) {
            int c = this.in.read();
            switch (c) {
                case -1: {
                    if (cmdBuf.length() == 0) {
                        return null;
                    }
                }
                case 10: {
                    comment = false;
                    if (cmdBuf.length() <= 0) continue block6;
                    hasWs = false;
                    if (wasCr) {
                        if (cmdBuf.length() <= 0) continue block6;
                        return cmdBuf.toString();
                    }
                    wasCr = true;
                    continue block6;
                }
                case 9: 
                case 32: {
                    if (wasCr) {
                        cmdBuf.append('\n');
                        wasCr = false;
                    }
                    if (comment || hasWs) continue block6;
                    hasWs = true;
                    cmdBuf.append(' ');
                    continue block6;
                }
                case 35: {
                    if (wasCr) {
                        cmdBuf.append('\n');
                        wasCr = false;
                    }
                    comment = true;
                    hasWs = false;
                    continue block6;
                }
            }
            if (c < 32) continue;
            if (wasCr) {
                cmdBuf.append('\n');
                wasCr = false;
            }
            hasWs = false;
            if (comment) continue;
            cmdBuf.append((char)c);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void processResponse(String resp) throws IOException, GtpException {
        char start = resp.charAt(0);
        if (start != '=' && start != '?') {
            this.emit(8, resp);
            return;
        }
        assert (LockOrder.testAcquire(this.lock));
        Object object = this.lock;
        synchronized (object) {
            int eventType;
            boolean success;
            if (this.cmdList.isEmpty()) {
                this.emit(9, resp);
                return;
            }
            Command cmd = this.cmdList.removeFirst();
            if (start == '=') {
                success = true;
                eventType = 6;
            } else {
                success = false;
                eventType = 7;
            }
            this.emit(eventType, new CommandResponsePair(cmd.text, resp));
            Command nextCommand = this.cmdList.isEmpty() ? null : this.cmdList.getFirst();
            cmd.responseReceived(resp.substring(1).trim(), success);
            if (nextCommand == null) {
                this.emit(1);
            } else {
                this.out.write(nextCommand.text);
                this.out.write(10);
                this.out.flush();
                this.emit(4, nextCommand.text);
            }
        }
    }

    public boolean isCommandSupported(String command) {
        return this.engineCommands.contains(command);
    }

    public String getEngineName() {
        return this.engineName;
    }

    public String getEngineVersion() {
        return this.engineVersion;
    }

    public static Loc parseGtpVertex(String gtpLoc, int boardSize) throws GtpException {
        if (gtpLoc.equalsIgnoreCase("pass")) {
            return Loc.PASS;
        }
        Matcher matcher = VERTEX_PATTERN.matcher(gtpLoc);
        if (!matcher.matches()) {
            throw new GtpException("Cannot recognize vertex \"" + gtpLoc + "\" that came from engine.");
        }
        int x = matcher.group(1).charAt(0);
        if (x >= 65 && x <= 90) {
            x += 32;
        }
        if (x > 105) {
            --x;
        }
        int y = boardSize - Integer.parseInt(matcher.group(2));
        if ((x -= 97) < 0 || x >= boardSize || y < 0 || y >= boardSize) {
            throw new GtpException("Move \"" + gtpLoc + "\" from engine is not possible on a " + boardSize + "x" + boardSize + " board.");
        }
        return Loc.get(x, y);
    }

    public static String toGtpVertex(Loc loc, int boardSize) {
        if (boardSize > 25) {
            throw new IllegalArgumentException("Board size of " + boardSize + " is bigger than GTP limit of " + 25);
        }
        if (loc == Loc.PASS) {
            return "pass";
        }
        if (loc.x >= boardSize || loc.y >= boardSize) {
            throw new IllegalArgumentException("Location " + loc.x + "," + loc.y + "is bigger than board size of " + boardSize);
        }
        StringBuilder sbuf = new StringBuilder();
        if (loc.x < 8) {
            sbuf.append((char)(loc.x + 97));
        } else {
            sbuf.append((char)(loc.x + 98));
        }
        sbuf.append(boardSize - loc.y);
        return sbuf.toString();
    }

    public static char toGtpColor(int color) {
        if (color == 0) {
            return 'b';
        }
        if (color == 1) {
            return 'w';
        }
        throw new IllegalArgumentException("Color " + color + " cannot be put into GTP notation");
    }

    public String buildTimeSettings(Rules rules) {
        String kgsTime;
        String gtpTime;
        switch (rules.getTimeSystem()) {
            case 0: {
                gtpTime = "0 1 0";
                kgsTime = "none";
                break;
            }
            case 1: {
                gtpTime = rules.getMainTime() + " 0 0";
                kgsTime = "absolute " + Integer.toString(rules.getMainTime());
                break;
            }
            case 2: {
                gtpTime = rules.getMainTime() + rules.getByoYomiTime() * rules.getByoYomiPeriods() + " 0 0";
                kgsTime = "byoyomi " + rules.getMainTime() + ' ' + rules.getByoYomiTime() + ' ' + rules.getByoYomiPeriods();
                break;
            }
            case 3: {
                gtpTime = rules.getMainTime() + " " + rules.getByoYomiTime() + " " + rules.getByoYomiStones();
                kgsTime = "canadian " + gtpTime;
                break;
            }
            default: {
                throw new RuntimeException("Unknown time system " + rules.getTimeSystem());
            }
        }
        if (this.isCommandSupported("kgs-time_settings")) {
            return "kgs-time_settings " + kgsTime;
        }
        if (this.isCommandSupported("time_settings")) {
            return "time_settings " + gtpTime;
        }
        return null;
    }

    public boolean isIdle() {
        return this.cmdList.isEmpty();
    }

    private void commandListReceived(String[] commands) {
        HashSet<String> tempEngineCommands = new HashSet<String>();
        if (commands != null) {
            for (int i = 0; i < commands.length; ++i) {
                if (commands[i] == null || commands[i].length() <= 0) continue;
                tempEngineCommands.add(commands[i]);
            }
        }
        this.engineCommands = tempEngineCommands;
    }

    public Object getLock() {
        return this.lock;
    }

    public class CommandResponsePair {
        public final String command;
        public final String response;

        public CommandResponsePair(String command, String response) {
            this.command = command;
            this.response = response;
        }
    }
}
