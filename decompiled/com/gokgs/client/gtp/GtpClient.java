/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client.gtp;

import com.gokgs.client.KCChallenge;
import com.gokgs.client.KCGame;
import com.gokgs.client.KCProposal;
import com.gokgs.client.KClient;
import com.gokgs.client.gtp.ClientGtpGame;
import com.gokgs.client.gtp.GtpChal;
import com.gokgs.client.gtp.GtpConvo;
import com.gokgs.client.gtp.Options;
import com.gokgs.client.gtp.State;
import com.gokgs.shared.KBundle;
import com.gokgs.shared.KGameType;
import com.gokgs.shared.KProposal;
import com.gokgs.shared.KRole;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.igoweb.go.Chain;
import org.igoweb.go.Go;
import org.igoweb.go.Rules;
import org.igoweb.go.gtp.GtpGame;
import org.igoweb.go.gtp.ParallelOpen;
import org.igoweb.go.gtp.Protocol;
import org.igoweb.igoweb.Config;
import org.igoweb.igoweb.client.CArchive;
import org.igoweb.igoweb.client.CConvo;
import org.igoweb.igoweb.client.CGameListEntry;
import org.igoweb.igoweb.client.CRoom;
import org.igoweb.igoweb.client.Client;
import org.igoweb.igoweb.client.LockConnMutex;
import org.igoweb.igoweb.client.SocketConnector;
import org.igoweb.igoweb.shared.ClientType;
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.RoomCategories;
import org.igoweb.igoweb.shared.User;
import org.igoweb.util.Defs;
import org.igoweb.util.Event;
import org.igoweb.util.EventListener;
import org.igoweb.util.LockOrder;

