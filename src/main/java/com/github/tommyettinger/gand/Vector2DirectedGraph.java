package com.github.tommyettinger.gand;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.NumberUtils;

import java.util.Collection;
import java.util.Set;

public class Vector2DirectedGraph extends DirectedGraph<Vector2> implements Json.Serializable {

    public Vector2DirectedGraph() {
        super();
    }

    public Vector2DirectedGraph(Collection<Vector2> vertices) {
        super(vertices);
    }

    public Vector2DirectedGraph(Collection<Vector2> vertices, Collection<Edge<Vector2>> edges, float defaultEdgeWeight) {
        super(vertices, edges, defaultEdgeWeight);
    }

    public Vector2DirectedGraph(Graph<Vector2> graph) {
        super(graph);
    }

    /**
     * Given a 2D boolean array, adds a vertex where a cell in {@code validGrid} is true, or ignores it otherwise.
     * This only adds vertices, not edges.
     * @param validGrid a 2D boolean array where true means to add that vertex; may be jagged, but this will just use its largest dimensions then
     * @param defaultEdgeWeight the default edge weight to use when a weight is unspecified
     */
    public Vector2DirectedGraph(boolean[][] validGrid, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int x = 0; x < validGrid.length; x++) {
            for (int y = 0; y < validGrid[x].length; y++) {
                if(validGrid[x][y])
                    addVertex(new Vector2(x, y));
            }
        }
    }

    /**
     * Given a 2D char array, adds a vertex where a cell in {@code validGrid} is equal to {@code validChar}, or ignores
     * it otherwise.
     * This only adds vertices, not edges.
     * @param validGrid a 2D char array where {@code validChar} means to add that vertex; may be jagged, but this will just use its largest dimensions then
     * @param validChar the char that, when found in {@code validGrid}, means a vertex will be added
     * @param defaultEdgeWeight the default edge weight to use when a weight is unspecified
     */
    public Vector2DirectedGraph(char[][] validGrid, char validChar, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int x = 0; x < validGrid.length; x++) {
            for (int y = 0; y < validGrid[x].length; y++) {
                if(validGrid[x][y] == validChar)
                    addVertex(new Vector2(x, y));
            }
        }
    }

    /**
     * Given a 2D float array, adds a vertex where a cell in {@code validGrid} has a value between
     * {@code minimumThreshold} and {@code maximumThreshold}, both inclusive, or ignores it otherwise.
     * This only adds vertices, not edges.
     * @param validGrid a 2D float array; may be jagged, but this will just use its largest dimensions then
     * @param minimumThreshold the minimum inclusive value in {@code validGrid} to allow as a vertex
     * @param maximumThreshold the maximum inclusive value in {@code validGrid} to allow as a vertex
     * @param defaultEdgeWeight the default edge weight to use when a weight is unspecified
     */
    public Vector2DirectedGraph(float[][] validGrid, float minimumThreshold, float maximumThreshold, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int x = 0; x < validGrid.length; x++) {
            for (int y = 0; y < validGrid[x].length; y++) {
                if(validGrid[x][y] >= minimumThreshold && validGrid[x][y] <= maximumThreshold)
                    addVertex(new Vector2(x, y));
            }
        }
    }

    @Override
    public Vector2DirectedGraph createNew() {
        return new Vector2DirectedGraph();
    }

    /**
     * Get the hash used to calculate the index in the table at which the Node<V> associated with
     * v would be held. What this returns is also used in {@link Node#mapHash}.
     *
     * @param gp a non-null Vector2 to hash
     */
    @Override
    public int hash(Vector2 gp) {
//        // Harmonious numbers
        return (int)(NumberUtils.floatToIntBits(gp.x) * 0xC13FA9A902A6328FL + NumberUtils.floatToIntBits(gp.y) * 0x91E10DA5C79E7B1DL >>> 32);
//        // int-based
//        return (gp.x * 0xC13FB ^ gp.y * 0x91E0F);
//        return gp.x ^ gp.y * 107;
//        // Cantor pairing function
//        return gp.y + ((gp.x + gp.y) * (gp.x + gp.y + 1) >> 1);
//        // Rosenberg-Strong pairing function
//        final int max = Math.max(gp.x, gp.y);
//        return (max * max + max + gp.x - gp.y);
//        // Rosenberg-Strong followed up with an XLCG that's GWT-safe
//        final int max = Math.max(gp.x, gp.y);
//        return (max * max + max + gp.x - gp.y) * 0x9E373 ^ 0x7F4A7C15;
    }

    @Override
    public String toString() {
        return "Vector2DirectedGraph: { size=" + size() + " }";
    }

    @Override
    public void write(Json json) {
        Set<?> vertices = getVertices();
        json.writeArrayStart("v");
        for(Object vertex : vertices) {
            json.writeValue(vertex, Vector2.class);
        }
        json.writeArrayEnd();
        Collection<? extends Edge<?>> edges = getEdges();
        json.writeArrayStart("e");
        for(Edge<?> edge : edges) {
            json.writeValue(edge.getA(), Vector2.class);
            json.writeValue(edge.getB(), Vector2.class);
            json.writeValue(edge.getWeight(), float.class);
        }
        json.writeArrayEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        this.removeAllVertices();
        JsonValue entry = jsonData.getChild("v");
        for (; entry != null; entry = entry.next) {
            addVertex(json.readValue(Vector2.class, entry));
        }
        entry = jsonData.getChild("e");
        for (; entry != null; entry = entry.next) {
            addEdge(json.readValue(Vector2.class, entry), json.readValue(Vector2.class, entry = entry.next), (entry = entry.next).asFloat());
        }
    }
}
