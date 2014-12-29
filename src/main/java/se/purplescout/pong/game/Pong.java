package se.purplescout.pong.game;

import se.purplescout.pong.game.collision.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import static se.purplescout.pong.game.SomeoneScoredListener.PLAYER.LEFT;
import static se.purplescout.pong.game.SomeoneScoredListener.PLAYER.RIGHT;

public class Pong implements Runnable {

    public static final int AREA_HEIGHT = 495;
    public static final int AREA_WIDTH = 800;
    public static final int MINIMUM_MOVABLE_EXTENT = 6;
    private static final Vector minimumBallSpeed = new Vector(5, 5);
    private static final double PADDLE_MAX_SPEED = 5;

    public static int MAX_MOVEMENT_PER_STEP;

    static {
        assert MINIMUM_MOVABLE_EXTENT % 2 == 0;
        MAX_MOVEMENT_PER_STEP = MINIMUM_MOVABLE_EXTENT / 2;
    }

    public static enum BALL_START_X_DIRECTION {

        TO_RIGHT, TO_LEFT, RANDOM;
    }
    private final Object PAUSE_MONITOR = new Object();

    private int millisecondsToSleepBetweenFrames;
    private volatile boolean shouldStop = false;
    private volatile boolean pauseThread = false;
    private final Ball ball;
    private final Paddle leftPaddle;
    private final Paddle rightPaddle;
    private boolean resetBallOnScore = true;
    private SomethingMovedListener onSomethingMovedListener;
    private SomeoneScoredListener someoneScoredListener;
    private long leftPlayerLastDecisionTime, rightPlayerLastDecisionTime;
    private long leftPlayerTotalDecisionTime, rightPlayerTotalDecisionTime;
    private long leftPlayerStartedDecidingAt, rightPlayerStartedDecidingAt;
    private SomeoneScoredListener.PLAYER currentlyDeciding;

    public Pong(int millisecondsToSleepBetweenFrames, Paddle leftPaddle, Paddle rightPaddle) {
        this.millisecondsToSleepBetweenFrames = millisecondsToSleepBetweenFrames;
        this.leftPaddle = leftPaddle;
        this.rightPaddle = rightPaddle;

        int paddleWidth = 16;
        int paddleHeight = 70;
        int ballRadius = 8;

        ball = new Ball(new Vector(), new Circle(new Vector(AREA_WIDTH / 2 - ballRadius, AREA_HEIGHT / 2 - ballRadius), ballRadius));
        leftPaddle.setBoundingBox(new Rectangle(new Vector(30, (AREA_HEIGHT - paddleWidth) / 2), paddleWidth, paddleHeight));
        rightPaddle.setBoundingBox(new Rectangle(new Vector(AREA_WIDTH - 30 - paddleWidth, (AREA_HEIGHT - paddleWidth) / 2), paddleWidth, paddleHeight));
    }

    public void setMillisecondsToSleepBetweenFrames(int millisecondsToSleepBetweenFrames) {
        this.millisecondsToSleepBetweenFrames = millisecondsToSleepBetweenFrames;
    }

    public void resetBall(BALL_START_X_DIRECTION startDirection) {
        ball.setAbsoluteXPosition(AREA_WIDTH / 2 - 5);
        ball.setAbsoluteYPosition(AREA_HEIGHT / 2 - 5);

        setBallStartVelocity();

        if (Math.random() < 0.5) {
            ball.inverseMovementY();
        }

        if (startDirection == BALL_START_X_DIRECTION.RANDOM) {
            if (Math.random() < 0.5) {
                ball.inverseMovementX();
            }
        } else if (startDirection == BALL_START_X_DIRECTION.TO_LEFT) {
            // The ball velocity always starts positive in both x and y, meaning
            // that it always travels right initially. So we inverse the
            // x movement here
            ball.inverseMovementX();
        } else if (startDirection == BALL_START_X_DIRECTION.TO_RIGHT) {
            // The ball velocity always starts positive in both x and y, meaning
            // that it always travels right initially. So, we don't need to do
            // anything here
        } else {
            throw new RuntimeException("Unknown ball start direction " + startDirection);
        }
    }

    public void setBallStartVelocity() {
        ball.movementVector().setX(minimumBallSpeed.getX() + Math.random() * 4);
        ball.movementVector().setY(minimumBallSpeed.getY() + Math.random() * 5);

        Vector newVector = VectorAlgebra.sameDirectionButNewLength(ball.movementVector(), minimumBallSpeed.length());
        ball.movementVector().setX(newVector.getX());
        ball.movementVector().setY(newVector.getY());
    }

    public void setResetBallOnScore(boolean resetBallOnScore) {
        this.resetBallOnScore = resetBallOnScore;
    }

    public void setOnSomethingMovedListener(SomethingMovedListener onSomethingMovedListener) {
        this.onSomethingMovedListener = onSomethingMovedListener;
    }

