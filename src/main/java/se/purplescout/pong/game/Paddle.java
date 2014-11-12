package se.purplescout.pong.game;

import se.purplescout.pong.collision.Rectangle;
import se.purplescout.pong.collision.Vector;

public abstract class Paddle extends Movable {

    private Vector movementVector;
    private Rectangle boundingBox;
    private Vector ploxxPos;

    protected Paddle() {
        this.movementVector = new Vector();
    }

    /**
     * Invoked several times per second. Don't put your heavy lifting here.
     *
     * Invoked just before the paddle is to move.
     *
     * @param context Information about the game round before anything moves
     */
    public abstract void decideWhatToDoThisTick(GameRound context);

    public abstract String getTeamName();

    /**
     * @return A copy of the rectangle representing the paddle. X and Y are
     * absolute positions.
     */
    public final Rectangle getBoundingBox() {
        return new Rectangle(boundingBox);
    }

    void setBoundingBox(Rectangle boundingBox) {
        this.boundingBox = boundingBox;
    }

    /**
     * @return The paddle's velocity vector. (3,5) means that the paddle travels
     * 3 pixels to the right and 5 pixels down each tick. (-3, -5) means that
     * the paddle travels 3 pixels to the left and 5 pixels up each tick. The
     * x value is always zero
     */
    public final Vector getMovementVector() {
        return movementVector.clone();
    }

    void setMovementVector(Vector velocity) {
        movementVector = velocity;
    }

    void setAbsoluteXPosition(int x) {
        boundingBox.getPos().setX(x);
    }

    void setAbsoluteYPosition(int y) {
        boundingBox.getPos().setY(y);
    }

    Vector getPloxxPos() {
        return ploxxPos;
    }

    /**
     * Invoked when you want to move the paddle. (0,0) is the top left corner of the game board.
     *
     * The paddle can only move a
     * short distance every tick. If the position you ploxx move to if further
     * away than the short distance the paddle will continue the movement next
     * tick.
     *
     * @param y The y-coordinate you want your examplepaddles center at
     */
    public final void ploxxMoveMyCenterTo(double y) {
        ploxxMoveMyCenterTo(new Vector(getBoundingBox().getX(), y));
    }

    private final void ploxxMoveMyCenterTo(Vector center) {
        center.setX(0);
        ploxxPos = center;
    }

    /**
     * @return True if the paddle is not at it's ploxx pos.
     */
    public final boolean willMoveThisTick() {
        if (ploxxPos == null) {
            return false;
        } else {
            return Math.abs(boundingBox.getCenterY() - ploxxPos.getY()) < 0.001;
        }
    }

    @Override
    void move(Vector toMove) {
        Vector position = boundingBox.getPos();

        position.setX(position.getX() + toMove.getX());
        position.setY(position.getY() + toMove.getY());
    }

    @Override
    public final String toString() {
        return getTeamName();
    }
}
