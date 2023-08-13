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




import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.setup.JzrSetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class DumpBeanInfoParser {

	public static final String CAPTURE_TIME = "capture time\t";
	public static final String PROCESS_CPU = "process cpu\t";
	public static final String PROCESS_UP_TIME = "process up time\t";
	public static final String PROCESS_OPEN_FD_COUNT = "open file desc count\t";
	public static final String SYSTEM_CPU = "system cpu\t";
	public static final String SYSTEM_PHYSICAL_FREE_MEMORY = "system free memory\t";
	public static final String SYSTEM_PHYSICAL_TOTAL_MEMORY = "system total memory\t";
	public static final String MEMORY_OBJ_PENDING_FINALIZATION = "memory:obj pending finalization\t";
	public static final String VIRTUAL_THREAD_CREATED_COUNT = "virtual thread created\t";
	public static final String VIRTUAL_THREAD_TERMINATED_COUNT = "virtual thread terminated\t";
	public static final String JEYZER_CTX_PARAM = "Jz cxt param-";
	public static final String MX_BEAN_PARAM = "mx:";
	public static final String PARAM_EQUALS = "\t";
	
	private static final Logger logger = LoggerFactory.getLogger(DumpBeanInfoParser.class);	
	
	private final DiskSpaceBeanInfoParser diskSpaceParser = new DiskSpaceBeanInfoParser();
	private final DiskWriteBeanInfoParser diskWriteParser = new DiskWriteBeanInfoParser();
	private final EventBeanParserInfoParser eventParser = new EventBeanParserInfoParser();

	private final MemoryPoolBeanInfoParser memoryPoolParser;
	private final GarbageCollectorBeanInforParser garbageCollectorParser;
	
	public DumpBeanInfoParser(JzrSetupManager setupMgr) {
		memoryPoolParser = new MemoryPoolBeanInfoParser(setupMgr.getMemoryPoolSetupManager());
		garbageCollectorParser =  new GarbageCollectorBeanInforParser(
				setupMgr.getGarbageCollectorSetupManager(), 
				setupMgr.getMemoryPoolSetupManager());
	}

	public void parse(ThreadDump dump, String line) {
		if (line.startsWith(EventBeanParserInfoParser.JEYZER_APP_EVENT)) {
			eventParser.parseApplicationEvent(dump, line);
		}
		else if (line.startsWith(EventBeanParserInfoParser.JEYZER_PUB_EVENT)) {
			eventParser.parsePublisherEvent(dump, line);
		}
		else if (line.startsWith(CAPTURE_TIME)){
			parseCaptureTime(dump, line);
		}		
		else if (line.startsWith(PROCESS_CPU)){
			parseProcessCPUTime(dump, line);
		}
		else if (line.startsWith(PROCESS_UP_TIME)){
			parseProcessUpTime(dump, line);
		}
		else if (line.startsWith(PROCESS_OPEN_FD_COUNT)){
			parseProcesOpenFileDescriptorCount(dump, line);
		}
		else if (line.startsWith(SYSTEM_CPU)){
			parseSystemCPUInfo(dump, line);
		}
		else if (line.startsWith(SYSTEM_PHYSICAL_FREE_MEMORY)){
			parseSystemFreeMemoryInfo(dump, line);
		}
		else if (line.startsWith(SYSTEM_PHYSICAL_TOTAL_MEMORY)){
			parseSystemTotalMemoryInfo(dump, line);
		}
		else if (line.startsWith(MEMORY_OBJ_PENDING_FINALIZATION)){
			parseMemoryObjectPendingFinalization(dump, line);
		}
		else if (line.startsWith(VIRTUAL_THREAD_CREATED_COUNT)){
			parseVirtualThreadCreatedCount(dump, line);
		}
		else if (line.startsWith(VIRTUAL_THREAD_TERMINATED_COUNT)){
			parseVirtualThreadTerminatedCount(dump, line);
		}
		else if (line.startsWith(MemoryPoolBeanInfoParser.MEMORY_POOL_PREFIX)){
			memoryPoolParser.parse(dump, line, true);
		}		
		else if (line.startsWith(MemoryPoolBeanInfoParser.MEMORY_PREFIX)){
			memoryPoolParser.parse(dump, line, false);
		}
		else if (line.startsWith(GarbageCollectorBeanInforParser.GARBAGE_COLLECTOR_PREFIX)){
			garbageCollectorParser.parse(dump, line);
		}
		else if (line.startsWith(DiskSpaceBeanInfoParser.DISK_SPACE_PREFIX)){
			diskSpaceParser.parse(dump, line);
		}
		else if (line.startsWith(DiskWriteBeanInfoParser.DISK_WRITE_PREFIX)){
			diskWriteParser.parse(dump, line);
		}
		else if (line.startsWith(JEYZER_CTX_PARAM)){
			parseJeyzerMXCtxParam(dump, line);
		}
		else if (line.startsWith(MX_BEAN_PARAM)){
			parseMXBeanParam(dump, line);
		}
		else{
			logger.warn("Could not parse dump bean info : {}", line);
		}	
	}

	private void parseMemoryObjectPendingFinalization(ThreadDump dump, String line) {
		int posStart = line.indexOf(MEMORY_OBJ_PENDING_FINALIZATION) + MEMORY_OBJ_PENDING_FINALIZATION.length();
		
		Integer objectPendingFinalization = Ints.tryParse(line.substring(posStart));
		if (objectPendingFinalization == null){
			logger.warn("Failed to convert object pending finalization count : ", line.substring(posStart));
			objectPendingFinalization = -1;
		}
		
		dump.setObjectPendingFinalizationCount(objectPendingFinalization);		
	}

	private void parseCaptureTime(ThreadDump dump, String line) {
		int posStart = line.indexOf(CAPTURE_TIME) + CAPTURE_TIME.length();
		
		Long captureTime = Longs.tryParse(line.substring(posStart));
		if (captureTime == null){
			logger.warn("Failed to convert capture time : ", line.substring(posStart));
			captureTime = -1L;
		}
		
		dump.setCaptureTime(captureTime);
	}

	private void parseProcessCPUTime(ThreadDump dump, String line) {
		int posStart = line.indexOf(PROCESS_CPU) + PROCESS_CPU.length();

		Double processCpu = Doubles.tryParse(line.substring(posStart));
		if (processCpu == null){
			logger.warn("Failed to convert process CPU : ", line.substring(posStart));
			processCpu = -1D;
		}
		
		dump.setProcessCPU(processCpu);
	}

	private void parseProcessUpTime(ThreadDump dump, String line) {
		int posStart = line.indexOf(PROCESS_UP_TIME) + PROCESS_UP_TIME.length();
		
		Long processUpTime = Longs.tryParse(line.substring(posStart));
		if (processUpTime == null){
			logger.warn("Failed to convert process up time : ", line.substring(posStart));
			processUpTime = -1L;
		}
		
		dump.setProcessUpTime(processUpTime);
	}
	
	private void parseProcesOpenFileDescriptorCount(ThreadDump dump, String line) {
		int posStart = line.indexOf(PROCESS_OPEN_FD_COUNT) + PROCESS_OPEN_FD_COUNT.length();
		
		Long processOpenFDCount = Longs.tryParse(line.substring(posStart));
		if (processOpenFDCount == null){
			logger.warn("Failed to convert process open file descriptor count : {}", line.substring(posStart));
			processOpenFDCount = -1L;
		}
		
		dump.setProcessOpenFileDescriptorCount(processOpenFDCount);
	}
	
	private void parseVirtualThreadCreatedCount(ThreadDump dump, String line) {
		int posStart = line.indexOf(VIRTUAL_THREAD_CREATED_COUNT) + VIRTUAL_THREAD_CREATED_COUNT.length();
		
		Integer vtCreatedCount = Ints.tryParse(line.substring(posStart));
		if (vtCreatedCount == null){
			logger.warn("Failed to convert virtual thread created count : {}", line.substring(posStart));
			vtCreatedCount = -1;
		}
		
		dump.getVirtualThreads().setCreatedCount(vtCreatedCount);
	}

	private void parseVirtualThreadTerminatedCount(ThreadDump dump, String line) {
		int posStart = line.indexOf(VIRTUAL_THREAD_TERMINATED_COUNT) + VIRTUAL_THREAD_TERMINATED_COUNT.length();
		
		Integer vtTerminatedCount = Ints.tryParse(line.substring(posStart));
		if (vtTerminatedCount == null){
			logger.warn("Failed to convert virtual thread terminated count : {}", line.substring(posStart));
			vtTerminatedCount = -1;
		}
		
		dump.getVirtualThreads().setTerminatedCount(vtTerminatedCount);
	}	
	
	private void parseSystemCPUInfo(ThreadDump dump, String line) {
		int posStart = line.indexOf(SYSTEM_CPU) + SYSTEM_CPU.length();
		
		Double systemCpu = Doubles.tryParse(line.substring(posStart));
		if (systemCpu == null){
			logger.warn("Failed to convert system CPU : ", line.substring(posStart));
			systemCpu = -1D;
		}
		
		dump.setOperatingSystemCPU(systemCpu);
	}
	
	private void parseSystemFreeMemoryInfo(ThreadDump dump, String line) {
		int posStart = line.indexOf(SYSTEM_PHYSICAL_FREE_MEMORY) + SYSTEM_PHYSICAL_FREE_MEMORY.length();
		
		Long freeMemory = Longs.tryParse(line.substring(posStart));
		if (freeMemory == null){
			logger.warn("Failed to convert system free memory : ", line.substring(posStart));
			freeMemory = -1L;
		}
		
		dump.setSystemPhysicalFreeMemory(freeMemory);
	}
	
	private void parseSystemTotalMemoryInfo(ThreadDump dump, String line) {
		int posStart = line.indexOf(SYSTEM_PHYSICAL_TOTAL_MEMORY) + SYSTEM_PHYSICAL_TOTAL_MEMORY.length();
		
		Long totalMemory = Longs.tryParse(line.substring(posStart));
		if (totalMemory == null){
			logger.warn("Failed to convert system total memory : ", line.substring(posStart));
			totalMemory = -1L;
		}
		
		dump.setSystemPhysicalTotalMemory(totalMemory);
	}		

	private void parseJeyzerMXCtxParam(ThreadDump dump, String line) {
		String param;
		String value = null;
	
		int posStartParam = line.indexOf(JEYZER_CTX_PARAM) + JEYZER_CTX_PARAM.length();
		int posStartEquals = line.indexOf(PARAM_EQUALS);
		
		param = line.substring(posStartParam, posStartEquals);
		if (line.length() > posStartEquals)
			value = line.substring(posStartEquals+1).intern();
		
		dump.addJeyzerMXContextParam(param, value);
	}
	
	private void parseMXBeanParam(ThreadDump dump, String line) {
		String param;
		String value = null;
	
		int posStartParam = line.indexOf(MX_BEAN_PARAM) + MX_BEAN_PARAM.length();
		int posStartEquals = line.indexOf(PARAM_EQUALS);
		
		param = line.substring(posStartParam, posStartEquals);
		if (line.length() > posStartEquals)
			value = line.substring(posStartEquals+1).intern();
		
		dump.addJMXBeanParam(param, value);
	}
}
