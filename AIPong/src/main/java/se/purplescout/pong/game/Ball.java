package se.purplescout.pong.game;

import se.purplescout.pong.game.collision.Circle;
import se.purplescout.pong.game.collision.Rectangle;
import se.purplescout.pong.game.collision.Vector;

public class Ball extends Movable {

    private final Vector movementVector;
    private final Circle shapeAndPosition;

    Ball(Vector movementVector, Circle shape) {
        this.movementVector = movementVector;
        this.shapeAndPosition = shape;
    }

    Circle getShapeAndPosition() {
        return shapeAndPosition;
    }

    void inverseMovementX() {
        movementVector.setLocation(-1 * movementVector.getX(), movementVector.getY());
    }

    void inverseMovementY() {
        movementVector.setLocation(movementVector.getX(), -1 * movementVector.getY());
    }

    Vector movementVector() {
        return movementVector;
    }

    void move(Vector toMove) {
        Vector position = shapeAndPosition.getPos();

        position.setX(position.getX() + toMove.getX());
        position.setY(position.getY() + toMove.getY());
    }

    void setAbsoluteXPosition(double x) {
        shapeAndPosition.getPos().setX(x + shapeAndPosition.getRadius());
    }

    void setAbsoluteYPosition(double y) {
        shapeAndPosition.getPos().setY(y + shapeAndPosition.getRadius());
    }

    public Rectangle getBoundingBox() {
        Vector topLeft = new Vector(shapeAndPosition.getPos().getX() - shapeAndPosition.getRadius(), shapeAndPosition.getPos().getY() - shapeAndPosition.getRadius());
        return new Rectangle(topLeft, shapeAndPosition.getRadius() * 2, shapeAndPosition.getRadius() * 2);
    }
}
