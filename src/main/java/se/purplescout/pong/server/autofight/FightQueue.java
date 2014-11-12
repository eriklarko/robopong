package se.purplescout.pong.server.autofight;

import se.purplescout.pong.game.Paddle;
import se.purplescout.pong.gui.client.paddle.classselector.PaddleCache;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FightQueue {

    private final FightRoundDoneListener fightRoundDoneListener;
    private final Map<String, Class<Paddle>> paddles = new HashMap<>();
    private final BlockingDeque<Set<Class<Paddle>>> queuedFightRounds = new LinkedBlockingDeque<>();
    private final Thread fightRoundConsumer = new Thread(new Runnable() {

        private volatile boolean stop;

        public void stop() {
            stop = true;
        }

        @Override
        public void run() {
            while (!stop) {
                try {
                    startFightRound(queuedFightRounds.takeFirst());
                } catch (InterruptedException e) {
                    System.out.println("Fight round consumer was interrupted! No moar fajts will häppen");
                    e.printStackTrace();
                }
            }
        }
    }, "Fight round consumer");

    public FightQueue(FightRoundDoneListener fightRoundDoneListener) {
        this.fightRoundDoneListener = fightRoundDoneListener;
    }

    public synchronized void addPaddle(Class<Paddle> paddleClass) {
        String teamName = PaddleCache.getTeamName(paddleClass);
        if (teamName == null) {
            System.out.println("Tried to add unregistered paddle to fight: " + paddleClass);
            return;
        }

        paddles.put(teamName, paddleClass);
        replaceQueuedFightRound();
    }

    private void replaceQueuedFightRound() {
        if (!queuedFightRounds.isEmpty()) {
            queuedFightRounds.clear();
        }

        queuedFightRounds.add(new HashSet<>(paddles.values()));
        if (!fightRoundConsumer.isAlive()) {
            fightRoundConsumer.start();
        }
    }

    public synchronized void removePaddle(Class<? extends Paddle> paddleClass) {
        String teamName = PaddleCache.getTeamName(paddleClass);
        if (teamName == null) {
            System.out.println("Tried to remove unregistered paddle from fight: " + paddleClass);
            return;
        }

        Class<Paddle> value = paddles.remove(teamName);
        if (value != null) { // Something was removed from the map
            replaceQueuedFightRound();
        }
    }

    private void startFightRound(Set<Class<Paddle>> paddlesToFight) {
        System.out.println("========= Starting fight round with " + paddlesToFight.size() + " paddles =========");
        List<AutoFight> fights = getFightPairs(paddlesToFight).stream().map(this::makeFightFromPair)
                .filter((a) -> a.isPresent())
                .map((a) -> a.get())
                .collect(Collectors.toList());

        try {
            ForkJoinPool fightRoundExecutor = new ForkJoinPool();
            fightRoundExecutor.invokeAll(fights);
            fightRoundExecutor.shutdown();
            boolean allDone = fightRoundExecutor.awaitTermination(30, TimeUnit.MINUTES);

            if (!allDone) {
                // TODO: Hantera !allDone
                System.out.println("A fight round timed out!");
            } else if (allDone && fightRoundDoneListener != null) {
                fightRoundDoneListener.fightRoundDone(fights);
                System.out.println("========= FIGHT ROUND DONE =========");
            }
        } catch (InterruptedException e) {
            System.out.println("Some fight round was interrupted");
            e.printStackTrace();
        }
    }

    private Set<Pair<Class<Paddle>, Class<Paddle>>> getFightPairs(Iterable<Class<Paddle>> paddlesInFight) {
        Set<Pair<Class<Paddle>, Class<Paddle>>> fights = new HashSet<>();
        for (Class<Paddle> p1 : paddlesInFight) {
            for (Class<Paddle> p2 : paddlesInFight) {
                if (p1 != p2) {
                    fights.add(new Pair<Class<Paddle>, Class<Paddle>>(p1, p2));
                }
            }
        }
        return fights;
    }

    private Optional<AutoFight> makeFightFromPair(Pair<Class<Paddle>, Class<Paddle>> pair) {
        Paddle left = PaddleCache.getInstance(pair.a);
        Paddle right = PaddleCache.getInstance(pair.b);
        if (left == null || right == null) {
            System.out.println("Some fight pair contained nulls :/");
            return Optional.empty();
        }
        return Optional.of(new AutoFight(left, right));
    }
}
