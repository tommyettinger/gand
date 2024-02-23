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

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.github.tommyettinger.gand.Float2UndirectedGraph;
import com.github.tommyettinger.gand.Path;
import com.github.tommyettinger.gand.algorithms.SearchStep;
import com.github.tommyettinger.gand.points.PointF2;
import com.github.tommyettinger.gand.utils.Heuristic;

/** This test shows how a {@link Float2UndirectedGraph} can be used on a grid-based map with no diagonal movement.
 *  It also shows how to use a {@link PathSmoother} on the found path to reduce the zigzag.
 * 
 * @author davebaol */
public class CaveAStarTest extends PathFinderTestBase {

	final static float width = 8; // 5; // 10;

	ShapeRenderer renderer;
	Vector3 tmpUnprojection = new Vector3();

	int lastScreenX;
	int lastScreenY;
	int lastEndTileX;
	int lastEndTileY;
	int startTileX;
	int startTileY;

	char[][] worldMap;
	Path<PointF2> path;
	Heuristic<PointF2> heuristic = (a, b) -> Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
	Float2UndirectedGraph pathFinder;
	PathSmoother<PointF2> pathSmoother;

	boolean smooth = false;
	boolean diagonal = false;

	CheckBox checkDiagonal;
	CheckBox checkSmooth;

	public CaveAStarTest(PathFinderDemos container) {
		super(container, "Flat Tiled A*");
	}

