package org.jeyzer.analyzer.rule;

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







import java.util.Comparator;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;


public abstract class Rule {

	public static final int RULE_OPTIONAL_PRIORITY_MAX = 1000;
	public static final int RULE_OPTIONAL_PRIORITY_MIN = 10;
	public static final int RULE_OPTIONAL_PRIORITY_NOT_SET = -1;
	
	protected int priority = 0;
	
	public Rule(int priority){
		this.priority = priority;
	}
	
	public abstract boolean apply (ThreadDump dump);
	
	public abstract boolean apply (ThreadStack stack);
	
	public int getPriority(){
		return this.priority;
	}
	
	public static class RuleComparable implements Comparator<Rule>{
		 
	    @Override
	    public int compare(Rule r1, Rule r2) {
	        return r1.getPriority()>r2.getPriority() ? -1 : r1.getPriority()==r2.getPriority() ? 0 : 1;
	    }
	}	
	
}
