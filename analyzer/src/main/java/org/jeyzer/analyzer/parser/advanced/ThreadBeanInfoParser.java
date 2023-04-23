package org.jeyzer.analyzer.parser.advanced;

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




import org.jeyzer.analyzer.data.stack.ThreadStackJeyzerMXInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Longs;

public class ThreadBeanInfoParser {
	
	public static final String CPU_TIME_TAG = "cpu time\t";
	public static final String USER_TIME_TAG = "user time\t";
	public static final String THREAD_ALLOCATED_MEMORY_TAG = "memory\t";
	
	// Jeyzer MX
	public static final String JEYZER_MX_TAG = "Jz ";
	public static final String JEYZER_MX_USER_TAG = "user\t";
	public static final String JEYZER_MX_CONTEXT_ID_TAG = "context id\t";
	public static final String JEYZER_MX_ACTION_TAG = "action\t";
	public static final String JEYZER_MX_ACTION_START_TIME_TAG = "action start time\t";
	public static final String JEYZER_MX_ACTION_ID_TAG = "action id\t";
	public static final String JEYZER_MX_CONTEXT_PARAM_PREFIX_TAG = "cxt param-";
	
	private static final Logger logger = LoggerFactory.getLogger(ThreadBeanInfoParser.class);

	public static class ThreadBeanInfo{
		private ThreadStackJeyzerMXInfo jeyzerMinfo = null;
		
		private long memory = -1;
		private long cpuTime = -1;
		private long userTime = -1;
		
		public long getMemory() {
			return memory;
		}

		public long getCpuTime() {
			return cpuTime;
		}

		public long getUserTime() {
			return userTime;
		}
		
		public ThreadStackJeyzerMXInfo getThreadStackJeyzerMXInfo() {
			return jeyzerMinfo;
		}
		
		public void setThreadStackJeyzerMXInfo(ThreadStackJeyzerMXInfo info) {
			jeyzerMinfo = info;
		}		
	}
	
	public ThreadBeanInfoParser() {
	}
	
	public void parse(String line, ThreadBeanInfo info) {
		if (line.startsWith(CPU_TIME_TAG)){
			parseCPUTime(line, info);
		}
		else if (line.startsWith(USER_TIME_TAG)){
			parseUserTime(line, info);
		}
		else if (line.startsWith(THREAD_ALLOCATED_MEMORY_TAG)){
			parseAllocatedBytes(line, info);
		}
		else if (line.startsWith(JEYZER_MX_TAG)){
			ThreadStackJeyzerMXInfo jeyzerMXInfo = info.getThreadStackJeyzerMXInfo();
			if (jeyzerMXInfo ==  null){
				jeyzerMXInfo = new ThreadStackJeyzerMXInfo();
				info.setThreadStackJeyzerMXInfo(jeyzerMXInfo);
			}
			parseJeyzerMXInfo(line.substring(JEYZER_MX_TAG.length()), jeyzerMXInfo);
		}		
		else{
			logger.warn("Could not parse thread bean info : {}", line);
		}
	}

	private void parseJeyzerMXInfo(String line, ThreadStackJeyzerMXInfo jeyzerMXInfo) {
		if (line.startsWith(JEYZER_MX_ACTION_ID_TAG)){
			jeyzerMXInfo.setJzrId(line.substring(line.indexOf(JEYZER_MX_ACTION_ID_TAG) + JEYZER_MX_ACTION_ID_TAG.length()));
		}
		else if (line.startsWith(JEYZER_MX_ACTION_TAG)){
			jeyzerMXInfo.setFunctionPrincipal(line.substring(line.indexOf(JEYZER_MX_ACTION_TAG) + JEYZER_MX_ACTION_TAG.length()));
		}
		else if (line.startsWith(JEYZER_MX_ACTION_START_TIME_TAG)){
			parseJeyzerMXActionStartTime(line, jeyzerMXInfo);
		}
		else if (line.startsWith(JEYZER_MX_USER_TAG)){
			jeyzerMXInfo.setUser(line.substring(line.indexOf(JEYZER_MX_USER_TAG) + JEYZER_MX_USER_TAG.length()));
		}
		else if (line.startsWith(JEYZER_MX_CONTEXT_PARAM_PREFIX_TAG)){
			parseJeyzerMXInfoContextParam(line.substring(JEYZER_MX_CONTEXT_PARAM_PREFIX_TAG.length()), jeyzerMXInfo);
		}
		else if (line.startsWith(JEYZER_MX_CONTEXT_ID_TAG)){
			jeyzerMXInfo.setId(line.substring(line.indexOf(JEYZER_MX_CONTEXT_ID_TAG) + JEYZER_MX_CONTEXT_ID_TAG.length()));
		}
		else{
			logger.warn("Could not parse thread bean Jeyzer MX info : {}", line);
		}
	}

	private void parseJeyzerMXActionStartTime(String line, ThreadStackJeyzerMXInfo jeyzerMXInfo) {
		int posStart = line.indexOf(JEYZER_MX_ACTION_START_TIME_TAG) + JEYZER_MX_ACTION_START_TIME_TAG.length();
		
		Long value = Longs.tryParse(line.substring(posStart));
		if (value == null){
			logger.warn("Failed to convert thread Jeyzer MX action start time : ", line.substring(posStart));
			value = -1L;
		}
		
		jeyzerMXInfo.setStartTime(value);
	}

	private void parseJeyzerMXInfoContextParam(String line, ThreadStackJeyzerMXInfo jeyzerMXInfo) {
		String param;
		String value = null;
	
		int posStartEquals = line.indexOf('\t');
		
		param = line.substring(0, posStartEquals);
		if (line.length() > posStartEquals)
			value = line.substring(posStartEquals+1);
		
		jeyzerMXInfo.addContextParam(param, value);
	}

	private void parseCPUTime(String cpuDetails, ThreadBeanInfo info) {
		int posStart = cpuDetails.indexOf(CPU_TIME_TAG) + CPU_TIME_TAG.length();
		
		Long value = Longs.tryParse(cpuDetails.substring(posStart));
		if (value == null){
			logger.warn("Failed to convert thread CPU time : ", cpuDetails.substring(posStart));
			value = -1L;
		}
		
		info.cpuTime = value;
	}

	private void parseUserTime(String cpuDetails, ThreadBeanInfo info) {
		int posStart = cpuDetails.indexOf(USER_TIME_TAG) + USER_TIME_TAG.length();
		
		Long value = Longs.tryParse(cpuDetails.substring(posStart));
		if (value == null){
			logger.warn("Failed to convert thread user time : ", cpuDetails.substring(posStart));
			value = -1L;
		}
		
		info.userTime = value;
	}
	
	private void parseAllocatedBytes(String memoryDetails, ThreadBeanInfo info) {
		int posStart = memoryDetails.indexOf(THREAD_ALLOCATED_MEMORY_TAG) + THREAD_ALLOCATED_MEMORY_TAG.length();
		
		Long value = Longs.tryParse(memoryDetails.substring(posStart));
		if (value == null){
			logger.warn("Failed to convert thread memory : ", memoryDetails.substring(posStart));
			value = -1L;
		}
		
		info.memory = value;
	}

	public ThreadBeanInfo createThreadBeanInfo() {
		return new ThreadBeanInfo();
	}

}
