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

package com.github.tommyettinger.gand;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.crux.Point2;
import com.github.tommyettinger.gdcrux.PointI2;
import com.github.tommyettinger.gand.utils.GridMetric;
import com.github.tommyettinger.gdcrux.PointF2;

import java.util.Collection;

/**
 * A group of pathfinding algorithms that explore in all directions equally, and are commonly used when there is more
 * than one valid goal, or when you want a gradient floodfill to mark each cell in an area with its distance from a
 * goal. This type of pathfinding is also called a Dijkstra Map because it produces the same type of grid of
 * distances from the nearest goal as Dijkstra's Pathfinding Algorithm can, but the actual algorithm used here is
 * simpler than Dijkstra's Algorithm, and is more comparable to an optimized breadth-first search that doesn't consider
 * edge costs. You can set more than one goal with {@link #setGoal(Point2)} or {@link #setGoals(Iterable)}, unlike A*;
 * having multiple goals enables such features as pathfinding for creatures that can attack targets between a specified
 * minimum and maximum distance, and the standard uses of Dijkstra Maps such as finding ideal paths to run away. All
 * these features have some price; when paths are short or unobstructed, A* tends to be faster, though some convoluted
 * map shapes can slow down A* more than GradientGrid.
 * <br>
 * One unique optimization made possible here is for when only one endpoint of a path can change in some
 * section of a game, such as when you want to draw a path from the (stationary) player's current cell to the cell the
 * mouse is over, and the mouse can move quickly. This can be done very efficiently by setting the player as a goal with
 * {@link #setGoal(Point2)}, scanning the map to find distances with {@link #scan(Iterable)}, and then as long as the
 * player's position is unchanged (and no obstacles are added/moved), you can get the path by calling
 * {@link #findPathPreScanned(Point2)} and giving it the mouse position as a Point2. If various parts of the path can
 * change instead of just one (such as other NPCs moving around), then you should set a goal or goals and call
 * {@link #findPath(int, Collection, Collection, Point2, Collection)}. The parameters for this are used in various methods
 * in this class with only slight differences: length is the length of path that can be moved "in one go," so 1 for most
 * roguelikes and more for most strategy games, impassable used for enemies and solid moving obstacles, onlyPassable can
 * be null in most roguelikes but in strategy games should contain ally positions that can be moved through as long as
 * no one stops in them (it can also contain terrain that must be jumped over without falling in, like lava), start is
 * the pathfinding NPC's starting position, and targets is a Collection of Point2 that the NPC should pathfind
 * toward (it could be just one Point2, with or without explicitly putting it in an array, or it could be more and the
 * NPC will pick the closest).
 * <br>
 * Other versions of this algorithm are also "out there" in various libraries. This implementation is different from the
 * kinds in SquidLib and SquidSquad in that it permits the interface {@link Point2} for most places a 2D point could be
 * given to it, but when this produces any points, it uses the class {@link PointI2}. You can, awkwardly, pass
 * {@link PointF2} as a Point2 here, but it has to be rounded or cast to an int for
 * this class to use it, and unless every coordinate in your PointF2 values has no fractional part, you're much
 * better-off using PointI2 (or some other integer-based Point2, such as Coord in SquidSquad).
 * <br>
 * As a bit of introduction, <a href="http://www.roguebasin.com/index.php?title=Dijkstra_Maps_Visualized">this article
 * on RogueBasin</a> can provide some useful information on how these work and how to visualize the information they can
 * produce, while
 * <a href="http://www.roguebasin.com/index.php?title=The_Incredible_Power_of_Dijkstra_Maps">the original article that
 * introduced Dijkstra Maps</a> is an inspiring list of the various features Dijkstra Maps can enable.
 * <br>
 * You shouldn't use GradientGrid for all purposes; it can't handle terrains with a cost to enter, and
 * can't handle directional costs like a one-way ledge. For those tasks, {@link Int2UndirectedGraph}
 * or {@link Int2DirectedGraph} will be better fits. Int2DirectedGraph and similar versions of
 * {@link DirectedGraph} can handle even very complicated kinds of map.
 */
public class GradientGridI2 extends GradientGrid<PointI2> implements Json.Serializable {

    private transient final PointI2 workPt = new PointI2();

    @Override
    protected PointI2 acquire(int x, int y) {
        return new PointI2(x, y);
    }

    @Override
    protected PointI2 workingEdit(int x, int y) {
        return workPt.set(x, y);
    }

    /**
     * Construct a GradientGrid without a level to actually scan. If you use this constructor, you must call an
     * initialize() method before using this class.
     */
    public GradientGridI2() {
    }

    /**
     * Used to construct a GradientGrid from the output of another. Uses {@link GridMetric#EUCLIDEAN}.
     *
     * @param level                a 2D float array that was produced by {@link #scan()}
     */
    public GradientGridI2(final float[][] level) {
        this(level, GridMetric.EUCLIDEAN);
    }

    /**
     * Used to construct a GradientGrid from the output of another, specifying a distance calculation.
     *
     * @param level                a 2D float array that was produced by {@link #scan()}
     * @param measurement          the distance calculation to use
     */
    public GradientGridI2(final float[][] level, GridMetric measurement) {
        this.setMeasurement(measurement);
        initialize(level);
    }

