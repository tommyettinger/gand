/*
 * Copyright (c) 2024 See AUTHORS file.
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

import com.github.tommyettinger.gand.ds.ObjectOrderedSet;
import com.github.tommyettinger.gand.ds.ObjectSet;
import com.github.tommyettinger.gdcrux.PointI2;
import com.github.tommyettinger.gand.utils.FlowRandom;

import java.util.Collection;
import java.util.Random;

/**
 * This generates orthogonally-connected paths of {@link PointI2} that meander through an area;
 * this won't ever generate paths that cross themselves.
 * <br>
 * This randomly generates a graph with only some valid edges easily connected, then solves it with
 * {@link com.github.tommyettinger.gand.algorithms.Algorithms#findShortestPath(Object, Object)}. If an edge
 * is "easily connected", it has a cost of 1, and if it isn't, it has a cost of 1024, which strongly discourages
 * the pathfinder from traversing that connection (but still permits it if only one path is an option).
 * The "twisted-ness" of the path can be decreased by setting {@code relaxation} to a value greater than 0.
 * The relaxation only needs to be increased a little above 0 to start having an effect; most values greater than 0.5
 * will look the same as 1.0 (mostly optimal paths, and not very "twisty").
 */
public class TwistedLineI2 {
   
    public Random random;
   
    public Int2UndirectedGraph graph;
   
    public transient final Path<PointI2> lastPath;

    private transient final PointI2[] dirs = new PointI2[]{
            new PointI2(1, 0), new PointI2(0, 1), new PointI2(-1, 0), new PointI2(0, -1)
    };

    private transient final ObjectOrderedSet<PointI2> frontier = new ObjectOrderedSet<>();
    private transient final ObjectSet<PointI2> done = new ObjectSet<>();

    private transient final PointI2 tempPt = new PointI2();

    /**
     * You probably don't want this constructor; use {@link #TwistedLineI2(Random, PointI2[], float)} instead.
     */
    public TwistedLineI2() {
        this(null, new PointI2[]{new PointI2(0, 0), new PointI2(1, 0)}, 0f);
    }

    /**
     * Builds a TwistedLineI2 and calls {@link #reinitialize(PointI2[], float)} using the given traversable points.
     * You can get a line between two points using {@link #line(PointI2, PointI2)} after this.
     * @param random any Random or subclass; if null, this will create a new {@link FlowRandom}
     * @param traversable an array of points that this line is permitted to travel through
     */
    public TwistedLineI2(Random random, PointI2[] traversable) {
        this(random, traversable, 0f);
    }

    /**
     * Builds a TwistedLineI2 and calls {@link #reinitialize(PointI2[], float)} using the given traversable points.
     * You can get a line between two points using {@link #line(PointI2, PointI2)} after this. How "twisty" the line
     * will be can be configured by changing {@code relaxation}.
     * @param random any Random or subclass; if null, this will create a new {@link FlowRandom}
     * @param traversable an array of points that this line is permitted to travel through
     * @param relaxation between 0.0 and 1.0, with lower values being very "twisty" and higher values being closer to straight lines
     */
    public TwistedLineI2(Random random, PointI2[] traversable, float relaxation) {
        graph = new Int2UndirectedGraph();
        this.random = random == null ? new FlowRandom() : random;
        lastPath = new Path<>();
        reinitialize(traversable, relaxation);
    }

