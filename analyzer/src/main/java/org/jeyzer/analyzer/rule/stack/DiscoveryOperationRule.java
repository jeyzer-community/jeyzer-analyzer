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







import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.rule.DiscoveryPatternRule;
import org.jeyzer.analyzer.rule.pattern.Pattern;

public class DiscoveryOperationRule extends DiscoveryPatternRule{

	public static final int OPERATION_RULE_DEFAULT_PRIORITY = 2;
	
	public static final int STACK_OPERATION_LIMIT = 20;	
	
	public DiscoveryOperationRule(Pattern pattern) {
		super(pattern, OPERATION_RULE_DEFAULT_PRIORITY);
	}

	@Override
	public boolean apply(ThreadDump dump) {
		return false;
	}

	@Override
	public boolean apply(ThreadStack stack) {
		List<String> lines = stack.getStackHandler().getCodeLines();
		int size = lines.size();
		
		if (stack.isOfInterest() && matchPattern(lines,0, size>STACK_OPERATION_LIMIT? STACK_OPERATION_LIMIT:lines.size())){
			stack.addOperationTag(this.pattern.getName());
			stack.addOperationTagIndexed(this.pattern.getLineMatch(), this.pattern.getName());
			return true;
		}
		
		return false;
	}
	
}
