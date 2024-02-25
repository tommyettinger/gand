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

import com.github.tommyettinger.gand.points.Point3;
import com.github.tommyettinger.gand.points.PointPair;
import com.github.tommyettinger.gand.utils.IntIntIntPredicate;

/** A raycast collision detector used for path smoothing in 3D, with cells considered passable if a predicate returns
 * true. This only considers orthogonally-touching cells as connected.
 * <br>
 * The algorithm is from <a href="http://www.redblobgames.com/grids/line-drawing.html#stepping">Red Blob Games</a>.
 * <br>
 * This is typically used by passing in a lambda that either looks up a value in a 3D array (and should check the bounds
 * of the array against the indices given), or sets a {@link com.github.tommyettinger.gand.points.PointI3} with the int
 * parameters and looks that up in a map or set. The former might look like:
 * {@code (x, y, z) -> x >= 0 && x < booleanWorld.length && y >= 0 && y < booleanWorld[x].length && z >= 0 && z < booleanWorld[x][y].length && booleanWorld[x][y][z]} .
 *
 * @param <P> typically {@link com.github.tommyettinger.gand.points.PointI3} or {@link com.github.tommyettinger.gand.points.PointF3}
 */
public class Ortho3DRaycastCollisionDetector<P extends Point3<P>> implements RaycastCollisionDetector<P> {
	private final IntIntIntPredicate predicate;

	/**
     * Creates a Ortho3DRaycastCollisionDetector that uses the given {@code predicate} to determine if an x,y,z cell
	 * is passable.
	 * <br>
	 * {@code predicate} is typically a lambda that either looks up a value in a 3D array (and should check the bounds
	 * of the array against the indices given), or sets a {@link com.github.tommyettinger.gand.points.PointI3} with the
	 * int parameters and looks that up in a map or set. The former might look like:
	 * {@code (x, y, z) -> x >= 0 && x < booleanWorld.length && y >= 0 && y < booleanWorld[x].length && z >= 0 && z < booleanWorld[x][y].length && booleanWorld[x][y][z]} .
	 * @param predicate should bounds-check an x,y,z point and return true if it is considered passable
	 */
	public Ortho3DRaycastCollisionDetector(final IntIntIntPredicate predicate) {
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
	 * @param predicate should bounds-check an x,y,z point and return true if it is considered passable
	 * @return true if any cell in the line is blocked, as per the given predicate
	 */
	public static<P extends Point3<P>> boolean collides (final PointPair<P> ray, final IntIntIntPredicate predicate) {
		int startX = (int)(ray.a.x() + 0.5f);
		int startY = (int)(ray.a.y() + 0.5f);
		int startZ = (int)(ray.a.z() + 0.5f);
		int targetX = (int)(ray.b.x() + 0.5f);
		int targetY = (int)(ray.b.y() + 0.5f);
		int targetZ = (int)(ray.b.z() + 0.5f);

		int dx = targetX - startX, dy = targetY - startY, dz = targetZ - startZ,
				nx = Math.abs(dx), ny = Math.abs(dy), nz = Math.abs(dz);
		int signX = dx >> 31 | 1, signY = dy >> 31 | 1, signZ = dz >> 31 | 1,
				x = startX, y = startY, z = startZ;

		if(startX == targetX && startY == targetY && startZ == targetZ) {
			return false;
		}

		for (int ix = 0, iy = 0, iz = 0; (ix <= nx || iy <= ny || iz <= nz); ) {
			if (x == targetX && y == targetY && z == targetZ) {
				return false;
			}

			if(!predicate.test(x, y, z))
				return true;

			if ((1 + ix + ix) * ny < (1 + iy + iy) * nx) {
				if ((1 + ix + ix) * nz < (1 + iz + iz) * nx) {
					x += signX;
					ix++;
				} else {
					z += signZ;
					iz++;
				}
			}else {
				if ((1 + iy + iy) * nz < (1 + iz + iz) * ny) {
					y += signY;
					iy++;
				} else {
					z += signZ;
					iz++;
				}
			}
		}
		return false;
	}
}
