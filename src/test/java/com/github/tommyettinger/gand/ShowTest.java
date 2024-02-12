package com.github.tommyettinger.gand;

public class ShowTest {
    public static void main(String[] args) {
        Grid2DDirectedGraph graph = new Grid2DDirectedGraph(TestUtils.DUNGEON, '.', 1f);
        graph.connectAdjacent(null, true);
        System.out.println(graph);
    }
}
