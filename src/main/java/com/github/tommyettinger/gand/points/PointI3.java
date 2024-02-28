package com.github.tommyettinger.gand.points;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.crux.Point2;
import com.github.tommyettinger.crux.Point3;

import static com.badlogic.gdx.math.MathUtils.round;

/**
 * The same as {@link GridPoint2}, just implementing {@link Point2} and {@link Json.Serializable}.
 */
public class PointI3 extends GridPoint3 implements Point3<PointI3>, Json.Serializable {

    public PointI3() {
        super();
    }

    public PointI3(int x, int y, int z) {
        super(x, y, z);
    }

    public PointI3(float x, float y, float z) {
        super(round(x), round(y), round(z));
    }

    public PointI3(GridPoint3 v) {
        super(v);
    }

    public PointI3(Vector3 v) {
        super(round(v.x), round(v.y), round(v.z));
    }

    public PointI3(PointI3 v) {
        super(v);
    }

    public PointI3(Point3<? extends Point2<?>> v) {
        this(v.x(), v.y(), v.z());
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
    public PointI3 cpy() {
        return new PointI3(this);
    }

    @Override
    public float len2() {
        return x * x + y * y + z * z;
    }

    @Override
    public PointI3 set(PointI3 point) {
        super.set(point);
        return this;
    }

    @Override
    public PointI3 sub(PointI3 point) {
        super.sub(point);
        return this;
    }

    @Override
    public PointI3 add(PointI3 point) {
        super.add(point);
        return this;
    }

    @Override
    public PointI3 scl(PointI3 point) {
        x *= point.x;
        y *= point.y;
        z *= point.z;
        return this;
    }

    @Override
    public float dst(PointI3 point) {
        return super.dst(point);
    }

    @Override
    public float dst2(PointI3 point) {
        return super.dst2(point);
    }

    @Override
    public boolean isUnit() {
        return (Math.abs(x) + Math.abs(y) + Math.abs(z) == 1);
    }

    @Override
    public boolean isUnit(float v) {
        return MathUtils.isEqual(Math.abs(x) + Math.abs(y) + Math.abs(z), 1, v);
    }

    @Override
    public boolean isZero() {
        return (x | y | z) == 0;
    }

    @Override
    public boolean isZero(float v) {
        return MathUtils.isZero(x, v) && MathUtils.isZero(y, v) && MathUtils.isZero(z, v);
    }

    @Override
    public PointI3 setZero() {
        set(0, 0, 0);
        return this;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public PointI3 x(float next) {
        x = round(next);
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public PointI3 y(float next) {
        y = round(next);
        return this;
    }

    @Override
    public float z() {
        return z;
    }

    @Override
    public PointI3 z(float next) {
        z = round(next);
        return this;
    }

    @Override
    public int hashCode() {
        return (int)(x * 0xD1B54A32D192ED03L + y * 0xABC98388FB8FAC03L + z * 0x8CB92BA72F3D8DD7L >>> 31);
    }

    public PointI3 set(float x, float y, float z){
        this.x = round(x);
        this.y = round(y);
        this.z = round(z);
        return this;
    }

    @Override
    public void write(Json json) {
        json.writeValue("x", x, int.class);
        json.writeValue("y", y, int.class);
        json.writeValue("z", z, int.class);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        this.x = jsonData.getInt("x");
        this.y = jsonData.getInt("y");
        this.z = jsonData.getInt("z");
    }
}
