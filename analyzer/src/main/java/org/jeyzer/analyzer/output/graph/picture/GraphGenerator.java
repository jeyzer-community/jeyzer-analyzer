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







import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Quality;
import org.graphstream.stream.file.FileSinkImages.RendererType;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.jeyzer.analyzer.config.graph.ConfigGraphExtend;
import org.jeyzer.analyzer.config.report.ConfigGraph;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.setup.GraphSetupManager;
import org.jeyzer.analyzer.util.SystemHelper;
import org.slf4j.Logger;

public abstract class GraphGenerator {

	private static final Random rootIdGenerator = new Random();
	
	protected static final String NODE_ELEMENT_COUNT = "element.count";
	protected static final String NODE_UI_SIZE = "ui.size";
	protected static final String NODE_UI_CLASS = "ui.class";
	protected static final String NODE_UI_STYLE = "ui.style";
	protected static final String ELEMENT_UI_LABEL = "ui.label";
	
	protected static final String NODE_UI_CLASS_FUNCTION = "Function";
	protected static final String NODE_UI_CLASS_EXECUTOR = "Executor";
	protected static final String NODE_UI_CLASS_COUNT = "Count";
	
	protected static final String NODE_SIZE_MODE = "size-mode";
	protected static final String NODE_DYN_SIZE = "dyn-size";
	protected static final String NODE_FILL_COLOR = "fill-color: ";
	
	protected static final String IMAGE_EXT = ".jpg";
	
	protected static final int ELEMENT_COUNT_LIMIT = 80;
	protected static final int PIXELS_UNIT = 5;
	
	static {
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		if (SystemHelper.isWindows())
			System.setProperty("sun.java2d.directx", "True ");
		else
			System.setProperty("sun.java2d.opengl", "True ");
	}
	
	protected String outputDir;
	protected ConfigGraph configGraph;
	protected int configGraphHashCode;
	private int nodeLeafId = 0; // Used to make leaf id unique
	protected Map<String, ExcelGraphPicture> graphPictureRepository;
	
	public GraphGenerator(GraphSetupManager graphSetupManager, ConfigGraph configGraph, String sheetName, Map<String, ExcelGraphPicture> graphPictureRepository) throws JzrException{
		this.outputDir = graphSetupManager.getOutputRootDirectory() + "/" + sheetName;
		this.configGraph = graphSetupManager.prepareConfigGraph(configGraph);
		this.configGraphHashCode = this.configGraph.hashCode(); 
		this.graphPictureRepository = graphPictureRepository;
		SystemHelper.createDirectory(this.outputDir);
	}

	protected Graph createGraph(String name) {
		Graph graph = new SingleGraph(name);

		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
		graph.addAttribute("ui.stylesheet", this.configGraph.getStyleSheetUrl());
		
		return graph;
	}
	
	protected Node createExecutorRootNode(Graph graph, String name) {
		return createRootNode(graph, name, NODE_UI_CLASS_EXECUTOR);
	}
	
	protected Node createFunctionRootNode(Graph graph, String name) {
		return createRootNode(graph, name, NODE_UI_CLASS_FUNCTION);
	}	
	
