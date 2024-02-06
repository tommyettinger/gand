/*
 * Copyright (c) 2020-2024 See AUTHORS file.
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
 */

package com.github.tommyettinger.old;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/** A {@code Heuristic} generates estimates of the cost to move from a given node to the goal.
 * This is a functional interface whose functional method is {@link #estimate(Object, Object)}.
 *
 * @param <V> Type of vertex; this is usually {@link Vector2} or {@link Vector3}
 */
public interface Heuristic<V> {

	/** Calculates an estimated cost to reach the goal node from the given node.
	 * @param node the start node
	 * @param endNode the end node
	 * @return the estimated cost */
	float estimate(V node, V endNode);
}
