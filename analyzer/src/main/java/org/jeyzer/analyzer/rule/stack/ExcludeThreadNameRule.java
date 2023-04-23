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

public class ExcludeThreadNameRule extends PresetPatternRule {

	public static final int PATTERN_RULE_DEFAULT_PRIORITY = 1002;
	
	private long size;
	
	public ExcludeThreadNameRule(Pattern pattern){
		super(pattern, PATTERN_RULE_DEFAULT_PRIORITY);
		this.size = this.pattern.getSize();
	}
	
	@Override
	public boolean apply(ThreadDump dump) {
		// not supported
		return false;
	}

	@Override
	public boolean apply(ThreadStack stack) {
		if (stack.isOfInterest() 
				&& !stack.isInDeadlock() 
				&& !stack.isSuspended() 
				&& stack.getDepthLength()<=size) 
		{
			List<String> threadName = new ArrayList<>(1);
			threadName.add(stack.getName());
			if (matchPattern(threadName, 1)){
				stack.setInterest(false);
				return true;
			}
		}
		
		return false;
	}

}
