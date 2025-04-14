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
import com.github.tommyettinger.gand.ds.ObjectDeque;
import com.github.tommyettinger.gdcrux.PointI2;
import com.github.tommyettinger.gand.utils.GridMetric;
import org.junit.Assert;
import org.junit.Test;

import static com.github.tommyettinger.gdcrux.PointMaker.pt;

public class TestGradientGridI2 {
    /**
     * Prints with y pointing down, matching how 2D arrays are entered in source code.
     * @param level a 2D char array that will be printed to stdout with y-down
     */
    public static void print(char[][] level) {
        for (int y = 0; y < level[0].length; y++) {
            for (int x = 0; x < level.length; x++) {
                System.out.print(level[x][y]);
            }
            System.out.println();
        }
    }
    @Test
    public void testMultipleGoals() {
        char[][] map = {
                "#########".toCharArray(),
                "#.......#".toCharArray(),
                "#.......#".toCharArray(),
                "#.......#".toCharArray(),
                "#....#..#".toCharArray(),
                "#...#...#".toCharArray(),
                "#.......#".toCharArray(),
                "#.......#".toCharArray(),
                "#########".toCharArray(),
        };
        GradientGridI2 gg = new GradientGridI2(map, GridMetric.EUCLIDEAN);
        gg.setBlockingRequirement(2);
        ObjectDeque<PointI2> path = new ObjectDeque<>(16);

        PointI2 start = pt(4, 4), goal0 = pt(5, 5), goal1 = pt(5, 6);

//        gg.setGoal(goal0);
//        gg.setGoal(goal1);
//        gg.partialScan(10, null);
//        gg.findPathPreScanned(path, start);

        gg.findPath(path, 10, 10, null, null, start, ObjectDeque.with(goal0, goal1));

        char ch = '1';
        for(PointI2 c : path) {
            map[c.x][c.y] = ch++;
        }
        map[start.x][start.y] = '0';
        print(map);
        Assert.assertEquals(goal1, path.last());
    }

    @Test
    public void testJson() {
        Json json = new Json();

        char[][] map = {
                "#########".toCharArray(),
                "#.......#".toCharArray(),
                "#.......#".toCharArray(),
                "#.......#".toCharArray(),
                "#....#..#".toCharArray(),
                "#...#...#".toCharArray(),
                "#.......#".toCharArray(),
                "#.......#".toCharArray(),
                "#########".toCharArray(),
        };
        GradientGridI2 gg = new GradientGridI2(map, GridMetric.EUCLIDEAN);
        gg.setBlockingRequirement(2);
        ObjectDeque<PointI2> path = new ObjectDeque<>(16);

        PointI2 start = pt(4, 4), goal0 = pt(5, 5), goal1 = pt(5, 6);

        gg.setGoal(goal0);
        gg.setGoal(goal1);
        gg.partialScan(10, null);
        gg.findPathPreScanned(path, start);

        gg.findPath(path, 10, 10, null, null, start, ObjectDeque.with(goal0, goal1));
        print(map);
        System.out.println(gg);

        System.out.println("Original, pretty-printed:");
        String ppgg = json.prettyPrint(gg);
        System.out.println(ppgg);
        GradientGridI2 gg2 = json.fromJson(GradientGridI2.class, ppgg);
        Assert.assertEquals(gg, gg2);
        System.out.println("Read back from JSON and pretty-printed again:");
        System.out.println(json.prettyPrint(gg2));
    }

    @Test
    public void testJsonCornerCases() {
        Json json = new Json();

        GradientGridI2 gg = new GradientGridI2();
        gg.setBlockingRequirement(2);

        PointI2 goal0 = pt(5, 5), goal1 = pt(5, 6);

        gg.setGoal(goal0);
        gg.setGoal(goal1);

        System.out.println("Original, pretty-printed:");
        String ppgg = json.prettyPrint(gg);
        System.out.println(ppgg);
        GradientGridI2 gg2 = json.fromJson(GradientGridI2.class, ppgg);
        Assert.assertEquals(gg, gg2);
        System.out.println("Read back from JSON and pretty-printed again:");
        System.out.println(json.prettyPrint(gg2));
    }
}
