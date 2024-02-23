package com.github.tommyettinger.gand.points;

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
