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







import org.graphstream.graph.Graph;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.jeyzer.analyzer.config.graph.ConfigGraphRendering;

public class GraphContext {

	private Graph graph;
	private SpriteManager sman;
	private int period;
	private ConfigGraphRendering dynamicGraphCfg;
	private boolean viewerEnabled;
	
	@SuppressWarnings("unused")
	private GraphContext(){
	}
	
	public GraphContext(final Graph graph, SpriteManager sman, int period, ConfigGraphRendering replayCfg, boolean viewerEnabled){
		this.graph = graph;
		this.sman = sman;
		this.period = period;
		this.dynamicGraphCfg = replayCfg;
		this.viewerEnabled = viewerEnabled;
	}
	
	public Graph getGraph() {
		return graph;
	}

	public SpriteManager getSpriteManager() {
		return sman;
	}

	public int getThreadDumpPeriod() {
		return period;
	}

	public ConfigGraphRendering getDynamicGraphCfg() {
		return dynamicGraphCfg;
	}

	public boolean isGraphViewerEnabled() {
		return viewerEnabled;
	}
	
}
