package org.jeyzer.analyzer.rule.pattern;

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

import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.rule.Rule;

public abstract class Pattern {
	
	protected int lineMatch = Matcher.NO_LINE_MATCH;

	protected int priority = Rule.RULE_OPTIONAL_PRIORITY_NOT_SET;

	protected String source; // profile name
	
	protected int hitCount;
	
	public Pattern(String src) {
		this.source = src;
	}

	public int getPriority() {
		return priority;
	}
	
	public int getLineMatch() {
		return lineMatch;
	}
	
	public String getSource() {
		return source;
	}
	
	public int getHitCount() {
		return hitCount;
	}
	
	public boolean hasHit() {
		return hitCount > 0;
	}

	public abstract String getName();
	
	public abstract String getDisplayName();
	
	public abstract String getType(); // optional
	
	public abstract boolean matchPattern(List<String> lines, int linesCount);

	public abstract void updatePriority(int p) throws JzrInitializationException;
	
	public abstract List<String> getPatterns();
	
	public abstract boolean isLowLevelPattern();
}
