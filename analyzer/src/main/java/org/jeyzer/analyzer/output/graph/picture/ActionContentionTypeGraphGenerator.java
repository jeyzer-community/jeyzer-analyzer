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
import org.jeyzer.analyzer.data.action.ActionGraphSection;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.setup.GraphSetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multiset;

public class ActionContentionTypeGraphGenerator extends GraphGenerator{
	
	private static final Logger logger = LoggerFactory.getLogger(ActionContentionTypeGraphGenerator.class);

	private static final String PICTURE_FILE_PREFIX = "ct-";
	
	private int count = 0;
	
	private ConfigContentionTypes configContentionTypes;
	
	public ActionContentionTypeGraphGenerator(GraphSetupManager graphSetupManager, ConfigContentionTypes configContentionTypes, String sheetName, Map<String, ExcelGraphPicture> graphPictureRepository) throws JzrException {
		super(graphSetupManager, configContentionTypes.getConfigGraph(), sheetName, graphPictureRepository);
		this.configContentionTypes = configContentionTypes;
	}

	public ExcelGraphPicture generateGraphPicture(String graphName, String rootName, ActionGraphSection rootSection, String extraId){
		ExcelGraphPicture picture;
		
		if (count > this.configGraph.getGenerationMaximum())
			return null;
		
		if (rootSection.getStackCount() < this.configGraph.getActionSizeThreshold())
			return null;
		
		if (rootSection.getTreeFunctions().size() + rootSection.getTreeOperations().size() < this.configGraph.getNodeThreshold())
			return null;
		
		if (this.configGraph.getMode() != ConfigGraph.GENERATION_MODE.RADIAL){
			logger.warn("Generating action contention type graph {} failed. Only graph radial mode is supported.", graphName);
			return null;
		}

		logger.info("Generating action contention type graph for : {}", graphName);

		this.count++;
		
		// Check picture cache first
		String key = ActionContentionTypeGraphGenerator.class.getName() 
				+ graphName
				+ ((extraId != null) ? extraId : "")
				+ configGraphHashCode;
		picture = this.graphPictureRepository.get(key);
		
		if (picture == null){
			picture = createGraphPicture(graphName, graphName, rootSection);
			this.graphPictureRepository.put(key, picture);
			logger.debug("Action contention type graph created for key : " + configGraphHashCode);
		}
		else{
			logger.debug("Action contention type graph obtained from cache for key : " + configGraphHashCode);
		}
		
		return picture;
	}
	
	private ExcelGraphPicture createGraphPicture(String graphName, String rootName, ActionGraphSection rootSection) {
		Graph graph = createGraph(ActionContentionTypeGraphGenerator.class.getName());

		SpriteManager sman = new SpriteManager(graph);
		
		Node root = createFunctionRootNode(graph, rootName);
	
		addContentionTypes(graph, sman, rootSection, root);

		filterNodes(graph, root);
		
		return generateImage(graph, PICTURE_FILE_PREFIX + graphName, this.count);
	}

	public ExcelGraphPicture generateGraphPicture(String graphName, String rootName, ActionGraphSection rootSection){
		return generateGraphPicture(graphName, rootName, rootSection, null);
	}

	private void addContentionTypes(Graph graph, SpriteManager sman, ActionGraphSection rootSection, Node parent) {
		Multiset<Tag> contentionTypeTags = rootSection.getPrincipalContentionTypes();

		int stackCount = contentionTypeTags.size();

		Map<String, String> hexaHighlights = configContentionTypes.getHighlightsConfig().getHexaHighlights();
		
		for (Tag tag : contentionTypeTags.elementSet()) {
			int tagCount = contentionTypeTags.count(tag);
			int percent = FormulaHelper.percentRound(tagCount, stackCount);
			Node child = addGraphChildNode(graph, sman, parent, tag.getTypeName(), tag.getName(), percent, true);
			
			String hexaHighlight = hexaHighlights.get(tag.getName());
			if (hexaHighlight == null)
				continue;
			
			child.addAttribute(NODE_UI_STYLE, NODE_FILL_COLOR + hexaHighlight + ";");
		}
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
