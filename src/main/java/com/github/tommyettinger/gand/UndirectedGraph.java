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

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.gand.Connection.UndirectedConnection;
import com.github.tommyettinger.gand.algorithms.UndirectedGraphAlgorithms;

import java.util.Collection;
import java.util.Set;

public class UndirectedGraph<V> extends Graph<V> implements Json.Serializable {

    UndirectedGraphAlgorithms<V> algorithms;

    //================================================================================
    // Constructors
    //================================================================================

    public UndirectedGraph() {
        super();
        algorithms = new UndirectedGraphAlgorithms<>(this);
    }

    public UndirectedGraph(Collection<V> vertices) {
        super(vertices);
        algorithms = new UndirectedGraphAlgorithms<>(this);
    }

    public UndirectedGraph(Collection<V> vertices, Collection<Edge<V>> edges, float defaultEdgeWeight) {
        super(vertices, edges, defaultEdgeWeight);
        algorithms = new UndirectedGraphAlgorithms<>(this);
    }

    public UndirectedGraph(Graph<V> graph) {
        super(graph);
        algorithms = new UndirectedGraphAlgorithms<>(this);
    }


    //================================================================================
    // Graph building
    //================================================================================

    @Override
    protected UndirectedConnection<V> obtainEdge() {
        return new UndirectedConnection<>();
    }

    @Override
    Connection<V> addConnection(Node<V> a, Node<V> b, float weight) {
        Connection<V> e = a.getEdge(b);
        if (e == null) {
            UndirectedConnection<V> e1 = obtainEdge(), e2 = obtainEdge();
            e1.link(e2);
            e2.link(e1);
            e1.set(a, b, weight);
            e2.set(b, a, weight);
            a.addEdge(e1);
            b.addEdge(e2);
            edgeSet.add(e1);
            e = e1;
        } else {
            e.setWeight(weight);
        }
        return e;
    }

    @Override
    Connection<V> addConnection(Node<V> a, Node<V> b) {
        Connection<V> e = a.getEdge(b);
        return e != null ? edgeSet.get(e) : addConnection(a, b, getDefaultEdgeWeight());
    }

    @Override
    boolean removeConnection(Node<V> a, Node<V> b) {
        Connection<V> e = a.removeEdge(b);
        if (e == null) return false;
        b.removeEdge(a);
        edgeSet.remove(e);
        return true;
    }

    @Override
    Connection<V> getEdge(Node<V> a, Node<V> b) {
        Connection<V> edge = a.getEdge(b);
        // get from ObjectOrderedSet to ensure consistent instance is returned
        return edge == null ? null : edgeSet.get(edge);
    }


    //================================================================================
    // Superclass implementations
    //================================================================================

    @Override
    public boolean isDirected() {
        return false;
    }

    @Override
    public UndirectedGraph<V> createNew() {
        return new UndirectedGraph<>();
    }

    @Override
    public UndirectedGraphAlgorithms<V> algorithms() {
        return algorithms;
    }


    //================================================================================
    // Misc
    //================================================================================

    /**
     * @return the degree of this vertex, or -1 if it is not in the graph
     */
    public int getDegree(V v) {
        Node<V> node = getNode(v);
        return node == null ? -1 : node.getOutDegree();
    }

    @Override
    public void write(Json json) {
        Set<?> vertices = getVertices();
        json.writeArrayStart("v");
        for(Object vertex : vertices) {
            json.writeValue(vertex, null);
        }
        json.writeArrayEnd();
        Collection<? extends Edge<?>> edges = getEdges();
        json.writeArrayStart("e");
        for(Edge<?> edge : edges) {
            json.writeValue(edge.getA(), null);
            json.writeValue(edge.getB(), null);
            json.writeValue(edge.getWeight(), float.class);
        }
        json.writeArrayEnd();

    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        this.removeAllVertices();
        JsonValue entry = jsonData.getChild("v");
        for (; entry != null; entry = entry.next) {
            addVertex(json.readValue(null, entry));
        }
        entry = jsonData.getChild("e");
        for (; entry != null; entry = entry.next) {
            addEdge(json.readValue(null, entry), json.readValue(null, entry = entry.next), (entry = entry.next).asFloat());
        }
    }
}
