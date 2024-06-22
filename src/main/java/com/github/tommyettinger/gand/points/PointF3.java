package com.github.tommyettinger.gand.points;

import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.NumberUtils;
import com.github.tommyettinger.crux.Point3;

/**
 * The same as {@link Vector3}, just implementing {@link Point3} and {@link Json.Serializable}.
 */
public class PointF3 extends Vector3 implements Point3<PointF3>, Json.Serializable {

    public PointF3() {
        super();
    }

    public PointF3(float x, float y, float z) {
        super(x, y, z);
    }

    public PointF3(Vector3 v) {
        super(v);
    }

    public PointF3(GridPoint3 v) {
        super(v.x, v.y, v.z);
    }

    public PointF3(PointF3 v) {
        super(v);
    }

    public PointF3(Point3<? extends Point3<?>> v) {
        this(v.x(), v.y(), v.z());
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
    public PointF3 cpy() {
        return new PointF3(this);
    }

    @Override
    public PointF3 set(PointF3 point) {
        super.set(point);
        return this;
    }

    @Override
    public PointF3 sub(PointF3 point) {
        super.sub(point);
        return this;
    }

    @Override
    public PointF3 add(PointF3 point) {
        super.add(point);
        return this;
    }

    @Override
    public PointF3 scl(PointF3 point) {
        super.scl(point);
        return this;
    }

    @Override
    public float dst(PointF3 point) {
        return super.dst(point);
    }

    @Override
    public float dst2(PointF3 point) {
        return super.dst2(point);
    }

    @Override
    public PointF3 setZero() {
        super.setZero();
        return this;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public PointF3 x(float next) {
        x = next;
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public PointF3 y(float next) {
        y = next;
        return this;
    }

    @Override
    public float z() {
        return z;
    }

    @Override
    public PointF3 z(float next) {
        z = next;
        return this;
    }

    public PointF3 set(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public int hashCode() {
        return NumberUtils.floatToIntBits(x) * 0x1A36A9 ^ NumberUtils.floatToIntBits(y) * 0x157931 ^ NumberUtils.floatToIntBits(z) * 0x119725;
    }

    @Override
    public void write(Json json) {
        json.writeValue("x", x, float.class);
        json.writeValue("y", y, float.class);
        json.writeValue("z", z, float.class);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        this.x = jsonData.getFloat("x");
        this.y = jsonData.getFloat("y");
        this.z = jsonData.getFloat("z");
    }

    /** Converts this {@code PointF3} to a string in the format {@code (x,y,z)}.
     * @return a string representation of this object. */
    @Override
    public String toString () {
        return "(" + x + "," + y + "," + z + ")";
    }

    /** Sets this {@code PointF3} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param v the string.
     * @return this vector for chaining */
    public PointF3 fromString (String v) {
        int s0 = v.indexOf(',', 1);
        int s1 = v.indexOf(',', s0 + 1);
        if (s0 != -1 && s1 != -1 && v.charAt(0) == '(' && v.charAt(v.length() - 1) == ')') {
                float x = Float.parseFloat(v.substring(1, s0));
                float y = Float.parseFloat(v.substring(s0 + 1, s1));
                float z = Float.parseFloat(v.substring(s1 + 1, v.length() - 1));
                return this.set(x, y, z);
        }
        throw new IllegalArgumentException("Not a valid format for a PointF3: " + v);
    }

}
