package org.jeyzer.analyzer.output.graph.picture;

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

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.jeyzer.analyzer.config.report.ConfigContentionTypes;
import org.jeyzer.analyzer.config.report.ConfigGraph;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.setup.GraphSetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalContentionTypeGraphGenerator extends GraphGenerator{

	protected static final String GLOBAL_GRAPH_NAME = "Global contention types";
	
	private static final Logger logger = LoggerFactory.getLogger(GlobalContentionTypeGraphGenerator.class);	
	
	private Graph graph;         // stateful, non thread safe
	private SpriteManager sman;  // stateful, non thread safe
	private Node rootNode;       // stateful, non thread safe
	
	private ConfigContentionTypes configContentionTypes;
	
	public GlobalContentionTypeGraphGenerator(GraphSetupManager graphSetupManager, ConfigContentionTypes configContentionTypes, String sheetName, Map<String, ExcelGraphPicture> graphPictureRepository) throws JzrException {
		super(graphSetupManager, configContentionTypes.getConfigGraph(), sheetName, graphPictureRepository);
		this.configContentionTypes = configContentionTypes;
		initialize();
	}

	private void initialize() {
		this.graph = createGraph(GlobalContentionTypeGraphGenerator.class.getName());
		this.sman = new SpriteManager(graph);
		this.rootNode = createFunctionRootNode(graph, GLOBAL_GRAPH_NAME);
	}

	public void addContentionType(Tag tag, int tagCount, int globalTagCount) {
		// action size threshold and nodes size threshold CONDITIONS are ignored : all stacks are taken into account
		
		if (this.configGraph.getMode() != ConfigGraph.GENERATION_MODE.RADIAL){
			logger.warn("Generating global contention type graph failed. Only graph radial mode is supported.");
			return;
		}
		
		int percent = FormulaHelper.percentRound(tagCount, globalTagCount);
		Node child = addGraphChildNode(this.graph, this.sman, this.rootNode, tag.getTypeName(), tag.getName(), percent, true);

		Map<String, String> hexaHighlights = configContentionTypes.getHighlightsConfig().getHexaHighlights();
		String hexaHighlight = hexaHighlights.get(tag.getName());
		if (hexaHighlight != null)
			child.addAttribute(NODE_UI_STYLE, NODE_FILL_COLOR + hexaHighlight + ";");
	}

	public ExcelGraphPicture generateGraphPicture(){
		ExcelGraphPicture picture;
		
		filterNodes(graph, rootNode);
		if (graph.getNodeSet().isEmpty()){
			logger.info("Global contention type graph generation skipped : node filtering removed all nodes.");
			return null;
		}
		
		logger.info("Generating the global contention type graph.");
		
		// Check picture cache first
		String key = GlobalContentionTypeGraphGenerator.class.getName() 
				+ GLOBAL_GRAPH_NAME
				+ this.graph.getNodeCount()
				+ configGraphHashCode;
		picture = this.graphPictureRepository.get(key);
		
		if (picture == null){
			picture = generateImage(this.graph, GLOBAL_GRAPH_NAME, 0);
			this.graphPictureRepository.put(key, picture);
			logger.debug("Global contention type graph created for key : " + configGraphHashCode);
		}
		else{
			logger.debug("Global contention type graph obtained from cache for key : " + configGraphHashCode);
		}
		
		// re-initialize
		initialize();
		
		return picture;
	}

	@Override
	public Logger getLogger() {
		return logger;
	}
}
