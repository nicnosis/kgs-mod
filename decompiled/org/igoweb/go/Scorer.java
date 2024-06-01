/*
 * Decompiled with CFR 0.152.
 */
package org.igoweb.go;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;
import org.igoweb.go.Chain;
import org.igoweb.go.Game;
import org.igoweb.go.Go;
import org.igoweb.go.Loc;
import org.igoweb.go.Rules;
import org.igoweb.go.ScoringGoban;

public class Scorer {
    private final float[] scores = new float[2];
    private final ArrayList<Loc> blackTerritory = new ArrayList();
    private final ArrayList<Loc> whiteTerritory = new ArrayList();

    public Scorer(Game game, Collection<Loc> deadLocs) {
        Rules rules = game.getRules();
        this.scores[0] = 0.0f;
        this.scores[1] = rules.getKomi() + (float)rules.getHandicapComp();
        ScoringGoban cleanGoban = new ScoringGoban(game);
        if (rules.isScoreCaptures()) {
            this.scores[0] = this.scores[0] + (float)game.caps(0);
            this.scores[1] = this.scores[1] + (float)game.caps(1);
            Scorer.removeDeadStones(cleanGoban, deadLocs, this.scores);
        } else {
            Scorer.removeDeadStones(cleanGoban, deadLocs, null);
        }
        this.updateTerritoryMarks(cleanGoban, rules, this.scores);
        game.setScore(1, this.scores[1]);
        game.setScore(0, this.scores[0]);
    }

    private void updateTerritoryMarks(ScoringGoban goban, Rules rules, float[] scoresOut) {
        this.territory(0).clear();
        this.territory(1).clear();
        Scorer.computeVisibility(goban, (float[])(rules.isScoreLivingStones() ? scoresOut : null));
        if (rules.isScoreCaptures()) {
            Scorer.fillFakeEyes(goban);
        }
        if (!rules.scoreSeki()) {
            Scorer.markSeki(goban);
        }
        Iterator<Loc> locs = goban.allLocs();
        while (locs.hasNext()) {
            int paramColor;
            Loc loc = locs.next();
            int color = goban.finalColor(loc);
            if (color != 2 || goban.get(loc, ScoringGoban.canSee(0)) == goban.get(loc, ScoringGoban.canSee(1))) continue;
            if (goban.get(loc, ScoringGoban.canSee(0))) {
                paramColor = 0;
                if (goban.finalColor(loc) == 2 || rules.isScoreLivingStones()) {
                    scoresOut[0] = (float)((double)scoresOut[0] + 1.0);
                }
            } else {
                paramColor = 1;
                if (goban.finalColor(loc) == 2 || rules.isScoreLivingStones()) {
                    scoresOut[1] = (float)((double)scoresOut[1] + 1.0);
                }
            }
            this.territory(paramColor).add(loc);
        }
    }

    private static void computeVisibility(ScoringGoban goban, float[] scores) {
        Iterator<Loc> locs = goban.allLocs();
        while (locs.hasNext()) {
            Loc loc = locs.next();
            int color = goban.finalColor(loc);
            if (color != 0 && color != 1) continue;
            if (scores != null) {
                int n = color;
                scores[n] = (float)((double)scores[n] + 1.0);
            }
            if (goban.get(loc, ScoringGoban.canSee(color))) continue;
            int flag = ScoringGoban.canSee(color);
            ArrayList<Loc> visibleLocs = new ArrayList<Loc>();
            visibleLocs.add(loc);
            goban.set(loc, flag, true);
            while (!visibleLocs.isEmpty()) {
                Loc visibleLoc = (Loc)visibleLocs.remove(visibleLocs.size() - 1);
                Iterator<Loc> neighbors = visibleLoc.neighbors(goban.size);
                while (neighbors.hasNext()) {
                    Loc neighbor = neighbors.next();
                    if (goban.finalColor(neighbor) == Go.opponent(color) || goban.get(neighbor, flag)) continue;
                    goban.set(neighbor, flag, true);
                    visibleLocs.add(neighbor);
                }
            }
        }
    }

    private static void removeDeadStones(ScoringGoban goban, Collection<Loc> deadLocs, float[] scores) {
        for (Loc loc : deadLocs) {
            if (goban.getColor(loc) == 2) {
                System.err.println("ERROR - non-stone " + loc + " marked dead!");
                continue;
            }
            if (scores != null) {
                int n = Go.opponent(goban.getColor(loc));
                scores[n] = (float)((double)scores[n] + 1.0);
            }
            goban.set(loc, 0, true);
        }
    }

