package com.github.tommyettinger.gand;

import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.gand.utils.Heuristic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class Grid3DirectedGraph extends DirectedGraph<GridPoint3> implements Json.Serializable {

    private int width = 0;
    private int height = 0;
    private int depth = 0;
    /**
     * Adds a vertex to the graph.
     *
     * @param gridPoint2 the vertex to be added
     * @return true if the vertex was not already in the graph, false otherwise
     */
    @Override
    public boolean addVertex(GridPoint3 gridPoint2) {
        if(super.addVertex(gridPoint2))
        {
            width = Math.max(width, gridPoint2.x+1);
            height = Math.max(height, gridPoint2.y+1);
            depth = Math.max(depth, gridPoint2.z+1);
            return true;
        }
        return false;
    }

    public Grid3DirectedGraph() {
        super();
    }

    public Grid3DirectedGraph(Collection<GridPoint3> vertices) {
        super(vertices);
    }

    public Grid3DirectedGraph(Collection<GridPoint3> vertices, Collection<Edge<GridPoint3>> edges, float defaultEdgeWeight) {
        super(vertices, edges, defaultEdgeWeight);
    }

    public Grid3DirectedGraph(Graph<GridPoint3> graph) {
        super(graph);
    }

    /**
     * Given a 3D boolean array, adds a vertex where a cell in {@code validGrid} is true, or ignores it otherwise.
     * This only adds vertices; to automatically add edges you can use {@link #connectAdjacent(Heuristic, boolean)}.
     * @param validGrid a 3D boolean array where true means to add that vertex; may be jagged, but this will just use its largest dimensions then
     * @param defaultEdgeWeight the default edge weight to use when a weight is unspecified
     */
    public Grid3DirectedGraph(boolean[][][] validGrid, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int z = 0; z < validGrid.length; z++) {
            for (int y = 0; y < validGrid[z].length; y++) {
                for (int x = 0; x < validGrid[z][y].length; x++) {
                    if (validGrid[z][y][x])
                        addVertex(new GridPoint3(x, y, z));
                }
            }
        }
    }

    /**
     * Given a 3D char array, adds a vertex where a cell in {@code validGrid} is equal to {@code validChar}, or ignores
     * it otherwise.
     * This only adds vertices; to automatically add edges you can use {@link #connectAdjacent(Heuristic, boolean)}.
     * @param validGrid a 3D char array where {@code validChar} means to add that vertex; may be jagged, but this will just use its largest dimensions then
     * @param validChar the char that, when found in {@code validGrid}, means a vertex will be added
     * @param defaultEdgeWeight the default edge weight to use when a weight is unspecified
     */
    public Grid3DirectedGraph(char[][][] validGrid, char validChar, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int z = 0; z < validGrid.length; z++) {
            for (int y = 0; y < validGrid[z].length; y++) {
                for (int x = 0; x < validGrid[z][y].length; x++) {
                    if (validGrid[z][y][x] == validChar)
                        addVertex(new GridPoint3(x, y, z));
                }
            }
        }
    }

    /**
     * Given a 3D float array, adds a vertex where a cell in {@code validGrid} has a value between
     * {@code minimumThreshold} and {@code maximumThreshold}, both inclusive, or ignores it otherwise.
     * This only adds vertices; to automatically add edges you can use {@link #connectAdjacent(Heuristic, boolean)}.
     * @param validGrid a 3D float array; may be jagged, but this will just use its largest dimensions then
     * @param minimumThreshold the minimum inclusive value in {@code validGrid} to allow as a vertex
     * @param maximumThreshold the maximum inclusive value in {@code validGrid} to allow as a vertex
     * @param defaultEdgeWeight the default edge weight to use when a weight is unspecified
     */
    public Grid3DirectedGraph(float[][][] validGrid, float minimumThreshold, float maximumThreshold, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int z = 0; z < validGrid.length; z++) {
            for (int y = 0; y < validGrid[z].length; y++) {
                for (int x = 0; x < validGrid[z][y].length; x++) {
                    if(validGrid[z][y][x] >= minimumThreshold && validGrid[z][y][x] <= maximumThreshold)
                        addVertex(new GridPoint3(x, y, z));
                }
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
    public void connectAdjacent(Heuristic<GridPoint3> heu, boolean permitDiagonal) {
        GridPoint3 test = new GridPoint3(), next = new GridPoint3(), t;
        Node<GridPoint3> nmt, nmn;
        if(heu == null) heu = (a, b) -> getDefaultEdgeWeight();
        for (int x = 0; x < width; x++) {
            test.x = x;
            for (int y = 0; y < height; y++) {
                test.y = y;
                for (int z = 0; z < depth; z++) {
                    test.z = z;
                    if ((nmt = nodeMap.get(test)) != null) {
                        t = nmt.getObject();
                        // @formatter:off
                        if(permitDiagonal){
                            next.x = x-1; next.y = y-1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x+1; next.y = y-1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x-1; next.y = y+1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x+1; next.y = y+1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.z = z-1;
                            next.x = x-1; next.y = y-1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x+1; next.y = y-1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x-1; next.y = y+1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x+1; next.y = y+1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x; next.y = y-1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x-1; next.y = y; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x+1; next.y = y; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x; next.y = y+1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.z = z+1;
                            next.x = x-1; next.y = y-1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x+1; next.y = y-1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x-1; next.y = y+1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x+1; next.y = y+1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x; next.y = y-1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x-1; next.y = y; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x+1; next.y = y; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                            next.x = x; next.y = y+1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                        }
                        next.x = x; next.y = y;
                        next.z = z-1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                        next.z = z+1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                        next.z = z;
                        next.x = x; next.y = y-1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                        next.x = x-1; next.y = y; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                        next.x = x+1; next.y = y; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                        next.x = x; next.y = y+1; if((nmn = nodeMap.get(next)) != null) addEdge(t, nmn.getObject(), heu.getEstimate(test, next));
                        // @formatter:on
                    }
                }
            }
        }
    }

    @Override
    public Grid3DirectedGraph createNew() {
        return new Grid3DirectedGraph();
    }

    /**
     * Get the hash used to calculate the index in the table at which the Node<V> associated with
     * v would be held. What this returns is also used in {@link Node#mapHash}.
     *
     * @param gp a non-null GridPoint3 to hash
     */
    @Override
    public int hash(GridPoint3 gp) {
//        // Harmonious numbers
        return (int)(gp.x * 0xD1B54A32D192ED03L + gp.y * 0xABC98388FB8FAC03L + gp.z * 0x8CB92BA72F3D8DD7L >>> 32);
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

    /**
     * Creates a 1D char array (which can be passed to {@link String#valueOf(char[])}) filled with a grid made of the
     * vertices in this Graph and their estimated costs, if this has done an estimate. Each estimate is rounded to the
     * nearest int and only printed if it is 4 digits or fewer; otherwise this puts '####' in the grid cell. This is a
     * building-block for toString() implementations that may have debugging uses as well.
     * @return a 1D char array containing newline-separated rows of space-separated grid cells that contain estimated costs or '####' for unexplored
     */
    public char[] show() {
        final int w5 = width * 5;
        final int hw = w5 * height;
        final char[] cs = new char[(hw + 1) * depth];
        Arrays.fill(cs, '#');
        for (int i = 4; i < hw; i += 5) {
            cs[i] = (i + 1) % w5 == 0 ? '\n' : ' ';
        }
        cs[hw] = '\n';
        for (int i = 1; i < depth; i++) {
            System.arraycopy(cs, 0, cs, (hw+1) * i, hw+1);
        }
        final int rid = algorithms.lastRunID();
        for (Node<GridPoint3> nc : nodeMap.nodeCollection) {
            if(nc == null || nc.getLastRunID() != rid || nc.getDistance() >= 9999.5)
                continue;
            int d = (int) (nc.getDistance() + 0.5), x = nc.getObject().x * 5, y = nc.getObject().y, z = nc.getObject().z;
//            if(y * w5 + x + 3 >= cs.length)
//                System.out.printf("x: %d, y: %d", x, y);
            int i = z * (hw + 1) + y * w5 + x;
            cs[i    ] = (d >= 1000) ? (char) ('0' + d / 1000) : ' ';
            cs[i + 1] = (d >= 100)  ? (char) ('0' + d / 100 % 10) : ' ';
            cs[i + 2] = (d >= 10)   ? (char) ('0' + d / 10 % 10) : ' ';
            cs[i + 3] = (char) ('0' + d % 10);
        }
        return cs;
    }

    @Override
    public String toString() {
        return "Grid2DirectedGraph: {\n" + String.valueOf(show()) + "\n}";
    }

    @Override
    public void write(Json json) {
        Set<?> vertices = getVertices();
        json.writeArrayStart("v");
        for(Object vertex : vertices) {
            json.writeValue(vertex, GridPoint3.class);
        }
        json.writeArrayEnd();
        Collection<? extends Edge<?>> edges = getEdges();
        json.writeArrayStart("e");
        for(Edge<?> edge : edges) {
            json.writeValue(edge.getA(), GridPoint3.class);
            json.writeValue(edge.getB(), GridPoint3.class);
            json.writeValue(edge.getWeight(), float.class);
        }
        json.writeArrayEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        this.removeAllVertices();
        JsonValue entry = jsonData.getChild("v");
        for (; entry != null; entry = entry.next) {
            addVertex(json.readValue(GridPoint3.class, entry));
        }
        entry = jsonData.getChild("e");
        for (; entry != null; entry = entry.next) {
            addEdge(json.readValue(GridPoint3.class, entry), json.readValue(GridPoint3.class, entry = entry.next), (entry = entry.next).asFloat());
        }
    }
}
