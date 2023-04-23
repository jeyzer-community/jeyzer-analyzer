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







import java.util.ArrayList;
import java.util.List;

import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.rule.Rule;

public class PresetPattern extends Pattern {

	protected String name;
	protected String type; // can be null
	
	private RegexMatcher regexMatcher;
	private StringMatcher stringMatcher;

	private long size = Long.MAX_VALUE;
	
	public PresetPattern(String name, String type, String source) {
		super(source);
		this.name = name;
		this.type = type;
	}

	@Override
	public boolean matchPattern(List<String> lines, int linesCount){
		this.lineMatch = Matcher.NO_LINE_MATCH;
		int indexLine = 0; 
		
		if (lines == null || lines.isEmpty())
			return false;
		
		// compare with 1 line patterns
		for (String line : lines){
			if (matchLine(line)){
				this.lineMatch = linesCount - indexLine -1; // must be read from stack top 
				this.hitCount++;
				return true;
			}
			indexLine++;
		}
		
		// compare with multiple line patterns
		return matchMultipleLine(lines, linesCount);
	}

	@Override
	public void updatePriority(int p) throws JzrInitializationException {
		// validate the priority first
		if (p < Rule.RULE_OPTIONAL_PRIORITY_MIN || p > Rule.RULE_OPTIONAL_PRIORITY_MAX)
			throw new JzrInitializationException("Invalid pattern priority for pattern \"" + this.name 
					+ "\". Value must be between " + Rule.RULE_OPTIONAL_PRIORITY_MIN 
					+ " and " + Rule.RULE_OPTIONAL_PRIORITY_MAX 
					+ ". Current value is : " + p);
		
		// override current value
		priority = p > priority ? p : priority;
	}

	@Override
	public boolean isLowLevelPattern(){
		if (this.regexMatcher != null)
			return false; 	 // Regex patterns do not support "." feature
		else if (this.stringMatcher == null)
			return false;    // Won't happen then
		else 
			return this.stringMatcher.isLowLevelPattern();
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return name;
	}
	
	@Override
	public String getType() {
		return type; // can be null
	}
	
	@Override
	public List<String> getPatterns(){
		List<String> patterns = new ArrayList<>();
		if (regexMatcher != null){
			patterns.addAll(regexMatcher.getPatterns());
		}
		if (stringMatcher != null){
			patterns.addAll(stringMatcher.getPatterns());
		}
		return patterns; 
	}	
	
	public void addPattern(String pattern, String type) {
		if (pattern == null || pattern.isEmpty())
			return;
		
		if (Matcher.MATCHER_STRING.equals(type)){
			if (stringMatcher == null)
				stringMatcher = new StringMatcher();
			stringMatcher.addPattern(pattern);
		}
		else if (Matcher.MATCHER_REGEX.equals(type)){
			if (regexMatcher == null)
				regexMatcher = new RegexMatcher();
			regexMatcher.addPattern(pattern);
		}
	}
	
	public long getSize() {
		return this.size;
	}

	public void setSize(long size) {
		if (this.size > size)
			this.size = size;
	}
	
	private boolean matchMultipleLine(List<String> lines, int linesCount) {
		if (this.stringMatcher != null && this.stringMatcher.matchMultipleLine(lines, linesCount)){
			this.lineMatch = this.stringMatcher.getMatchingMultipleLine();
			this.hitCount++;
			return true;
		}
		else if (this.regexMatcher != null && this.regexMatcher.matchMultipleLine(lines, linesCount)){
			this.lineMatch = this.regexMatcher.getMatchingMultipleLine();
			this.hitCount++;
			return true;
		}
		else
			return false;
	}

	private boolean matchLine(String line) {
		if (this.stringMatcher != null && this.stringMatcher.matchLine(line))
			return true;
		else if (this.regexMatcher != null && this.regexMatcher.matchLine(line))
			return true;
		else
			return false;
	}
	
}