    /**
     * Constructor meant to take a char[][] in a simple, classic-roguelike-ish format, where
     * '#' means a wall and anything else (often '.' or ' ') is a walkable tile.
     * This uses {@link GridMetric#EUCLIDEAN}, allowing 8-way movement
     * but preferring orthogonal directions in case of a tie.
     *
     * @param level a 2D char array where '#' indicates a wall and any other char is walkable
     */
    public GradientGridI2(final char[][] level) {
        this(level, GridMetric.EUCLIDEAN);
    }

    /**
     * Constructor meant to take a char[][] in a simple, classic-roguelike-ish format, where
     * '#' means a wall and anything else (often '.' or ' ') is a walkable tile.
     * This uses {@link GridMetric#EUCLIDEAN}, allowing 8-way movement
     * but preferring orthogonal directions in case of a tie.
     * You can specify the character used for walls.
     *
     * @param level a 2D char array where {@code alternateWall} indicates a wall and any other char is walkable
     * @param alternateWall the char that indicates a wall in {@code level}
     */
    public GradientGridI2(final char[][] level, char alternateWall) {
        initialize(level, alternateWall);
    }

    /**
     * Constructor meant to take a char[][] in a simple, classic-roguelike-ish format, where
     * '#' means a wall and anything else (often '.' or ' ') is a walkable tile.
     * Also takes a distance measurement, which you may want to set
     * to {@link GridMetric#MANHATTAN} for 4-way movement only, {@link GridMetric#CHEBYSHEV}
     * for unpredictable 8-way movement or {@link GridMetric#EUCLIDEAN} for more reasonable 8-way
     * movement that prefers straight lines. EUCLIDEAN usually looks the most natural.
     *
     * @param level       a char[x][y] map where '#' is a wall, and anything else is walkable
     * @param measurement how this should measure orthogonal vs. diagonal measurement, such as {@link GridMetric#MANHATTAN} for 4-way only movement
     */
    public GradientGridI2(final char[][] level, GridMetric measurement) {
        this.setMeasurement(measurement);

        initialize(level);
    }

    /**
     * Constructor meant to take a char[][] in a simple, classic-roguelike-ish format, where
     * '#' means a wall and anything else (often '.' or ' ') is a walkable tile.
     * Also takes a distance measurement, which you may want to set
     * to {@link GridMetric#MANHATTAN} for 4-way movement only, {@link GridMetric#CHEBYSHEV}
     * for unpredictable 8-way movement or {@link GridMetric#EUCLIDEAN} for more reasonable 8-way
     * movement that prefers straight lines. EUCLIDEAN usually looks the most natural.
     * You can specify the character used for walls.
     *
     * @param level       a char[x][y] map where {@code alternateWall} is a wall, and anything else is walkable
     * @param alternateWall the char that indicates a wall in {@code level}
     * @param measurement how this should measure orthogonal vs. diagonal measurement, such as {@link GridMetric#MANHATTAN} for 4-way only movement
     */
    public GradientGridI2(final char[][] level, char alternateWall, GridMetric measurement) {
        this.setMeasurement(measurement);

        initialize(level, alternateWall);
    }

    @Override
    public void write(Json json) {
        json.writeValue("m", measurement.ordinal(), int.class);
        json.writeValue("w", width, int.class);
        json.writeValue("h", height, int.class);
        json.writeArrayStart("pm");
        for (int x = 0; x < width; x++) {
            json.writeValue(physicalMap[x], float[].class, float.class);
        }
        json.writeArrayEnd();
        json.writeArrayStart("gm");
        for (int x = 0; x < width; x++) {
            json.writeValue(gradientMap[x], float[].class, float.class);
        }
        json.writeArrayEnd();
        json.writeValue("b", blockingRequirement, int.class);
        json.writeArrayStart("g");
        for (int i = 0; i < goals.size(); i++) {
            json.writeValue(goals.get(i));
        }
        json.writeArrayEnd();
        json.writeArrayStart("p");
        for (int i = 0; i < path.size(); i++) {
            PointI2 pt = path.get(i);
            json.writeValue(pt.x);
            json.writeValue(pt.y);
        }
        json.writeArrayEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        GridMetric m = GridMetric.ALL[jsonData.getInt("m", 0)];
        setMeasurement(m);
        int w = jsonData.getInt("w");
        int h = jsonData.getInt("h");
        float[][] pm = new float[w][];
        JsonValue a2 = jsonData.get("pm");
        int x = 0;
        for(JsonValue sub = a2.child; sub != null; sub = sub.next){
            pm[x] = sub.asFloatArray();
            ++x;
        }
        initialize(pm);
        a2 = jsonData.get("gm");
        x = 0;
        for(JsonValue sub = a2.child; sub != null; sub = sub.next){
            gradientMap[x] = sub.asFloatArray();
            ++x;
        }
        setBlockingRequirement(jsonData.getInt("b"));
        goals.clear();
        goals.addAll(jsonData.get("g").asIntArray());
        /* Not even trying Path yet... */
        path.clear();
    }
}
