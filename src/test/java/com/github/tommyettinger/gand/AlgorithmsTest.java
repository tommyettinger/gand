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


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.NumberUtils;
import com.github.tommyettinger.gand.points.PointF2;
import com.github.tommyettinger.gand.points.PointF3;
import com.github.tommyettinger.gand.points.PointI2;
import com.github.tommyettinger.gand.points.PointI3;
import com.github.tommyettinger.gand.utils.Heuristic;
import com.github.tommyettinger.gand.utils.SearchProcessor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class AlgorithmsTest {

    @Test
    public void shortestPathShouldBeCorrect() {
        int n = 20;
        Graph<Vector2> undirectedGraph = TestUtils.makeGridGraph(new UndirectedGraph<>(), n);
        Graph<Vector2> diGraph = TestUtils.makeGridGraph(new DirectedGraph<>(), n);

        Vector2 start = new Vector2(0, 0), end = new Vector2(n - 1, n - 1);
        Path<Vector2> path;

        // without heuristic
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end);
        assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));

        // with heuristic
        Heuristic<Vector2> h = Vector2::dst;

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end, h);
        assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));
        assertTrue("Shortest path is not connected", diGraph.algorithms().isConnected(start, end));

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(2*(n-1) + 1, path.size());
        assertEquals(start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));
        assertTrue("Shortest path is not connected", undirectedGraph.algorithms().isConnected(start, end));


        // no path exists
        undirectedGraph.disconnect(end);
        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(0, path.size());
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        assertEquals(0, path.size());
        assertFalse(undirectedGraph.algorithms().isConnected(start, end));

        diGraph.disconnect(end);
        path = diGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(0, path.size());
        path = diGraph.algorithms().findShortestPath(start, end);
        assertEquals(0, path.size());
        assertFalse(diGraph.algorithms().isConnected(start, end));
    }

    @Test
    public void shortestFloat2PathShouldBeCorrect() {
        int n = 15;
        Graph<PointF2> diGraph = TestUtils.makeGridGraph2D(new Float2DirectedGraph(), n, new PointF2());
        System.out.println("Float2DirectedGraph: ");
        System.out.println(diGraph);
        Graph<PointF2> undirectedGraph = TestUtils.makeGridGraph2D(new Float2UndirectedGraph(), n, new PointF2());
        System.out.println("Float2UndirectedGraph: ");
        System.out.println(undirectedGraph);

        PointF2 start = new PointF2(0, 0), end = new PointF2(n - 1, n - 1);
        Path<PointF2> path;

        // without heuristic
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end);
        assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));

        // with heuristic
        Heuristic<PointF2> h = PointF2::dst;

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end, h);
        assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));
        assertTrue("Shortest path is not connected", diGraph.algorithms().isConnected(start, end));

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(2*(n-1) + 1, path.size());
        assertEquals(start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));
        assertTrue("Shortest path is not connected", undirectedGraph.algorithms().isConnected(start, end));


        // no path exists
        undirectedGraph.disconnect(end);
        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(0, path.size());
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        assertEquals(0, path.size());
        assertFalse(undirectedGraph.algorithms().isConnected(start, end));

        diGraph.disconnect(end);
        path = diGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(0, path.size());
        path = diGraph.algorithms().findShortestPath(start, end);
        assertEquals(0, path.size());
        assertFalse(diGraph.algorithms().isConnected(start, end));
    }

    @Test
    public void shortestDungeonPathShouldBeCorrect() {
        int n = 15;
        Int2DirectedGraph diGraph = new Int2DirectedGraph(TestUtils.DUNGEON, '.', 1f);
        diGraph.connectAdjacent(null, true);
        System.out.println("Int2DirectedGraph: ");
        System.out.println(diGraph);
        Int2UndirectedGraph undirectedGraph = new Int2UndirectedGraph(TestUtils.DUNGEON, '.', 1f);
        undirectedGraph.connectAdjacent(null, true);
        System.out.println("Int2UndirectedGraph: ");
        System.out.println(undirectedGraph);

        PointI2 start = new PointI2(1, 1), end = new PointI2(n - 2, n - 2);
        Path<PointI2> path;

        // without heuristic
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));

        // with heuristic
        Heuristic<PointI2> h = PointI2::dst;

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end, h);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));
        assertTrue("Shortest path is not connected", diGraph.algorithms().isConnected(start, end));

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertNotEquals(0, path.size());
        assertEquals(start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));
        assertTrue("Shortest path is not connected", undirectedGraph.algorithms().isConnected(start, end));

        // no path exists
        undirectedGraph.disconnect(end);
        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(0, path.size());
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        assertEquals(0, path.size());
        assertFalse(undirectedGraph.algorithms().isConnected(start, end));

        diGraph.disconnect(end);
        path = diGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(0, path.size());
        path = diGraph.algorithms().findShortestPath(start, end);
        assertEquals(0, path.size());
        assertFalse(diGraph.algorithms().isConnected(start, end));
    }


    @Test
    public void shortestDungeon3PathShouldBeCorrect() {
        int n = 25;
        Int3UndirectedGraph undirectedGraph = new Int3UndirectedGraph(TestUtils.DUNGEON_3D, '.', 1f);
        undirectedGraph.connectAdjacent(null, true);
        System.out.println("Int3UndirectedGraph: ");
        System.out.println(undirectedGraph);

        Int3DirectedGraph diGraph = new Int3DirectedGraph(TestUtils.DUNGEON_3D, '.', 1f);
        diGraph.connectAdjacent(null, true);
        System.out.println("Int3DirectedGraph: ");
        System.out.println(diGraph);

        diGraph.sortVertices((g, h) -> (h.z * 4 + h.y * 2 + h.x) - (g.z * 4 + g.y * 2 + g.x));
        PointI3 start = new PointI3(1, 1, 0), end = diGraph.getVertices().iterator().next();
        Path<PointI3> path;

        // without heuristic
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));

        // with heuristic
        Heuristic<PointI3> h = PointI3::dst;

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end, h);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));
        assertTrue("Shortest path is not connected", diGraph.algorithms().isConnected(start, end));

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertNotEquals(0, path.size());
        assertEquals(start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));
        assertTrue("Shortest path is not connected", undirectedGraph.algorithms().isConnected(start, end));

        // no path exists
        undirectedGraph.disconnect(end);
        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(0, path.size());
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        assertEquals(0, path.size());
        assertFalse(undirectedGraph.algorithms().isConnected(start, end));

        diGraph.disconnect(end);
        path = diGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(0, path.size());
        path = diGraph.algorithms().findShortestPath(start, end);
        assertEquals(0, path.size());
        assertFalse(diGraph.algorithms().isConnected(start, end));
    }


    @Test
    public void shortestPointF3PathShouldBeCorrect() {
        int n = 6;
        Graph<PointF3> diGraph = TestUtils.makeGridGraph3D(new Float3DirectedGraph(), n, new PointF3());
        System.out.println("Float3DirectedGraph: ");
        System.out.println(diGraph);
        Graph<PointF3> undirectedGraph = new Float3UndirectedGraph(diGraph);
        System.out.println("Float3UndirectedGraph: ");
        System.out.println(undirectedGraph);

        diGraph.sortVertices((g, h) -> NumberUtils.floatToIntBits((h.z * 4 + h.y * 2 + h.x) - (g.z * 4 + g.y * 2 + g.x) + 0f));
        PointF3 start = new PointF3(1, 1, 2), end = diGraph.getVertices().iterator().next();
        Path<PointF3> path;

        // without heuristic
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));

        // with heuristic
        Heuristic<PointF3> h = PointF3::dst;

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end, h);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));
        assertTrue("Shortest path is not connected", diGraph.algorithms().isConnected(start, end));

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertNotEquals(0, path.size());
        assertEquals(start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));
        assertTrue("Shortest path is not connected", undirectedGraph.algorithms().isConnected(start, end));

        // no path exists
        undirectedGraph.disconnect(end);
        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(0, path.size());
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        assertEquals(0, path.size());
        assertFalse(undirectedGraph.algorithms().isConnected(start, end));

        diGraph.disconnect(end);
        path = diGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(0, path.size());
        path = diGraph.algorithms().findShortestPath(start, end);
        assertEquals(0, path.size());
        assertFalse(diGraph.algorithms().isConnected(start, end));
    }

    @Test
    public void shortestPointI3PathShouldBeCorrect() {
        int n = 6;
        Graph<PointI3> diGraph = TestUtils.makeGridGraph3D(new Int3DirectedGraph(), n, new PointI3());
        System.out.println("Int3DirectedGraph: ");
        System.out.println(diGraph);
        Graph<PointI3> undirectedGraph = new Int3UndirectedGraph(diGraph);
        System.out.println("Int3UndirectedGraph: ");
        System.out.println(undirectedGraph);

        diGraph.sortVertices((g, h) -> NumberUtils.floatToIntBits((h.z * 4 + h.y * 2 + h.x) - (g.z * 4 + g.y * 2 + g.x) + 0f));
        PointI3 start = new PointI3(1, 1, 2), end = diGraph.getVertices().iterator().next();
        Path<PointI3> path;

        // without heuristic
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));

        // with heuristic
        Heuristic<PointI3> h = PointI3::dst;

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end, h);
        assertNotEquals("Shortest path is wrong size", 0, path.size());
        assertEquals("Shortest path has wrong starting point", start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));
        assertTrue("Shortest path is not connected", diGraph.algorithms().isConnected(start, end));

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertNotEquals(0, path.size());
        assertEquals(start, path.get(0));
        assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));
        assertTrue("Shortest path is not connected", undirectedGraph.algorithms().isConnected(start, end));

        // no path exists
        undirectedGraph.disconnect(end);
        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(0, path.size());
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        assertEquals(0, path.size());
        assertFalse(undirectedGraph.algorithms().isConnected(start, end));

        diGraph.disconnect(end);
        path = diGraph.algorithms().findShortestPath(start, end, h);
        assertEquals(0, path.size());
        path = diGraph.algorithms().findShortestPath(start, end);
        assertEquals(0, path.size());
        assertFalse(diGraph.algorithms().isConnected(start, end));
    }

    private static <V> boolean pathIsConnected(Path<V> path, Graph<V> graph) {
        for (int i = 0; i < path.size()-1; i++) {
            if (!graph.edgeExists(path.get(i), path.get(i+1))) return false;
        }
        return true;
    }
    
    @Test
    public void cyclesShouldBeDetected() {

        Graph<Integer> graph = new DirectedGraph<>();

        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }

        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        assertFalse(graph.algorithms().containsCycle());

        graph.addEdge(0,2);
        assertFalse(graph.algorithms().containsCycle());

        graph.addEdge(2,0);
        assertTrue(graph.algorithms().containsCycle());

        graph = new UndirectedGraph<>();

        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }

        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        assertFalse(graph.algorithms().containsCycle());

        graph.addEdge(0,2);
        assertTrue(graph.algorithms().containsCycle());

    }
    
    private DirectedGraph<Integer> createDirectedSearchGraph() {
        DirectedGraph<Integer> graph = new DirectedGraph<>();

        for (int i = 0; i < 7; i++) {
            graph.addVertex(i);
        }

        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 3);
        graph.addEdge(2, 4);
        graph.addEdge(3, 5);
        graph.addEdge(4, 5);
        
        return graph;
    }

    @Test
    public void bfsShouldWork() {
        Graph<Integer> graph = createDirectedSearchGraph();

        Graph<Integer> tree = graph.createNew();

        SearchProcessor<Integer> processor = (step) -> {
            tree.addVertex(step.vertex());
            if (step.count() > 0) {
                tree.addEdge(step.edge().getA(), step.edge().getB());
            }
        };

        tree.addVertex(0);
        graph.algorithms().breadthFirstSearch(0, processor);
        assertEquals(6, tree.size());
        assertEquals(5, tree.getEdgeCount());

        List<Integer> vxs = new ArrayList<>(tree.getVertices());

        List<List<Integer>> expectedOrders = new ArrayList<>();
        expectedOrders.add(Arrays.asList(0, 1, 2, 3, 4, 5));
        expectedOrders.add(Arrays.asList(0, 2, 1, 3, 4, 5));
        expectedOrders.add(Arrays.asList(0, 1, 2, 4, 3, 5));
        expectedOrders.add(Arrays.asList(0, 2, 1, 3, 4, 5));

        assertTrue("BFS not in correct order", expectedOrders.contains(vxs));
    }

    @Test
    public void dfsShouldWork() {
        Graph<Integer> graph = createDirectedSearchGraph();
        Graph<Integer> tree = graph.createNew();

        SearchProcessor<Integer> processor = step -> {
            if (step.depth() > 4) {
                step.ignore();
                return;
            }
            tree.addVertex(step.vertex());
            if (step.count() > 0) {
                tree.addEdge(step.edge().getA(), step.edge().getB());
            }
        };

        graph.algorithms().depthFirstSearch(0, processor);


        assertEquals(6, tree.size());
        assertEquals(5, tree.getEdgeCount());
        List<Integer> vxs = new ArrayList<>(tree.getVertices());

        List<List<Integer>> expectedOrders = new ArrayList<>();
        expectedOrders.add(Arrays.asList(0, 1, 3, 5, 2, 4));
        expectedOrders.add(Arrays.asList(0, 2, 4, 5, 1, 3));
        assertTrue("DFS not in correct order", expectedOrders.contains(vxs));
    }

    @Test
    public void topologicalSortShouldWork() {
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        int n = 10;
        for (int i = 0; i < n; i++) graph.addVertex(i);

        graph.addEdge(9,8);
        graph.addEdge(6,7);
        assertTrue(graph.topologicalSort());

        graph.removeAllEdges();

        for (int i = 0; i < n-1; i++) graph.addEdge(i+1, i);

        assertTrue(graph.topologicalSort());
        int i = n-1;
        for (Integer vertex : graph.getVertices()) {
            Integer expected = Integer.valueOf(i--);
            assertEquals(expected, vertex);
        }


        graph.addEdge(n/2, n/2 + 1);
        boolean success = graph.topologicalSort();
        assertFalse(success);

        graph = new DirectedGraph<>();
        graph.addVertices(0, 1, 2, 3, 4, 5);
        graph.addEdge(2,0);
        graph.addEdge(1,2);
        graph.addEdge(4,1);
        graph.addEdge(4,2);
        graph.addEdge(3,5);
        assertTrue(graph.topologicalSort());

        graph = new DirectedGraph<>();
        graph.addVertices(0, 1, 2, 3, 4, 5);
        graph.addEdge(2,0);
        graph.addEdge(1,2);
        graph.addEdge(4,1);
        graph.addEdge(4,2);
        graph.addEdge(3,5);

        graph.addEdge(2,4);
        assertFalse(graph.topologicalSort());
    }

    @Test
    public void mwstShouldBeTree() {

        int n = 20;
        UndirectedGraph<Integer> graph = new UndirectedGraph<>();
        for (int i = 0; i < n; i++) graph.addVertex(i);
        GraphBuilder.buildCompleteGraph(graph);

        graph.getEdge(0, 1).setWeight(4);
        graph.getEdge(4, 6).setWeight(0.5f);

        Graph<Integer> mwst = graph.algorithms().findMinimumWeightSpanningTree();

        assertEquals("Tree doesn't have correct number of vertices", n, mwst.size());
        assertEquals("Tree doesn't have correct number of edges", n-1, mwst.getEdgeCount());
        assertFalse("Tree contains a cycle", mwst.algorithms().containsCycle());
        assertEquals("Tree is not minimum weight", n-1 - 0.5f, mwst.getEdges().stream().mapToDouble(Connection::getWeight).sum(), 0.0001f);

    }
}
