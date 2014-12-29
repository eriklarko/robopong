package se.purplescout.pong.game.collision;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Created by eriklark on 2014-10-07.
 */
public class CollisionDetection {

    // clamp(value, min, max) - limits value to the range min..max
    private static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }

        return value;
    }

    public static boolean intersects(Rectangle rectangle, Circle circle) {
        // Find the closest point to the circle within the rectangle
        double closestX = clamp(circle.getX(), rectangle.getX(), rectangle.getX() + rectangle.getWidth());
        double closestY = clamp(circle.getY(), rectangle.getY(), rectangle.getY() + rectangle.getHeight());

        // Calculate the distance between the circle's center and this closest point
        double distanceX = circle.getX() - closestX;
        double distanceY = circle.getY() - closestY;

        // If the distance is less than the circle's radius, an intersection occurs
        double distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);
        return distanceSquared < (circle.getRadius() * circle.getRadius());
    }

    public static boolean intersects(Ellipse2D.Double circle, Rectangle2D.Double rectangle) {
        double radius = circle.getHeight() / 2;

        // Find the closest point to the circle within the rectangle
        double closestX = clamp(circle.getCenterX(), rectangle.getX(), rectangle.getX() + rectangle.getWidth());
        double closestY = clamp(circle.getCenterY(), rectangle.getY(), rectangle.getY() + rectangle.getHeight());

        // Calculate the distance between the circle's center and this closest point
        double distanceX = circle.getCenterX() - closestX;
        double distanceY = circle.getCenterY() - closestY;

        // If the distance is less than the circle's radius, an intersection occurs
        double distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);
        return distanceSquared < (radius * radius);
    }
}