    private static void fillFakeEyes(ScoringGoban goban) {
        boolean fakeFilled;
        goban.setAll(7, true);
        do {
            Loc loc;
            fakeFilled = false;
            Iterator<Loc> locs = goban.allLocs();
            while (locs.hasNext()) {
                loc = locs.next();
                if (!goban.get(loc, 7)) continue;
                goban.set(loc, 6, false);
            }
            locs = goban.allLocs();
            while (locs.hasNext()) {
                loc = locs.next();
                if (goban.finalColor(loc) != 0 && goban.finalColor(loc) != 1 || goban.get(loc, 6)) continue;
                fakeFilled |= Scorer.checkGroupForFakeEyes(goban, loc);
            }
        } while (fakeFilled);
    }

    private static boolean checkGroupForFakeEyes(ScoringGoban goban, Loc loc) {
        boolean possible;
        Scorer.paintConnectionsAndEyes(goban, loc);
        int numEyes = 0;
        boolean hasRealEye = false;
        Loc eyeLoc = null;
        boolean fakeFound = false;
        Iterator<Loc> locs = goban.allLocs();
        while (locs.hasNext()) {
            loc = locs.next();
            if (!goban.get(loc, 4)) continue;
            ++numEyes;
            eyeLoc = loc;
            boolean allConnected = true;
            Iterator<Loc> neighbors = loc.neighbors(goban.size);
            while (allConnected && neighbors.hasNext()) {
                allConnected = goban.get(neighbors.next(), 5);
            }
            if (!allConnected) continue;
            hasRealEye = true;
        }
        if (hasRealEye) {
            possible = false;
        } else if (numEyes <= 1) {
            possible = false;
            if (numEyes == 1) {
                fakeFound = true;
                goban.set(eyeLoc, ScoringGoban.canSee(0), true);
                goban.set(eyeLoc, ScoringGoban.canSee(1), true);
            }
        } else {
            possible = true;
        }
        Iterator<Loc> locs2 = goban.allLocs();
        while (locs2.hasNext()) {
            loc = locs2.next();
            if (!goban.get(loc, 5)) continue;
            goban.set(loc, 6, true);
            goban.set(loc, 7, possible);
        }
        return fakeFound;
    }

    private static void paintConnectionsAndEyes(ScoringGoban goban, Loc loc) {
        int color = goban.getColor(loc);
        Stack<Loc> locs = new Stack<Loc>();
        goban.setAll(4, false);
        goban.setAll(5, false);
        goban.set(loc, 5, true);
        locs.push(loc);
        while (!locs.empty()) {
            loc = (Loc)locs.pop();
            Iterator<Loc> neighbors = loc.neighbors(goban.size);
            while (neighbors.hasNext()) {
                Loc neighbor = neighbors.next();
                if (goban.finalColor(neighbor) == 2) {
                    if (goban.get(neighbor, 4) || goban.get(neighbor, 5)) continue;
                    if (goban.get(neighbor, ScoringGoban.canSee(Go.opponent(color)))) {
                        goban.set(neighbor, 5, true);
                        locs.push(neighbor);
                        continue;
                    }
                    goban.set(neighbor, 4, true);
                    continue;
                }
                if (goban.getColor(neighbor) != color || goban.get(neighbor, 5)) continue;
                locs.push(neighbor);
                goban.set(neighbor, 5, true);
            }
        }
    }

    private static void markSeki(ScoringGoban goban) {
        Iterator<Loc> locs = goban.allLocs();
        while (locs.hasNext()) {
            Loc loc = locs.next();
            if (goban.getColor(loc) != 0 && goban.getColor(loc) != 1 || goban.get(loc, 8)) continue;
            Scorer.checkGroupForSeki(goban, loc);
        }
    }

    private static void checkGroupForSeki(ScoringGoban goban, Loc loc) {
        boolean isSeki = Scorer.isGroupSeki(goban, loc);
        Iterator<Loc> locs = goban.allLocs();
        while (locs.hasNext()) {
            loc = locs.next();
            if (goban.get(loc, 5)) {
                goban.set(loc, 8, true);
            }
            if (!isSeki || !goban.get(loc, 4)) continue;
            goban.set(loc, ScoringGoban.canSee(0), true);
            goban.set(loc, ScoringGoban.canSee(1), true);
        }
    }

