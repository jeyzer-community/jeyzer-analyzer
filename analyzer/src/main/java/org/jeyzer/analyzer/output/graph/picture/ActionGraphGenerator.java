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







import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.jeyzer.analyzer.config.report.ConfigGraph;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.action.ActionGraphSection;
import org.jeyzer.analyzer.data.stack.ThreadState;
import org.jeyzer.analyzer.data.tag.ATBITag;
import org.jeyzer.analyzer.data.tag.FunctionTag;
import org.jeyzer.analyzer.data.tag.OperationTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.data.tag.ThreadStateTag;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.setup.GraphSetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionGraphGenerator extends GraphGenerator{
	
	private static final Logger logger = LoggerFactory.getLogger(ActionGraphGenerator.class);

	private int count = 0;
	
	public ActionGraphGenerator(GraphSetupManager graphSetupManager, ConfigGraph configGraph, String sheetName, Map<String, ExcelGraphPicture> graphPictureRepository) throws JzrException {
		super(graphSetupManager, configGraph, sheetName, graphPictureRepository);
	}

	public ExcelGraphPicture generateGraphPicture(String graphName, String rootName, ActionGraphSection rootSection, String extraId){
		ExcelGraphPicture picture;
		
		if (count > this.configGraph.getGenerationMaximum())
			return null;
		
		if (rootSection.getStackCount() < this.configGraph.getActionSizeThreshold())
			return null;
		
		if (rootSection.getTreeFunctions().size() + rootSection.getTreeOperations().size() < this.configGraph.getNodeThreshold())
			return null;

		logger.info("Generating action graph for : {}", graphName);

		this.count++;
		
		// Check picture cache first
		String key = ActionGraphGenerator.class.getName() 
				+ graphName
				+ ((extraId != null) ? extraId : "")
				+ configGraphHashCode;
		picture = this.graphPictureRepository.get(key);
		
		if (picture == null){
			picture = createGraphPicture(graphName, rootName, rootSection);
			this.graphPictureRepository.put(key, picture);
			logger.debug("Action graph created for key : " + configGraphHashCode);
		}
		else{
			logger.debug("Action graph obtained from cache for key : " + configGraphHashCode);
		}
		
		return picture;
	}
	
	private ExcelGraphPicture createGraphPicture(String graphName, String rootName, ActionGraphSection rootSection) {
		Graph graph = createGraph(ActionGraphGenerator.class.getName());

		SpriteManager sman = new SpriteManager(graph);
		
		Node root = createExecutorRootNode(graph, rootName);
	
		browseSection(graph, sman, rootSection, root);

		// if enabled, the graph display doesn't expand
		// addActionInfo(sman, action);
		
		filterNodes(graph, root);
		
		return generateImage(graph, graphName, this.count);
	}

	public ExcelGraphPicture generateGraphPicture(String graphName, String rootName, ActionGraphSection rootSection){
		return generateGraphPicture(graphName, rootName, rootSection, null);
	}

	private void addActionInfo(SpriteManager sman, ThreadAction action) {
		Sprite s = sman.addSprite("action-" + action.getId());
		s.addAttribute(ELEMENT_UI_LABEL, action.getCompositeFunction());
		s.addAttribute(NODE_UI_CLASS, "ActionDetails");
		s.setPosition(30, 30, 0);
	}

	private void browseSection(Graph graph, SpriteManager sman, ActionGraphSection section, Node parent) {
		// process the functions
		SortedMap<Integer, String> functionTags = section.getSourceLocalizedFunctionTags();
		SortedMap<Integer, String> operationTags = section.getSourceLocalizedOperationTags();
		
		// use multi set to get it order by appearance
		List<Tag> tags = new ArrayList<>();
		for (Integer pos : functionTags.keySet()){
			tags.add(new FunctionTag(functionTags.get(pos)));
		}
		for (Integer pos : operationTags.keySet()){
			tags.add(new OperationTag(operationTags.get(pos)));
		}
		
		Node lastNode = parent; // in case map is empty, last node will be current node
		if (tags.isEmpty() && this.configGraph.isDisplayATBINodes())
			lastNode = addATBIChildNode(graph, sman, lastNode, section);
			
		for (Tag tag : tags){
			lastNode = addChildNode(graph, sman, lastNode, tag.getTypeName(), tag.getName(), section.getStackCount());
		}
		
		// States are most of the time on end leafs, but some exceptions exist with blocked threads.
		addStates(graph, sman, lastNode, section);
		if (section.getChildren() == null){
			// build the thread state tag
			return;
		}
		
		for (ActionGraphSection childSection : section.getChildren()){
			browseSection(graph, sman, childSection, lastNode);
		}
	}

	private Node addATBIChildNode(Graph graph, SpriteManager sman, Node node, ActionGraphSection section) {
		// support ATBI nodes only in tree mode
		if (this.configGraph.getMode() != ConfigGraph.GENERATION_MODE.TREE)
			return node;
		
		if (section.getStackCount() < this.configGraph.getStackCountThreshold()
				|| section.getSize() < this.configGraph.getSectionSizeThreshold())
			return node;
		
		Tag tag = new ATBITag("ATBI");
		return addChildNode(graph, sman, node, tag.getTypeName(), tag.getName(), section.getStackCount());
	}

	private void addStates(Graph graph, SpriteManager sman, Node node, ActionGraphSection section) {
		// Do it only for tree graphs
		if (this.configGraph.getMode() != ConfigGraph.GENERATION_MODE.TREE 
				&& this.configGraph.getMode() != ConfigGraph.GENERATION_MODE.TREE_MERGE)
			return;
		
		List<ThreadState> states = this.configGraph.getDisplayedThreadStates();
		if (states == null)
			return;
		
		for (ThreadState state : states){
			int stateCount = section.getThreadStateCount(state);
			if (stateCount > 0){
				Tag tag = null;
				
				// get state
				if (state.isBlocked())
					tag = new ThreadStateTag(ThreadStateTag.THREAD_STATE.LOCKED);
				else if (state.isRunning())
					tag = new ThreadStateTag(ThreadStateTag.THREAD_STATE.RUNNING);
				else if (state.isWaiting())
					tag = new ThreadStateTag(ThreadStateTag.THREAD_STATE.WAITING);
				else if (state.isTimedWaiting())
					tag = new ThreadStateTag(ThreadStateTag.THREAD_STATE.TIMED_WAITING);

				if (tag != null)
					addChildNode(graph, sman, node, tag.getName(), tag.getName(), stateCount);
			}
		}
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
