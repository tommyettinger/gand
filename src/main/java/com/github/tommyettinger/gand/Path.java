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

import com.badlogic.gdx.utils.Json;
import com.github.tommyettinger.gand.ds.ObjectDeque;

import java.util.Collection;

public class Path<V> extends ObjectDeque<V> implements Json.Serializable {

    public static final Path EMPTY_PATH = new Path(0, false);

    private float length = 0;

    /**
     * Creates a new Path which can hold 16 values without needing to resize its backing array.
     */
    public Path() {
        super();
    }

    public Path(int capacity) {
        super(capacity);
    }

    public Path(int capacity, boolean resize) {
        super(capacity);
        if (resize) this.size = capacity;
    }

    /**
     * Creates a new Path using all the contents of the given Collection.
     *
     * @param coll a Collection of T that will be copied into this and used in full
     */
    public Path(Collection<? extends V> coll) {
        super(coll);
    }

    /**
     * Copies the given ObjectDeque exactly into a new Path. Individual values will be shallow-copied.
     * Length will be 0.
     *
     * @param deque an ObjectDeque to copy
     */
    public Path(ObjectDeque<? extends V> deque) {
        super(deque);
    }

    /**
     * Copies the given Path exactly into this one. Individual values will be shallow-copied.
     * Length will be the same as in the parameter.
     *
     * @param path a Path to copy
     */
    public Path(Path<? extends V> path) {
        super(path);
        length = path.length;
    }

    /**
     * Creates a new Path using all the contents of the given array.
     *
     * @param a an array of T that will be copied into this and used in full
     */
    public Path(V[] a) {
        super(a);
    }

    /**
     * Creates a new Path using {@code count} items from {@code a}, starting at {@code offset}.
     *
     * @param a      an array of T
     * @param offset where in {@code a} to start using items
     * @param count  how many items to use from {@code a}
     */
    public Path(V[] a, int offset, int count) {
        super(a, offset, count);
    }

    /**
     * @return the length of this path, that is, the sum of the edge weights of all edges contained in the path.
     */
    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }
//
    public static <T> Path<T> with (T item) {
        Path<T> path = new Path<>();
        path.add(item);
        return path;
    }

    @SafeVarargs
    public static <T> Path<T> with (T... items) {
        return new Path<>(items);
    }
}