public class GtpClient {
    private static final String CLIENT_ID_PREF = "GnT(2aYR";
    private KClient client;
    private Logger logger;
    private Protocol protocol;
    private boolean success = true;
    private boolean disconnected = false;
    private CRoom subscribedRoom;
    private ArrayList<CRoom> allRooms;
    private boolean waitingToStartGame;
    private boolean engineError = false;
    private boolean createGameSent = false;
    private KCGame gameInPlay = null;
    private Options options;
    private HashSet<Integer> joinedGameIds = new HashSet();
    private static boolean defsLoaded;
    private HashSet<Long> myTournamentGames = new HashSet();
    private RoomWatch roomWatch;
    private final EventListener roomListener = this::roomEvent;
    private final EventListener roomCatListener = this::roomCatEvent;
    private final EventListener gameListener = this::gameEvent;
    private final EventListener protocolListener = this::protocolEvent;
    private final EventListener clientListener = this::clientEvent;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public GtpClient(InputStream in, OutputStream out, Options newOptions) throws IOException {
        this.options = newOptions;
        this.logger = newOptions.logger;
        assert (LockOrder.testAcquire(this));
        GtpClient gtpClient = this;
        synchronized (gtpClient) {
            this.protocol = new Protocol(in, out, this);
            this.protocol.addListener(this.protocolListener);
        }
        this.protocol.waitForReady();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean go() {
        if (!defsLoaded) {
            Defs.setBundle(new KBundle("com.gokgs.client.res.Res", Locale.getDefault(), false));
            defsLoaded = true;
        }
        try {
            assert (LockOrder.testAcquire(this));
            GtpClient gtpClient = this;
            synchronized (gtpClient) {
                boolean firstTime = true;
                do {
                    if (!firstTime) {
                        long now;
                        if (!this.protocol.isCommandSupported("clear_board")) {
                            this.logger.warning("Reconnect is set, but the engine does not support \"clear_board\"; will not reconnect");
                            break;
                        }
                        long waitUntil = System.currentTimeMillis() + 300000L;
                        this.logger.fine("Will wait 5 minutes, then try to connect again.");
                        while ((now = System.currentTimeMillis()) < waitUntil) {
                            assert (LockOrder.testWait(this));
                            this.wait(waitUntil - now);
                            if (!this.engineError) continue;
                        }
                    }
                    firstTime = false;
                    this.connect();
                } while (this.options.reconnect && !this.engineError);
            }
        }
        catch (InterruptedException excep) {
            throw new RuntimeException(excep);
        }
        return this.success;
    }

    private void connect() throws InterruptedException {
        this.client = new KClient(this.options.userName, this.options.password, ClientType.GTP, Preferences.userNodeForPackage(this.getClass()).getLong(CLIENT_ID_PREF, 0L), new LockConnMutex(this), new SocketConnector(this.options.serverHost, this.options.serverPort));
        this.disconnected = false;
        State.create(this.client, this.protocol, this.options);
        this.client.addListener(this.clientListener);
        this.client.go();
        assert (LockOrder.testWait(this));
        while (!this.disconnected) {
            this.wait();
        }
    }

    private void clientEvent(Event event) {
        assert (Thread.holdsLock(this));
        switch (event.type) {
            case 86: {
                this.logger.fine("Login successful." + (event.arg == null ? "" : " Message = " + event.arg));
                this.subscribedRoom = null;
                this.allRooms = new ArrayList();
                this.createGameSent = false;
                this.gameInPlay = null;
                this.joinedGameIds.clear();
                this.myTournamentGames.clear();
                this.roomWatch = new RoomWatch(){

                    @Override
                    public void activate() {
                        GtpClient.this.lookForRoom();
                    }

                    @Override
                    public void deactivate() {
                        GtpClient.this.allRooms = null;
                    }
                };
                this.client.sendSync(this.roomWatch);
                State.get(this.client).canPlayRanked(true);
                this.client.requestArchive(this.client.getMe().name, archEvent -> {
                    if (archEvent.type == 35) {
                        State.get(this.client).setArchive((CArchive)archEvent.source);
                    }
                });
                if (this.options.mode != 2) break;
                this.client.requestArchive(this.client.getMe().name, archEvent -> {
                    if (archEvent.type == 35) {
                        for (Object rawGame : (Collection)archEvent.arg) {
                            GameSummary game = (GameSummary)rawGame;
                            if (!game.getGameType().isTournament()) continue;
                            this.myTournamentGames.add(game.id);
                        }
                    }
                });
                break;
            }
            case 93: {
                this.logger.warning("Error from server: " + event.arg);
                break;
            }
            case 87: {
                this.client.removeListener(this.clientListener);
                if (event.arg == null) {
                    this.logger.fine("Normal disconnection from server.");
                } else {
                    this.logger.severe("Unexpected disconnect: " + event.arg);
                    this.success = false;
                }
                this.disconnected = true;
                this.notifyAll();
                break;
            }
            case 90: {
                CRoom room = (CRoom)event.arg;
                if (this.allRooms != null) {
                    this.allRooms.add(room);
                }
                room.addListener(this.roomListener);
                this.foundRoom(room);
                break;
            }
            case 96: {
                CGameListEntry gle = (CGameListEntry)event.arg;
                if (gle.gameType == GameType.CHALLENGE) break;
                ((KCGame)event.arg).addListener(this.gameListener);
                break;
            }
            case 95: {
                this.client.sendKeepAlive();
                break;
            }
            case 94: {
                CConvo convo = (CConvo)event.arg;
                new GtpConvo(convo, this.protocol, this.options, GtpConvo.Type.PRIVATE, convo.peer.name);
                break;
            }
            case 104: {
                GameSummary gsum = (GameSummary)event.arg;
                if (!(this.gameInPlay != null || gsum.isFinished() || gsum.getGameType().isEditable() || this.options.mode == 2 && !gsum.getGameType().isTournament())) {
                    this.logger.fine("Game \"" + gsum.getLocalDesc() + "\" loaded, joining");
                    if (State.get((Client)this.client).myChal != null) {
                        this.logger.finer("Closing my open game so I can join the loaded game");
                        State.get((Client)this.client).myChal.sendUnjoinRequest();
                    }
                    this.client.sendJoinRequest(gsum.id);
                    break;
                }
                this.logger.fine("Game \"" + gsum.getLocalDesc() + "\" loaded, I'm already playing, so I can't join");
                break;
            }
            case 105: {
                this.logger.log(Level.SEVERE, "Fatal internal error", (Throwable)event.arg);
                this.success = false;
                if (this.client == null) break;
                this.client.logout();
                break;
            }
            case 106: {
                Preferences.userNodeForPackage(this.getClass()).putLong(CLIENT_ID_PREF, (Long)event.arg);
                break;
            }
            case 109: {
                Object[] args = (Object[])event.arg;
                KCGame game = (KCGame)args[1];
                if (game.isJoined()) break;
                this.logger.fine("Tournament game found, will join");
                if (State.get((Client)this.client).myChal != null) {
                    this.logger.warning("Had an open game when tournament game found, closing open game");
                    State.get((Client)this.client).myChal.sendUnjoinRequest();
                }
                if (this.gameInPlay == null) {
                    game.sendJoinRequest();
                    break;
                }
                this.logger.warning("Already had a game in play! Can't join tournament game.");
                break;
            }
        }
    }

    private void roomCatEvent(Event event) {
    }

    private void roomEvent(Event event) {
        switch (event.type) {
            case 15: {
                CRoom room = (CRoom)event.source;
                if (this.subscribedRoom != null) {
                    this.logger.finer("Entered \"" + room.getName() + "\"; already subscribed to a room, leaving.");
                    room.sendUnjoinRequest();
                    break;
                }
                if (this.options.roomWanted != null && !this.options.roomWanted.equals(room.getName())) {
                    this.logger.finer("Entered \"" + room.getName() + "\"; want to be in \"" + this.options.roomWanted + "\" instead, so I will leave.");
                    room.sendUnjoinRequest();
                    break;
                }
                this.logger.fine("Joined room \"" + room.getName() + "\"");
                this.subscribedRoom = room;
                this.startGame();
                break;
            }
            case 75: {
                this.gameInRoomEvent((CRoom)event.source, (KCGame)event.arg);
                break;
            }
            case 78: {
                this.foundRoom((CRoom)event.source);
                break;
            }
            case 57: {
                CGameListEntry game = (CGameListEntry)event.arg;
                if (!(game instanceof KCChallenge) || this.options.mode != 3 && this.options.mode != 2) break;
                this.examineChallengeAndJoin((KCChallenge)game);
                break;
            }
        }
    }

    private void foundRoom(final CRoom room) {
        if (this.subscribedRoom == null && room.getName().equals(this.options.roomWanted)) {
            this.logger.finer("Found room " + room.getName() + ", will join");
            room.sendJoinRequest();
            this.roomWatch = new RoomWatch(){

                @Override
                public void activate() {
                    GtpClient.this.logger.warning("Couldn't join room " + room.getName());
                    GtpClient.this.client.logout();
                }
            };
            this.client.sendSync(this.roomWatch);
        }
    }

    private void gameInRoomEvent(CRoom gameRoom, KCGame game) {
        if (gameRoom == this.subscribedRoom) {
            if (this.gameInPlay != null) {
                return;
            }
            this.examineGameAndJoin(game);
        }
    }

    private void gameEvent(Event event) {
        KCGame game = (KCGame)event.source;
        switch (event.type) {
            case 15: {
                this.logger.finer("Joined game");
                this.createGameSent = false;
                this.joinedGameIds.add(game.id);
                if (game.getRole() == null) {
                    this.logger.warning("Joined a game, I am not in it. I'll just leave.");
                    game.sendUnjoinRequest();
                    return;
                }
                this.gameInPlay = game;
                ClientGtpGame cgGame = new ClientGtpGame(this.client, game, this.protocol, this.options);
                cgGame.addListener(this.protocolListener);
                new GtpConvo(game, this.protocol, this.options, GtpConvo.Type.GAME, game.getPlayer((Role)(game.getRole() == KRole.WHITE ? KRole.BLACK : KRole.WHITE)).name);
                break;
            }
            case 16: {
                if (this.gameInPlay == game) {
                    this.gameInPlay = null;
                }
                if (game.getRole() == null) break;
                this.logger.fine("Game ended. Starting another.");
                this.startGame();
                break;
            }
            case 22: {
                this.joinedGameIds.remove(game.id);
            }
        }
    }

    private static void showErrors(InputStream errIn, Logger staticLogger) {
        byte[] buf = new byte[1024];
        try {
            while (true) {
                int len;
                if ((len = errIn.read(buf)) == -1) {
                    staticLogger.finer("EOF from engine's stderr");
                    return;
                }
                System.err.write(buf, 0, len);
            }
        }
        catch (Throwable thrown) {
            staticLogger.log(Level.WARNING, "Error while copying engine's stderr", thrown);
            return;
        }
    }

    private void protocolEvent(Event event) {
        switch (event.type) {
            case 0: {
                this.engineError = true;
                if (this.gameInPlay == null) {
                    this.logger.fine("EOF from engine, closing down.");
                    if (this.client == null) {
                        this.success = false;
                        break;
                    }
                    this.client.logout();
                    break;
                }
                this.logger.severe("EOF from engine in the middle of a game! Shutting down.");
                this.success = false;
                if (this.client == null) break;
                this.client.logout();
                break;
            }
            case 1: {
                if (!this.waitingToStartGame) break;
                this.startGame();
                break;
            }
            case 2: {
                this.engineError = true;
                this.logger.log(Level.SEVERE, "Error from GTP Communications layer, closing down.", (Throwable)event.arg);
                this.success = false;
                if (this.client == null) break;
                this.client.logout();
                break;
            }
            case 3: {
                this.logger.finest("Command sent to engine: " + event.arg);
                break;
            }
            case 4: {
                this.logger.finest("Queued command sent to engine: " + event.arg);
                break;
            }
            case 5: {
                this.logger.finest("Command queued for sending to engine: " + event.arg);
                break;
            }
            case 6: {
                Protocol.CommandResponsePair pair = (Protocol.CommandResponsePair)event.arg;
                this.logger.finest("Got successful response to command \"" + pair.command + "\": " + pair.response);
                break;
            }
            case 7: {
                Protocol.CommandResponsePair pair = (Protocol.CommandResponsePair)event.arg;
                this.logger.warning("Got error response to command \"" + pair.command + "\": " + pair.response);
                break;
            }
            case 8: {
                this.logger.severe("Got malformed response from engine: " + event.arg);
                break;
            }
            case 9: {
                this.logger.warning("Got response from engine when none was expected: " + event.arg);
                break;
            }
            case 10: {
                this.logger.finer("Restarting game. Reason: " + event.arg);
                break;
            }
            case 11: {
                this.logger.fine("Aborting command: " + event.arg);
                break;
            }
            case 19: {
                this.logger.fine("Undo after move submitted to KGS, but before move came back. Assuming move will never happen.");
                break;
            }
            case 12: {
                this.logger.warning("Engine did not provide as many handicap stones as rules requested.");
                break;
            }
            case 13: {
                this.logger.warning("Fewer handicap stones than rules requested found in game data.");
                break;
            }
            case 14: {
                this.logger.warning("Engine provided \"pass\" as a handicap stone");
                break;
            }
            case 15: {
                this.logger.fine("Ignoring response to aborted command: " + event.arg);
                break;
            }
            case 16: {
                this.logger.warning("After board cleanup, engine still claimed dead stone at " + Protocol.toGtpVertex(((Chain)event.arg).getMember(), ((GtpGame)event.source).getGame().size));
                break;
            }
            case 17: {
                this.logger.fine("Dead stones all submitted to server");
                break;
            }
            case 18: {
                this.logger.fine("Cannot restart with a new game, quitting the engine.");
                break;
            }
            default: {
                this.logger.warning("Unknown event code " + event.type + " from " + (event.source == this.protocol ? "Protocol" : "ClientGtpGame"));
            }
        }
    }

    private void startGame() {
        if (!this.protocol.isIdle()) {
            this.logger.finest("Still an outstanding command, will wait until the system is idle before making a new game.");
            this.waitingToStartGame = true;
            return;
        }
        if (State.get((Client)this.client).myChal != null || this.gameInPlay != null) {
            this.logger.finest("Already in a game, will not make the open game yet.");
            return;
        }
        for (CGameListEntry game : this.subscribedRoom.getGames().values()) {
            if (!(game.gameType == GameType.CHALLENGE ? this.options.mode != 1 && this.examineChallengeAndJoin((KCChallenge)game) : this.examineGameAndJoin((KCGame)game))) continue;
            return;
        }
        boolean gameStarted = false;
        if (this.options.mode == 1 || this.options.mode == 4) {
            this.logger.finer("No games to join. Creating an open game.");
            gameStarted = true;
            this.subscribedRoom.sendNewGameRequest(this.makeDefaultProposal(), this.options.gameNotes, true, cliEvent -> {
                if (cliEvent.type == 118) {
                    new GtpChal(this.client, (KCChallenge)cliEvent.arg);
                }
            });
            this.createGameSent = true;
        }
        if (this.options.mode == 0 || this.options.mode == 4) {
            this.logger.finer("No games to join. Starting automatch.");
            gameStarted = true;
            if (this.client.getMe().getRank() == 0 && this.options.automatchRank == 0) {
                throw new IllegalStateException("In automatch mode with no rank, you must provide the \"automatch.rank\" option");
            }
            this.client.sendAutomatchCreate(0x960 | (this.protocol.isCommandSupported("place_free_handicap") && this.protocol.isCommandSupported("set_free_handicap") ? 9 : 0) | this.options.automatchSpeed | this.options.automatchRank << 26);
        }
        if (!gameStarted) {
            this.logger.finer("No games to join. I'll just sit and wait.");
        }
        this.waitingToStartGame = false;
    }

    private boolean examineChallengeAndJoin(KCChallenge chal) {
        if (this.options.opponent != null && chal.getPlayer(KRole.CHALLENGE_CREATOR).canonName().equals(User.canonName(this.options.opponent)) && this.options.mode != 2) {
            this.logger.fine("Found a challenge with " + this.options.opponent + ", joining it");
            new GtpChal(this.client, chal);
            chal.sendJoinRequest();
            return true;
        }
        return false;
    }

    private boolean examineGameAndJoin(KCGame game) {
        if (this.options.mode == 2 && !game.gameType.isTournament()) {
            return false;
        }
        if (game.gameType == GameType.CHALLENGE && this.options.mode != 1 && this.options.opponent != null && this.options.opponent.equalsIgnoreCase(game.getPlayer((Role)KRole.WHITE).name)) {
            this.logger.finer("Found game with our opponent " + this.options.opponent + "; will join it.");
            if (this.protocol.isIdle()) {
                if (this.createGameSent) {
                    this.createGameSent = false;
                } else {
                    game.sendJoinRequest();
                }
                return true;
            }
            this.logger.finest("Still an outstanding command, will wait until the system is idle before joining the game.");
            this.waitingToStartGame = true;
            return true;
        }
        if (!(game.isOver() || game.gameType == GameType.CHALLENGE || game.gameType.isEditable() || this.joinedGameIds.contains(game.id) || !game.gameType.isMainRole(game.getRole()) || this.options.opponent != null && game.getRole(this.options.opponent) == null)) {
            if (State.get((Client)this.client).myChal != null) {
                this.logger.finer("Closing my open game so I can join a resumed game");
                State.get((Client)this.client).myChal.sendUnjoinRequest();
            }
            this.logger.finer("Found old game with opponent " + game.getPlayer((Role)KRole.forColor((int)Go.opponent((int)game.getRole().team))).name + "; will join it.");
            if (this.protocol.isIdle()) {
                if (this.createGameSent) {
                    this.createGameSent = false;
                } else {
                    game.sendJoinRequest();
                }
                return true;
            }
            this.logger.finest("Still an outstanding command, will wait until the system is idle before joining the game.");
            this.waitingToStartGame = true;
            return true;
        }
        return false;
    }

    private KCProposal makeDefaultProposal() {
        KCProposal prop = new KCProposal(State.get(this.client).canPlayRanked() ? KGameType.RANKED : KGameType.FREE);
        prop.setRules(new Rules(this.options.rules));
        ((KProposal.KUserRole)prop.getUserRole(false)).setUser(this.client.getMe());
        return prop;
    }

    public static void main(String[] cmdLine) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            System.exit(1);
        });
        Defs.setBundle(new KBundle("com.gokgs.client.res.Res", Locale.getDefault(), false));
        defsLoaded = true;
        List<String> argList = Arrays.asList(cmdLine);
        if (argList.contains("-help") || argList.contains("--help") || argList.contains("-usage") || argList.contains("--usage") || cmdLine.length == 0) {
            Options.usage();
            System.exit(1);
        }
        Options options = null;
        String versString = "KGS GTP Client v" + Config.get("version.major") + "." + Config.get("version.minor") + "." + Config.get("version.bugfix") + (Config.get("version.beta") == null ? "" : " " + Config.get("version.beta"));
        try {
            OutputStream out;
            InputStream in;
            Properties props = new Properties();
            props.load(new BufferedInputStream(new FileInputStream(cmdLine[0])));
            for (int i = 1; i < cmdLine.length; ++i) {
                String[] parts = cmdLine[i].split("=", 2);
                if (parts.length == 1) {
                    props.setProperty(parts[0], "t");
                    continue;
                }
                props.setProperty(parts[0], parts[1]);
            }
            options = new Options(props, Defs.getPkgName(GtpClient.class));
            options.logger.fine(versString + " starting up");
            if (options.engineCommand != null) {
                Process proc = Runtime.getRuntime().exec(options.engineCommand);
                InputStream err = proc.getErrorStream();
                Logger logger = options.logger;
                new Thread(() -> GtpClient.showErrors(err, logger), "stderr relay").start();
                in = proc.getInputStream();
                out = proc.getOutputStream();
            } else if (options.inFile != null && options.outFile != null) {
                ParallelOpen opener = new ParallelOpen(options.inFile, options.outFile);
                in = opener.getIn();
                out = opener.getOut();
            } else if (options.enginePort != -1) {
                ServerSocket serverSock = new ServerSocket(options.enginePort);
                options.logger.finer("Waiting for a connection on port " + options.enginePort);
                Socket sock = serverSock.accept();
                options.logger.finer("Got connection. Closing server socket.");
                serverSock.close();
                in = sock.getInputStream();
                out = sock.getOutputStream();
            } else {
                in = System.in;
                out = System.out;
            }
            GtpClient client = new GtpClient(in, out, options);
            System.exit(client.go() ? 0 : 1);
        }
        catch (Throwable thrown) {
            if (options == null) {
                System.err.println(versString + " cannot start up, fatal error:");
                thrown.printStackTrace();
            } else {
                options.logger.log(Level.SEVERE, "Fatal exception", thrown);
            }
            System.exit(2);
        }
    }

    private void lookForRoom() {
        for (CRoom room : this.allRooms) {
            if (!room.getName().equals(this.options.roomWanted)) continue;
            this.logger.finer("Found room " + room.getName() + ", will join");
            this.subscribedRoom = room;
            room.sendJoinRequest();
            return;
        }
        for (CRoom room : this.allRooms) {
            room.fetchName();
        }
        this.allRooms = null;
        this.roomWatch = new RoomWatch(){

            @Override
            public void activate() {
                GtpClient.this.allRoomsSeen();
            }
        };
        this.client.sendSync(this.roomWatch);
    }

    private void allRoomsSeen() {
        for (RoomCategories cat : RoomCategories.values()) {
            if (!cat.isVisible()) continue;
            this.client.getRoomCategory(cat).addListener(this.roomCatListener);
        }
        this.client.sendSync(this::joinedCatChannels);
    }

    private void joinedCatChannels() {
        for (RoomCategories cat : RoomCategories.values()) {
            if (!cat.isVisible()) continue;
            this.client.getRoomCategory(cat).removeListener(this.roomCatListener);
        }
        this.roomWatch = new RoomWatch(){

            @Override
            public void activate() {
                GtpClient.this.logger.severe("Client cannot find room to join. Logging out.");
                GtpClient.this.client.logout();
            }
        };
        this.client.sendSync(this.roomWatch);
    }

    private abstract class RoomWatch
    implements Runnable {
        private RoomWatch() {
        }

        @Override
        public void run() {
            if (GtpClient.this.roomWatch == this) {
                GtpClient.this.roomWatch = null;
                if (GtpClient.this.subscribedRoom == null) {
                    this.activate();
                    return;
                }
            }
            this.deactivate();
        }

        public abstract void activate();

        public void deactivate() {
        }
    }
}
