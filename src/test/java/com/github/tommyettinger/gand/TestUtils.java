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

import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;

class TestUtils {
    static Graph<Vector2> makeGridGraph(Graph<Vector2> graph, int n) {

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Vector2 v = new Vector2(i, j);
                graph.addVertex(v);
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i<n-1) {
                    Vector2 v1 = new Vector2(i, j), v2 = new Vector2(i+1,j);
                    graph.addEdge(v1, v2, v1.dst(v2));
                    if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                }
                if (j<n-1) {
                    Vector2 v1 = new Vector2(i, j), v2 = new Vector2(i,j+1);
                    graph.addEdge(v1, v2, v1.dst(v2));
                    if (graph.isDirected()) graph.addEdge(v2, v1, v1.dst(v2));
                }
            }
        }

        return graph;
    }

    static final char[][] DUNGEON = new char[][]{
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
        for (int y = 1; y < halls.length-1; y++) {
            for (int x = 1; x < halls[y].length-1; x++) {
                if((x & y & 3) == 1 && random.nextFloat() < 0.15f){
                    halls[y][x] = '.';
                } else halls[y][x] = '#';
            }
        }
    }

}
