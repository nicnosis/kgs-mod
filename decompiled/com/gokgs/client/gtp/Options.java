/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client.gtp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.igoweb.go.Rules;
import org.igoweb.igoweb.Config;
import org.igoweb.igoweb.shared.User;

public class Options {
    public static final String VERBOSE_KEY = "verbose";
    public static final String LOGFILE_KEY = "logFile";
    public static final String IN_FILE_KEY = "gtp.in";
    public static final String OUT_FILE_KEY = "gtp.out";
    public static final String ENGINE_EXEC_KEY = "engine";
    public static final String ENGINE_PORT_KEY = "engine.port";
    public static final String SERVER_HOST_KEY = "server.host";
    public static final String SERVER_PORT_KEY = "server.port";
    public static final String RECONNECT_KEY = "reconnect";
    public static final String USER_NAME_KEY = "name";
    public static final String PASSWORD_KEY = "password";
    public static final String ROOM_WANTED_KEY = "room";
    public static final String OPPONENT_KEY = "opponent";
    public static final String ALLOW_UNDO_KEY = "undo";
    public static final String CONVO_ANSWER_KEY = "talk";
    public static final String GAME_NOTES_KEY = "gameNotes";
    public static final String RULES_KEY = "rules";
    public static final String KOMI_KEY = "komi";
    public static final String BOARD_SIZE_KEY = "rules.boardSize";
    public static final String TIME_SYSTEM_KEY = "rules.time";
    public static final String CLEANUP_HINT_KEY = "hint.cleanup";
    public static final String CANT_DISAGREE_KEY = "hint.noArguing";
    public static final String MODE_KEY = "mode";
    public static final String[] MODES = "auto custom tournament wait both".split(" ");
    public static final int MODE_AUTO = 0;
    public static final int MODE_CUSTOM = 1;
    public static final int MODE_TOURNAMENT = 2;
    public static final int MODE_WAIT = 3;
    public static final int MODE_BOTH = 4;
    public static final String AUTOMATCH_SPEED_KEY = "automatch.speed";
    public static final String AUTOMATCH_RANK_KEY = "automatch.rank";
    public final String serverHost;
    public final short serverPort;
    public final boolean reconnect;
    public final String userName;
    public final String password;
    public final String gameNotes;
    public final String convoAnswer;
    public final String opponent;
    public final boolean allowUndo;
    public final String roomWanted;
    public final int mode;
    public final int automatchSpeed;
    public final int automatchRank;
    public final String cleanupHintMessage;
    public final String cantDisagreeMessage;
    public final Rules rules = new Rules();
    public final boolean forceKomi;
    public final String engineCommand;
    public final String inFile;
    public final String outFile;
    public final int enginePort;
    public final Logger logger;
    private static final Map<String, Integer> automatchSpeeds = Options.buildAutomatchSpeeds();
    private static final Map<String, Integer> ruleTypes = Options.buildRuleTypes();
    private static final Pattern RANK_PARSER = Pattern.compile("(\\p{Digit}+)(k|d|p)");

