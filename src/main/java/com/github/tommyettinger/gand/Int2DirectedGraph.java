package com.github.tommyettinger.gand;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.gand.points.PointI2;
import com.github.tommyettinger.gand.utils.Heuristic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class Int2DirectedGraph extends DirectedGraph<PointI2> implements Json.Serializable {

    private int width = 0;
    private int height = 0;
    /**
     * Adds a vertex to the graph.
     *
     * @param gridPoint2 the vertex to be added
     * @return true if the vertex was not already in the graph, false otherwise
     */
    @Override
    public boolean addVertex(PointI2 gridPoint2) {
        if(super.addVertex(gridPoint2))
        {
            width = Math.max(width, gridPoint2.x+1);
            height = Math.max(height, gridPoint2.y+1);
            return true;
        }
        return false;
    }

    public Int2DirectedGraph() {
        super();
    }

    public Int2DirectedGraph(Collection<PointI2> vertices) {
        super(vertices);
    }

    public Int2DirectedGraph(Collection<PointI2> vertices, Collection<Edge<PointI2>> edges, float defaultEdgeWeight) {
        super(vertices, edges, defaultEdgeWeight);
    }

    public Int2DirectedGraph(Graph<PointI2> graph) {
        super(graph);
    }

    /**
     * Given a 2D boolean array, adds a vertex where a cell in {@code validGrid} is true, or ignores it otherwise.
     * This only adds vertices; to automatically add edges you can use {@link #connectAdjacent(Heuristic, boolean)}.
     * @param validGrid a 2D boolean array where true means to add that vertex; may be jagged, but this will just use its largest dimensions then
     * @param defaultEdgeWeight the default edge weight to use when a weight is unspecified
     */
    public Int2DirectedGraph(boolean[][] validGrid, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int x = 0; x < validGrid.length; x++) {
            for (int y = 0; y < validGrid[x].length; y++) {
                if(validGrid[x][y])
                    addVertex(new PointI2(x, y));
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
    public Int2DirectedGraph(char[][] validGrid, char validChar, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int x = 0; x < validGrid.length; x++) {
            for (int y = 0; y < validGrid[x].length; y++) {
                if(validGrid[x][y] == validChar)
                    addVertex(new PointI2(x, y));
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
    public Int2DirectedGraph(float[][] validGrid, float minimumThreshold, float maximumThreshold, float defaultEdgeWeight){
        super();
        setDefaultEdgeWeight(defaultEdgeWeight);
        for (int x = 0; x < validGrid.length; x++) {
            for (int y = 0; y < validGrid[x].length; y++) {
                if(validGrid[x][y] >= minimumThreshold && validGrid[x][y] <= maximumThreshold)
                    addVertex(new PointI2(x, y));
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
    public void connectAdjacent(Heuristic<PointI2> heu, boolean permitDiagonal) {
        PointI2 test = new PointI2(), next = new PointI2(), t;
        Node<PointI2> nmt, nmn;
        if(heu == null) heu = (a, b) -> getDefaultEdgeWeight();
        for (int x = 0; x < width; x++) {
            test.x = x;
            for (int y = 0; y < height; y++) {
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

    @Override
    public Int2DirectedGraph createNew() {
        return new Int2DirectedGraph();
    }

    /**
     * Get the hash used to calculate the index in the table at which the Node<V> associated with
     * v would be held. What this returns is also used in {@link Node#mapHash}.
     *
     * @param gp a non-null PointI2 to hash
     */
    @Override
    public int hash(PointI2 gp) {
//        // Harmonious numbers
        return (int)(gp.x * 0xC13FA9A902A6328FL + gp.y * 0x91E10DA5C79E7B1DL >>> 32);
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
        if(width == 0 || height == 0) return new char[0];
        final int w5 = width * 5;
        final char[] cs = new char[w5 * height];
        Arrays.fill(cs,  '#');
        for (int i = 4; i < cs.length; i += 5) {
            cs[i] = (i + 1) % w5 == 0 ? '\n' : ' ';
        }
        final int rid = algorithms.lastRunID();
        for (Node<PointI2> nc : nodeMap.nodeCollection) {
            if(nc == null || nc.getLastRunID() != rid || nc.getDistance() >= 9999.5)
                continue;
            int d = (int) (nc.getDistance() + 0.5), x = nc.getObject().x * 5, y = nc.getObject().y;
//            if(y * w5 + x + 3 >= cs.length)
//                System.out.printf("x: %d, y: %d", x, y);
            cs[y * w5 + x    ] = (d >= 1000) ? (char) ('0' + d / 1000) : ' ';
            cs[y * w5 + x + 1] = (d >= 100)  ? (char) ('0' + d / 100 % 10) : ' ';
            cs[y * w5 + x + 2] = (d >= 10)   ? (char) ('0' + d / 10 % 10) : ' ';
            cs[y * w5 + x + 3] = (char) ('0' + d % 10);
        }
        return cs;
    }

    /**
     * Creates a 1D char array (which can be passed to {@link String#valueOf(char[])}) filled with a grid made of the
     * vertices in this Graph and their estimated costs, if this has done an estimate. Each estimate is rounded to the
     * nearest int and only printed if it is 4 digits or fewer; otherwise this puts '####' in the grid cell. If
     * {@code showEdges} is true, edges are also shown with one-way arrows for each edge, pointing in the direction a
     * pathfinder can take. This is a building-block for toString() implementations that may also have debugging uses.
     * @param showEdges true if edges should be shown as arrows; this uses about twice as many lines
     * @return a 1D char array containing newline-separated rows of space-separated grid cells that contain estimated costs or '####' for unexplored
     */
    public char[] show(boolean showEdges) {
        if(!showEdges) return show();
        if(width == 0 || height == 0) return new char[0];
        final int w6 = width * 6, len = w6 * height * 2 - w6;
        final char[] cs = new char[len];
        Arrays.fill(cs,  '#');
        for (int i = 4; i < cs.length; i += 6) {
            cs[i] = ' ';
            cs[i+1] = (i + 2) % w6 == 0 ? '\n' : ' ';
        }
        final int rid = algorithms.lastRunID();
        for (Node<PointI2> nc : nodeMap.nodeCollection) {
            if(nc == null || nc.getLastRunID() != rid || nc.getDistance() >= 9999.5)
                continue;
            int d = (int) (nc.getDistance() + 0.5), x = nc.getObject().x * 6, y = nc.getObject().y * 2;
            cs[len - w6 - y * w6 + x    ] = (d >= 1000) ? (char) ('0' + d / 1000) : ' ';
            cs[len - w6 - y * w6 + x + 1] = (d >= 100)  ? (char) ('0' + d / 100 % 10) : ' ';
            cs[len - w6 - y * w6 + x + 2] = (d >= 10)   ? (char) ('0' + d / 10 % 10) : ' ';
            cs[len - w6 - y * w6 + x + 3] = (char) ('0' + d % 10);
            for(Connection<PointI2> edge : nc.getOutEdges()){
                PointI2 other = (edge.getA() == nc.getObject()) ? edge.getB() : edge.getA();
                //←→↑↓↖↗↘↙
                if(other.x < nc.getObject().x && other.y == nc.getObject().y)
                    cs[len - w6 - y * w6 + x - 1] = '←';
                else if(other.x > nc.getObject().x && other.y == nc.getObject().y)
                    cs[len - w6 - y * w6 + x + 4] = '→';
                else if(other.x == nc.getObject().x && other.y < nc.getObject().y)
                    cs[len - y * w6 + x + 1] = '↓';
                else if(other.x == nc.getObject().x && other.y > nc.getObject().y)
                    cs[len - w6 - w6 - y * w6 + x + 2] = '↑';
                else if(other.x < nc.getObject().x && other.y > nc.getObject().y)
                    cs[len - w6 - w6 - y * w6 + x] = '↖';
                else if(other.x > nc.getObject().x && other.y > nc.getObject().y)
                    cs[len - w6 - w6 - y * w6 + x + 3] = '↗';
                else if(other.x < nc.getObject().x && other.y < nc.getObject().y)
                    cs[len - y * w6 + x - 1] = '↙';
                else if(other.x > nc.getObject().x && other.y < nc.getObject().y)
                    cs[len - y * w6 + x + 4] = '↘';
            }
        }
        return cs;
    }

    @Override
    public String toString() {
        return "Int2DirectedGraph: {\n" + String.valueOf(show()) + "\n}";
    }

    @Override
    public void write(Json json) {
        Set<?> vertices = getVertices();
        json.writeArrayStart("v");
        for(Object vertex : vertices) {
            json.writeValue(vertex, PointI2.class);
        }
        json.writeArrayEnd();
        Collection<? extends Edge<?>> edges = getEdges();
        json.writeArrayStart("e");
        for(Edge<?> edge : edges) {
            json.writeValue(edge.getA(), PointI2.class);
            json.writeValue(edge.getB(), PointI2.class);
            json.writeValue(edge.getWeight(), float.class);
        }
        json.writeArrayEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        this.removeAllVertices();
        JsonValue entry = jsonData.getChild("v");
        for (; entry != null; entry = entry.next) {
            addVertex(json.readValue(PointI2.class, entry));
        }
        entry = jsonData.getChild("e");
        for (; entry != null; entry = entry.next) {
            addEdge(json.readValue(PointI2.class, entry), json.readValue(PointI2.class, entry = entry.next), (entry = entry.next).asFloat());
        }
    }
}
