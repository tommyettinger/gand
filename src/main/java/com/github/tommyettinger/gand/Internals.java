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

import com.github.tommyettinger.gand.ds.ObjectOrderedSet;

import java.util.Collection;

/**
 * Used by {@link Graph} internally; not meant for external usage.
 * <br>
 * This class is public so serialization code can access it, but is also final to try to prevent improper usage.
 * @param <V>
 */
public final class Internals<V> {

    transient final Graph<V> graph;

    Internals(Graph<V> graph) {
        this.graph = graph;
    }

    public Node<V> getNode(V v) {
        return graph.getNode(v);
    }

    public Collection<Node<V>> getNodes() {
        return graph.nodeMap.nodeCollection;
    }

    public ObjectOrderedSet<Connection<V>> getConnections() {
        return graph.edgeSet;
    }

    public void addConnection(Node<V> a, Node<V> b, float weight) {
        graph.addConnection(a, b, weight);
    }
}
