package org.jeyzer.analyzer.parser;

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




import org.jeyzer.analyzer.setup.JzrSetupManager;

public class AdvancedAgentStackParser extends AdvancedJMXStackParser {

	// format name
	public static final String FORMAT_NAME = "Jeyzer Recorder - Advanced MX Agent";
	public static final String FORMAT_SHORT_NAME = "Advanced Agent";

	public static final String FIRST_LINE = "Full Agent Advanced Java thread dump";
	
	public AdvancedAgentStackParser(JzrSetupManager setupMgr) {
		super(setupMgr);
	}
	
	@Override
	public String getFormatName() {
		return FORMAT_NAME;
	}

	@Override
	public String getFormatShortName() {
		return FORMAT_SHORT_NAME;
	}
	
	@Override
	public boolean isDiskWriteTimeMeasurementUsed(){
		return true;
	}
}
