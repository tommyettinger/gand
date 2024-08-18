package com.github.tommyettinger.gand.points;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.crux.Point2;
import com.github.tommyettinger.crux.PointN;

import static com.badlogic.gdx.math.MathUtils.round;

/**
 * The same as {@link GridPoint2}, just implementing {@link Point2} and {@link Json.Serializable}.
 */
public class PointI2 extends GridPoint2 implements Point2<PointI2>, Json.Serializable {

    public PointI2() {
        super();
    }

    public PointI2(int x, int y) {
        super(x, y);
    }

    public PointI2(float x, float y) {
        super(round(x), round(y));
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

    public PointI2(Point2<? extends Point2<?>> v) {
        this(v.x(), v.y());
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
        return (Math.abs(x) + Math.abs(y) == 1);
    }

    @Override
    public boolean isUnit(float v) {
        return MathUtils.isEqual(Math.abs(x) + Math.abs(y), 1, v);
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

    /**
     * Sets the coordinates of this point to that of another.
     *
     * @param point The 2D grid point (which may be a PointI2 or GridPoint2) to copy coordinates of.
     * @return this PointI2 for chaining.
     */
    @Override
    public PointI2 set(GridPoint2 point) {
        super.set(point);
        return this;
    }

    /**
     * Sets the coordinates of this PointI2.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return this PointI2 for chaining.
     */
    @Override
    public PointI2 set(int x, int y) {
        super.set(x, y);
        return this;
    }

    public PointI2 set(Point2<?> pt) {
        x = round(pt.x());
        y = round(pt.y());
        return this;
    }
    /**
     * Adds another point to this point.
     *
     * @param other The other point
     * @return this PointI2 for chaining.
     */
    @Override
    public PointI2 add(GridPoint2 other) {
        super.add(other);
        return this;
    }

    /**
     * Adds another x,y,z point to this point.
     *
     * @param x The x-coordinate of the other point
     * @param y The y-coordinate of the other point
     * @return this PointI2 for chaining.
     */
    @Override
    public PointI2 add(int x, int y) {
        super.add(x, y);
        return this;
    }

    /**
     * Subtracts another point from this point.
     *
     * @param other The other point
     * @return this PointI2 for chaining.
     */
    @Override
    public PointI2 sub(GridPoint2 other) {
        super.sub(other);
        return this;
    }

    /**
     * Subtracts another x,y,z point from this point.
     *
     * @param x The x-coordinate of the other point
     * @param y The y-coordinate of the other point
     * @return this PointI2 for chaining.
     */
    @Override
    public PointI2 sub(int x, int y) {
        super.sub(x, y);
        return this;
    }

    @Override
    public int hashCode() {
        return x * 0x1827F5 ^ y * 0x123C21;
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

    /** Converts this {@code PointI2} to a string in the format {@code (x,y,z)}.
     * @return a string representation of this object. */
    @Override
    public String toString () {
        return "(" + x + "," + y + ")";
    }

    /** Sets this {@code PointI2} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param v the string.
     * @return this vector for chaining */
    public PointI2 fromString (String v) {
        int s0 = v.indexOf(',', 1);
        if (s0 != -1 && v.charAt(0) == '(' && v.charAt(v.length() - 1) == ')') {
            int x = Integer.parseInt(v.substring(1, s0));
            int y = Integer.parseInt(v.substring(s0 + 1, v.length() - 1));
            return this.set(x, y);
        }
        throw new IllegalArgumentException("Not a valid format for a PointI2: " + v);
    }
}
