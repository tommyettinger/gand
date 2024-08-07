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

import com.github.tommyettinger.gand.algorithms.Algorithms;
import com.github.tommyettinger.gand.ds.ObjectDeque;
import com.github.tommyettinger.gand.ds.ObjectOrderedSet;
import com.github.tommyettinger.gand.utils.Errors;
import com.github.tommyettinger.gand.utils.GwtIncompatible;
import com.github.tommyettinger.gand.utils.ObjectPredicate;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;


public abstract class Graph<V> implements Externalizable {

    //================================================================================
    // Members
    //================================================================================

    final NodeMap<V> nodeMap;

    /**
     * This is a map so that for undirected graphs, a consistent edge instance can be obtained from
     * either (u, v) or (v, u)
     */
    protected final ObjectOrderedSet<Connection<V>> edgeSet;

    protected transient final Internals<V> internals = new Internals<>(this);

    protected float defaultEdgeWeight = 1;


    //================================================================================
    // Constructors
    //================================================================================

    protected Graph() {
        nodeMap = new NodeMap<>(this);
        edgeSet = new ObjectOrderedSet<>();
    }

    protected Graph(Collection<V> vertices) {
        this();
        for (V v : vertices) {
            addVertex(v);
        }
    }

    protected Graph(Collection<V> vertices, Collection<Edge<V>> edges, float defaultEdgeWeight) {
        nodeMap = new NodeMap<>(this);
        this.setDefaultEdgeWeight(defaultEdgeWeight);
        edgeSet = new ObjectOrderedSet<>(edges.size());
        for (V v : vertices) {
            addVertex(v);
        }
        for(Edge<V> edge : edges) {
            addEdge(edge);
        }
    }

    protected Graph(Graph<V> graph) {
        nodeMap = new NodeMap<>(this);
        this.setDefaultEdgeWeight(graph.getDefaultEdgeWeight());
        Set<Connection<V>> edges = graph.getEdges();
        edgeSet = new ObjectOrderedSet<>(edges.size());
        Set<V> vertices = graph.getVertices();
        for (V v : vertices) {
            addVertex(v);
        }
        for(Edge<V> edge : edges) {
            // Each Edge is guaranteed to be valid here, so we don't need to re-add its vertices.
            addEdge(edge.getA(), edge.getB(), edge.getWeight());
        }
    }

    //================================================================================
    // Graph Builders
    //================================================================================

    //--------------------
    //  Abstract Methods
    //--------------------

    abstract Connection<V> obtainEdge();

    public abstract Graph<V> createNew();

    public abstract Algorithms<V> algorithms();

    //--------------------
    //  Public Methods
    //--------------------

    /**
     * Adds a vertex to the graph.
     *
     * @param v the vertex to be added
     * @return true if the vertex was not already in the graph, false otherwise
     */

    public boolean addVertex(V v) {
        return nodeMap.put(v) != null;
    }

    /**
     * Adds all the vertices in the collection to the graph.
     *
     * @param vertices a collection of vertices to be added
     */
    public void addVertices(Collection<V> vertices) {
        for (V v : vertices) {
            addVertex(v);
        }
    }

    @SafeVarargs
    public final void addVertices(V... vertices) {
        for (V v : vertices) {
            addVertex(v);
        }
    }

    /**
     * Removes a vertex from the graph, and any adjacent edges.
     *
     * @param v the vertex to be removed
     * @return true if the vertex was in the graph, false otherwise
     */
    public boolean removeVertex(V v) {
        Node<V> existing = nodeMap.remove(v);
        if (existing == null) return false;
        disconnect(existing);
        return true;
    }

    public void disconnect(V v) {
        Node<V> existing = nodeMap.get(v);
        if (existing == null) Errors.throwVertexNotInGraphVertexException(false);
        disconnect(existing);
    }

    protected void disconnect(Node<V> node) {
        for (int i = node.getOutEdges().size() - 1; i >= 0; i--) {
            removeConnection(node, node.getOutEdges().get(i).b);
        }
        if (node.getInEdges() != null) {
            for (int i = node.getInEdges().size() - 1; i >= 0; i--) {
                removeConnection(node.getInEdges().get(i).a, node);
            }
        }
        node.disconnect();
    }

