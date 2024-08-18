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

import com.github.tommyettinger.crux.Point2;
import com.github.tommyettinger.crux.PointN;
import com.github.tommyettinger.crux.PointPair;
import com.github.tommyettinger.gand.ds.IntDeque;
import com.github.tommyettinger.gand.ds.IntList;
import com.github.tommyettinger.gand.ds.ObjectDeque;
import com.github.tommyettinger.gand.ds.ObjectSet;
import com.github.tommyettinger.gand.points.PointI2;
import com.github.tommyettinger.gand.smoothing.Ortho2DRaycastCollisionDetector;
import com.github.tommyettinger.gand.utils.Direction;
import com.github.tommyettinger.gand.utils.FlowRandom;
import com.github.tommyettinger.gand.utils.GridMetric;

import java.util.Collection;
import java.util.Random;

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
 * {@link com.github.tommyettinger.gand.points.PointF2} as a Point2 here, but it has to be rounded or cast to an int for
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
public abstract class GradientGrid<P extends PointN<P> & Point2<P>> {

    /**
     * The main extension point for GradientGrid, this must be implemented with some way of obtaining a {@code P}
     * instance, which might simply be {@code return new PointI2(x, y);} if {@code P} is {@link PointI2}.
     * Implementations might employ a cache to avoid allocating points. Even though x and y are floats here to allow for
     * the most flexibility, they are almost always small-ish (less than 10,000) non-negative ints.
     *
     * @param x the int x coordinate to use in the new {@code P}
     * @param y the int y coordinate to use in the new {@code P}
     * @return a new or cached {@code P} instance
     */
    protected abstract P acquire(float x, float y);

    /**
     * Simply calls {@link #acquire(float, float)} with {@code (other.x(), other.y())}.
     * @param other any Point2 instance; does not have to be a {@code P}
     * @return a new or cached {@code P} instance with the same x and y values as {@code other}
     */
    protected P acquire(Point2<?> other) {
        return acquire(other.x(), other.y());
    }

    /**
     * An internal extension point for GradientGrid, this can be implemented in different ways depending on whether
     * {@code P} is a mutable or immutable type. If {@code P} is immutable, this doesn't need to be changed in the
     * subclass, and the default behavior will simply return {@link #acquire(float, float)}. If {@code P} is mutable, you
     * will typically have a {@code P} instance as a member of your GradientGrid subclass, and calling this will set its
     * position to the x and y here, then return that member.
     *
     * @param x the x coordinate that the working {@code P} will have
     * @param y the y coordinate that the working {@code P} will have
     * @return the working {@code P} if there is one, or a new {@code P} otherwise
     */
    protected P workingEdit(float x, float y){
        return acquire(x, y);
    }

    /**
     * Simply calls {@link #workingEdit(float, float)} with {@code (other.x(), other.y())}.
     * @param other any Point2 instance; does not have to be a {@code P}
     * @return the working {@code P} if there is one, or a new {@code P} otherwise; will have {@code other}'s x and y
     */
    protected P workingEdit(Point2<?> other) {
        return workingEdit(other.x(), other.y());
    }


    /**
     * Goals are by default marked with 0f. Some situations may have positions with lower values that are especially
     * urgent to move towards.
     */
    public static final float GOAL = 0f;
    /**
     * Floor cells, which include any walkable cell, are marked with a high number equal to 999200f .
     */
    public static final float FLOOR = 999200f;
    /**
     * Walls, which are solid no-entry cells, are marked with a high number equal to 999500f .
     */
    public static final float WALL = 999500f;
    /**
     * This is used to mark cells that the scan couldn't reach, and these dark cells are marked with a high number
     * equal to 999800f .
     */
    public static final float DARK = 999800f;


    /**
     * This affects how distance is measured on diagonal directions vs. orthogonal directions. MANHATTAN should form a
     * diamond shape on a featureless map, while CHEBYSHEV and EUCLIDEAN will form a square. EUCLIDEAN does not affect
     * the length of paths, though it will change the GradientGrid's gradientMap to have many non-integer values, and
     * that in turn will make paths this finds much more realistic and smooth (favoring orthogonal directions unless a
     * diagonal one is a better option). This defaults to EUCLIDEAN.
     */
    public GridMetric measurement = GridMetric.EUCLIDEAN;


    /**
     * Stores which parts of the map are accessible and which are not. Should not be changed unless the actual physical
     * terrain has changed. You should call initialize() with a new map instead of changing this directly.
     */
    public float[][] physicalMap;
    /**
     * The frequently-changing values that are often the point of using this class; goals will have a value of 0, and
     * any cells that can have a character reach a goal in n steps will have a value of n. Cells that cannot be
     * entered because they are solid will have a very high value equal to the WALL constant in this class, and cells
     * that cannot be entered because they cannot reach a goal will have a different very high value equal to the
     * DARK constant in this class.
     */
    public float[][] gradientMap;

    /**
     * Height of the map. Exciting stuff. Don't change this, instead call initialize().
     */
    public int height;
    /**
     * Width of the map. Exciting stuff. Don't change this, instead call initialize().
     */
    public int width;
    /**
     * The latest path that was obtained by calling findPath(). It will not contain the value passed as a starting
     * cell; only steps that require movement will be included, and so if the path has not been found or a valid
     * path toward a goal is impossible, this ObjectDeque will be empty.
     */
    public Path<P> path = new Path<>();

    private ObjectSet<Point2<?>> blocked;

    public transient boolean cutShort;

    /**
     * Goals that pathfinding will seek out. Each item is an encoded point, as done by {@link #encode(int, int)}.
     */
    protected IntList goals = new IntList(256);
    /**
     * Working data used during scanning, this tracks the perimeter of the scanned area so far. This is a member
     * variable and not a local one to avoid reallocating the data structure. Each item is an encoded point, as done by
     * {@link #encode(int, int)}.
     */
    protected IntDeque fresh = new IntDeque(256);

    /**
     * The FlowRandom used to decide which one of multiple equally-short paths to take; this has its state set
     * deterministically before any usage. There will only be one path produced for a given set of parameters, and it
     * will be returned again and again if the same parameters are requested.
     */
    protected final FlowRandom rng = new FlowRandom(0L, 0x9E3779B97F4A7C15L);
    private transient int frustration;

    private final Direction[] dirs = new Direction[9];

    private boolean initialized;

    private transient int mappedCount;

    private int blockingRequirement = 2;

    private transient float cachedLongerPaths = 1.2f;
    private transient final ObjectSet<Point2<?>> cachedImpassable = new ObjectSet<>(32);
    private transient ObjectSet<Point2<?>> cachedFearSources;
    private transient float[][] cachedFleeMap;

    private transient final PointPair<P> workRay = new PointPair<>(acquire(0, 0), acquire(1, 0));

    /**
     * Construct a GradientGrid without a level to actually scan. If you use this constructor, you must call an
     * initialize() method before using this class.
     */
    public GradientGrid() {
    }

    /**
     * Used to construct a GradientGrid from the output of another. Uses {@link GridMetric#EUCLIDEAN}.
     *
     * @param level                a 2D float array that was produced by {@link #scan()}
     */
    public GradientGrid(final float[][] level) {
        this(level, GridMetric.EUCLIDEAN);
    }

