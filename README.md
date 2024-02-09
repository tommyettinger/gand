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

The last major change is that `ObjectDeque`, its specialized subclass `Path`, `DirectedGraph`, and `UndirectedGraph`
all implement `Json.Serializable`, making them easy to feed into the libGDX Json class to read or write. These do
need the vertex type to be serializable somehow, so our `JsonRegistration` class makes that easier and more concise
for the common vertex types `Vector2`, `Vector3`, `Vector4`, `GridPoint2`, and `GridPoint3`.

More changes are coming! I want to make the path smoothing algorithm from gdx-ai available here. I also want to make
specialized `Graph` implementations that work only on `GridPoint2` or `Vector2`, since those are commonly used in
games, and they're sometimes easier to describe with a 2D array.

# Find It

`implementation "com.github.tommyettinger:gand:0.0.1"`

If you use GWT, then your GWT module needs to depend on:

`implementation "com.github.tommyettinger:gand:0.0.1:sources"`

GWT also needs this `inherits` line added to your `GdxDefinition.gwt.xml` file, with the other inherits lines:

`<inherits name="com.github.tommyettinger.gand" />`

This library always needs at least JDK 8 or a compatible version. It is designed to be compatible with RoboVM's
partial implementation of JDK 8.


# License

[Apache License Version 2.0](LICENSE).

# Miscellaneous

[Namesake](https://starwars.fandom.com/wiki/Gand).