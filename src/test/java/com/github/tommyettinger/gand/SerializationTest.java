package com.github.tommyettinger.gand;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.github.tommyettinger.gand.ds.ObjectDeque;
import org.junit.Assert;
import org.junit.Test;

import static com.github.tommyettinger.gand.utils.JsonRegistration.registerVector2;

public class SerializationTest {
    @Test
    public void testObjectDeque() {
        Json json = new Json();
        registerVector2(json);
        ObjectDeque<Vector2> data = ObjectDeque.with(new Vector2(1, 0.1f), new Vector2(2, 0.2f), new Vector2(3, 0.3f));
        String text = json.toJson(data, ObjectDeque.class, Vector2.class);
        System.out.println(text);
        ObjectDeque next = json.fromJson(ObjectDeque.class, Vector2.class, text);
        System.out.println(json.toJson(next, ObjectDeque.class, Vector2.class));
        Assert.assertEquals(data, next);
    }

    @Test
    public void testPath() {
        Json json = new Json();
        registerVector2(json);
        int n = 20;
        Graph<Vector2> undirectedGraph = TestUtils.makeGridGraph(new UndirectedGraph<>(), n);
        Vector2 start = new Vector2(0, 0), end = new Vector2(n - 1, n - 1);
        Path<Vector2> data = undirectedGraph.algorithms().findShortestPath(start, end);

        String text = json.toJson(data, Path.class, Vector2.class);
        System.out.println(text);
        Path next = json.fromJson(Path.class, Vector2.class, text);
        System.out.println(json.toJson(next, Path.class, Vector2.class));
        Assert.assertEquals(data, next);
    }

    @Test
    public void testUndirectedGraph() {
        Json json = new Json();
        registerVector2(json);
        UndirectedGraph<Vector2> data = new UndirectedGraph<>();
        TestUtils.makeGridGraph(data, 15);
        String text = json.toJson(data, UndirectedGraph.class);
        System.out.println(text);
        UndirectedGraph next = json.fromJson(UndirectedGraph.class, text);
        System.out.println(json.toJson(next, UndirectedGraph.class));
        Assert.assertEquals(data, next);
    }

    @Test
    public void testDirectedGraph() {
        Json json = new Json();
        registerVector2(json);
        DirectedGraph<Vector2> data = new DirectedGraph<>();
        TestUtils.makeGridGraph(data, 15);
        String text = json.toJson(data, DirectedGraph.class);
        System.out.println(text);
        DirectedGraph next = json.fromJson(DirectedGraph.class, text);
        System.out.println(json.toJson(next, DirectedGraph.class));
        Assert.assertEquals(data, next);
    }
}
