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







import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegexPattern extends SnapshotFileNamePattern{

	private static final Logger logger = LoggerFactory.getLogger(SnapshotFileNamePattern.class);	
	
	private java.util.regex.Pattern regex;
	
	public RegexPattern(String pattern){
		super(pattern);
		regex = Pattern.compile(pattern);
	}

	@Override
	public boolean accept(String name) {
		Matcher matcher = regex.matcher(name);
		if (matcher.find()){
			logger.debug("RegexPattern {} matched - file named : {}", regex.pattern(), name);
			return true;
		}
		else{
			logger.debug("RegexPattern {} not matched - file named : {}", regex.pattern(), name);
			return false;			
		}
	}

	@Override
	public String getDescriptor() {
		String descriptor = regex.pattern();
		if (descriptor.startsWith("\\.") && descriptor.endsWith("$"))
			descriptor = descriptor.replace('$',' ').replaceFirst("\\\\.", "*.").trim();
		return descriptor;
	}
	
}
