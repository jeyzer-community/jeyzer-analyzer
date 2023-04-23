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

public class ConfigGraphResolution {
	
	public static final String JZRA_RESOLUTION = "picture_resolution";
	
	private static final String JZRA_WIDTH = "width";
	private static final String JZRA_HEIGHT = "height";
	
	private int width = 2560; // default
	private int height = 2048; // default
	
	public ConfigGraphResolution(Element resolutionNode){
		if (resolutionNode.hasAttribute(JZRA_WIDTH))
			this.width = Integer.valueOf(ConfigUtil.getAttributeValue(resolutionNode,JZRA_WIDTH));
		if (resolutionNode.hasAttribute(JZRA_HEIGHT))
			this.height = Integer.valueOf(ConfigUtil.getAttributeValue(resolutionNode,JZRA_HEIGHT));
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + height;
		result = prime * result + width;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigGraphResolution other = (ConfigGraphResolution) obj;
		if (height != other.height)
			return false;
		if (width != other.width)
			return false;
		return true;
	}
}