	protected ExcelGraphPicture generateImage(Graph graph, String name, int id) {
		FileSinkImages fsi = new FileSinkImages(
				OutputType.JPG, 
				new FileSinkImages.CustomResolution(
						this.configGraph.getPictureResolution().getWidth(),
						this.configGraph.getPictureResolution().getHeight()
						)
				);
        fsi.setLayoutPolicy(LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
        fsi.setStyleSheet(this.configGraph.getStyleSheetUrl());
        fsi.setAutofit(true);
        fsi.setRenderer(RendererType.SCALA);
        fsi.setQuality(Quality.HIGH);

        ConfigGraphExtend extend = this.configGraph.getGraphExtend();
        fsi.setFramePercentBottom(extend.getBottom());
        fsi.setFramePercentLeft(extend.getLeft());
        fsi.setFramePercentRight(extend.getRight());
        fsi.setFramePercentTop(extend.getTop());
        
        String filePath = this.outputDir + "/" + id + "-" + name + IMAGE_EXT;
        
        try {
			fsi.writeAll(graph, filePath);
		} catch (IOException e) {
			getLogger().error("Failed to write graph picture : " + filePath, e);
			return null;
		}
        
        return new ExcelGraphPicture(filePath, this.configGraph.getExcelResolution());
	}
	
	protected Node addChildNode(Graph graph, SpriteManager sman, Node lastNode, String typeName, String name, int stackCount) {
		if (this.configGraph.getMode() == ConfigGraph.GENERATION_MODE.GRAPH)
			return addGraphChildNode(graph, sman, lastNode, typeName, name, stackCount);
		else if (this.configGraph.getMode() == ConfigGraph.GENERATION_MODE.RADIAL_MERGE){
			// Always attach it to the root node and return it
			addGraphChildNode(graph, sman, lastNode, typeName, name, stackCount);
			return lastNode;
		}		
		else if (this.configGraph.getMode() == ConfigGraph.GENERATION_MODE.TREE)
			return addTreeChildNode(graph, sman, lastNode, typeName, name, stackCount);
		else if (this.configGraph.getMode() == ConfigGraph.GENERATION_MODE.TREE_MERGE)
			return addMergeTreeChildNode(graph, sman, lastNode, typeName, name, stackCount);
		else if (this.configGraph.getMode() == ConfigGraph.GENERATION_MODE.RADIAL){
			// Always attach it to the root node and return it
			addTreeChildNode(graph, sman, lastNode, typeName, name, stackCount);
			return lastNode;
		}
		else
			return null;
	}
	
	private Node createRootNode(Graph graph, String name, String className) {
		// random name to avoid conflicts with children
		Node root = graph.addNode(rootIdGenerator.nextDouble() + name);
		
		root.addAttribute(NODE_SIZE_MODE, NODE_DYN_SIZE);
		root.setAttribute(NODE_UI_SIZE, 30);
		root.addAttribute(ELEMENT_UI_LABEL, name);
		root.addAttribute(NODE_UI_CLASS, className);

		return root;
	}
	
	private Node addMergeTreeChildNode(Graph graph, SpriteManager sman, Node parent, String typeName, String name, int stackCount) { 
		Iterator<Node> iter = parent.getNeighborNodeIterator();
	
		while(iter.hasNext()){
			Node child = iter.next();
			if (child.getAttribute(ELEMENT_UI_LABEL).equals(name) && parent.hasEdgeToward(child))
				return mergeChildNode(graph, sman, child, stackCount);
		}
		
		return addTreeChildNode(graph, sman, parent, typeName, name, stackCount);
	}

	private Node mergeChildNode(Graph graph, SpriteManager sman, Node child, int elementCount) {

		// Enrich existing node
		int sizePixels = child.getAttribute(NODE_UI_SIZE);
		int size = child.getAttribute(NODE_ELEMENT_COUNT);
		int newSize = size + elementCount;
		child.setAttribute(NODE_ELEMENT_COUNT, newSize);
		if (newSize >= ELEMENT_COUNT_LIMIT) 
			child.setAttribute(NODE_UI_SIZE, ELEMENT_COUNT_LIMIT * PIXELS_UNIT);
		else
			child.setAttribute(NODE_UI_SIZE, sizePixels + elementCount * PIXELS_UNIT);
		
		String name = child.getId();
		if (isNodeValueDisplayable(newSize)){
			String spriteName = name.replace('.', '-');
			Sprite s = sman.getSprite(spriteName);
			if (s == null){
				s = sman.addSprite(spriteName);
				s.attachToNode(name);
				s.addAttribute(NODE_UI_CLASS, NODE_UI_CLASS_COUNT);
			}
			s.setAttribute(ELEMENT_UI_LABEL, newSize);
		}
		
		return child;
	}

	protected Node addGraphChildNode(Graph graph, SpriteManager sman, Node parent, String typeName, String name, int elementCount, boolean percent) {
		Node child = graph.getNode(name);
		
		if (child == null){
			// Make the node
			child = graph.addNode(name);
			child.addAttribute(NODE_SIZE_MODE, NODE_DYN_SIZE);
			child.addAttribute(NODE_ELEMENT_COUNT, elementCount);
			if (elementCount >= ELEMENT_COUNT_LIMIT){
				child.addAttribute(NODE_UI_SIZE, ELEMENT_COUNT_LIMIT * PIXELS_UNIT); // limit the size
			}
			else{
				child.addAttribute(NODE_UI_SIZE, PIXELS_UNIT * elementCount);
			}
			
			if (isNodeValueDisplayable(elementCount)){
				// set now a sprite
				Sprite s = sman.addSprite(name.replace('.', '-'));
				if (percent)
					s.addAttribute(ELEMENT_UI_LABEL, elementCount + "%");
				else
					s.addAttribute(ELEMENT_UI_LABEL, elementCount);
				s.attachToNode(name);
				s.addAttribute(NODE_UI_CLASS, NODE_UI_CLASS_COUNT);
			}
			
			child.addAttribute(NODE_UI_CLASS, typeName);
			child.addAttribute(ELEMENT_UI_LABEL, name);
		}else{
			// Enrich existing node
			int sizePixels = child.getAttribute(NODE_UI_SIZE);
			int size = child.getAttribute(NODE_ELEMENT_COUNT);
			int newSize = size + elementCount;
			child.setAttribute(NODE_ELEMENT_COUNT, newSize);
			if (newSize >= ELEMENT_COUNT_LIMIT) 
				child.setAttribute(NODE_UI_SIZE, ELEMENT_COUNT_LIMIT * PIXELS_UNIT);
			else
				child.setAttribute(NODE_UI_SIZE, sizePixels + elementCount * PIXELS_UNIT);
			
			if (isNodeValueDisplayable(newSize)){
				String spriteName = name.replace('.', '-');
				Sprite s = sman.getSprite(spriteName);
				if (s == null){
					s = sman.addSprite(spriteName);
					s.attachToNode(name);
					s.addAttribute(NODE_UI_CLASS, NODE_UI_CLASS_COUNT);
				}
				if (percent)
					s.setAttribute(ELEMENT_UI_LABEL, newSize + "%");
				else
					s.setAttribute(ELEMENT_UI_LABEL, newSize);
			}
		}
		
		if (parent != null)
			createLink(graph, parent, child);
		
		return child;
	}
	
	protected Node addGraphChildNode(Graph graph, SpriteManager sman, Node parent, String typeName, String name, int stackCount) {
		return addGraphChildNode(graph, sman, parent, typeName, name, stackCount, false);
	}	

	private boolean isNodeValueDisplayable(int nodeSize) {
		// Potentially override with the node display size threshold (otherwise sprites will remain)  
		int threshold = this.configGraph.getNodeDisplaySizeThreshold() > this.configGraph.getNodeValueDisplaySizeThreshold() ?
				this.configGraph.getNodeDisplaySizeThreshold() : this.configGraph.getNodeValueDisplaySizeThreshold();
				
		return nodeSize >= threshold;
	}

	private void createLink(Graph graph, Node parent, Node child) {
		String edgeId = parent.getId() + "@" +  child.getId();
		
		// Make the link with the parent
		Edge edge = graph.getEdge(edgeId);
		if (edge ==  null){
			// graph.addEdge(edgeId, parent, child, true);
			graph.addEdge(edgeId, child, parent, true);
		}
	}

	protected void filterNodes(Graph graph, Node root) {
		int threshold = this.configGraph.getNodeDisplaySizeThreshold();
		if (threshold == ConfigGraph.NODE_DISPLAY_SIZE_THRESHOLD_DISABLED)
			return;
		
		threshold = threshold * PIXELS_UNIT;
		List<Node> nodesToRemove = new ArrayList<>();
		for (Node node : graph.getNodeSet()){
			int uiNodeSize = Integer.parseInt(node.getAttribute(NODE_UI_SIZE).toString());
			if (node != root && uiNodeSize < threshold)
				nodesToRemove.add(node);
		}
		
		for (Node node : nodesToRemove){
			graph.removeNode(node); // sprites must be removed as well but are already filtered as node_display controls the node_value_display value.
		}
	}	

	protected Node addTreeChildNode(Graph graph, SpriteManager sman, Node parent, String typeName, String name, int stackCount) {
		// graphstream node names must be unique
		String nodeName = name + "@" + nodeLeafId++;

		Node child = graph.addNode(nodeName);
		child.addAttribute(NODE_SIZE_MODE, NODE_DYN_SIZE);
		if (stackCount > ELEMENT_COUNT_LIMIT){
			child.addAttribute(NODE_UI_SIZE, ELEMENT_COUNT_LIMIT * PIXELS_UNIT); // limit the size
		}
		else{
			child.addAttribute(NODE_UI_SIZE, PIXELS_UNIT * stackCount);	
		}
		
		if (isNodeValueDisplayable(stackCount)){
			// set now a sprite
			Sprite s = sman.addSprite(nodeName.replace('.', '-'));
			s.addAttribute(ELEMENT_UI_LABEL, stackCount);
			s.attachToNode(nodeName);
			s.addAttribute(NODE_UI_CLASS, NODE_UI_CLASS_COUNT);
		}
		
		child.addAttribute(NODE_ELEMENT_COUNT, stackCount);
		child.addAttribute(NODE_UI_CLASS, typeName);
		child.addAttribute(ELEMENT_UI_LABEL, name);

		createLink(graph, parent, child);
		
		return child;
	}	
	
	public abstract Logger getLogger();
	
}
