package com.github.tommyettinger.gand;

public class ShowTest {
    public static void main(String[] args) {
        {
            Int2DirectedGraph graph = new Int2DirectedGraph(TestUtils.DUNGEON, '.', 1f);
            graph.connectAdjacent(null, true);
            System.out.println(graph);
        }
        System.out.println();
        {
            Int2UndirectedGraph graph = new Int2UndirectedGraph(TestUtils.DUNGEON, '.', 1f);
            graph.connectAdjacent(null, true);
            System.out.println(graph);
        }
    }
}
