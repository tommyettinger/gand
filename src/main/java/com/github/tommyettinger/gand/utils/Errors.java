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
package com.github.tommyettinger.gand.utils;

public final class Errors {
    /**
     * Not instantiable.
     */
    private Errors() {
    }

    public static void throwNullVertexException() {
        throw new IllegalArgumentException("Vertices cannot be null");
    }

    public static void throwNullItemException() {
        throw new IllegalArgumentException("No item can be null");
    }

    public static void throwSameVertexException() {
        throw new IllegalArgumentException("Self loops are not allowed");
    }

    public static void throwVertexNotInGraphVertexException(boolean multiple) {
        if (multiple) throw new IllegalArgumentException("At least one vertex is not in the graph");
        else throw new IllegalArgumentException("Vertex is not in the graph");
    }

    public static void throwModificationException() {
        throw new UnsupportedOperationException("You cannot modify this Collection - use the Graph object.");
    }


}