    private static boolean isGroupSeki(ScoringGoban goban, Loc loc) {
        Scorer.paintReachableEyes(goban, loc);
        boolean eyeFound = false;
        int color = goban.getColor(loc);
        Iterator<Loc> locs = goban.allLocs();
        while (locs.hasNext()) {
            loc = locs.next();
            if (!goban.get(loc, 4) || goban.get(loc, 9)) continue;
            if (eyeFound) {
                return false;
            }
            eyeFound = true;
            if (!Scorer.paintEyeAlreadySeen(goban, loc, color)) continue;
            return false;
        }
        return eyeFound;
    }

    private static void paintReachableEyes(ScoringGoban goban, Loc loc) {
        int color = goban.getColor(loc);
        goban.setAll(4, false);
        goban.setAll(5, false);
        goban.setAll(9, false);
        Stack<Loc> locs = new Stack<Loc>();
        locs.push(loc);
        while (!locs.empty()) {
            loc = (Loc)locs.pop();
            Iterator<Loc> neighbors = loc.neighbors(goban.size);
            while (neighbors.hasNext()) {
                Loc neighbor = neighbors.next();
                if (goban.finalColor(neighbor) != 2 && goban.getColor(neighbor) != color || goban.get(neighbor, 4) || goban.get(neighbor, 5)) continue;
                if (goban.getColor(neighbor) == color || goban.get(neighbor, ScoringGoban.canSee(Go.opponent(color)))) {
                    goban.set(neighbor, 5, true);
                } else {
                    goban.set(neighbor, 4, true);
                }
                locs.push(neighbor);
            }
        }
    }

    private static boolean paintEyeAlreadySeen(ScoringGoban goban, Loc loc, int color) {
        Chain enemyChain = null;
        int opponent = Go.opponent(color);
        Stack<Loc> locs = new Stack<Loc>();
        locs.push(loc);
        goban.set(loc, 9, true);
        boolean emptyAwayFromEnemy = false;
        int eyeSize = 0;
        int[] deadEnemyConn = new int[5];
        int enemySize = 0;
        int[] specialConn = new int[5];
        while (!locs.empty()) {
            loc = (Loc)locs.pop();
            ++eyeSize;
            if (goban.getColor(loc) == opponent) {
                Chain chain = goban.getChain(loc);
                if (enemyChain != null && enemyChain != chain) {
                    return true;
                }
                enemyChain = chain;
                int n = Scorer.countNeighbors(goban, loc, opponent);
                deadEnemyConn[n] = deadEnemyConn[n] + 1;
                ++enemySize;
            } else {
                int emptyNeighbors;
                int n = emptyNeighbors = Scorer.countNeighbors(goban, loc, 2);
                specialConn[n] = specialConn[n] + 1;
                if (emptyNeighbors >= 3) {
                    return true;
                }
                if (Scorer.countNeighbors(goban, loc, opponent) == 0) {
                    emptyAwayFromEnemy = true;
                }
            }
            Iterator<Loc> neighbors = loc.neighbors(goban.size);
            while (neighbors.hasNext()) {
                Loc neighbor = neighbors.next();
                if (!goban.get(neighbor, 4) || goban.get(neighbor, 9)) continue;
                locs.push(neighbor);
                goban.set(neighbor, 9, true);
            }
        }
        if (enemySize > 0 && emptyAwayFromEnemy) {
            return true;
        }
        switch (enemySize) {
            case 0: {
                return eyeSize == 3 || eyeSize == 4 && specialConn[2] != 4 || eyeSize > 4;
            }
            case 1: 
            case 2: 
            case 3: {
                return false;
            }
            case 4: {
                return deadEnemyConn[1] == 2 && deadEnemyConn[2] == 2;
            }
            case 5: {
                return deadEnemyConn[4] != 1 && deadEnemyConn[1] != 1;
            }
            case 6: {
                return deadEnemyConn[1] != 2 || deadEnemyConn[2] != 3 || deadEnemyConn[4] != 1;
            }
            case 7: {
                return deadEnemyConn[2] != 6 || deadEnemyConn[4] != 1;
            }
        }
        return true;
    }

    private static int countNeighbors(ScoringGoban goban, Loc loc, int color) {
        int count = 0;
        Iterator<Loc> neighbors = loc.neighbors(goban.size);
        while (neighbors.hasNext()) {
            Loc neighbor = neighbors.next();
            if (goban.getColor(neighbor) != color || !goban.get(neighbor, 4)) continue;
            ++count;
        }
        return count;
    }

    public final float getScore(int color) {
        return this.scores[color];
    }

    public Collection<Loc> getTerritoryLocs(int color) {
        return Collections.unmodifiableCollection(this.territory(color));
    }

    private ArrayList<Loc> territory(int color) {
        return color == 0 ? this.blackTerritory : this.whiteTerritory;
    }
}
