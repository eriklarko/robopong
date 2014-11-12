package se.purplescout.pong.server.highscore;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import se.purplescout.pong.game.Paddle;
import se.purplescout.pong.game.Pong;
import se.purplescout.pong.game.SomeoneScoredListener;

public class HighScore {

    private final Map<String, Paddle> paddles = new HashMap<>();
    private final NewScoreListener newScoreListener;

    public HighScore(NewScoreListener newScoreListener) {
        this.newScoreListener = newScoreListener;
    }

    public synchronized void addPaddle(Paddle p) {
        paddles.put(p.getTeamName(), p);
        updateHighscore();
    }

    public synchronized void removePaddle(Paddle p) {
        paddles.remove(p.getTeamName());
        updateHighscore();
    }

    private void updateHighscore() {
        Map<Paddle, Integer> unsortedScores = calculateScores(paddles.values());
        Map<Paddle, Integer> sortedScores = sort(unsortedScores);

        newScoreListener.newScores(sortedScores);
    }

    public Map<Paddle, Integer> calculateScores(Iterable<Paddle> paddles) {
        Set<Pair<Paddle, Paddle>> fights = new HashSet<>();
        for (Paddle p1 : paddles) {
            for (Paddle p2 : paddles) {
                if (p1 != p2) {
                    fights.add(new Pair<>(p1, p2));
                }
            }
        }

        Map<Paddle, Integer> scores = new HashMap<>();
        for (final Pair<Paddle, Paddle> fight : fights) {

            Integer aScore = scores.get(fight.a);
            Integer bScore = scores.get(fight.b);

            if (aScore == null) {
                aScore = 0;
            }
            if (bScore == null) {
                bScore = 0;
            }

            Pair<Integer, Integer> fightScore;
            System.out.println("Fighting " + fight.a.getTeamName() + " againts " + fight.b.getTeamName());
            fightScore = fight(fight.a, fight.b);
            System.out.println("Done fighting " + fight.a.getTeamName() + " againts " + fight.b.getTeamName());

            scores.put(fight.a, aScore + fightScore.a);
            scores.put(fight.b, bScore + fightScore.b);
        }
        return scores;
    }

    public Pair<Integer, Integer> fight(Paddle t, Paddle t1) {
        //final ThreadLocal<Integer> score1 = new ThreadLocal<>();
        final AtomicInteger score1 = new AtomicInteger(0);
        score1.set(0);
        //final ThreadLocal<Integer> score2 = new ThreadLocal<>();
        final AtomicInteger score2 = new AtomicInteger(0);
        score2.set(0);
        final ThreadLocal<Pong.BALL_START_X_DIRECTION> ballStartDirection = new ThreadLocal<>();
        ballStartDirection.set(Pong.BALL_START_X_DIRECTION.RANDOM);

        final Pong pong = new Pong(0, t, t1);
        pong.setResetBallOnScore(false);
        pong.setSomeoneScoredListener(new SomeoneScoredListener() {

            @Override
            public void someoneScored(SomeoneScoredListener.PLAYER player, String name, Paddle paddle) {
                //System.out.println("Score! Score 1: " + score1.get()+ ", score 2: " + score2.get());
                if (player == SomeoneScoredListener.PLAYER.LEFT) {
                    score1.set(score1.get() + 1);
                    ballStartDirection.set(Pong.BALL_START_X_DIRECTION.TO_LEFT);
                } else {
                    score2.set(score2.get() + 1);
                    ballStartDirection.set(Pong.BALL_START_X_DIRECTION.TO_RIGHT);
                }

                pong.resetBall(ballStartDirection.get());
            }
        });

        pong.resetBall(ballStartDirection.get());

        final ThreadLocal<Pair<Integer, Integer>> fightScore = new ThreadLocal<>();
        Thread fightThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Score1: " + score1 + ", " + score1.get());
                try {
                    while (score1.get() + score2.get() < 2000) {
                        pong.playRound();
                    }
                } catch (ThreadDeath e) {
                    System.out.println("Thread died..");
                }
            }
        });
        fightThread.setName("Fighting " + t.getTeamName() + " against " + t1.getTeamName());
        fightThread.start();

        while (fightThread.isAlive()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }

            long startedDeciding;
            SomeoneScoredListener.PLAYER player = pong.getCurrentlyDeciding();
            if (player == SomeoneScoredListener.PLAYER.LEFT) {
                startedDeciding = pong.getLeftPlayerStartedDecidingAt();
            } else {
                startedDeciding = pong.getRightPlayerStartedDecidingAt();
            }
            System.out.println("Current time: " + System.currentTimeMillis() + ", started: " + startedDeciding);
            if (System.currentTimeMillis() > startedDeciding + 4000) {
                if (player == SomeoneScoredListener.PLAYER.LEFT) {
                    System.out.println("player " + t.getTeamName() + " exceeded time limit for decision. Making player LOOOSE.");
                    score1.set(0);
                } else {
                    System.out.println("player " + t1.getTeamName() + " exceeded time limit for decision. Making player LOOOSE.");
                    score2.set(0);
                }


                fightThread.interrupt();
                fightThread.stop();
            }
        }

        long p1d = pong.getLeftPlayerTotalDecisionTime();
        long p2d = pong.getRightPlayerTotalDecisionTime();
        if (Math.min(p1d, p2d) * 5 < Math.max(p1d, p2d)) {
            if (pong.getLeftPlayerTotalDecisionTime() < pong.getRightPlayerTotalDecisionTime()) {
                System.out.println(t.getTeamName() + " was much faster than " + t1.getTeamName() + ". Bad");
                score2.set((int) (score2.get() * .9));
            } else {
                System.out.println(t1.getTeamName() + " was much faster than " + t.getTeamName() + ". Bad");
                score1.set((int) (score1.get() * .9));
            }
        }

        return new Pair<>(score1.get(), score2.get());
    }

    public Map<Paddle, Integer> sort(final Map<Paddle, Integer> toSort) {
        TreeMap<Paddle, Integer> sorted = new TreeMap<>(new Comparator<Paddle>() {

            @Override
            public int compare(Paddle o1, Paddle o2) {
                Integer score1 = toSort.get(o1);
                Integer score2 = toSort.get(o2);

                return score2 - score1;
            }
        });
        sorted.putAll(toSort);

        return sorted;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Epox");
        final JPanel pp = new JPanel();
        HighScore highScore1 = new HighScore(new NewScoreListener() {

            @Override
            public void newScores(Map<Paddle, Integer> scores) {
                System.out.println(scores);
                //JPanel p = HighScoreGui.generatePanelFromScore(scores, null);
                pp.removeAll();
                //pp.add(p);
            }
        });
        frame.add(pp);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        /*highScore1.addPaddle(new NopPaddle("A"));
        System.out.println("------------------------------");
        highScore1.addPaddle(new NopPaddle("B"));
        System.out.println("------------------------------");
        highScore1.addPaddle(new NopPaddle("C"));*/
    }

    public static class Pair<A, B> {

        final A a;
        final B b;

        public Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash += Objects.hashCode(this.a);
            hash += Objects.hashCode(this.b);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Pair<?, ?> other = (Pair<?, ?>) obj;

            if (other.a == this.a && other.b == this.b) {
                return true;
            }

            if (other.a == this.b && other.b == this.a) {
                return true;
            }

            return false;
        }
    }
}
