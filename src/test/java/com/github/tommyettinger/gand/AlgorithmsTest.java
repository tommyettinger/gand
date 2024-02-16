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


import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.NumberUtils;
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
    public void shortestVector2PathShouldBeCorrect() {
        int n = 20;
        Graph<Vector2> undirectedGraph = TestUtils.makeGridGraph(new Vector2UndirectedGraph(), n);
        Graph<Vector2> diGraph = TestUtils.makeGridGraph(new Vector2DirectedGraph(), n);

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
    public void shortestGridPathShouldBeCorrect() {
        int n = 25;
        Grid2UndirectedGraph undirectedGraph = new Grid2UndirectedGraph(TestUtils.DUNGEON, '.', 1f);
        undirectedGraph.connectAdjacent(null, true);
        Grid2DirectedGraph diGraph = new Grid2DirectedGraph(TestUtils.DUNGEON, '.', 1f);
        diGraph.connectAdjacent(null, true);

        GridPoint2 start = new GridPoint2(1, 1), end = new GridPoint2(n - 2, n - 2);
        Path<GridPoint2> path;

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
        Heuristic<GridPoint2> h = GridPoint2::dst;

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
    public void shortestGrid3PathShouldBeCorrect() {
        int n = 25;
        Grid3UndirectedGraph undirectedGraph = new Grid3UndirectedGraph(TestUtils.DUNGEON_3D, '.', 1f);
        undirectedGraph.connectAdjacent(null, true);
        Grid3DirectedGraph diGraph = new Grid3DirectedGraph(TestUtils.DUNGEON_3D, '.', 1f);
        diGraph.connectAdjacent(null, true);

        diGraph.sortVertices((g, h) -> (h.z * 4 + h.y * 2 + h.x) - (g.z * 4 + g.y * 2 + g.x));
        GridPoint3 start = new GridPoint3(1, 1, 0), end = diGraph.getVertices().iterator().next();
        Path<GridPoint3> path;

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
        Heuristic<GridPoint3> h = GridPoint3::dst;

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
    public void shortestVector3PathShouldBeCorrect() {
        int n = 15;
        Graph<Vector3> diGraph = TestUtils.makeGridGraph3(new Vector3DirectedGraph(), n);
        Graph<Vector3> undirectedGraph = new Vector3UndirectedGraph(diGraph);

        diGraph.sortVertices((g, h) -> NumberUtils.floatToIntBits((h.z * 4 + h.y * 2 + h.x) - (g.z * 4 + g.y * 2 + g.x) + 0f));
        Vector3 start = new Vector3(1, 1, 2), end = diGraph.getVertices().iterator().next();
        Path<Vector3> path;

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
        Heuristic<Vector3> h = Vector3::dst;

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
