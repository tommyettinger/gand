package com.github.tommyettinger.gand.points;

import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

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
}
