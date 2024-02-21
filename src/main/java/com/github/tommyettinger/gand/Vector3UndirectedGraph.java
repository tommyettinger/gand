package com.github.tommyettinger.gand;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.NumberUtils;

import java.util.Collection;
import java.util.Set;

public class Vector3UndirectedGraph extends UndirectedGraph<Vector3> implements Json.Serializable {
    public Vector3UndirectedGraph() {
        super();
    }

    public Vector3UndirectedGraph(Collection<Vector3> vertices) {
        super(vertices);
    }

    public Vector3UndirectedGraph(Collection<Vector3> vertices, Collection<Edge<Vector3>> edges, float defaultEdgeWeight) {
        super(vertices, edges, defaultEdgeWeight);
    }

    public Vector3UndirectedGraph(Graph<Vector3> graph) {
        super(graph);
    }

    /**
     * Given a 3D boolean array, adds a vertex where a cell in {@code validGrid} is true, or ignores it otherwise.
     * This only adds vertices.
     * @param validGrid a 3D boolean array where true means to add that vertex; may be jagged, but this will just use its largest dimensions then
     * @param defaultEdgeWeight the default edge weight to use when a weight is unspecified
     */
    public Vector3UndirectedGraph(boolean[][][] validGrid, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int z = 0; z < validGrid.length; z++) {
            for (int y = 0; y < validGrid[z].length; y++) {
                for (int x = 0; x < validGrid[z][y].length; x++) {
                    if (validGrid[z][y][x])
                        addVertex(new Vector3(x, y, z));
                }
            }
        }
    }

    /**
     * Given a 3D char array, adds a vertex where a cell in {@code validGrid} is equal to {@code validChar}, or ignores
     * it otherwise.
     * This only adds vertices.
     * @param validGrid a 3D char array where {@code validChar} means to add that vertex; may be jagged, but this will just use its largest dimensions then
     * @param validChar the char that, when found in {@code validGrid}, means a vertex will be added
     * @param defaultEdgeWeight the default edge weight to use when a weight is unspecified
     */
    public Vector3UndirectedGraph(char[][][] validGrid, char validChar, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int z = 0; z < validGrid.length; z++) {
            for (int y = 0; y < validGrid[z].length; y++) {
                for (int x = 0; x < validGrid[z][y].length; x++) {
                    if (validGrid[z][y][x] == validChar)
                        addVertex(new Vector3(x, y, z));
                }
            }
        }
    }

    /**
     * Given a 3D float array, adds a vertex where a cell in {@code validGrid} has a value between
     * {@code minimumThreshold} and {@code maximumThreshold}, both inclusive, or ignores it otherwise.
     * This only adds vertices.
     * @param validGrid a 3D float array; may be jagged, but this will just use its largest dimensions then
     * @param minimumThreshold the minimum inclusive value in {@code validGrid} to allow as a vertex
     * @param maximumThreshold the maximum inclusive value in {@code validGrid} to allow as a vertex
     * @param defaultEdgeWeight the default edge weight to use when a weight is unspecified
     */
    public Vector3UndirectedGraph(float[][][] validGrid, float minimumThreshold, float maximumThreshold, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int z = 0; z < validGrid.length; z++) {
            for (int y = 0; y < validGrid[z].length; y++) {
                for (int x = 0; x < validGrid[z][y].length; x++) {
                    if(validGrid[z][y][x] >= minimumThreshold && validGrid[z][y][x] <= maximumThreshold)
                        addVertex(new Vector3(x, y, z));
                }
            }
        }
    }

    @Override
    public Vector3UndirectedGraph createNew() {
        return new Vector3UndirectedGraph();
    }

    /**
     * Get the hash used to calculate the index in the table at which the Node<V> associated with
     * v would be held. What this returns is also used in {@link Node#mapHash}.
     *
     * @param gp a non-null Vector3 to hash
     */
    @Override
    public int hash(Vector3 gp) {
//        // Harmonious numbers
        return (int)(NumberUtils.floatToIntBits(gp.x) * 0xD1B54A32D192ED03L
                + NumberUtils.floatToIntBits(gp.y) * 0xABC98388FB8FAC03L
                + NumberUtils.floatToIntBits(gp.z) * 0x8CB92BA72F3D8DD7L >>> 32);
    }

    @Override
    public String toString() {
        return "Vector3UndirectedGraph: { size=" + size() + " }";
    }

    @Override
    public void write(Json json) {
        Set<?> vertices = getVertices();
        json.writeArrayStart("v");
        for(Object vertex : vertices) {
            json.writeValue(vertex, Vector3.class);
        }
        json.writeArrayEnd();
        Collection<? extends Edge<?>> edges = getEdges();
        json.writeArrayStart("e");
        for(Edge<?> edge : edges) {
            json.writeValue(edge.getA(), Vector3.class);
            json.writeValue(edge.getB(), Vector3.class);
            json.writeValue(edge.getWeight(), float.class);
        }
        json.writeArrayEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        this.removeAllVertices();
        JsonValue entry = jsonData.getChild("v");
        for (; entry != null; entry = entry.next) {
            addVertex(json.readValue(Vector3.class, entry));
        }
        entry = jsonData.getChild("e");
        for (; entry != null; entry = entry.next) {
            addEdge(json.readValue(Vector3.class, entry), json.readValue(Vector3.class, entry = entry.next), (entry = entry.next).asFloat());
        }
    }
}