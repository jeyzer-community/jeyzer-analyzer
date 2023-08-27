package org.jeyzer.monitor.engine.rule.condition.task;

import java.util.Map;

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




import org.jeyzer.analyzer.data.stack.ThreadStack;

public interface SignalWithContextTaskProvider {

	public boolean matchSignalWithContext(ThreadStack stack, Map<String, Object> context);
	
	public boolean matchBeginSignalWithContext(ThreadStack stack, Map<String, Object> context);
	
}
 
