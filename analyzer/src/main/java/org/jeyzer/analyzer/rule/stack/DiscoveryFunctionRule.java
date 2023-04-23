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

public class DiscoveryFunctionRule extends DiscoveryPatternRule{

	public static final int FUNCTION_RULE_DEFAULT_PRIORITY = 5;
	
	public DiscoveryFunctionRule(Pattern pattern) {
		super(pattern, FUNCTION_RULE_DEFAULT_PRIORITY);
	}

	@Override
	public boolean apply(ThreadDump dump) {
		return false;
	}

	@Override
	public boolean apply(ThreadStack stack) {
		List<String> lines = stack.getStackHandler().getCodeLines();
		if (stack.isOfInterest() && matchPattern(lines, lines.size())){
			stack.addFunctionTag(this.pattern.getName());
			stack.addFunctionTagIndexed(this.pattern.getLineMatch(), this.pattern.getName());
			return true;
		}
		
		return false;
	}
	
}
