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

import java.util.*;

import com.github.tommyettinger.gand.NodeMap.NodeIterator;
import com.github.tommyettinger.gand.utils.Errors;

/**
 * A simple unmodifiable Set of {@code V} vertex items that acts as a view into a {@link NodeMap}.
 * <br>
 * This is not meant for external usage. It is public primarily so reflection code can access it.
 * @param <V> the vertex type associated with each {@link Node}
 */
public class VertexSet<V> extends AbstractSet<V> {

    final NodeMap<V> nodeMap;

    VertexSet(NodeMap<V> nodeMap) {
        this.nodeMap = nodeMap;
    }

    @Override
    public int size() {
        return nodeMap.size;
    }

    @Override
    public boolean isEmpty() {
        return nodeMap.size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return o != null && nodeMap.contains(o);
    }

    @Override
    public Iterator<V> iterator() {
        return new VertexIterator<>(nodeMap);
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[nodeMap.size];
        int index = 0;
        Node<V> node = nodeMap.head;
        while (node != null) {
            array[index++] = node.getObject();
            node = node.nextInOrder;
        }
        return array;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] array) {
        if (array.length < nodeMap.size) {
            array = Arrays.copyOf(array, nodeMap.size);
        }
        int index = 0;
        Node<V> node = nodeMap.head;
        while (node != null) {
            array[index++] = (T) node.getObject();
            node = node.nextInOrder;
        }
        return array;
    }

    @Override
    public boolean add(V v) {
        Errors.throwModificationException();
        return false;
    }

    @Override
    public boolean remove(Object o) {
        Errors.throwModificationException();
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends V> collection) {
        Errors.throwModificationException();
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        Errors.throwModificationException();
        return false;
    }


    @Override
    public boolean retainAll(Collection<?> collection) {
        Errors.throwModificationException();
        return false;
    }

    @Override
    public void clear() {
        Errors.throwModificationException();
    }

    static class VertexIterator<V> implements Iterator<V> {

        private final NodeIterator<V> nodeIterator;

        VertexIterator(NodeMap<V> nodeMap) {
            nodeIterator = new NodeIterator<>(nodeMap);
        }

        @Override
        public boolean hasNext() {
            return nodeIterator.hasNext();
        }

        @Override
        public V next() {
            return nodeIterator.next().getObject();
        }

        @Override
        public void remove() {
            Errors.throwModificationException();
        }
    }

}
