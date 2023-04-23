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




import org.jeyzer.analyzer.config.ConfigUtil;
import org.w3c.dom.Element;

public class ConfigTopStackSheet extends ConfigProfilingSheet{
	
	public static final String TYPE = "top_stacks";
	
	public static final String DEFAULT_DESCRIPTION = "Displays all stacks grouped by apperance order.\n"
			+ "Permits to spot eventual bottlenecks.";
	
	private static final String JZRR_THRESHOLD = "threshold";
	private static final String JZRR_STATES = "states";
	private static final String JZRR_LOCKED = "locked";
	private static final String JZRR_WAITING = "waiting";
	private static final String JZRR_TIMED_WAITING = "timed_waiting";
	private static final String JZRR_RUNNING = "running";
	
	private String lockedColor;
	private String waitingColor;
	private String timedWaitingColor;
	private String runningColor;
	
	private int threshold;
	
	public ConfigTopStackSheet(Element configNode, int index){
		super(configNode, index);
		this.threshold = Integer.valueOf(ConfigUtil.getAttributeValue(configNode,JZRR_THRESHOLD));
		loadStateColors(configNode);
	}

	public int getThreshold() {
		return threshold;
	}
	
	public String getLockedColor() {
		return lockedColor;
	}

	public String getWaitingColor() {
		return waitingColor;
	}

	public String getTimedWaitingColor() {
		return timedWaitingColor;
	}

	public String getRunningColor() {
		return runningColor;
	}
	
	public boolean hasLockedColor() {
		return lockedColor != null;
	}
	
	public boolean hasWaitingColor() {
		return waitingColor != null;
	}
	
	public boolean hasTimedWaitingColor() {
		return timedWaitingColor != null;
	}
	
	public boolean hasRunningColor() {
		return runningColor != null;
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}
	
	private void loadStateColors(Element configNode) {
		Element statesNode = ConfigUtil.getFirstChildNode(configNode, JZRR_STATES);
		if(statesNode == null)
			return;
		
		this.lockedColor = loadStateColor(statesNode, JZRR_LOCKED);
		this.waitingColor = loadStateColor(statesNode, JZRR_WAITING);
		this.timedWaitingColor = loadStateColor(statesNode, JZRR_TIMED_WAITING);
		this.runningColor = loadStateColor(statesNode, JZRR_RUNNING);
	}

	private String loadStateColor(Element statesNode, String state) {
		Element stateNode = ConfigUtil.getFirstChildNode(statesNode, state);
		if(stateNode == null)
			return null;
		
		String color = ConfigUtil.getAttributeValue(stateNode,JZRR_COLOR);
		return !color.isEmpty()? color : null;
	}
}
