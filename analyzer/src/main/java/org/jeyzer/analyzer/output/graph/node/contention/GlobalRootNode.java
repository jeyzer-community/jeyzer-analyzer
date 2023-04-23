package org.jeyzer.analyzer.output.graph.node.contention;

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

import org.graphstream.graph.Edge;
import org.jeyzer.analyzer.data.tag.FunctionTag;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.graph.motion.GraphContext;
import org.jeyzer.analyzer.output.graph.node.GraphNode;

import com.google.common.collect.Multiset;

public class GlobalRootNode extends GraphNode{

	protected List<ContentionTypeNode> nodes = new ArrayList<>();
	protected List<Edge> edges = new ArrayList<>();
	
	public GlobalRootNode(final GraphContext graphCtx, String id, String name){
		super(graphCtx, id, name); // id is unique
		node.addAttribute(NODE_UI_CLASS, getDisplayType()); // force initial root display
	}

	@Override
	protected String getDisplayType() {
		return FunctionTag.DISPLAY_NAME;
	}
	
	@Override
	protected int getGraphNodeStartSize() {
		return 40;
	}
	
	@Override
	public boolean phantom() {
		for (ContentionTypeNode nodeToPhantom : nodes)
			nodeToPhantom.phantom();

		for (Edge edge : edges)
			edge.setAttribute(NODE_UI_CLASS, getDisplayType() + NODE_UI_CLASS_PHANTOM_SUFFIX);
		
		phantomGraphNode();
		return true;
	}

	public void remove() {
		for (Edge edge : this.edges)
			this.graphCtx.getGraph().removeEdge(edge.getId());
		this.edges.clear();
		
		removeGraphNode();
		
		for (ContentionTypeNode nodeToRemove : this.nodes)
			nodeToRemove.remove();
		this.nodes.clear();
	}

	public void update(Multiset<Tag> principalContentionTypes, Map<String, String> highlights) {
		int stackCount = principalContentionTypes.size();

		for (Tag tag : principalContentionTypes.elementSet()) {
			int tagCount = principalContentionTypes.count(tag);
			int percent = FormulaHelper.percentRound(tagCount, stackCount);
			
			ContentionTypeNode child = new ContentionTypeNode(
					this.graphCtx, 
					tag.getName(), 
					tag.getName(), 
					percent,
					highlights.get(tag.getName())
					);
			
			this.nodes.add(child);
			
			Edge edge = createGraphLink(this.node, child.getGraphNode(), true);
			edge.setAttribute(NODE_UI_CLASS, getDisplayType());
			this.edges.add(edge);
		}
	}
}
