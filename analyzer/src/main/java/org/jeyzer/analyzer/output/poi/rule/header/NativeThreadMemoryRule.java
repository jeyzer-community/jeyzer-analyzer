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

public class NativeThreadMemoryRule extends AbstractThreadCounterRule implements Header {

	public static final String RULE_NAME = "native_thread_memory";
	private static final String DISPLAY_NAME = "Native thread memory";	
	public static final String CELL_LABEL_COMMENT = "Memory in Mb consumed by the native threads\n"
			+ "Value is approximative : native thread is known to require 2Mb."
			+ "Memory is allocated outside of the heap.";
		
	public NativeThreadMemoryRule(ConfigSheetHeader displayCfg, SheetDisplayContext context) {
		super(displayCfg, context);
	}

	@Override
	public int getThreadCount(ThreadDump dump) {
		return dump.getNativeStackSize() * 2;
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
}
