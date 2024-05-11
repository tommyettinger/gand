/*
MIT License

Copyright (c) 2020 earlygrey

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.github.tommyettinger.gand;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.github.tommyettinger.cringe.ContinuousNoise;
import com.github.tommyettinger.cringe.FoamNoise;
import com.github.tommyettinger.crux.Point2;
import com.github.tommyettinger.crux.Point3;
import com.github.tommyettinger.gand.points.PointF2;
import com.github.tommyettinger.gand.points.PointF3;
import com.github.tommyettinger.gand.points.PointI2;

public class TestUtils {
    public static Graph<Vector2> makeGridGraph(Graph<Vector2> graph, int n) {

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Vector2 v = new Vector2(i, j);
                graph.addVertex(v);
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i < n - 1) {
                    Vector2 v1 = new Vector2(i, j), v2 = new Vector2(i + 1, j);
                    graph.addEdge(v1, v2, v1.dst(v2));
                    if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                }
                if (j < n - 1) {
                    Vector2 v1 = new Vector2(i, j), v2 = new Vector2(i, j + 1);
                    graph.addEdge(v1, v2, v1.dst(v2));
                    if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                }
            }
        }
        return graph;
    }

    public static Graph<PointI2> makeGridGraphI2(Graph<PointI2> graph, int n) {

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                PointI2 v = new PointI2(i, j);
                graph.addVertex(v);
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i < n - 1) {
                    PointI2 v1 = new PointI2(i, j), v2 = new PointI2(i + 1, j);
                    graph.addEdge(v1, v2, v1.dst(v2));
                    if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                }
                if (j < n - 1) {
                    PointI2 v1 = new PointI2(i, j), v2 = new PointI2(i, j + 1);
                    graph.addEdge(v1, v2, v1.dst(v2));
                    if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                }
            }
        }
        return graph;
    }

    public static Graph<PointF2> makeGridGraphF2(Graph<PointF2> graph, int n) {

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                PointF2 v = new PointF2(i, j);
                graph.addVertex(v);
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i < n - 1) {
                    PointF2 v1 = new PointF2(i, j), v2 = new PointF2(i + 1, j);
                    graph.addEdge(v1, v2, v1.dst(v2));
                    if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                }
                if (j < n - 1) {
                    PointF2 v1 = new PointF2(i, j), v2 = new PointF2(i, j + 1);
                    graph.addEdge(v1, v2, v1.dst(v2));
                    if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                }
            }
        }
        return graph;
    }

    public static Graph<PointF3> makeGridGraphF3(Graph<PointF3> graph, int n) {

        long newSeed = MathUtils.random.nextLong();
        MathUtils.random.setSeed(123456789L);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    if ((i & j & k & 1) == 0 || MathUtils.randomBoolean(0.3f))
                        graph.addVertex(new PointF3(i, j, k));
                }
            }
        }

        PointF3 v1 = new PointF3(), v2 = new PointF3();
        for (int i = 0; i < n; i++) {
            v1.x = i;
            for (int j = 0; j < n; j++) {
                v1.y = j;
                for (int k = 0; k < n; k++) {
                    v1.z = k;
                    if (!graph.contains(v1))
                        continue;
                    if (i < n - 1) {
                        v2.set(i + 1, j, k);
                        if (graph.contains(v2)) {
                            graph.addEdge(v1, v2, v1.dst(v2));
                            if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                        }
                    }
                    if (j < n - 1) {
                        v2.set(i, j + 1, k);
                        if (graph.contains(v2)) {
                            graph.addEdge(v1, v2, v1.dst(v2));
                            if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                        }
                    }
                    if (k < n - 1) {
                        v2.set(i, j, k + 1);
                        if (graph.contains(v2)) {
                            graph.addEdge(v1, v2, v1.dst(v2));
                            if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                        }
                    }
                }
            }
        }
        MathUtils.random.setSeed(newSeed);
        return graph;
    }

    public static <V extends Point2<V>> Graph<V> makeGridGraph2D(Graph<V> graph, int sideLength, V basis) {
        long newSeed = MathUtils.random.nextLong();
        MathUtils.random.setSeed(123456789L);

        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                if ((i & j & 1) == 0 || MathUtils.randomBoolean(0.3f))
                    graph.addVertex(basis.cpy().set(i, j));
            }
        }

        V v1 = basis.cpy(), v2 = basis.cpy();
        for (int i = 0; i < sideLength; i++) {
            v1.x(i);
            for (int j = 0; j < sideLength; j++) {
                v1.y(j);
                if (!graph.contains(v1))
                    continue;
                if (i < sideLength - 1) {
                    v2.set(i + 1, j);
                    if (graph.contains(v2)) {
                        graph.addEdge(v1, v2, v1.dst(v2));
                        if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                    }
                }
                if (j < sideLength - 1) {
                    v2.set(i, j + 1);
                    if (graph.contains(v2)) {
                        graph.addEdge(v1, v2, v1.dst(v2));
                        if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                    }
                }
            }
        }
        MathUtils.random.setSeed(newSeed);
        return graph;
    }

    public static<V extends Point3<V>> Graph<V> makeGridGraph3D(Graph<V> graph, int sideLength, V basis) {
        long newSeed = MathUtils.random.nextLong();
        MathUtils.random.setSeed(123456789L);

        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                for (int k = 0; k < sideLength; k++) {
                    if ((i & j & k & 1) == 0 || MathUtils.randomBoolean(0.3f))
                        graph.addVertex(basis.cpy().set(i, j, k));
                }
            }
        }

        V v1 = basis.cpy(), v2 = basis.cpy();
        for (int i = 0; i < sideLength; i++) {
            v1.x(i);
            for (int j = 0; j < sideLength; j++) {
                v1.y(j);
                for (int k = 0; k < sideLength; k++) {
                    v1.z(k);
                    if (!graph.contains(v1))
                        continue;
                    if (i < sideLength - 1) {
                        v2.set(i + 1, j, k);
                        if (graph.contains(v2)) {
                            graph.addEdge(v1, v2, v1.dst(v2));
                            if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                        }
                    }
                    if (j < sideLength - 1) {
                        v2.set(i, j + 1, k);
                        if (graph.contains(v2)) {
                            graph.addEdge(v1, v2, v1.dst(v2));
                            if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                        }
                    }
                    if (k < sideLength - 1) {
                        v2.set(i, j, k + 1);
                        if (graph.contains(v2)) {
                            graph.addEdge(v1, v2, v1.dst(v2));
                            if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                        }
                    }
                }
            }
        }
        MathUtils.random.setSeed(newSeed);
        return graph;
    }

    public static final char[][] DUNGEON = new char[][]{
            "#########################".toCharArray(),
            "#.......###.......#..####".toCharArray(),
            "#...............###..####".toCharArray(),
            "#....................####".toCharArray(),
            "#.##..##.............####".toCharArray(),
            "#.#....#......##....#####".toCharArray(),
            "#.#....#....####....#####".toCharArray(),
            "#.#....#########....#####".toCharArray(),
            "###......#######..#######".toCharArray(),
            "#........#######..#######".toCharArray(),
            "#........###......#######".toCharArray(),
            "###########........######".toCharArray(),
            "##.................######".toCharArray(),
            "#..................######".toCharArray(),
            "#...##...########.......#".toCharArray(),
            "#...#...#########.......#".toCharArray(),
            "#......####....##########".toCharArray(),
            "#.....#####....#######..#".toCharArray(),
            "##...######........###..#".toCharArray(),
            "##..#######........###..#".toCharArray(),
            "###.....###........###..#".toCharArray(),
            "###.....###........##...#".toCharArray(),
            "#.#......##.............#".toCharArray(),
            "#.##....................#".toCharArray(),
            "#..##..........#....#...#".toCharArray(),
            "#...##.........#....##..#".toCharArray(),
            "#....####...........###.#".toCharArray(),
            "#...................#####".toCharArray(),
            "#..................######".toCharArray(),
            "######..####..#..########".toCharArray(),
            "#####..#####..###########".toCharArray(),
            "#.........##..#....####.#".toCharArray(),
            "#.........##..#....###..#".toCharArray(),
            "#####..#..##..#....##...#".toCharArray(),
            "#...#..#..##..#....##...#".toCharArray(),
            "#...#..#..##..#....##...#".toCharArray(),
            "#...####..##............#".toCharArray(),
            "#.........##............#".toCharArray(),
            "#.........##.......##...#".toCharArray(),
            "#########################".toCharArray(),
    },
            DUNGEON2 = {
                    "#########################".toCharArray(),
                    "#........##..##....######".toCharArray(),
                    "#........##..##.....#...#".toCharArray(),
                    "#........#...##.........#".toCharArray(),
                    "#..###..##..............#".toCharArray(),
                    "#....#####..........#...#".toCharArray(),
                    "##...........##.....#..##".toCharArray(),
                    "###..........##....##...#".toCharArray(),
                    "######...############...#".toCharArray(),
                    "######..####....#########".toCharArray(),
                    "###.....####....######..#".toCharArray(),
                    "#.......####....######..#".toCharArray(),
                    "#.....###.##....#.......#".toCharArray(),
                    "#.....#.................#".toCharArray(),
                    "#.....#.................#".toCharArray(),
                    "#.........####..##......#".toCharArray(),
                    "#.##......####..##......#".toCharArray(),
                    "#.##......####..##..##..#".toCharArray(),
                    "##############..######..#".toCharArray(),
                    "##############..######..#".toCharArray(),
                    "##############..######..#".toCharArray(),
                    "##############..######..#".toCharArray(),
                    "####............#####...#".toCharArray(),
                    "#.###...................#".toCharArray(),
                    "#..###..................#".toCharArray(),
                    "#...###......#####....###".toCharArray(),
                    "##...##.....######....###".toCharArray(),
                    "###........##...........#".toCharArray(),
                    "####.....###............#".toCharArray(),
                    "####..######..###########".toCharArray(),
                    "##.....#####......#######".toCharArray(),
                    "##.....#####.....####...#".toCharArray(),
                    "#.........##....#####...#".toCharArray(),
                    "#...##....##..####......#".toCharArray(),
                    "#..#####..##....##.....##".toCharArray(),
                    "#..#####..##........#####".toCharArray(),
                    "#..#####..##........#####".toCharArray(),
                    "#.##......######....#####".toCharArray(),
                    "###.......###.....#######".toCharArray(),
                    "#########################".toCharArray(),
            };

    public static final char[][][] DUNGEON_3D = {
            DUNGEON,
            new char[DUNGEON.length][DUNGEON[0].length],
            DUNGEON2
    };

    static {
        RandomXS128 random = new RandomXS128(123456789);
        char[][] halls = DUNGEON_3D[1];
        for (int y = 1; y < halls.length - 1; y++) {
            for (int x = 1; x < halls[y].length - 1; x++) {
                if ((x & y & 3) == 1 && random.nextFloat() < 0.15f) {
                    halls[y][x] = '.';
                } else halls[y][x] = '#';
            }
        }
    }

    public static final float[][] NOISE_2D = new float[125][75];

    public static final float[][][] NOISE_3D = new float[40][40][20];

    static {
        ContinuousNoise noise = new ContinuousNoise(new FoamNoise(123), 123, 0.05f, ContinuousNoise.FBM, 2);
        for (int x = 0; x < NOISE_2D.length; x++) {
            for (int y = 0; y < NOISE_2D[x].length; y++) {
                NOISE_2D[x][y] = noise.getNoise(x, y);
            }
        }
        for (int x = 0; x < NOISE_3D.length; x++) {
            for (int y = 0; y < NOISE_3D[x].length; y++) {
                for (int z = 0; z < NOISE_3D[x][y].length; z++) {
                    NOISE_3D[x][y][z] = noise.getNoise(x, y, z);
                }
            }
        }
    }
}
