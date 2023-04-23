package org.jeyzer.service.action.id;

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



import java.util.concurrent.atomic.AtomicInteger;


import org.jeyzer.analyzer.data.stack.ThreadStack;

public class ActionIdAnalyzerGenerator implements ActionIdGenerator {

	private static final AtomicInteger idCounter = new AtomicInteger(1);	
	
	@Override
	public int getActionId(ThreadStack stack) {
		return idCounter.incrementAndGet();
	}
	
	@Override
	public void analysisInitClose() {
	}

}
