# gand

> "We find things... see?"

This library helps with pathfinding and other graph algorithms.
It is very closely based upon [simple-graphs](https://github.com/earlygrey/simple-graphs), but
changes some features to make serializing graphs easier, and integrates more closely into
[libGDX](https://github.com/libgdx/libgdx), which this has as its only dependency.

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

Another major change is that `ObjectDeque`, its specialized subclass `Path`, `DirectedGraph`, and `UndirectedGraph`
all implement `Json.Serializable`, making them easy to feed into the libGDX Json class to read or write. These do
need the vertex type to be serializable somehow, so our `JsonRegistration` class makes that easier and more concise
for the common vertex types `Vector2`, `Vector3`, `Vector4`, `GridPoint2`, and `GridPoint3`. However, you might not
want to use those types for points, because...

Version 0.1.0 adds the types `PointI2`, `PointF2`, `PointI3`, and `PointF3`, which have either int (for the "I"
types) or float (for the "F" types) components, and can have 2 or 3 of each. These extend `GridPoint2`, `Vector2`,
and so on, but also implement a common interface (all of them): `PointN`, from the new and tiny
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
Array class in order to maximize speed. Our ObjectDeque here has 981 SLoC, while the Array in simple-graphs has
only 170, so you would expect ObjectDeque to slow things down a bit with added complexity. It actually does slow
some things down, but none of them are done especially often. Plus, other code is a little faster, so it all
essentially evens out.

A small change in 0.1.1 makes the Graph types (all of them) `Externalizable`, which enables
[Fury](https://fury.apache.org) to serialize them without needing any extra work in your code. Only the Graph
types actually needed this; everything else exposed to users can be handled by Fury already. Fury is in
"incubating" status in the Apache project, but it's already faster than Kryo (both in their benchmarks and with
a more modest gain in my benchmarks), and is substantially easier to use, for me. The other reason you might want
to prefer Fury over Kryo for a binary serializer is that...

There appears to be some kind of bug or other issue in Kryo 5.x that makes one class from this library shred
serialized Kryo files while it's being written. Writing `PointI3` values to the end of a Kryo `Output` somehow
changes the bytes at the beginning of the `Output`, making them invalid. Any classes that use `PointI3` seem to
always have this happen, and classes that don't use `PointI3` won't notice it. The one consolation prize here
is that `PointI3` is still `Json.Serializable`, and with gand depending on libGDX, that point type can be
written as JSON and then sent to Kryo. It's far from optimal, but it should work.

# Find It

`implementation "com.github.tommyettinger:gand:0.1.0"`

If you use GWT, then your GWT module needs to depend on:

```
implementation "com.github.tommyettinger:gand:0.1.0:sources"
implementation "com.github.tommyettinger:crux:0.0.1:sources"
```

GWT also needs this `inherits` line added to your `GdxDefinition.gwt.xml` file, with the other inherits lines:

```
<inherits name="com.github.tommyettinger.crux" />
<inherits name="com.github.tommyettinger.gand" />
```

This library always needs at least JDK 8 or a compatible version. It is designed to be compatible with RoboVM's
partial implementation of JDK 8.


# License

[Apache License Version 2.0](LICENSE).

# Miscellaneous

[Namesake](https://starwars.fandom.com/wiki/Gand).