/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
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
 ******************************************************************************/

package com.github.tommyettinger.gand.smoothing;

import com.badlogic.gdx.math.Vector;

/** A {@code VectorPair} is made up of two points in order. It can represent a ray, where {@code a} is the start and
 * {@code b} is the direction the ray points in. It can also represent a collision, where {@code a} is the collision
 * point and {@code b} is the normal vector.
 * 
 * @param <T> Type of vector, either 2D or 3D, implementing the {@link Vector} interface
 * 
 * @author davebaol */
public class VectorPair<T extends Vector<T>> {

	/** The starting point of a ray or the collision point of a collision. */
	public T a;

	/** The ending point of a ray or the normal vector of a collision. */
	public T b;

	/** Creates a {@code VectorPair} with the given {@code start} and {@code end} points.
	 * @param start the starting point of this ray
	 * @param end the starting point of this ray */
	public VectorPair(T start, T end) {
		this.a = start;
		this.b = end;
	}

	/** Sets this ray from the given ray.
	 * @param ray The ray
	 * @return this ray for chaining. */
	public VectorPair<T> set (VectorPair<T> ray) {
		a.set(ray.a);
		b.set(ray.b);
		return this;
	}

	/** Sets this VectorPair from the given start and end points.
	 * @param start the starting point of this ray
	 * @param end the starting point of this ray
	 * @return this ray for chaining. */
	public VectorPair<T> set (T start, T end) {
		this.a.set(start);
		this.b.set(end);
		return this;
	}
}
