package com.github.tommyettinger.gand.algorithms;

import com.badlogic.gdx.utils.NumberUtils;
import com.github.tommyettinger.gand.Connection;
import com.github.tommyettinger.gand.Node;
import com.github.tommyettinger.gand.UndirectedGraph;
import com.github.tommyettinger.gand.utils.ObjectDeque;

public class MinimumWeightSpanningTree<V> extends Algorithm<V>{

    private UndirectedGraph<V> spanningTree;
    private ObjectDeque<Connection<V>> edgeQueue;
    private int finishAt;

    // adapted from https://www.baeldung.com/java-spanning-trees-kruskal

    protected MinimumWeightSpanningTree(int id, UndirectedGraph<V> graph, boolean minSpanningTree) {
        super(id);

        spanningTree = graph.createNew();

        spanningTree.addVertices(graph.getVertices());

        edgeQueue = new ObjectDeque<>(graph.internals().getConnections());
        if(minSpanningTree)
            edgeQueue.sort((a, b) -> NumberUtils.floatToIntBits(a.getWeight() - b.getWeight() + 0f));
        else
            edgeQueue.sort((a, b) -> NumberUtils.floatToIntBits(b.getWeight() - a.getWeight() + 0f));

        finishAt = graph.isConnected() ? graph.size() - 1 : -1;
    }

    @Override
    public boolean update() {
        if (isFinished()) return true;

        Connection<V> edge = edgeQueue.poll();

        if (doesEdgeCreateCycle(edge.getNodeA(), edge.getNodeB(), id)) {
            return false;
        }
        spanningTree.addEdge(edge.getA(), edge.getB(), edge.getWeight());

        return isFinished();
    }

    private void unionByRank(Node<V> rootU, Node<V> rootV) {
        if (rootU.getIndex() < rootV.getIndex()) {
            rootU.setPrev(rootV);
        } else {
            rootV.setPrev(rootU);
            if (rootU.getIndex() == rootV.getIndex()) rootU.setIndex(rootU.getIndex() + 1);
        }
    }

    private Node<V> find(Node<V> node) {
        if (node.equals(node.getPrev())) {
            return node;
        } else {
            return find(node.getPrev());
        }
    }
    private Node<V> pathCompressionFind(Node<V> node) {
        if (node.equals(node.getPrev())) {
            return node;
        } else {
            Node<V> parentNode = find(node.getPrev());
            node.setPrev(parentNode);
            return parentNode;
        }
    }

    private boolean doesEdgeCreateCycle(Node<V> u, Node<V> v, int runID) {
        if (u.resetAlgorithmAttribs(runID)) u.setPrev(u);
        if (v.resetAlgorithmAttribs(runID)) v.setPrev(v);
        Node<V> rootU = pathCompressionFind(u);
        Node<V> rootV = pathCompressionFind(v);
        if (rootU.equals(rootV)) {
            return true;
        }
        unionByRank(rootU, rootV);
        return false;
    }

    @Override
    public boolean isFinished() {
        return finishAt < 0 ? edgeQueue.isEmpty() : spanningTree.getEdgeCount() == finishAt;
    }

    public UndirectedGraph<V> getSpanningTree() {
        return spanningTree;
    }
}
