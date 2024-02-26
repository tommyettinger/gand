package com.github.tommyettinger.gand;

import com.github.tommyettinger.gand.points.PointI2;

import java.util.ArrayList;

public class ShowTest {
    public static void main(String[] args) {
        final char[][] partial = new char[20][20];
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
//            System.out.println("Full graph has " + graph.numberOfComponents() + " components.");
            ArrayList<Graph<PointI2>> components = graph.getComponents();
            for(Graph<PointI2> comp : components) {
                System.out.println(comp);
//                System.out.println("Component graph has " + comp.getEdgeCount() + " edges.");
            }
        }

    }
}