    /**
     * Removes all the vertices in the collection from the graph, and any adjacent edges.
     *
     * @param vertices vertices a collection of vertices to be removed
     */
    public void removeVertices(Collection<V> vertices) {
        for (V v : vertices) {
            removeVertex(v);
        }
    }

    public void removeVertexIf(final ObjectPredicate<V> predicate) {
        Collection<V> existing = getVertices();
        ObjectDeque<V> vertices = new ObjectDeque<>(existing.size());
        for(V v : existing){
            if(predicate.test(v)) vertices.add(v);
        }
        removeVertices(vertices);
    }

    /**
     * Add an edge to the graph, from v to w. The edge will have a default weight of 1.
     * If there is already an edge between v and w, its weight will be set to 1.
     *
     * @param v the tail vertex of the edge
     * @param w the head vertex of the edge
     * @return the edge
     */
    public Connection<V> addEdge(V v, V w) {
        return addEdge(v, w, getDefaultEdgeWeight());
    }

    /**
     * Add an edge to the graph, with the same endpoints as the given edge. If the endpoints are not in the graph they
     * will be added.
     * If there is already an edge between v and w, its weight will be set to the weight of given edge.
     *
     * @param edge an edge (possibly from another graph)
     * @return the edge belonging to this graph
     */
    public Connection<V> addEdge(Edge<V> edge) {
        addVertex(edge.getA());
        addVertex(edge.getB());
        return addEdge(edge.getA(), edge.getB(), edge.getWeight());
    }

    /**
     * Add an edge to the graph, from v to w and with the specified weight.
     * If there is already an edge between v and w, its weight will be set to the specified weight.
     *
     * @param v      the tail vertex of the edge
     * @param w      the head vertex of the edge
     * @param weight the weight of the edge
     * @return the edge
     */
    public Connection<V> addEdge(V v, V w, float weight) {
        if (v == null || w == null) Errors.throwNullVertexException();
        if (v.equals(w)) Errors.throwSameVertexException();
        Node<V> a = getNode(v);
        Node<V> b = getNode(w);
        if (a == null || b == null) Errors.throwVertexNotInGraphVertexException(true);
        return addConnection(a, b, weight);
    }

    /**
     * Removes the edge from v to w from the graph.
     *
     * @param v the tail vertex of the edge
     * @param w the head vertex of the edge
     * @return the edge if there exists an edge from v to w, or null if there is no edge
     */
    public boolean removeEdge(V v, V w) {
        Node<V> a = getNode(v), b = getNode(w);
        if (a == null || b == null) Errors.throwVertexNotInGraphVertexException(true);
        return removeConnection(a, b);
    }

    public boolean removeEdge(Edge<V> edge) {
        return removeConnection(edge.getInternalNodeA(), edge.getInternalNodeB());
    }

    public void removeEdges(Collection<? extends Edge<V>> edges) {
        for (Edge<V> e : edges) {
            removeConnection(e.getInternalNodeA(), e.getInternalNodeB());
        }
    }

    public void removeEdgeIf(final ObjectPredicate<Edge<V>> predicate) {
        Collection<Connection<V>> existing = getEdges();
        ObjectDeque<Edge<V>> edges = new ObjectDeque<>(existing.size());
        for(Edge<V> v : existing){
            if(predicate.test(v)) edges.add(v);
        }
        removeEdges(edges);
    }

    /**
     * Removes all edges from the graph.
     */
    public void removeAllEdges() {
        for (Node<V> v : getNodes()) {
            v.disconnect();
        }
        edgeSet.clear();
    }

    /**
     * Removes all vertices and edges from the graph.
     */
    public void removeAllVertices() {
        edgeSet.clear();
        nodeMap.clear();
    }

    /**
     * An alias for {@link #removeAllVertices()}, this removes all vertices and all edges from the graph.
     */
    public void clear() {
        removeAllVertices();
    }

