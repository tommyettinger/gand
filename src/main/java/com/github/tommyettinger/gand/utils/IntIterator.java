/*
 * Copyright (c) 2013-2023 See AUTHORS file.
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
 *
 */

package com.github.tommyettinger.gand.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An Iterator specialized for {@code int} values.
 * This iterates over primitive ints using {@link #nextInt()}.
 * <br>
 * This is roughly equivalent to {@code IntIterator} in Java 8, and is present here so environments
 * don't fully support Java 8 APIs (such as RoboVM) can use it.
 */
public interface IntIterator extends Iterator<Integer> {
	/**
	 * Returns the next {@code int} element in the iteration.
	 *
	 * @return the next {@code int} element in the iteration
	 * @throws NoSuchElementException if the iteration has no more elements
	 */
	int nextInt ();

	/**
	 * {@inheritDoc}
	 *
	 * @implSpec The default implementation boxes the result of calling
	 * {@link #nextInt()}, and returns that boxed result.
	 */
	@Override
	default Integer next () {
		return nextInt();
	}
}