    /**
     * This sets up a random maze as a {@link Int2UndirectedGraph} so a path can be found, using the given array of PointI2
     * to represent which cells on a 2D grid can actually be traversed easily (and so can be used in a random path).
     * You can call this after construction to change the paths this can find.
     * @param traversable an array of PointI2 points that are valid vertices this can travel through
     * @param relaxation between 0.0 and 1.0, with lower values being very "twisty" and higher values being closer to straight lines
     */
    public void reinitialize(PointI2[] traversable, float relaxation) {
        graph.removeAllVertices();
        graph.addVertices(traversable);

        PointI2 start = traversable[random.nextInt(traversable.length)];

        frontier.clear();
        done.clear();
        frontier.add(start);

        PointI2 v;

        while (!frontier.isEmpty()) {
            float cost = 1f;
            PointI2 p = frontier.getAt(frontier.size() - 1);
            if(random.nextFloat() >= relaxation) {
                shuffle(dirs);
                for (int j = 0; j < dirs.length; j++) {
                    PointI2 dir = dirs[j];
                    tempPt.set(p).add(dir);
                    if ((v = graph.getStoredVertex(tempPt)) != null) {
                        if (!done.contains(v) && frontier.add(v)) {
                            graph.addEdge(p, v, cost);
                            cost = 1024f;
                        }
                    }
                }
            } else {
                for (int j = 0; j < dirs.length; j++) {
                    PointI2 dir = dirs[j];
                    tempPt.set(p).add(dir);
                    if ((v = graph.getStoredVertex(tempPt)) != null) {
                        if (!done.contains(v)) {
                            frontier.add(v);
                            graph.addEdge(p, v, 1f);
                        }
                    }
                }
            }
            done.add(p);
            frontier.remove(p);
        }
    }


    /**
     * This sets up a random maze as an {@link Int2UndirectedGraph} so a path can be
     * found. You can call this after construction to change the paths this can find.
     * @param relaxation between 0.0 and 1.0, with lower values being very "twisty" and higher values being closer to straight lines
     */
    public void randomize(float relaxation) {
        if(graph.getVertices().isEmpty()) return;
        graph.removeAllEdges();

        frontier.clear();
        done.clear();
        frontier.add(graph.getVertices().iterator().next());

        PointI2 v;

        while (!frontier.isEmpty()) {
            float cost = 1f;
            PointI2 p = frontier.getAt(frontier.size() - 1);
            if(random.nextFloat() >= relaxation) {
                shuffle(dirs);
                for (int j = 0; j < dirs.length; j++) {
                    PointI2 dir = dirs[j];
                    tempPt.set(p).add(dir);
                    if ((v = graph.getStoredVertex(tempPt)) != null) {
                        if (!done.contains(v) && frontier.add(v)) {
                            graph.addEdge(p, v, cost);
                            cost = 1024f;
                        }
                    }
                }
            } else {
                for (int j = 0; j < dirs.length; j++) {
                    PointI2 dir = dirs[j];
                    tempPt.set(p).add(dir);
                    if ((v = graph.getStoredVertex(tempPt)) != null) {
                        if (!done.contains(v)) {
                            frontier.add(v);
                            graph.addEdge(p, v, 1f);
                        }
                    }
                }
            }
            done.add(p);
            frontier.remove(p);
        }
    }

    public Path<PointI2> line(PointI2 start, PointI2 end) {
        lastPath.clear();
        lastPath.addAll(graph.algorithms().findShortestPath(start, end, PointI2::dst));
        return lastPath;
    }

    /**
     * Finds a twisted line from {@code start} to {@code end} and appends the points of that
     * line into {@code path}. This does not remove any existing points from {@code path},
     * so you may want to {@link Collection#clear()} it first.
     * @param path will be appended to; will not be cleared first
     * @param start the start point to include in the path first
     * @param end the end point to include in the path last
     * @return {@code path}, after modifications
     */
    public Collection<PointI2> line(Collection<PointI2> path, PointI2 start, PointI2 end) {
        path.addAll(graph.algorithms().findShortestPath(start, end, PointI2::dst));
        return path;
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random == null ? new FlowRandom() : random;
    }

    /**
     * Gets the last path this found, which may be empty. This returns the same reference to any path this produces,
     * and the path is cleared when a new twisted line is requested. You probably want to copy the contents of this path
     * into another Path, Deque, or List if you want to keep its contents.
     * @return the most recent path of PointI2, as a Path (essentially an ObjectDeque), this found.
     */
    public Path<PointI2> getLastPath() {
        return lastPath;
    }

    protected <T> void shuffle (T[] items) {
        int offset = 0;
        int length = items.length;
        for (int i = offset + length - 1; i > offset; i--) {
            int ii = offset + random.nextInt(i + 1 - offset);
            T temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }
    }

}
