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

import java.time.Duration;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.graph.ConfigFunctionGraphPlayer;
import org.jeyzer.analyzer.config.graph.ConfigGraphPlayer;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigReplay {
	
	private static final String JZRA_ENABLED = "enabled";
	private static final String JZRA_REFRESH_PERIOD = "refresh_period";
	private static final String JZRA_OUTPUT_DIRECTORY = "output_directory";

	private boolean enabled = false;
	private Duration refreshPeriod;
	private ConfigFunctionGraphPlayer functionGraphPlayerCfg;

	public ConfigReplay(Element replayNode) throws JzrInitializationException {
		if (replayNode == null)
			return;
		
		this.enabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(replayNode, JZRA_ENABLED));
		if (!enabled)
			return;
		
		this.refreshPeriod = ConfigUtil.getAttributeDuration(replayNode, JZRA_REFRESH_PERIOD);
		
		// load dynamic graph configuration
		Element functionGraphNode = ConfigUtil.getFirstChildNode(replayNode, ConfigGraphPlayer.JZRA_FUNCTION_GRAPH_PLAYER);
		this.functionGraphPlayerCfg = new ConfigFunctionGraphPlayer(functionGraphNode, ConfigUtil.getAttributeValue(replayNode, JZRA_OUTPUT_DIRECTORY));
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public Duration getRefreshPeriod() {
		return refreshPeriod;
	}

	public ConfigFunctionGraphPlayer getFunctionGraphPlayerCfg() {
		return functionGraphPlayerCfg;
	}
}
