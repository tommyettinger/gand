package com.github.tommyettinger.gand.points;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.NumberUtils;
import com.github.tommyettinger.crux.Point2;

/**
 * The same as {@link Vector2}, just implementing {@link Point2} and {@link Json.Serializable}.
 */
public class PointF2 extends Vector2 implements Point2<PointF2>, Json.Serializable {

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

    public PointF2(Point2<? extends Point2<?>> v) {
        this(v.x(), v.y());
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

    @Override
    public float x() {
        return x;
    }

    @Override
    public PointF2 x(float next) {
        x = next;
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public PointF2 y(float next) {
        y = next;
        return this;
    }

    public PointF2 set(float x, float y){
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public int hashCode() {
        int h = NumberUtils.floatToIntBits(x);
        h = (h << 11 | h >>> 21) + NumberUtils.floatToIntBits(y);
        return h;
    }

    @Override
    public void write(Json json) {
        json.writeValue("x", x, float.class);
        json.writeValue("y", y, float.class);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        this.x = jsonData.getFloat("x");
        this.y = jsonData.getFloat("y");
    }

    /** Converts this {@code PointF2} to a string in the format {@code (x,y)}.
     * @return a string representation of this object. */
    @Override
    public String toString () {
        return "(" + x + "," + y + ")";
    }

    /** Sets this {@code PointF2} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param v the string.
     * @return this vector for chaining */
    public PointF2 fromString (String v) {
        int s0 = v.indexOf(',', 1);
        if (s0 != -1 && v.charAt(0) == '(' && v.charAt(v.length() - 1) == ')') {
            float x = Float.parseFloat(v.substring(1, s0));
            float y = Float.parseFloat(v.substring(s0 + 1, v.length() - 1));
            return this.set(x, y);
        }
        throw new IllegalArgumentException("Not a valid format for a PointF2: " + v);
    }
}
