package org.jeyzer.analyzer.parser.io;

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

import org.jeyzer.analyzer.config.analysis.ConfigFilePattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnapshotFileNamePatternBuilder {

	private static final SnapshotFileNamePatternBuilder builder = new SnapshotFileNamePatternBuilder();

	private static final Logger logger = LoggerFactory.getLogger(SnapshotFileNamePatternBuilder.class);		
	
	private SnapshotFileNamePatternBuilder(){}
	
	public static SnapshotFileNamePatternBuilder newInstance(){
		return builder;
	}
	
	public List<SnapshotFileNamePattern> buildPatterns(List<ConfigFilePattern> confPatterns){
		List<SnapshotFileNamePattern> filePatterns = new ArrayList<>(); 
		
		for (ConfigFilePattern confPattern : confPatterns){
			SnapshotFileNamePattern pattern;
			
			if (ConfigFilePattern.JZRA_RECORDING_FILE_TIMESTAMP_PATTERN.equals(confPattern.getType())){
				pattern = new TimestampPattern(confPattern.getPattern(), confPattern.isSuffixIgnored());
			}
			else if (ConfigFilePattern.JZRA_RECORDING_FILE_REGEX_PATTERN.equals(confPattern.getType())){
				pattern = new RegexPattern(confPattern.getPattern());
			}
			else if (ConfigFilePattern.JZRA_RECORDING_FILE_JZR_PATTERN.equals(confPattern.getType())){
				pattern = new JzrPattern(confPattern.getPattern());
			}
			else{
				logger.warn("File name pattern invalid : " + confPattern.getType());
				continue;
			}
			filePatterns.add(pattern);
		}
		
		return filePatterns;
	}
	
}
