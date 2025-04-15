/*
 * Copyright (c) 2022-2024 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.tommyettinger.gand.fury;

import com.badlogic.gdx.math.Vector2;
import com.github.tommyettinger.crux.Point2;
import com.github.tommyettinger.crux.Point3;
import com.github.tommyettinger.gand.*;
import com.github.tommyettinger.gdcrux.PointF2;
import com.github.tommyettinger.gdcrux.PointF3;
import com.github.tommyettinger.gdcrux.PointI2;
import com.github.tommyettinger.gdcrux.PointI3;
import org.apache.fury.Fury;
import org.apache.fury.config.Language;
import org.apache.fury.logging.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.github.tommyettinger.gdcrux.PointMaker.pt;

/**
 * Testing for serialization and deserialization with <a href="https://fury.apache.org">Fury</a>.
 */
public class FuryTest {
    public static Graph<Vector2> makeGridGraph(Graph<Vector2> graph, int sideLength) {

        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                Vector2 v = new Vector2(i, j);
                graph.addVertex(v);
            }
        }

        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                if (i<sideLength-1) {
                    Vector2 v1 = new Vector2(i, j), v2 = new Vector2(i+1,j);
                    float dst = v1.dst(v2);
                    graph.addEdge(v1, v2, dst);
                    if (graph.isDirected()) graph.addEdge(v2, v1, dst);
                }
                if (j<sideLength-1) {
                    Vector2 v1 = new Vector2(i, j), v2 = new Vector2(i,j+1);
                    float dst = v1.dst(v2);
                    graph.addEdge(v1, v2, dst);
                    if (graph.isDirected()) graph.addEdge(v2, v1, dst);
                }
            }
        }

        return graph;
    }

    public static<V extends Point2<V>> Graph<V> makeGridGraph2D(Graph<V> graph, int sideLength, V basis) {

        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                graph.addVertex(basis.cpy().seti(i, j));
            }
        }

        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                if (i<sideLength-1) {
                    V v1 = basis.cpy().seti(i, j), v2 = basis.cpy().seti(i+1,j);
                    float dst = v1.dst(v2);
                    graph.addEdge(v1, v2, dst);
                    if (graph.isDirected()) graph.addEdge(v2, v1, dst);
                }
                if (j<sideLength-1) {
                    V v1 = basis.cpy().seti(i, j), v2 = basis.cpy().seti(i,j+1);
                    float dst = v1.dst(v2);
                    graph.addEdge(v1, v2, dst);
                    if (graph.isDirected()) graph.addEdge(v2, v1, dst);
                }
            }
        }

        return graph;
    }

    public static<V extends Point3<V>> Graph<V> makeGridGraph3D(Graph<V> graph, int sideLength, V basis) {

        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                for (int k = 0; k < sideLength; k++) {
                    graph.addVertex(basis.cpy().seti(i, j, k));
                }
            }
        }

        for (int i = 0; i < sideLength; i++) {
            for (int j = 0; j < sideLength; j++) {
                for (int k = 0; k < sideLength; k++) {
                    if (i < sideLength - 1) {
                        V v1 = basis.cpy().seti(i, j, k), v2 = basis.cpy().seti(i + 1, j, k);
                        float dst = v1.dst(v2);
                        graph.addEdge(v1, v2, dst);
                        if (graph.isDirected()) graph.addEdge(v2, v1, dst);
                    }
                    if (j < sideLength - 1) {
                        V v1 = basis.cpy().seti(i, j, k), v2 = basis.cpy().seti(i, j + 1, k);
                        float dst = v1.dst(v2);
                        graph.addEdge(v1, v2, dst);
                        if (graph.isDirected()) graph.addEdge(v2, v1, dst);
                    }
                    if (k < sideLength - 1) {
                        V v1 = basis.cpy().seti(i, j, k), v2 = basis.cpy().seti(i, j, k + 1);
                        float dst = v1.dst(v2);
                        graph.addEdge(v1, v2, dst);
                        if (graph.isDirected()) graph.addEdge(v2, v1, dst);
                    }
                }
            }
        }
        System.out.println("Graph has " + graph.getVertices().size() + " vertices and " + graph.getEdgeCount() + " edges.");
        return graph;
    }

    @Test
    public void testUndirectedGraph() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(UndirectedGraph.class);
        fury.register(Vector2.class);

        int n = 5;
        Graph<Vector2> data = makeGridGraph(new UndirectedGraph<>(), n);

        byte[] bytes = fury.serializeJavaObject(data);
        System.out.println("UndirectedGraph byte length: " + bytes.length);
        UndirectedGraph<?> data2 = fury.deserializeJavaObject(bytes, UndirectedGraph.class);
        Assert.assertEquals(data.numberOfComponents(), data2.numberOfComponents());
        Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
        Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
        Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
        Assert.assertEquals(data, data2);
    }

    @Test
    public void testDirectedGraph() {
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(DirectedGraph.class);
        fury.register(Vector2.class);

        int n = 5;
        Graph<Vector2> data = makeGridGraph(new DirectedGraph<>(), n);

        byte[] bytes = fury.serializeJavaObject(data);
        System.out.println("DirectedGraph byte length: " + bytes.length);
        DirectedGraph<?> data2 = fury.deserializeJavaObject(bytes, DirectedGraph.class);
        Assert.assertEquals(data.numberOfComponents(), data2.numberOfComponents());
        Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
        Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
        Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
        Assert.assertEquals(data, data2);
    }


    @Test
    public void testDirectedGraph3D() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(DirectedGraph.class);
        fury.register(PointI3.class);

        int n = 5;
        Graph<PointI3> data = makeGridGraph3D(new DirectedGraph<>(), n, new PointI3());

        System.out.println("Initial graph with length " + data.getVertices().size() + ", edge count " + data.getEdgeCount() + ": ");
        byte[] bytes = fury.serializeJavaObject(data);
        System.out.println("DirectedGraph byte length: " + bytes.length);

        DirectedGraph<?> data2 = fury.deserializeJavaObject(bytes, DirectedGraph.class);
        System.out.println("Read back in with length " + data2.getVertices().size() + ", edge count " + data2.getEdgeCount() + ": ");
        Assert.assertEquals(data.numberOfComponents(), data2.numberOfComponents());
        Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
        Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
        Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
        Assert.assertEquals(data, data2);
    }


    @Test
    public void testInt2DirectedGraph() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(Int2DirectedGraph.class);
        fury.register(PointI2.class);

        int n = 5;
        Graph<PointI2> data = makeGridGraph2D(new Int2DirectedGraph(), n, new PointI2());
        System.out.println("Initial graph with length " + data.getVertices().size() + ": ");
        System.out.println(data.getVertices());
        System.out.println(data);
        byte[] bytes = fury.serializeJavaObject(data);
        System.out.println("Int2DirectedGraph byte length: " + bytes.length);

        Int2DirectedGraph data2 = fury.deserializeJavaObject(bytes, Int2DirectedGraph.class);
        System.out.println("Read back in with length " + data2.getVertices().size() + ": ");
        System.out.println(data2.getVertices());
        System.out.println(data2);
        Assert.assertEquals(data.numberOfComponents(), data2.numberOfComponents());
        Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
        Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
        Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
        Assert.assertEquals(data, data2);
    }

    @Test
    public void testInt2UndirectedGraph() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(Int2UndirectedGraph.class);
        fury.register(PointI2.class);

        int n = 5;
        Graph<PointI2> data = makeGridGraph2D(new Int2UndirectedGraph(), n, new PointI2());
        System.out.println("Initial graph with length " + data.getVertices().size() + ": ");
        System.out.println(data.getVertices());
        System.out.println(data);
        byte[] bytes = fury.serializeJavaObject(data);
        System.out.println("Int2UndirectedGraph byte length: " + bytes.length);

        Int2UndirectedGraph data2 = fury.deserializeJavaObject(bytes, Int2UndirectedGraph.class);
        System.out.println("Read back in with length " + data2.getVertices().size() + ": ");
        System.out.println(data2.getVertices());
        System.out.println(data2);
        Assert.assertEquals(data.numberOfComponents(), data2.numberOfComponents());
        Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
        Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
        Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
        Assert.assertEquals(data, data2);
    }

    @Test
    public void testFloat2DirectedGraph() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(Float2DirectedGraph.class);
        fury.register(PointF2.class);

        int n = 5;
        Graph<PointF2> data = makeGridGraph2D(new Float2DirectedGraph(), n, new PointF2());

        byte[] bytes = fury.serializeJavaObject(data);
        System.out.println("Float2DirectedGraph byte length: " + bytes.length);

        Float2DirectedGraph data2 = fury.deserializeJavaObject(bytes, Float2DirectedGraph.class);
        Assert.assertEquals(data.numberOfComponents(), data2.numberOfComponents());
        Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
        Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
        Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
        Assert.assertEquals(data, data2);
    }

    @Test
    public void testFloat2UndirectedGraph() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(Float2UndirectedGraph.class);
        fury.register(PointF2.class);

        int n = 5;
        Graph<PointF2> data = makeGridGraph2D(new Float2UndirectedGraph(), n, new PointF2());

        byte[] bytes = fury.serializeJavaObject(data);
        System.out.println("Float2UndirectedGraph byte length: " + bytes.length);

        Float2UndirectedGraph data2 = fury.deserializeJavaObject(bytes, Float2UndirectedGraph.class);
        Assert.assertEquals(data.numberOfComponents(), data2.numberOfComponents());
        Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
        Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
        Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
        Assert.assertEquals(data, data2);
    }

    @Test
    public void testInt3DirectedGraph() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(Int3DirectedGraph.class);
        fury.register(PointI3.class);

        int n = 5;
        Graph<PointI3> data = makeGridGraph3D(new Int3DirectedGraph(), n, new PointI3());

        System.out.println("Initial graph with length " + data.getVertices().size() + ", edge count " + data.getEdgeCount() + ": ");
        System.out.println(data.getVertices());
        System.out.println(data);
        byte[] bytes = fury.serializeJavaObject(data);
        System.out.println("Int3DirectedGraph byte length: " + bytes.length);

        Int3DirectedGraph data2 = fury.deserializeJavaObject(bytes, Int3DirectedGraph.class);
        System.out.println("Read back in with length " + data2.getVertices().size() + ", edge count " + data2.getEdgeCount() + ": ");
        System.out.println(data2.getVertices());
        System.out.println(data2);
        Assert.assertEquals(data.numberOfComponents(), data2.numberOfComponents());
        Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
        Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
        Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
        Assert.assertEquals(data, data2);
    }

    @Test
    public void testInt3UndirectedGraph() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(Int3UndirectedGraph.class);
        fury.register(PointI3.class);

        int n = 5;
        Graph<PointI3> data = makeGridGraph3D(new Int3UndirectedGraph(), n, new PointI3());

        System.out.println("Initial graph with length " + data.getVertices().size() + ", edge count " + data.getEdgeCount() + ": ");
        System.out.println(data.getVertices());
        System.out.println(data);
        byte[] bytes = fury.serializeJavaObject(data);
        System.out.println("Int3UndirectedGraph byte length: " + bytes.length);

        Int3UndirectedGraph data2 = fury.deserializeJavaObject(bytes, Int3UndirectedGraph.class);
        System.out.println("Read back in with length " + data2.getVertices().size() + ", edge count " + data2.getEdgeCount() + ": ");
        System.out.println(data2.getVertices());
        System.out.println(data2);
        Assert.assertEquals(data.numberOfComponents(), data2.numberOfComponents());
        Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
        Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
        Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
        Assert.assertEquals(data, data2);
    }
    
    @Test
    public void testFloat3DirectedGraph() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(Float3DirectedGraph.class);
        fury.register(PointF3.class);

        int n = 5;
        Graph<PointF3> data = makeGridGraph3D(new Float3DirectedGraph(), n, new PointF3());

        System.out.println("Initial graph with length " + data.getVertices().size() + ", edge count " + data.getEdgeCount() + ": ");
        System.out.println(data.getVertices());
        System.out.println(data);
        byte[] bytes = fury.serializeJavaObject(data);
        System.out.println("Float3DirectedGraph byte length: " + bytes.length);

        Float3DirectedGraph data2 = fury.deserializeJavaObject(bytes, Float3DirectedGraph.class);
        System.out.println("Read back in with length " + data2.getVertices().size() + ", edge count " + data2.getEdgeCount() + ": ");
        System.out.println(data2.getVertices());
        System.out.println(data2);
        Assert.assertEquals(data.numberOfComponents(), data2.numberOfComponents());
        Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
        Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
        Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
        Assert.assertEquals(data, data2);
    }

    @Test
    public void testFloat3UndirectedGraph() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(Float3UndirectedGraph.class);
        fury.register(PointF3.class);

        int n = 5;
        Graph<PointF3> data = makeGridGraph3D(new Float3UndirectedGraph(), n, new PointF3());

        System.out.println("Initial graph with length " + data.getVertices().size() + ", edge count " + data.getEdgeCount() + ": ");
        System.out.println(data.getVertices());
        System.out.println(data);
        byte[] bytes = fury.serializeJavaObject(data);
        System.out.println("Float3UndirectedGraph byte length: " + bytes.length);

        Float3UndirectedGraph data2 = fury.deserializeJavaObject(bytes, Float3UndirectedGraph.class);
        System.out.println("Read back in with length " + data2.getVertices().size() + ", edge count " + data2.getEdgeCount() + ": ");
        System.out.println(data2.getVertices());
        System.out.println(data2);
        Assert.assertEquals(data.numberOfComponents(), data2.numberOfComponents());
        Assert.assertEquals(data.getEdgeCount(), data2.getEdgeCount());
        Assert.assertEquals(new ArrayList<>(data.getVertices()), new ArrayList<>(data2.getVertices()));
        Assert.assertEquals(data.getEdges().stream().map(Object::toString).collect(Collectors.toList()),
                data2.getEdges().stream().map(Object::toString).collect(Collectors.toList()));
        Assert.assertEquals(data, data2);
    }
    

    @Test
    public void testPath() {
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(PointI2.class);
        fury.register(Path.class);

        Path<PointI2> data = Path.with(pt(1, 1), pt(1, 2), pt(1, 3), pt(2, 3), pt(2, 4));
        data.setLength(4f);

        byte[] bytes = fury.serializeJavaObject(data);
        Path<?> data2 = fury.deserializeJavaObject(bytes, Path.class);
        Assert.assertEquals(data, data2);

    }
}
