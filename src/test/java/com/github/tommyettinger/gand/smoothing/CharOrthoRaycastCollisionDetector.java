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

import com.github.tommyettinger.gand.points.PointF2;
import com.github.tommyettinger.gand.points.PointPair;

/** A raycast collision detector used for path smoothing against a simple 2D char array as a map.
 * This only considers orthogonally-touching cells as connected.
 *
 * @author davebaol */
public class CharOrthoRaycastCollisionDetector implements RaycastCollisionDetector<PointF2> {
	private final char[][] worldMap;

	public CharOrthoRaycastCollisionDetector(char[][] worldMap) {
		this.worldMap = worldMap;
	}

	/**
     * Draws a line using Bresenham's line algorithm to see if any cell in the world map is not {@code '.'}, which
	 * indicates a floor or walkable cell; if any cell was not walkable, then this returns true (meaning there is a
	 * collision).
	 * <br>
	 * <a href="https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm">See Wikipedia</a> for more info.
	 *
	 * @param ray the ray to cast; will not be modified
     * @return true if any cell in the line is blocked (not {@code '.'})
	 */
	@Override
	public boolean collides (final PointPair<PointF2> ray) {
		int startX = (int)ray.a.x;
		int startY = (int)ray.a.y;
		int targetX = (int)ray.b.x;
		int targetY = (int)ray.b.y;

		int dx = targetX - startX, dy = targetY - startY, nx = Math.abs(dx), ny = Math.abs(dy);
		int signX = dx >> 31 | 1, signY = dy >> 31 | 1, x = startX, y = startY;

		if(startX == targetX && startY == targetY) {
			return false;
		}

		for (int ix = 0, iy = 0; (ix <= nx || iy <= ny); ) {
			if (x == targetX && y == targetY) {
				return false;
			}

			if(x < 0 || y < 0 || x >= worldMap.length || y >= worldMap[x].length || worldMap[x][y] != '.')
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
