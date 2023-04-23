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




import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.service.location.JzrLocationResolver;
import org.w3c.dom.Element;

public class ConfigMonitoringSequenceSheet extends ConfigSequenceSheet {

	public static final String DEFAULT_DESCRIPTION = "Displays monitoring events in a time line manner along with process info (thread count, CPU..).\n"
			+ "Each configured monitoring sequence sheet permits to focus on specific areas.";

	public static final String TYPE = "monitoring_sequence";
	
	private ConfigMonitoringSheet monitoringCfg;
	
	public ConfigMonitoringSequenceSheet(Element configNode, int index, JzrLocationResolver resolver) throws JzrInitializationException {
		super(configNode, index);
		this.monitoringCfg = new ConfigMonitoringSheet(configNode, index, resolver); 
	}

	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}

	public ConfigMonitoringSheet getMonitoringConfig() {
		return monitoringCfg;
	}
	
}
