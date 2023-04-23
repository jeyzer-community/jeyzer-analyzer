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







import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.ProcessCard.ProcessCardProperty;

public class ProcessCommandLine {
	
	public static final String PROPERTY_INPUT_PARAMETERS = "jzr.ext.process.input.parameters";
	
	public static final String SYSTEM_PROPERTY_PREFIX = "-D";
	public static final String SYSTEM_PROPERTY_DASH = "-";
	public static final String SYSTEM_PROPERTY_DOUBLE_DASH = "--";
	public static final String SYSTEM_PROPERTY_EQUALS = "=";
	
	private Properties props; // key value parameters. ex: my.param=value 
	private List<String> params; // all parameters. ex: my.param=value Xmx4Go
	
	public ProcessCommandLine(ProcessCard processCard){
		props = new Properties();
		params = new ArrayList<String>();
		
		buildProcessCommandLineParameters(processCard);
	}

	private void buildProcessCommandLineParameters(ProcessCard processCard) {
		if (processCard == null)
			return;
		
		ProcessCardProperty processCardCommandLineProperty = processCard.getValue(PROPERTY_INPUT_PARAMETERS);
		if (processCardCommandLineProperty == null || processCardCommandLineProperty.getValue() == null)
			return;
		
		StringTokenizer st = new StringTokenizer(processCardCommandLineProperty.getValue());
		while (st.hasMoreTokens()) {
	         String param = st.nextToken();
	         
	         if (param.startsWith(SYSTEM_PROPERTY_PREFIX))
	        	 param = param.substring(SYSTEM_PROPERTY_PREFIX.length()); // remove -D
	         else if (param.startsWith(SYSTEM_PROPERTY_DOUBLE_DASH))
	        	 param = param.substring(SYSTEM_PROPERTY_DOUBLE_DASH.length()); // remove the double dash
	         else if (param.startsWith(SYSTEM_PROPERTY_DASH))
	        	 param = param.substring(SYSTEM_PROPERTY_DASH.length()); // remove the dash
	         
	         params.add(param);
	         
	         int equalsPos = param.indexOf(SYSTEM_PROPERTY_EQUALS);
	         if (equalsPos != -1){
	        	 String key = param.substring(0, equalsPos);
	        	 String value = param.substring(equalsPos+1);
	        	 if (!value.isEmpty()){
	        		 props.put(key, value);
	        	 }
	         }
	     }
	}
	
	public String getParameter(Object name){
		if (name instanceof String)
			return getParameter((String)name);
		else
			return getParameter((Pattern)name);
	}
	
	private String getParameter(String name){
		for (String param : this.params){
			if (param.startsWith(name))
				return param; 
		}
		return null;
	}
	
	private String getParameter(Pattern pattern){
		for (String param : this.params){
			// return the first one that matches
			if (pattern.matcher(param).find()){
				return param; 
			}
		}
		return null;
	}
	
	public CommandLineProperty getValue(Object name){
		if (name instanceof String)
			return getValue((String)name);
		else
			return getValue((Pattern)name);
	}
	
	private CommandLineProperty getValue(String name){
		String value = props.getProperty(name);
		if (value != null)
			return new CommandLineProperty(
					name,
					value
					);
		else
			return null;
	}
	
	private CommandLineProperty getValue(Pattern pattern){
		for (Object key : props.keySet()){
			String propertyName = (String) key;
			
			// return the first one that matches
			if (pattern.matcher(propertyName).find()){
				return new CommandLineProperty(
						propertyName,
						props.getProperty(propertyName)
					); 
			}
		}
		return null;
	}
	
	public static class CommandLineProperty{
		private String name;
		private String value;
		
		public CommandLineProperty(String name, String value){
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
