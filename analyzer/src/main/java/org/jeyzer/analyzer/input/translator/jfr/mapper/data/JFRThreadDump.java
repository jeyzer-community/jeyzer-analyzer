package org.jeyzer.analyzer.input.translator.jfr.mapper.data;

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

import java.io.PrintWriter;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jeyzer.analyzer.input.translator.jfr.reader.JFRDescriptor;

import jdk.jfr.consumer.RecordedEvent;

public class JFRThreadDump {
	
	private static final String JZ_PREFIX = "\tJz>\t";
	private static final String JZR_FIELD_EQUALS = "\t";
	
	private static final String SYSTEM_CPU_FIELD = JZ_PREFIX + "system cpu" + JZR_FIELD_EQUALS;
	private static final String SYSTEM_FREE_MEMORY_FIELD = JZ_PREFIX + "system free memory" + JZR_FIELD_EQUALS;
	private static final String SYSTEM_TOTAL_MEMORY_FIELD = JZ_PREFIX + "system total memory" + JZR_FIELD_EQUALS;
	private static final String PROCESS_CPU_FIELD = JZ_PREFIX + "process cpu" + JZR_FIELD_EQUALS;
	private static final String PROCESS_UP_TIME = JZ_PREFIX + "process up time" + JZR_FIELD_EQUALS;
	
	private static final String MEMORY_HEAP_USED = JZ_PREFIX + "memory:heap:used" + JZR_FIELD_EQUALS;
	private static final String MEMORY_HEAP_MAX = JZ_PREFIX + "memory:heap:max" + JZR_FIELD_EQUALS;
	
	private static final String MEM_PREFIX = JZ_PREFIX + "mem pool:";
	private static final String GC_PREFIX = JZ_PREFIX + "gc:";

	// JFR specific 
	private static final String JZT_PREFIX = "\tJzt>\t";
	private static final String THREAD_MEMORY_ALLOCATED = JZT_PREFIX + "memory" + JZR_FIELD_EQUALS;
//	private static final String THREAD_USR_CPU_LOAD = JZT_PREFIX + "user time" + JZR_FIELD_EQUALS;
//	private static final String THREAD_SYS_CPU_LOAD = JZT_PREFIX + "cpu time" + JZR_FIELD_EQUALS;
	
	private Date date;
	private Instant instant;
	private String jfrTd;
	
	// header
	private float sysCpu;
	private float processCpu;
	
	private long sysFreeMemory;
	private long sysTotalMemory;
	
	private long processUpTime = -1L;

	private Map<Long, Long> threadMemoryAllocateds = new HashMap<>();
//	private Map<Long, Float> threadUsrCPULoad = new HashMap<>();
//	private Map<Long, Float> threadSysCPULoad = new HashMap<>();
	
	private JFRGarbageCollection gc;
	
	public JFRThreadDump(RecordedEvent tdEvent) {
		this.jfrTd = tdEvent.getString("result");
		this.date = new Date(tdEvent.getStartTime().toEpochMilli());
		this.instant = tdEvent.getStartTime();
	}

	public Date getDate() {
		return this.date;
	}
	
	public Instant getInstant() {
		return this.instant;
	}

	public void addSystemCPU(float value) {
		sysCpu = value;
	}
	
	public void addProcessCPU(float value) {
		processCpu = value;
	}
	
	public void addSystemFreeMemory(long value) {
		sysFreeMemory = value;
	}
	
	public void addSystemTotalMemory(long value) {
		sysTotalMemory = value;
	}

	public void addProcessUpTime(long processUpTime) {
		this.processUpTime = processUpTime;
	}
	
	public void addThreadMemoryAllocation(long threadId, long memoryAllocated) {
		this.threadMemoryAllocateds.put(threadId, memoryAllocated);
	}
	
//	public void addThreadUsrCPULoad(long threadId, float usrCPULoad) {
//		this.threadUsrCPULoad.put(threadId, usrCPULoad);
//	}
//	
//	public void addThreadSysCPULoad(long threadId, float sysCPULoad) {
//		this.threadSysCPULoad.put(threadId, sysCPULoad);
//	}

	public void setJFRGarbageCollection(JFRGarbageCollection gc) {
		this.gc = gc;
	}
	
	public JFRGarbageCollection getGarbageCollection() {
		return gc;
	}

