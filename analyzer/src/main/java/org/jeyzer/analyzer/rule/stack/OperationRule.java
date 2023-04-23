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
import org.jeyzer.analyzer.rule.PresetPatternRule;
import org.jeyzer.analyzer.rule.pattern.Pattern;


public class OperationRule extends PresetPatternRule {
	
	public static final int OPERATION_RULE_DEFAULT_PRIORITY = 2;
	
	private int limit = 20;
	
	public OperationRule(Pattern pattern){
		super(pattern, OPERATION_RULE_DEFAULT_PRIORITY);
		if (this.pattern.isLowLevelPattern())
			this.limit = 2;
	}
	
	@Override
	public boolean apply(ThreadDump dump) {
		// not supported
		return false;
	}

	@Override
	public boolean apply(ThreadStack stack) {
		List<String> lines = stack.getStackHandler().getCodeLines();
		int size = lines.size();
		
		if (stack.isOfInterest() && matchPattern(lines,0, size>limit? limit:size)){
			stack.addOperationTag(this.pattern.getName());
			stack.addOperationTagIndexed(this.pattern.getLineMatch(), this.pattern.getName());
			if (this.pattern.getType() != null){
				if (stack.addContentionTypeTag(this.pattern.getType())) // if accepted
					stack.addContentionTypeTagIndexed(this.pattern.getLineMatch(), this.pattern.getType());
			}
			return true;
		}
		
		return false;
	}
}
