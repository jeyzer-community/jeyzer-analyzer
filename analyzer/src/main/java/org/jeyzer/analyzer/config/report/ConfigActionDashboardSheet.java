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
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.service.location.JzrLocationResolver;
import org.w3c.dom.Element;

public class ConfigActionDashboardSheet extends ConfigProfilingSheet {

	public static final String TYPE = "action_dashboard";	
	
	private static final String DEFAULT_DESCRIPTION = "Displays the main actions, including their function graph picture and details.\n"
												+ "Permits to detect bottlenecks at a glance.";
	
	private static final String JZRR_INCLUDE_ATBI = "include_atbi";
	
	private boolean includeAtbi;
	private ConfigMonitoringSheet monitoringCfg;
	
	public ConfigActionDashboardSheet(Element configNode, int index, JzrLocationResolver resolver) throws JzrInitializationException {
		super(configNode, index);
		this.includeAtbi = Boolean.parseBoolean(ConfigUtil.getAttributeValue(configNode,JZRR_INCLUDE_ATBI));
		this.monitoringCfg = new ConfigMonitoringSheet(configNode, index, resolver);
	}

	public boolean isAtbiIncluded() {
		return includeAtbi;
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}
	
	public ConfigMonitoringSheet getMonitoringConfig() {
		return monitoringCfg;
	}
	
}
