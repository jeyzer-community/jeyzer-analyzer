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
import java.util.StringTokenizer;

public class StringMatcher implements Matcher {

	private static final String SUFFIX_LOW_LEVEL = "."; 
	
	protected List<String> patterns = new ArrayList<>(3);
	protected List<List<String>> multiLinePatterns = new ArrayList<>(0);

	protected int lineMatch = NO_LINE_MATCH;
	
	public boolean isMultiLinePattern(){
		return this.multiLinePatterns.size() > 0;
	}

	@Override	
	public void addPattern(String pattern) {
		if (pattern.contains(MULTILINE_SEPARATOR)){
			// multiple line pattern
			addMultipleLinePattern(pattern);
		}
		else {
			// single line pattern
			this.patterns.add(pattern);
		}
	}	

	@Override
	public boolean matchLine(String value){
		if (value == null)
			return false;
		
		for (String pattern : this.patterns){
			if (value.indexOf(pattern) != -1)
				return true;
		}
		
		return false;
	}
	
	@Override
	public boolean matchMultipleLine(List<String> lines, int linesCount){
		// for each multiple line pattern 
		for (List<String> mlpattern : this.multiLinePatterns){
			// for each line (and subsequents) to match
			for (int lineIndex=0; lineIndex<lines.size(); lineIndex++){
				// let's try to match it against the multi-line pattern
				if (matchNextLine(lines, lineIndex, mlpattern, 0, linesCount))
					return true;
			}
		}
		
		return false;
	}	

	private boolean matchNextLine(List<String> lines, int lineIndex, List<String> mlpattern, int patternIndex, final int linesCount) {
		String pattern = mlpattern.get(patternIndex);
		String line = lines.get(lineIndex);
		
		if (line.indexOf(pattern) != -1){
			if (patternIndex+1 == mlpattern.size()){
				this.lineMatch = linesCount - lineIndex - mlpattern.size() + 1;
				return true;  // exact match
			}
			else{
				if (lineIndex+1 == lines.size())
					return false;  // no more lines to match against pattern
				else
					return matchNextLine(lines, lineIndex+1, mlpattern, patternIndex+1, linesCount);
			}
		}
		else
			return false;
	}

	@Override
	public List<String> getPatterns() {
		List<String> result = new ArrayList<>();
		
		result.addAll(patterns);
		
		for (List<String> multiLinePattern : this.multiLinePatterns){
			String multiLineResult = "[ML]" + multiLinePattern.get(0);
			for (int i=1; i<multiLinePattern.size(); i++){
				multiLineResult += ";" + multiLinePattern.get(i);
			}
			result.add(multiLineResult);
		}
		
		return result;
	}
	
	private void addMultipleLinePattern(String pattern){
		List<String> patternLines = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(pattern, MULTILINE_SEPARATOR);
		while (st.hasMoreTokens()){
			patternLines.add(st.nextToken());
		}
		// multiple line pattern  
		this.multiLinePatterns.add(patternLines);
	}
	
	
	public boolean isLowLevelPattern() {
		if (isMultiLinePattern())
			return false;
		
		for (String pattern : this.patterns){
			if (!pattern.endsWith(SUFFIX_LOW_LEVEL))
				return false;
		}
		return true;
	}

	@Override
	public int getMatchingMultipleLine() {
		return this.lineMatch;
	}
	
}
