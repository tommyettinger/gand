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
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.NumberUtils;
import com.github.tommyettinger.gand.ds.ObjectDeque;

import java.util.Collection;

public class Path<V> extends ObjectDeque<V> implements Json.Serializable {

    public static final Path EMPTY_PATH = new Path(0);

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Path<?> path = (Path<?>) o;

        return Float.compare(length, path.length) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (length != 0.0f ? NumberUtils.floatToIntBits(length) : 0);
        return result;
    }

    public String toString () {
        if (size == 0) {
            return "[|length=" + length + "]";
        }
        final V[] values = this.values;
        final int head = this.head;
        final int tail = this.tail;

        StringBuilder sb = new StringBuilder(64);
        sb.append('[');
        sb.append(values[head]);
        for (int i = (head + 1) % values.length; i != tail;) {
            sb.append(", ").append(values[i]);
            if(++i == tail) break;
            if(i == values.length) i = 0;
        }
        sb.append("|length=").append(length).append(']');
        return sb.toString();
    }

    public String toString (String separator) {
        if (size == 0)
            return "|length=" + length;
        final V[] values = this.values;
        final int head = this.head;
        final int tail = this.tail;

        StringBuilder sb = new StringBuilder(64);
        sb.append(values[head]);
        for (int i = (head + 1) % values.length; i != tail;) {
            sb.append(separator).append(values[i]);
            if(++i == tail) break;
            if(i == values.length) i = 0;
        }
        return sb.append("|length=").append(length).toString();
    }

    @Override
    public void write(Json json) {
        json.writeArrayStart("items");
        for (int i = 0; i < size; i++) {
            json.writeValue(get(i), null);
        }
        json.writeArrayEnd();
        json.writeValue("len", length);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        clear();
        for (JsonValue value = jsonData.child; value != null; value = value.next) {
            add(json.readValue(null, value));
        }
        setLength(jsonData.next.asFloat());
    }


    /**
     * Constructs an empty path given the type as a generic type argument.
     * This is usually less useful than just using the constructor, but can be handy
     * in some code-generation scenarios when you don't know how many arguments you will have.
     *
     * @param <T>    the type of items; must be given explicitly
     * @return a new path containing nothing
     */
    public static <T> Path<T> with () {
        return new Path<>(0);
    }

    /**
     * Creates a new Path that holds only the given item, but can be resized.
     * @param item one T item
     * @return a new Path that holds the given item
     * @param <T> the type of item, typically inferred
     */
    public static <T> Path<T> with (T item) {
        Path<T> path = new Path<>(1);
        path.add(item);
        return path;
    }

    /**
     * Creates a new Path that holds only the given items, but can be resized.
     * @param item0 a T item
     * @param item1 a T item
     * @return a new Path that holds the given items
     * @param <T> the type of item, typically inferred
     */
    public static <T> Path<T> with (T item0, T item1) {
        Path<T> path = new Path<>(2);
        path.add(item0);
        path.add(item1);
        return path;
    }

    /**
     * Creates a new Path that holds only the given items, but can be resized.
     * @param item0 a T item
     * @param item1 a T item
     * @param item2 a T item
     * @return a new Path that holds the given items
     * @param <T> the type of item, typically inferred
     */
    public static <T> Path<T> with (T item0, T item1, T item2) {
        Path<T> path = new Path<>(3);
        path.add(item0);
        path.add(item1);
        path.add(item2);
        return path;
    }

    /**
     * Creates a new Path that holds only the given items, but can be resized.
     * @param item0 a T item
     * @param item1 a T item
     * @param item2 a T item
     * @param item3 a T item
     * @return a new Path that holds the given items
     * @param <T> the type of item, typically inferred
     */
    public static <T> Path<T> with (T item0, T item1, T item2, T item3) {
        Path<T> path = new Path<>(4);
        path.add(item0);
        path.add(item1);
        path.add(item2);
        path.add(item3);
        return path;
    }

    /**
     * Creates a new Path that holds only the given items, but can be resized.
     * @param item0 a T item
     * @param item1 a T item
     * @param item2 a T item
     * @param item3 a T item
     * @param item4 a T item
     * @return a new Path that holds the given items
     * @param <T> the type of item, typically inferred
     */
    public static <T> Path<T> with (T item0, T item1, T item2, T item3, T item4) {
        Path<T> path = new Path<>(5);
        path.add(item0);
        path.add(item1);
        path.add(item2);
        path.add(item3);
        path.add(item4);
        return path;
    }

    /**
     * Creates a new Path that holds only the given items, but can be resized.
     * @param item0 a T item
     * @param item1 a T item
     * @param item2 a T item
     * @param item3 a T item
     * @param item4 a T item
     * @param item5 a T item
     * @return a new Path that holds the given items
     * @param <T> the type of item, typically inferred
     */
    public static <T> Path<T> with (T item0, T item1, T item2, T item3, T item4, T item5) {
        Path<T> path = new Path<>(6);
        path.add(item0);
        path.add(item1);
        path.add(item2);
        path.add(item3);
        path.add(item4);
        path.add(item5);
        return path;
    }

    /**
     * Creates a new Path that holds only the given items, but can be resized.
     * @param item0 a T item
     * @param item1 a T item
     * @param item2 a T item
     * @param item3 a T item
     * @param item4 a T item
     * @param item5 a T item
     * @param item6 a T item
     * @return a new Path that holds the given items
     * @param <T> the type of item, typically inferred
     */
    public static <T> Path<T> with (T item0, T item1, T item2, T item3, T item4, T item5, T item6) {
        Path<T> path = new Path<>(7);
        path.add(item0);
        path.add(item1);
        path.add(item2);
        path.add(item3);
        path.add(item4);
        path.add(item5);
        path.add(item6);
        return path;
    }

    /**
     * Creates a new Path that holds only the given items, but can be resized.
     * @param item0 a T item
     * @param item1 a T item
     * @param item2 a T item
     * @param item3 a T item
     * @param item4 a T item
     * @param item5 a T item
     * @param item6 a T item
     * @param item7 a T item
     * @return a new Path that holds the given items
     * @param <T> the type of item, typically inferred
     */
    public static <T> Path<T> with (T item0, T item1, T item2, T item3, T item4, T item5, T item6, T item7) {
        Path<T> path = new Path<>(8);
        path.add(item0);
        path.add(item1);
        path.add(item2);
        path.add(item3);
        path.add(item4);
        path.add(item5);
        path.add(item6);
        path.add(item7);
        return path;
    }

    /**
     * Creates a new Path that will hold the items in the given array or varargs.
     * This overload will only be used when an array is supplied and the type of the
     * items requested is the component type of the array, or if varargs are used and
     * there are 9 or more arguments.
     * @param varargs either 0 or more T items, or an array of T
     * @return a new Path that holds the given T items
     * @param <T> the type of items, typically inferred by all the items being the same type
     */
    @SafeVarargs
    public static <T> Path<T> with (T... varargs) {
        return new Path<>(varargs);
    }
}
