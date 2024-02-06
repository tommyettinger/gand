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
package com.github.tommyettinger.old;

import com.badlogic.gdx.math.Vector2;
import com.github.tommyettinger.old.ds.ObjectDeque;
import org.junit.Test;

import static org.junit.Assert.*;

public class AlgorithmsTest {

    @Test
    public void shortestPathShouldBeCorrect() {
        int n = 20;
        Graph<Vector2> undirectedGraph = TestUtils.makeGridGraph(new UndirectedGraph<>(), n);
        Graph<Vector2> diGraph = TestUtils.makeGridGraph(new DirectedGraph<>(), n);

        Vector2 start = new Vector2(0, 0), end = new Vector2(n - 1, n - 1);
        ObjectDeque<Vector2> path;

        // without heuristic
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        Assert.assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        Assert.assertEquals("Shortest path has wrong starting point", start, path.get(0));
        Assert.assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end);
        Assert.assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        Assert.assertEquals("Shortest path has wrong starting point", start, path.get(0));
        Assert.assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));

        // with heuristic
        Heuristic<Vector2> h = Vector2::dst;

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        Assert.assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        Assert.assertEquals("Shortest path has wrong starting point", start, path.get(0));
        Assert.assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));

        path = diGraph.algorithms().findShortestPath(start, end, h);
        Assert.assertEquals("Shortest path is wrong size", 2*(n-1) + 1, path.size());
        Assert.assertEquals("Shortest path has wrong starting point", start, path.get(0));
        Assert.assertTrue("Shortest path is not connected", pathIsConnected(path, diGraph));
        Assert.assertTrue("Shortest path is not connected", diGraph.algorithms().isConnected(start, end));

        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        Assert.assertEquals(2*(n-1) + 1, path.size());
        Assert.assertEquals(start, path.get(0));
        Assert.assertTrue("Shortest path is not connected", pathIsConnected(path, undirectedGraph));
        Assert.assertTrue("Shortest path is not connected", undirectedGraph.algorithms().isConnected(start, end));


        // no path exists
        undirectedGraph.disconnect(end);
        path = undirectedGraph.algorithms().findShortestPath(start, end, h);
        Assert.assertEquals(0, path.size());
        path = undirectedGraph.algorithms().findShortestPath(start, end);
        Assert.assertEquals(0, path.size());
        Assert.assertFalse(undirectedGraph.algorithms().isConnected(start, end));

        diGraph.disconnect(end);
        path = diGraph.algorithms().findShortestPath(start, end, h);
        Assert.assertEquals(0, path.size());
        path = diGraph.algorithms().findShortestPath(start, end);
        Assert.assertEquals(0, path.size());
        Assert.assertFalse(diGraph.algorithms().isConnected(start, end));
    }


    private static boolean pathIsConnected(ObjectDeque<Vector2> path, Graph<Vector2> graph) {
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
        Assert.assertFalse(graph.algorithms().containsCycle());

        graph.addEdge(0,2);
        Assert.assertFalse(graph.algorithms().containsCycle());

        graph.addEdge(2,0);
        Assert.assertTrue(graph.algorithms().containsCycle());

        graph = new UndirectedGraph<>();

        for (int i = 0; i < 6; i++) {
            graph.addVertex(i);
        }

        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        Assert.assertFalse(graph.algorithms().containsCycle());

        graph.addEdge(0,2);
        Assert.assertTrue(graph.algorithms().containsCycle());

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
//Commented because we don't have the concept of a SearchProcessor yet.
//    @Test
//    public void bfsShouldWork() {
//        Graph<Integer> graph = createDirectedSearchGraph();
//
//        Graph<Integer> tree = graph.createNew();
//
//        SearchProcessor<Integer> processor = (step) -> {
//            tree.addVertex(step.vertex());
//            if (step.count() > 0) {
//                tree.addEdge(step.edge().getA(), step.edge().getB());
//            }
//        };
//
//        tree.addVertex(0);
//        graph.algorithms().breadthFirstSearch(0, processor);
//        assertEquals(6, tree.size());
//        assertEquals(5, tree.getEdgeCount());
//
//        List<Integer> vxs = new ArrayList<>(tree.getVertices());
//
//        List<List<Integer>> expectedOrders = new ArrayList<>();
//        expectedOrders.add(Arrays.asList(0, 1, 2, 3, 4, 5));
//        expectedOrders.add(Arrays.asList(0, 2, 1, 3, 4, 5));
//        expectedOrders.add(Arrays.asList(0, 1, 2, 4, 3, 5));
//        expectedOrders.add(Arrays.asList(0, 2, 1, 3, 4, 5));
//
//        assertTrue("BFS not in correct order", expectedOrders.contains(vxs));
//    }
//
//    @Test
//    public void dfsShouldWork() {
//        Graph<Integer> graph = createDirectedSearchGraph();
//        Graph<Integer> tree = graph.createNew();
//
//        SearchProcessor<Integer> processor = step -> {
//            if (step.depth() > 4) {
//                step.ignore();
//                return;
//            }
//            tree.addVertex(step.vertex());
//            if (step.count() > 0) {
//                tree.addEdge(step.edge().getA(), step.edge().getB());
//            }
//        };
//
//        graph.algorithms().depthFirstSearch(0, processor);
//
//
//        assertEquals(6, tree.size());
//        assertEquals(5, tree.getEdgeCount());
//        List<Integer> vxs = new ArrayList<>(tree.getVertices());
//
//        List<List<Integer>> expectedOrders = new ArrayList<>();
//        expectedOrders.add(Arrays.asList(0, 1, 3, 5, 2, 4));
//        expectedOrders.add(Arrays.asList(0, 2, 4, 5, 1, 3));
//        assertTrue("DFS not in correct order", expectedOrders.contains(vxs));
//    }

    @Test
    public void topologicalSortShouldWork() {
        DirectedGraph<Integer> graph = new DirectedGraph<>();
        int n = 10;
        for (int i = 0; i < n; i++) graph.addVertex(i);

        graph.addEdge(9,8);
        graph.addEdge(6,7);
        Assert.assertTrue(graph.algorithms.topologicalSort());

        graph.removeAllEdges();

        for (int i = 0; i < n-1; i++) graph.addEdge(i+1, i);

        Assert.assertTrue(graph.algorithms.topologicalSort());
        int i = n-1;
        for (Integer vertex : graph.getVertices()) {
            Integer expected = i--;
            Assert.assertEquals(expected, vertex);
        }


        graph.addEdge(n/2, n/2 + 1);
        boolean success = graph.algorithms.topologicalSort();
        Assert.assertFalse(success);

        graph = new DirectedGraph<>();
        graph.addVertices(0, 1, 2, 3, 4, 5);
        graph.addEdge(2,0);
        graph.addEdge(1,2);
        graph.addEdge(4,1);
        graph.addEdge(4,2);
        graph.addEdge(3,5);
        Assert.assertTrue(graph.algorithms.topologicalSort());

        graph = new DirectedGraph<>();
        graph.addVertices(0, 1, 2, 3, 4, 5);
        graph.addEdge(2,0);
        graph.addEdge(1,2);
        graph.addEdge(4,1);
        graph.addEdge(4,2);
        graph.addEdge(3,5);

        graph.addEdge(2,4);
        Assert.assertFalse(graph.algorithms.topologicalSort());
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

        Assert.assertEquals("Tree doesn't have correct number of vertices", n, mwst.size());
        Assert.assertEquals("Tree doesn't have correct number of edges", n-1, mwst.getEdgeCount());
        Assert.assertFalse("Tree contains a cycle", mwst.algorithms().containsCycle());
        Assert.assertEquals("Tree is not minimum weight", n-1 - 0.5f, mwst.getEdges().stream().mapToDouble(Edge::getWeight).sum(), 0.0001f);

    }
}