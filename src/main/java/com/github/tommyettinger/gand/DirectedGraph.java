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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.gand.Connection.DirectedConnection;
import com.github.tommyettinger.gand.algorithms.DirectedGraphAlgorithms;

public class DirectedGraph<V> extends Graph<V> implements Json.Serializable {

    final DirectedGraphAlgorithms<V> algorithms;

    //================================================================================
    // Constructors
    //================================================================================

    public DirectedGraph () {
        super();
        algorithms = new DirectedGraphAlgorithms<>(this);
    }

    public DirectedGraph (Collection<V> vertices) {
        super(vertices);
        algorithms = new DirectedGraphAlgorithms<>(this);
    }

    public DirectedGraph(Collection<V> vertices, Collection<Edge<V>> edges, float defaultEdgeWeight) {
        super(vertices, edges, defaultEdgeWeight);
        algorithms = new DirectedGraphAlgorithms<>(this);
    }

    public DirectedGraph(Graph<V> graph) {
        super(graph);
        algorithms = new DirectedGraphAlgorithms<>(this);
    }

    //================================================================================
    // Superclass implementations
    //================================================================================

    @Override
    protected Connection<V> obtainEdge() {
        return new DirectedConnection<>();
    }

    @Override
    public DirectedGraph<V> createNew() {
        return new DirectedGraph<>();
    }

    @Override
    public DirectedGraphAlgorithms<V> algorithms() {
        return algorithms;
    }


    //================================================================================
    // Misc
    //================================================================================

    /**
     * @return the out degree of this vertex, or -1 if it is not in the graph
     */
    public int getOutDegree(V v) {
        Node<V> node = getNode(v);
        return node == null ? -1 : node.getOutDegree();
    }

    /**
     * @return the in degree of this vertex, or -1 if it is not in the graph
     */
    public int getInDegree(V v) {
        Node<V> node = getNode(v);
        return node == null ? -1 : node.getInDegree();
    }

    /**
     * Get a collection containing all the edges which have v as a head.
     * That is, for every edge e in the collection, e = (u, v) for some vertex u.
     * @param v the vertex which all edges will have as a head
     * @return an unmodifiable collection of edges
     */
    public Collection<Edge<V>> getInEdges(V v) {
        Node<V> node = getNode(v);
        if (node==null) return null;
        return Collections.unmodifiableCollection(node.getInEdges());
    }

    /**
     * Sort the vertices of this graph in topological order. That is, for every edge from vertex u to vertex v, u comes before v in the ordering.
     * This is reflected in the iteration order of the collection returned by {@link Graph#getVertices()}.
     * Note that the graph cannot contain any cycles for a topological order to exist. If a cycle exists, this method will do nothing.
     * @return true if the sort was successful, false if the graph contains a cycle
     */
    public boolean topologicalSort() {
        return nodeMap.topologicalSort();
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
