package com.github.tommyettinger.gand.algorithms;

import com.github.tommyettinger.gand.Connection;
import com.github.tommyettinger.gand.Node;
import com.github.tommyettinger.gand.ds.ObjectDeque;
import com.github.tommyettinger.gand.utils.SearchProcessor;

import java.util.ArrayList;

public class BreadthFirstSearch<V> extends Algorithm<V> {

    private SearchProcessor<V> processor;
    private SearchStep<V> step = new SearchStep<>();

    private final ObjectDeque<Node<V>> queue;

    public BreadthFirstSearch(int id, Node<V> start, SearchProcessor<V> processor) {
        super(id);
        this.processor = processor;
        queue = new ObjectDeque<>();
        start.resetAlgorithmAttribs(id);
        queue.add(start);
        start.setSeen(true);
    }

    @Override
    public boolean update() {
        if (isFinished()) return true;

        Node<V> v = queue.pollFirst();
        if (processor != null) {
            step.prepare(v);
            processor.accept(step);
            if (step.terminate) {
                queue.clear();
                return true;
            }
            if (step.ignore) return isFinished();
        }
        ArrayList<Connection<V>> outEdges = v.getOutEdges();
        for (Connection<V> e : outEdges) {
            Node<V> w = e.getNodeB();
            w.resetAlgorithmAttribs(id);
            if (!w.isSeen()) {
                w.setIndex(v.getIndex() + 1);
                w.setDistance(v.getDistance() + e.getWeight());
                w.setConnection(e);
                w.setSeen(true);
                queue.addLast(w);
            }
        }
        return isFinished();
    }

    @Override
    public boolean isFinished() {
        return queue.isEmpty();
    }

}
