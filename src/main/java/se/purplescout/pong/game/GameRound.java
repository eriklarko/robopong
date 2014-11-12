package se.purplescout.pong.game;

import se.purplescout.pong.collision.Rectangle;
import se.purplescout.pong.collision.Size;
import se.purplescout.pong.collision.Vector;

import java.awt.Point;

public final class GameRound {

    private final Vector ballPosition;
    private final Vector ballVelocity;

    private final Rectangle otherPaddlePosition;
    private final Vector otherPaddleVelocity;

    private final Size boardSize;

    GameRound(Ball ball, Paddle otherPaddle, Size boardSize) {
        ballPosition = ball.getShapeAndPosition().getPos().clone();
        ballVelocity = ball.movementVector();

        otherPaddlePosition = otherPaddle.getBoundingBox();
        otherPaddleVelocity = otherPaddle.getMovementVector();

        this.boardSize = boardSize;
    }

    /**
     * @return The center x- and y-position of the ball. (0,0) is the top left corner of the game board.
     */
    public Vector getBallPosition() {
        return ballPosition;
    }

    /**
     * @return The ball's velocity vector. (3,5) means that the ball travels
     * 3 pixels to the right and 5 pixels down each tick. (-3, -5) means that
     * the ball travels 3 pixels to the left and 5 pixels up each tick.
     */
    public Vector getBallVelocity() {
        return ballVelocity;
    }

    /**
     * @return The other paddle's position, and size. The x and y are absolute. (0,0) is the top left corner of the game board.
     */
    public Rectangle getOtherPaddlePosition() {
        return otherPaddlePosition;
    }

    /**
     * @return The paddle's velocity vector. (3,5) means that the paddle travels
     * 3 pixels to the right and 5 pixels down each tick. (-3, -5) means that
     * the paddle travels 3 pixels to the left and 5 pixels up each tick. The
     * x value is always zero
     */
    public Vector getOtherPaddleVelocity() {
        return otherPaddleVelocity;
    }

    /**
     * @return The size in pixels of the entire game area.
     */
    public Size getBoardSize() {
        return boardSize;
    }
}
