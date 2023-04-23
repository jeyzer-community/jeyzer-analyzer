package org.jeyzer.analyzer.data.jar;

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

public class ProcessJars {
	
	// logger
	private static final Logger logger = LoggerFactory.getLogger(ProcessJars.class);
	
	public static final String PROCESS_JAR_PATHS_FILE_NAME = "process-jar-paths.txt";
	
	private List<ProcessJarVersion> jarVersions = new ArrayList<>();
	private Map<String,ProcessJarVersion> jarVersionsByName = new HashMap<>();

	public ProcessJars(File processJarPathsFile) throws IOException {
		try (
				FileReader reader = new FileReader(processJarPathsFile);
				BufferedReader bfReader = new BufferedReader(reader);
			)
		{
			String line = bfReader.readLine();
			while (line != null) {
				if (!line.isEmpty()) {
					ProcessJarVersion jarVersion = new ProcessJarVersion(line);
					jarVersions.add(jarVersion);
					jarVersionsByName.put(jarVersion.getJarName(), jarVersion);
				}
				line = bfReader.readLine();
			}
		}
		
		// order it by jar name
		java.util.Collections.sort(jarVersions, new java.util.Comparator<ProcessJarVersion>() {
			@Override
			public int compare(ProcessJarVersion pjv1, ProcessJarVersion pjv2) {
				return pjv1.getJarName().compareTo(pjv2.getJarName());
			}
		});
	}
	
	public static ProcessJars loadProcessJars(File processJarPathsFile) {
		if (processJarPathsFile == null) {
			logger.debug("Process jar paths file not provided.");
			return null;
		}

		ProcessJars processJars = null;
		try {
			processJars = new ProcessJars(processJarPathsFile);
		} catch (FileNotFoundException e) {
			logger.info("Process jar paths file not found : " + processJarPathsFile.getAbsolutePath());
		} catch (IOException e) {
			logger.info("Failed to load the process jar paths : " + processJarPathsFile.getAbsolutePath());
		}
		
		return processJars; 
	}

	public List<ProcessJarVersion> getJarVersions() {
		return jarVersions;
	}
	
	public ProcessJarVersion getProcessJarVersion(String key) {
		return this.jarVersionsByName.get(key);
	}
}