	public void write(JFRDescriptor descriptor, PrintWriter writer) {
		
		// Classic JZR headers
		
		writer.write(SYSTEM_CPU_FIELD + this.sysCpu + System.lineSeparator());
		writer.write(SYSTEM_FREE_MEMORY_FIELD + this.sysFreeMemory + System.lineSeparator());
		writer.write(SYSTEM_TOTAL_MEMORY_FIELD + this.sysTotalMemory + System.lineSeparator());
		writer.write(PROCESS_CPU_FIELD + this.processCpu + System.lineSeparator());
		writer.write(PROCESS_UP_TIME + this.processUpTime + System.lineSeparator());
		writeGCData(descriptor, writer);
		writer.write(System.lineSeparator());
		
		// Thread values - JFR specific
		for (Long threadId : this.threadMemoryAllocateds.keySet())
			writer.write(THREAD_MEMORY_ALLOCATED + threadId + JZR_FIELD_EQUALS + this.threadMemoryAllocateds.get(threadId) + System.lineSeparator());
//		for (Long threadId : this.threadUsrCPULoad.keySet())
//			writer.write(THREAD_USR_CPU_LOAD + threadId + JZR_FIELD_EQUALS + this.threadUsrCPULoad.get(threadId) + System.lineSeparator());
//		for (Long threadId : this.threadSysCPULoad.keySet())
//			writer.write(THREAD_SYS_CPU_LOAD + threadId + JZR_FIELD_EQUALS + this.threadSysCPULoad.get(threadId) + System.lineSeparator());
		
		writer.write(System.lineSeparator());
		writer.write(this.jfrTd + System.lineSeparator());
	}

