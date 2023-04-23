package org.jeyzer.analyzer.data.module;

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




import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessModules {
	
	// logger
	private static final Logger logger = LoggerFactory.getLogger(ProcessModules.class);
	
	public static final String PROCESS_MODULES_FILE_NAME = "process-modules.txt";
	
	private List<ProcessModule> modules = new ArrayList<>();
	private Map<String,ProcessModule> modulesByName = new HashMap<>();

	public ProcessModules(File processModulesFile) throws IOException {
		try (
				FileReader reader = new FileReader(processModulesFile);
				BufferedReader bfReader = new BufferedReader(reader);
			)
		{
			String line = bfReader.readLine();
			while (line != null) {
				if (!line.isEmpty()) {
					ProcessModule module = new ProcessModule(line);
					modules.add(module);
					modulesByName.put(module.getName(), module);
				}
				line = bfReader.readLine();
			}
		}
		
		// order it by module name
		java.util.Collections.sort(modules, new java.util.Comparator<ProcessModule>() {
			@Override
			public int compare(ProcessModule pjv1, ProcessModule pjv2) {
				return pjv1.getName().compareTo(pjv2.getName());
			}
		});
	}
	
	public static ProcessModules loadProcessModules(File processModulesFile) {
		if (processModulesFile == null) {
			logger.debug("Process modules file not provided.");
			return null;
		}

		ProcessModules processModules = null;
		try {
			processModules = new ProcessModules(processModulesFile);
		} catch (FileNotFoundException e) {
			logger.info("Process modules file not found : " + processModulesFile.getAbsolutePath());
		} catch (IOException e) {
			logger.info("Failed to load the process modules : " + processModulesFile.getAbsolutePath());
		}
		
		return processModules; 
	}

	public List<ProcessModule> getProcessModules() {
		return modules;
	}
	
	public ProcessModule getProcessModule(String key) {
		return this.modulesByName.get(key);
	}
}
