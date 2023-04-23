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







import java.util.List;

import org.jeyzer.analyzer.rule.pattern.Pattern;
import org.jeyzer.analyzer.rule.pattern.PresetPattern;


public abstract class PresetPatternRule extends Rule {

	protected PresetPattern pattern;
	
	public PresetPatternRule(Pattern pattern, int defaultPriority){
		super(pattern.getPriority() != RULE_OPTIONAL_PRIORITY_NOT_SET ? 
				pattern.getPriority() : defaultPriority);
		this.pattern = (PresetPattern) pattern;	
	}	

	public PresetPattern getPattern() {
		return this.pattern;
	}
	
	protected boolean matchPattern(List<String> lines, int stackSize){
		return pattern.matchPattern(lines, stackSize);
	}		
	
	protected boolean matchPattern(List<String> lines, int start, int end){
		return matchPattern(lines.subList(start, end), lines.size());
	}
	
}
