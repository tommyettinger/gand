package com.github.tommyettinger.gand.points;

/**
 * A minimal super-interface that can be implemented by 2D {@link PointN} types, such as (subclasses of)
 * {@link com.badlogic.gdx.math.GridPoint2} and {@link com.badlogic.gdx.math.Vector2}.
 * @param <P> in implementing types, this is the implementing type
 */
public interface Point2<P extends Point2<P>> extends PointN<P> {
    float x();
    P x(float next);

    float y();
    P y(float next);

    P set(float x, float y);

    /**
     * Gets how many components this type of point has; 2 here. This could also be called the dimensionality.
     *
     * @return how many components this type of point has (2)
     */
    default int rank() {
        return 2;
    }
}
