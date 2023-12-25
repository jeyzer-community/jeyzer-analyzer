package org.jeyzer.analyzer.parser.virtual;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2023 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.virtual.VirtualStackBuilder;
import org.jeyzer.analyzer.error.JzrParsingException;

public abstract class VirtualDumpParser {
	
	protected static final String VIRTUAL_THREAD_CARRIER_CODE_SIGNATURE = "java.lang.VirtualThread.runContinuation";
	protected static final String VIRTUAL_THREAD_UNMOUNTED_CODE_SIGNATURE_21 = "java.lang.VirtualThread.park"; // parkNanos or park
	protected static final String VIRTUAL_THREAD_UNMOUNTED_CODE_SIGNATURE_20 = "java.lang.VirtualThread.yieldContinuation";
	protected static final String VIRTUAL_THREAD_CODE_SIGNATURE = "java.lang.VirtualThread$VThreadContinuation.lambda$new";

	protected VirtualStackBuilder vtBuilder = new VirtualStackBuilder();
	
	public abstract void parseVirtualThreadDump(File file, Date date, ThreadDump dump) throws FileNotFoundException, JzrParsingException;
	
	protected boolean isUnmountedVirtualThread(List<String> codeLines) {
		return codeLines.get(0).contains(VIRTUAL_THREAD_UNMOUNTED_CODE_SIGNATURE_21) 
				|| codeLines.get(1).contains(VIRTUAL_THREAD_UNMOUNTED_CODE_SIGNATURE_20);
	}
	
	// Helper method
	public static boolean isCarrierThread(List<String> codeLines) {
		return codeLines.get(1).contains(VIRTUAL_THREAD_CARRIER_CODE_SIGNATURE);
	}
	
}
