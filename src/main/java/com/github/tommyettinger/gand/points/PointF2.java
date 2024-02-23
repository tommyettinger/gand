package com.github.tommyettinger.gand.points;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;

/**
 * The same as {@link Vector2}, just implementing {@link PointN}.
 */
public class PointF2 extends Vector2 implements PointN<PointF2> {

    public PointF2() {
        super();
    }

    public PointF2(float x, float y) {
        super(x, y);
    }

    public PointF2(Vector2 v) {
        super(v);
    }

    public PointF2(GridPoint2 v) {
        super(v.x, v.y);
    }

    public PointF2(PointF2 v) {
        super(v);
    }

    /**
     * Gets how many components this type of point has; 2 here. This could also be called the dimensionality.
     *
     * @return how many components this type of point has (2)
     */
    @Override
    public int rank() {
        return 2;
    }

    /**
     * Returns true if this type of point uses {@code float} or {@code double} for its components, or false otherwise.
     * This always returns true.
     *
     * @return true
     */
    @Override
    public boolean floatingPoint() {
        return true;
    }

    @Override
    public PointF2 cpy() {
        return new PointF2(this);
    }

    @Override
    public PointF2 set(PointF2 point) {
        super.set(point);
        return this;
    }

    @Override
    public PointF2 sub(PointF2 point) {
        super.sub(point);
        return this;
    }

    @Override
    public PointF2 add(PointF2 point) {
        super.add(point);
        return this;
    }

    @Override
    public PointF2 scl(PointF2 point) {
        super.scl(point);
        return this;
    }

    @Override
    public float dst(PointF2 point) {
        return super.dst(point);
    }

    @Override
    public float dst2(PointF2 point) {
        return super.dst2(point);
    }

    @Override
    public PointF2 setZero() {
        super.setZero();
        return this;
    }
}
