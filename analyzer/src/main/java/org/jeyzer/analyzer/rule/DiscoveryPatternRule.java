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

import org.jeyzer.analyzer.rule.pattern.DiscoveryPattern;
import org.jeyzer.analyzer.rule.pattern.Pattern;

public abstract class DiscoveryPatternRule extends Rule {

	protected DiscoveryPattern pattern;
	
	public DiscoveryPatternRule(Pattern pattern, int priority) {
		super(priority);
		this.pattern = (DiscoveryPattern)pattern;
	}

	protected boolean matchPattern(List<String> lines, int linesCount){
		return pattern.matchPattern(lines, linesCount);
	}		
	
	protected boolean matchPattern(List<String> lines, int start, int end){
		return matchPattern(lines.subList(start, end), lines.size());
	}	
	
}
