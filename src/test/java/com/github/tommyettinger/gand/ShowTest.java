package com.github.tommyettinger.gand;

import com.badlogic.gdx.utils.ObjectIntMap;
import com.github.tommyettinger.cringe.RandomAce320;
import com.github.tommyettinger.gand.points.PointI2;

import java.util.ArrayList;

public class ShowTest {
    public static void main(String[] args) {
        final char[][] partial = new char[20][20];
        final ObjectIntMap<PointI2> arrows = new ObjectIntMap<>(4);
        arrows.put(new PointI2(0, -1), '↑');
        arrows.put(new PointI2(1, 0), '→');
        arrows.put(new PointI2(0, 1), '↓');
        arrows.put(new PointI2(-1, 0), '←');
        for (int x = 0; x < 20; x++) {
            System.arraycopy(TestUtils.DUNGEON[x+10], 2, partial[x], 0, 20);
        }
        {
            Int2DirectedGraph graph = new Int2DirectedGraph(partial, '.', 1f);
            graph.connectAdjacent(null, false);
            System.out.println(graph);
        }
        System.out.println();
        {
            Int2DirectedGraph graph = new Int2DirectedGraph(partial, '.', 1f);
            graph.connectAdjacent(null, true);
            System.out.println(graph);
        }
        System.out.println();
        {
            Int2UndirectedGraph graph = new Int2UndirectedGraph(partial, '.', 1f);
            graph.connectAdjacent(null, true);
            System.out.println(graph);
        }
        System.out.println('\n');
        {
            Int2DirectedGraph graph = new Int2DirectedGraph(partial, '.', 1f);
            graph.connectAdjacent(null, false);
            ArrayList<Graph<PointI2>> components = graph.getComponents();
            for(Graph<PointI2> comp : components) {
                System.out.println(comp);
            }
        }
        System.out.println("\nLargest Component:");
        {
            Int2DirectedGraph graph = new Int2DirectedGraph(partial, '.', 1f);
            graph.connectAdjacent(null, false);
            System.out.println(graph.largestComponent());
        }
        System.out.println("\nTwistedLineI2:");
        {
            RandomAce320 random = new RandomAce320(123456789L);
            Int2UndirectedGraph graph = new Int2UndirectedGraph(partial, '.', 1f);
            graph.connectAdjacent(null, false);
            PointI2[] arr = graph.largestComponent().getVertices().toArray(new PointI2[0]);
            PointI2 start = random.randomElement(arr), end = random.randomElement(arr);
            TwistedLineI2 twist = new TwistedLineI2(random, arr, 0.16f);
            Path<PointI2> path = twist.line(start, end);
            char[][] grid = new char[20][20];
            for (int x = 0; x < 20; x++) {
                System.arraycopy(TestUtils.DUNGEON[x + 10], 2, grid[x], 0, 20);
            }
            PointI2 previous = path.first(), current;
            grid[previous.x][previous.y] = '@';
            for (int i = 1; i < path.size; i++) {
                current = path.get(i);
                grid[current.x][current.y] = (char) ('@' + i);//(char)arrows.get(current.cpy().sub(previous), '*');
                previous.set(current);
            }
            grid[path.last().x][path.last().y] = '^';

            for (int x = 0; x < 20; x++) {
                for (int y = 0; y < 20; y++) {
                    System.out.print(partial[x][y]);
                }
                System.out.println();
            }
            System.out.println();

            for (int x = 0; x < 20; x++) {
                for (int y = 0; y < 20; y++) {
                    System.out.print(grid[x][y]);
                }
                System.out.println();
            }
        }

    }
}
