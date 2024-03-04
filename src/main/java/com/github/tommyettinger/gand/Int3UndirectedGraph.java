package com.github.tommyettinger.gand;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.gand.points.PointI3;
import com.github.tommyettinger.gand.utils.Heuristic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class Int3UndirectedGraph extends UndirectedGraph<PointI3> implements Json.Serializable {

    private int maxX = Integer.MIN_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private int maxZ = Integer.MIN_VALUE;
    /**
     * Adds a vertex to the graph.
     *
     * @param gridPoint3 the vertex to be added
     * @return true if the vertex was not already in the graph, false otherwise
     */
    @Override
    public boolean addVertex(PointI3 gridPoint3) {
        if(super.addVertex(gridPoint3))
        {
            maxX = Math.max(maxX, gridPoint3.x+1);
            maxY = Math.max(maxY, gridPoint3.y+1);
            maxZ = Math.max(maxZ, gridPoint3.z+1);
            return true;
        }
        return false;
    }

    public Int3UndirectedGraph() {
        super();
    }

    public Int3UndirectedGraph(Collection<PointI3> vertices) {
        this(vertices, Collections.emptyList(), 1f);
    }

    public Int3UndirectedGraph(Collection<PointI3> vertices, Collection<Edge<PointI3>> edges, float defaultEdgeWeight) {
        super();
        this.setDefaultEdgeWeight(defaultEdgeWeight);
        for (PointI3 v : vertices) {
            addVertex(v);
        }
        for(Edge<PointI3> edge : edges) {
            addEdge(edge);
        }
    }

    public Int3UndirectedGraph(Graph<PointI3> graph) {
        super();
        this.setDefaultEdgeWeight(graph.getDefaultEdgeWeight());
        Set<Connection<PointI3>> edges = graph.getEdges();
        Set<PointI3> vertices = graph.getVertices();
        for (PointI3 v : vertices) {
            addVertex(v);
        }
        for(Edge<PointI3> edge : edges) {
            // Each Edge is guaranteed to be valid here, so we don't need to re-add its vertices.
            addEdge(edge.getA(), edge.getB(), edge.getWeight());
        }
    }

    /**
     * Given a 3D boolean array, adds a vertex where a cell in {@code validGrid} is true, or ignores it otherwise.
     * This only adds vertices; to automatically add edges you can use {@link #connectAdjacent(Heuristic, boolean)}.
     * @param validGrid a 3D boolean array where true means to add that vertex; may be jagged, but this will just use its largest dimensions then
     * @param defaultEdgeWeight the default edge weight to use when a weight is unspecified
     */
    public Int3UndirectedGraph(boolean[][][] validGrid, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int z = 0; z < validGrid.length; z++) {
            for (int y = 0; y < validGrid[z].length; y++) {
                for (int x = 0; x < validGrid[z][y].length; x++) {
                    if (validGrid[z][y][x])
                        addVertex(new PointI3(x, y, z));
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
    public Int3UndirectedGraph(char[][][] validGrid, char validChar, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int z = 0; z < validGrid.length; z++) {
            for (int y = 0; y < validGrid[z].length; y++) {
                for (int x = 0; x < validGrid[z][y].length; x++) {
                    if (validGrid[z][y][x] == validChar)
                        addVertex(new PointI3(x, y, z));
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
    public Int3UndirectedGraph(float[][][] validGrid, float minimumThreshold, float maximumThreshold, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int z = 0; z < validGrid.length; z++) {
            for (int y = 0; y < validGrid[z].length; y++) {
                for (int x = 0; x < validGrid[z][y].length; x++) {
                    if(validGrid[z][y][x] >= minimumThreshold && validGrid[z][y][x] <= maximumThreshold)
                        addVertex(new PointI3(x, y, z));
                }
            }
        }
    }

    /**
     * Adds edges between all pairs of adjacent vertices, where adjacency is 6-way if {@code permitDiagonal} is false,
     * or 26-way if it is true. The given Heuristic is used to determine the edge weight for any connection, unless
     * {@code heu} is null, in which case the {@link #getDefaultEdgeWeight()} is used for all edges.
     * @param heu used to calculate the weight for each edge; may be null to use {@link #getDefaultEdgeWeight()}
     * @param permitDiagonal if false, this will use 6-way adjacency only; if true, it will use 26-way
     */
    public void connectAdjacent(Heuristic<PointI3> heu, boolean permitDiagonal) {
        PointI3 test = new PointI3(), next = new PointI3(), t;
        Node<PointI3> nmt, nmn;
        if(heu == null) heu = (a, b) -> getDefaultEdgeWeight();
        for (int x = 0; x < maxX; x++) {
            test.x = x;
            for (int y = 0; y < maxY; y++) {
                test.y = y;
                for (int z = 0; z < maxZ; z++) {
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
    public Int3UndirectedGraph createNew() {
        return new Int3UndirectedGraph();
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    /**
     * Get the hash used to calculate the index in the table at which the Node<V> associated with
     * v would be held. What this returns is also used in {@link Node#mapHash}.
     *
     * @param gp a non-null PointI3 to hash
     */
    @Override
    public int hash(PointI3 gp) {
//        // Harmonious numbers
        return (int)(gp.x * 0xD1B54A32D192ED03L + gp.y * 0xABC98388FB8FAC03L + gp.z * 0x8CB92BA72F3D8DD7L >>> 31);
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
        if(maxX == 0 || maxY == 0 || maxZ == 0) return new char[0];
        final int w5 = maxX * 5;
        final int hw = w5 * maxY;
        final char[] cs = new char[(hw + 1) * maxZ];
        Arrays.fill(cs, '#');
        for (int i = 4; i < hw; i += 5) {
            cs[i] = (i + 1) % w5 == 0 ? '\n' : ' ';
        }
        cs[hw] = '\n';
        for (int i = 1; i < maxZ; i++) {
            System.arraycopy(cs, 0, cs, (hw+1) * i, hw+1);
        }
        final int rid = algorithms.lastRunID();
        for (Node<PointI3> nc : nodeMap.nodeCollection) {
            if(nc == null || nc.getLastRunID() != rid || nc.getDistance() >= 9999.5)
                continue;
            int d = (int) (nc.getDistance() + 0.5), x = nc.getObject().x * 5, y = nc.getObject().y, z = nc.getObject().z;
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
        return "Int3UndirectedGraph: {\n" + String.valueOf(show()) + "\n}";
    }

    @Override
    public void write(Json json) {
        Set<?> vertices = getVertices();
        json.writeArrayStart("v");
        for(Object vertex : vertices) {
            json.writeValue(vertex, PointI3.class);
        }
        json.writeArrayEnd();
        Collection<? extends Edge<?>> edges = getEdges();
        json.writeArrayStart("e");
        for(Edge<?> edge : edges) {
            json.writeValue(edge.getA(), PointI3.class);
            json.writeValue(edge.getB(), PointI3.class);
            json.writeValue(edge.getWeight(), float.class);
        }
        json.writeArrayEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        this.removeAllVertices();
        JsonValue entry = jsonData.getChild("v");
        for (; entry != null; entry = entry.next) {
            addVertex(json.readValue(PointI3.class, entry));
        }
        entry = jsonData.getChild("e");
        for (; entry != null; entry = entry.next) {
            addEdge(json.readValue(PointI3.class, entry), json.readValue(PointI3.class, entry = entry.next), (entry = entry.next).asFloat());
        }
    }
}
