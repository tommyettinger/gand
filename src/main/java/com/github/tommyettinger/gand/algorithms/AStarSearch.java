package com.github.tommyettinger.gand.algorithms;

import com.github.tommyettinger.gand.Connection;
import com.github.tommyettinger.gand.Node;
import com.github.tommyettinger.gand.Path;
import com.github.tommyettinger.gand.utils.Heuristic;
import com.github.tommyettinger.gand.utils.SearchProcessor;

import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class AStarSearch<V> extends Algorithm<V> {

    private Heuristic<V> heuristic;
    private SearchProcessor<V> processor;
    private SearchStep<V> step = new SearchStep<>();
    private final BinaryHeap heap;
    private Node<V> start, target, u, end;
    private Path<V> path;

    protected AStarSearch(int id, final Node<V> start, final Node<V> target, final Heuristic<V> heuristic, final SearchProcessor<V> processor) {
        super(id);
        this.start = start;
        this.target = target;
        this.heuristic = heuristic;
        this.processor = processor;
        heap = new BinaryHeap();
        start.resetAlgorithmAttribs(id);
        start.setDistance(0);
        heap.add(start);
    }

    @Override
    public boolean update() {
        if (isFinished()) return true;

        u = heap.pop();

        if (!u.isProcessed()) {
            if (processor != null && u.getIndex() > 0) {
                step.prepare(u);
                processor.accept(step);
                if (step.terminate) {
                    heap.clear();
                    return true;
                }
                if (step.ignore) {
                    return isFinished();
                }
            }
            u.setProcessed(true);

            if (u == target) {
                heap.clear();
                end = u;
                return true;
            }

            ArrayList<Connection<V>> outEdges = u.getOutEdges();
            for (Connection<V> e : outEdges) {
                Node<V> v = e.getNodeB();
                v.resetAlgorithmAttribs(id);
                if (!v.isProcessed()) {
                    float newDistance = u.getDistance() + e.getWeight();
                    if (newDistance < v.getDistance()) {
                        v.setDistance(newDistance);
                        v.setPrev(u);
                        v.setConnection(e);
                        if (heuristic != null && !v.isSeen()) {
                            v.setEstimate(heuristic.getEstimate(v.getObject(), target.getObject()));
                        }
                        if (!v.isSeen()) {
                            heap.add(v, v.getDistance() + v.getEstimate());
                        } else {
                            heap.setValue(v, v.getDistance() + v.getEstimate());
                        }
                        v.setIndex(u.getIndex() + 1);
                        v.setSeen(true);
                    }
                }
            }
        }
        return isFinished();
    }

    @Override
    public boolean isFinished() {
        return heap.size == 0;
    }

    public Path<V> getPath() {
        if (!isFinished()) return null;
        if (path == null) {
            path = end != null ? new AlgorithmPath<>(end) : Path.EMPTY_PATH;
        }
        return path;
    }

    Node<V> getEnd() {
        return end;
    }

    /**
     *
     * @param start
     * @param target
     * @param heuristic
     * @return the target Node if reachable, otherwise null
     */
    private Node<V> aStarSearch(final Node<V> start, final Node<V> target, final Heuristic<V> heuristic, final SearchProcessor<V> processor) {

        start.resetAlgorithmAttribs(id);
        start.setDistance(0);

        heap.add(start);

        final SearchStep<V> step = processor != null ? new SearchStep<>() : null;

        while(heap.size != 0) {
            Node<V> u = heap.pop();
            if (u == target) {
                heap.clear();
                return u;
            }
            if (!u.isProcessed()) {
                if (processor != null && u.getIndex() > 0) {
                    step.prepare(u);
                    processor.accept(step);
                    if (step.terminate) {
                        heap.clear();
                        return null;
                    }
                    if (step.ignore) continue;
                }
                u.setProcessed(true);
                ArrayList<Connection<V>> outEdges = u.getOutEdges();
                for (Connection<V> e : outEdges) {
                    Node<V> v = e.getNodeB();
                    v.resetAlgorithmAttribs(id);
                    if (!v.isProcessed()) {
                        float newDistance = u.getDistance() + e.getWeight();
                        if (newDistance < v.getDistance()) {
                            v.setDistance(newDistance);
                            v.setPrev(u);
                            v.setConnection(e);
                            if (heuristic != null && !v.isSeen()) {
                                v.setEstimate(heuristic.getEstimate(v.getObject(), target.getObject()));
                            }
                            if (!v.isSeen()) {
                                heap.add(v, v.getDistance() + v.getEstimate());
                            } else {
                                heap.setValue(v, v.getDistance() + v.getEstimate());
                            }
                            v.setIndex(u.getIndex() + 1);
                            v.setSeen(true);
                        }
                    }
                }
            }
        }
        heap.clear();
        return null;
    }
}