    public void setSomeoneScoredListener(SomeoneScoredListener someoneScoredListener) {
        this.someoneScoredListener = someoneScoredListener;
    }

    public Ball getBall() {
        return ball;
    }

    public Paddle getLeftPaddle() {
        return leftPaddle;
    }

    public Paddle getRightPaddle() {
        return rightPaddle;
    }

    public final void requestStop() {
        shouldStop = true;
    }

    private void checkForPaused() {
        synchronized (PAUSE_MONITOR) {
            while (pauseThread) {
                try {
                    PAUSE_MONITOR.wait();
                } catch (Exception e) {
                }
            }
        }
    }

    public final void pauseThread() {
        pauseThread = true;
    }

    public final void resumeThread() {
        synchronized (PAUSE_MONITOR) {
            pauseThread = false;
            PAUSE_MONITOR.notify();
        }
    }

    public final boolean isPaused() {
        return pauseThread;
    }

    @Override
    public void run() {
        if (resetBallOnScore) {
            resetBall(BALL_START_X_DIRECTION.RANDOM);
        }

        while (!shouldStop) {
            checkForPaused();
            playRound();
        }
    }

    public void playRound() {
        long start = System.currentTimeMillis();
        tickPaddles();

        movePaddle(leftPaddle);
        movePaddle(rightPaddle);
        moveBall();

        sleepForFps(start);
    }

    private void sleepForFps(long start) {
        if (millisecondsToSleepBetweenFrames > 0) {
            try {
                long frameDuration = System.currentTimeMillis() - start;
                long toSleep = millisecondsToSleepBetweenFrames - frameDuration;
                if (toSleep > 0) {
                    Thread.sleep(toSleep);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Pong.class.getName()).log(Level.SEVERE, "Pong interrupted", ex);
            }
        }
    }

    private void tickPaddles() {
        Size boardSize = new Size(AREA_WIDTH, AREA_HEIGHT);

        GameRound leftGameRound = new GameRound(ball, rightPaddle, boardSize);
        currentlyDeciding = LEFT;
        leftPlayerStartedDecidingAt = System.currentTimeMillis();
        leftPaddle.decideWhatToDoThisTick(leftGameRound);
        leftPlayerLastDecisionTime = System.currentTimeMillis() - leftPlayerStartedDecidingAt;
        leftPlayerTotalDecisionTime += leftPlayerLastDecisionTime;

        GameRound rightGameRound = new GameRound(ball, leftPaddle, boardSize);
        currentlyDeciding = RIGHT;
        rightPlayerStartedDecidingAt = System.currentTimeMillis();
        rightPaddle.decideWhatToDoThisTick(rightGameRound);
        rightPlayerLastDecisionTime = System.currentTimeMillis() - rightPlayerStartedDecidingAt;
        rightPlayerTotalDecisionTime += rightPlayerLastDecisionTime;

        currentlyDeciding = null;
    }

    public long getLeftPlayerLastDecisionTime() {
        return leftPlayerLastDecisionTime;
    }

    public long getRightPlayerLastDecisionTime() {
        return rightPlayerLastDecisionTime;
    }

    public long getLeftPlayerStartedDecidingAt() {
        return leftPlayerStartedDecidingAt;
    }

    public long getRightPlayerStartedDecidingAt() {
        return rightPlayerStartedDecidingAt;
    }

    public long getLeftPlayerTotalDecisionTime() {
        return leftPlayerTotalDecisionTime;
    }

    public long getRightPlayerTotalDecisionTime() {
        return rightPlayerTotalDecisionTime;
    }

    public SomeoneScoredListener.PLAYER getCurrentlyDeciding() {
        return currentlyDeciding;
    }

    private void movePaddle(Paddle paddle) {
        if (paddle.getPloxxPos() == null) {
            return;
        }

        Vector paddleCenter = new Vector(paddle.getBoundingBox().getCenterX(), paddle.getBoundingBox().getCenterY());
        Vector velocity = new Vector(0, paddle.getPloxxPos().getY() - paddleCenter.getY());
        if (velocity.length() > PADDLE_MAX_SPEED) {
            velocity = VectorAlgebra.sameDirectionButNewLength(velocity, PADDLE_MAX_SPEED);
        }
        paddle.setMovementVector(velocity);

        double length = Math.sqrt(velocity.getX() * velocity.getX() + velocity.getY() * velocity.getY());
        int steps = (int) Math.ceil(length / MAX_MOVEMENT_PER_STEP);

        for (int i = 0; i < steps; i++) {

            doMove(paddle, i, length, velocity);

            if (paddle.getBoundingBox().getY() < 0) {
                paddle.setAbsoluteYPosition(0);
            } else if (paddle.getBoundingBox().getY() + paddle.getBoundingBox().getHeight() > AREA_HEIGHT) {
                paddle.setAbsoluteYPosition(AREA_HEIGHT - (int) paddle.getBoundingBox().getHeight());
            }

            if (paddle.getBoundingBox().getX() < 0) {
                paddle.setAbsoluteXPosition(0);
            } else if (paddle.getBoundingBox().getX() > AREA_WIDTH) {
                paddle.setAbsoluteXPosition(AREA_WIDTH);
            }

            fireSomethingMovedEvent();
        }
    }

