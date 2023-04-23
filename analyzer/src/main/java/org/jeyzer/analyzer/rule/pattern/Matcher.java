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

public interface Matcher {

	public static final int NO_LINE_MATCH = -1;	
	
	public static final String MULTILINE_SEPARATOR = ";";	
	
	public static final String MATCHER_STRING = "pattern";
	public static final String MATCHER_REGEX = "pattern_regex";
	
	public void addPattern(String pattern);
	
	public boolean matchLine(String value);
	
	public boolean matchMultipleLine(List<String> lines, int linesCount);
	
	public int getMatchingMultipleLine();
	
	public List<String> getPatterns();
}
