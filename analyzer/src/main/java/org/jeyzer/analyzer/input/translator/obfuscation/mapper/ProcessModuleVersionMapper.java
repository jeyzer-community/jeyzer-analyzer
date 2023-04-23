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

import org.jeyzer.analyzer.data.module.ProcessModule;
import org.jeyzer.analyzer.data.module.ProcessModules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessModuleVersionMapper {
	
	public static final String PROCESS_MODULE_TOKEN = "##";
	public static final Pattern PROCESS_MODULE_REGEX = Pattern.compile(".*##([^#]+)##.*");

	// logger
	private static final Logger logger = LoggerFactory.getLogger(ProcessModuleVersionMapper.class);
	
	private ProcessModuleVersionMapper() {
	}
	
	public static String mapVersion(String templatePath, ProcessModules processModules) {
		if (processModules == null) {
			logger.warn("No process modules available in the recording although the configuration path contains a process module version. Path is : " + templatePath);
			return templatePath;
		}
		
		// Template paths should contain only one module version to expand usually. 
		// Cover anyway the multiple case in a while loop
		while (true){
			Matcher matcher = PROCESS_MODULE_REGEX.matcher(templatePath);
			if (!matcher.matches())
				break; // done
			
			String moduleName = matcher.group(1);
			if (moduleName == null)
				break; // done
			
			ProcessModule pm = processModules.getProcessModule(moduleName);
			if (pm == null) {
				logger.warn("No process module version found for the module : " + moduleName + ". Path is : " + templatePath);
				return templatePath;
			}
			
			String version = pm.getVersion();
			if (version == null || version.isEmpty()) {
				logger.warn("No process module version is empty for the module : " + moduleName + ". Path is : " + templatePath);
				return templatePath;
			}
			
			templatePath = templatePath.replaceAll(PROCESS_MODULE_TOKEN + moduleName + PROCESS_MODULE_TOKEN, version);
		}
		
		if (templatePath.contains(PROCESS_MODULE_TOKEN))
			logger.warn("Template path still contains process module tokens (" + PROCESS_MODULE_TOKEN + ") after expansion which is not expected. Please check the configuration. Path is : " + templatePath);

		return templatePath;
	}
}
