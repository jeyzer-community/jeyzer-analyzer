package org.jeyzer.analyzer.config.graph;

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




import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigFunctionGraphRendering extends ConfigGraphRendering{

	private static final String JZRA_MAX_AGE = "max_age";
	private static final String JZRA_EXECUTOR ="executor";
	private static final String JZRA_CPU_LOAD_THRESHOLD ="cpu_load_threshold";
	
	private static final String JZRA_MODE_TYPE_ACTION_SINGLE = "action_single";
	private static final String JZRA_MODE_TYPE_ACTION_MERGED = "action_merged";
	
	public enum GENERATION_MODE { ACTION_SINGLE, ACTION_MERGED, NOT_SET }
	
	private GENERATION_MODE mode = GENERATION_MODE.NOT_SET;
	private int nodeMaxAge = -1;
	private int executorCpuLoadThreshold = -1;
	
	public ConfigFunctionGraphRendering(Element renderingNode)
			throws JzrInitializationException {
		super(renderingNode);
		
		Element nodeNode = ConfigUtil.getFirstChildNode(renderingNode, JZRA_NODE);
		if (nodeNode != null)
			this.nodeMaxAge = Integer.valueOf(ConfigUtil.getAttributeValue(nodeNode,JZRA_MAX_AGE));
		
		Element executorNode = ConfigUtil.getFirstChildNode(renderingNode, JZRA_EXECUTOR);
		if (executorNode != null)
			this.executorCpuLoadThreshold = Integer.valueOf(ConfigUtil.getAttributeValue(executorNode,JZRA_CPU_LOAD_THRESHOLD));

		loadGenerationMode(renderingNode);
	}

	public GENERATION_MODE getGenerationMode() {
		return mode;
	}

	public int getNodeMaxAge() {
		return nodeMaxAge;
	}

	public int getExecutorCpuLoadThreshold() {
		return executorCpuLoadThreshold;
	}
	
	private void loadGenerationMode(Element renderingNode) {
		Element modeNode = ConfigUtil.getFirstChildNode(renderingNode, JZRA_MODE);
		if (modeNode != null){
			String value = ConfigUtil.getAttributeValue(modeNode, JZRA_TYPE);
			if (value == null)
				mode = GENERATION_MODE.NOT_SET;
			else if (value.equalsIgnoreCase(JZRA_MODE_TYPE_ACTION_SINGLE))
				mode = GENERATION_MODE.ACTION_SINGLE;
			else if (value.equalsIgnoreCase(JZRA_MODE_TYPE_ACTION_MERGED))
				mode = GENERATION_MODE.ACTION_MERGED;
			else
				mode = GENERATION_MODE.NOT_SET;
		}
	}	

}