    public Options(Properties props, String logName) throws IOException {
        this.logger = Logger.getLogger(logName);
        this.logger.setUseParentHandlers(false);
        String logfile = (String)props.remove(LOGFILE_KEY);
        if (logfile == null) {
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.ALL);
            this.logger.addHandler(consoleHandler);
        } else {
            FileHandler fh = new FileHandler(logfile + "-%g.log");
            fh.setFormatter(new SimpleFormatter());
            this.logger.addHandler(fh);
        }
        this.logger.setLevel(this.isTrue(props.remove(VERBOSE_KEY)) ? Level.ALL : Level.FINE);
        String tmp = (String)props.remove(SERVER_HOST_KEY);
        this.serverHost = tmp == null ? Config.get("defaultHost") : tmp;
        tmp = (String)props.remove(SERVER_PORT_KEY);
        this.serverPort = Short.parseShort(tmp == null ? Config.get("defaultPort") : tmp);
        this.reconnect = this.isTrue(props.remove(RECONNECT_KEY));
        this.userName = (String)props.remove(USER_NAME_KEY);
        if (this.userName == null) {
            throw new IllegalArgumentException("Required argument \"name\" not provided");
        }
        if (!User.nameValid(this.userName)) {
            throw new IllegalArgumentException("Invalid user name: \"" + this.userName + "\"");
        }
        this.password = (String)props.remove(PASSWORD_KEY);
        if (this.password == null) {
            throw new IllegalArgumentException("Required argument \"password\" not provided");
        }
        tmp = (String)props.remove(MODE_KEY);
        if (tmp == null) {
            this.mode = 0;
        } else {
            int i;
            for (i = 0; i < MODES.length && !tmp.equals(MODES[i]); ++i) {
            }
            if (i >= MODES.length) {
                throw new IllegalArgumentException("Invalid \"mode\" setting: " + tmp);
            }
            this.mode = i;
        }
        tmp = (String)props.remove(AUTOMATCH_SPEED_KEY);
        int tmpAutomatchSpeed = 0;
        for (String speed : (tmp == null ? "blitz,medium,fast" : tmp).split(",")) {
            try {
                tmpAutomatchSpeed |= automatchSpeeds.get(speed.trim()).intValue();
            }
            catch (NullPointerException excep) {
                throw new IllegalArgumentException("Unknown setting for \"automatch.speed\": " + tmp);
            }
        }
        if (tmpAutomatchSpeed == 0) {
            throw new IllegalArgumentException("Must specify a speed when using automatch.speed");
        }
        this.automatchSpeed = tmpAutomatchSpeed;
        tmp = (String)props.remove(AUTOMATCH_RANK_KEY);
        if (tmp == null) {
            this.automatchRank = 0;
        } else {
            this.automatchRank = this.parseRank(tmp);
            if (this.automatchRank > 30) {
                throw new IllegalArgumentException("Rank must be 1k or lower");
            }
        }
        this.roomWanted = (String)props.remove(ROOM_WANTED_KEY);
        this.allowUndo = this.isTrue(props.remove(ALLOW_UNDO_KEY), true);
        this.opponent = (String)props.remove(OPPONENT_KEY);
        if (this.mode == 3 && this.opponent == null) {
            throw new IllegalArgumentException("You must specify \"opponent\" when the mode is \"" + MODES[3] + "\"");
        }
        tmp = (String)props.remove(GAME_NOTES_KEY);
        String string = this.gameNotes = tmp == null ? "Computer player" : tmp;
        if (this.gameNotes.length() > 80) {
            throw new IllegalArgumentException("Game notes must be at most 80 characters");
        }
        tmp = (String)props.remove(CONVO_ANSWER_KEY);
        this.convoAnswer = tmp == null ? "Sorry, I am a computer program. I cannot talk." : tmp;
        tmp = (String)props.remove(RULES_KEY);
        if (tmp != null) {
            try {
                this.rules.setType(ruleTypes.get(tmp.toLowerCase(Locale.US)));
            }
            catch (NullPointerException excep) {
                throw new IllegalArgumentException("Unknown rules type " + tmp);
            }
        }
        if ((tmp = (String)props.remove(BOARD_SIZE_KEY)) != null) {
            this.rules.setSize(Integer.parseInt(tmp));
        }
        if ((tmp = (String)props.remove(TIME_SYSTEM_KEY)) != null) {
            if (tmp.equals("0")) {
                this.rules.setTimeSystem(0);
            } else {
                Matcher m = Pattern.compile("(\\d+):(\\d\\d)").matcher(tmp);
                if (m.matches()) {
                    this.rules.setTimeSystem(1);
                    this.rules.setMainTime(Integer.parseInt(m.group(1)) * 60 + Integer.parseInt(m.group(2)));
                } else {
                    m = Pattern.compile("(\\d+):(\\d\\d)\\+(\\d+)[xX\u00d7](\\d+):(\\d\\d)").matcher(tmp);
                    if (m.matches()) {
                        this.rules.setTimeSystem(2);
                        this.rules.setMainTime(Integer.parseInt(m.group(1)) * 60 + Integer.parseInt(m.group(2)));
                        this.rules.setByoYomiPeriods(Integer.parseInt(m.group(3)));
                        this.rules.setByoYomiTime(Integer.parseInt(m.group(4)) * 60 + Integer.parseInt(m.group(5)));
                    } else {
                        m = Pattern.compile("(\\d+):(\\d\\d)\\+(\\d+)/(\\d+):(\\d\\d)").matcher(tmp);
                        if (m.matches()) {
                            this.rules.setTimeSystem(3);
                            this.rules.setMainTime(Integer.parseInt(m.group(1)) * 60 + Integer.parseInt(m.group(2)));
                            this.rules.setByoYomiStones(Integer.parseInt(m.group(3)));
                            this.rules.setByoYomiTime(Integer.parseInt(m.group(4)) * 60 + Integer.parseInt(m.group(5)));
                        } else {
                            throw new IllegalArgumentException("Cannot parse time setup \"" + tmp + "\"");
                        }
                    }
                }
            }
        }
        if ((tmp = (String)props.remove(KOMI_KEY)) == null) {
            this.forceKomi = false;
        } else {
            this.forceKomi = true;
            try {
                this.rules.setKomi(Float.parseFloat(tmp));
            }
            catch (NumberFormatException excep) {
                throw new IllegalArgumentException("Komi value \"" + tmp + "\" cannot be parsed");
            }
        }
        this.engineCommand = (String)props.remove(ENGINE_EXEC_KEY);
        this.inFile = (String)props.remove(IN_FILE_KEY);
        this.outFile = (String)props.remove(OUT_FILE_KEY);
        tmp = (String)props.remove(ENGINE_PORT_KEY);
        if (tmp != null) {
            int tmpEp;
            try {
                tmpEp = Integer.parseInt(tmp);
            }
            catch (NumberFormatException excep) {
                tmpEp = -1;
            }
            if (tmpEp < 0 || tmpEp > 65535) {
                throw new IllegalArgumentException("Ports must be an integer from 0 through 65535; \"" + tmp + "\" is not valid");
            }
            this.enginePort = tmpEp;
        } else {
            this.enginePort = -1;
        }
        tmp = (String)props.remove(CLEANUP_HINT_KEY);
        this.cleanupHintMessage = tmp == null ? "You and the engine seem to disagree about which stones are dead. To solve this problem, you can press \"undo\", play until all dead stones are removed from the board, then score again." : tmp;
        tmp = (String)props.remove(CANT_DISAGREE_KEY);
        this.cantDisagreeMessage = tmp == null ? "You and the engine seem to disagree about which stones are dead. Sorry, this cannot be solved. You must either accept the engine's choices or adjourn the game. If you think the engine is incorrect, then please contact its author." : tmp;
        this.rules.addListener(event -> {
            throw new RuntimeException("This rules object should not change");
        });
        if (props.remove("tournament") != null) {
            throw new IllegalArgumentException("\"tournament\" is obsolete, please use \"mode=tournament\" instead");
        }
        if (props.remove("open") != null) {
            throw new IllegalArgumentException("\"open\" is obsolete, please use \"mode=auto\" or \"mode=custom\" instead");
        }
        if (!props.isEmpty()) {
            for (Object option : props.keySet()) {
                this.logger.warning("Unknown option \"" + option + "\"");
            }
        }
    }

    private boolean isTrue(Object val) {
        return this.isTrue(val, false);
    }

    private boolean isTrue(Object val, boolean def) {
        if (val == null) {
            return def;
        }
        if (val.equals("t") || val.equals("true")) {
            return true;
        }
        if (val.equals("f") || val.equals("false")) {
            return false;
        }
        throw new IllegalArgumentException("Expected \"t\", \"true\", \"f\", or \"false\"; got \"" + val + "\"");
    }

    public static void usage() {
        System.err.println("Usage: gtpClient <properties file> [prop1=val1 [prop2=val2 ...]]\nProperties:\n  logFile=<file> - Log to file, -nn.log will be appended\n  verbose=t|f - Verbose output\n\n  engine=<command> - Execute the command to start the engine and use engine's stdin/stdout to communicate\n  gtp.in=<filename> - Read named pipe for data from engine\n  gtp.out=<filename> - Write named pipe for data to engine\n  engine.port=<port> - Listen at the specified TCP port for connections from the engine\n\n  server.host=<host> - Set host or IP address of KGS\n  server.port=<port> - Set TCP port of KGS\n  name=<name> - Set user name (required)\n  password=<value> - Set password (required)\n  room=<room name> - Join this room on startup\n" + "  mode=" + MODES[0] + " - Set up automatch game (default)\n" + "    automatch.speed= One or more of blitz,fast,medium - select acceptable speeds for automatch pairing (blitz,fast,medium is default)\n" + "    automatch.rank=[30k..1k] - Select estimated rank for automatch pairing (Required if robot is unranked)\n" + "  mode=" + MODES[1] + " - Set up custom open game\n" + "    rules=(japanese|chinese|aga|newzealand) - Set rule system to use\n" + "    " + BOARD_SIZE_KEY + "=<size> - Set board size\n" + "    rules.time=<0|m:ss|m:ss+nxm:ss|m:ss+n/m:ss> - Set time system\n" + "  mode=" + MODES[2] + " - Play in tournament games only\n" + "  mode=" + MODES[3] + " - Wait for opponent to create custom game\n" + "    " + OPPONENT_KEY + "=<name> - Play against this player only\n" + "  mode=" + MODES[4] + " - Both automatch and custom open games at the same time\n" + "  talk=<text> - Set message we give to anybody who tries to talk to us.\n" + "  " + GAME_NOTES_KEY + "=<text> - Set game notes that appear\n" + "  hint.cleanup=<text> - Set notes to opponent about cleanup mode\n" + "  hint.noArguing=<text> - Set message to opponent when the game can't be scored \n    due to disagreement over dead stones\n" + "\n  -help, -usage - Show this information.");
    }

    public int parseRank(String rankStr) {
        Matcher m = RANK_PARSER.matcher(rankStr.toLowerCase(Locale.US));
        if (!m.matches()) {
            throw new IllegalArgumentException("Cannot decode rank \"" + rankStr + "\"");
        }
        int val = Integer.parseInt(m.group(1));
        if (val < 1) {
            throw new IllegalArgumentException("Cannot decode rank \"" + rankStr + "\"");
        }
        char type = m.group(2).charAt(0);
        switch (type) {
            case 'k': {
                val = 31 - val;
                if (val >= 1) break;
                throw new IllegalArgumentException("Cannot decode rank \"" + rankStr + "\"");
            }
            case 'd': {
                if ((val += 30) <= 39) break;
                throw new IllegalArgumentException("Cannot decode rank \"" + rankStr + "\"");
            }
            case 'p': {
                if ((val += 39) <= 48) break;
                throw new IllegalArgumentException("Cannot decode rank \"" + rankStr + "\"");
            }
        }
        return val;
    }

    private static Map<String, Integer> buildAutomatchSpeeds() {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        result.put("blitz", 512);
        result.put("fast", 4096);
        result.put("medium", 1024);
        return result;
    }

    private static Map<String, Integer> buildRuleTypes() {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        result.put("japanese", 0);
        result.put("chinese", 1);
        result.put("aga", 2);
        result.put("newzealand", 3);
        return result;
    }
}
