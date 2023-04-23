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







import java.util.Iterator;
import java.util.Map;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.jeyzer.analyzer.config.report.ConfigGraph;
import org.jeyzer.analyzer.data.action.ActionGraphSection;
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.analyzer.data.tag.FunctionTag;
import org.jeyzer.analyzer.data.tag.OperationTag;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.setup.GraphSetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalStackGraphGenerator extends GraphGenerator{

	protected static final String GLOBAL_GRAPH_NAME = "Global graph";
	
	private static final Logger logger = LoggerFactory.getLogger(GlobalStackGraphGenerator.class);	
	
	private Graph graph;         // stateful, non thread safe
	private SpriteManager sman;  // stateful, non thread safe
	private Node rootNode;       // stateful, non thread safe
	
	public GlobalStackGraphGenerator(GraphSetupManager graphSetupManager, ConfigGraph configGraph, String sheetName, Map<String, ExcelGraphPicture> graphPictureRepository) throws JzrException {
		super(graphSetupManager, configGraph, sheetName, graphPictureRepository);
		initialize();
	}

	private void initialize() {
		this.graph = createGraph(GlobalStackGraphGenerator.class.getName());
		this.sman = new SpriteManager(graph);
		this.rootNode = createExecutorRootNode(graph, GLOBAL_GRAPH_NAME);
	}

	public void addStack(ThreadStackHandler stack, int stackCount) {
		// ignore if stack count is lower than size threshold (although note this is NOT action)
		if (stackCount < this.configGraph.getActionSizeThreshold())
			return;
		
		int nodeCount = stack.getThreadStack().getOperationTags().size() + stack.getThreadStack().getFunctionTags().size(); 
		if (nodeCount < this.configGraph.getNodeThreshold())
			return;
		
		ActionGraphSection section = new ActionGraphSection(stack.getThreadStack(), 0, false);
		Iterator<String> functionTag = section.getSourceFunctionTags().iterator();
		Iterator<String> operationTag = section.getSourceOperationTags().iterator();
		Iterator<String> codeLineIter = section.getCodeLines().iterator();
		
		Node lastNode = this.rootNode;
		while(codeLineIter.hasNext()){
			codeLineIter.next();
			String function = functionTag.next();
			if (function != null)
				lastNode = addChildNode(graph, sman, lastNode, FunctionTag.DISPLAY_NAME, function, stackCount);
			String operation = operationTag.next();
			if (operation != null)
				lastNode = addChildNode(graph, sman, lastNode, OperationTag.DISPLAY_NAME, operation, stackCount);
		}
	}

	public ExcelGraphPicture generateGraphPicture(){
		ExcelGraphPicture picture;
		
		filterNodes(graph, rootNode);
		if (graph.getNodeSet().isEmpty()){
			logger.info("Global graph generation skipped : node filtering removed all nodes.");
			return null;
		}
		
		logger.info("Generating global graph");
		
		// Check picture cache first
		String key = GlobalStackGraphGenerator.class.getName() 
				+ GLOBAL_GRAPH_NAME
				+ this.graph.getNodeCount()
				+ configGraphHashCode;
		picture = this.graphPictureRepository.get(key);
		
		if (picture == null){
			picture = generateImage(this.graph, GLOBAL_GRAPH_NAME, 0);
			this.graphPictureRepository.put(key, picture);
			logger.debug("Global graph created for key : " + configGraphHashCode);
		}
		else{
			logger.debug("Global graph obtained from cache for key : " + configGraphHashCode);
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
