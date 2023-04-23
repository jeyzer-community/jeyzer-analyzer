package org.jeyzer.analyzer.data.flags;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2021 Jeyzer
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
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JVMFlags {

	// logger
	private static final Logger logger = LoggerFactory.getLogger(JVMFlags.class);
	
	public static final String JVM_FLAGS_FILE_NAME = "jvm-flags.txt";
	public static final String JVM_FLAG_PROPERTY_PREFIX = "jzr.jdk.flag.";
	
	private Map<String, JVMFlag> flags = new TreeMap<>();
	
	public JVMFlags(File jvmFlagsFile) throws IOException {
		try (
				FileReader reader = new FileReader(jvmFlagsFile);
				BufferedReader bfReader = new BufferedReader(reader);
			)
		{
			String line = bfReader.readLine();
			while (line != null) {
				if (!line.isEmpty()) {
					JVMFlag flag = new JVMFlag(line);
					if (flags.containsKey(flag.getName())) {
						// process any change event : keep the latest
						JVMFlag other = flags.get(flag.getName());
						if (flag.getTime() > other.getTime())
							flags.put(flag.getName(), flag);
					} else {
						flags.put(flag.getName(), flag);
					}
				}
				line = bfReader.readLine();
			}
		}
	}
	
	public static JVMFlags loadJVMFlags(File jvmFlagsFile) {
		if (jvmFlagsFile == null) {
			logger.debug("JVM flags file not provided.");
			return null;
		}

		JVMFlags jvmflags = null;
		try {
			jvmflags = new JVMFlags(jvmFlagsFile);
		} catch (FileNotFoundException e) {
			logger.info("JVM flags file not found : {}", jvmFlagsFile.getAbsolutePath());
		} catch (IOException e) {
			logger.info("Failed to load the JVM flags : {}", jvmFlagsFile.getAbsolutePath());
		}
		
		return jvmflags; 
	}
	
	public Collection<JVMFlag> getJVMFlags(){
		return this.flags.values();
	}

	public Properties getProperties() {
		Properties props = new Properties();
		
		for (JVMFlag flag : getJVMFlags()) {
			String name = JVM_FLAG_PROPERTY_PREFIX + flag.getName();
			props.put(name, flag.getValue());
		}
		
		return props;
	}
}
