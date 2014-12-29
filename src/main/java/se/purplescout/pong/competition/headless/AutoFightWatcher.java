package se.purplescout.pong.competition.headless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.purplescout.pong.game.Paddle;
import se.purplescout.pong.game.SomeoneScoredListener;
import se.purplescout.pong.competition.headless.AutoFight.RESULT;

class AutoFightWatcher extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(AutoFightWatcher.class);
    private final AutoFight fightToWatch;

    public AutoFightWatcher(AutoFight fightToWatch) {
        this.fightToWatch = fightToWatch;
    }

    @Override
    public void run() {
        setName("Watcher for " + fightToWatch);
        try {
            while (fightToWatch.getState() != AutoFight.STATE.DONE_FIGHTING) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }

                long startedDeciding = getStartedDeciding();
                if (startedDeciding <= 0) {
                    //System.out.println("Fight not started yet.");
                    continue;
                }

                long currentTime = System.currentTimeMillis();
                long decisionTime = currentTime - startedDeciding;

                //System.out.println(getCurrentlyDecidingPaddle().getTeamName() + " has been deciding what to do for " + decisionTime + " ms");
                if (decisionTime > 4000) {
                    synchronized (fightToWatch.getCurrentThread()) {
                        SomeoneScoredListener.PLAYER currentlyDeciding = getCurrentlyDeciding();
                        if (currentlyDeciding == SomeoneScoredListener.PLAYER.LEFT) {
                            LOG.info("Team " + getLeftPaddle().getTeamName() + " has been deciding for " + decisionTime + " ms. Making player LOOOSE.");
                            fightToWatch.setFightResult(RESULT.LEFT_PADDLE_TOOK_TOO_LONG);
                        } else if (currentlyDeciding == SomeoneScoredListener.PLAYER.RIGHT) {
                            LOG.info("Team " + getRightPaddle().getTeamName() + " has been deciding for " + decisionTime + " ms. Making player LOOOSE.");
                            fightToWatch.setFightResult(RESULT.RIGHT_PADDLE_TOOK_TOO_LONG);
                        } else {
                            LOG.error("Now something is terribly wrong... No one is deciding, but the decision has been going on too long");
                            fightToWatch.setFightResult(RESULT.UNKNOWN_ERROR);
                        }

                        fightToWatch.setState(AutoFight.STATE.DONE_FIGHTING);
                        fightToWatch.getCurrentThread().interrupt();
                    }
                    break;
                }
            }
        } catch (ThreadToWatchSeemsDead ex) {
            LOG.warn("Thread to watch seems dead...");
        }
    }

    private SomeoneScoredListener.PLAYER getCurrentlyDeciding() throws ThreadToWatchSeemsDead {
        try {
            return fightToWatch.getPong().getCurrentlyDeciding();
        } catch (NullPointerException ex) {
            throw new ThreadToWatchSeemsDead();
        }
    }

    private Paddle getLeftPaddle() throws ThreadToWatchSeemsDead {
        try {
            return fightToWatch.getPong().getLeftPaddle();
        } catch (NullPointerException ex) {
            throw new ThreadToWatchSeemsDead();
        }
    }

    private Paddle getRightPaddle() throws ThreadToWatchSeemsDead {
        try {
            return fightToWatch.getPong().getRightPaddle();
        } catch (NullPointerException ex) {
            throw new ThreadToWatchSeemsDead();
        }
    }

    private Paddle getCurrentlyDecidingPaddle() throws ThreadToWatchSeemsDead {
        if (getCurrentlyDeciding() == SomeoneScoredListener.PLAYER.LEFT) {
            return getLeftPaddle();
        } else if (getCurrentlyDeciding() == SomeoneScoredListener.PLAYER.RIGHT) {
            return getRightPaddle();
        } else {
            return null;
        }
    }

    private long getLeftPlayerStartedDecidingAt() throws ThreadToWatchSeemsDead {
        try {
            return fightToWatch.getPong().getLeftPlayerStartedDecidingAt();
        } catch (NullPointerException ex) {
            throw new ThreadToWatchSeemsDead();
        }
    }

    private long getRightPlayerStartedDecidingAt() throws ThreadToWatchSeemsDead {
        try {
            return fightToWatch.getPong().getRightPlayerStartedDecidingAt();
        } catch (NullPointerException ex) {
            throw new ThreadToWatchSeemsDead();
        }
    }

    private long getStartedDeciding() throws ThreadToWatchSeemsDead {
        if (getCurrentlyDeciding() == SomeoneScoredListener.PLAYER.LEFT) {
            return getLeftPlayerStartedDecidingAt();
        } else if (getCurrentlyDeciding() == SomeoneScoredListener.PLAYER.RIGHT) {
            return getRightPlayerStartedDecidingAt();
        } else {
            return -1;
        }
    }

    private static class ThreadToWatchSeemsDead extends Exception {
    }
}