    /**
     * Sort the vertices using the provided comparator. This is reflected in the iteration order of the collection returned
     * by {@link #getVertices()}, as well as algorithms which involve iterating over all vertices.
     *
     * @param comparator a comparator for comparing vertices
     */
    public void sortVertices(Comparator<V> comparator) {
        nodeMap.sort(comparator);
    }

    /**
     * Sort the edges using the provided comparator. This is reflected in the iteration order of the collection returned
     * by {@link #getEdges()}, as well as algorithms which involve iterating over all edges.
     *
     * @param comparator a comparator for comparing edges
     */
    public void sortEdges(final Comparator<Connection<V>> comparator) {
        edgeSet.order().sort(comparator);
    }


    /**
     * Get the hash used to calculate the index in the table at which the Node<V> associated with
     * v would be held. What this returns is also used in {@link Node#mapHash}.
     * <br>
     * The default implementation performs an invertible bitwise operation on the hashCode() it receives and returns the
     * result. The invertible bitwise operation is to XOR an input with two different bitwise rotations of that input;
     * it is important that it is invertible because otherwise there would be values this could never return, producing
     * unnecessary gaps in hash tables. This uses int math only because any math involving long values is drastically
     * slower on GWT (and somewhat slower on TeaVM).
     * <br>
     * This can be overridden by Graph subclasses that know more about their vertex type to use a more appropriate
     * mixing algorithm.
     */
    public int hash(V v) {
        // used in version 0.2.0
//        return (int)(v.hashCode() * 0xABC98388FB8FAC03L >>> 25);

        // avoids math on longs, which is quite slow on GWT
        final int h = v.hashCode();
        return h ^ (h << 23 | h >>> 9) ^ (h << 7 | h >>> 25);

        // The original mixer used here.
//        final int h = v.hashCode();
//        return h ^ h >>> 16;
    }

    //--------------------
    //  Internal Methods
    //--------------------

    Connection<V> addConnection(Node<V> a, Node<V> b) {
        Connection<V> e = a.getEdge(b);
        return e != null ? e : addConnection(a, b, getDefaultEdgeWeight());
    }

    Connection<V> addConnection(Node<V> a, Node<V> b, float weight) {
        Connection<V> e = a.getEdge(b);
        if (e == null) {
            e = obtainEdge();
            e.set(a, b, weight);
            a.addEdge(e);
            edgeSet.add(e);
        } else {
            e.setWeight(weight);
        }
        return e;
    }

    boolean removeConnection(Node<V> a, Node<V> b) {
        return removeConnection(a, b, true);
    }

    boolean removeConnection(Node<V> a, Node<V> b, boolean removeFromMap) {
        Connection<V> e = a.removeEdge(b);
        if (e == null) return false;
        if (removeFromMap) edgeSet.remove(e);
        return true;
    }

    //================================================================================
    // Getters
    //================================================================================

    //--------------------
    //  Public Getters
    //--------------------

    /**
     * Check if the graph contains a vertex.
     *
     * @param v the vertex with which to check
     * @return true if the graph contains the vertex, false otherwise
     */
    public boolean contains(V v) {
        return nodeMap.contains(v);
    }

    /**
     * Retrieve the edge which is from v to w.
     *
     * @param v the tail vertex of the edge
     * @param w the head vertex of the edge
     * @return the edge if it is in the graph, otherwise null
     */
    public Edge<V> getEdge(V v, V w) {
        Node<V> a = getNode(v), b = getNode(w);
        if (a == null || b == null) Errors.throwVertexNotInGraphVertexException(true);
        Connection<V> edge = getEdge(a, b);
        if (edge == null) return null;
        return edge;
    }

    /**
     * Check if the graph contains an edge from v to w.
     *
     * @param v the tail vertex of the edge
     * @param w the head vertex of the edge
     * @return true if the edge is in the graph, false otherwise
     */
    public boolean edgeExists(V v, V w) {
        Node<V> a = getNode(v), b = getNode(w);
        if (a == null || b == null) Errors.throwVertexNotInGraphVertexException(true);
        return connectionExists(a, b);
    }