	@Override
	public void create () {
		lastEndTileX = -1;
		lastEndTileY = -1;
		startTileX = 1;
		startTileY = 1;

		// Create the map
		String[] caves = new String[]{
						"###########################################################################",
						"####.........#######......####.............#####.....#########.......######",
						"####.........######......#####.............######....#########......#######",
						"####...##....######.....######.............######...###########....########",
						"####..############....#########..............####...############..#########",
						"####..#############...##########.............#####..############..#########",
						"###....##########......###..####...#.........####.....#########....########",
						"#.......#########...................##.......###......#########....########",
						"#.......########...##...........#....##.....####......##..###.......#######",
						"#.......##...###....####..............###...####.....##...###............##",
						"##......##..........#######...............########...#.....##............##",
						"###.....#..........###########.............#####....#......##.....#.......#",
						"####...............#####..######............##.............##.....#.......#",
						"####................####....#######.......................##...####.......#",
						"####..........###....###....#########.................#...#....##.........#",
						"#.........########....####...########.................#####....#........###",
						"#.........########....####...########.......##........######...#......#####",
						"#.......#########.....####...########......###........#####....#..#########",
						"#.......########......###....#######...#######........######..##...########",
						"###....#########......##.....#######...########......##########....########",
						"##......########.....###.....#######....#######......#########......#######",
						"###....###...####..#####....######......########......########.......######",
						"##.....##.....#########.....######......#########.......#####........######",
						"##.....##.....####........#######........#########.......####........######",
						"#.......##....###.........######.........##########.......###........######",
						"#...#....##...###.....##########.........##########.....................###",
						"#...#....###...##.....#########..........###########.....................##",
						"#.####....###........######..##...........###########.....................#",
						"######.....#......########................###########............#........#",
						"#######........#..........................###########.....###....####.....#",
						"#######.......##............##............##########......####..######....#",
						"##.......#...####..........####.........############......#################",
						"#........########........######.........############.....##################",
						"#..........########....########........############....####################",
						"#......#...########...##########......#############..##############...#####",
						"##....###...#######...##########......#############...###########.....#####",
						"##...####...#######....#######........############.....##..#####....#######",
						"########......#######...######........###########.......................###",
						"#######.......#######.....####..........#########........................##",
						"#######......#########.....######..........###............................#",
						"########.....###########......####............................#...........#",
						"#...#####...#############......#####..###..................#.###..#####...#",
						"#....#########.############....##########................######...#######.#",
						"#.....#######.......######....##########................#######...##...####",
						"#......####............##.....####........####........#########...#......##",
						"###...........................####........#####......###########.........##",
						"###...........##.............####........######...######..######..........#",
						"###.........####...........#####........#######...#####...######..........#",
						"####....########.........#######........#######....##.....######....##....#",
						"####...##########......##########....##########...........######...########",
						"###....##########......#########......##########..........######...########",
						"##......##########......########.......#########..........######.....######",
						"##......###########.....#######........#########..........########.......##",
						"#........#..#######.......#####........#########..........#######........##",
						"#.....#.......######........###........##########........########.........#",
						"#.....#........#####......................#######.......##....#...........#",
						"#...##....#....######......................#######......#............###..#",
						"#...#....##....#######.........................####....##............###..#",
						"##.....###.....#######.............#............#########............###..#",
						"###...##.......######.......###....####......#...#######...##........###..#",
						"#######......#######......######..######....##....#####....#####......###.#",
						"####......#....####.....#######################.....#.....#####........####",
						"###......##.....###.....########################.........######........####",
						"###.....####...####....###########################.....########........####",
						"##......##########....###############...###########...##########..#########",
						"##......##########.....############.....###########...##########..#########",
						"###...############......##....####....#############....#########..#########",
						"###....##############.....................#######......#######....##....###",
						"####....###..####..##......................#####.......#######............#",
						"######.......###............................###.......########............#",
						"####............................#...........#.........###########.........#",
						"###..................#####.....###..#####...........##############........#",
						"##.....................##.....###...#######........###############........#",
						"#...........................#####...##...###.......###########..##........#",
						"#...........####...........######...#......###....####....###...####.....##",
						"#.###......#####..........########.........###....####.........######...###",
						"#.###.....########......##########..........##.................####......##",
						"####.....#########################..........###.........###....####......##",
						"####...###########################....##....###........###########..##..###",
						"###....###########################...##########......#############..#######",
						"####...#############....#..#######...##########......############....######",
						"##......###..###............#####.....########..........########.........##",
						"##...........................####.....######..............######.........##",
						"#...##.......................###......####.................#####..........#",
						"#....####.....................##......###..........##.......#.......###...#",
						"#....#######...........................##.........#####.............###...#",
						"#...###########........................##........######............####...#",
						"#...#####..#####...#...................####......#######..........######..#",
						"#....####....#######..#................###.......#########.......##########",
						"##....###....#########........##.......###........###########....##########",
						"###....####...#######.......####.......###.........########################",
						"###....####...######.....#..####.......####.........###############..######",
						"##.....####...######.....#######......######.........############.....#####",
						"#......###....#####.....#########....#########.......###########.......####",
						"#......##.....#####...############..############.....###########.......####",
						"#.....###.....#####...###########...############.....###########.......####",
						"##..#####....######...###########...###########..........#######.......####",
						"########.....#####....##########....##########.............#.#........#####",
						"###........######......#######......##########.....................#......#",
						"##.........######......#######.......########......................#......#",
						"##.....##########......########.......#####.......###...........####......#",
						"##.....##########.....###########........##......#####.....#.#######......#",
						"#.....######..###......#########.................######....##########.....#",
						"#..########.............########.....##........########....###########....#",
						"#.......................#########....###...############....##########.....#",
						"#............##........###########...##################...#########....#..#",
						"##..........######.....###########....################....#######......####",
						"##........########.....###########....##############......#######......####",
						"####....###########...#############...##############......#######....######",
						"####...############...##############..#########..###.....#########...######",
						"####...############..###############...#######...##.....##########...######",
						"###.....##########....##############...###..##...##.....##########....#####",
						"###.......######.......##############............#.....#########......#####",
						"#####.......####.......##############...........##.....##...###.......#####",
						"######......####.......##...###..#######........###....##...##.......######",
						"####.........####......##.........#####..........##....##............######",
						"##............####.....#...........####...........#....##..........########",
						"#.............#####..................###.....#....#....##.........#########",
						"#.............#####...................#.....##....##..###.........#########",
						"#........##..######..........##...........#####...######....#....####....##",
						"#.......#######..........#########.......#######...#####...##....####.....#",
						"###.....#######..........###########....########....##....###.............#",
						"###.....#######........##############...#########.........####.........####",
						"###....#########.......##############....#########......######........#####",
						"###########################################################################",
		};
		worldMap = new char[caves.length][];
		for (int i = 0; i < caves.length; i++) {
			worldMap[i] = caves[i].toCharArray();
		}
		path = new Path<>(worldMap.length + worldMap[0].length);
		startTileX = 1;
		startTileY = caves[1].indexOf('.');
		lastEndTileX = caves.length-2;
		lastEndTileY = caves[lastEndTileX].lastIndexOf('.');
		updatePath(true);
		pathSmoother = new PathSmoother<>(new CharOrthoRaycastCollisionDetector(worldMap));
//		pathSmoother = new PathSmoother<>(new CharBresenhamRaycastCollisionDetector(worldMap));

		renderer = new ShapeRenderer();
		inputProcessor = new TiledAStarInputProcessor(this);

		Table detailTable = new Table(container.skin);

		detailTable.row();
		checkSmooth = new CheckBox("[RED]S[]mooth Path", container.skin);
		checkSmooth.setChecked(smooth);
		checkSmooth.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				CheckBox checkBox = (CheckBox)event.getListenerActor();
				smooth = checkBox.isChecked();
				updatePath(true);
			}
		});
		detailTable.add(checkSmooth);

		detailTable.row();
		checkDiagonal = new CheckBox("Prefer [RED]D[]iagonal", container.skin);
		checkDiagonal.setChecked(diagonal);
		checkDiagonal.addListener(new ChangeListener() {
			@Override
			public void changed (ChangeEvent event, Actor actor) {
				CheckBox checkBox = (CheckBox)event.getListenerActor();
				diagonal = checkBox.isChecked();
				updatePath(true);
			}
		});
		detailTable.add(checkDiagonal);

		detailTable.row();
		addSeparator(detailTable);

		detailWindow = createDetailWindow(detailTable);
	}

	@Override
	public void render () {
		renderer.begin(ShapeType.Filled);
		for (int x = 0; x < worldMap.length; x++) {
			for (int y = 0; y < worldMap[x].length; y++) {
				switch (worldMap[x][y]) {
					case '.':
					renderer.setColor(Color.WHITE);
					break;
				case '#':
					renderer.setColor(Color.GRAY);
					break;
				default:
					renderer.setColor(Color.BLACK);
					break;
				}
				renderer.rect(x * width, y * width, width, width);
			}
		}

		renderer.setColor(Color.RED);
		int nodeCount = path.size();
		for (int i = 0; i < nodeCount; i++) {
			PointF2 node = path.get(i);
			renderer.rect(node.x * width, node.y * width, width, width);
		}
		if (smooth) {
			renderer.end();
			renderer.begin(ShapeType.Line);
			float hw = width / 2f;
			if (nodeCount > 0) {
				PointF2 prevNode = path.getFirst();
				for (int i = 1; i < nodeCount; i++) {
					PointF2 node = path.get(i);
					renderer.line(node.x * width + hw, node.y * width + hw, prevNode.x * width + hw, prevNode.y * width + hw);
					prevNode = node;
				}
			}
		}
		renderer.end();
	}

	@Override
	public void dispose () {
		renderer.dispose();

		worldMap = null;
		path = null;
		heuristic = null;
		pathFinder = null;
		pathSmoother = null;
	}

	public Camera getCamera () {
		return container.stage.getViewport().getCamera();
	}

	private void updatePath (boolean forceUpdate) {
		getCamera().unproject(tmpUnprojection.set(lastScreenX, lastScreenY, 0));
		int tileX = (int)(tmpUnprojection.x / width);
		int tileY = (int)(tmpUnprojection.y / width);
		if (forceUpdate || tileX != lastEndTileX || tileY != lastEndTileY) {
			if(pathFinder == null) pathFinder = new Float2UndirectedGraph(worldMap, '.', 1f, PointF2::dst, diagonal);
			else {
				pathFinder.removeAllVertices();
				pathFinder.initVertices(worldMap, '.');
				pathFinder.initEdges(worldMap.length, worldMap[0].length, 1f, PointF2::dst, diagonal);
			}
			PointF2 startVec = new PointF2(startTileX, startTileY), endVec = new PointF2(tileX, tileY);
			char endNode = ' ';
			if(tileX >= 0 && tileY >= 0 && tileX < worldMap.length && tileY < worldMap[tileX].length)
				endNode = worldMap[tileX][tileY];
			if (forceUpdate || endNode == '.') {
				if (endNode == '.') {
					lastEndTileX = tileX;
					lastEndTileY = tileY;
				} else {
					endVec.set(lastEndTileX, lastEndTileY);
				}
				path.clear();
				path.addAll(pathFinder.algorithms().findShortestPath(startVec, endVec, heuristic, SearchStep::vertex));
				if (smooth) {
					pathSmoother.smoothPath(path);
				}
			}
		}
	}

	/** An {@link InputProcessor} that allows you to define a path to find.
	 * 
	 * @autor davebaol */
	static class TiledAStarInputProcessor extends InputAdapter {
		CaveAStarTest test;

		public TiledAStarInputProcessor (CaveAStarTest test) {
			this.test = test;
		}

		@Override
		public boolean keyTyped (char character) {
			switch (character) {
			case 'd':
			case 'D':
				test.checkDiagonal.toggle();
				break;
			case 's':
			case 'S':
				test.checkSmooth.toggle();
				break;
			}
			return true;
		}

		@Override
		public boolean touchUp (int screenX, int screenY, int pointer, int button) {
			test.getCamera().unproject(test.tmpUnprojection.set(screenX, screenY, 0));
			int tileX = (int)(test.tmpUnprojection.x / width);
			int tileY = (int)(test.tmpUnprojection.y / width);
			char startNode = test.worldMap[tileX][tileY];
			if (startNode == '.') {
				test.startTileX = tileX;
				test.startTileY = tileY;
				test.updatePath(true);
			}
			return true;
		}

		@Override
		public boolean mouseMoved (int screenX, int screenY) {
			test.lastScreenX = screenX;
			test.lastScreenY = screenY;
			test.updatePath(false);
			return true;
		}
	}
}
