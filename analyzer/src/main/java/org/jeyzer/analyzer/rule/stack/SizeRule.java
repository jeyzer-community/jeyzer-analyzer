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




import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.rule.AnalysisPreFilter;
import org.jeyzer.analyzer.rule.Rule;


public class SizeRule extends Rule {

	public static final int SIZE_RULE_PRIORITY = 1003;  // highest value
	
	private AnalysisPreFilter filter;
	
	public SizeRule(AnalysisPreFilter filter){
		super(SIZE_RULE_PRIORITY);
		this.filter = filter;
	}
	
	@Override
	public boolean apply(ThreadDump dump) {
		// not supported
		return false;
	}

	@Override
	public boolean apply(ThreadStack stack){
		
		// optional filters first. Exclude internal VM threads (size of 0)
		if (filter.getConfigFilterKeep().isRunningKept() && stack.isRunning() && stack.getDepthLength()!=0)
			return false;
		
		if (filter.getConfigFilterKeep().isLockedKept() && stack.isLocked())
			return false;

		int depth = stack.getDepthLength();
		
		if (stack.isOfInterest() 
				&& !stack.isInDeadlock() 
				&& !stack.isSuspended()
				&& depth < filter.getStackMinimumSize()){
			stack.setInterest(false);
			return true;
		}
		
		return false;
	}
}
