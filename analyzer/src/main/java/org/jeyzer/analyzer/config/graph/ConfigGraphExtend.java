package org.jeyzer.analyzer.config.graph;

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




import org.jeyzer.analyzer.config.ConfigUtil;
import org.w3c.dom.Element;

public class ConfigGraphExtend {
	
	private static final String JZRA_LEFT = "left";
	private static final String JZRA_RIGHT = "right";
	private static final String JZRA_TOP = "top";
	private static final String JZRA_BOTTOM = "bottom";

	private int extendLeft = -1;
	private int extendRight = -1;
	private int extendBottom = -1;
	private int extendTop = -1;

	public ConfigGraphExtend(Element extendAreaNode) {
		if (extendAreaNode == null)
			return;
		
		int value = Integer.valueOf(ConfigUtil.getAttributeValue(extendAreaNode,JZRA_LEFT));
		if (value >= 0 && value <100)
			this.extendLeft = value;
		
		value = Integer.valueOf(ConfigUtil.getAttributeValue(extendAreaNode,JZRA_RIGHT));
		if (value >= 0 && value <100)
			this.extendRight = value;
		
		value = Integer.valueOf(ConfigUtil.getAttributeValue(extendAreaNode,JZRA_TOP));
		if (value >= 0 && value <100)
			this.extendTop = value;
		
		value = Integer.valueOf(ConfigUtil.getAttributeValue(extendAreaNode,JZRA_BOTTOM));
		if (value >= 0 && value <100)
			this.extendBottom = value;
	}
	
	public int getTop() {
		return extendTop;
	}
	
	public int getLeft() {
		return extendLeft;
	}
	
	public int getRight() {
		return extendRight;
	}
	
	public int getBottom() {
		return extendBottom;
	}
	
	public void setLeft(int value) {
		this.extendLeft = value;
	}

	public void setRight(int value) {
		this.extendRight = value;
	}
	
	public void setTop(int value) {
		this.extendTop = value;
	}
	
	public void setBottom(int value) {
		this.extendBottom = value;
	}
	
}
