package com.github.tommyettinger.gand.points;

/**
 * A minimal super-interface that can be implemented by (subclasses of) both {@code com.badlogic.gdx.math.GridPoint2}
 * and {@code com.badlogic.gdx.math.Vector2}, as well as their 3D (or higher) counterparts.
 * @param <P> in implementing types, this is the implementing type
 */
public interface PointN<P extends PointN<P>> {
    /**
     * Gets how many components this type of point has; typically 2 (for points with x and y) or 3 (for points with x,
     * y, and z). This could also be called the dimensionality.
     * @return how many components this type of point has
     */
    int rank();

    /**
     * Returns true if this type of point uses {@code float} or {@code double} for its components, or false otherwise.
     * @return true if this point can use {@code float} values for its components
     */
    boolean floatingPoint();

    /**
     * Returns true if this type of point is mutable and can be edited in-place. If this is false, then the type can
     * either allocate another point when a modified version is needed, or pull one out of an existing pool (in some
     * cases where this makes sense).
     * <br>
     * This is a default method that returns true unless overridden. Most types can leave this alone.
     *
     * @return true if this type is mutable, or false otherwise.
     */
    default boolean mutable() {
        return true;
    }

    /**
     * Returns a copy of this point, or if this type is immutable, this same point.
     * @return a copy of this point, if it is mutable, or this point as-is, if it is immutable
     */
    P cpy();

    default float len() {
        return (float) Math.sqrt(len2());
    }

    float len2();

    /**
     * Returns a point that holds the same contents as the {@code point} parameter.
     * For mutable types, this should edit this value in-place. For immutable types, it must return a new value.
     * @param point another point of the same type; will not be modified
     * @return if this is mutable, then this value after editing; if this is immutable, then a different edited point
     */
    P set(P point);

    /**
     * Returns a point that holds 0 for all components. For mutable types, this should edit
     * this value in-place. For immutable types, it must return a new value.
     * @return if this is mutable, then this value after editing; if this is immutable, then a different edited point
     */
    P setZero();

    /**
     * Subtracts the components in {@code point} from those in this, and returns a point with the subtracted values.
     * For mutable points, this changes the value in-place, and for immutable points, it returns a different point.
     * @param point another point of the same type; will not be modified
     * @return if this is mutable, then this value after editing; if this is immutable, then a different edited point
     */
    P sub(P point);

    /**
     * Adds the components in {@code point} to those in this, and returns a point with the added values.
     * For mutable points, this changes the value in-place, and for immutable points, it returns a different point.
     * @param point another point of the same type; will not be modified
     * @return if this is mutable, then this value after editing; if this is immutable, then a different edited point
     */
    P add(P point);

    /**
     * Multiplies the components in {@code point} with those in this, and returns a point with the multiplied values.
     * For mutable points, this changes the value in-place, and for immutable points, it returns a different point.
     * @param point another point of the same type; will not be modified
     * @return if this is mutable, then this value after editing; if this is immutable, then a different edited point
     */
    P scl(P point);

    /**
     * Gets the distance from this point to the parameter {@code point}, using Euclidean distance, as a float.
     * @param point another point of the same type; will not be modified
     * @return the distance from this point to the parameter, using Euclidean distance, as a float
     */
    default float dst(P point) {
        return (float) Math.sqrt(dst2(point));
    }

    /**
     * Gets the distance from this point to the parameter {@code point}, using squared Euclidean distance, as a float.
     * Note that because this is squared, it is an inadmissible metric for some purposes, such as {@code A*} heuristics.
     * @param point another point of the same type; will not be modified
     * @return the distance from this point to the parameter, using squared Euclidean distance, as a float
     */
    float dst2(P point);

    /**
     * Returns true if the distance from the origin to this point is 1 (within floating-point error of 0.000001f).
     * @return true if this point has distance 1 from the origin, within a tolerance of 0.000001f
     */
    default boolean isUnit() {
        return Math.abs(1f - len2()) <= 0.000001f;
    }

    /**
     * Returns true if the distance from the origin to this point is 1 (within the given tolerance up or down).
     * @param tolerance how much floating-point error to tolerate and still treat this as equal to 1
     * @return true if this point has distance 1 from the origin, within the given tolerance
     */
    default boolean isUnit(float tolerance){
        return Math.abs(1f - len2()) <= tolerance;
    }

    /**
     * Returns true if the distance from the origin to this point is 0 (within floating-point error of 0.000001f).
     * @return true if this point has distance 0 from the origin, within a tolerance of 0.000001f
     */
    default boolean isZero(){
        return Math.abs(len2()) <= 0.000001f;

    }

    /**
     * Returns true if the distance from the origin to this point is 0 (within the given tolerance up or down).
     * @param tolerance how much floating-point error to tolerate and still treat this as equal to 0
     * @return true if this point has distance 0 from the origin, within the given tolerance
     */
    default boolean isZero(float tolerance){
        return Math.abs(len2()) <= tolerance;
    }
}
