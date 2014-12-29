package se.purplescout.pong.game;

import se.purplescout.pong.game.collision.Vector;

public final class VectorAlgebra {

    public static Vector sameDirectionButNewLength(Vector vector, double length) {
        Vector normalizedVector = vector.clone().normalize();
        return new Vector(
                normalizedVector.getX() * length,
                normalizedVector.getY() * length
        );
    }

    private VectorAlgebra() {
    }
}
