/*
 * Decompiled with CFR 0.152.
 */
package com.gokgs.client;

import com.gokgs.shared.KGameSummary;
import com.gokgs.shared.KGameType;
import com.gokgs.shared.KRole;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.igoweb.games.Clock;
import org.igoweb.go.Game;
import org.igoweb.go.Go;
import org.igoweb.go.Loc;
import org.igoweb.go.Rules;
import org.igoweb.go.sgf.GameUpdater;
import org.igoweb.go.sgf.Node;
import org.igoweb.go.sgf.Prop;
import org.igoweb.go.sgf.SgfEvent;
import org.igoweb.go.sgf.Tree;
import org.igoweb.igoweb.client.CChannel;
import org.igoweb.igoweb.client.CGame;
import org.igoweb.igoweb.client.CGameListEntry;
import org.igoweb.igoweb.client.Conn;
import org.igoweb.igoweb.shared.GameAction;
import org.igoweb.igoweb.shared.GameSummary;
import org.igoweb.igoweb.shared.GameType;
import org.igoweb.igoweb.shared.IBundle;
import org.igoweb.igoweb.shared.MsgTypesDown;
import org.igoweb.igoweb.shared.MsgTypesUp;
import org.igoweb.igoweb.shared.Role;
import org.igoweb.igoweb.shared.TxMessage;
import org.igoweb.igoweb.shared.User;
import org.igoweb.resource.Plural;
import org.igoweb.util.Defs;

