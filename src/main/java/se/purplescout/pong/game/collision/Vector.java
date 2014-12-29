package se.purplescout.pong.game.collision;


public class Vector {

    private double x, y;

    /**
     * Constructs a new Vector at (0,0)
     */
    public Vector() {
    }

    /**
     * Create a new Vector, passing in the `x` and `y` coordinates.
     *
     * @param x The x position.
     * @param y The y position.
     * @constructor
     */
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Copy the values of another Vector into this one.
     *
     * @param other The other Vector.
     * @return This for chaining.
     */
    public Vector copy(Vector other) {
        this.x = other.x;
        this.y = other.y;

        return this;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    /**
     * Create a new vector with the same coordinates as this on.
     *
     * @return The new cloned vector
     */
    @Override
    public Vector clone() {
        return new Vector(this.x, this.y);
    }

    /**
     * Change this vector to be perpendicular to what it was before. (Effectively roatates it 90 degrees in a clockwise direction)
     *
     * @return This for chaining.
     */
    public Vector perp() {
        double oldX = this.x;
        this.x = this.y;
        this.y = -oldX;

        return this;
    }

    /**
     * Rotate this vector (counter-clockwise) by the specified angle (in radians).
     *
     * @param angleInRadians The angle to rotate (in radians)
     * @return This for chaining.
     */
    public Vector rotate(double angleInRadians) {
        double x = this.x;
        double y = this.y;
        this.x = x * Math.cos(angleInRadians) - y * Math.sin(angleInRadians);
        this.y = x * Math.sin(angleInRadians) + y * Math.cos(angleInRadians);
        return this;
    }

    /**
     * Reverse this vector.
     *
     * @return This for chaining.
     */
    public Vector reverse() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    /**
     * Normalize this vector. (make it have length of `1`)
     *
     * @return This for chaining.
     */
    public Vector normalize() {
        double d = this.length();
        if (d > 0) {
            this.x = this.x / d;
            this.y = this.y / d;
        }
        return this;
    }

    /**
     * Add another vector to this one.
     *
     * @param other The other Vector.
     * @return This for chaining.
     */
    public Vector add(Vector other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    /**
     * Subtract another vector from this one.
     *
     * @param other The other Vector.
     * @return This for chaining.
     */
    public Vector sub(Vector other) {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    /**
     * Scale this vector. An independent scaling factor can be provided for each axis, or a single scaling factor that will scale both `x` and `y`.
     *
     * @param x The scaling factor.
     * @return This for chaining.
     */
    public Vector scale(double x) {
        return scale(x, x);
    }

    /**
     * Scale this vector. An independent scaling factor can be provided for each axis, or a single scaling factor that will scale both `x` and `y`.
     *
     * @param x The scaling factor in the x direction.
     * @param y The scaling factor in the y direction.
     * @return This for chaining.
     */
    public Vector scale(double x, double y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    /**
     * Project this vector on to another vector.
     *
     * @param other The vector to project onto.
     * @return This for chaining.
     */
    public Vector project(Vector other) {
        double amt = this.dot(other) / other.lengthSquared();
        this.x = amt * other.x;
        this.y = amt * other.y;
        return this;
    }

    /**
     * Project this vector onto a vector of unit length. This is slightly more efficient than `project` when dealing with unit vectors.
     *
     * @param other The unit vector to project onto.
     * @return This for chaining.
     */
    public Vector projectN(Vector other) {
        double amt = this.dot(other);
        this.x = amt * other.x;
        this.y = amt * other.y;
        return this;
    }

    /**
     * Reflect this vector on an arbitrary axis.
     *
     * @param axis The vector representing the axis.
     * @return This for chaining.
     */
    public Vector reflect(Vector axis) {
        double x = this.x;
        double y = this.y;
        this.project(axis).scale(2);
        this.x -= x;
        this.y -= y;
        return this;
    }

    /**
     * Reflect this vector on an arbitrary axis (represented by a unit vector). This is slightly more efficient than `reflect` when dealing with an axis that is a unit vector.
     *
     * @param axis The unit vector representing the axis.
     * @return This for chaining.
     */
    public Vector reflectN(Vector axis) {
        double x = this.x;
        double y = this.y;
        this.projectN(axis).scale(2);
        this.x -= x;
        this.y -= y;
        return this;
    }

    /**
     * Get the dot product of this vector and another.
     *
     * @param other The vector to dot this one against.
     * @return The dot product.
     */
    public double dot(Vector other) {
        return this.x * other.x + this.y * other.y;
    }

    /**
     * Get the squared length of this vector.
     *
     * @return The length^2 of this vector.
     */
    public double lengthSquared() {
        return this.dot(this);
    }

    /**
     * Get the length of this vector.
     *
     * @return The length of this vector.
     */
    public double length() {
        return Math.sqrt(this.lengthSquared());
    }

    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     *
     * @return The angle between this vector and `other` in radians. Always
     * a positive number.
     */
    public double angleBetween(Vector other) {
        double dot = this.dot(other);
        double lengths = this.length() * other.length();
        double angle = Math.acos(dot/lengths);

        return angle;
    }

    /**
     * @return this for chaining
     */
    public Vector setLength(double length) {
        this.normalize();
        this.scale(length);

        return this;
    }
}
