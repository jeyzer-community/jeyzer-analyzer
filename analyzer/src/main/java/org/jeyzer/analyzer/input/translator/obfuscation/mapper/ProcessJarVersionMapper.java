package org.jeyzer.analyzer.input.translator.obfuscation.mapper;

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

import org.jeyzer.analyzer.data.jar.ProcessJarVersion;
import org.jeyzer.analyzer.data.jar.ProcessJars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessJarVersionMapper {
	
	public static final String PROCESS_JAR_TOKEN = "%%";
	public static final Pattern PROCESS_JAR_REGEX = Pattern.compile(".*%%([^%]+)%%.*");

	// logger
	private static final Logger logger = LoggerFactory.getLogger(ProcessJarVersionMapper.class);
	
	private ProcessJarVersionMapper() {
	}
	
	public static String mapVersion(String templatePath, ProcessJars processJars) {
		if (processJars == null) {
			logger.warn("No process jars available in the recording although the configuration path contains a process jar version. Path is : " + templatePath);
			return templatePath;
		}
		
		// Template paths should contain only one jar version to expand usually. 
		// Cover anyway the multiple case in a while loop
		while (true){
			Matcher matcher = PROCESS_JAR_REGEX.matcher(templatePath);
			if (!matcher.matches())
				break; // done
			
			String jarName = matcher.group(1);
			if (jarName == null)
				break; // done
			
			ProcessJarVersion pjv = processJars.getProcessJarVersion(jarName);
			if (pjv == null) {
				logger.warn("No process jar version found for the jar : " + jarName + ". Path is : " + templatePath);
				return templatePath;
			}
			
			String version = pjv.getJarVersion();
			if (version == null || version.isEmpty()) {
				logger.warn("No process jar version is empty for the jar : " + jarName + ". Path is : " + templatePath);
				return templatePath;
			}
			
			templatePath = templatePath.replaceAll(PROCESS_JAR_TOKEN + jarName + PROCESS_JAR_TOKEN, version);
		}
		
		if (templatePath.contains(PROCESS_JAR_TOKEN))
			logger.warn("Template path still contains process jar tokens (" + PROCESS_JAR_TOKEN + ") after expansion which is not expected. Please check the configuration. Path is : " + templatePath);

		return templatePath;
	}
}
