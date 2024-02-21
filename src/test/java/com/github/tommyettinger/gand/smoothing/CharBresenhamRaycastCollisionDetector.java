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

import com.badlogic.gdx.math.Vector2;

/** A raycast collision detector used for path smoothing against a simple 2D char array as a map.
 * This treats diagonally-connected walkable cells as walkable. It uses Bresenham's line algorithm.
 * <a href="https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm">See Wikipedia</a> for more info.
 *
 * @author davebaol */
public class CharBresenhamRaycastCollisionDetector implements RaycastCollisionDetector<Vector2> {
	private final char[][] worldMap;

	public CharBresenhamRaycastCollisionDetector(char[][] worldMap) {
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
	public boolean collides (final VectorPair<Vector2> ray) {
		int x0 = (int)ray.a.x;
		int y0 = (int)ray.a.y;
		int x1 = (int)ray.b.x;
		int y1 = (int)ray.b.y;

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
			char tile = '#';
			if(steep) {
				if(y >= 0 && x >= 0 && y < worldMap.length && x < worldMap[y].length)
					tile = worldMap[y][x];
			} else {
				if(x >= 0 && y >= 0 && x < worldMap.length && y < worldMap[x].length)
					tile = worldMap[x][y];
			}
			if (tile != '.') return true; // We've hit a wall
			error += deltay;
			if (error + error >= deltax) {
				y += ystep;
				error -= deltax;
			}
		}

		return false;
	}
}
