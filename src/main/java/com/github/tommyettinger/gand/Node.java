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

import com.badlogic.gdx.utils.ObjectMap;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A graph node that wraps a given type of position, V.
 * @param <V> some kind of position, such as a {@link com.github.tommyettinger.gdcrux.PointI2} for a grid
 */
public class Node<V> {

    //================================================================================
    // Graph structure related members
    //================================================================================

    private final V object;

    protected ObjectMap<Node<V>, Connection<V>> neighbors = new ObjectMap<>(8, 0.5f);
    protected ArrayList<Connection<V>> outEdges = new ArrayList<>(8);
    protected ArrayList<Connection<V>> inEdges;

    //================================================================================
    // Node map fields
    //================================================================================

    public final int mapHash;
    Node<V> nextInOrder = null, prevInOrder = null;
    Node<V> nextInBucket = null;

    //================================================================================
    // Algorithm fields
    //================================================================================

    // util fields for algorithms, don't store data in them
    private boolean processed;
    private boolean seen;
    private float distance;
    private float estimate;
    private Node<V> prev;
    private Connection<V> connection;
    private int index;
    private int lastRunID = -1;

    //================================================================================
    // Heap fields
    //================================================================================

    public int heapIndex;
    public float heapValue;

    //================================================================================
    // Constructor
    //================================================================================

    public Node(V v, boolean trackInEdges, int objectHash) {
        this.object = v;
        this.mapHash = objectHash;
        if (trackInEdges) setInEdges(new ArrayList<>(8));
    }

    //================================================================================
    // Internal methods
    //================================================================================

    Connection<V> getEdge(Node<V> v) {
        return neighbors.get(v);
    }

    void addEdge(Connection<V> edge) {
        Node<V> to = edge.getNodeB();
        neighbors.put(to, edge);
        getOutEdges().add(edge);
        if (to.getInEdges() != null) to.getInEdges().add(edge);
    }

    Connection<V> removeEdge(Node<V> v) {
        Connection<V> edge = neighbors.remove(v);
        if (edge == null) return null;
        getOutEdges().remove(edge);
        if (v.getInEdges() != null) v.getInEdges().remove(edge);
        return edge;
    }

    //================================================================================
    // Public Methods
    //================================================================================

    public ArrayList<Connection<V>> getConnections() {
        return getOutEdges();
    }

    public V getObject() {
        return object;
    }

    public int getInDegree() {
        return getInEdges() == null ? getOutDegree() : getInEdges().size();
    }

    public int getOutDegree() {
        return getOutEdges().size();
    }

    public void disconnect() {
        neighbors.clear();
        getOutEdges().clear();
        if (getInEdges() != null) getInEdges().clear();
    }

    //================================================================================
    // Algorithm methods
    //================================================================================

    public boolean resetAlgorithmAttribs(int runID) {
        if (runID == this.getLastRunID()) return false;
        setProcessed(false);
        setPrev(null);
        setConnection(null);
        setDistance(Float.MAX_VALUE);
        setEstimate(0);
        setIndex(0);
        setSeen(false);
        this.setLastRunID(runID);
        return true;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getEstimate() {
        return estimate;
    }

    public void setEstimate(float estimate) {
        this.estimate = estimate;
    }

    public Node<V> getPrev() {
        return prev;
    }

    public void setPrev(Node<V> prev) {
        this.prev = prev;
    }

    public Connection<V> getConnection() {
        return connection;
    }

    public void setConnection(Connection<V> connection) {
        this.connection = connection;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLastRunID() {
        return lastRunID;
    }

    public void setLastRunID(int lastRunID) {
        this.lastRunID = lastRunID;
    }


    //================================================================================
    // Misc
    //================================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node<?> node = (Node<?>) o;

        return Objects.equals(object, node.object);
    }

    @Override
    public int hashCode() {
        return mapHash;
    }

    @Override
    public String toString() {
        return "["+object+"]";
    }

    public ArrayList<Connection<V>> getOutEdges() {
        return outEdges;
    }

    public void setOutEdges(ArrayList<Connection<V>> outEdges) {
        this.outEdges = outEdges;
    }

    public ArrayList<Connection<V>> getInEdges() {
        return inEdges;
    }

    public void setInEdges(ArrayList<Connection<V>> inEdges) {
        this.inEdges = inEdges;
    }
}
