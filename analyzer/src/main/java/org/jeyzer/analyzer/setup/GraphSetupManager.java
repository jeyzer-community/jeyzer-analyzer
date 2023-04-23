package org.jeyzer.analyzer.setup;

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




import org.jeyzer.analyzer.config.graph.ConfigGraphExtend;
import org.jeyzer.analyzer.config.report.ConfigGraph;
import org.jeyzer.analyzer.config.setup.ConfigGraphSetup;

public class GraphSetupManager {

	private ConfigGraph defaultConfigGraph;
	
	private String outputRootDirectory;
	private boolean archivingEnabled;
	
	public GraphSetupManager(ConfigGraphSetup graphSetupCfg) {
		defaultConfigGraph = graphSetupCfg.getDefaultConfigGraph();
		outputRootDirectory = graphSetupCfg.getRootDirectory();
		archivingEnabled = graphSetupCfg.isArchivingEnabled();
	}
	
	public ConfigGraph prepareConfigGraph(ConfigGraph graphCfg){
		if (graphCfg.getGenerationMaximum() < 0)
			graphCfg.setGenerationMaximum(defaultConfigGraph.getGenerationMaximum());

		if (graphCfg.getNodeThreshold() < 0)
			graphCfg.setNodeThreshold(defaultConfigGraph.getNodeThreshold());

		if (graphCfg.getActionSizeThreshold() < 0)
			graphCfg.setActionSizeThreshold(defaultConfigGraph.getActionSizeThreshold());
		
		if (graphCfg.getStyleSheetPath() == null)
			graphCfg.setStyleSheetPath(defaultConfigGraph.getStyleSheetPath());
		
		if (graphCfg.getExcelResolution() == null)
			graphCfg.setExcelResolution(defaultConfigGraph.getExcelResolution());

		if (graphCfg.getPictureResolution() == null)
			graphCfg.setPictureResolution(defaultConfigGraph.getPictureResolution());
		
		if (graphCfg.getNodeDisplaySizeThreshold() == ConfigGraph.NODE_DISPLAY_SIZE_THRESHOLD_NOT_SET)
			graphCfg.setNodeDisplaySizeThreshold(defaultConfigGraph.getNodeDisplaySizeThreshold());

		if (graphCfg.getNodeValueDisplaySizeThreshold() == ConfigGraph.NODE_VALUE_DISPLAY_SIZE_THRESHOLD_NOT_SET)
			graphCfg.setNodeValueDisplaySizeThreshold(defaultConfigGraph.getNodeValueDisplaySizeThreshold());
		
		if (graphCfg.getMode() == ConfigGraph.GENERATION_MODE.NOT_SET)
			graphCfg.setMode(defaultConfigGraph.getMode());
		
		if (graphCfg.getMode() == ConfigGraph.GENERATION_MODE.TREE 
				|| graphCfg.getMode() == ConfigGraph.GENERATION_MODE.TREE_MERGE){
			boolean enabled = graphCfg.getThreadStateDisplayed() != null;
			if (!enabled)
				graphCfg.setThreadStateDisplayedEnabled(defaultConfigGraph.getThreadStateDisplayed());
			
			if (!enabled && defaultConfigGraph.getThreadStateDisplayed() && graphCfg.getDisplayedThreadStates() == null)
				graphCfg.setDisplayedThreadStates(defaultConfigGraph.getDisplayedThreadStates());
		}
		
		ConfigGraphExtend graphExtend = graphCfg.getGraphExtend();
		ConfigGraphExtend defaultGraphExtend = defaultConfigGraph.getGraphExtend();
		if (graphExtend.getLeft() < 0)
			graphExtend.setLeft(defaultGraphExtend.getLeft());
		
		if (graphExtend.getRight() < 0)
			graphExtend.setRight(defaultGraphExtend.getRight());
		
		if (graphExtend.getTop() < 0)
			graphExtend.setTop(defaultGraphExtend.getTop());
		
		if (graphExtend.getBottom() < 0)
			graphExtend.setBottom(defaultGraphExtend.getBottom());
		
		return graphCfg;
	}

	public String getOutputRootDirectory() {
		return outputRootDirectory;
	}

	public boolean isArchivingEnabled() {
		return archivingEnabled;
	}

}
