# gand

> "We find things... see?"

This library helps with pathfinding and other graph algorithms.
It is very closely based upon [simple-graphs](https://github.com/earlygrey/simple-graphs), but
changes some features to make serializing graphs easier, and integrates more closely into
[libGDX](https://github.com/libgdx/libgdx), which this has as one of two dependencies. The
other dependency is a tiny library of mostly interfaces, [crux](https://github.com/tommyettinger/crux).
Crux generalizes the point types used by libGDX and other libraries so they can share an API in subclasses.
You can optionally depend on [Fury](https://fury.apache.org) for binary serialization, or you can
simply use the JSON serialization built into libGDX. Kryo should work again soon!

**[JavaDocs are here.](https://tommyettinger.github.io/gand/apidocs/)**

# What's Different?

The main change here is actual a feature elimination rather than an addition. In simple-graphs, each edge between
nodes has a `WeightFunction`, almost always a lambda, that allows it to dynamically adjust its weight. However, a
`WeightFunction` isn't serializable in any way, especially not with potentially thousands of different lambdas that
calculate weight differently (such as based on a particular creature or terrain feature). In order to allow
(de-)serialization of graphs, I forked simple-graphs and made weights simple `float`s. That's the main thing.

There are some other parts that changed. Paths produced by pathfinding algorithms by default will use a type of
double-ended queue, also called a `Deque`, instead of a `List` or a libGDX `Array` that doesn't implement anything.
The class used here is `ObjectDeque`, which is drawn from the jdkgdxds library; it mostly eliminates the reasons why
one would prefer a List because it permits random-access of items by index in constant time, while also having
constant-time removal from the head and tail. It can be sorted and reversed, also.

Another major change is that large parts of the library can be automatically serialized by libGDX Json. There is also
additional support for serialization with Apache Fury, even though it isn't a dependency. See
Serialization below.

Version 0.1.0 added the types `PointI2`, `PointF2`, `PointI3`, and `PointF3`, and they have been moved to an external
dependency, [crux](https://github.com/tommyettinger/gdcrux), as of version 0.3.0 . These have either int (for the "I"
types) or float (for the "F" types) components, and can have a fixed number of each. These extend `GridPoint2`,
`Vector2`, and so on, but also implement a common interface (all of them): `PointN`, from the new and tiny
[crux](https://github.com/tommyettinger/crux) library. The 2D points, more specifically, implement `Point2`, while
the 3D ones implement `Point3`. Having this generalization helps some code work with either int-based grid
coordinates or float-based smooth coordinates. This is especially relevant for...

The path smoothing algorithm from gdx-ai has been ported here. Interruptible pathfinding, not so much, but the
first step, the `PathSmoother#smoothPath(Path)` method, works well. There are existing `RaycastCollisionDetector`
implementations in 2D and 3D, providing Bresenham for when diagonals are considered adjacent and an orthogonal
line algorithm for when only orthogonal connections are considered adjacent.

Something that hasn't changed much from simple-graphs is the performance. This library is extremely competitive
with simple-graphs for pathfinding speed, usually within 5% time taken per path (either more or less). This is
a little surprising, because simple-graphs stripped out quite a few features from things like BinaryHeap and its
Array class in order to maximize speed. Our ObjectDeque here has 1474 SLoC, while the Array in simple-graphs has
only 170, so you would expect ObjectDeque to slow things down a bit with added complexity. It actually does slow
some things down, but none of them are done especially often. Plus, other code is a little faster, so it all
essentially evens out.

New in 0.2.0 (and changed in 0.2.1) is the GradientGrid class, which is a port of DijkstraMap in SquidSquad and its
precursor, SquidLib. It allows more efficient pathfinding when you have many goals, or when one point never changes but
other points do change frequently. It works by performing a gradient flood-fill of a grid-based space, and using the
gradient at a position as the pathfinding distance between that position and the nearest goal. The class is not
structured quite as well as the graph code (I wrote most of it several years ago). Even with that in mind, it is still
useful when a single path to a single goal isn't what you want. GradientGrid changed in 0.2.1; now the original version,
with very few changes, is in `GradientGridI2`, while `GradientGrid` is a parent abstract class that can use more than
just `PointI2`, depending on the subclass. If Gand is the only library you use that has types implementing `Point2` from
crux, which is likely, then you can use `GradientGridI2` and don't need to worry about any kind of inheritance.

## Serialization

`ObjectDeque`, its specialized subclass `Path`, `DirectedGraph`, and `UndirectedGraph`
all implement `Json.Serializable`, making them easy to feed into the libGDX Json class to read or write. These do
need the vertex type to be serializable somehow, so our `JsonRegistration` class makes that easier and more concise
for the common vertex types `Vector2`, `Vector3`, `Vector4`, `GridPoint2`, and `GridPoint3`. You can instead use
`PointI2`, `PointI3`, `PointF2`, or `PointF3` from the dependency gdcrux, which are already `Json.Serializable`.

A small change in 0.1.1 makes the Graph types (all of them) `Externalizable`, which enables
[Fury](https://fury.apache.org) to serialize them without needing any extra work in your code. Only the Graph
types actually needed this; everything else exposed to users can be handled by Fury already. Fury is in
"incubating" status in the Apache project, but it's already faster than Kryo (both in their benchmarks and with
a more modest gain in my benchmarks), and is substantially easier to use, for me. They both work fine, and both
have some quirks to get them working... In an earlier version of this README.md, I had seriously struggled with
one such quirk in Kryo, but it was a user error in the end and Kryo does work fine here.

# Find It

`implementation "com.github.tommyettinger:gand:0.3.5"`

If you use GWT, then your GWT module needs to depend on libGDX 1.14.0 or higher, as well as:

```
implementation "com.github.tommyettinger:gand:0.3.5:sources"
implementation "com.github.tommyettinger:gdcrux:0.1.1:sources"
implementation "com.github.tommyettinger:crux:0.1.2:sources"
```

GWT also needs this `inherits` line added to your `GdxDefinition.gwt.xml` file, with the other inherits lines:

```
<inherits name="com.github.tommyettinger.gdcrux" />
<inherits name="com.github.tommyettinger.crux" />
<inherits name="com.github.tommyettinger.gand" />
```

This library always needs at least JDK 8 or a compatible version. It is designed to be compatible with RoboVM's
partial implementation of JDK 8.


# License

[Apache License Version 2.0](LICENSE).

# Miscellaneous

[Namesake](https://starwars.fandom.com/wiki/Gand).