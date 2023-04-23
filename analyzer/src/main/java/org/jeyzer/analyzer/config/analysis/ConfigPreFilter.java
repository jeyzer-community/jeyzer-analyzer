package org.jeyzer.analyzer.config.analysis;

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



import java.util.Arrays;


import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigPreFilter {
	
	public enum StackSizeInterestStrategy { AUTO, KNOWN, MASTER_PROFILE_STATE_DEPENDENT }
	
	public static final String JZRA_PREFILTER = "analysis_prefilter";
	public static final String JZRA_PREFILTER_STACK_SIZE_INTEREST = "stack_size_interest";
	public static final String JZRA_PREFILTER_STACK_SIZE_INTEREST_STRATEGY = "strategy";
	public static final String JZRA_PREFILTER_STACK_SIZE_INTEREST_STRATEGY_TYPE = "type";
	
	public static final String JZRA_PREFILTER_STACK_SIZE_INTEREST_KNOWN_STRATEGY = "known_size_strategy";
	public static final String JZRA_PREFILTER_STACK_SIZE_INTEREST_KNOWN_STRATEGY_MIN = "min";
	
	private StackSizeInterestStrategy strategy;
	
	private int minSize;
	private ConfigAutoStrategy autoStrategy;
	private ConfigFilterKeep filter;
	
	public ConfigPreFilter(Element stackNode) throws JzrInitializationException {
		Element prefilterNode = ConfigUtil.getFirstChildNode(stackNode, JZRA_PREFILTER);
		Element stackSizeInterestNode = ConfigUtil.getFirstChildNode(prefilterNode, JZRA_PREFILTER_STACK_SIZE_INTEREST);
		
		loadStrategy(stackSizeInterestNode);
		filter = new ConfigFilterKeep(stackSizeInterestNode);
	}
	
	private void loadStrategy(Element stackSizeInterestNode) throws JzrInitializationException {
		Element strategyNode = ConfigUtil.getFirstChildNode(stackSizeInterestNode, JZRA_PREFILTER_STACK_SIZE_INTEREST_STRATEGY);
		String value = ConfigUtil.getAttributeValue(strategyNode, JZRA_PREFILTER_STACK_SIZE_INTEREST_STRATEGY_TYPE);
		try {
			this.strategy = StackSizeInterestStrategy.valueOf(value.trim().toUpperCase());
		}catch(IllegalArgumentException ex) {
			throw new JzrInitializationException("Stack size interest strategy  : " + value + " is not recognized. "
				+ "Must be one of those values :" + Arrays.toString(StackSizeInterestStrategy.values()).toLowerCase());
		}
		
		if (StackSizeInterestStrategy.AUTO.equals(this.strategy) || StackSizeInterestStrategy.MASTER_PROFILE_STATE_DEPENDENT.equals(this.strategy))
			autoStrategy = new ConfigAutoStrategy(strategyNode);
		
		if (StackSizeInterestStrategy.KNOWN.equals(this.strategy) || StackSizeInterestStrategy.MASTER_PROFILE_STATE_DEPENDENT.equals(this.strategy))
			loadKnownStrategy(strategyNode);
	}

	private void loadKnownStrategy(Element strategyNode) throws JzrInitializationException {
		Element knownStrategyNode = ConfigUtil.getFirstChildNode(strategyNode, JZRA_PREFILTER_STACK_SIZE_INTEREST_KNOWN_STRATEGY);
		if (knownStrategyNode == null)
			throw new JzrInitializationException("Stack size interest known strategy configuration is missing. Please add it.");
		
		this.minSize = Integer.valueOf(ConfigUtil.getAttributeValue(knownStrategyNode,JZRA_PREFILTER_STACK_SIZE_INTEREST_KNOWN_STRATEGY_MIN));
		if (this.minSize < 1)
			throw new JzrInitializationException("Stack size interest known strategy has invalid value : " + this.minSize + "Must be positive.");
	}

	public boolean isProfileStateDependentStackSizeInterestStrategy() {
		return StackSizeInterestStrategy.MASTER_PROFILE_STATE_DEPENDENT.equals(this.strategy);
	}
	
	public boolean isAutoStackSizeInterestStrategy() {
		return StackSizeInterestStrategy.AUTO.equals(this.strategy);
	}
	
	public boolean isKnownStackSizeInterestStrategy() {
		return StackSizeInterestStrategy.KNOWN.equals(this.strategy);
	}
	
	public StackSizeInterestStrategy getStackSizeInterestStrategy() {
		return this.strategy;
	}
	
	public int getMinSize() {
		return this.minSize;
	}
	
	public ConfigAutoStrategy getAutoStrategyCfg() {
		return this.autoStrategy;
	}
	
	public ConfigFilterKeep getFilterKeepCfg() {
		return this.filter;
	}
	
	public class ConfigAutoStrategy {
		
		public static final String JZRA_PREFILTER_STACK_SIZE_INTEREST_AUTO_STRATEGY = "auto_size_strategy";	
		public static final String JZRA_PREFILTER_STACK_SIZE_INTEREST_AUTO_STRATEGY_SCAN_COVERAGE = "scan_coverage";
		public static final String JZRA_PREFILTER_STACK_SIZE_INTEREST_AUTO_STRATEGY_SCAN_COVERAGE_SNAPSHOT_SAMPLES = "snapshot_samples";
		public static final String JZRA_PREFILTER_STACK_SIZE_INTEREST_AUTO_STRATEGY_SCAN_COVERAGE_SNAPSHOT_SAMPLE_PERCENTAGE = "snapshot_sample_percentage";
		public static final String JZRA_PREFILTER_STACK_SIZE_INTEREST_AUTO_STRATEGY_SCAN_COVERAGE_PERCENTAGE = "percentage";
		public static final String JZRA_PREFILTER_STACK_SIZE_INTEREST_AUTO_STRATEGY_SCAN_COVERAGE_DEFAULT_SIZE = "default_size";
		public static final String JZRA_PREFILTER_STACK_SIZE_INTEREST_AUTO_STRATEGY_SCAN_COVERAGE_STOP_IF_ACTIVE_FOUND = "stop_if_active_found";
		
		private static final int COVERAGE_SNAPSHOT_SAMPLE_PERCENTAGE_DEFAULT_VALUE = 5;
		
		private boolean stopIfActiveFound;
		private int samplesLimit;
		private int percentage;
		private int representativeSamplePercentage;
		private int defaultSize;
		
		public ConfigAutoStrategy(Element strategyNode) throws JzrInitializationException {
			Element autoStrategyNode = ConfigUtil.getFirstChildNode(strategyNode, JZRA_PREFILTER_STACK_SIZE_INTEREST_AUTO_STRATEGY);
			if (autoStrategyNode == null)
				throw new JzrInitializationException("Stack size interest auto strategy configuration is missing. Please add it.");
			
			Element scanCoverageNode = ConfigUtil.getFirstChildNode(autoStrategyNode, JZRA_PREFILTER_STACK_SIZE_INTEREST_AUTO_STRATEGY_SCAN_COVERAGE);
			
			this.samplesLimit = Integer.valueOf(ConfigUtil.getAttributeValue(scanCoverageNode, JZRA_PREFILTER_STACK_SIZE_INTEREST_AUTO_STRATEGY_SCAN_COVERAGE_SNAPSHOT_SAMPLES));
			if (this.samplesLimit < 5 || this.samplesLimit >100)
				throw new JzrInitializationException("Stack size interest auto strategy has invalid snapshot samples value : " + this.samplesLimit + ". Allowed value range is between 5 and 100.");
			
			this.percentage = Integer.valueOf(ConfigUtil.getAttributeValue(scanCoverageNode, JZRA_PREFILTER_STACK_SIZE_INTEREST_AUTO_STRATEGY_SCAN_COVERAGE_PERCENTAGE));
			if (this.percentage < 1 || this.percentage >100)
				throw new JzrInitializationException("Stack size interest auto strategy has invalid percentage value : " + this.percentage + ". Allowed value range is between 1 and 100.");
			
			String value = ConfigUtil.getAttributeValue(scanCoverageNode, JZRA_PREFILTER_STACK_SIZE_INTEREST_AUTO_STRATEGY_SCAN_COVERAGE_SNAPSHOT_SAMPLE_PERCENTAGE);
			if (value != null && !value.isEmpty()) {
				this.representativeSamplePercentage = Integer.valueOf(value);
				if (this.representativeSamplePercentage < 1 || this.representativeSamplePercentage >100)
					throw new JzrInitializationException("Stack size interest auto strategy has invalid representative percentage value : " + this.representativeSamplePercentage + ". Allowed value range is between 1 and 100.");
			}
			else {
				this.representativeSamplePercentage = COVERAGE_SNAPSHOT_SAMPLE_PERCENTAGE_DEFAULT_VALUE; // historical value. Quite severe	
			}
			
			
			this.defaultSize = Integer.valueOf(ConfigUtil.getAttributeValue(scanCoverageNode, JZRA_PREFILTER_STACK_SIZE_INTEREST_AUTO_STRATEGY_SCAN_COVERAGE_DEFAULT_SIZE));
			if (this.defaultSize < 1)
				throw new JzrInitializationException("Stack size interest auto strategy has invalid default size value : " + this.percentage + ". Default size must be positive value.");
			
			this.stopIfActiveFound = Boolean.parseBoolean(ConfigUtil.getAttributeValue(scanCoverageNode, JZRA_PREFILTER_STACK_SIZE_INTEREST_AUTO_STRATEGY_SCAN_COVERAGE_STOP_IF_ACTIVE_FOUND));
		}

		public boolean isStopIfActiveFound() {
			return stopIfActiveFound;
		}

		public int getSamplesLimit() {
			return samplesLimit;
		}
		
		public int getRepresentativeSamplePercentage() {
			return representativeSamplePercentage;
		}

		public int getPercentage() {
			return percentage;
		}
		
		public int getDefaultSize() {
			return defaultSize;
		}
	}
	
	public class ConfigFilterKeep {

		public static final String JZRA_PREFILTER_STACK_SIZE_INTEREST_KEEP_STACKS = "keep_stacks";
		public static final String JZRA_PREFILTER_STACK_SIZE_INTEREST_KEEP_STACKS_RUNNING = "running";
		public static final String JZRA_PREFILTER_STACK_SIZE_INTEREST_KEEP_LOCKED = "locked";

		private boolean keepRunning;
		private boolean keepLocked;
		
		public ConfigFilterKeep(Element stackSizeInterestNode) {
			Element keepStacksNode = ConfigUtil.getFirstChildNode(stackSizeInterestNode, JZRA_PREFILTER_STACK_SIZE_INTEREST_KEEP_STACKS);
			
			this.keepRunning = Boolean.parseBoolean(ConfigUtil.getAttributeValue(keepStacksNode, JZRA_PREFILTER_STACK_SIZE_INTEREST_KEEP_STACKS_RUNNING));
			this.keepLocked = Boolean.parseBoolean(ConfigUtil.getAttributeValue(keepStacksNode, JZRA_PREFILTER_STACK_SIZE_INTEREST_KEEP_LOCKED));
		}

		public boolean isRunningKept() {
			return keepRunning;
		}
		
		public boolean isLockedKept() {
			return keepLocked;
		}
	}
}
