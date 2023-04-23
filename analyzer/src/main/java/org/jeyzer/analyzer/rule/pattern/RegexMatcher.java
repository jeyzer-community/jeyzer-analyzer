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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatcher implements org.jeyzer.analyzer.rule.pattern.Matcher {

	private List<java.util.regex.Pattern> regularExpressions = new ArrayList<>(3);
	private List<List<java.util.regex.Pattern>> multiLineRegularExpressions = new ArrayList<>(0);

	protected int lineMatch = NO_LINE_MATCH;	
	
	public boolean isMultiLinePattern(){
		return this.multiLineRegularExpressions.size() > 0;
	}
	
	public boolean matchLine(String value) {
		if (value == null)
			return false;
		
		for (java.util.regex.Pattern regex : this.regularExpressions){
			Matcher matcher = regex.matcher(value);
			if (matcher.find()){
				return true;
			}
		}
		
		return false;
	}

	public boolean matchMultipleLine(List<String> lines, int linesCount) {
		// for each multiple line pattern 
		for (List<java.util.regex.Pattern> mlRegex : this.multiLineRegularExpressions){
			boolean continuity = false;
			int index = 0;
			java.util.regex.Pattern regex = mlRegex.get(index);
			int lineIndex = 0;
			
			for (String line : lines){
				Matcher matcher = regex.matcher(line);
				
				if (matcher.find()){
					// pattern has match
					if (index+1 == mlRegex.size() && continuity){
						// if last pattern, time to exit in case of success continuity
						this.lineMatch = linesCount - lineIndex - mlRegex.size() + 1;
						return true;
					}
					else {
						// otherwise get next pattern
						regex = mlRegex.get(++index);
						continuity = true;
					}
				}
				else{
					// not match
					if (continuity){
						// no consecutive match : pattern must be reset
						index = 0;
						regex = mlRegex.get(index);
						continuity = false;	
					}
				}
				lineIndex++;
			}
		}
		
		return false;
	}
	
	@Override
	public void addPattern(String pattern) {
		if (pattern.contains(MULTILINE_SEPARATOR)){
			// multiple line pattern
			addMultipleLineRegexPattern(pattern);
		}
		else {
			// single line pattern
			this.regularExpressions.add(Pattern.compile(pattern));
		}
	}

	private void addMultipleLineRegexPattern(String pattern) {
		List<java.util.regex.Pattern> patternLines = new ArrayList<>();
		
		// No conflict foreseen : ";" is not used by java regular expressions
		StringTokenizer st = new StringTokenizer(pattern, MULTILINE_SEPARATOR);
		while (st.hasMoreTokens()){
			patternLines.add(Pattern.compile(st.nextToken()));
		}
		// multiple line pattern  
		this.multiLineRegularExpressions.add(patternLines);
	}


	@Override
	public int getMatchingMultipleLine() {
		return this.lineMatch;
	}

	@Override
	public List<String> getPatterns() {
		List<String> patterns = new ArrayList<>();
		
		for (java.util.regex.Pattern pattern : regularExpressions){
			patterns.add(pattern.pattern());	
		}
		
		for (List<java.util.regex.Pattern> multiLine : multiLineRegularExpressions){
			String multiLinePattern = "";
			
			for (int i=0; i<multiLine.size(); i++){
				java.util.regex.Pattern pattern = multiLine.get(i);
				multiLinePattern += pattern.pattern();
				if (i != multiLine.size()-1)
					multiLinePattern += "\n";
			}
			patterns.add(multiLinePattern);
		}
		
		return patterns;
	}
	
}
