package org.jeyzer.analyzer.rule.stack;

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







import java.util.ArrayList;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.rule.PresetPatternRule;
import org.jeyzer.analyzer.rule.pattern.Pattern;

public class ExecutorThreadNameRule extends PresetPatternRule {

	public static final int EXECUTOR_RULE_DEFAULT_PRIORITY = 3;

	public ExecutorThreadNameRule(Pattern pattern){
		super(pattern, EXECUTOR_RULE_DEFAULT_PRIORITY);
	}	
	
	@Override
	public boolean apply(ThreadDump dump) {
		// not supported
		return false;
	}

	@Override
	public boolean apply(ThreadStack stack) {
		if (stack.isOfInterest()){
			List<String> threadName = new ArrayList<>(1);
			threadName.add(stack.getName());
			if (matchPattern(threadName, 1)){
				stack.setExecutor(this.pattern.getName());
				return true;
			}
		}
		
		return false;
	}

}
