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

import com.github.tommyettinger.gand.points.Point2;
import com.github.tommyettinger.gand.points.PointPair;
import com.github.tommyettinger.gand.utils.IntIntPredicate;

/** A raycast collision detector used for path smoothing in 2D, with cells considered passable if a predicate returns
 * true. This treats diagonally-connected passable cells as passable. It uses Bresenham's line algorithm.
 * <a href="https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm">See Wikipedia</a> for more info.
 * <br>
 * This is typically used by passing in a lambda that either looks up a value in a 2D array (and should check the bounds
 * of the array against the indices given), or sets a {@link com.github.tommyettinger.gand.points.PointI2} with the int
 * parameters and looks that up in a map or set. The former might look like:
 * {@code (x, y) -> x >= 0 && x < booleanWorld.length && y >= 0 && y < booleanWorld[x].length && booleanWorld[x][y]} .
 *
 * @param <P> typically {@link com.github.tommyettinger.gand.points.PointI2} or {@link com.github.tommyettinger.gand.points.PointF2}
 * @author davebaol */
public class Bresenham2DRaycastCollisionDetector<P extends Point2<P>> implements RaycastCollisionDetector<P> {
	private final IntIntPredicate predicate;

	/**
	 * Creates a Bresenham2DRaycastCollisionDetector that uses the given {@code predicate} to determine if an x,y cell
	 * is passable.
	 * <br>
	 * {@code predicate} is typically a lambda that either looks up a value in a 2D array (and should check the bounds
	 * of the array against the indices given), or sets a {@link com.github.tommyettinger.gand.points.PointI2} with the
	 * int parameters and looks that up in a map or set. The former might look like:
	 * {@code (x, y) -> x >= 0 && x < booleanWorld.length && y >= 0 && y < booleanWorld[x].length && booleanWorld[x][y]} .
	 * @param predicate should bounds-check an x,y point and return true if it is considered passable
	 */
	public Bresenham2DRaycastCollisionDetector(final IntIntPredicate predicate) {
		this.predicate = predicate;
	}

	/**
	 * Draws a line using Bresenham's line algorithm to see if all cells in the line are passable; if any cell was not
	 * passable, then this returns true (meaning there is a collision). If the point type this uses allows
	 * floating-point values for coordinates, then this rounds coordinates to their nearest integers.
	 * <br>
	 * <a href="https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm">See Wikipedia</a> for more info.
	 *
	 * @param ray the ray to cast; will not be modified
	 * @return true if any cell in the line is blocked, as per the given predicate
	 */
	@Override
	public boolean collides (final PointPair<P> ray) {
		return collides(ray, predicate);
	}
	/**
	 * Draws a line using Bresenham's line algorithm to see if all cells in the line are passable; if any cell was not
	 * passable, then this returns true (meaning there is a collision). If the point type this uses allows
	 * floating-point values for coordinates, then this rounds coordinates to their nearest integers.
	 * <br>
	 * <a href="https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm">See Wikipedia</a> for more info.
	 *
	 * @param ray the ray to cast; will not be modified
	 * @param predicate should bounds-check an x,y point and return true if it is considered passable
	 * @return true if any cell in the line is blocked, as per the given predicate
	 */
	public static<P extends Point2<P>> boolean collides (final PointPair<P> ray, final IntIntPredicate predicate) {
		int x0 = (int)(ray.a.x() + 0.5f);
		int y0 = (int)(ray.a.y() + 0.5f);
		int x1 = (int)(ray.b.x() + 0.5f);
		int y1 = (int)(ray.b.y() + 0.5f);

		int tmp;
		boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
		if (steep) {
			// Swap x0 and y0
			tmp = x0;
			x0 = y0;
			y0 = tmp;
			// Swap x1 and y1
			tmp = x1;
			x1 = y1;
			y1 = tmp;
		}
		if (x0 > x1) {
			// Swap x0 and x1
			tmp = x0;
			x0 = x1;
			x1 = tmp;
			// Swap y0 and y1
			tmp = y0;
			y0 = y1;
			y1 = tmp;
		}

		int deltax = x1 - x0;
		int deltay = Math.abs(y1 - y0);
		int error = 0;
		int y = y0;
		int ystep = (y0 < y1 ? 1 : -1);
		for (int x = x0; x <= x1; x++) {
			if(steep) {
				if(!predicate.test(y, x)) return true;
			} else {
				if(!predicate.test(x, y)) return true;
			}
			error += deltay;
			if (error + error >= deltax) {
				y += ystep;
				error -= deltax;
			}
		}

		return false;
	}
}
