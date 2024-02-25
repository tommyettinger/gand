package com.github.tommyettinger.gand.points;

/**
 * Utility class for constructing the various {@link PointN} types using types inferred from the parameter types.
 * This is meant to be statically imported, so you can call {@code PointI2 grid = pt(1, 2);} or
 * {@code PointF3 = pt(1f, 2f, 3f);} . You can also create {@link PointPair} objects with this in the same way, just
 * using twice as many parameters.
 */
public final class PointMaker {
    private PointMaker() {
    }

    /**
     * Creates a {@link PointI2} from the given ints, x and y.
     * @param x x-coordinate, as an int
     * @param y y-coordinate, as an int
     * @return a new {@link PointI2}
     */
    public static PointI2 pt(int x, int y) {
        return new PointI2(x, y);
    }

    /**
     * Creates a {@link PointI3} from the given ints, x, y, and z.
     * @param x x-coordinate, as an int
     * @param y y-coordinate, as an int
     * @param z z-coordinate, as an int
     * @return a new {@link PointI3}
     */
    public static PointI3 pt(int x, int y, int z) {
        return new PointI3(x, y, z);
    }

    /**
     * Creates a {@link PointF2} from the given floats, x and y.
     * @param x x-coordinate, as a float
     * @param y y-coordinate, as a float
     * @return a new {@link PointF2}
     */
    public static PointF2 pt(float x, float y) {
        return new PointF2(x, y);
    }

    /**
     * Creates a {@link PointF3} from the given floats, x, y, and z.
     * @param x x-coordinate, as a float
     * @param y y-coordinate, as a float
     * @param z z-coordinate, as a float
     * @return a new {@link PointF3}
     */
    public static PointF3 pt(float x, float y, float z) {
        return new PointF3(x, y, z);
    }

    /**
     * Creates a {@link PointPair} of {@link PointI2} from the given ints for point A and then for point B.
     * @param xA x-coordinate for point A, as an int
     * @param yA y-coordinate for point A, as an int
     * @param xB x-coordinate for point B, as an int
     * @param yB y-coordinate for point B, as an int
     * @return a new {@link PointPair} of {@link PointI2}
     */
    public static PointPair<PointI2> pair(int xA, int yA, int xB, int yB) {
        return new PointPair<>(pt(xA, yA), pt(xB, yB));
    }

    /**
     * Creates a {@link PointPair} of {@link PointI3} from the given ints for point A and then for point B.
     * @param xA x-coordinate for point A, as an int
     * @param yA y-coordinate for point A, as an int
     * @param zA z-coordinate for point A, as an int
     * @param xB x-coordinate for point B, as an int
     * @param yB y-coordinate for point B, as an int
     * @param zB z-coordinate for point B, as an int
     * @return a new {@link PointPair} of {@link PointI3}
     */
    public static PointPair<PointI3> pair(int xA, int yA, int zA, int xB, int yB, int zB) {
        return new PointPair<>(pt(xA, yA, zA), pt(xB, yB, zB));
    }

    /**
     * Creates a {@link PointPair} of {@link PointF2} from the given floats for point A and then for point B.
     * @param xA x-coordinate for point A, as a float
     * @param yA y-coordinate for point A, as a float
     * @param xB x-coordinate for point B, as a float
     * @param yB y-coordinate for point B, as a float
     * @return a new {@link PointPair} of {@link PointF2}
     */
    public static PointPair<PointF2> pair(float xA, float yA, float xB, float yB) {
        return new PointPair<>(pt(xA, yA), pt(xB, yB));
    }

    /**
     * Creates a {@link PointPair} of {@link PointF3} from the given floats for point A and then for point B.
     * @param xA x-coordinate for point A, as a float
     * @param yA y-coordinate for point A, as a float
     * @param zA z-coordinate for point A, as a float
     * @param xB x-coordinate for point B, as a float
     * @param yB y-coordinate for point B, as a float
     * @param zB z-coordinate for point B, as a float
     * @return a new {@link PointPair} of {@link PointF2}
     */
    public static PointPair<PointF3> pair(float xA, float yA, float zA, float xB, float yB, float zB) {
        return new PointPair<>(pt(xA, yA, zA), pt(xB, yB, zB));
    }
}
