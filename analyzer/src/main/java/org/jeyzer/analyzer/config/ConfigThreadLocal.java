package org.jeyzer.analyzer.config;

import java.util.Enumeration;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigThreadLocal {

	// properties specific to current thread, used to resolve variables
	private static final ThreadLocal<Map<String, String>> threadContext = new ThreadLocal<Map<String, String>>(){
		@Override
		protected Map<String, String> initialValue(){
			return new HashMap<>();
		}
	};
	
	private ConfigThreadLocal(){
	}

	public static void put(Properties props){
		if (props == null)
			return;
		
		// Must follow this Properties reading way to not lose the non-String values
		Enumeration <?> enumKey = props.propertyNames();
		while (enumKey.hasMoreElements()) {
			String key = (String)enumKey.nextElement();
			threadContext.get().put(key, props.get(key).toString());			
		}
	}
	
	public static void remove(Properties props){
		if (props == null)
			return;
		
		for (String key : props.stringPropertyNames())
			threadContext.get().remove(key);
	}
	
	public static void putAll(Map<String, String> map){
		if (map == null)
			return;
		
		threadContext.get().putAll(map);
	}	

	public static String get(String key){
		if (key == null || key.isEmpty())
			return null;
		
		return threadContext.get().get(key);
	}
	
	public static Map<String, String> getProperties(){
		return threadContext.get();
	}

	public static void empty(){
		threadContext.remove();
	}
	
}
