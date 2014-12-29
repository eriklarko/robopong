package se.purplescout.pong.competition.headless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.purplescout.pong.game.Paddle;
import se.purplescout.pong.game.Pong;
import se.purplescout.pong.game.SomeoneScoredListener;

import java.util.concurrent.Callable;
import static se.purplescout.pong.game.SomeoneScoredListener.PLAYER.LEFT;
import static se.purplescout.pong.game.SomeoneScoredListener.PLAYER.RIGHT;

public class AutoFight implements Runnable, Callable<AutoFight> {

    public static enum STATE {

        BEFORE_FIGHT, FIGHTING, DONE_FIGHTING;
    }

    public static enum RESULT {

        UNDETERMINED,
        FIGHT_TOOK_TOO_LONG,
        LEFT_PADDLE_TOOK_TOO_LONG, LEFT_PADDLE_THREW_EXCEPTION,
        RIGHT_PADDLE_TOOK_TOO_LONG, RIGHT_PADDLE_THREW_EXCEPTION,
        UNKNOWN_ERROR,
        SUCCESS
    }

    private static final Logger LOG = LoggerFactory.getLogger(AutoFight.class);
    private final Pong pong;
    private final AutoFightWatcher watcher = new AutoFightWatcher(this);
    private int leftScore, rightScore;
    private Pong.BALL_START_X_DIRECTION ballStartDirection;
    private STATE state = STATE.BEFORE_FIGHT;
    private RESULT result = RESULT.UNDETERMINED;

    public AutoFight(Paddle left, Paddle right) {
        pong = new Pong(0, left, right);
        pong.setResetBallOnScore(false);

        ballStartDirection = Pong.BALL_START_X_DIRECTION.RANDOM;
        pong.setSomeoneScoredListener(new SomeoneScoredListener() {

            @Override
            public void someoneScored(SomeoneScoredListener.PLAYER player, String name, Paddle paddle) {
                if (player == SomeoneScoredListener.PLAYER.LEFT) {
                    leftScore++;
                    //ballStartDirection = Pong.BALL_START_X_DIRECTION.TO_LEFT;
                } else {
                    rightScore++;
                    //ballStartDirection = Pong.BALL_START_X_DIRECTION.TO_RIGHT;
                }

                pong.resetBall(ballStartDirection);
            }
        });
    }

    public Paddle getLeftPaddle() {
        return pong.getLeftPaddle();
    }

    public Paddle getRightPaddle() {
        return pong.getRightPaddle();
    }

    Pong getPong() {
        return pong;
    }

    Thread getCurrentThread() {
        return Thread.currentThread();
    }

    public RESULT getResult() {
        return result;
    }

    void setFightResult(RESULT result) {
        if (this.result != RESULT.UNDETERMINED) {
            LOG.debug(Thread.currentThread().getName() + " - ALREADY HAD RESULT: " + this.result);
            return;
        }

        LOG.debug(Thread.currentThread().getName() + " - Results are in: " + result);

        this.result = result;
        switch (result) {
            case FIGHT_TOOK_TOO_LONG:
                leftScore = rightScore = 0;
                break;
            case LEFT_PADDLE_TOOK_TOO_LONG:
            case LEFT_PADDLE_THREW_EXCEPTION:
                leftScore = 0;
                break;
            case RIGHT_PADDLE_TOOK_TOO_LONG:
            case RIGHT_PADDLE_THREW_EXCEPTION:
                rightScore = 0;
                break;
        }
    }

    public int getLeftScore() {
        return leftScore;
    }

    public int getRightScore() {
        return rightScore;
    }

    public void startWatcher() {
        watcher.start();
    }

    public STATE getState() {
        return state;
    }

    void setState(STATE state) {
        this.state = state;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        startWatcher();
        pong.resetBall(ballStartDirection);
        while (leftScore + rightScore < 2000) {
            try {
                if (System.currentTimeMillis() - start > 4000) {
                    // TODO: Make the slowest paddle loose more points
                    setFightResult(RESULT.FIGHT_TOOK_TOO_LONG);
                    break;
                }
                pong.playRound();
            } catch (Exception ex) {
                if (pong.getCurrentlyDeciding() == LEFT) {
                    setFightResult(RESULT.LEFT_PADDLE_THREW_EXCEPTION);
                } else if (pong.getCurrentlyDeciding() == RIGHT) {
                    setFightResult(RESULT.RIGHT_PADDLE_THREW_EXCEPTION);
                } else {
                    setFightResult(RESULT.UNKNOWN_ERROR);
                    LOG.error("Autofight threw exception but no one was deciding at the time, no one is penalized", ex);
                }

                break;
            }
            state = STATE.FIGHTING;
        }
        state = STATE.DONE_FIGHTING;

        if (result == RESULT.UNDETERMINED) {
            result = RESULT.SUCCESS;
        }
    }

    @Override
    public AutoFight call() throws Exception {
        run();
        return this;
    }

    @Override
    public String toString() {
        if (pong == null) {
            return super.toString();
        }
        return pong.getLeftPaddle().getTeamName() + " vs " + pong.getRightPaddle().getTeamName();
    }
}
