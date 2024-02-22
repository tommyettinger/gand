package com.github.tommyettinger.gand.points;

/**
 * A minimal super-interface that can be implemented by (subclasses of) both {@link com.badlogic.gdx.math.GridPoint2}
 * and {@link com.badlogic.gdx.math.Vector2}, as well as their 3D (or higher) counterparts.
 * @param <P>
 */
public interface PointN<P extends PointN<P>> {
    P cpy();

    default float len() {
        return (float) Math.sqrt(len2());
    }

    float len2();

    P set(P point);

    P sub(P point);

    P add(P point);

    P scl(P point);

    default float dst(P point){
        return (float) Math.sqrt(dst2(point));
    }

    float dst2(P point);

    boolean isUnit();

    boolean isUnit(float v);

    boolean isZero();

    boolean isZero(float v);

    P setZero();
}
