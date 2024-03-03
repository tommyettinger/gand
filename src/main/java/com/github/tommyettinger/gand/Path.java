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
