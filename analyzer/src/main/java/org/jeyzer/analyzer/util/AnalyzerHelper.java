package org.jeyzer.analyzer.util;

import java.io.File;
import java.util.Arrays;

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







import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jeyzer.analyzer.data.ProcessCard;
import org.jeyzer.analyzer.data.flags.JVMFlags;
import org.jeyzer.analyzer.data.jar.ProcessJars;
import org.jeyzer.analyzer.data.module.ProcessModules;
import org.jeyzer.analyzer.data.stack.ThreadStack;

public class AnalyzerHelper {
	
	public static final String MODULE_APP_PREFIX = "app/";
	public static final String AT_PREFIX = "at ";
	public static final String SPACE_PREFIX = "    ";
	
	private static final List<String> recordingDescriptorfiles = Arrays.asList(
			ProcessCard.PROCESS_CARD_FILE_NAME, 
			ProcessJars.PROCESS_JAR_PATHS_FILE_NAME, 
			ProcessModules.PROCESS_MODULES_FILE_NAME,
			JVMFlags.JVM_FLAGS_FILE_NAME
			);

	private AnalyzerHelper(){
	}
	
	public static int getMissingFilesCount(final Date previous, final Date current, final int period) {
		long diff = current.getTime() - previous.getTime();
		return ((int)diff / (period * 1000));
	}	
	
	public static int countParagraphMaxLength(String str) {
		if(str == null || str.isEmpty())
			return 0;

		int maxLength = 0;
		int currentLength = 0;
		int pos = 0;
		int startPos = 0;
		while ((pos = str.indexOf('\n', pos) + 1) != 0) {
			currentLength = pos - startPos - 1; // ignore the carriage return
			maxLength = currentLength > maxLength ?  currentLength : maxLength;
			startPos = pos;
		}
		
		// last line (or first line if single)
		currentLength = str.length() - startPos;
		maxLength = currentLength > maxLength ?  currentLength : maxLength;
		
		return maxLength;
	}
	
	public static int countLines(String str) {
		if(str == null || str.isEmpty())
			return 0;

		int lines = 1;
		int pos = 0;
		while ((pos = str.indexOf('\n', pos) + 1) != 0) {
			lines++;
		}
		return lines;
	}
	
	public static String getPrincipalCompositeFunction(List<ThreadStack> stacks){
		Map<String,Integer> candidates = new HashMap<>();
		ValueComparator vc =  new ValueComparator(candidates);
		SortedMap<String,Integer> sortedMap = new TreeMap<>(vc);
		
		for (ThreadStack stack : stacks){
			String principal = stack.getPrincipalTag();
			Integer count = candidates.get(principal);
			if (count == null)
				count = 0;
			candidates.put(principal, count + 1);
		}
		
		sortedMap.putAll(candidates);
	    
		return sortedMap.firstKey();
	}

	public static String getPrincipalCompositeOperation(List<ThreadStack> stacks){
		Map<String,Integer> candidates = new HashMap<>();
		ValueComparator vc =  new ValueComparator(candidates);
		SortedMap<String,Integer> sortedMap = new TreeMap<>(vc);
		
		for (ThreadStack stack : stacks){
			String principal = stack.getPrincipalOperation();
			Integer count = candidates.get(principal);
			if (count == null)
				count = 0;
			candidates.put(principal, count + 1);
		}
		
		sortedMap.putAll(candidates);
	    
		return sortedMap.firstKey();
	}	
	
	public static String getPrincipalCompositeContentionType(List<ThreadStack> stacks){
		Map<String,Integer> candidates = new HashMap<>();
		ValueComparator vc =  new ValueComparator(candidates);
		SortedMap<String,Integer> sortedMap = new TreeMap<>(vc);
		
		for (ThreadStack stack : stacks){
			String principal = stack.getPrincipalContentionType();
			Integer count = candidates.get(principal);
			if (count == null)
				count = 0;
			candidates.put(principal, count + 1);
		}
		
		sortedMap.putAll(candidates);
	    
		return sortedMap.firstKey();
	}
	
	public static String getListAsString(List<String> values){
		if (values == null || values.isEmpty())
			return "";
		
		// [value1, value2, value3]
		String concatenatedValues = values.toString();
		
		// Remove the start [ and ending ]
		return concatenatedValues.substring(1, concatenatedValues.length()-1);
	}
	
	public static String stripCodeLine(String codeLine, boolean strip) {
		if (codeLine == null)
			return codeLine;

		int pos = -1;
		boolean stripModule = false;
		
		if (strip) {
			// Ignore this case (strip only the at) :
			// Example : at java.lang.Object.wait(java.base@11.0.9/Object.java:328)
			int parPos = codeLine.indexOf('(');
			int modulePos = codeLine.indexOf('@');
			stripModule = modulePos != -1 && modulePos < parPos;
		}
		
		// Remove the java module prefix
		if (stripModule) {
			
			if (codeLine.contains(MODULE_APP_PREFIX)) {
				// Strip the app and module name
				// at app/org.jeyzer.demo@2.3-SNAPSHOT/org.jeyzer.demo.features.Feature.hold(Feature.java:69)
				pos = codeLine.indexOf(MODULE_APP_PREFIX);
				if (pos != -1)
					pos = codeLine.indexOf('/', pos + MODULE_APP_PREFIX.length());
			}
			else {
				// Strip the module name
				// at jdk.jfr@11.0.6/jdk.jfr.internal.PlatformRecorder$$Lambda$141/0x0000000100220040.run(PlatformRecorder$$Lambda.java)
				pos = codeLine.indexOf('/');
			}
		}
		else {
			// Strip the at
			// at org.jeyzer.demo@2.3-SNAPSHOT/org.jeyzer.demo.features.Feature.hold(Feature.java:69)
			pos = codeLine.indexOf(AT_PREFIX);
			if (pos != -1)
				pos += AT_PREFIX.length() - 1;
		}
		if (pos != -1)
			// add some space up front for better readability
			return (SPACE_PREFIX + codeLine.substring(pos+1)).intern();
		else
			return codeLine;
	}
	
	public static class ValueComparator implements Comparator<String> {

	    Map<String, Integer> base;
	    
	    public ValueComparator(Map<String, Integer> base) {
	        this.base = base;
	    }

	    @Override
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}
	
	public static boolean isRecordingStaticFile(File file) {
		return recordingDescriptorfiles.contains(file.getName());
	}
}
