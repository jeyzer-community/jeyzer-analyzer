package org.jeyzer.analyzer.output.poi.rule;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */







public class Delta {

	public static final String INCREASE_FACTOR_FIELD = "increase_factor";
	public static final String INCREASE_COLOR_FIELD = "increase_color";
	
	public static final String DECREASE_FACTOR_FIELD = "decrease_factor";
	public static final String DECREASE_COLOR_FIELD = "decrease_color";
	
	private float increaseFactor;
	private Object increaseColor;
	
	private float decreaseFactor;
	private Object decreaseColor;
	
	public Delta() {
	}

	public float getIncreaseFactor() {
		return increaseFactor;
	}

	public void setIncreaseFactor(float increaseFactor) {
		this.increaseFactor = increaseFactor;
	}

	public Object getIncreaseColor() {
		return increaseColor;
	}

	public void setIncreaseColor(Object color) {
		this.increaseColor = color;
	}

	public float getDecreaseFactor() {
		return decreaseFactor;
	}

	public void setDecreaseFactor(float decreaseFactor) {
		this.decreaseFactor = decreaseFactor;
	}

	public Object getDecreaseColor() {
		return decreaseColor;
	}

	public void setDecreaseColor(Object color) {
		this.decreaseColor = color;
	}

}
