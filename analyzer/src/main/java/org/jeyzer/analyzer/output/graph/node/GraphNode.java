package org.jeyzer.analyzer.output.graph.node;

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







import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.jeyzer.analyzer.output.graph.motion.GraphContext;

public abstract class GraphNode {

	public static final String NODE_UI_CLASS = "ui.class";
	public static final String ELEMENT_UI_LABEL = "ui.label";

	protected static final String NODE_UI_CLASS_PHANTOM_SUFFIX = "Phantom";
	protected static final String NODE_UI_CLASS_COUNT = "Count";
	
	protected static final String NODE_SIZE_MODE = "size-mode";
	protected static final String NODE_DYN_SIZE = "dyn-size";
	protected static final String NODE_FILL_COLOR = "fill-color: ";
	
	protected static final String NODE_UI_SIZE = "ui.size";
	protected static final String NODE_UI_STYLE = "ui.style";
	
	protected static final String NODE_UI_CLASS_HIDDEN = "Hidden";
	
	protected static final String LINK_ID_SEPARATOR = "@";
	
	protected static final int PIXELS_UNIT = 5;
	
	protected final GraphContext graphCtx;
	
	protected Node node; // graph node
	protected String id; // unique id
	
	public GraphNode(GraphContext graphCtx, String id, String name){
		this.graphCtx = graphCtx;
		this.id = name; // yes, name is unique under that node.
		this.node = createGraphNode(id, name); // id must be unique within the graph
	}

	public String getId(){
		return this.id;
	}
	
	public Node getGraphNode(){
		return this.node;
	}

	public abstract boolean phantom();
	
	protected abstract String getDisplayType();
	
	protected abstract int getGraphNodeStartSize();

	protected Node createGraphNode(String id, String name) {
		Node newNode = graphCtx.getGraph().addNode(id);
		newNode.addAttribute(NODE_SIZE_MODE, NODE_DYN_SIZE);
		newNode.setAttribute(NODE_UI_SIZE, getGraphNodeStartSize());
		newNode.addAttribute(NODE_UI_CLASS, NODE_UI_CLASS_HIDDEN);
		newNode.addAttribute(ELEMENT_UI_LABEL, name);
		return newNode;
	}
	
	protected Edge createGraphLink(Node parent, Node child, boolean invert) {
		String edgeId = getLinkId(parent, child);
		
		// Make the link with the parent
		Edge edge = graphCtx.getGraph().getEdge(edgeId);
		if (edge ==  null){
			if (invert)
				edge = graphCtx.getGraph().addEdge(edgeId, child, parent, true);
			else
				edge = graphCtx.getGraph().addEdge(edgeId, parent, child, true);
			if (graphCtx.isGraphViewerEnabled()){
				try {
					Thread.sleep(80); // this makes the graph display less messy and avoid getting initial arrow display in bad random location
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
		
		return edge;
	}
	
	protected void phantomGraphNode() {
		this.node.setAttribute(NODE_UI_CLASS, getDisplayType() + NODE_UI_CLASS_PHANTOM_SUFFIX);
	}
	
	protected void removeGraphNode() {
		// remove from the graph
		String spriteName = this.node.getId().replace('.', '-');
		this.graphCtx.getGraph().removeNode(this.node);
		this.graphCtx.getSpriteManager().removeSprite(spriteName);
	}
	
	protected boolean isNodeValueDisplayable(int value) {
		int threshold = this.graphCtx.getDynamicGraphCfg().getNodeValueDisplaySizeThresold();
		if (threshold == -1)
			return false;
		return value >= threshold;
	}
	
	protected String getLinkId(Node parent, Node child) {
		return parent.getId() + LINK_ID_SEPARATOR +  child.getId();
	}	
	
}
