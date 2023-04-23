package org.jeyzer.analyzer.config.report;

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







import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.graph.ConfigGraphExtend;
import org.jeyzer.analyzer.config.graph.ConfigGraphResolution;
import org.jeyzer.analyzer.data.stack.ThreadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class ConfigGraph {

	private static final Logger logger = LoggerFactory.getLogger(ConfigGraph.class);
	
	public enum GENERATION_MODE { GRAPH, TREE, TREE_MERGE, RADIAL, RADIAL_MERGE, NOT_SET }
	
	public static final int NODE_VALUE_DISPLAY_SIZE_THRESHOLD_NOT_SET = -2;
	public static final int NODE_DISPLAY_SIZE_THRESHOLD_NOT_SET = -2;
	public static final int NODE_DISPLAY_SIZE_THRESHOLD_DISABLED = -1;

	public static final String JZRA_FUNCTION_GRAPH = "function_graph";
	public static final String JZRA_CONTENTION_GRAPH = "contention_graph";
	public static final String JZRA_MODULE_GRAPH = "module_graph";
	
	public static final String JZRA_GRAPH = "graph";
	public static final String JZRA_FILE = "file";
	
	private static final String JZRA_RENDERING = "rendering";
	private static final String JZRA_STYLE_SHEET = "style_sheet";
	private static final String JZRA_EXCEL_RESOLUTION = "excel_resolution";
	private static final String JZRA_NODE_DISPLAY = "node_display";
	private static final String JZRA_NODE_VALUE_DISPLAY = "node_value_display";
	private static final String JZRA_SIZE_THRESHOLD = "size_threshold";
	private static final String JZRA_MODE = "mode";
	private static final String JZRA_TYPE = "type";
	private static final String JZRA_MODE_TYPE_GRAPH = "graph";
	private static final String JZRA_MODE_TYPE_TREE = "tree";
	private static final String JZRA_MODE_TYPE_TREE_MERGE = "tree_merge";
	private static final String JZRA_MODE_TYPE_RADIAL = "radial";
	private static final String JZRA_MODE_TYPE_RADIAL_MERGE = "radial_merge";
	private static final String JZRA_THREAD_STATE = "thread_state";
	private static final String JZRA_ENABLED = "enabled";
	private static final String JZRA_STATES = "states";
	private static final String JZRA_GRAPH_AREA_EXTEND = "graph_area_extend";
	
	private static final String JZRA_CONDITIONS = "conditions";
	private static final String JZRA_GENERATION = "generation";
	private static final String JZRA_MAXIMUM = "maximum";
	private static final String JZRA_NODES = "nodes";
	private static final String JZRA_ACTION = "action_size";
	private static final String JZRA_THRESHOLD = "threshold";
	
	private static final String JZRR_ATBI = "atbi";
	private static final String JZRR_STACK_COUNT_THRESHOLD = "stack_count_threshold";
	private static final String JZRR_SECTION_SIZE_THRESHOLD = "section_size_threshold";
	
	private int nodeDisplaySizeThreshold = NODE_DISPLAY_SIZE_THRESHOLD_NOT_SET;
	private int nodeValueDisplaySizeThreshold = NODE_VALUE_DISPLAY_SIZE_THRESHOLD_NOT_SET;
	private String styleSheetPath = null;
	private ConfigGraphResolution pictureResolution = null;
	private ConfigGraphResolution excelResolution = null;
	private ConfigGraphExtend graphExtend;
	
	private int generationMaximum = -1;
	private int nodeThreshold = -1;
	private int actionSizeThreshold = -1;
	private GENERATION_MODE mode = GENERATION_MODE.NOT_SET;
	
	private List<ThreadState> displayedThreadstates = null;
	private Boolean displayThreadstatesEnabled = null;
	
	private boolean displayATBINodes = false;
	private int stackCountThreshold = 4;
	private int sectionSizeThreshold = 4;
	
	public ConfigGraph(Element configNode){
		loadRendering(configNode);
		loadConditions(configNode);
	}

	public String getStyleSheetUrl(){
		String styleSheetUrl =  null;
		try {
			styleSheetUrl = "url(" + new File(styleSheetPath).toURI().toURL() + ")";
		} catch (MalformedURLException e) {
			logger.error("Failed to create graph stream css file URI for path : " + styleSheetPath, e);
		}
		return styleSheetUrl;
	}

	public int getGenerationMaximum() {
		return generationMaximum;
	}
	
	public void setGenerationMaximum(int generationMaximum) {
		this.generationMaximum = generationMaximum;
	}

	public int getNodeThreshold() {
		return nodeThreshold;
	}
	
	public void setNodeThreshold(int nodeThreshold) {
		this.nodeThreshold = nodeThreshold;
	}

	public String getStyleSheetPath() {
		return this.styleSheetPath;
	}	
	
	public void setStyleSheetPath(String styleSheetPath) {
		this.styleSheetPath = styleSheetPath;
	}

	public int getActionSizeThreshold() {
		return this.actionSizeThreshold;
	}

	public void setActionSizeThreshold(int actionSizeThreshold) {
		this.actionSizeThreshold = actionSizeThreshold;
	}

	public ConfigGraphResolution getPictureResolution() {
		return pictureResolution;
	}

	public void setPictureResolution(ConfigGraphResolution pictureResolution) {
		this.pictureResolution = pictureResolution;
	}

	public ConfigGraphResolution getExcelResolution() {
		return excelResolution;
	}

	public void setExcelResolution(ConfigGraphResolution excelResolution) {
		this.excelResolution = excelResolution;
	}

	public int getNodeDisplaySizeThreshold() {
		return nodeDisplaySizeThreshold;
	}

	public void setNodeDisplaySizeThreshold(int nodeDisplaySizeThreshold) {
		this.nodeDisplaySizeThreshold = nodeDisplaySizeThreshold;
	}

	public int getNodeValueDisplaySizeThreshold() {
		return nodeValueDisplaySizeThreshold;
	}

	public void setNodeValueDisplaySizeThreshold(int nodeValueDisplaySizeThreshold) {
		this.nodeValueDisplaySizeThreshold = nodeValueDisplaySizeThreshold;
	}

	public GENERATION_MODE getMode() {
		return mode;
	}
	
	public void setMode(GENERATION_MODE mode) {
		this.mode = mode;
	}

	public Boolean getThreadStateDisplayed() {
		return this.displayThreadstatesEnabled;
	}
	
	public void setThreadStateDisplayedEnabled(Boolean value) {
		this.displayThreadstatesEnabled =  value;
	}
	
	public boolean isThreadStateDisplayed() {
		if (this.displayThreadstatesEnabled == null)
			return false;
		return this.displayThreadstatesEnabled;
	}
	
	public List<ThreadState> getDisplayedThreadStates() {
		return this.displayedThreadstates;
	}
	
	public void setDisplayedThreadStates(List<ThreadState> states) {
		this.displayedThreadstates = states;
	}	

	public boolean isDisplayATBINodes() {
		return displayATBINodes;
	}

	public int getStackCountThreshold() {
		return stackCountThreshold;
	}

	public int getSectionSizeThreshold() {
		return sectionSizeThreshold;
	}
	
	public ConfigGraphExtend getGraphExtend() {
		return graphExtend;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + actionSizeThreshold;
		result = prime * result + (displayATBINodes ? 1231 : 1237);
		result = prime
				* result
				+ ((displayThreadstatesEnabled == null) ? 0
						: displayThreadstatesEnabled.hashCode());
		result = prime
				* result
				+ ((displayedThreadstates == null) ? 0 : displayedThreadstates
						.hashCode());
		result = prime * result
				+ ((excelResolution == null) ? 0 : excelResolution.hashCode());
		result = prime * result + generationMaximum;
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + nodeDisplaySizeThreshold;
		result = prime * result + nodeThreshold;
		result = prime * result + nodeValueDisplaySizeThreshold;
		result = prime
				* result
				+ ((pictureResolution == null) ? 0 : pictureResolution
						.hashCode());
		result = prime * result + sectionSizeThreshold;
		result = prime * result + stackCountThreshold;
		result = prime * result
				+ ((styleSheetPath == null) ? 0 : styleSheetPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigGraph other = (ConfigGraph) obj;
		if (actionSizeThreshold != other.actionSizeThreshold)
			return false;
		if (displayATBINodes != other.displayATBINodes)
			return false;
		if (displayThreadstatesEnabled == null) {
			if (other.displayThreadstatesEnabled != null)
				return false;
		} else if (!displayThreadstatesEnabled
				.equals(other.displayThreadstatesEnabled))
			return false;
		if (displayedThreadstates == null) {
			if (other.displayedThreadstates != null)
				return false;
		} else if (!displayedThreadstates.equals(other.displayedThreadstates))
			return false;
		if (excelResolution == null) {
			if (other.excelResolution != null)
				return false;
		} else if (!excelResolution.equals(other.excelResolution))
			return false;
		if (generationMaximum != other.generationMaximum)
			return false;
		if (mode != other.mode)
			return false;
		if (nodeDisplaySizeThreshold != other.nodeDisplaySizeThreshold)
			return false;
		if (nodeThreshold != other.nodeThreshold)
			return false;
		if (nodeValueDisplaySizeThreshold != other.nodeValueDisplaySizeThreshold)
			return false;
		if (pictureResolution == null) {
			if (other.pictureResolution != null)
				return false;
		} else if (!pictureResolution.equals(other.pictureResolution))
			return false;
		if (sectionSizeThreshold != other.sectionSizeThreshold)
			return false;
		if (stackCountThreshold != other.stackCountThreshold)
			return false;
		if (styleSheetPath == null) {
			if (other.styleSheetPath != null)
				return false;
		} else if (!styleSheetPath.equals(other.styleSheetPath))
			return false;
		return true;
	}
	
	private void loadConditions(Element configNode) {
		Element conditionsNode = ConfigUtil.getFirstChildNode(configNode, JZRA_CONDITIONS);
		if (conditionsNode == null)
			return;
		
		Element generationNode = ConfigUtil.getFirstChildNode(conditionsNode, JZRA_GENERATION);
		if (generationNode != null)
			this.generationMaximum = Integer.valueOf(ConfigUtil.getAttributeValue(generationNode,JZRA_MAXIMUM));
		
		Element nodesNode = ConfigUtil.getFirstChildNode(conditionsNode, JZRA_NODES);
		if (nodesNode != null)
			this.nodeThreshold = Integer.valueOf(ConfigUtil.getAttributeValue(nodesNode,JZRA_THRESHOLD));
		
		Element actionNode = ConfigUtil.getFirstChildNode(conditionsNode, JZRA_ACTION);
		if (actionNode != null)
			this.actionSizeThreshold = Integer.valueOf(ConfigUtil.getAttributeValue(actionNode,JZRA_THRESHOLD));
	}

	private void loadRendering(Element configNode) {
		Element renderingNode = ConfigUtil.getFirstChildNode(configNode, JZRA_RENDERING);
		if (renderingNode == null)
			return;
		
		Element styleSheetNode = ConfigUtil.getFirstChildNode(renderingNode, JZRA_STYLE_SHEET);
		if (styleSheetNode != null)
			styleSheetPath = ConfigUtil.getAttributeValue(styleSheetNode, JZRA_FILE); // resolve any variable
		
		Element pictureResolutionNode = ConfigUtil.getFirstChildNode(renderingNode, ConfigGraphResolution.JZRA_RESOLUTION);
		if (pictureResolutionNode != null)
			pictureResolution = new ConfigGraphResolution(pictureResolutionNode);
		
		Element excelResolutionNode = ConfigUtil.getFirstChildNode(renderingNode, JZRA_EXCEL_RESOLUTION);
		if (excelResolutionNode != null)
			excelResolution = new ConfigGraphResolution(excelResolutionNode);
		
		Element nodeDisplayNode = ConfigUtil.getFirstChildNode(renderingNode, JZRA_NODE_DISPLAY);
		if (nodeDisplayNode != null)
			nodeDisplaySizeThreshold = Integer.valueOf(ConfigUtil.getAttributeValue(nodeDisplayNode, JZRA_SIZE_THRESHOLD));

		Element nodeValueDisplayNode = ConfigUtil.getFirstChildNode(renderingNode, JZRA_NODE_VALUE_DISPLAY);
		if (nodeValueDisplayNode != null)
			nodeValueDisplaySizeThreshold = Integer.valueOf(ConfigUtil.getAttributeValue(nodeValueDisplayNode, JZRA_SIZE_THRESHOLD));
		
		Element atbiNode = ConfigUtil.getFirstChildNode(renderingNode, JZRR_ATBI);
		if (atbiNode != null){
			displayATBINodes = true;
			stackCountThreshold = Integer.valueOf(ConfigUtil.getAttributeValue(atbiNode, JZRR_STACK_COUNT_THRESHOLD));
			sectionSizeThreshold = Integer.valueOf(ConfigUtil.getAttributeValue(atbiNode, JZRR_SECTION_SIZE_THRESHOLD));
		}
		
		graphExtend = new ConfigGraphExtend(ConfigUtil.getFirstChildNode(renderingNode, JZRA_GRAPH_AREA_EXTEND)); 
		
		loadGenerationMode(renderingNode);
		
		loadStates(renderingNode);
	}

	private void loadStates(Element renderingNode) {
		Element nodeThreadStateDisplayNode = ConfigUtil.getFirstChildNode(renderingNode, JZRA_THREAD_STATE);
		if (nodeThreadStateDisplayNode == null){
			this.displayThreadstatesEnabled = null;
			return;
		}
		
		String nodeValueDisplayEnabled = ConfigUtil.getAttributeValue(nodeThreadStateDisplayNode, JZRA_ENABLED);
		if (nodeValueDisplayEnabled.isEmpty()){
			this.displayThreadstatesEnabled = null;
			return;
		}

		this.displayThreadstatesEnabled = Boolean.parseBoolean(nodeValueDisplayEnabled);
		if (!this.displayThreadstatesEnabled){
			return;
		}		
		
		String statesList = null;
		if (nodeThreadStateDisplayNode != null)
			statesList = ConfigUtil.getAttributeValue(nodeThreadStateDisplayNode, JZRA_STATES);
		
		if (statesList.isEmpty()){
			this.displayThreadstatesEnabled = false;
			this.displayedThreadstates = null;
			return;
		}

		String[] states = statesList.split(",");
		
		for (int i=0; i<states.length; i++){
			ThreadState state = null;
			try{
				state = ThreadState.valueOf(states[i].trim());
			}catch(Exception ex){
				logger.warn("Invalid thread state : "+ states[i] + ". Thread state will be ignored in the graph configuration.");
			}
			
			if (state != null){
				if (this.displayedThreadstates == null)
					this.displayedThreadstates = new ArrayList<ThreadState>();
				this.displayedThreadstates.add(state);
			}
			
		}
	}

	private void loadGenerationMode(Element renderingNode) {
		Element modeNode = ConfigUtil.getFirstChildNode(renderingNode, JZRA_MODE);
		if (modeNode != null){
			String value = ConfigUtil.getAttributeValue(modeNode, JZRA_TYPE);
			if (value == null)
				mode = GENERATION_MODE.NOT_SET;
			else if (value.equalsIgnoreCase(JZRA_MODE_TYPE_GRAPH))
				mode = GENERATION_MODE.GRAPH;
			else if (value.equalsIgnoreCase(JZRA_MODE_TYPE_TREE))
				mode = GENERATION_MODE.TREE;
			else if (value.equalsIgnoreCase(JZRA_MODE_TYPE_TREE_MERGE))
				mode = GENERATION_MODE.TREE_MERGE;
			else if (value.equalsIgnoreCase(JZRA_MODE_TYPE_RADIAL))
				mode = GENERATION_MODE.RADIAL;
			else if (value.equalsIgnoreCase(JZRA_MODE_TYPE_RADIAL_MERGE))
				mode = GENERATION_MODE.RADIAL_MERGE;
			else
				mode = GENERATION_MODE.NOT_SET;
		}
	}
	
}