	private void writeGCData(JFRDescriptor descriptor, PrintWriter writer) {
		if (!descriptor.hasGCValidData())
			return;
		
		// Heap memory
		JFRLastGarbageCollection latest = this.getGarbageCollection().getLatestGC();
		writer.write(MEMORY_HEAP_USED + latest.getHeapUsedAfterGC() + System.lineSeparator());
		writer.write(MEMORY_HEAP_MAX + latest.getHeapMaxAfterGC() + System.lineSeparator());

		// Memory pool section = latest GC whatever its type as JFR does not expose memory pool events
		String jzrOldPoolName = this.getGarbageCollection().getOldDescriptor().getJzrPoolName();
		String jzrYoungPoolName = this.getGarbageCollection().getYoungDescriptor().getJzrPoolName();
		writer.write(MEM_PREFIX + jzrOldPoolName + ":peak:used" + JZR_FIELD_EQUALS + this.getGarbageCollection().getUsedPeakOld() + System.lineSeparator());
		writer.write(MEM_PREFIX + jzrOldPoolName + ":usage:used" + JZR_FIELD_EQUALS + latest.getOldUsedAfterGC() + System.lineSeparator());
		writer.write(MEM_PREFIX + jzrOldPoolName + ":usage:max" + JZR_FIELD_EQUALS + latest.getOldMaxAfterGC() + System.lineSeparator());
		writer.write(MEM_PREFIX + jzrOldPoolName + ":usage:committed" + JZR_FIELD_EQUALS + latest.getOldCommittedAfterGC() + System.lineSeparator());
		writer.write(MEM_PREFIX + jzrOldPoolName + ":collection:used" + JZR_FIELD_EQUALS + latest.getOldUsedAfterGC() + System.lineSeparator());
		writer.write(MEM_PREFIX + jzrYoungPoolName + ":peak:used" + JZR_FIELD_EQUALS + this.getGarbageCollection().getUsedPeakEden() + System.lineSeparator());
		writer.write(MEM_PREFIX + jzrYoungPoolName + ":usage:used" + JZR_FIELD_EQUALS + latest.getEdenUsedAfterGC() + System.lineSeparator());
		writer.write(MEM_PREFIX + jzrYoungPoolName + ":usage:max" + JZR_FIELD_EQUALS + latest.getEdenMaxAfterGC() + System.lineSeparator());
		writer.write(MEM_PREFIX + jzrYoungPoolName + ":usage:committed" + JZR_FIELD_EQUALS + latest.getEdenCommittedAfterGC() + System.lineSeparator()); // not available in JFR
		
		// GC section
		String jzrGCName = this.getGarbageCollection().getYoungDescriptor().getJzrGCName();
		writer.write(GC_PREFIX + jzrGCName + ":count" + JZR_FIELD_EQUALS + this.getGarbageCollection().getYoungGCCount() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":time" + JZR_FIELD_EQUALS + this.getGarbageCollection().getYoungGCTime() + System.lineSeparator());
		
		JFRLastGarbageCollection last = this.getGarbageCollection().getNewLastGC();
		writer.write(GC_PREFIX + jzrGCName + ":last gc:id" + JZR_FIELD_EQUALS + last.getGCId() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:duration" + JZR_FIELD_EQUALS + last.getDuration() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:start_time" + JZR_FIELD_EQUALS + last.getStartTime() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:end_time" + JZR_FIELD_EQUALS + last.getEndTime() + System.lineSeparator());
		
		writer.write(GC_PREFIX + jzrGCName + ":last gc:before:" + jzrOldPoolName + ":usage:used" + JZR_FIELD_EQUALS + last.getOldUsedBeforeGC() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:before:" + jzrOldPoolName + ":usage:max" + JZR_FIELD_EQUALS + last.getOldMaxBeforeGC() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:before:" + jzrYoungPoolName + ":usage:used" + JZR_FIELD_EQUALS + last.getEdenUsedBeforeGC() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:before:" + jzrYoungPoolName + ":usage:max" + JZR_FIELD_EQUALS + last.getEdenMaxBeforeGC() + System.lineSeparator());
//		writer.write(GC_PREFIX + jzrGCName + ":last gc:before:" + jzrYoungPoolName + ":usage:max" + JZR_FIELD_EQUALS + "-1" + System.lineSeparator());
		
		writer.write(GC_PREFIX + jzrGCName + ":last gc:after:" + jzrOldPoolName + ":usage:used" + JZR_FIELD_EQUALS + last.getOldUsedAfterGC() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:after:" + jzrOldPoolName + ":usage:max" + JZR_FIELD_EQUALS + last.getOldMaxAfterGC() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:after:" + jzrYoungPoolName + ":usage:used" + JZR_FIELD_EQUALS + last.getEdenUsedAfterGC() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:after:" + jzrYoungPoolName + ":usage:max" + JZR_FIELD_EQUALS + last.getEdenMaxAfterGC() + System.lineSeparator());
//		writer.write(GC_PREFIX + jzrGCName + ":last gc:after:" + jzrYoungPoolName + ":usage:max" + JZR_FIELD_EQUALS + "-1" + System.lineSeparator());
		
		jzrGCName = this.getGarbageCollection().getOldDescriptor().getJzrGCName();
		writer.write(GC_PREFIX + jzrGCName + ":count" + JZR_FIELD_EQUALS + this.getGarbageCollection().getOldGCCount() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":time" + JZR_FIELD_EQUALS + this.getGarbageCollection().getOldGCTime() + System.lineSeparator());
		
		last = this.getGarbageCollection().getOldLastGC();
		writer.write(GC_PREFIX + jzrGCName + ":last gc:id" + JZR_FIELD_EQUALS + last.getGCId() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:duration" + JZR_FIELD_EQUALS + last.getDuration() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:start_time" + JZR_FIELD_EQUALS + last.getStartTime() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:end_time" + JZR_FIELD_EQUALS + last.getEndTime() + System.lineSeparator());
		
		writer.write(GC_PREFIX + jzrGCName + ":last gc:before:" + jzrOldPoolName + ":usage:used" + JZR_FIELD_EQUALS + last.getOldUsedBeforeGC() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:before:" + jzrOldPoolName + ":usage:max" + JZR_FIELD_EQUALS + last.getOldMaxBeforeGC() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:before:" + jzrYoungPoolName + ":usage:used" + JZR_FIELD_EQUALS + last.getEdenUsedBeforeGC() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:before:" + jzrYoungPoolName + ":usage:max" + JZR_FIELD_EQUALS + last.getEdenMaxBeforeGC() + System.lineSeparator());
//		writer.write(GC_PREFIX + jzrGCName + ":last gc:before:" + jzrYoungPoolName + ":usage:max" + JZR_FIELD_EQUALS + "-1" + System.lineSeparator());
		
		writer.write(GC_PREFIX + jzrGCName + ":last gc:after:" + jzrOldPoolName + ":usage:used" + JZR_FIELD_EQUALS + last.getOldUsedAfterGC() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:after:" + jzrOldPoolName + ":usage:max" + JZR_FIELD_EQUALS + last.getOldMaxAfterGC() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:after:" + jzrYoungPoolName + ":usage:used" + JZR_FIELD_EQUALS + last.getEdenUsedAfterGC() + System.lineSeparator());
		writer.write(GC_PREFIX + jzrGCName + ":last gc:after:" + jzrYoungPoolName + ":usage:max" + JZR_FIELD_EQUALS + last.getEdenMaxAfterGC() + System.lineSeparator());
//		writer.write(GC_PREFIX + jzrGCName + ":last gc:after:" + jzrYoungPoolName + ":usage:max" + JZR_FIELD_EQUALS + "-1" + System.lineSeparator());
	}
}
