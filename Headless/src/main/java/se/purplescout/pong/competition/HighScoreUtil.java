package se.purplescout.pong.competition;

import se.purplescout.pong.competition.headless.AutoFight;
import se.purplescout.pong.competition.paddlecache.PaddleCache;
import se.purplescout.pong.game.Paddle;

import java.util.*;

public class HighScoreUtil {

    public static SortedMap<Paddle, Integer> calculateAndSortScoresForEachPaddle(Iterable<AutoFight> fights) {
        Map<Paddle, Integer> unsortedScores = new HashMap<>();
        for (AutoFight fight : fights) {
            Integer leftScore = unsortedScores.get(fight.getLeftPaddle());
            Integer rightScore = unsortedScores.get(fight.getRightPaddle());

            if (leftScore == null) {
                leftScore = 0;
            }
            if (rightScore == null) {
                rightScore = 0;
            }

            leftScore += fight.getLeftScore();
            rightScore += fight.getRightScore();

            unsortedScores.put(fight.getLeftPaddle(), leftScore);
            unsortedScores.put(fight.getRightPaddle(), rightScore);
        }

        SortedMap<Paddle, Integer> sortedScores = new TreeMap<>(new ValueComparator(unsortedScores));
        sortedScores.putAll(unsortedScores);
        return sortedScores;
    }

    private static class ValueComparator implements Comparator<Paddle> {

        Map<Paddle, Integer> base;
        public ValueComparator(Map<Paddle, Integer> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(Paddle a, Paddle b) {

            Integer valueA = base.get(a);
            Integer valueB = base.get(b);
            if (valueA == null) {
                return 1;
            }

            if (valueB == null) {
                return -1;
            }

            int scoreDiff = valueB - valueA;
            if (scoreDiff == 0) {
                return PaddleCache.getTeamName(a.getClass()).compareTo(PaddleCache.getTeamName(b.getClass()));
            }
            return scoreDiff;
        }
    }

    private HighScoreUtil(){}
}
