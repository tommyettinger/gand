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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.StringBuilder;

/** A label showing current FPS prefixed by a constant string.
 * 
 * @author davebaol */
public class FpsLabel extends Label {

	float oldValue;
	int appendIndex;

	public FpsLabel(CharSequence text, Skin skin) {
		this(text, skin.get(Label.LabelStyle.class));
	}

	public FpsLabel(CharSequence text, Skin skin, String styleName) {
		this(text, skin.get(styleName, Label.LabelStyle.class));
	}

	public FpsLabel(CharSequence text, Skin skin, String fontName, Color color) {
		this(text, new Label.LabelStyle(skin.getFont(fontName), color));
	}

	public FpsLabel(CharSequence text, Skin skin, String fontName, String colorName) {
		this(text, new Label.LabelStyle(skin.getFont(fontName), skin.getColor(colorName)));
	}

	public FpsLabel(CharSequence text, Label.LabelStyle style) {
		this(text, -1, style);
	}

	public FpsLabel(CharSequence text, float initialValue, LabelStyle style) {
		super(text.toString() + initialValue, style);
		this.oldValue = initialValue;
		this.appendIndex = text.length();

	}

	public int getValue () {
		return Gdx.graphics.getFramesPerSecond();
	}

	@Override
	public void act (float delta) {
		int newValue = getValue();
		if (oldValue != newValue) {
			oldValue = newValue;
			StringBuilder sb = getText();
			sb.setLength(appendIndex);
			sb.append(oldValue);
			invalidateHierarchy();
		}
		super.act(delta);
	}
}