    /**
     * Get a collection containing all the edges which have v as a tail.
     * That is, for every edge e in the collection, e = (v, u) for some vertex u.
     *
     * @param v the vertex which all edges will have as a tail
     * @return an unmodifiable collection of edges
     */
    public Collection<Edge<V>> getEdges(V v) {
        Node<V> node = getNode(v);
        if (node == null) return null;
        return Collections.unmodifiableCollection(node.getOutEdges());
    }

    /**
     * <p>Get a collection containing all the edges in the graph.</p>
     *
     * <p>Note that for an undirected graph, there is no guarantee on the order of the vertices.
     * For example if there exists and edge between u and v, the returned collection will contain
     * exactly one edge for which either edge.getA().equals(u) and edge.getB().equals(v), or
     * edge.getA().equals(v) and edge.getB().equals(u). See {@link Edge#hasEndpoints(Object, Object)}.</p>
     *
     * @return an unmodifiable collection of all the edges in the graph
     */
    public Set<Connection<V>> getEdges() {
        return Collections.unmodifiableSet(edgeSet);
    }

    /**
     * Get a collection containing all the vertices in the graph.
     *
     * @return an unmodifiable collection of all the vertices in the graph
     */
    public Set<V> getVertices() {
        return nodeMap.vertexSet;
    }

    /**
     * Given a V {@code vertex} that may be equivalent (but not necessarily identical by reference) to a vertex in this
     * Graph, this gets and returns a vertex that is considered equivalent if one is present, or returns null otherwise.
     * This can be helpful if you have a {@code V} that can be modified, which can be used to check the presence of
     * vertices in the Graph but cannot be actually placed into the Graph (because vertices in a Graph cannot be
     * modified).
     * @param vertex a {@code V} that is used to check if an equivalent vertex is present in the Graph
     * @return an equivalent vertex to {@code vertex}, if one was present, or null otherwise
     */
    public V getStoredVertex(V vertex) {
        Node<V> node = getNode(vertex);
        if(node == null) return null;
        return node.getObject();
    }

    /**
     * Check if the graph is directed, that is whether the edges form an ordered pair or a set.
     *
     * @return whether the graph is directed
     */
    public boolean isDirected() {
        return true;
    }

    /**
     * Get the number of vertices in the graph.
     *
     * @return the number of vertices
     */
    public int size() {
        return nodeMap.size;
    }

    /**
     * Get the number of edges in the graph.
     *
     * @return the number of edges
     */
    public int getEdgeCount() {
        return edgeSet.size();
    }


    public Internals<V> internals() {
        return internals;
    }

    /**
     * Get the current default edge weight. If none has been set, the default is 1f.
     *
     * @return the current default edge weight
     */
    public float getDefaultEdgeWeight() {
        return defaultEdgeWeight;
    }

    /**
     * Set the default edge weight, which will be given to every edge for which the edge weight is not specified.
     *
     * @param defaultEdgeWeight the edge weight
     */
    public void setDefaultEdgeWeight(float defaultEdgeWeight) {
        this.defaultEdgeWeight = defaultEdgeWeight;
    }

    /**
     * @return whether the graph is connected
     */
    public boolean isConnected() {
        return numberOfComponents() == 1;
    }

    public int numberOfComponents() {
        for(Node<V> node : getNodes())
            node.setSeen(false);
        int[] visited = {0};
        int components = 0;
        Iterator<V> iter = getVertices().iterator();
        while (iter.hasNext() && visited[0] < size()) {
            V n = iter.next();
            if(nodeMap.get(n).isSeen()) continue;
            ++components;
            algorithms().depthFirstSearch(n, v -> {
                visited[0] += 1;
            });
        }
        return components;
    }

    /**
     * Gets a new ArrayList of Graph items (of the same class as this Graph), where each sub-graph only has connections
     * to its own vertices. Each Graph in the returned list will have no connections to vertices in the other graphs.
     * @return a new ArrayList of Graph items where each Graph is not connected to any other Graph
     */
    public ArrayList<Graph<V>> getComponents() {
        for(Node<V> node : getNodes())
            node.setSeen(false);
        int[] visited = {0};
        ArrayList<Graph<V>> components = new ArrayList<>(16);
        Iterator<V> iter = getVertices().iterator();
        while (iter.hasNext() && visited[0] < size()) {
            V n = iter.next();
            if(nodeMap.get(n).isSeen()) continue;
            final Graph<V> comp = createNew();
            algorithms().depthFirstSearch(n, v -> {
                comp.addVertex(v.vertex());
                comp.edgeSet.addAll(v.neighbors());
                ++visited[0];
            });
            components.add(comp);
        }
        return components;
    }


