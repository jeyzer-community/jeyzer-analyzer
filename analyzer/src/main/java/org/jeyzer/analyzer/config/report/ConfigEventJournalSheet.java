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

public class ConfigEventJournalSheet extends ConfigMonitoringSheet {

	public ConfigEventJournalSheet(Element configNode, int index, JzrLocationResolver resolver)throws JzrInitializationException {
		super(configNode, index, resolver);
	}

	public static final String TYPE = "event_journal";
	
	public static final String DEFAULT_DESCRIPTION = "Displays all the raised monitoring events, focusing on the applicative apparance time.\n"
			+ "Permits to correlate applicative events with system incidents.";
	
	@Override
	public String getDefaultDescription() {
		return DEFAULT_DESCRIPTION;
	}
}