public class KCGame
extends CGame {
    private static final int EVENT_BASE = 136;
    public static final int EVENT_LIMIT = 136;
    private int moveNum;
    private Rules rules;
    private Tree tree;
    private Game game;
    private final boolean[] doneSent = new boolean[2];
    private int doneId = 1;
    private int doneIdSent = -1;
    private int prevChatMoveNum = -2;

    public KCGame(Conn newConn, GameType newGameType, int chanId, DataInputStream in) throws IOException {
        super(newConn, newGameType, chanId, in);
    }

    @Override
    protected void handleMessage(MsgTypesDown msgType, DataInputStream in) throws IOException {
        switch (msgType) {
            case GAME_LOOP_WARNING: {
                this.emit(13, Defs.getString(-1772731644));
                break;
            }
            case GAME_OVER: {
                Object[] scoreDesc = new Object[]{this.computeScoreDesc(0), this.computeScoreDesc(1), in.readShort()};
                this.emit(65, scoreDesc);
                break;
            }
            case GAME_EDITOR_LEFT: {
                User u = this.conn.getUser(in);
                if (u == this.conn.getMe()) break;
                this.emit(13, Defs.getString(2031923679, u.name));
                break;
            }
            default: {
                super.handleMessage(msgType, in);
            }
        }
    }

    public Rules getRules() {
        return this.rules;
    }

    @Override
    public int getMoveNum() {
        return this.tree == null ? (this.isOver() ? 0 : this.moveNum) : this.tree.getActiveNode().getMoveNum();
    }

    public short getScore() {
        if (!this.isOver()) {
            throw new IllegalStateException("Asked for score in a game that wasn't scored yet");
        }
        return (short)this.moveNum;
    }

    public void sendMove(Loc loc) {
        TxMessage msg = this.buildMessage(MsgTypesUp.GAME_MOVE);
        msg.write(loc.x);
        msg.write(loc.y);
        this.conn.send(msg);
        this.getActions().clear();
    }

    public void sendMarkDead(Loc loc, boolean isDead) {
        TxMessage msg = this.buildMessage(MsgTypesUp.GAME_MARK_LIFE);
        msg.writeBoolean(!isDead);
        msg.write(loc.x);
        msg.write(loc.y);
        this.conn.send(msg);
    }

    @Override
    protected void readFrom(DataInputStream in) throws IOException {
        super.readFrom(in);
        byte size = in.readByte();
        byte hcap = in.readByte();
        byte komi2 = in.readByte();
        if (this.rules == null) {
            this.rules = new Rules(size);
            this.rules.setHandicap(hcap);
            this.rules.setKomi((float)komi2 * 0.5f);
        }
        this.moveNum = in.readShort();
    }

    public Tree getSgfTree() {
        return this.tree;
    }

    public boolean isDoneSent(int color) {
        return this.doneSent[color];
    }

    public void sendDone() {
        if (this.doneIdSent != this.doneId) {
            TxMessage tx = this.buildMessage(MsgTypesUp.GAME_SCORING_DONE);
            tx.writeShort(this.doneId);
            this.conn.send(tx);
            if (this.getRole() != null && this.getRole().team >= 0) {
                this.doneSent[this.getRole().team] = true;
            }
            this.doneIdSent = this.doneId;
        }
    }

    @Override
    protected void join(DataInputStream in) throws IOException {
        byte eventType;
        boolean firstEvent = true;
        while ((eventType = in.readByte()) != -1) {
            if (firstEvent) {
                this.tree = new Tree();
                firstEvent = false;
            }
            this.applyTreeEvent(SgfEvent.readFrom(in, eventType, this.tree.getActiveNode().id));
        }
        this.rules = this.tree.root.findProp(0).getRules();
        super.join(in);
    }

    @Override
    protected void unjoin() {
        this.tree = null;
        this.game = null;
        super.unjoin();
    }

    @Override
    protected Clock buildClock(Role role) {
        return role == KRole.WHITE || role == KRole.BLACK ? this.rules.buildClock() : null;
    }

    @Override
    protected boolean readGameState(DataInputStream in) throws IOException {
        if (this.game == null) {
            this.game = new Game(this.rules);
            new GameUpdater(this.tree, this.game, this.getClock(KRole.WHITE), this.getClock(KRole.BLACK));
        }
        boolean changed = super.readGameState(in);
        switch (in.readByte()) {
            case 1: {
                int prevScore = this.moveNum;
                this.moveNum = in.readShort();
                if (prevScore == this.moveNum) break;
                changed = true;
                break;
            }
            case 2: {
                this.doneSent[1] = in.readBoolean();
                this.doneSent[0] = in.readBoolean();
                this.game.setScore(1, (float)in.readShort() * 0.5f);
                this.game.setScore(0, (float)in.readShort() * 0.5f);
                this.doneId = in.readShort();
                if (this.doneId != this.doneIdSent) {
                    this.doneIdSent = -1;
                }
                changed = true;
                break;
            }
        }
        return changed;
    }

    @Override
    protected void readGameUpdate(DataInputStream in) throws IOException {
        if (this.tree == null) {
            return;
        }
        while (in.available() > 0) {
            this.applyTreeEvent(SgfEvent.readFrom(in, in.readByte(), this.tree.getActiveNode().id));
        }
    }

    private void applyTreeEvent(SgfEvent event) {
        this.tree.apply(event);
        int srcId = event.srcId;
        switch (event.type) {
            case 0: 
            case 2: {
                this.extractChats((Prop)event.arg, srcId);
                break;
            }
            case 8: {
                for (Object rawProp : (Collection)event.arg) {
                    this.extractChats((Prop)rawProp, srcId);
                }
                break;
            }
        }
    }

    private void extractChats(Prop prop, int srcId) {
        if (prop.type == 24) {
            int chatMoveNum;
            int n = chatMoveNum = this.isOver() ? -1 : this.tree.getNode(srcId).getMoveNum();
            if (chatMoveNum != this.prevChatMoveNum) {
                this.prevChatMoveNum = chatMoveNum;
                this.appendChat(new CChannel.Chat(null, Defs.getString(-1772731640, chatMoveNum), true, false));
            }
            for (String comment : prop.getText().split("\n")) {
                this.appendChat(new CChannel.Chat(null, comment, false, false));
            }
        }
    }

    public Game getGoGame() {
        return this.game;
    }

    public void sendSgfChange(List<SgfEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        boolean messageSent = false;
        try {
            int start = 0;
            int end = events.size();
            while (start < events.size()) {
                TxMessage tx = this.buildMessage(MsgTypesUp.KGS_SGF_CHANGE);
                for (int cur = start; cur < end; ++cur) {
                    events.get(cur).writeTo(tx);
                    if (tx.size() < 8192) continue;
                    tx = null;
                    end = cur;
                    if (end > start) break;
                    ++start;
                    end = events.size();
                    break;
                }
                if (tx == null) continue;
                this.conn.send(tx);
                messageSent = true;
                start = end;
                end = events.size();
            }
            if (messageSent) {
                this.getActions().clear();
                this.getActions().add(new CGameListEntry.UserAction(this.conn.getMe(), GameAction.EDIT_DELAY));
            }
        }
        catch (IOException excep) {
            throw new RuntimeException();
        }
    }

    @Override
    protected GameSummary<User> loadGameSummary(DataInputStream in) throws IOException {
        return KGameSummary.load(in);
    }

    @Override
    public String getDescription(IBundle bundle) {
        switch (this.gameType.id) {
            case 1: {
                return this.getPlayer(KRole.OWNER).getNameAndRank(bundle);
            }
            case 2: {
                return bundle.str(-669080763, new Object[]{this.getPlayer(KRole.OWNER).getNameAndRank(bundle), this.getPlayer(KRole.WHITE).getNameAndRank(bundle), this.getPlayer(KRole.BLACK).getNameAndRank(bundle)});
            }
            case 3: {
                return bundle.str(696435397, new Object[]{this.getPlayer(Role.OWNER).getNameAndRank(bundle), this.getPlayer(KRole.WHITE).getNameAndRank(bundle), this.getPlayer(KRole.WHITE_2).getNameAndRank(bundle), this.getPlayer(KRole.BLACK).getNameAndRank(bundle), this.getPlayer(KRole.BLACK_2).getNameAndRank(bundle)});
            }
            case 6: {
                return bundle.str(877402151, new Object[]{this.getPlayer(KRole.WHITE).getNameAndRank(bundle), this.getPlayer(KRole.WHITE_2).getNameAndRank(bundle), this.getPlayer(KRole.BLACK).getNameAndRank(bundle), this.getPlayer(KRole.BLACK_2).getNameAndRank(bundle)});
            }
        }
        return bundle.str(-1337055791, new Object[]{this.getPlayer(KRole.WHITE).getNameAndRank(bundle), this.getPlayer(KRole.BLACK).getNameAndRank(bundle)});
    }

    @Override
    protected void setOriginal(CGame cGame) {
        if (cGame.isJoined()) {
            this.tree = new Tree(((KCGame)cGame).tree);
        }
        super.setOriginal(cGame);
    }

    public void sendScores(double scoreW, double scoreB) {
        TxMessage tx = this.buildMessage(MsgTypesUp.GAME_SET_SCORES);
        tx.writeShort((short)(scoreW * 2.0));
        tx.writeShort((short)(scoreB * 2.0));
        this.conn.send(tx);
    }

    public String[] formatFinalScores(Object rawDesc) {
        int msgId;
        Object[] descArray = (Object[])rawDesc;
        short scoreResult = (Short)descArray[2];
        Object[] args = new Object[2];
        KRole winner = scoreResult > 0 ? KRole.BLACK : KRole.WHITE;
        KRole loser = KRole.opponent(winner);
        if (this.gameType == KGameType.RENGO) {
            args[0] = Defs.getString(-1772731642, new Object[]{winner.team, this.getPlayer((Role)winner).name, this.getPlayer((Role)(winner == KRole.WHITE ? KRole.WHITE_2 : KRole.BLACK_2)).name});
            args[1] = Defs.getString(-1772731642, new Object[]{loser.team, this.getPlayer((Role)loser).name, this.getPlayer((Role)(loser == KRole.WHITE ? KRole.WHITE_2 : KRole.BLACK_2)).name});
        } else {
            args[0] = Defs.getString(-1772731643, new Object[]{winner.team, this.getPlayer((Role)winner).name});
            args[1] = Defs.getString(-1772731643, new Object[]{loser.team, this.getPlayer((Role)loser).name});
        }
        switch (scoreResult) {
            case -16384: 
            case 16384: {
                msgId = 2031923654;
                break;
            }
            case -16388: 
            case -16385: 
            case 16385: 
            case 16388: {
                msgId = 2031923673;
                break;
            }
            case 16386: {
                msgId = 2031923666;
                break;
            }
            case 16389: {
                throw new IllegalArgumentException(Integer.toString(scoreResult, 16));
            }
            default: {
                return new String[]{Defs.getString(2031923675, this.getGameTitle()), this.formatScoreDesc((float[])descArray[1]), this.formatScoreDesc((float[])descArray[0]), Defs.getString(scoreResult == 0 ? 2031923676 : 2031923685, new Object[]{args[0], Float.valueOf((float)Math.abs(scoreResult) * 0.5f), Plural.getCategory((double)Math.abs(scoreResult) * 0.5, Locale.getDefault())})};
            }
        }
        return new String[]{Defs.getString(msgId, args)};
    }

    public final String getGameTitle() {
        return this.getGameTitle(IBundle.get());
    }

    public String getGameTitle(IBundle bundle) {
        String name = this.getName();
        if (name == null) {
            return this.getGameSummary().getLocalDesc();
        }
        User owner = this.getPlayer(this.gameType.owner);
        if (owner == null) {
            return name;
        }
        String ownerText = owner.isRankWanted() && owner.getRank() != 0 ? owner.getNameAndRank(bundle) : owner.name;
        return Defs.getString(-1772731641, new Object[]{ownerText, name});
    }

    private float[] computeScoreDesc(int color) {
        float[] vals = new float[9];
        Node endNode = this.getSgfTree().getActiveNode();
        int[] numStones = new int[3];
        int[] numTerritory = new int[2];
        int[] numDead = new int[2];
        Iterator<Comparable<Loc>> allLocs = this.game.allLocs();
        while (allLocs.hasNext()) {
            int n = this.game.getColor((Loc)allLocs.next());
            numStones[n] = numStones[n] + 1;
        }
        for (Prop p : endNode) {
            switch (p.type) {
                case 22: {
                    int n = p.getColor();
                    numTerritory[n] = numTerritory[n] + 1;
                    break;
                }
                case 23: {
                    int stoneColor = this.game.getColor(p.getLoc());
                    if (stoneColor >= 2) break;
                    int n = stoneColor;
                    numDead[n] = numDead[n] + 1;
                }
            }
        }
        vals[1] = numTerritory[color];
        if (this.rules.isScoreCaptures()) {
            vals[2] = this.game.caps(color) + numDead[Go.opponent(color)];
        } else {
            vals[3] = numStones[color] - numDead[color];
            vals[4] = (float)(this.game.size * this.game.size + numDead[0] + numDead[1] - (numStones[0] + numStones[1] + numTerritory[0] + numTerritory[1])) * 0.5f;
        }
        if (color == 1) {
            vals[5] = this.rules.getKomi();
            vals[6] = -this.rules.getKomi();
            vals[7] = this.rules.getHandicapComp();
        }
        vals[0] = vals[7];
        for (int i = 1; i < 6; ++i) {
            vals[0] = vals[0] + vals[i];
        }
        vals[8] = color;
        return vals;
    }

    private String formatScoreDesc(float[] vals) {
        Object[] args = new Object[18];
        for (int i = 0; i < 9; ++i) {
            args[i] = Float.valueOf(vals[i]);
            args[i + 9] = Plural.getCategory(vals[i], Locale.getDefault());
        }
        return Defs.getString(2031923677, args);
    }
}