    private void doMove(Movable movable, int i, double length, Vector vector) {
        double distanceToMove;
        if (length < MAX_MOVEMENT_PER_STEP) {
            distanceToMove = length;
        } else if (i * MAX_MOVEMENT_PER_STEP > length) {
            // last step is not a full step
            distanceToMove = length - (i * MAX_MOVEMENT_PER_STEP);
        } else {
            // Move a full step
            distanceToMove = MAX_MOVEMENT_PER_STEP;
        }
        Vector toMove = VectorAlgebra.sameDirectionButNewLength(vector, distanceToMove);
        movable.move(toMove);
    }

    private void fireSomethingMovedEvent() {
        if (onSomethingMovedListener != null) {
            onSomethingMovedListener.onSomethingMoved();
        }
    }

    private void moveBall() {
        Vector vector = ball.movementVector();
        double length = Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY());
        int steps = (int) Math.ceil(length / MAX_MOVEMENT_PER_STEP);

        for (int i = 0; i < steps; i++) {

            doMove(ball, i, length, vector);

            if (ball.getBoundingBox().getY() < 0) {
                ball.setAbsoluteYPosition(0);
                ball.inverseMovementY();
            } else if (ball.getBoundingBox().getY() + ball.getBoundingBox().getHeight() > AREA_HEIGHT) {
                ball.setAbsoluteYPosition(AREA_HEIGHT - (int) ball.getBoundingBox().getHeight());
                ball.inverseMovementY();
            }

            if (ball.getBoundingBox().getX() < 0) {
                // Point to right player
                if (someoneScoredListener != null) {
                    someoneScoredListener.someoneScored(SomeoneScoredListener.PLAYER.RIGHT, rightPaddle.getTeamName(), rightPaddle);
                }

                if (resetBallOnScore) {
                    resetBall(BALL_START_X_DIRECTION.RANDOM);
                }
                return;
            } else if (ball.getBoundingBox().getX() + ball.getBoundingBox().getWidth() > AREA_WIDTH) {
                // Point to left player
                if (someoneScoredListener != null) {
                    someoneScoredListener.someoneScored(SomeoneScoredListener.PLAYER.LEFT, leftPaddle.getTeamName(), leftPaddle);
                }

                if (resetBallOnScore) {
                    resetBall(BALL_START_X_DIRECTION.RANDOM);
                }
                return;
            }

            if (CollisionDetection.intersects(leftPaddle.getBoundingBox(), ball.getShapeAndPosition())) {
                bounceBallOffPaddle(leftPaddle);
                ball.setAbsoluteXPosition(leftPaddle.getBoundingBox().getX() + leftPaddle.getBoundingBox().getWidth() + 1);
            } else if (CollisionDetection.intersects(rightPaddle.getBoundingBox(), ball.getShapeAndPosition())) {
                bounceBallOffPaddle(rightPaddle);
                ball.setAbsoluteXPosition(rightPaddle.getBoundingBox().getX() - ball.getBoundingBox().getWidth() - 1);
            }

            fireSomethingMovedEvent();
        }
    }

    private void bounceBallOffPaddle(Paddle paddle) {
        //ball.movementVector().rotate(0.0174532925 * (1 + Math.random() * 4));
        //ball.inverseMovementX();

        double oldSpeed = ball.movementVector().length();

        // Set the ball to move along the x-axis in the same direction it did before
        ball.movementVector().setY(0);
        ball.movementVector().setLength(oldSpeed);

        // Set the ball to move in the opposite direction
        ball.inverseMovementX();

        // Rotate the ball's velocity based on where on the paddle it hit.
        double impactHeight = ball.getBoundingBox().getCenterY() - paddle.getBoundingBox().getCenterY();
        double percentage = impactHeight / (paddle.getBoundingBox().getHeight() / 2);
        double reflectionAngle = (Math.PI / 3) * percentage;
        if (ball.movementVector().getX() < 0) {
            reflectionAngle *= -1;
        }
        ball.movementVector().rotate(reflectionAngle);

        accelerateBall();
    }

    private void accelerateBall() {
        Vector ballMovement = VectorAlgebra.sameDirectionButNewLength(ball.movementVector(), ball.movementVector().length() * 1.1);
        ball.movementVector().setX(ballMovement.getX());
        ball.movementVector().setY(ballMovement.getY());

        if (ball.movementVector().length() > AREA_WIDTH * 0.9) {
            System.out.println("Ball is moving too fast");
            resetBall(BALL_START_X_DIRECTION.RANDOM);
        }
    }
}
