package org.jeyzer.analyzer.data;

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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessCard {
	
	// logger
	private static final Logger logger = LoggerFactory.getLogger(ProcessCard.class);

	public static final String PROCESS_CARD_FILE_NAME = "process-card.properties";
	
	// Standard properties
	public static final String AVAILABLE_PROCESSORS = "jzr.ext.process.available.processors";
	public static final String JFR_AVAILABLE_PROCESSORS = "jfr.system.cpu.cores";
	
	public static final String GC_OLD_NAME = "jzr.analysis.gc.old.name";
	public static final String GC_YOUNG_NAME = "jzr.analysis.gc.young.name";
	
	public static final String FILE_DESCRIPTOR_MAX = "jzr.ext.system.unix.max.file.descriptor.count";
	
	private Properties props;

	public ProcessCard(File processCardFile) throws IOException {
		props = new Properties();
		
		try (
				FileReader reader = new FileReader(processCardFile);
			)
		{
			// load the property file
			props.load(preprocessPropertiesFile(reader));
		}
	}

	public Properties getProperties(){
		return (Properties)this.props.clone();
	}
	
	public void addProperties(Properties addedProps){
		this.props.putAll(addedProps);
	}

	public ProcessCardProperty getValue(Object name){
		if (name instanceof String)
			return getValue((String)name);
		else
			return getValue((Pattern)name);
	}
	
	public List<ProcessCardProperty> getValues(Pattern pattern){
		List<ProcessCardProperty> values = new ArrayList<>();
		
		for (Object key : props.keySet()){
			String propertyName = (String) key;
			
			// return the first one that matches
			if (pattern.matcher(propertyName).find()){
				values.add(
					new ProcessCardProperty(
						propertyName,
						props.getProperty(propertyName))
					);
			}
		}
		return values;
	}
	
	private ProcessCardProperty getValue(String name){
		String value = props.getProperty(name);
		if (value != null)
			return new ProcessCardProperty(
					name,
					value
					);
		else
			return null;
	}
	
	private ProcessCardProperty getValue(Pattern pattern){
		for (Object key : props.keySet()){
			String propertyName = (String) key;
			
			// return the first one that matches
			if (pattern.matcher(propertyName).find()){
				return new ProcessCardProperty(
						propertyName,
						props.getProperty(propertyName)
					); 
			}
		}
		return null;
	}
	
	private InputStream preprocessPropertiesFile(FileReader reader) throws IOException{
		try (
				Scanner in = new Scanner(reader);
			    ByteArrayOutputStream out = new ByteArrayOutputStream();
			)
		{
		    while(in.hasNext()){
		        out.write(preprocessLine(in.nextLine()).getBytes());
		    	out.write("\n".getBytes());
		    }
		    return new ByteArrayInputStream(out.toByteArray());
		}
	}
	
	private String preprocessLine(String nextLine) {
		String line = nextLine.replace("\\","\\\\");
		
		// escape early ':' chars to not get it interpreted as '=' 
		int columnPos = line.indexOf(':');
		if (columnPos == -1)
			return line;
		
		int equalPos = line.indexOf('=');
		while(columnPos < equalPos){
			if (columnPos != -1)
				line = line.substring(0, columnPos) + "\\" + line.substring(columnPos);
			else
				return line;
			columnPos = line.indexOf(':', columnPos+2); // as character is shifted to the right
			equalPos++; // shift to the right as well
		}
		return line;
	}
	
	public static ProcessCard loadProcessCard(File processCardFile) {
		if (processCardFile == null) {
			logger.debug("Process card file not provided.");
			return null;
		}

		ProcessCard card = null;
		try {
			card = new ProcessCard(processCardFile);
		} catch (FileNotFoundException e) {
			logger.info("Process card file not found : " + processCardFile.getAbsolutePath());
		} catch (IOException e) {
			logger.info("Failed to load the process card : " + processCardFile.getAbsolutePath());
		}
		
		return card; 
	}
	
	public static class ProcessCardProperty{
		private String name;
		private String value;
		
		public ProcessCardProperty(String name, String value){
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}
	}
}
