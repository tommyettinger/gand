/*
 * Copyright (c) 2020-2024 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tommyettinger.gand.utils;

import com.badlogic.gdx.math.MathUtils;
import com.github.tommyettinger.gand.points.PointI2;

/**
 * Represents the eight grid directions and the deltaX, deltaY values associated
 * with those directions.
 * <br>
 * The grid referenced has x positive to the right and y positive upwards on
 * screen.
 *
 * @author <a href="http://squidpony.com">Eben Howard</a> - howard@squidpony.com
 */
public enum Direction {

    UP(0, 1), DOWN(0, -1), LEFT(-1, 0), RIGHT(1, 0), UP_LEFT(-1, 1), UP_RIGHT(1, 1), DOWN_LEFT(-1, -1), DOWN_RIGHT(1, -1), NONE(0, 0);
    /**
     * An array which holds only the four cardinal directions.
     */
    public static final Direction[] CARDINALS = {UP, DOWN, LEFT, RIGHT};
    /**
     * An array which holds only the four cardinal directions in clockwise order.
     */
    public static final Direction[] CARDINALS_CLOCKWISE = {UP, RIGHT, DOWN, LEFT};
    /**
     * An array which holds only the four cardinal directions in counter-clockwise order.
     */
    public static final Direction[] CARDINALS_COUNTERCLOCKWISE = {UP, LEFT, DOWN, RIGHT};
    /**
     * An array which holds only the four diagonal directions.
     */
    public static final Direction[] DIAGONALS = {UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT};
    /**
     * An array which holds all eight OUTWARDS directions.
     */
    public static final Direction[] OUTWARDS = {UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT};
    /**
     * An array which holds all eight OUTWARDS directions in clockwise order.
     */
    public static final Direction[] CLOCKWISE = {UP, UP_RIGHT, RIGHT, DOWN_RIGHT, DOWN, DOWN_LEFT, LEFT, UP_LEFT};
    /**
     * An array which holds all eight OUTWARDS directions in counter-clockwise order.
     */
    public static final Direction[] COUNTERCLOCKWISE = {UP, UP_LEFT, LEFT, DOWN_LEFT, DOWN, DOWN_RIGHT, RIGHT, UP_RIGHT};
    /**
     * The x coordinate difference for this direction.
     */
    public final int deltaX;
    /**
     * The y coordinate difference for this direction.
     */
    public final int deltaY;
    
    /**
     * Returns the direction that most closely matches the input. This can be any
     * direction, including cardinal and diagonal directions as well as {@link #NONE}
     * in the case that x and y are both 0.
     * <br>
     * This can be used to get the primary intercardinal direction
     * from an origin point to an event point, such as a mouse click on a grid.
     * If the point given is exactly on a boundary between directions then the
     * direction clockwise is returned.
     *
     * @param x the x position relative to the origin (0,0)
     * @param y the y position relative to the origin (0,0)
     * @return the closest matching Direction enum, which may be {@link #NONE}
     */
    public static Direction getDirection(int x, int y) {
        if ((x | y) == 0) return NONE;
        return COUNTERCLOCKWISE[(int)(MathUtils.atan2Deg360(y, x) * (1f/45f) + 6.5f) & 7];
    }

    /**
     * Returns the direction that most closely matches the input.
     * This can be any of the four cardinal directions as well as {@link #NONE}
     * in the case that x and y are both 0.
     * <br>
     * This can be used to get the primary cardinal direction from an
     * origin point to an event point, such as a mouse click on a grid.
     * If the point given is directly diagonal then the direction clockwise is
     * returned.
     *
     * @param x the x position relative to the origin (0,0)
     * @param y the y position relative to the origin (0,0)
     * @return the closest matching cardinal Direction enum, which may also be {@link #NONE}
     */
    public static Direction getCardinalDirection(int x, int y) {
        if ((x | y) == 0) return NONE;
        return CARDINALS_COUNTERCLOCKWISE[(int)(MathUtils.atan2Deg360(y, x) * (1f/90f) + 3.5f) & 3];
    }

	/**
	 * @param from
	 *            The starting point.
	 * @param to
	 *            The desired point to reach.
	 * @return The direction to follow to go from {@code from} to {@code to}. It
	 *         can be cardinal or diagonal.
	 */
	public static Direction toGoTo(PointI2 from, PointI2 to) {
		return getDirection(to.x - from.x, to.y - from.y);
	}

    /**
     * @return Whether this is a diagonal move.
     */
    public boolean isDiagonal() {
        return (deltaX & deltaY) != 0;
    }

    /**
     * @return Whether this is a cardinal-direction move.
     */
    public boolean isCardinal() {
        return (deltaX + deltaY & 1) == 1;
    }

    Direction(int x, int y) {
        deltaX = x;
        deltaY = y;
    }
}
