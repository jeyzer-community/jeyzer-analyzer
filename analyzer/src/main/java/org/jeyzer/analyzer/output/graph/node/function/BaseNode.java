package org.jeyzer.analyzer.output.graph.node.function;

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
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.graphstream.graph.Edge;
import org.graphstream.ui.spriteManager.Sprite;
import org.jeyzer.analyzer.config.graph.ConfigFunctionGraphRendering;
import org.jeyzer.analyzer.data.tag.FunctionTag;
import org.jeyzer.analyzer.data.tag.OperationTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.data.tag.ThreadStateTag;
import org.jeyzer.analyzer.output.graph.motion.GraphContext;
import org.jeyzer.analyzer.output.graph.node.GraphNode;

public abstract class BaseNode extends GraphNode{
	
	protected static final String NODE_UI_CLASS_VISIBLE = "Visible";
	
	protected static final int ACTIVITY_LIMIT = 40;
	
	protected static AtomicInteger idCount = new AtomicInteger(0);
	
	protected int inactivityAge = 0;
	protected int activity = 0;
	
	protected Map<String, BaseNode> nodes = new HashMap<>();
	
	public BaseNode(final GraphContext graphCtx, String id, String name){
		super(graphCtx, id, name);
	}

	public void growOlder(){
		for (BaseNode nodeToGrow : nodes.values())
			nodeToGrow.growOlder();

		this.inactivityAge++;
	}
	
	public boolean removeOutDated(int limit) {
		if (this.inactivityAge > limit){
			remove();
			return true;
		}

		List<String> nodesToRemove = new ArrayList<>();
		for (BaseNode nodeCandidate : nodes.values())
			if (nodeCandidate.removeOutDated(limit))
				nodesToRemove.add(nodeCandidate.getId());
		
		for (String key : nodesToRemove)
			nodes.remove(key);
		
		return false;
	}
	
	private void remove() {
		removeLinks();
		removeGraphNode();
		
		for (BaseNode nodeToRemove : nodes.values())
			nodeToRemove.remove();
		nodes.clear();
	}
	
	private void removeLinks() {
		for (BaseNode child : nodes.values()){
			String edgeId = getLinkId(getGraphNode(), child.getGraphNode());
			this.graphCtx.getGraph().removeEdge(edgeId);
		}
	}

	public void update(Deque<Tag> displayDeque){
		if (this.inactivityAge > 0){
			// back to life, reset 
			this.inactivityAge = 0;
			this.node.setAttribute(NODE_UI_CLASS, getDisplayType());
		}
		activity++;
		
		// process possible child
		Tag tag = displayDeque.pollFirst();
		if (tag == null)
			return;
		
		BaseNode child = this.nodes.get(tag.getName());
		if (child == null){
			if (((ConfigFunctionGraphRendering)this.graphCtx.getDynamicGraphCfg()).getNodeMaxAge() == 0){
				// optimization. Remove previous before displaying the new ones
				for (BaseNode node : nodes.values()){
					node.remove();
				}
				nodes.clear(); // there should be only one node to be removed
			}
		
			child = createChildNode(tag);
			if (child == null)
				return; // should not happen
			
			this.nodes.put(tag.getName(), child);
			Edge edge = createGraphLink(this.node, child.getGraphNode(), false);
			child.displayChildNode(edge);
		}
		
		child.update(displayDeque);
		
		updateGraphNode();
	}
	
	private void displayChildNode(Edge edge) {
		edge.addAttribute(NODE_UI_CLASS, NODE_UI_CLASS_VISIBLE);
		node.addAttribute(NODE_UI_CLASS, getDisplayType());
	}

	@Override
	public boolean phantom() {
		for (BaseNode nodeToPhantom : nodes.values())
			nodeToPhantom.phantom();
				
		if (inactivityAge == 1){
			phantomGraphNode();
			return true;
		}
		
		return false;
	}	

	private void updateGraphNode() {
		// always reset style
		node.setAttribute(NODE_UI_CLASS, getDisplayType());
		if (this.activity >= ACTIVITY_LIMIT){
			node.setAttribute(NODE_UI_SIZE, ACTIVITY_LIMIT * PIXELS_UNIT); // limit the size
		}
		else{
			node.setAttribute(NODE_UI_SIZE, PIXELS_UNIT * this.activity);
		}
		updateSprite();
	}

	private BaseNode createChildNode(Tag tag) {
		if (tag instanceof FunctionTag)
			return new FunctionNode(this.graphCtx, tag.getName() + (idCount.incrementAndGet()), tag.getName());
		else if (tag instanceof OperationTag)
			return new OperationNode(this.graphCtx, tag.getName() + (idCount.incrementAndGet()), tag.getName());
		else if (tag instanceof ThreadStateTag)
			return new ThreadStateNode(this.graphCtx, tag.getName() + (idCount.incrementAndGet()), tag.getName());
		else 
			return null;
	}
	
	protected void plotSpriteValue(int value){
		if (isNodeValueDisplayable(value)){
			String spriteName = this.node.getId().replace('.', '-');
			Sprite s = this.graphCtx.getSpriteManager().getSprite(spriteName);
			if (s == null){
				s = this.graphCtx.getSpriteManager().addSprite(spriteName);
				s.attachToNode(this.node.getId());
				s.addAttribute(NODE_UI_CLASS, NODE_UI_CLASS_COUNT);
			}
			s.setAttribute(ELEMENT_UI_LABEL, value);
		}
	}
	
	protected abstract void updateSprite();
}

