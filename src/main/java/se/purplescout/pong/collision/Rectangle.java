package se.purplescout.pong.collision;


public class Rectangle {

    private final Vector pos;
    private final double w;
    private final double h;

    /**
     * Create a new box, with the specified position, width, and height. If no position
     * is given, the position will be `(0,0)`. If no width or height are given, they will
     * be set to `0`.
     *
     * @param pos A vector representing the top-left of the box.
     * @param width   The width of the box.
     * @param height   The height of the box.
     * @constructor
     */
    public Rectangle(Vector pos, double width, double height) {
        this.pos = pos;
        this.w = width;
        this.h = height;
    }

    public Rectangle(Rectangle template) {
        this(template.pos, template.w, template.h);
    }

    public double getX() {
        return pos.getX();
    }

    public double getY() {
        return pos.getY();
    }

    public double getCenterX() {
        return pos.getX() + w / 2;
    }

    public double getCenterY() {
        return pos.getY() + h / 2;
    }

    public Vector getPos() {
        return pos;
    }

    public double getWidth() {
        return w;
    }

    public double getHeight() {
        return h;
    }
}
