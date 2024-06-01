/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.igoweb.go.GameUndo;
import org.igoweb.go.Go;
import org.igoweb.go.Goban;
import org.igoweb.go.GobanSnapshot;
import org.igoweb.go.Loc;
import org.igoweb.go.Rules;
import org.igoweb.go.Undo;
import org.igoweb.util.Defs;

public class Game
extends Goban {
    public static final int CHANGE_WHOSE_MOVE_EVENT = 2;
    public static final int MOVE_EVENT = 3;
    public static final int CHANGE_SCORE_EVENT = 4;
    public static final int CHANGE_STATE_EVENT = 5;
    public static final int LEGAL = 0;
    public static final int ILLEGAL = 1;
    public static final int KO = 2;
    private int whoseMove;
    private int whoMadeLastMove;
    private int moveNum = 0;
    private final HashMap<GobanSnapshot, Integer> snapshots = new HashMap();
    private static final Integer ONE = new Integer(1);
    private int passes = 0;
    private Rules rules;
    private Loc lastMove = null;
    private float[] scores = new float[2];
    private final ArrayList<Undo> undoStack;
    private GameUndo currentUndo;
    private boolean forceSnapshots = false;
    public static final NumberFormat SCORE_FORMATTER = Game.makeScoreFormatter();

    public Game(Rules rules) {
        this(rules, true);
    }

    public Game(Rules rules, boolean useUndoStack) {
        super(rules.getSize());
        this.undoStack = useUndoStack ? new ArrayList() : null;
        this.rules = new Rules(rules);
        this.whoseMove = rules.isBlackFirst() ? 0 : 1;
        this.whoMadeLastMove = 2;
    }

    public boolean hasUndoStack() {
        return this.undoStack != null;
    }

    public boolean isPlayOver() {
        return this.passes >= 2;
    }

    public final boolean move(Loc loc) {
        return this.move(this.whoseMove, loc);
    }

    public boolean move(int color, Loc loc) {
        if (this.isLegal(color, loc) != 0) {
            return false;
        }
        this.addSnapshot();
        if (color != this.whoseMove) {
            this.clearSimpleKo();
            this.whoseMove = color;
        }
        if (loc == Loc.PASS) {
            ++this.passes;
            ++this.moveNum;
            this.clearSimpleKo();
            this.whoMadeLastMove = this.whoseMove;
            this.whoseMove = Go.opponent(this.whoseMove);
            this.lastMove = loc;
            this.emit(3, loc);
            if (this.passes == 2) {
                this.emit(5);
            }
            return true;
        }
        this.passes = 0;
        this.lastMove = loc;
        ++this.moveNum;
        if (this.rules.isFixedHandicap() || this.moveNum >= this.rules.getHandicap() || this.whoseMove == 1 || this.whoMadeLastMove == 1) {
            this.whoMadeLastMove = this.whoseMove;
            this.whoseMove = Go.opponent(this.whoseMove);
        } else {
            this.whoMadeLastMove = this.whoseMove;
        }
        if (!super.addStone(loc, this.whoMadeLastMove, true, this.currentUndo)) {
            throw new RuntimeException("Move should be legal!");
        }
        this.emit(3, loc);
        return true;
    }

    public final int isLegal(Loc loc) {
        return this.isLegal(this.whoseMove, loc);
    }

    public int isLegal(int color, Loc loc) {
        if (loc == Loc.PASS) {
            return 0;
        }
        if (loc.x >= this.size || loc.y >= this.size) {
            return 1;
        }
        if (this.getColor(loc) != 2) {
            return 1;
        }
        if (this.isSimpleKo(loc, color)) {
            return 2;
        }
        boolean singleSuicide = true;
        boolean multiSuicide = true;
        Iterator<Loc> neighbors = loc.neighbors(this.size);
        while (neighbors.hasNext()) {
            Loc n = neighbors.next();
            int nColor = this.getColor(n);
            if (nColor == 2) {
                singleSuicide = false;
                multiSuicide = false;
                break;
            }
            if (nColor == color) {
                singleSuicide = false;
                if (this.getChain(n).countLiberties() <= 1) continue;
                multiSuicide = false;
                break;
            }
            if (nColor != Go.opponent(color) || this.getChain(n).countLiberties() != 1) continue;
            singleSuicide = false;
            multiSuicide = false;
            break;
        }
        if (singleSuicide || multiSuicide && !this.rules.isSuicideLegal()) {
            return 1;
        }
        if (this.rules.getKoType() == 0) {
            return 0;
        }
        if (this.isRepeat(color, loc)) {
            return 2;
        }
        return 0;
    }

    private boolean isRepeat(int color, Loc loc) {
        GameUndo undo = this.createUndo();
        if (!super.addStone(loc, color, true, false, undo)) {
            throw new RuntimeException("Can't test for repeat");
        }
        int newWhoseMove = this.whoseMove;
        if (this.rules.isFixedHandicap() || this.moveNum + 1 >= this.rules.getHandicap()) {
            newWhoseMove = Go.opponent(this.whoseMove);
        }
        boolean result = this.snapshots.containsKey(new GobanSnapshot(this, this.rules, newWhoseMove));
        this.undo(undo, false);
        return result;
    }

    public int getRepeatCount() {
        Integer result = this.snapshots.get(new GobanSnapshot(this, this.rules, this.whoseMove));
        return result == null ? 0 : result;
    }

    @Override
    protected void undo(Undo gobanUndo, boolean notifyListeners) {
        GameUndo undo = (GameUndo)gobanUndo;
        int prevWhoseMove = this.whoseMove;
        this.lastMove = undo.lastMove;
        this.whoseMove = undo.whoseMove;
        this.moveNum = undo.moveNum;
        this.whoMadeLastMove = undo.whoMadeLastMove;
        Iterator<GobanSnapshot> newSnaps = undo.getSnapshots();
        while (newSnaps.hasNext()) {
            this.snapshots.remove(newSnaps.next());
        }
        super.undo(undo, notifyListeners);
        if (this.passes != undo.numPasses) {
            int oldPasses = this.passes;
            this.passes = undo.numPasses;
            this.emit(3, Loc.PASS);
            if (oldPasses >= 2 != this.passes >= 2) {
                this.emit(5);
            }
        } else if (this.whoseMove != prevWhoseMove) {
            this.emit(2);
        }
    }

    public final Loc getLastMove() {
        return this.lastMove;
    }

    public int getWhoseMove() {
        return this.whoseMove;
    }

    public void setWhoseMove(int newWhoseMove) {
        if (this.whoseMove != newWhoseMove) {
            this.whoseMove = newWhoseMove;
            this.emit(2);
        }
    }

    public final int getMoveNum() {
        return this.moveNum;
    }

    public void setRulesAndReset(Rules newRules) {
        this.reset();
        this.rules = new Rules(newRules);
    }

    @Override
    public void reset() {
        int prevWhoseMove = this.whoseMove;
        this.whoseMove = this.rules.isBlackFirst() ? 0 : 1;
        this.moveNum = 0;
        this.passes = 0;
        this.lastMove = null;
        this.snapshots.clear();
        if (this.undoStack != null) {
            this.undoStack.clear();
        }
        super.reset();
        this.currentUndo = this.createUndo();
        if (this.whoseMove != prevWhoseMove) {
            this.emit(2);
        }
    }

    @Override
    public void removeStone(Loc loc) {
        super.removeStone(loc, this.currentUndo);
    }

    @Override
    public boolean addStone(Loc loc, int color, boolean countCaptures) {
        return super.addStone(loc, color, countCaptures, this.currentUndo);
    }

    @Override
    public String toString() {
        return super.toString() + "Passes: " + this.passes + "\n";
    }

    private void addSnapshot() {
        if (this.forceSnapshots || this.rules.getKoType() != 0) {
            GobanSnapshot im = new GobanSnapshot(this, this.rules, this.whoseMove);
            Integer prevVal = this.snapshots.put(im, ONE);
            if (prevVal != null) {
                this.snapshots.put(im, new Integer(prevVal + 1));
            }
            this.currentUndo.addSnapshot(im);
        }
    }

    public final Rules getRules() {
        return new Rules(this.rules);
    }

    public final void setScore(int color, float val) {
        this.scores[color] = val;
        this.emit(4);
    }

    public final float getScore(int color) {
        return this.scores[color];
    }

    public boolean hasMoved(int color) {
        if (color == 1) {
            if (this.rules.getHandicap() == 0) {
                return this.moveNum >= 2;
            }
            if (this.rules.isFixedHandicap()) {
                return this.moveNum > 0;
            }
            return this.moveNum > this.rules.getHandicap();
        }
        if (color == 0) {
            if (this.rules.getHandicap() > 0 && this.rules.isFixedHandicap()) {
                return this.moveNum > 1;
            }
            return this.moveNum > 0;
        }
        return false;
    }

    public String lastMoveDesc() {
        if (this.lastMove == null) {
            return null;
        }
        return Defs.getString(-1337055800, new Object[]{new Integer(this.whoMadeLastMove), this.lastMove.toCoords(this.size)});
    }

    public int getWhoMadeLastMove() {
        return this.whoMadeLastMove;
    }

    public void makeUndoBreakpoint() {
        if (this.undoStack != null) {
            this.undoStack.add(this.currentUndo);
        }
        this.currentUndo = this.createUndo();
    }

    public final void undo() {
        this.undo(1);
    }

    public void undo(int numUndos) {
        if (numUndos < 1) {
            return;
        }
        if (numUndos == 1) {
            this.undo(this.currentUndo);
            this.currentUndo = this.undoStack == null ? null : (GameUndo)this.undoStack.remove(this.undoStack.size() - 1);
        } else {
            this.undoStack.add(this.currentUndo);
            int newStackSize = this.undoStack.size() - numUndos;
            GameUndo undo = (GameUndo)this.undoStack.get(newStackSize);
            for (int i = 1; i < numUndos; ++i) {
                undo.combine((GameUndo)this.undoStack.get(newStackSize + i));
            }
            this.undo(undo);
            this.currentUndo = (GameUndo)this.undoStack.get(newStackSize - 1);
            this.undoStack.subList(newStackSize - 1, this.undoStack.size()).clear();
        }
    }

    private GameUndo createUndo() {
        return new GameUndo(this.captures, this.getSimpleKoLoc(), this.getSimpleKoColor(), this.whoseMove, this.moveNum, this.whoMadeLastMove, this.lastMove, this.passes);
    }

    public final void setForceSnapshots(boolean newVal) {
        this.forceSnapshots = newVal;
    }

    private static NumberFormat makeScoreFormatter() {
        NumberFormat result = NumberFormat.getInstance();
        result.setMinimumFractionDigits(1);
        return result;
    }
}
