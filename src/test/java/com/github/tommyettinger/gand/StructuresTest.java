package com.github.tommyettinger.gand;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import com.github.tommyettinger.gand.utils.BadHashInteger;

import static org.junit.Assert.*;


public class StructuresTest {

    @Test
    public void nodeMapShouldWork() {

        Graph<Integer> graph = new UndirectedGraph<>();
        NodeMap<Integer> nodeMap = graph.nodeMap;
        int n = 16;
        List<Integer> list = new ArrayList<>();

        int threshold = nodeMap.threshold;
        for (int i = 0; i < threshold; i++) {
            assertNotNull(nodeMap.put(i));
            list.add(i);
        }

        assertNotNull("Put did not return a node", nodeMap.put(NodeMap.MIN_TABLE_LENGTH));
        assertTrue("Object not contained in map", nodeMap.contains(NodeMap.MIN_TABLE_LENGTH));
        assertEquals("Map is not correct size", threshold+1, nodeMap.size);


        Node<Integer> removed = nodeMap.remove(2);
        assertNotNull("Removal did not return node", removed);


        // test via graph object
        Graph<BadHashInteger> badGraph = new UndirectedGraph<>();
        for (int i = 0; i < n; i++) {
            assertNotNull("Put did not return a node", badGraph.nodeMap.put(new BadHashInteger(i)));
        }

        assertEquals("Graph is not correct size", badGraph.size(), n);

        badGraph.removeVertex(new BadHashInteger(2));

        assertEquals(badGraph.size(), n - 1);

        badGraph.nodeMap.clear();

        for (int i = 0; i < n; i++) {
            assertNotNull(badGraph.nodeMap.put(new BadHashInteger(i)));
        }
    }
}
