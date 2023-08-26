package org.jeyzer.analyzer.output.poi.rule.header;

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


import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeader;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;

public class VirtualThreadTerminatedCounterRule extends AbstractThreadCounterRule implements Header {

	public static final String RULE_NAME = "virtual_thread_terminated_counter";
	private static final String DISPLAY_NAME = "Virtual thread terminated";	
	public static final String CELL_LABEL_COMMENT = "Number of virtual threads terminated since the last recording snapshot (or recording start time)\n"
			+ "Source : count of jdk.VirtualThreadEnd events\n"
			+ "If zero, make sure that the jdk.VirtualThreadEnd events are enabled in the JFC profile configuration";
	
	public VirtualThreadTerminatedCounterRule(ConfigSheetHeader displayCfg, SheetDisplayContext context) {
		super(displayCfg, context);
	}

	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	@Override
	public String getComment() {
		return CELL_LABEL_COMMENT;
	}

	@Override
	public String getName() {
		return RULE_NAME;
	}

	@Override
	public int getThreadCount(ThreadDump dump) {
		return dump.getVirtualThreads().getTerminatedCount();
	}
}
