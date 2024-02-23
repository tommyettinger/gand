package com.github.tommyettinger.gand.points;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * The same as {@link GridPoint2}, just implementing {@link PointN}.
 */
public class PointI2 extends GridPoint2 implements PointN<PointI2> {

    public PointI2() {
        super();
    }

    public PointI2(int x, int y) {
        super(x, y);
    }

    public PointI2(GridPoint2 v) {
        super(v);
    }

    public PointI2(Vector2 v) {
        super(MathUtils.round(v.x), MathUtils.round(v.y));
    }

    public PointI2(PointI2 v) {
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
     * This always returns false.
     *
     * @return false
     */
    @Override
    public boolean floatingPoint() {
        return false;
    }

    @Override
    public PointI2 cpy() {
        return new PointI2(this);
    }

    @Override
    public float len2() {
        return x * x + y * y;
    }

    @Override
    public PointI2 set(PointI2 point) {
        super.set(point);
        return this;
    }

    @Override
    public PointI2 sub(PointI2 point) {
        super.sub(point);
        return this;
    }

    @Override
    public PointI2 add(PointI2 point) {
        super.add(point);
        return this;
    }

    @Override
    public PointI2 scl(PointI2 point) {
        x *= point.x;
        y *= point.y;
        return this;
    }

    @Override
    public float dst(PointI2 point) {
        return super.dst(point);
    }

    @Override
    public float dst2(PointI2 point) {
        return super.dst2(point);
    }

    @Override
    public boolean isUnit() {
        return x != y && (Math.abs(x) == 1 || Math.abs(y) == 1);
    }

    @Override
    public boolean isUnit(float v) {
        return x != y && (MathUtils.isEqual(Math.abs(x), 1, v) || MathUtils.isEqual(Math.abs(y), 1, v));
    }

    @Override
    public boolean isZero() {
        return (x | y) == 0;
    }

    @Override
    public boolean isZero(float v) {
        return MathUtils.isZero(x, v) && MathUtils.isZero(y, v);
    }

    @Override
    public PointI2 setZero() {
        set(0, 0);
        return this;
    }
}
