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


public class LockerRule extends PresetPatternRule {
	
	// JEYZ-52 : locker rule priority must be higher than the stack size rule one
	//  to permit the locked stacks to be kept in the automatic filtering (like the Blocked stacks).
	public static final int LOCKER_RULE_DEFAULT_PRIORITY = 1004;
	public static final int LINES_LIMIT = 20;

	public LockerRule(Pattern pattern){
		super(pattern, LOCKER_RULE_DEFAULT_PRIORITY);
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
		if (stack.isOfInterest() && matchPattern(lines,0, size>LINES_LIMIT? LINES_LIMIT : size)){
			stack.setCodeLocked(true);
			stack.setCodeLockName(this.pattern.getDisplayName());
			return true;
		}
		
		return false;
	}

}
