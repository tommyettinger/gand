package com.github.tommyettinger.gand;

public class ShowTest {
    public static void main(String[] args) {
        {
            Grid2DirectedGraph graph = new Grid2DirectedGraph(TestUtils.DUNGEON, '.', 1f);
            graph.connectAdjacent(null, true);
            System.out.println(graph);
        }
        System.out.println();
        {
            Grid2UndirectedGraph graph = new Grid2UndirectedGraph(TestUtils.DUNGEON, '.', 1f);
            graph.connectAdjacent(null, true);
            System.out.println(graph);
        }
    }
}
