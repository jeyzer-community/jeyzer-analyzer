package org.jeyzer.analyzer.setup;

import org.jeyzer.analyzer.config.analysis.ConfigStackSorting.StackSortingKey;

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




import org.jeyzer.analyzer.config.report.security.ConfigSecurity;
import org.jeyzer.analyzer.config.setup.ConfigSetupManager;

public class JzrSetupManager {

	private MemoryPoolSetupManager memPoolSetupMgr;
	private GarbageCollectorSetupManager gcSetupMgr;
	private GraphSetupManager graphSetupMgr;
	private CPURunnableContentionTypesManager cpuRunnableContentionTypesManager;
	private RepositorySetupManager repositorySetupManager;
	private MonitorSetupManager monitorSetupManager;
	
	private final ConfigSecurity reportSecurity;
	
	private String logoFilePath;
	private String iconFilePath;
	private String theme;
	
	private boolean chartSoftLineType;
	private String chartDotStyle; // can be null
	private boolean columnGroupingEnabled;
	private boolean actionLinkEnabled;
	private boolean headerDateLinkEnabled;
	private boolean headerHiatusOrRestartLinkEnabled;
	private boolean headerActionLinkEnabled;
	private boolean headerActionHighlightEnabled;
	
	private int headerUnfreezePaneThreshold;
	private int rowHeaderUnfreezePaneThreshold;
	private int optimizeStacksThreshold;
	
	private StackSortingKey stackSortingKey;
	
	public JzrSetupManager(ConfigSetupManager tdSetupCfg){
		memPoolSetupMgr = new MemoryPoolSetupManager(tdSetupCfg.getMemoryPoolSetupConfig());
		gcSetupMgr = new GarbageCollectorSetupManager(tdSetupCfg.getGarbageCollectorSetupConfig());
		graphSetupMgr =  new GraphSetupManager(tdSetupCfg.getGraphSetupCfg());
		cpuRunnableContentionTypesManager = new CPURunnableContentionTypesManager(tdSetupCfg.getConfigCPURunnableContentionTypesSetup()); 
		repositorySetupManager = new RepositorySetupManager(tdSetupCfg.getRepositorySetupConfig());
		monitorSetupManager = new MonitorSetupManager(tdSetupCfg.getMonitorSetupConfig());
		
		reportSecurity = tdSetupCfg.getReportSecurity();
		
		logoFilePath = tdSetupCfg.getLogoFilePath();
		iconFilePath = tdSetupCfg.getIconFilePath();
		theme = tdSetupCfg.getDefaultTheme();
		chartSoftLineType = tdSetupCfg.isGraphSoftLine();
		chartDotStyle = tdSetupCfg.getGraphDotStyle();
		columnGroupingEnabled = tdSetupCfg.isColumnGroupingEnabled();
		headerUnfreezePaneThreshold = tdSetupCfg.getHeaderUnfreezePaneThreshold();
		rowHeaderUnfreezePaneThreshold = tdSetupCfg.getRowHeaderUnfreezePaneThreshold();
		actionLinkEnabled = tdSetupCfg.isActionLinkEnabled();
		headerDateLinkEnabled = tdSetupCfg.isHeaderDateLinkEnabled();
		headerHiatusOrRestartLinkEnabled = tdSetupCfg.isHeaderHiatusOrRestartLinkEnabled();
		headerActionLinkEnabled = tdSetupCfg.isHeaderActionLinkEnabled();
		headerActionHighlightEnabled = tdSetupCfg.isHeaderActionHighlightEnabled();
		optimizeStacksThreshold = tdSetupCfg.getReportOptimizeStacksThreshold();
		stackSortingKey = tdSetupCfg.getDefaultStackSortingKey();
	}

	public MemoryPoolSetupManager getMemoryPoolSetupManager() {
		return memPoolSetupMgr;
	}

	public GarbageCollectorSetupManager getGarbageCollectorSetupManager() {
		return gcSetupMgr;
	}
	
	public GraphSetupManager getGraphSetupMgr() {
		return graphSetupMgr;
	}
	
	public CPURunnableContentionTypesManager getCPURunnableContentionTypesManager(){
		return cpuRunnableContentionTypesManager;
	}

	public RepositorySetupManager getRepositorySetupManager() {
		return repositorySetupManager;
	}

	public MonitorSetupManager getMonitorSetupManager() {
		return monitorSetupManager;
	}

	public ConfigSecurity getReportSecurity() {
		return reportSecurity;
	}

	public String getLogoFilePath(){
		return this.logoFilePath;
	}
	
	public String getIconFilePath(){
		return this.iconFilePath;
	}
	
	public String getDefaultTheme(){
		return this.theme;
	}
	
	public boolean isGraphSoftLine(){
		return chartSoftLineType;
	}

	public String getGraphDotStyle() {
		return chartDotStyle;
	}
	
	public int getHeaderUnfreezePaneThreshold() {
		return headerUnfreezePaneThreshold;
	}

	public int getRowHeaderUnfreezePaneThreshold() {
		return rowHeaderUnfreezePaneThreshold;
	}
	
	public int getReportOptimizeStacksThreshold(){
		return optimizeStacksThreshold;
	}
	
	public boolean isColumnGroupingEbabled(){
		return columnGroupingEnabled;
	}
	
	public boolean isActionLinkEnabled(){
		return actionLinkEnabled;
	}
	
	public boolean isHeaderDateLinkEnabled(){
		return headerDateLinkEnabled;
	}
	
	public boolean isHiatusOrRestartLinkEnabled(){
		return headerHiatusOrRestartLinkEnabled;
	}
	
	public boolean isHeaderActionLinkEnabled(){
		return headerActionLinkEnabled;
	}
	
	public boolean isHeaderActionHighlightEnabled(){
		return headerActionHighlightEnabled;
	}
	
	public StackSortingKey getDefaultStackSortingKey() {
		return stackSortingKey;
	}
}
