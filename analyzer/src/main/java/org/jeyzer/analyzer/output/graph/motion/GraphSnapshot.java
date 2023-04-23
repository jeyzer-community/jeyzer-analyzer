package org.jeyzer.analyzer.output.graph.motion;

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







public class GraphSnapshot {
	
	private String picturePath; // can be null
	private String htmlPath;
	
	public GraphSnapshot(String picturePath, String htmlPath){
		this.picturePath = picturePath;
		this.htmlPath = htmlPath;
	}

	public String getPicturePath() {
		return picturePath;   // can be null
	}

	public String getHtmlPath() {
		return htmlPath;
	}
}