    /**
     * Gets the sub-graph that is the largest Graph (of the same class as this Graph) within this one, that only has
     * connections to its own vertices. This acts like calling {@link #getComponents()} and finding the largest
     * component Graph inside it, except that this can be faster by returning early when there is no possible larger
     * sub-graph.
     * @return a new Graph made from the largest connected area of vertices (and edges) from this
     */
    public Graph<V> largestComponent() {
        for(Node<V> node : getNodes())
            node.setSeen(false);
        int[] visited = {0};
        Iterator<V> iter = getVertices().iterator();
        Graph<V> best = null;
        while (iter.hasNext() && visited[0] < size()) {
            V n = iter.next();
            if(nodeMap.get(n).isSeen()) continue;
            final Graph<V> work = createNew();
            algorithms().depthFirstSearch(n, v -> {
                work.addVertex(v.vertex());
                work.edgeSet.addAll(v.neighbors());
                ++visited[0];
            });
            if(best == null || work.size() > best.size()) {
                if(work.size() >= size() - visited[0])
                    return work;
                best = work;
            }
        }
        if(best == null)
            best = createNew();
        return best;
    }

    //--------------------
    //  Internal Getters
    //--------------------

    Node<V> getNode(V v) {
        return nodeMap.get(v);
    }

    Collection<Node<V>> getNodes() {
        return nodeMap.nodeCollection;
    }

    boolean connectionExists(Node<V> u, Node<V> v) {
        return u.getEdge(v) != null;
    }

    Connection<V> getEdge(Node<V> a, Node<V> b) {
        return a.getEdge(b);
    }


    /**
     * Meant for serialization using <a href="https://fury.apache.org">Fury</a>.
     * If a class overrides this with different behavior, {@link #readExternal(ObjectInput)}
     * must also be overridden to match that behavior.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     * @serialData <ul>
     *     <li>int nv: the number of vertices</li>
     *     <li>object[nv] vertices: a sequence of vertex objects, with count equal to nv</li>
     *     <li>int ne: the number of edges</li>
     *     <li>triple[ne] edges: interleaved in a flat sequence; for each triple:
     *     <ul>
     *         <li>object vertexA</li>
     *         <li>object vertexB</li>
     *         <li>float weight</li>
     *     </ul>
     *     </li>
     * </ul>
     */
    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        Set<?> vertices = getVertices();
        out.writeInt(vertices.size());
        for(Object vertex : vertices) {
            out.writeObject(vertex);
        }
        Collection<? extends Edge<?>> edges = getEdges();
        out.writeInt(edges.size());
        for(Edge<?> edge : edges) {
            out.writeObject(edge.getA());
            out.writeObject(edge.getB());
            out.writeFloat(edge.getWeight());
        }
    }

    /**
     * Meant for deserialization using <a href="https://fury.apache.org">Fury</a>.
     * If a class overrides this with different behavior, {@link #writeExternal(ObjectOutput)}
     * must also be overridden to match that behavior.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException            if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     *                                restored cannot be found.
     */
    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.removeAllVertices();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            addVertex((V) in.readObject());
        }
        count = in.readInt();
        for (int i = 0; i < count; i++) {
            addEdge((V) in.readObject(), (V) in.readObject(), in.readFloat());
        }
    }

    @Override
    public String toString() {
        return (isDirected() ? "Directed" : "Undirected") + " graph with " +
                size() + " vertices and " + getEdgeCount() + " edges";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Graph<?> graph = (Graph<?>) o;

        if (!nodeMap.vertexSet.equals(graph.nodeMap.vertexSet)) return false;
        return edgeSet.equals(graph.edgeSet);
    }

    @Override
    public int hashCode() {
        int result = nodeMap.hashCode();
        result = 31 * result + edgeSet.hashCode();
        return result;
    }
}
