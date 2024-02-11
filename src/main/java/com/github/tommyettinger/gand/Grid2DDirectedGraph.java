package com.github.tommyettinger.gand;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.gand.utils.Heuristic;

import java.util.Collection;
import java.util.Set;

public class Grid2DDirectedGraph extends DirectedGraph<GridPoint2> implements Json.Serializable {

    private int width = 0;
    private int height = 0;
    /**
     * Adds a vertex to the graph.
     *
     * @param gridPoint2 the vertex to be added
     * @return true if the vertex was not already in the graph, false otherwise
     */
    @Override
    public boolean addVertex(GridPoint2 gridPoint2) {
        if(super.addVertex(gridPoint2))
        {
            width = Math.max(width, gridPoint2.x);
            height = Math.max(height, gridPoint2.y);
            return true;
        }
        return false;
    }

    public Grid2DDirectedGraph() {
        super();
    }

    public Grid2DDirectedGraph(Collection<GridPoint2> vertices) {
        super(vertices);
    }

    public Grid2DDirectedGraph(Collection<GridPoint2> vertices, Collection<Edge<GridPoint2>> edges, float defaultEdgeWeight) {
        super(vertices, edges, defaultEdgeWeight);
    }

    public Grid2DDirectedGraph(Graph<GridPoint2> graph) {
        super(graph);
    }

    /**
     * Given a 2D boolean array, adds a vertex where a cell in {@code validGrid} is true, or ignores it otherwise.
     * This only adds vertices; to automatically add edges you can use {@link #connectAdjacent(Heuristic, boolean)}.
     * @param validGrid a 2D boolean array where true means to add that vertex; may be jagged, but this will just use its largest dimensions then
     * @param defaultEdgeWeight the default edge weight to use when a weight is unspecified
     */
    public Grid2DDirectedGraph(boolean[][] validGrid, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int x = 0; x < validGrid.length; x++) {
            for (int y = 0; y < validGrid[x].length; y++) {
                if(validGrid[x][y])
                    addVertex(new GridPoint2(x, y));
            }
        }
    }

    /**
     * Given a 2D char array, adds a vertex where a cell in {@code validGrid} is equal to {@code validChar}, or ignores
     * it otherwise.
     * This only adds vertices; to automatically add edges you can use {@link #connectAdjacent(Heuristic, boolean)}.
     * @param validGrid a 2D char array where {@code validChar} means to add that vertex; may be jagged, but this will just use its largest dimensions then
     * @param validChar the char that, when found in {@code validGrid}, means a vertex will be added
     * @param defaultEdgeWeight the default edge weight to use when a weight is unspecified
     */
    public Grid2DDirectedGraph(char[][] validGrid, char validChar, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int x = 0; x < validGrid.length; x++) {
            for (int y = 0; y < validGrid[x].length; y++) {
                if(validGrid[x][y] == validChar)
                    addVertex(new GridPoint2(x, y));
            }
        }
    }

    /**
     * Given a 2D float array, adds a vertex where a cell in {@code validGrid} has a value between
     * {@code minimumThreshold} and {@code maximumThreshold}, both inclusive, or ignores it otherwise.
     * This only adds vertices; to automatically add edges you can use {@link #connectAdjacent(Heuristic, boolean)}.
     * @param validGrid a 2D float array; may be jagged, but this will just use its largest dimensions then
     * @param minimumThreshold the minimum inclusive value in {@code validGrid} to allow as a vertex
     * @param maximumThreshold the maximum inclusive value in {@code validGrid} to allow as a vertex
     * @param defaultEdgeWeight the default edge weight to use when a weight is unspecified
     */
    public Grid2DDirectedGraph(float[][] validGrid, float minimumThreshold, float maximumThreshold, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int x = 0; x < validGrid.length; x++) {
            for (int y = 0; y < validGrid[x].length; y++) {
                if(validGrid[x][y] >= minimumThreshold && validGrid[x][y] <= maximumThreshold)
                    addVertex(new GridPoint2(x, y));
            }
        }
    }

    /**
     * Adds edges between all pairs of adjacent vertices, where adjacency is 4-way if {@code permitDiagonal} is false,
     * or 8-way if it is true. The given Heuristic is used to determine the edge weight for any connection, unless
     * {@code heu} is null, in which case the {@link #getDefaultEdgeWeight()} is used for all edges.
     * @param heu used to calculate the weight for each edge; may be null to use {@link #getDefaultEdgeWeight()}
     * @param permitDiagonal if false, this will use 4-way adjacency only; if true, it will use 8-way
     */
    public void connectAdjacent(Heuristic<GridPoint2> heu, boolean permitDiagonal) {
        GridPoint2 test = new GridPoint2(), next = new GridPoint2(), t;
        Node<GridPoint2> nmt, nmn;
        if(heu == null) heu = (a, b) -> getDefaultEdgeWeight();
        for (int x = 0; x <= width; x++) {
            test.x = x;
            for (int y = 0; y <= height; y++) {
                test.y = y;
                if((nmt = nodeMap.get(test)) != null){
                    t = nmt.getObject();
                    if(permitDiagonal){
                        next.x = x-1; next.y = y-1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                        next.x = x+1; next.y = y-1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                        next.x = x-1; next.y = y+1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                        next.x = x+1; next.y = y+1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                    }
                    next.x = x; next.y = y-1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                    next.x = x-1; next.y = y; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                    next.x = x+1; next.y = y; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                    next.x = x; next.y = y+1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                }
            }
        }
    }

    /**
     * Get the hash used to calculate the index in the table at which the Node<V> associated with
     * v would be held. What this returns is also used in {@link Node#mapHash}.
     *
     * @param gp a non-null GridPoint2 to hash
     */
    @Override
    public int hash(GridPoint2 gp) {
        // Rosenberg-Strong followed up with an XLCG that's GWT-safe
        final int max = Math.max(gp.x, gp.y);
        return (max * max + max + gp.x - gp.y) * 0x9E373 ^ 0x7F4A7C15;
    }

    @Override
    public void write(Json json) {
        Set<?> vertices = getVertices();
        json.writeArrayStart("v");
        for(Object vertex : vertices) {
            json.writeValue(vertex, GridPoint2.class);
        }
        json.writeArrayEnd();
        Collection<? extends Edge<?>> edges = getEdges();
        json.writeArrayStart("e");
        for(Edge<?> edge : edges) {
            json.writeValue(edge.getA(), GridPoint2.class);
            json.writeValue(edge.getB(), GridPoint2.class);
            json.writeValue(edge.getWeight(), float.class);
        }
        json.writeArrayEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        this.removeAllVertices();
        JsonValue entry = jsonData.getChild("v");
        for (; entry != null; entry = entry.next) {
            addVertex(json.readValue(GridPoint2.class, entry));
        }
        entry = jsonData.getChild("e");
        for (; entry != null; entry = entry.next) {
            addEdge(json.readValue(GridPoint2.class, entry), json.readValue(GridPoint2.class, entry = entry.next), (entry = entry.next).asFloat());
        }

    }
}
