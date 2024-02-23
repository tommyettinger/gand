package com.github.tommyettinger.gand.points;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import static com.badlogic.gdx.math.MathUtils.round;

/**
 * The same as {@link GridPoint2}, just implementing {@link PointN}.
 */
public class PointI2 extends GridPoint2 implements Point2<PointI2>, Json.Serializable {

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
        super(round(v.x), round(v.y));
    }

    public PointI2(PointI2 v) {
        super(v);
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

    @Override
    public float x() {
        return x;
    }

    @Override
    public PointI2 x(float next) {
        x = round(next);
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public PointI2 y(float next) {
        y = round(next);
        return this;
    }

    public PointI2 set(float x, float y){
        this.x = round(x);
        this.y = round(y);
        return this;
    }

    @Override
    public void write(Json json) {
        json.writeValue("x", x, int.class);
        json.writeValue("y", y, int.class);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        this.x = jsonData.getInt("x");
        this.y = jsonData.getInt("y");
    }
}
