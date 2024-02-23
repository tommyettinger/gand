package com.github.tommyettinger.gand.points;

/**
 * A minimal super-interface that can be implemented by (subclasses of) both {@link com.badlogic.gdx.math.GridPoint2}
 * and {@link com.badlogic.gdx.math.Vector2}, as well as their 3D (or higher) counterparts.
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

    P cpy();

    default float len() {
        return (float) Math.sqrt(len2());
    }

    float len2();

    P set(P point);

    P sub(P point);

    P add(P point);

    P scl(P point);

    default float dst(P point) {
        return (float) Math.sqrt(dst2(point));
    }

    float dst2(P point);

    boolean isUnit();

    boolean isUnit(float v);

    boolean isZero();

    boolean isZero(float v);

    P setZero();
}
