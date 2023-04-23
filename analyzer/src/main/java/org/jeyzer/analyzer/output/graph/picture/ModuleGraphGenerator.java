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




import java.util.List;
import java.util.Map;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.jeyzer.analyzer.config.report.ConfigGraph;
import org.jeyzer.analyzer.data.module.ProcessModule;
import org.jeyzer.analyzer.data.tag.ModuleTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.setup.GraphSetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleGraphGenerator extends GraphGenerator{
	
	private static final Logger logger = LoggerFactory.getLogger(ModuleGraphGenerator.class);
	
	protected static final String NODE_UI_CLASS_MODULE = "Module";
	
	public ModuleGraphGenerator(GraphSetupManager graphSetupManager, ConfigGraph configGraph, String sheetName, Map<String, ExcelGraphPicture> graphPictureRepository) throws JzrException {
		super(graphSetupManager, configGraph, sheetName, graphPictureRepository);
	}

	public ExcelGraphPicture generateGraphPicture(String graphName, List<ProcessModule> modules, String extraId){
		ExcelGraphPicture picture;
		
		if (modules == null || modules.isEmpty())
			return null;
		
		logger.info("Generating module graph for : {}", graphName);
		
		// Check picture cache first
		String key = ModuleGraphGenerator.class.getName() 
				+ graphName
				+ ((extraId != null) ? extraId : "")
				+ configGraphHashCode;
		picture = this.graphPictureRepository.get(key);
		
		if (picture == null){
			picture = createGraphPicture(graphName, modules);
			this.graphPictureRepository.put(key, picture);
			logger.debug("Module graph created for key : " + configGraphHashCode);
		}
		else{
			logger.debug("Module graph obtained from cache for key : " + configGraphHashCode);
		}
		
		return picture;
	}
	
	private ExcelGraphPicture createGraphPicture(String graphName, List<ProcessModule> modules) {
		Graph graph = createGraph(ModuleGraphGenerator.class.getName());
		SpriteManager sman = new SpriteManager(graph);
	
		browseModules(graph, sman, modules);
		
		return generateImage(graph, graphName, 1);
	}

	public ExcelGraphPicture generateGraphPicture(String graphName, List<ProcessModule> modules){
		return generateGraphPicture(graphName, modules, null);
	}

	private void browseModules(Graph graph, SpriteManager sman, List<ProcessModule> modules) {
		
		for (ProcessModule module : modules) {
			Node parent = graph.getNode(module.getName());
			if (parent == null)
				parent = createParentNode(graph, module);

			for (String childModuleName : module.getRequires()) {
				Tag tag = new ModuleTag(childModuleName);
				addChildNode(graph, sman, parent, tag.getTypeName(), tag.getName(), 1);
			}
		}
	}

	private Node createParentNode(Graph graph, ProcessModule module) {
		Tag tag = new ModuleTag(module.getName());
		
		Node node = graph.addNode(module.getName());
		node.addAttribute(NODE_SIZE_MODE, NODE_DYN_SIZE);
		node.addAttribute(NODE_ELEMENT_COUNT, 0); // number of dependencies on this node
		node.addAttribute(NODE_UI_SIZE, PIXELS_UNIT * 1);		
		node.addAttribute(NODE_UI_CLASS, tag.getTypeName());
		node.addAttribute(ELEMENT_UI_LABEL, tag.getName());
		
		return node;
	}

	@Override
	public Logger getLogger() {
		return logger;
	}
}
