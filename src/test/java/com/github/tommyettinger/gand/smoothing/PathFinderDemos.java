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

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/** Test class for pathfinding algorithms.
 * 
 * @author davebaol */
public class PathFinderDemos extends ApplicationAdapter {

	public static void main (String[] argv) {
		GraphicalTestUtils.launch(new PathFinderDemos());
	}

	private static final boolean DEBUG_STAGE = false;

	public CollapsableWindow algorithmSelectionWindow;

	PathFinderTestBase [] tests = {
		new CaveAStarTest(this),
	};

	Table testsTable;
	PathFinderTestBase currentTest;

	public Skin skin;
	public float stageWidth;
	public float stageHeight;
	public Stage stage;

	@Override
	public void create () {
		Gdx.gl.glClearColor(.3f, .3f, .3f, 1);

		skin = new Skin(Gdx.files.internal("data/uiskin.json"));

		// Enable color markup
		BitmapFont font = skin.get("default-font", BitmapFont.class);
		font.getData().markupEnabled = true;

		stage = new Stage();
		stage.setDebugAll(DEBUG_STAGE);
		stageWidth = stage.getWidth();
		stageHeight = stage.getHeight();

		Gdx.input.setInputProcessor(new InputMultiplexer(stage));

		Stack stack = new Stack();
		stage.addActor(stack);
		stack.setSize(stageWidth, stageHeight);
		testsTable = new Table();
		stack.add(testsTable);

		// Create behavior selection window
		List<String> testList = createTestList();
		algorithmSelectionWindow = addBehaviorSelectionWindow("Path Finder Tests", testList, 0, -1);

		// Set selected test
		changeTest(0);

		stage.addActor(new FpsLabel("FPS: ", skin));
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (currentTest != null) currentTest.render();

		stage.act();
		stage.draw();
	}

	@Override
	public void resize (int width, int height) {
		super.resize(width, height);
		stage.getViewport().update(width, height, true);
		stageWidth = width;
		stageHeight = height;
	}

	private List<String> createTestList () {
		// Create behavior names
		int numBehaviors = tests.length;
		String[] algorithmNames = new String[numBehaviors];
		for (int i = 0; i < numBehaviors; i++) {
			algorithmNames[i] = tests[i].testName;
		}

		final List<String> algorithmList = new List<>(skin);
		algorithmList.setItems(algorithmNames);
		algorithmList.addListener(new ClickListener() {
			@Override
			public void clicked (InputEvent event, float x, float y) {
				if (!algorithmSelectionWindow.isCollapsed() && getTapCount() == 2) {
					changeTest(algorithmList.getSelectedIndex());
					algorithmSelectionWindow.collapse();
				}
			}
		});
		return algorithmList;
	}

	protected CollapsableWindow addBehaviorSelectionWindow (String title, List<String> testList, float x, float y) {

		CollapsableWindow window = new CollapsableWindow(title, skin);
		window.row();

		ScrollPane pane = new ScrollPane(testList, skin);
		pane.setFadeScrollBars(false);
		pane.setScrollX(0);
		pane.setScrollY(0);

		window.add(pane);
		window.pack();
		window.pack();
		if (window.getHeight() > stage.getHeight()) {
			window.setHeight(stage.getHeight());
		}
		window.setX(x < 0 ? stage.getWidth() - (window.getWidth() - (x + 1)) : x);
		window.setY(y < 0 ? stage.getHeight() - (window.getHeight() - (y + 1)) : y);
		window.layout();
		window.collapse();
		stage.addActor(window);

		return window;
	}

	void changeTest (int index) {
		// Remove the old behavior and its window
		testsTable.clear();
		if (currentTest != null) {
			if (currentTest.getDetailWindow() != null) currentTest.getDetailWindow().remove();
			currentTest.dispose();
		}

		// Add the new behavior and its window
		currentTest = tests[index];
		currentTest.create();
		InputMultiplexer im = (InputMultiplexer)Gdx.input.getInputProcessor();
		if (im.size() > 1) im.removeProcessor(1);
		if (currentTest.getInputProcessor() != null) im.addProcessor(currentTest.getInputProcessor());
		if (currentTest.getDetailWindow() != null) stage.addActor(currentTest.getDetailWindow());
	}

}
