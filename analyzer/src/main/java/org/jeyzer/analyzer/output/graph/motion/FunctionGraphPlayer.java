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







import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Map.Entry;

import org.jeyzer.analyzer.config.graph.ConfigFunctionGraphPlayer;
import org.jeyzer.analyzer.config.graph.ConfigFunctionGraphRendering;
import org.jeyzer.analyzer.config.graph.ConfigFunctionGraphRendering.GENERATION_MODE;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.tag.FunctionTag;
import org.jeyzer.analyzer.data.tag.OperationTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.data.tag.ThreadStateTag;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.output.graph.node.function.RootNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionGraphPlayer extends GraphPlayer{
	
	private static final Logger logger = LoggerFactory.getLogger(FunctionGraphPlayer.class);
	
	private static final String PLAYER_NAME = "function";
	
	private Map<String,RootNode> rootNodes = new HashMap<>();
	
	public FunctionGraphPlayer(ConfigFunctionGraphPlayer playerCfg, String target, int period) throws JzrInitializationException{
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
		updateGraph(dump);
			
		phantomGraph();
			
		cleanGraph();
	}

	@Override
	protected void phantomGraph() {
		logger.debug("Phantomazing the graph");
		for (Entry<String, RootNode> entry : this.rootNodes.entrySet()){
			RootNode root = entry.getValue();
			root.phantom();
		}		
	}

	private void cleanGraph() {
		logger.debug("Cleaning the graph");
		
		List<String> rootIdsToRemove = new ArrayList<>();
		for (Entry<String, RootNode> entry : this.rootNodes.entrySet()){
			RootNode root = entry.getValue();
			if (root.removeOutDated(((ConfigFunctionGraphRendering)graphCtx.getDynamicGraphCfg()).getNodeMaxAge()))
				rootIdsToRemove.add(entry.getKey());
			root.growOlder();
		}
		
		for (String id : rootIdsToRemove){
			this.rootNodes.remove(id);
		}
		
		if (rootNodes.isEmpty()){
			displayNoActivity();
		}
	}
	
	private void updateGraph(ThreadDump dump) {
		logger.debug("Updating the graph");
		
		if (!dump.getWorkingThreads().isEmpty())
			removeNoActivity();
		
		if (noThreadDump)
			removeNoThreadDump();

		if (restart)
			removeRestart();
		
		for (ThreadStack stack : dump.getWorkingThreads()){
			RootNode root = findRootNode(stack);
			
			Deque<Tag> displayDeque = buildDiplayDeque(stack);
			
			root.updateCPU(stack.getCpuInfo());
			
			root.update(displayDeque);
		}
	}

	private Deque<Tag> buildDiplayDeque(ThreadStack stack) {
		Deque<Tag> displayDeque = new ArrayDeque<Tag>();
		
		SortedMap<Integer, String> functionTags = stack.getSourceLocalizedFunctionTags();
		SortedMap<Integer, String> operationTags = stack.getSourceLocalizedOperationTags();
		
		// Add functions and operations. Functions get the priority over operations
		for (int i=0; i<stack.getStackHandler().getCodeLines().size(); i++){
			String function = functionTags.get(i);
			if (function != null)
				displayDeque.addLast(new FunctionTag(function));

			String operation = operationTags.get(i);
			if (operation != null)
				displayDeque.addLast(new OperationTag(operation));
		}
		
		// get state
		if (stack.isInDeadlock())
			displayDeque.addLast(new ThreadStateTag(ThreadStateTag.THREAD_STATE.DEADLOCK));
		else if (stack.isBlocked() || stack.isCodeLocked())
			displayDeque.addLast(new ThreadStateTag(ThreadStateTag.THREAD_STATE.LOCKED));
		else if (stack.isWaiting())
			displayDeque.addLast(new ThreadStateTag(ThreadStateTag.THREAD_STATE.WAITING));
		else if (stack.isTimedWaiting())
			displayDeque.addLast(new ThreadStateTag(ThreadStateTag.THREAD_STATE.TIMED_WAITING));
		else 
			displayDeque.addLast(new ThreadStateTag(ThreadStateTag.THREAD_STATE.RUNNING));
		
		return displayDeque;
	}

	private RootNode findRootNode(ThreadStack stack) {
		String key;
		if (GENERATION_MODE.ACTION_MERGED.equals(
				((ConfigFunctionGraphRendering)this.graphCtx.getDynamicGraphCfg()).getGenerationMode())
			)
			key = stack.getExecutor() + stack.getPrincipalTag();  // per thread executor + action type
		else
			key = stack.getID(); // per stack
		
		return rootNodes.computeIfAbsent(key, x -> new RootNode(graphCtx, key, stack.getName()));
	}
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

}
