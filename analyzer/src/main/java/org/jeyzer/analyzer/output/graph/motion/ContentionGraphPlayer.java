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







import java.util.Map;

import org.jeyzer.analyzer.config.graph.ConfigContentionGraphPlayer;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.output.graph.node.contention.GlobalRootNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentionGraphPlayer extends GraphPlayer{
	
	private static final Logger logger = LoggerFactory.getLogger(ContentionGraphPlayer.class);
	
	private static final String PLAYER_NAME = "contention";
	private static final String NODE_KEY = "globalContention";
	private static final String NODE_NAME = "Global contention types";
	
	private GlobalRootNode rootNode;
	
	public ContentionGraphPlayer(ConfigContentionGraphPlayer playerCfg, String target, int period) throws JzrInitializationException{
		super(playerCfg, target, period);
		
		if (playerCfg.getConfigViewer().isEnabled())
			this.graphCtx.getGraph().display();
	}
	
	@Override
	public String getName() {
		return PLAYER_NAME;
	}

	@Override
	public void play(ThreadDump dump) {
		if (!dump.getWorkingThreads().isEmpty()){
			resetGraph();
			updateGraph(dump);
		}
		else
		{
			displayNoActivity();
		}
	}

	private void resetGraph() {
		if (rootNode != null)
			rootNode.remove();
	}


	@Override
	protected void phantomGraph() {
		logger.debug("Phantomazing the graph");
		if (rootNode != null)
			rootNode.phantom();
	}
	
	private void updateGraph(ThreadDump dump) {
		logger.debug("Updating the graph");
		
		if (!dump.getWorkingThreads().isEmpty())
			removeNoActivity();
		
		if (noThreadDump)
			removeNoThreadDump();

		if (restart)
			removeRestart();

		this.rootNode = new GlobalRootNode(graphCtx, NODE_KEY, NODE_NAME);
		
		Map<String, String> highlights = ((ConfigContentionGraphPlayer)this.playerCfg).getHighlightsConfig().getHexaHighlights();
		
		this.rootNode.update(dump.getPrincipalContentionTypes(), highlights);
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

}