    /**
     * Used to construct a GradientGrid from the output of another, specifying a distance calculation.
     *
     * @param level                a 2D float array that was produced by {@link #scan()}
     * @param measurement          the distance calculation to use
     */
    public GradientGrid(final float[][] level, GridMetric measurement) {
        this.measurement = measurement;
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
    public GradientGrid(final char[][] level) {
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
    public GradientGrid(final char[][] level, char alternateWall) {
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
    public GradientGrid(final char[][] level, GridMetric measurement) {
        this.measurement = measurement;

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
    public GradientGrid(final char[][] level, char alternateWall, GridMetric measurement) {
        this.measurement = measurement;

        initialize(level, alternateWall);
    }

    /**
     * Used to initialize or re-initialize a GradientGrid that needs a new physicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ).
     *
     * @param level a 2D float array that should be used as the physicalMap for this GradientGrid
     * @return this for chaining
     */
    public GradientGrid initialize(final float[][] level) {
        int oldWidth = width, oldHeight = height;
        width = level.length;
        height = level[0].length;
        if (width != oldWidth || height != oldHeight) {
            gradientMap = new float[width][height];
            physicalMap = new float[width][height];
        }
        for (int x = 0; x < width; x++) {
            System.arraycopy(level[x], 0, gradientMap[x], 0, height);
            System.arraycopy(level[x], 0, physicalMap[x], 0, height);
        }
        if (blocked == null)
            blocked = new ObjectSet<>(32);
        else
            blocked.clear();
        initialized = true;
        return this;
    }

    /**
     * Used to initialize or re-initialize a GradientGrid that needs a new physicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ).
     *
     * @param level a 2D char array that this will use to establish which cells are walls ('#' as wall, others as floor)
     * @return this for chaining
     */
    public GradientGrid initialize(final char[][] level) {
        int oldWidth = width, oldHeight = height;
        width = level.length;
        height = level[0].length;
        if (width != oldWidth || height != oldHeight) {
            gradientMap = new float[width][height];
            physicalMap = new float[width][height];
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float t = (level[x][y] == '#') ? WALL : FLOOR;
                gradientMap[x][y] = t;
                physicalMap[x][y] = t;
            }
        }
        if (blocked == null)
            blocked = new ObjectSet<>(32);
        else
            blocked.clear();
        initialized = true;
        return this;
    }

    /**
     * Used to initialize or re-initialize a GradientGrid that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ). This
     * initialize() method allows you to specify an alternate wall char other than the default character, '#' .
     *
     * @param level         a 2D char array that this will use to establish which cells are walls (alternateWall defines the wall char, everything else is floor)
     * @param alternateWall the char to consider a wall when it appears in level
     * @return this for chaining
     */
    public GradientGrid initialize(final char[][] level, char alternateWall) {
        int oldWidth = width, oldHeight = height;
        width = level.length;
        height = level[0].length;
        if (width != oldWidth || height != oldHeight) {
            gradientMap = new float[width][height];
            physicalMap = new float[width][height];
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float t = (level[x][y] == alternateWall) ? WALL : FLOOR;
                gradientMap[x][y] = t;
                physicalMap[x][y] = t;
            }
        }
        if (blocked == null)
            blocked = new ObjectSet<>(32);
        else
            blocked.clear();
        initialized = true;
        return this;
    }

    /**
     * Internally, GradientGrid uses int primitives instead of Point2 objects. This method converts from a Point2 to an
     * encoded int that stores the same information, but is somewhat more efficient to work with.
     *
     * @param point a Point2 to find an encoded int for
     * @return an int that encodes the given Point2
     */
    public int encode(final Point2<?> point) {
        return (int)point.y() << 16 | ((int)point.x() & 0xFFFF);
    }

    /**
     * Internally, GradientGrid uses int primitives instead of Point2 objects. This method converts from an x,y point to
     * an encoded int that stores the same information, but is somewhat more efficient to work with.
     *
     * @param x the x component of the point to find an encoded int for
     * @param y the y component of the point to find an encoded int for
     * @return an int that encodes the given x,y point
     */
    public int encode(final int x, final int y) {
        return y << 16 | (x & 0xFFFF);
    }

    /**
     * If you for some reason have one of the internally-used ints produced by {@link #encode(Point2)}, this will write
     * it into a PointI2 if you need it as such. You may prefer using {@link #decodeX(int)} and  {@link #decodeY(int)}
     * to get the x and y components independently and without involving objects.
     *
     * @param changing a PointI2 that will be modified to receive the decoded coordinates
     * @param encoded an encoded int that stores a 2D point; see {@link #encode(Point2)}
     * @return the PointI2 that represents the same x,y position that the given encoded int stores
     */
    public Point2<?> decode(Point2<?> changing, final int encoded) {
        return changing.set(encoded & 0xFFFF, encoded >>> 16);
    }

    /**
     * If you for some reason have one of the internally-used ints produced by {@link #encode(Point2)}, this will decode
     * the x component of the point encoded in that int. This is an extremely simple method that is equivalent to the
     * code {@code encoded & 0xFFFF}. You probably would use this method in
     * conjunction with {@link #decodeY(int)}, or would instead use {@link #decode(Point2, int)} to get a Point2.
     *
     * @param encoded an encoded int; see {@link #encode(Point2)}
     * @return the x component of the position that the given encoded int stores
     */
    public int decodeX(final int encoded) {
        return encoded & 0xFFFF;
    }

    /**
     * If you for some reason have one of the internally-used ints produced by {@link #encode(Point2)}, this will decode
     * the y component of the point encoded in that int. This is an extremely simple method that is equivalent to the
     * code {@code encoded >>> 16}. You probably would use this method in
     * conjunction with {@link #decodeX(int)}, or would instead use {@link #decode(Point2, int)} to get a PointI2.
     *
     * @param encoded an encoded int; see {@link #encode(Point2)}
     * @return the y component of the position that the given encoded int stores
     */
    public int decodeY(final int encoded) {
        return encoded >>> 16;
    }

    /**
     * Resets the gradientMap to its original value from physicalMap.
     */
    public void resetMap() {
        if (!initialized) return;
        for (int x = 0; x < width; x++) {
            System.arraycopy(physicalMap[x], 0, gradientMap[x], 0, height);
        }
    }
    

    /**
     * Resets this GradientGrid to a state with no goals, no discovered path, and no changes made to gradientMap
     * relative to physicalMap.
     */
    public void reset() {
        resetMap();
        goals.clear();
        path.clear();
        fresh.clear();
        frustration = 0;
    }

    /**
     * Marks a cell as a goal for pathfinding, unless the cell is a wall or unreachable area (then it does nothing).
     *
     * @param x non-negative, less than {@link #width}
     * @param y non-negative, less than {@link #height}
     */
    public void setGoal(int x, int y) {
        if (!initialized || x < 0 || x >= width || y < 0 || y >= height) return;
        if (physicalMap[x][y] > FLOOR) {
            return;
        }

        goals.add(encode(x, y));
        gradientMap[x][y] = 0f;
    }

    /**
     * Marks a cell as a goal for pathfinding, unless the cell is a wall or unreachable area (then it does nothing).
     *
     * @param pt any Point2, such as a {@link PointI2}
     */
    public void setGoal(Point2<?> pt) {
        setGoal((int)pt.x(), (int)pt.y());

    }

    /**
     * Marks many cells as goals for pathfinding, ignoring cells in walls or unreachable areas. Simply loops through
     * pts and calls {@link #setGoal(Point2)} on each PointI2 in pts.
     *
     * @param pts any Iterable of PointI2, which can be a List, Set, Queue, etc. of Coords to mark as goals
     */
    public void setGoals(Iterable<? extends Point2<?>> pts) {
        if (!initialized) return;
        for (Point2<?> c : pts) {
            setGoal(c);
        }
    }

    /**
     * Marks many cells as goals for pathfinding, ignoring cells in walls or unreachable areas. Simply loops through
     * pts and calls {@link #setGoal(Point2)} on each PointI2 in pts.
     *
     * @param pts an array of PointI2 to mark as goals
     */
    public void setGoals(Point2<?>[] pts) {
        if (!initialized) return;
        for (int i = 0; i < pts.length; i++) {
            setGoal(pts[i]);
        }
    }

    /**
     * Marks a specific cell in gradientMap as completely impossible to enter.
     *
     * @param x
     * @param y
     */
    public void setOccupied(int x, int y) {
        if (!initialized || x < 0 || x >= width || y < 0 || y >= height) return;
        gradientMap[x][y] = WALL;
    }

    /**
     * Reverts a cell to the value stored in the original state of the level as known by physicalMap.
     *
     * @param x
     * @param y
     */
    public void resetCell(int x, int y) {
        if (!initialized || x < 0 || x >= width || y < 0 || y >= height) return;
        gradientMap[x][y] = physicalMap[x][y];
    }

    /**
     * Reverts a cell to the value stored in the original state of the level as known by physicalMap.
     *
     * @param pt
     */
    public void resetCell(Point2<?> pt) {
        if (!initialized || !isWithin(pt, width, height)) return;
        gradientMap[(int) pt.x()][(int) pt.y()] = physicalMap[(int) pt.x()][(int) pt.y()];
    }

    /**
     * Used to remove all goals and undo any changes to gradientMap made by having a goal present.
     */
    public void clearGoals() {
        if (!initialized)
            return;
        int sz = goals.size(), t;
        for (int i = 0; i < sz; i++) {
            resetCell(decodeX(t = goals.pop()), decodeY(t));
        }
    }

    private void setFresh(final int x, final int y, float counter) {
        if (x < 0 || x >= width || y < 0 || y >= height || gradientMap[x][y] < counter)
            return;
        gradientMap[x][y] = counter;
        fresh.addFirst(encode(x, y));
    }

    private void setFresh(final PointI2 pt, float counter) {
        if (!isWithin(pt, width, height) || gradientMap[pt.x][pt.y] < counter)
            return;
        gradientMap[pt.x][pt.y] = counter;
        fresh.addFirst(encode(pt));
    }

    /**
     * Recalculate the GradientGrid and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @return A 2D float[width][height] using the width and height of what this knows about the physical map.
     */
    public float[][] scan() {
        return scan(null);
    }

    /**
     * Recalculate the GradientGrid and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @param impassable An Iterable of PointI2 keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A 2D float[width][height] using the width and height of what this knows about the physical map.
     */
    public float[][] scan(final Iterable<? extends Point2<?>> impassable) {
        scan(null, impassable);
        float[][] gradientClone = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
            System.arraycopy(gradientMap[x], 0, gradientClone[x], 0, height);
        }

        return gradientClone;
    }

    /**
     * Recalculate the GradientGrid and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field, and nothing is returned.
     * If you want the data returned, you can use {@link #scan(Iterable)} (which calls this method with
     * null for the start parameter, then modifies the gradientMap field and returns a copy), or you can
     * just retrieve the gradientMap (maybe copying it; {@link #copy(float[][])} is a
     * convenient option for copying a 2D float array). If start is non-null, which is usually used when
     * finding a single path, then cells that didn't need to be explored (because they were further than the
     * path needed to go from start to goal) will have the value {@link #FLOOR}. You may wish to assign a
     * different value to these cells in some cases (especially if start is null, which means any cells that
     * are still FLOOR could not be reached from any goal), and the overloads of scan that return 2D float
     * arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to {@link #WALL}.
     *
     * @param start      a PointI2 representing the location of the pathfinder; may be null, which has this scan the whole map
     * @param impassable An Iterable of PointI2 keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     */
    public void scan(final Point2<?> start, final Iterable<? extends Point2<?>> impassable) {
        scan(start, impassable, false);
    }

    /**
     * Recalculate the GradientGrid and return it. Cells in {@link #gradientMap} that had the lowest value
     * will be treated as goals if {@code nonZeroOptimum} is true; otherwise, only cells marked as goals with
     * {@link #setGoal(Point2)} will be considered goals and some overhead will be saved. The cells adjacent
     * to goals will have a value of 1, and cells progressively further from goals will have a value equal to
     * the distance from the nearest goal. The exceptions are walls, which will have a value defined by the
     * {@link #WALL} constant in this class, and areas that the scan was unable to reach, which will have a
     * value defined by the {@link #DARK} constant in this class (typically, these areas should not be used to
     * place NPCs or items and should be filled with walls). This uses the current {@link #measurement}. The
     * result is stored in the {@link #gradientMap} field, and nothing is returned. If you want the data
     * returned, you can use {@link #scan(Iterable)} (which calls this method with null for the start
     * parameter, then modifies the gradientMap field and returns a copy), or you can
     * just retrieve the gradientMap (maybe copying it; {@link #copy(float[][])} is a
     * convenient option for copying a 2D float array). If start is non-null, which is usually used when
     * finding a single path, then cells that didn't need to be explored (because they were further than the
     * path needed to go from start to goal) will have the value {@link #FLOOR}. You may wish to assign a
     * different value to these cells in some cases (especially if start is null, which means any cells that
     * are still FLOOR could not be reached from any goal), and the overloads of scan that return 2D float
     * arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to {@link #WALL}.
     *
     * @param start          a PointI2 representing the location of the pathfinder; may be null, which has this scan the whole map
     * @param impassable     An Iterable of PointI2 keys representing the locations of enemies or other moving obstacles to a
     *                       path that cannot be moved through; this can be null if there are no such obstacles.
     * @param nonZeroOptimum if the cell to pathfind toward should have a value of {@link #GOAL} (0f), this should be
     *                       false; if it should have a different value or if you don't know, it should be true
     */
    public void scan(final Point2<?> start, final Iterable<? extends Point2<?>> impassable, final boolean nonZeroOptimum) {

        if (!initialized) return;
        if (impassable != null) {
            for (Point2<?> pt : impassable) {
                if (pt != null && isWithin(pt, width, height))
                    gradientMap[(int)pt.x()][(int)pt.y()] = WALL;
            }
        }
        int dec, adjX, adjY, cen, cenX, cenY;
        fresh.clear();
        fresh.addAll(goals);
        for (int i = 0; i < goals.size(); i++) {
            dec = goals.get(i);
            gradientMap[decodeX(dec)][decodeY(dec)] = GOAL;
        }
        float cs, dist;
        if (nonZeroOptimum) {
            float currentLowest = 999000f;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (gradientMap[x][y] <= FLOOR) {
                        if (gradientMap[x][y] < currentLowest) {
                            currentLowest = gradientMap[x][y];
                            fresh.clear();
                            fresh.add(encode(x, y));
                        } else if (gradientMap[x][y] == currentLowest) {
                            fresh.add(encode(x, y));
                        }
                    }
                }
            }
        }
        int fsz, numAssigned = fresh.size();
        mappedCount = goals.size();
        Direction[] moveDirs = (measurement == GridMetric.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        while (numAssigned > 0) {
            numAssigned = 0;
            fsz = fresh.size();
            for (int ci = fsz; ci > 0; ci--) {
                cen = fresh.removeLast();
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientMap[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if (d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if ((gradientMap[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientMap[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement) {
                            continue;
                        }
                    }
                    float h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h;
                    if (gradientMap[adjX][adjY] <= FLOOR && cs < gradientMap[adjX][adjY]) {
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                        ++mappedCount;
                        if (start != null && start.x() == adjX && start.y() == adjY) {
                            if (impassable != null) {
                                for (Point2<?> pt : impassable) {
                                    if (pt != null && isWithin(pt, width, height))
                                        gradientMap[(int)pt.x()][(int)pt.y()] = physicalMap[(int)pt.x()][(int)pt.y()];
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
        if (impassable != null) {
            for (Point2<?> pt : impassable) {
                if (pt != null && isWithin(pt, width, height))
                    gradientMap[(int)pt.x()][(int)pt.y()] = physicalMap[(int)pt.x()][(int)pt.y()];
            }
        }
    }

    /**
     * Recalculate the GradientGrid up to a limit and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. If a cell would take more steps to
     * reach than the given limit, it will have a value of DARK if it was passable instead of the distance. The
     * exceptions are walls, which will have a value defined by the WALL constant in this class, and areas that the scan
     * was unable to reach, which will have a value defined by the DARK constant in this class. This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @param limit The maximum number of steps to scan outward from a goal.
     * @return A 2D float[width][height] using the width and height of what this knows about the physical map.
     */
    public float[][] partialScan(final int limit) {
        return partialScan(limit, null);
    }

    /**
     * Recalculate the GradientGrid up to a limit and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. If a cell would take more steps to
     * reach than the given limit, it will have a value of DARK if it was passable instead of the distance. The
     * exceptions are walls, which will have a value defined by the WALL constant in this class, and areas that the scan
     * was unable to reach, which will have a value defined by the DARK constant in this class. This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @param impassable A Collection of PointI2 keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A 2D float[width][height] using the width and height of what this knows about the physical map.
     */
    public float[][] partialScan(final int limit, final Iterable<? extends Point2<?>> impassable) {
        partialScan(null, limit, impassable);
        float[][] gradientClone = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
            System.arraycopy(gradientMap[x], 0, gradientClone[x], 0, height);
        }

        return gradientClone;
    }

    /**
     * Recalculate the GradientGrid up to a limit and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. If a cell would take more steps to
     * reach than the given limit, or if it was otherwise unreachable, it will have a value of {@link #FLOOR} or greater
     * if it was passable instead of the distance. The exceptions are walls, which will have a value defined by the
     * {@link #WALL} constant in this class. This uses the current {@link #measurement}. The result is stored in the
     * {@link #gradientMap} field, and nothing is returned.If you want the data returned, you can use
     * {@link #partialScan(int, Iterable)} (which calls this method with null for the start parameter, then modifies
     * the gradientMap field and returns a copy), or you can just retrieve the gradientMap (maybe copying it;
     * {@link #copy(float[][])} is a convenient option for copying a 2D float array).
     * <br>
     * If start is non-null, which is usually used when finding a single path, then cells that didn't need to be
     * explored (because they were further than the path needed to go from start to goal) will have the value
     * {@link #FLOOR}. You may wish to assign a different value to these cells in some cases (especially if start is
     * null, which means any cells that are still FLOOR could not be reached from any goal), and the overloads of
     * partialScan that return 2D float arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to
     * {@link #WALL}.
     *
     * @param start      a PointI2 representing the location of the pathfinder; may be null to have this scan more of the map
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @param impassable An Iterable of PointI2 keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     */
    public void partialScan(final Point2<?> start, final int limit, final Iterable<? extends Point2<?>> impassable) {
        partialScan(start, limit, impassable, false);
    }

    /**
     * Recalculate the GradientGrid up to a limit and return it. Cells in {@link #gradientMap} that had the lowest value
     * will be treated as goals if {@code nonZeroOptimum} is true; otherwise, only cells marked as goals with
     * {@link #setGoal(Point2)} will be considered goals and some overhead will be saved. If a cell would take more steps
     * to reach than the given limit, or if it was otherwise unreachable, it will have a value of {@link #FLOOR} or
     * greater if it was passable instead of the distance. The exceptions are walls, which will have a value defined by
     * the {@link #WALL} constant in this class. This uses the current {@link #measurement}. The result is stored in the
     * {@link #gradientMap} field, and nothing is returned.If you want the data returned, you can use
     * {@link #partialScan(int, Iterable)} (which calls this method with null for the start parameter, then modifies
     * the gradientMap field and returns a copy), or you can just retrieve the gradientMap (maybe copying it;
     * {@link #copy(float[][])} is a convenient option for copying a 2D float array).
     * <br>
     * If start is non-null, which is usually used when finding a single path, then cells that didn't need to be
     * explored (because they were further than the path needed to go from start to goal) will have the value
     * {@link #FLOOR}. You may wish to assign a different value to these cells in some cases (especially if start is
     * null, which means any cells that are still FLOOR could not be reached from any goal), and the overloads of
     * partialScan that return 2D float arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to
     * {@link #WALL}.
     *
     * @param start          a PointI2 representing the location of the pathfinder; may be null to have this scan more of the map
     * @param limit          The maximum number of steps to scan outward from a goal.
     * @param impassable     An Iterable of PointI2 keys representing the locations of enemies or other moving obstacles to a
     *                       path that cannot be moved through; this can be null if there are no such obstacles.
     * @param nonZeroOptimum if the cell to pathfind toward should have a value of {@link #GOAL} (0f), this should be
     *                       false; if it should have a different value or if you don't know, it should be true
     */
    public void partialScan(final Point2<?> start, final int limit, final Iterable<? extends Point2<?>> impassable, final boolean nonZeroOptimum) {

        if (!initialized || limit <= 0) return;
        if (impassable != null) {
            for (Point2<?> pt : impassable) {
                if (pt != null && isWithin(pt, width, height))
                    gradientMap[(int)pt.x()][(int)pt.y()] = WALL;
            }
        }
        int dec, adjX, adjY, cen, cenX, cenY;
        fresh.clear();
        fresh.addAll(goals);
        for (int i = 0; i < goals.size(); i++) {
            dec = goals.get(i);
            gradientMap[decodeX(dec)][decodeY(dec)] = GOAL;
        }
        float cs, dist;
        if (nonZeroOptimum) {
            float currentLowest = 999000;
            if (start == null) {
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        if (gradientMap[x][y] <= FLOOR) {
                            if (gradientMap[x][y] < currentLowest) {
                                currentLowest = gradientMap[x][y];
                                fresh.clear();
                                fresh.add(encode(x, y));
                            } else if (gradientMap[x][y] == currentLowest) {
                                fresh.add(encode(x, y));
                            }
                        }
                    }
                }
            } else {
                final int x0 = Math.max(0, (int)start.x() - limit), x1 = Math.min((int)start.x() + limit + 1, width),
                        y0 = Math.max(0, (int)start.y() - limit), y1 = Math.min((int)start.y() + limit + 1, height);
                for (int x = x0; x < x1; x++) {
                    for (int y = y0; y < y1; y++) {
                        if (gradientMap[x][y] <= FLOOR) {
                            if (gradientMap[x][y] < currentLowest) {
                                currentLowest = gradientMap[x][y];
                                fresh.clear();
                                fresh.add(encode(x, y));
                            } else if (gradientMap[x][y] == currentLowest) {
                                fresh.add(encode(x, y));
                            }
                        }
                    }
                }
            }
        }
        int fsz, numAssigned = fresh.size();
        mappedCount = goals.size();
        Direction[] moveDirs = (measurement == GridMetric.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        int iter = 0;
        while (numAssigned > 0 && iter++ < limit) {
            numAssigned = 0;
            fsz = fresh.size();
            for (int ci = fsz; ci > 0; ci--) {
                cen = fresh.removeLast();
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientMap[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if (d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if ((gradientMap[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientMap[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement) {
                            continue;
                        }
                    }
                    float h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h;
                    if (gradientMap[adjX][adjY] <= FLOOR && cs < gradientMap[adjX][adjY]) {
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                        ++mappedCount;
                        if (start != null && (int)start.x() == adjX && (int)start.y() == adjY) {
                            if (impassable != null) {
                                for (Point2<?> pt : impassable) {
                                    if (pt != null && isWithin(pt, width, height))
                                        gradientMap[(int)pt.x()][(int)pt.y()] = physicalMap[(int)pt.x()][(int)pt.y()];
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
        if (impassable != null) {
            for (Point2<?> pt : impassable) {
                if (pt != null && isWithin(pt, width, height))
                    gradientMap[(int)pt.x()][(int)pt.y()] = physicalMap[(int)pt.x()][(int)pt.y()];
            }
        }
    }

    /**
     * Recalculate the GradientGrid for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned GradientGrid assumes that a creature is square, with a side length equal to the passed
     * size, that its minimum-x, minimum-y cell is the starting cell, and that any cell with a distance number
     * represents the distance for the creature's minimum-x, minimum-y cell to reach it. Cells that cannot be entered
     * by the minimum-x, minimum-y cell because of sizing (such as a floor cell next to a maximum-x and/or maximum-y
     * wall if size is &gt; 1) will be marked as DARK. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class. (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.  The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @param impassable An Iterable of PointI2 keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @return A 2D float[width][height] using the width and height of what this knows about the physical map.
     */
    public float[][] scan(final Iterable<? extends Point2<?>> impassable, final int size) {
        scan(null, impassable, size);
        float[][] gradientClone = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
            System.arraycopy(gradientMap[x], 0, gradientClone[x], 0, height);
        }
        return gradientClone;

    }

    /**
     * Recalculate the GradientGrid for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned GradientGrid assumes that a creature is square, with a side length equal to the passed
     * size, that its minimum-x, minimum-y cell is the starting cell, and that any cell with a distance number
     * represents the distance for the creature's minimum-x, minimum-y cell to reach it. Cells that cannot be entered
     * by the minimum-x, minimum-y cell because of sizing (such as a floor cell next to a maximum-x and/or maximum-y
     * wall if size is &gt; 1) will be marked as DARK. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class. (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.  The result is stored in the {@link #gradientMap} field, and nothing is returned.
     * If you want the data returned, you can use {@link #scan(Iterable, int)} (which calls this method with
     * null for the start parameter, then modifies the gradientMap field and returns a copy), or you can
     * just retrieve the gradientMap (maybe copying it; {@link #copy(float[][])} is a
     * convenient option for copying a 2D float array). If start is non-null, which is usually used when
     * finding a single path, then cells that didn't need to be explored (because they were further than the
     * path needed to go from start to goal) will have the value {@link #FLOOR}. You may wish to assign a
     * different value to these cells in some cases (especially if start is null, which means any cells that
     * are still FLOOR could not be reached from any goal), and the overloads of scan that return 2D float
     * arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to {@link #WALL}.
     *
     * @param impassable An Iterable of PointI2 keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     */
    public void scan(final Point2<?> start, final Iterable<? extends Point2<?>> impassable, final int size) {

        if (!initialized) return;
        float[][] gradientClone = copy(gradientMap);
        if (impassable != null) {
            for (Point2<?> pt : impassable) {
                if (pt != null && isWithin(pt, width, height))
                    gradientMap[(int)pt.x()][(int)pt.y()] = WALL;
            }
        }
        for (int xx = size; xx < width; xx++) {
            for (int yy = size; yy < height; yy++) {
                if (gradientMap[xx][yy] > FLOOR) {
                    for (int xs = xx, xi = 0; xi < size && xs >= 0; xs--, xi++) {
                        for (int ys = yy, yi = 0; yi < size && ys >= 0; ys--, yi++) {
                            gradientClone[xs][ys] = WALL;
                        }
                    }
                }
            }
        }
        int dec, adjX, adjY, cen, cenX, cenY;

        PER_GOAL:
        for (int i = 0; i < goals.size(); i++) {
            dec = goals.get(i);
            for (int xs = decodeX(dec), xi = 0; xi < size && xs >= 0; xs--, xi++) {
                for (int ys = decodeY(dec), yi = 0; yi < size && ys >= 0; ys--, yi++) {
                    if (physicalMap[xs][ys] > FLOOR)
                        continue PER_GOAL;
                    gradientClone[xs][ys] = GOAL;
                }
            }
        }
        float currentLowest = 999000, cs, dist;
        fresh.clear();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientClone[x][y] <= FLOOR) {
                    if (gradientClone[x][y] < currentLowest) {
                        currentLowest = gradientClone[x][y];
                        fresh.clear();
                        fresh.add(encode(x, y));
                    } else if (gradientClone[x][y] == currentLowest) {
                        fresh.add(encode(x, y));
                    }
                }
            }
        }
        int fsz, numAssigned = fresh.size();
        mappedCount = goals.size();
        Direction[] moveDirs = (measurement == GridMetric.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        while (numAssigned > 0) {
            numAssigned = 0;
            fsz = fresh.size();
            for (int ci = fsz; ci > 0; ci--) {
                cen = fresh.removeLast();
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientClone[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if (d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if ((gradientClone[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientClone[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement) {
                            continue;
                        }
                    }
                    float h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h;
                    if (gradientClone[adjX][adjY] <= FLOOR && cs < gradientClone[adjX][adjY]) {
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                        ++mappedCount;
                        if (start != null && (int)start.x() == adjX && (int)start.y() == adjY) {
                            if (impassable != null) {
                                for (Point2<?> pt : impassable) {
                                    if (pt != null && isWithin(pt, width, height)) {
                                        for (int xs = (int) pt.x(), xi = 0; xi < size && xs >= 0; xs--, xi++) {
                                            for (int ys = (int) pt.y(), yi = 0; yi < size && ys >= 0; ys--, yi++) {
                                                gradientClone[xs][ys] = physicalMap[xs][ys];
                                            }
                                        }
                                    }
                                }
                            }
                            gradientMap = gradientClone;
                            return;
                        }
                    }
                }
            }
        }
        if (impassable != null) {
            for (Point2<?> pt : impassable) {
                if (pt != null && isWithin(pt, width, height)) {
                    for (int xs = (int) pt.x(), xi = 0; xi < size && xs >= 0; xs--, xi++) {
                        for (int ys = (int) pt.y(), yi = 0; yi < size && ys >= 0; ys--, yi++) {
                            gradientClone[xs][ys] = physicalMap[xs][ys];
                        }
                    }
                }
            }
        }
        gradientMap = gradientClone;
    }


    /**
     * Recalculate the GradientGrid for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned GradientGrid assumes that a creature is square, with a side length equal to the passed
     * size, that its minimum-x, minimum-y cell is the starting cell, and that any cell with a distance number
     * represents the distance for the creature's minimum-x, minimum-y cell to reach it. Cells that cannot be entered
     * by the minimum-x, minimum-y cell because of sizing (such as a floor cell next to a maximum-x and/or maximum-y
     * wall if size is &gt; 1) will be marked as DARK. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class. (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.  The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @param impassable An Iterable of PointI2 keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @return A 2D float[width][height] using the width and height of what this knows about the physical map.
     */
    public float[][] partialScan(final int limit, final Iterable<? extends Point2<?>> impassable, final int size) {
        partialScan(limit, null, impassable, size);
        float[][] gradientClone = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
            System.arraycopy(gradientMap[x], 0, gradientClone[x], 0, height);
        }
        return gradientClone;

    }

    /**
     * Recalculate the GradientGrid for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned GradientGrid assumes that a creature is square, with a side length equal to the passed
     * size, that its minimum-x, minimum-y cell is the starting cell, and that any cell with a distance number
     * represents the distance for the creature's minimum-x, minimum-y cell to reach it. Cells that cannot be entered
     * by the minimum-x, minimum-y cell because of sizing (such as a floor cell next to a maximum-x and/or maximum-y
     * wall if size is &gt; 1) will be marked as DARK. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class. (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.  The result is stored in the {@link #gradientMap} field, and nothing is returned.
     * If you want the data returned, you can use {@link #partialScan(int, Iterable, int)} (which calls this method
     * with null for the start parameter, then modifies the gradientMap field and returns a copy), or you can
     * just retrieve the gradientMap (maybe copying it; {@link #copy(float[][])} is a
     * convenient option for copying a 2D float array). If start is non-null, which is usually used when
     * finding a single path, then cells that didn't need to be explored (because they were further than the
     * path needed to go from start to goal) will have the value {@link #FLOOR}. You may wish to assign a
     * different value to these cells in some cases (especially if start is null, which means any cells that
     * are still FLOOR could not be reached from any goal), and the overloads of partialScan that return 2D float
     * arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to {@link #WALL}.
     *
     * @param impassable An Iterable of PointI2 keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     */
    public void partialScan(final int limit, final Point2<?> start, final Iterable<? extends Point2<?>> impassable, final int size) {

        if (!initialized || limit <= 0) return;
        float[][] gradientClone = copy(gradientMap);
        if (impassable != null) {
            for (Point2<?> pt : impassable) {
                if (pt != null && isWithin(pt, width, height))
                    gradientMap[(int)pt.x()][(int)pt.y()] = WALL;
            }
        }
        for (int xx = size; xx < width; xx++) {
            for (int yy = size; yy < height; yy++) {
                if (gradientMap[xx][yy] > FLOOR) {
                    for (int xs = xx, xi = 0; xi < size && xs >= 0; xs--, xi++) {
                        for (int ys = yy, yi = 0; yi < size && ys >= 0; ys--, yi++) {
                            gradientClone[xs][ys] = WALL;
                        }
                    }
                }
            }
        }
        int dec, adjX, adjY, cen, cenX, cenY;

        PER_GOAL:
        for (int i = 0; i < goals.size(); i++) {
            dec = goals.get(i);
            for (int xs = decodeX(dec), xi = 0; xi < size && xs >= 0; xs--, xi++) {
                for (int ys = decodeY(dec), yi = 0; yi < size && ys >= 0; ys--, yi++) {
                    if (physicalMap[xs][ys] > FLOOR)
                        continue PER_GOAL;
                    gradientClone[xs][ys] = GOAL;
                }
            }
        }
        float currentLowest = 999000, cs, dist;
        fresh.clear();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientClone[x][y] <= FLOOR) {
                    if (gradientClone[x][y] < currentLowest) {
                        currentLowest = gradientClone[x][y];
                        fresh.clear();
                        fresh.add(encode(x, y));
                    } else if (gradientClone[x][y] == currentLowest) {
                        fresh.add(encode(x, y));
                    }
                }
            }
        }
        int fsz, numAssigned = fresh.size();
        mappedCount = goals.size();
        Direction[] moveDirs = (measurement == GridMetric.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        int iter = 0;
        while (numAssigned > 0 && iter++ < limit) {
            numAssigned = 0;
            fsz = fresh.size();
            for (int ci = fsz; ci > 0; ci--) {
                cen = fresh.removeLast();
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientClone[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if (d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if ((gradientClone[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientClone[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement) {
                            continue;
                        }
                    }
                    float h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h;
                    if (gradientClone[adjX][adjY] <= FLOOR && cs < gradientClone[adjX][adjY]) {
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                        ++mappedCount;
                        if (start != null && (int)start.x() == adjX && (int)start.y() == adjY) {
                            if (impassable != null) {
                                for (Point2<?> pt : impassable) {
                                    if (pt != null && isWithin(pt, width, height)) {
                                        for (int xs = (int) pt.x(), xi = 0; xi < size && xs >= 0; xs--, xi++) {
                                            for (int ys = (int) pt.y(), yi = 0; yi < size && ys >= 0; ys--, yi++) {
                                                gradientClone[xs][ys] = physicalMap[xs][ys];
                                            }
                                        }
                                    }
                                }
                            }
                            gradientMap = gradientClone;
                            return;
                        }
                    }
                }
            }
        }
        if (impassable != null) {
            for (Point2<?> pt : impassable) {
                if (pt != null && isWithin(pt, width, height)) {
                    for (int xs = (int) pt.x(), xi = 0; xi < size && xs >= 0; xs--, xi++) {
                        for (int ys = (int) pt.y(), yi = 0; yi < size && ys >= 0; ys--, yi++) {
                            gradientClone[xs][ys] = physicalMap[xs][ys];
                        }
                    }
                }
            }
        }
        gradientMap = gradientClone;
    }

    /**
     * Scans the dungeon using GradientGrid.scan with the listed goals and start point, and returns a list
     * of PointI2 positions (using the current measurement) needed to get closer to the closest reachable
     * goal. The maximum length of the returned list is given by length, which represents movement in a system where
     * a single move can be multiple cells if length is greater than 1 and should usually be 1 in standard roguelikes;
     * if moving the full length of the list would place the mover in a position shared  by one of the positions in
     * onlyPassable (which is typically filled with friendly units that can be passed through in multi-cell-movement
     * scenarios), it will recalculate a move so that it does not pass into that cell. The keys in impassable should
     * be the positions of enemies and obstacles that cannot be moved  through, and will be ignored if there is a goal
     * overlapping one. This overload always scans the whole map; use
     * {@link #findPath(int, int, Collection, Collection, Point2, Collection)} to scan a smaller area for performance reasons.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param length       the length of the path to calculate
     * @param impassable   a Set of impassable PointI2 positions that may change (not constant like walls); can be null
     * @param onlyPassable a Set of PointI2 positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start        the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets      a vararg or array of PointI2 that this will try to pathfind toward
     * @return an ObjectDeque of PointI2 that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ObjectDeque<P> findPath(int length, Collection<? extends Point2<?>> impassable,
                                       Collection<? extends Point2<?>> onlyPassable, Point2<?> start, Collection<? extends Point2<?>> targets) {
        return findPath(length, -1, impassable, onlyPassable, start, targets);
    }

    /**
     * Scans the dungeon using GradientGrid.scan or GradientGrid.partialScan with the listed goals and start
     * point, and returns a list of PointI2 positions (using the current measurement) needed to get closer
     * to the closest reachable goal. The maximum length of the returned list is given by length, which represents
     * movement in a system where a single move can be multiple cells if length is greater than 1 and should usually
     * be 1 in standard roguelikes; if moving the full length of the list would place the mover in a position shared
     * by one of the positions in onlyPassable (which is typically filled with friendly units that can be passed
     * through in multi-cell-movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * The full map will only be scanned if scanLimit is 0 or less; for positive scanLimit values this will scan only
     * that distance out from each goal, which can save processing time on maps where only a small part matters.
     * Generally, scanLimit should be significantly greater than length.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param length       the length of the path to calculate
     * @param scanLimit    how many cells away from a goal to actually process; negative to process whole map
     * @param impassable   a Set of impassable PointI2 positions that may change (not constant like walls); can be null
     * @param onlyPassable a Set of PointI2 positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start        the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets      a vararg or array of PointI2 that this will try to pathfind toward
     * @return an ObjectDeque of PointI2 that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ObjectDeque<P> findPath(int length, int scanLimit, Collection<? extends Point2<?>> impassable,
                                       Collection<? extends Point2<?>> onlyPassable, Point2<?> start, Collection<? extends Point2<?>> targets) {
        return findPath(null, length, scanLimit, impassable, onlyPassable, start, targets);
    }

    /**
     * Scans the dungeon using GradientGrid.scan or GradientGrid.partialScan with the listed goals and start
     * point, and returns a list of PointI2 positions (using the current measurement) needed to get closer
     * to the closest reachable goal. The maximum length of the returned list is given by length, which represents
     * movement in a system where a single move can be multiple cells if length is greater than 1 and should usually
     * be 1 in standard roguelikes; if moving the full length of the list would place the mover in a position shared
     * by one of the positions in onlyPassable (which is typically filled with friendly units that can be passed
     * through in multi-cell-movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * The full map will only be scanned if scanLimit is 0 or less; for positive scanLimit values this will scan only
     * that distance out from each goal, which can save processing time on maps where only a small part matters.
     * Generally, scanLimit should be significantly greater than length.
     * <br>
     * This overload takes a buffer parameter, an ObjectDeque of PointI2, that the results will be appended to. If the
     * buffer is null, a new ObjectDeque will be made and appended to. This caches its result in a member field, path,
     * which can be fetched after finding a path and will change with each call to a pathfinding method. Any existing
     * contents of buffer will not affect the path field of this GradientGrid.
     *
     * @param buffer       an existing ObjectDeque of PointI2 that will have the result appended to it (in-place); if null, this will make a new ObjectDeque
     * @param length       the length of the path to calculate
     * @param scanLimit    how many cells away from a goal to actually process; negative to process whole map
     * @param impassable   a Set of impassable PointI2 positions that may change (not constant like walls); can be null
     * @param onlyPassable a Set of PointI2 positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start        the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets      a vararg or array of PointI2 that this will try to pathfind toward
     * @return an ObjectDeque of PointI2 that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ObjectDeque<P> findPath(ObjectDeque<P> buffer, int length, int scanLimit, Collection<? extends Point2<?>> impassable,
                                       Collection<? extends Point2<?>> onlyPassable, Point2<?> start, Collection<? extends Point2<?>> targets) {
        path.clear();
        if (!initialized || length <= 0) {
            cutShort = true;
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }
        if (impassable == null)
            blocked.clear();
        else if(impassable != blocked){
            blocked.clear();
            blocked.addAll(impassable);
        }
        if (onlyPassable != null && length == 1)
            blocked.addAll(onlyPassable);

        resetMap();
        setGoals(targets);
        if (goals.isEmpty()) {
            cutShort = true;
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }
        if (scanLimit <= 0 || scanLimit < length)
            scan(start, blocked);
        else
            partialScan(start, scanLimit, blocked);
        P currentPos = workingEdit(start.x(), start.y());
        float paidLength = 0f;
        rng.setState(start.hashCode(), targets.size());
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            currentPos = currentPos.cpy();
            float best = gradientMap[(int) currentPos.x()][(int) currentPos.y()];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                int adjX = (int)currentPos.x() + dirs[d].deltaX;
                int adjY = (int)currentPos.y() + dirs[d].deltaY;
                if (adjX < 0 || adjY < 0 || adjX >= width || adjY >= height)
                    /* Outside the map */
                    continue;
                if (dirs[d].isDiagonal() && blockingRequirement > 0) // diagonal
                {
                    if ((gradientMap[adjX][(int)currentPos.y()] > FLOOR ? 1 : 0)
                            + (gradientMap[(int)currentPos.x()][adjY] > FLOOR ? 1 : 0)
                            >= blockingRequirement)
                        continue;
                }
                P workPt = workingEdit(adjX, adjY);
                if (gradientMap[adjX][adjY] < best && !blocked.contains(workPt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(workPt)) {
                        best = gradientMap[adjX][adjY];
                        choice = d;
                    }
                }
            }
            int x = (int)currentPos.x();
            int y = (int)currentPos.y();
            if (best >= gradientMap[x][y] || physicalMap[x + dirs[choice].deltaX][y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                if (buffer == null)
                    return new ObjectDeque<>(path);
                else {
                    buffer.addAll(path);
                    return buffer;
                }
            }
            currentPos = currentPos.set(x + dirs[choice].deltaX, y + dirs[choice].deltaY);
            path.add(currentPos);
            paidLength++;
            if (paidLength > length - 1f) {
                if (onlyPassable != null && onlyPassable.contains(currentPos)) {
                    frustration++;
                    blocked.add(currentPos);
                    return findPath(buffer, length, scanLimit, blocked, onlyPassable, start, targets);
                }
                break;
            }
            if (gradientMap[(int)currentPos.x()][(int)currentPos.y()] == 0)
                break;
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        if (buffer == null)
            return new ObjectDeque<>(path);
        else {
            buffer.addAll(path);
            return buffer;
        }
    }

    /**
     * Scans the dungeon using GradientGrid.scan with the listed goals and start point, and returns a deque
     * of P positions (using the current measurement) needed to get closer to a goal, until preferredRange is
     * reached, or further from a goal if the preferredRange has not been met at the current distance.
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param moveLength     the length of the path to calculate
     * @param preferredRange the distance this unit will try to keep from a target
     * @param impassable     a Collection of impassable Point2 positions that may change (not constant like walls); can be null
     * @param onlyPassable   a Collection of Point2 positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start          the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets        a Collection of Point2 that this will try to pathfind toward
     * @return an ObjectDeque of P that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ObjectDeque<P> findAttackPath(int moveLength, int preferredRange, Collection<? extends Point2<?>> impassable,
                                             boolean los,
                                             Collection<? extends Point2<?>> onlyPassable, Point2<?> start,
                                             Collection<? extends Point2<?>> targets) {
        return findAttackPath(moveLength, preferredRange, preferredRange, los, impassable, onlyPassable, start, targets);
    }

    /**
     * Scans the dungeon using GradientGrid.scan with the listed goals and start point, and returns a deque
     * of P positions (using the current measurement) needed to get closer to a goal, until a cell is reached with
     * a distance from a goal that is at least equal to minPreferredRange and no more than maxPreferredRange,
     * which may go further from a goal if the minPreferredRange has not been met at the current distance.
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param moveLength        the length of the path to calculate
     * @param minPreferredRange the (inclusive) lower bound of the distance this unit will try to keep from a target
     * @param maxPreferredRange the (inclusive) upper bound of the distance this unit will try to keep from a target
     * @param impassable        a Collection of impassable Point2 positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Collection of Point2 positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets           a Collection of Point2 that this will try to pathfind toward
     * @return an ObjectDeque of P that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ObjectDeque<P> findAttackPath(int moveLength, int minPreferredRange, int maxPreferredRange, boolean los,
                                               Collection<? extends Point2<?>> impassable, Collection<? extends Point2<?>> onlyPassable,
                                               Point2<?> start, Collection<? extends Point2<?>> targets) {
        return findAttackPath(null, moveLength, minPreferredRange, maxPreferredRange, los, impassable, onlyPassable, start, targets);
    }

    /**
     * Scans the dungeon using GradientGrid.scan with the listed goals and start point, and returns a deque
     * of P positions (using the current measurement) needed to get closer to a goal, until a cell is reached with
     * a distance from a goal that is at least equal to minPreferredRange and no more than maxPreferredRange,
     * which may go further from a goal if the minPreferredRange has not been met at the current distance.
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell. In most roguelikes where
     * movement happens one cell at a time, moveLength should be 1; if it is higher then the path will prefer getting
     * further away from the target (using up most or all of moveLength) while minPreferredRange and maxPreferredRange
     * can be satisfied. This does ensure a pathfinder with a ranged weapon stays far from melee range, but it may not
     * be the expected behavior because it will try to find the best path rather than the shortest it can attack from.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * <br>
     * This overload takes a buffer parameter, an ObjectDeque of P, that the results will be appended to. If the
     * buffer is null, a new ObjectDeque will be made and appended to. This caches its result in a member field, path,
     * which can be fetched after finding a path and will change with each call to a pathfinding method. Any existing
     * contents of buffer will not affect the path field of this GradientGrid.
     *
     * @param buffer            an existing ObjectDeque of P that will have the result appended to it (in-place); if null, this will make a new ObjectDeque
     * @param moveLength        the length of the path to calculate; almost always, the pathfinder will try to use this length in full to obtain the best range
     * @param minPreferredRange the (inclusive) lower bound of the distance this unit will try to keep from a target
     * @param maxPreferredRange the (inclusive) upper bound of the distance this unit will try to keep from a target
     * @param los               if true, this will only try to move toward cells with an unobstructed line-of-sight to the target
     * @param impassable        a Set of impassable PointI2 positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of PointI2 positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets           a vararg or array of PointI2 that this will try to pathfind toward
     * @return an ObjectDeque of PointI2 that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ObjectDeque<P> findAttackPath(ObjectDeque<P> buffer, int moveLength,
                                               int minPreferredRange, int maxPreferredRange, boolean los,
                                               Collection<? extends Point2<?>> impassable, Collection<? extends Point2<?>> onlyPassable,
                                               Point2<?> start, Collection<? extends Point2<?>> targets) {
        if (!initialized || moveLength <= 0) {
            cutShort = true;
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }
        if (minPreferredRange < 0) minPreferredRange = 0;
        if (maxPreferredRange < minPreferredRange) maxPreferredRange = minPreferredRange;
        path.clear();
        if (impassable == null)
            blocked.clear();
        else if(impassable != blocked) {
            blocked.clear();
            blocked.addAll(impassable);
        }
        if (onlyPassable != null && moveLength == 1)
            blocked.addAll(onlyPassable);

        resetMap();
        for (Point2<?> goal : targets) {
            setGoal(goal);
        }
        if (goals.isEmpty()) {
            cutShort = true;
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }

        GridMetric mess = measurement;
        if (measurement == GridMetric.EUCLIDEAN) {
            measurement = GridMetric.CHEBYSHEV;
        }
        scan(null, blocked);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
        }
        goals.clear();

        for (int x = 0; x < width; x++) {
            CELL:
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == WALL || gradientMap[x][y] == DARK)
                    continue;
                if (gradientMap[x][y] >= minPreferredRange && gradientMap[x][y] <= maxPreferredRange) {
                    workRay.a = workRay.a.set(x, y);
                    for (Point2<?> goal : targets) {
                        if (!los || !Ortho2DRaycastCollisionDetector.collides(
                                workRay.set(workRay.a, workRay.b.set(goal.x(), goal.y())), this::wallQuery)) {
                            setGoal(x, y);
                            gradientMap[x][y] = 0;
                            continue CELL;
                        }
                    }
                    gradientMap[x][y] = FLOOR;
                } else
                    gradientMap[x][y] = FLOOR;
            }
        }
        measurement = mess;
        scan(null, blocked);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
        }
        if (gradientMap[(int)start.x()][(int)start.y()] <= 0f) {
            cutShort = false;
            frustration = 0;
            goals.clear();
            if (buffer == null)
                return new ObjectDeque<>(path);
            else {
                buffer.addAll(path);
                return buffer;
            }

        }
        P currentPos = workingEdit(start);
        float paidLength = 0f;
        rng.setState(start.hashCode(), targets.size());
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            currentPos = currentPos.cpy();
            float best = gradientMap[(int) currentPos.x()][(int) currentPos.y()];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                int adjX = (int)currentPos.x() + dirs[d].deltaX;
                int adjY = (int)currentPos.y() + dirs[d].deltaY;
                if (adjX < 0 || adjY < 0 || adjX >= width || adjY >= height)
                    /* Outside the map */
                    continue;
                if (dirs[d].isDiagonal() && blockingRequirement > 0) // diagonal
                {
                    if ((gradientMap[adjX][(int)currentPos.y()] > FLOOR ? 1 : 0)
                            + (gradientMap[(int)currentPos.x()][adjY] > FLOOR ? 1 : 0)
                            >= blockingRequirement)
                        continue;
                }
                P workPt = workingEdit(adjX, adjY);
                if (gradientMap[adjX][adjY] < best && !blocked.contains(workPt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(workPt)) {
                        best = gradientMap[adjX][adjY];
                        choice = d;
                    }
                }
            }
            int x = (int)currentPos.x();
            int y = (int)currentPos.y();
            if (best >= gradientMap[x][y] || physicalMap[x + dirs[choice].deltaX][y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                if (buffer == null)
                    return new ObjectDeque<>(path);
                else {
                    buffer.addAll(path);
                    return buffer;
                }
            }
            currentPos = currentPos.set(x + dirs[choice].deltaX, y + dirs[choice].deltaY);
            path.add(currentPos);
            paidLength++;
            if (paidLength > moveLength - 1f) {
                if (onlyPassable != null && onlyPassable.contains(currentPos)) {
                    frustration++;
                    blocked.add(currentPos);
                    return findAttackPath(buffer, moveLength, minPreferredRange, maxPreferredRange, los, blocked,
                            onlyPassable, start, targets);
                }
                break;
            }
            if (gradientMap[(int)currentPos.x()][(int)currentPos.y()] == 0)
                break;
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        if (buffer == null)
            return new ObjectDeque<>(path);
        else {
            buffer.addAll(path);
            return buffer;
        }
    }

    /**
     * Scans the dungeon using GradientGrid.scan() with the listed fearSources and start point, and returns a list
     * of PointI2 positions (using Manhattan distance) needed to get further from the closest fearSources, meant
     * for running away. The maximum length of the returned list is given by length; if moving the full
     * length of the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a fearSource overlapping one. The preferLongerPaths parameter
     * is meant to be tweaked and adjusted; higher values should make creatures prefer to escape out of
     * doorways instead of hiding in the closest corner, and a value of 1.2f should be typical for many maps.
     * The parameters preferLongerPaths, impassable, and the varargs used for fearSources will be cached, and
     * any subsequent calls that use the same values as the last values passed will avoid recalculating
     * unnecessary scans.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param length            the length of the path to calculate
     * @param preferLongerPaths Set this to 1.2f if you aren't sure; it will probably need tweaking for different maps.
     * @param impassable        a Set of impassable PointI2 positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of PointI2 positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param fearSources       a vararg or array of PointI2 positions to run away from
     * @return an ObjectDeque of PointI2 that will contain the locations of this creature as it goes away from fear sources. Copy of path.
     */
    public ObjectDeque<P> findFleePath(int length, float preferLongerPaths, Collection<? extends Point2<?>> impassable,
                                           Collection<? extends Point2<?>> onlyPassable, Point2<?> start, Collection<Point2<?>> fearSources) {
        return findFleePath(null, length, -1, preferLongerPaths, impassable, onlyPassable, start, fearSources);
    }

    /**
     * Scans the dungeon using GradientGrid.scan or GradientGrid.partialScan with the listed fearSources and start
     * point, and returns a list of PointI2 positions (using this GradientGrid's metric) needed to get further from
     * the closest fearSources, meant for running away. The maximum length of the returned list is given by length,
     * which represents movement in a system where a single move can be multiple cells if length is greater than 1 and
     * should usually be 1 in standard roguelikes; if moving the full length of the list would place the mover in a
     * position shared by one of the positions in onlyPassable (which is typically filled with friendly units that can
     * be passed through in multi-cell-movement scenarios), it will recalculate a move so that it does not pass into
     * that cell. The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a fearSource overlapping one. The preferLongerPaths parameter
     * is meant to be tweaked and adjusted; higher values should make creatures prefer to escape out of
     * doorways instead of hiding in the closest corner, and a value of 1.2f should be typical for many maps.
     * The parameters preferLongerPaths, impassable, and the varargs used for fearSources will be cached, and
     * any subsequent calls that use the same values as the last values passed will avoid recalculating
     * unnecessary scans. However, scanLimit is not cached; if you use scanLimit then it is assumed you are using some
     * value for it that shouldn't change relative to the other parameters (like twice the length).
     * The full map will only be scanned if scanLimit is 0 or less; for positive scanLimit values this will scan only
     * that distance out from each goal, which can save processing time on maps where only a small part matters.
     * Generally, scanLimit should be significantly greater than length.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param length            the length of the path to calculate
     * @param scanLimit         how many steps away from a fear source to calculate; negative scans the whole map
     * @param preferLongerPaths Set this to 1.2f if you aren't sure; it will probably need tweaking for different maps.
     * @param impassable        a Set of impassable PointI2 positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of PointI2 positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param fearSources       a vararg or array of PointI2 positions to run away from
     * @return an ObjectDeque of PointI2 that will contain the locations of this creature as it goes away from fear sources. Copy of path.
     */
    public ObjectDeque<P> findFleePath(int length, int scanLimit, float preferLongerPaths, Collection<? extends Point2<?>> impassable,
                                           Collection<? extends Point2<?>> onlyPassable, Point2<?> start, Collection<Point2<?>> fearSources) {
        return findFleePath(null, length, scanLimit, preferLongerPaths, impassable, onlyPassable, start, fearSources);
    }

    /**
     * Scans the dungeon using GradientGrid.scan or GradientGrid.partialScan with the listed fearSources and start
     * point, and returns a list of PointI2 positions (using this GradientGrid's metric) needed to get further from
     * the closest fearSources, meant for running away. The maximum length of the returned list is given by length,
     * which represents movement in a system where a single move can be multiple cells if length is greater than 1 and
     * should usually be 1 in standard roguelikes; if moving the full length of the list would place the mover in a
     * position shared by one of the positions in onlyPassable (which is typically filled with friendly units that can
     * be passed through in multi-cell-movement scenarios), it will recalculate a move so that it does not pass into
     * that cell. The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a fearSource overlapping one. The preferLongerPaths parameter
     * is meant to be tweaked and adjusted; higher values should make creatures prefer to escape out of
     * doorways instead of hiding in the closest corner, and a value of 1.2f should be typical for many maps.
     * The parameters preferLongerPaths, impassable, and the varargs used for fearSources will be cached, and
     * any subsequent calls that use the same values as the last values passed will avoid recalculating
     * unnecessary scans. However, scanLimit is not cached; if you use scanLimit then it is assumed you are using some
     * value for it that shouldn't change relative to the other parameters (like twice the length).
     * The full map will only be scanned if scanLimit is 0 or less; for positive scanLimit values this will scan only
     * that distance out from each goal, which can save processing time on maps where only a small part matters.
     * Generally, scanLimit should be significantly greater than length.
     * <br>
     * This overload takes a buffer parameter, an ObjectDeque of PointI2, that the results will be appended to. If the
     * buffer is null, a new ObjectDeque will be made and appended to. This caches its result in a member field, path,
     * which can be fetched after finding a path and will change with each call to a pathfinding method. Any existing
     * contents of buffer will not affect the path field of this GradientGrid.
     *
     * @param buffer            an existing ObjectDeque of PointI2 that will have the result appended to it (in-place); if null, this will make a new ObjectDeque
     * @param length            the length of the path to calculate
     * @param scanLimit         how many steps away from a fear source to calculate; negative scans the whole map
     * @param preferLongerPaths Set this to 1.2f if you aren't sure; it will probably need tweaking for different maps.
     * @param impassable        a Set of impassable PointI2 positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of PointI2 positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param fearSources       a vararg or array of PointI2 positions to run away from
     * @return an ObjectDeque of PointI2 that will contain the locations of this creature as it goes away from fear sources. Copy of path.
     */
    public ObjectDeque<P> findFleePath(ObjectDeque<P> buffer, int length, int scanLimit, float preferLongerPaths,
                                             Collection<? extends Point2<?>> impassable,
                                           Collection<? extends Point2<?>> onlyPassable, Point2<?> start, Collection<Point2<?>> fearSources) {
        if (!initialized || length <= 0) {
            cutShort = true;
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }
        path.clear();
        if (fearSources == null || fearSources.isEmpty()) {
            cutShort = true;
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }

        if (impassable == null)
            blocked.clear();
        else if(impassable != blocked){
            blocked.clear();
            blocked.addAll(impassable);
        }
        if (onlyPassable != null && length == 1)
            blocked.addAll(onlyPassable);
        if (preferLongerPaths == cachedLongerPaths && blocked.equals(cachedImpassable) &&
            cachedFearSources != null && cachedFearSources.equals(fearSources)) {
            gradientMap = cachedFleeMap;
        } else {
            cachedLongerPaths = preferLongerPaths;
            cachedImpassable.clear();
            cachedImpassable.addAll(blocked);
            if(cachedFearSources == null) {
                cachedFearSources = new ObjectSet<>(fearSources);
            }
            else {
                cachedFearSources.clear();
                cachedFearSources.ensureCapacity(fearSources.size());
                cachedFearSources.addAll(fearSources);
            }
            resetMap();
            setGoals(fearSources);
            if (goals.isEmpty()) {
                cutShort = true;
                if (buffer == null)
                    return new ObjectDeque<>();
                else {
                    return buffer;
                }
            }

            if (scanLimit <= 0 || scanLimit < length)
                cachedFleeMap = scan(blocked);
            else
                cachedFleeMap = partialScan(scanLimit, blocked);


            for (int x = 0; x < gradientMap.length; x++) {
                for (int y = 0; y < gradientMap[x].length; y++) {
                    gradientMap[x][y] *= (gradientMap[x][y] >= FLOOR) ? 1f : -preferLongerPaths;
                }
            }

            if (scanLimit <= 0 || scanLimit < length) {
                scan(null, blocked, true);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        if (gradientMap[x][y] == FLOOR) {
                            gradientMap[x][y] = DARK;
                        }
                    }
                    System.arraycopy(gradientMap[x], 0, cachedFleeMap[x], 0, height);
                }
            } else
                cachedFleeMap = partialScan(scanLimit, blocked);
        }
        P currentPos = workingEdit(start);
        float paidLength = 0f;
        rng.setState(start.hashCode(), fearSources.size());

        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            currentPos = currentPos.cpy();

            float best = gradientMap[(int)currentPos.x()][(int)currentPos.y()];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                int adjX = (int)currentPos.x() + dirs[d].deltaX;
                int adjY = (int)currentPos.y() + dirs[d].deltaY;
                if (adjX < 0 || adjY < 0 || adjX >= width || adjY >= height)
                    /* Outside the map */
                    continue;
                if (dirs[d].isDiagonal() && blockingRequirement > 0) // diagonal
                {
                    if ((gradientMap[adjX][(int)currentPos.y()] > FLOOR ? 1 : 0)
                            + (gradientMap[(int)currentPos.x()][adjY] > FLOOR ? 1 : 0)
                            >= blockingRequirement)
                        continue;
                }
                P workPt = workingEdit(adjX, adjY);
                if (gradientMap[adjX][adjY] < best && !blocked.contains(workPt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(workPt)) {
                        best = gradientMap[adjX][adjY];
                        choice = d;
                    }
                }
            }
            if (best >= gradientMap[(int)start.x()][(int)start.y()] || physicalMap[(int)currentPos.x() + dirs[choice].deltaX][(int)currentPos.y() + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                if (buffer == null)
                    return new ObjectDeque<>(path);
                else {
                    buffer.addAll(path);
                    return buffer;
                }
            }
            currentPos = currentPos.set(currentPos.x() + dirs[choice].deltaX, currentPos.y() + dirs[choice].deltaY);
            if (!path.isEmpty()) {
                Point2<?> last = path.peekLast();
                if (gradientMap[(int)last.x()][(int)last.y()] <= gradientMap[(int)currentPos.x()][(int)currentPos.y()])
                    break;
            }
            path.add(currentPos);
            paidLength++;
            if (paidLength > length - 1f) {
                if (onlyPassable != null && onlyPassable.contains(currentPos)) {
                    frustration++;
                    blocked.add(currentPos);
                    return findFleePath(buffer, length, scanLimit, preferLongerPaths, blocked, onlyPassable, start, fearSources);
                }
                break;
            }
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        if (buffer == null)
            return new ObjectDeque<>(path);
        else {
            buffer.addAll(path);
            return buffer;
        }
    }

    /**
     * When you can control how often the (relatively time-intensive) scan() method is called, but may need simple paths
     * very frequently (such as for a path that follows the mouse), you can use this method to reduce the amount of work
     * needed to find paths. Needs scan() or partialScan() to already be called and at least one goal to already be set,
     * and does not restrict the length of the path or behave as if the pathfinder has allies or enemies.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param target the target cell
     * @return an ObjectDeque of PointI2 that make up the best path. Copy of path.
     */
    public ObjectDeque<P> findPathPreScanned(Point2<?> target) {
        return findPathPreScanned(null, target);
    }

    /**
     * When you can control how often the (relatively time-intensive) scan() method is called, but may need simple paths
     * very frequently (such as for a path that follows the mouse), you can use this method to reduce the amount of work
     * needed to find paths. Needs scan() or partialScan() to already be called and at least one goal to already be set,
     * and does not restrict the length of the path or behave as if the pathfinder has allies or enemies.
     * <br>
     * This overload takes a buffer parameter, an ObjectDeque of PointI2, that the results will be appended to. If the
     * buffer is null, a new ObjectDeque will be made and appended to. This caches its result in a member field, path,
     * which can be fetched after finding a path and will change with each call to a pathfinding method. Any existing
     * contents of buffer will not affect the path field of this GradientGrid.
     *
     * @param buffer an existing ObjectDeque of PointI2 that will have the result appended to it (in-place); if null, this will make a new ObjectDeque
     * @param target the target cell
     * @return an ObjectDeque of PointI2 that make up the best path, appended to buffer (if non-null)
     */
    public ObjectDeque<P> findPathPreScanned(ObjectDeque<P> buffer, Point2<?> target) {
        path.clear();
        if (!initialized || goals == null || goals.isEmpty()) {
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }
        P currentPos = acquire(target);
        if (gradientMap[(int)currentPos.x()][(int)currentPos.y()] <= FLOOR)
            path.add(currentPos);
        else {
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }
        rng.setState(target.hashCode(), 0x9E3779B97F4A7C15L);
        do {
            currentPos = currentPos.cpy();
            float best = gradientMap[(int)currentPos.x()][(int)currentPos.y()];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                int adjX = (int)currentPos.x() + dirs[d].deltaX;
                int adjY = (int)currentPos.y() + dirs[d].deltaY;
                if (adjX < 0 || adjY < 0 || adjX >= width || adjY >= height)
                    /* Outside the map */
                    continue;
                if (dirs[d].isDiagonal() && blockingRequirement > 0) // diagonal
                {
                    if ((gradientMap[adjX][(int)currentPos.y()] > FLOOR ? 1 : 0)
                            + (gradientMap[(int)currentPos.x()][adjY] > FLOOR ? 1 : 0)
                            >= blockingRequirement)
                        continue;
                }
                P workPt = workingEdit(adjX, adjY);
                if (gradientMap[adjX][adjY] < best && !blocked.contains(workPt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(workPt)) {
                        best = gradientMap[adjX][adjY];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[(int)currentPos.x()][(int)currentPos.y()] || physicalMap[(int) (currentPos.x() + dirs[choice].deltaX)][(int) (currentPos.y() + dirs[choice].deltaY)] > FLOOR) {
                cutShort = true;
                if (buffer == null)
                    return new ObjectDeque<>(path);
                else {
                    buffer.addAll(path);
                    return buffer;
                }
            }
            currentPos = currentPos.set(currentPos.x() + dirs[choice].deltaX, currentPos.y() + dirs[choice].deltaY);
            path.addFirst(currentPos);

        } while (gradientMap[(int)currentPos.x()][(int)currentPos.y()] != 0);
        cutShort = false;
        if (buffer == null)
            return new ObjectDeque<>(path);
        else {
            buffer.addAll(path);
            return buffer;
        }

    }

    public int getMappedCount() {
        return mappedCount;
    }

    /**
     * If you want obstacles present in orthogonal cells to prevent pathfinding along the diagonal between them, this
     * can be used to make thin diagonal walls non-viable to move through, or even to prevent diagonal movement if any
     * one obstacle is orthogonally adjacent to both the start and target cell of a diagonal move. If you haven't set
     * this yet, then the default is 2.
     * <br>
     * If this is 0, as a special case no orthogonal obstacles will block diagonal moves.
     * <br>
     * If this is 1, having one orthogonal obstacle adjacent to both the current cell and the cell the pathfinder is
     * trying to diagonally enter will block diagonal moves. This generally blocks movement around corners, the "hard
     * corner" rule used in some games.
     * <br>
     * If this is 2 (the default), having two orthogonal obstacles adjacent to both the current cell and the cell the
     * pathfinder is trying to diagonally enter will block diagonal moves. As an example, if there is a wall to the
     * north and a wall to the east, then the pathfinder won't be able to move northeast even if there is a floor there.
     *
     * @return the current level of blocking required to stop a diagonal move
     */
    public int getBlockingRequirement() {
        return blockingRequirement;
    }

    /**
     * If you want obstacles present in orthogonal cells to prevent pathfinding along the diagonal between them, this
     * can be used to make thin diagonal walls non-viable to move through, or even to prevent diagonal movement if any
     * one obstacle is orthogonally adjacent to both the start and target cell of a diagonal move. If you haven't set
     * this yet, then the default is 2.
     * <br>
     * If this is 0, as a special case no orthogonal obstacles will block diagonal moves.
     * <br>
     * If this is 1, having one orthogonal obstacle adjacent to both the current cell and the cell the pathfinder is
     * trying to diagonally enter will block diagonal moves. This generally blocks movement around corners, the "hard
     * corner" rule used in some games.
     * <br>
     * If this is 2 (the default), having two orthogonal obstacles adjacent to both the current cell and the cell the
     * pathfinder is trying to diagonally enter will block diagonal moves. As an example, if there is a wall to the
     * north and a wall to the east, then the pathfinder won't be able to move northeast even if there is a floor there.
     *
     * @param blockingRequirement the desired level of blocking required to stop a diagonal move
     */
    public void setBlockingRequirement(int blockingRequirement) {
        this.blockingRequirement = Math.min(Math.max(blockingRequirement, 0), 2);
    }

    private void appendDirToShuffle(Random rng) {
        switch (measurement){
            case MANHATTAN:
                System.arraycopy(Direction.CARDINALS, 0, dirs, 0, 4);
                for (int i = 3; i > 0; i--) {
                    // equivalent to rng.nextInt(i+1), but here it can omit an unnecessary check and be inlined.
                    final int r = (int) ((i + 1) * (rng.nextLong() & 0xFFFFFFFFL) >>> 32);
                    Direction t = dirs[r];
                    dirs[r] = dirs[i];
                    dirs[i] = t;
                }

                dirs[4] = Direction.NONE;
                break;
            case CHEBYSHEV:
                System.arraycopy(Direction.OUTWARDS, 0, dirs, 0, 8);
                for (int i = 7; i > 0; i--) {
                    // equivalent to rng.nextInt(i+1), but here it can omit an unnecessary check and be inlined.
                    final int r = (int) ((i + 1) * (rng.nextLong() & 0xFFFFFFFFL) >>> 32);
                    Direction t = dirs[r];
                    dirs[r] = dirs[i];
                    dirs[i] = t;
                }
                dirs[8] = Direction.NONE;
                break;
            default:
                System.arraycopy(Direction.OUTWARDS, 0, dirs, 0, 8);
                for (int i = 3; i > 0; i--) {
                    // equivalent to rng.nextInt(i+1), but here it can omit an unnecessary check and be inlined.
                    final int r = (int) ((i + 1) * (rng.nextLong() & 0xFFFFFFFFL) >>> 32);
                    Direction t = dirs[r];
                    dirs[r] = dirs[i];
                    dirs[i] = t;
                }
                for (int j = 7; j > 4; j--) {
                    // equivalent to 4+rng.nextInt(j-3), but here it can omit an unnecessary check and be inlined.
                    final int r = 4 + (int) ((j - 3) * (rng.nextLong() & 0xFFFFFFFFL) >>> 32);
                    Direction t = dirs[r];
                    dirs[r] = dirs[j];
                    dirs[j] = t;
                }
                dirs[8] = Direction.NONE;
        }
    }

    public static boolean isWithin(int x, int y, int width, int height) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public static boolean isWithin(Point2<?> pt, int width, int height) {
        return pt.x() >= 0 && pt.x() < width && pt.y() >= 0 && pt.y() < height;
    }

    private static final float[][] emptyFloats2D = new float[0][0];

    /**
     * Gets a copy of the 2D float array, source, that has the same data but shares no references with source.
     *
     * @param source a 2D float array
     * @return a copy of source, or null if source is null
     */
    public static float[][] copy(float[][] source) {
        if (source == null)
            return null;
        if (source.length < 1)
            return emptyFloats2D;
        float[][] target = new float[source.length][];
        for (int i = 0; i < source.length; i++) {
            final int len = source[i].length;
            target[i] = new float[len];
            System.arraycopy(source[i], 0, target[i], 0, len);
        }
        return target;
    }

    private boolean wallQuery(int gx, int gy) {
        return isWithin(gx, gy, width, height) && physicalMap[gx][gy] == WALL;
    }
}
