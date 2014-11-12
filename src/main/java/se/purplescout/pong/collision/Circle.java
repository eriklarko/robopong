package se.purplescout.pong.collision;

public class Circle {

    private Vector pos;
    private double r;

    /**
     * Represents a circle with a position and a radius. Create a new circle with it's center at the specified location
     *
     * @param pos A vector representing the position of the center of the circle
     * @param r   The radius of the circle
     */
    public Circle(Vector pos, double r) {
        this.pos = pos;
        this.r = r;
    }

    public double getX() {
        return pos.getX();
    }

    public double getY() {
        return pos.getY();
    }

    public double getRadius() {
        return r;
    }

    public void setRadius(double r) {
        this.r = r;
    }

    public Vector getPos() {
        return pos;
    }

    public void setPos(Vector pos) {
        this.pos = pos;
    }

    public Circle clone() {
        return new Circle(pos, r);
    }


}
