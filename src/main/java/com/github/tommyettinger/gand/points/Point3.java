package com.github.tommyettinger.gand.points;

/**
 * A minimal super-interface that can be implemented by 3D {@link PointN} types, such as (subclasses of)
 * {@link com.badlogic.gdx.math.GridPoint3} and {@link com.badlogic.gdx.math.Vector3}.
 * @param <P> in implementing types, this is the implementing type
 */
public interface Point3<P extends Point3<P>> extends PointN<P> {
    float x();
    P x(float next);

    float y();
    P y(float next);

    float z();
    P z(float next);

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
