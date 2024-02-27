package com.github.tommyettinger.gand.points;

/**
 * A minimal super-interface that can be implemented by 3D {@link PointN} types, such as (subclasses of)
 * {@code com.badlogic.gdx.math.GridPoint3} and {@code com.badlogic.gdx.math.Vector3}.
 * @param <P> in implementing types, this is the implementing type
 */
public interface Point3<P extends Point3<P>> extends PointN<P> {
    /**
     * Gets the first (x) coordinate. Always gets a float, even if {@link #floatingPoint()} is false.
     * @return the first (x) coordinate
     */
    float x();

    /**
     * Sets the first (x) coordinate to {@code next}. Always takes a float, even if {@link #floatingPoint()} is false.
     * For mutable types, this should edit this value in-place. For immutable types, it must return a new value.
     * @param next the new value for the first (x) coordinate
     * @return if this is mutable, then this value after editing; if this is immutable, then a different edited point
     */
    P x(float next);

    /**
     * Gets the second (y) coordinate. Always gets a float, even if {@link #floatingPoint()} is false.
     * @return the second (y) coordinate
     */
    float y();

    /**
     * Sets the second (y) coordinate to {@code next}. Always takes a float, even if {@link #floatingPoint()} is false.
     * For mutable types, this should edit this value in-place. For immutable types, it must return a new value.
     * @param next the new value for the second (y) coordinate
     * @return if this is mutable, then this value after editing; if this is immutable, then a different edited point
     */
    P y(float next);

    /**
     * Gets the third (z) coordinate. Always gets a float, even if {@link #floatingPoint()} is false.
     * @return the third (z) coordinate
     */
    float z();

    /**
     * Sets the third (z) coordinate to {@code next}. Always takes a float, even if {@link #floatingPoint()} is false.
     * For mutable types, this should edit this value in-place. For immutable types, it must return a new value.
     * @param next the new value for the third (z) coordinate
     * @return if this is mutable, then this value after editing; if this is immutable, then a different edited point
     */
    P z(float next);

    /**
     * Sets all coordinates to the given values. Always takes floats, even if {@link #floatingPoint()} is false.
     * For mutable types, this should edit this value in-place. For immutable types, it must return a new value.
     * @param x the new value for the first (x) coordinate
     * @param y the new value for the second (y) coordinate
     * @param z the new value for the third (z) coordinate
     * @return if this is mutable, then this value after editing; if this is immutable, then a different edited point
     */
    P set(float x, float y, float z);

    /**
     * Gets how many components this type of point has; 3 here. This could also be called the dimensionality.
     *
     * @return how many components this type of point has (3)
     */
    default int rank() {
        return 3;
    }
}
