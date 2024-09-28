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

import com.github.tommyettinger.crux.Point2;
import com.github.tommyettinger.crux.PointPair;
import com.github.tommyettinger.gand.utils.IntIntPredicate;
import com.github.tommyettinger.gdcrux.PointF2;
import com.github.tommyettinger.gdcrux.PointI2;

/** A raycast collision detector used for path smoothing in 2D, with cells considered passable if a predicate returns
 * true. This only considers orthogonally-touching cells as connected.
 * <br>
 * The algorithm is from <a href="http://www.redblobgames.com/grids/line-drawing.html#stepping">Red Blob Games</a>.
 * <br>
 * This is typically used by passing in a lambda that either looks up a value in a 2D array (and should check the bounds
 * of the array against the indices given), or sets a {@link PointI2} with the int
 * parameters and looks that up in a map or set. The former might look like:
 * {@code (x, y) -> x >= 0 && x < booleanWorld.length && y >= 0 && y < booleanWorld[x].length && booleanWorld[x][y]} .
 *
 * @param <P> typically {@link PointI2} or {@link PointF2}
 */
public class Ortho2DRaycastCollisionDetector<P extends Point2<P>> implements RaycastCollisionDetector<P> {
	private final IntIntPredicate predicate;

	/**
     * Creates a Ortho2DRaycastCollisionDetector that uses the given {@code predicate} to determine if an x,y cell
	 * is passable.
	 * <br>
	 * {@code predicate} is typically a lambda that either looks up a value in a 2D array (and should check the bounds
	 * of the array against the indices given), or sets a {@link PointI2} with the
	 * int parameters and looks that up in a map or set. The former might look like:
	 * {@code (x, y) -> x >= 0 && x < booleanWorld.length && y >= 0 && y < booleanWorld[x].length && booleanWorld[x][y]} .
	 * @param predicate should bounds-check an x,y point and return true if it is considered passable
	 */
	public Ortho2DRaycastCollisionDetector(final IntIntPredicate predicate) {
		this.predicate = predicate;
	}

	/**
	 * Draws a line using a simple orthogonal line algorithm to see if all cells in the line are passable; if any cell
	 * was not passable, then this returns true (meaning there is a collision). If the point type this uses allows
	 * floating-point values for coordinates, then this rounds coordinates to their nearest integers.
	 * <br>
	 * The algorithm is from <a href="http://www.redblobgames.com/grids/line-drawing.html#stepping">Red Blob Games</a>.
	 *
	 * @param ray the ray to cast; will not be modified
	 * @return true if any cell in the line is blocked, as per the given predicate
	 */
	@Override
	public boolean collides (final PointPair<P> ray) {
		return collides(ray, predicate);
	}

	/**
	 * Draws a line using a simple orthogonal line algorithm to see if all cells in the line are passable; if any cell
	 * was not passable, then this returns true (meaning there is a collision). If the point type this uses allows
	 * floating-point values for coordinates, then this rounds coordinates to their nearest integers.
	 * <br>
	 * The algorithm is from <a href="http://www.redblobgames.com/grids/line-drawing.html#stepping">Red Blob Games</a>.
	 *
	 * @param ray the ray to cast; will not be modified
	 * @param predicate should bounds-check an x,y point and return true if it is considered passable
	 * @return true if any cell in the line is blocked, as per the given predicate
	 */
	public static<P extends Point2<P>> boolean collides (final PointPair<P> ray, final IntIntPredicate predicate) {
		int startX = (int)(ray.a.x() + 0.5f);
		int startY = (int)(ray.a.y() + 0.5f);
		int targetX = (int)(ray.b.x() + 0.5f);
		int targetY = (int)(ray.b.y() + 0.5f);

		int dx = targetX - startX, dy = targetY - startY, nx = Math.abs(dx), ny = Math.abs(dy);
		int signX = dx >> 31 | 1, signY = dy >> 31 | 1, x = startX, y = startY;

		if(startX == targetX && startY == targetY) {
			return false;
		}

		for (int ix = 0, iy = 0; (ix <= nx || iy <= ny); ) {
			if (x == targetX && y == targetY) {
				return false;
			}

			if(!predicate.test(x, y))
				return true;

			if ((1 + ix + ix) * ny < (1 + iy + iy) * nx) {
				x += signX;
				ix++;
			} else {
				y += signY;
				iy++;
			}
		}
		return false;
	}
}
