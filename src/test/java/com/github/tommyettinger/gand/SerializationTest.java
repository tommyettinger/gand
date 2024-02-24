package com.github.tommyettinger.gand;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.github.tommyettinger.gand.ds.ObjectDeque;
import com.github.tommyettinger.gand.ds.ObjectOrderedSet;
import com.github.tommyettinger.gand.ds.ObjectSet;
import com.github.tommyettinger.gand.points.PointF2;
import com.github.tommyettinger.gand.points.PointI2;
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
        ObjectDeque<?> next = json.fromJson(ObjectDeque.class, Vector2.class, text);
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
        Path<?> next = json.fromJson(Path.class, Vector2.class, text);
        System.out.println(json.toJson(next, Path.class, Vector2.class));
        Assert.assertEquals(data, next);
    }

    @Test
    public void testObjectSet() {
        Json json = new Json();
        ObjectSet<String> words = ObjectSet.with("Peanut", "Butter", "Jelly", "Time");
        String data = json.toJson(words);
        System.out.println(data);
        ObjectSet<?> words2 = json.fromJson(ObjectSet.class, data);
        for(Object word : words2) {
            System.out.print(word);
            System.out.print(", ");
        }
        Assert.assertEquals(words, words2);
        System.out.println();
        ObjectSet<PointI2> points = ObjectSet.with(new PointI2(42, 42), new PointI2(23, 23), new PointI2(666, 666));
        data = json.toJson(points);
        System.out.println(data);
        ObjectSet<?> points2 = json.fromJson(ObjectSet.class, data);
        for(Object point : points2) {
            System.out.print(point);
            System.out.print(", ");
        }
        Assert.assertEquals(points, points2);
        System.out.println();
    }

    @Test
    public void testObjectOrderedSet() {
        Json json = new Json();
        ObjectOrderedSet<String> words = ObjectOrderedSet.with("Peanut", "Butter", "Jelly", "Time");
        String data = json.toJson(words);
        System.out.println(data);
        ObjectOrderedSet<?> words2 = json.fromJson(ObjectOrderedSet.class, data);
        for(Object word : words2) {
            System.out.print(word);
            System.out.print(", ");
        }
        Assert.assertEquals(words, words2);
        System.out.println();
        ObjectOrderedSet<PointI2> points = ObjectOrderedSet.with(new PointI2(42, 42), new PointI2(23, 23), new PointI2(666, 666));
        data = json.toJson(points);
        System.out.println(data);
        ObjectOrderedSet<?> points2 = json.fromJson(ObjectOrderedSet.class, data);
        for(Object point : points2) {
            System.out.print(point);
            System.out.print(", ");
        }
        Assert.assertEquals(points, points2);
        System.out.println();
    }

    @Test
    public void testUndirectedGraph() {
        Json json = new Json();
        registerVector2(json);
        UndirectedGraph<Vector2> data = new UndirectedGraph<>();
        TestUtils.makeGridGraph(data, 5);
        String text = json.toJson(data, UndirectedGraph.class);
        System.out.println(text);
        UndirectedGraph<?> next = json.fromJson(UndirectedGraph.class, text);
        System.out.println(json.toJson(next, UndirectedGraph.class));
        Assert.assertEquals(data, next);
    }

    @Test
    public void testDirectedGraph() {
        Json json = new Json();
        registerVector2(json);
        DirectedGraph<Vector2> data = new DirectedGraph<>();
        TestUtils.makeGridGraph(data, 5);
        String text = json.toJson(data, DirectedGraph.class);
        System.out.println(text);
        DirectedGraph<?> next = json.fromJson(DirectedGraph.class, text);
        System.out.println(json.toJson(next, DirectedGraph.class));
        Assert.assertEquals(data, next);
    }

    public static void main(String[] args) {
        int n = 5;
        Json json = new Json();
        Graph<PointF2> directedF = TestUtils.makeGridGraphF2(new Float2DirectedGraph(), n);
        Graph<PointF2> undirectedF = TestUtils.makeGridGraphF2(new Float2UndirectedGraph(), n);
        Graph<PointI2> directedI = TestUtils.makeGridGraphI2(new Int2DirectedGraph(), n);
        Graph<PointI2> undirectedI = TestUtils.makeGridGraphI2(new Int2UndirectedGraph(), n);
        String j;
        System.out.println("directedF: " + (j = json.toJson(directedF)));
        System.out.println("directedF length: " + j.length());
        System.out.println("undirectedF: " + (j = json.toJson(undirectedF)));
        System.out.println("undirectedF length: " + j.length());
        System.out.println("directedI: " + (j = json.toJson(directedI)));
        System.out.println("directedI length: " + j.length());
        System.out.println("undirectedI: " + (j = json.toJson(undirectedI)));
        System.out.println("undirectedI length: " + j.length());
    }
}
