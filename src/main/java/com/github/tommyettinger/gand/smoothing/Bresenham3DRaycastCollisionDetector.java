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

import com.github.tommyettinger.crux.Point3;
import com.github.tommyettinger.crux.PointPair;
import com.github.tommyettinger.gand.utils.IntIntIntPredicate;

/** A raycast collision detector used for path smoothing in 3D, with cells considered passable if a predicate returns
 * true. This treats diagonally-connected passable cells as connected. It uses Bresenham's line algorithm.
 * <a href="https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm">See Wikipedia</a> for more info.
 * <br>
 * This is typically used by passing in a lambda that either looks up a value in a 3D array (and should check the bounds
 * of the array against the indices given), or sets a {@link com.github.tommyettinger.gand.points.PointI3} with the int
 * parameters and looks that up in a map or set. The former might look like:
 * {@code (x, y, z) -> x >= 0 && x < booleanWorld.length && y >= 0 && y < booleanWorld[x].length && z >= 0 && z < booleanWorld[x][y].length && booleanWorld[x][y][z]} .
 *
 * @param <P> typically {@link com.github.tommyettinger.gand.points.PointI3} or {@link com.github.tommyettinger.gand.points.PointF3}
 * @author davebaol */
public class Bresenham3DRaycastCollisionDetector<P extends Point3<P>> implements RaycastCollisionDetector<P> {
	private final IntIntIntPredicate predicate;

	/**
	 * Creates a Bresenham3DRaycastCollisionDetector that uses the given {@code predicate} to determine if an x,y,z cell
	 * is passable.
	 * <br>
	 * {@code predicate} is typically a lambda that either looks up a value in a 3D array (and should check the bounds
	 * of the array against the indices given), or sets a {@link com.github.tommyettinger.gand.points.PointI2} with the
	 * int parameters and looks that up in a map or set. The former might look like:
	 * {@code (x, y, z) -> x >= 0 && x < booleanWorld.length && y >= 0 && y < booleanWorld[x].length && z >= 0 && z < booleanWorld[x][y].length && booleanWorld[x][y][z]} .
	 * @param predicate should bounds-check an x,y,z point and return true if it is considered passable
	 */
	public Bresenham3DRaycastCollisionDetector(final IntIntIntPredicate predicate) {
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
	 * @param predicate should bounds-check an x,y,z point and return true if it is considered passable
	 * @return true if any cell in the line is blocked, as per the given predicate
	 */
	public static<P extends Point3<P>> boolean collides (final PointPair<P> ray, final IntIntIntPredicate predicate) {
		int x0 = (int)(ray.a.x() + 0.5f);
		int y0 = (int)(ray.a.y() + 0.5f);
		int z0 = (int)(ray.a.z() + 0.5f);
		int x1 = (int)(ray.b.x() + 0.5f);
		int y1 = (int)(ray.b.y() + 0.5f);
		int z1 = (int)(ray.b.z() + 0.5f);

		int dx = x1 - x0;
		int dy = y1 - y0;
		int dz = z1 - z0;

		int ax = Math.abs(dx);
		int ay = Math.abs(dy);
		int az = Math.abs(dz);

		ax <<= 1;
		ay <<= 1;
		az <<= 1;

		int signx = (dx >> 31 | -dx >>> 31); // project nayuki signum
		int signy = (dy >> 31 | -dy >>> 31); // project nayuki signum
		int signz = (dz >> 31 | -dz >>> 31); // project nayuki signum

		int x = x0;
		int y = y0;
		int z = z0;

		int deltax, deltay, deltaz;
		if (ax >= Math.max(ay, az)) {
			// x dominant
			deltay = ay - (ax >> 1);
			deltaz = az - (ax >> 1);
			while (x != x1) {
				if (!predicate.test(x, y, z)) {
					return true;
				}

				if (deltay >= 0) {
					y += signy;
					deltay -= ax;
				}

				if (deltaz >= 0) {
					z += signz;
					deltaz -= ax;
				}

				x += signx;
				deltay += ay;
				deltaz += az;
			}
		} else if (ay >= Math.max(ax, az)) {
			// y dominant
			deltax = ax - (ay >> 1);
			deltaz = az - (ay >> 1);
			while (y != y1) {
				if (!predicate.test(x, y, z)) {
					return true;
				}

				if (deltax >= 0) {
					x += signx;
					deltax -= ay;
				}

				if (deltaz >= 0) {
					z += signz;
					deltaz -= ay;
				}

				y += signy;
				deltax += ax;
				deltaz += az;
			}
		} else {
			// z dominant
			deltax = ax - (az >> 1);
			deltay = ay - (az >> 1);
			while (z != z1) {
				if (!predicate.test(x, y, z)) {
					return true;
				}

				if (deltax >= 0) {
					x += signx;
					deltax -= az;
				}

				if (deltay >= 0) {
					y += signy;
					deltay -= az;
				}

				z += signz;
				deltax += ax;
				deltay += ay;
			}
		}
		return false;
	}
}
