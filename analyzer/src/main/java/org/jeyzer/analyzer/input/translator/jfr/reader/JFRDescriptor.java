package org.jeyzer.analyzer.input.translator.jfr.reader;

import java.text.SimpleDateFormat;
import java.time.Instant;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.jfr.consumer.RecordedEvent;

public class JFRDescriptor {
	
	private static final Logger logger = LoggerFactory.getLogger(JFRDescriptor.class);
	
	public static final String JFR_FIELD_NAME = "name";
	public static final String JFR_FIELD_START_TIME = "startTime";
	public static final String JFR_FIELD_THREAD = "thread";
	public static final String JFR_FIELD_EVENT_THREAD = "eventThread";
	public static final String JFR_FIELD_GC_ID = "gcId";
	public static final String JFR_FIELD_WHEN = "when";
	public static final String JFR_HEAP_USED = "heapUsed";
	public static final String JFR_HEAP_SPACE = "heapSpace";
	public static final String JFR_RESERVED_SIZE = "reservedSize";
	public static final String JFR_COMMITTED_SIZE = "committedSize";
	public static final String JFR_EDEN_USED_SIZE = "edenUsedSize";
	public static final String JFR_EDEN_TOTAL_SIZE = "edenTotalSize";
	public static final String JFR_USED = "used";

	public static final String PROPERTY_USER_TIMEZONE = "user.timezone";
	
	private List<String> loadedEventTypes = new ArrayList<>();
	private boolean gcDataValidated = true;
	
	private Map<Date, RecordedEvent> tdEvents = new TreeMap<>();
	private List<RecordedEvent> systemPropertyEvents = new ArrayList<>();
	private List<RecordedEvent> jvmInformationEvents = new ArrayList<>();
	private List<RecordedEvent> osInformationEvents = new ArrayList<>();
	private List<RecordedEvent> cpuInformationEvents = new ArrayList<>();
	private List<RecordedEvent> initialEnvironmentVariableEvents = new ArrayList<>();
	private List<RecordedEvent> moduleRequireEvents = new ArrayList<>();
	private List<RecordedEvent> moduleExportEvents = new ArrayList<>();
	private List<RecordedEvent> jvmFlagEvents = new ArrayList<>();
	
	private List<RecordedEvent> cpuLoadEvents = new ArrayList<>();
	private List<RecordedEvent> physicalMemoryEvents = new ArrayList<>();

	private List<RecordedEvent> gcConfigurationEvents = new ArrayList<>();
	private List<RecordedEvent> garbageCollectionEvents = new ArrayList<>(); // Eden, Old(=Old+Eden) and Full(=Old+Eden)
	private List<RecordedEvent> youngGarbageCollectionEvents = new ArrayList<>(); // Eden
	private List<RecordedEvent> oldGarbageCollectionEvents = new ArrayList<>();
	private List<RecordedEvent> gcGCHeapSummaryEvents = new ArrayList<>();
	private List<RecordedEvent> gcG1HeapSummaryEvents = new ArrayList<>();
	private List<RecordedEvent> gcPSHeapSummaryEvents = new ArrayList<>();
	
	private List<RecordedEvent> threadAllocationStatisticsEvents = new ArrayList<>();
	private List<RecordedEvent> threadCPULoadEvents = new ArrayList<>();
	private List<RecordedEvent> threadEndEvents = new ArrayList<>();
	
	// contextual, optimization for virtual thread events
	private int startVTCounter; 
	private Instant startVTNextTdInstant;
	private Iterator<RecordedEvent> startTdIterator;
	private boolean startAllTDsCovered;
	
	private Map<Date, Integer> startVTCounterMap = null;

	// contextual, optimization for virtual thread events
	private int endVTCounter;
	private Instant endVTNextTdInstant;
	private Iterator<RecordedEvent> endTdIterator;
	private boolean endAllTDsCovered;
	
	private Map<Date, Integer> endVTCounterMap = new HashMap<>();
	
	public void addThreadDumpEvent(RecordedEvent event) {
		Date date = new Date(event.getStartTime().toEpochMilli());
		if (!tdEvents.containsKey(date)) {
			tdEvents.put(date,event);			
		}
		else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd---HH-mm-ss-SSS");
			logger.warn("The JFR recording contains a duplicate jdk.ThreadDump event with start time {}. The duplicate JFR event will be ignored in the analysis. See the jdk.ThreadDump-jfr.txt dump file for more details on the events.", sdf.format(date));
		}
	}
	
	public boolean hasThreadDumpEvents() {
		return !tdEvents.isEmpty();
	}
	
	public void addSystemPropertyEvent(RecordedEvent event) {
		systemPropertyEvents.add(event);
	}
	
	public void addJVMInformation(RecordedEvent event) {
		jvmInformationEvents.add(event);
	}
	
	public void addOSInformation(RecordedEvent event) {
		osInformationEvents.add(event);
	}
	
	public void addCPUInformation(RecordedEvent event) {
		cpuInformationEvents.add(event);
	}
	
	public void addInitialEnvironmentVariable(RecordedEvent event) {
		initialEnvironmentVariableEvents.add(event);
	}

	public void addModuleRequire(RecordedEvent event) {
		moduleRequireEvents.add(event);
	}
	
	public void addModuleExport(RecordedEvent event) {
		moduleExportEvents.add(event);
	}
	
	public void addJVMFlag(RecordedEvent event) {
		this.jvmFlagEvents.add(event);
	}
	
	public void addCPULoad(RecordedEvent event) {
		cpuLoadEvents.add(event);
	}
	
	public void addSystemMemory(RecordedEvent event) {
		physicalMemoryEvents.add(event);
	}

	public void addGCConfiguration(RecordedEvent event) {
		this.gcConfigurationEvents.add(event);
	}
	
	public void addGarbageCollection(RecordedEvent event) {
		this.garbageCollectionEvents.add(event);
	}
	
	public void addYoungGarbageCollection(RecordedEvent event) {
		this.youngGarbageCollectionEvents.add(event);
	}

	public void addOldGarbageCollection(RecordedEvent event) {
		this.oldGarbageCollectionEvents.add(event);
	}
	
	public void addGCHeapSummary(RecordedEvent event) {
		this.gcGCHeapSummaryEvents.add(event);
	}
	
	public void addG1HeapSummary(RecordedEvent event) {
		this.gcG1HeapSummaryEvents.add(event);
	}

	public void addPSHeapSummary(RecordedEvent event) {
		this.gcPSHeapSummaryEvents.add(event);
	}
	
	public void addThreadAllocationStatistics(RecordedEvent event) {
		this.threadAllocationStatisticsEvents.add(event);
	}
	
	public void addThreadCPULoad(RecordedEvent event) {
		this.threadCPULoadEvents.add(event);
	}
	
	public void addThreadEnd(RecordedEvent event) {
		this.threadEndEvents.add(event);
	}
	
	public Collection<RecordedEvent> getThreadDumpEvents() {
		return this.tdEvents.values();
	}
	
	public List<RecordedEvent> getSystemPropertyEvents() {
		return this.systemPropertyEvents;
	}
	
	public String getTimeZoneId() {
		for (RecordedEvent tdEvent : this.systemPropertyEvents)
			if (PROPERTY_USER_TIMEZONE.equals( tdEvent.getString("key")))
				return tdEvent.getString("value");
		return null;
	}
	
	public List<RecordedEvent> getJVMInfoEvents() {
		return this.jvmInformationEvents;
	}

	public List<RecordedEvent> getOSInfoEvents() {
		return this.osInformationEvents;
	}
	
	public List<RecordedEvent> getCPUInfoEvents() {
		return this.cpuInformationEvents;
	}

	public List<RecordedEvent> getInitialEnvironmentVariableEvents() {
		return this.initialEnvironmentVariableEvents;
	}
	
	public List<RecordedEvent> getModuleExportEvents() {
		return this.moduleExportEvents;
	}
	
	public List<RecordedEvent> getModuleRequireEvents() {
		return this.moduleRequireEvents;
	}

	public List<RecordedEvent> getJVMFlagEvents() {
		return this.jvmFlagEvents;
	}
	
	public List<RecordedEvent> getCPULoadEvents() {
		return this.cpuLoadEvents;
	}

	public List<RecordedEvent> getPhysicalMemoryEvents() {
		return this.physicalMemoryEvents;
	}

	public List<RecordedEvent> getYoungGarbageCollectionEvents() {
		return this.youngGarbageCollectionEvents;
	}
	
	public List<RecordedEvent> getOldGarbageCollectionEvents() {
		return this.oldGarbageCollectionEvents;
	}

	public List<RecordedEvent> getGCConfigurationEvents() {
		return gcConfigurationEvents;
	}

	public List<RecordedEvent> getGCHeapSummaryEvents() {
		return gcGCHeapSummaryEvents;
	}

	public List<RecordedEvent> getG1HeapSummaryEvents() {
		return gcG1HeapSummaryEvents;
	}

	public List<RecordedEvent> getGarbageCollectionEvents() {
		return garbageCollectionEvents;
	}

	public List<RecordedEvent> getThreadAllocationStatisticsEvents() {
		return threadAllocationStatisticsEvents;
	}
	
	public List<RecordedEvent> getThreadCPULoadEvents() {
		return threadCPULoadEvents;
	}
	
	public List<RecordedEvent> getThreadEndEvents() {
		return threadEndEvents;
	}

	public List<RecordedEvent> getPSHeapSummaryEvents() {
		return gcPSHeapSummaryEvents;
	}

	public void setLoadedEventTypes(List<String> eventTypes) {
		this.loadedEventTypes = eventTypes;
	}
	
	public boolean containsEventType(String eventType) {
		return this.loadedEventTypes.contains(eventType);
	}

	public void invalidateGCData() {
		this.gcDataValidated = false;
	}
	
	public boolean hasGCValidData() {
		return this.gcDataValidated;
	}

	public Map<Date, Integer> getVirtualThreadStartCounters() {
		return this.startVTCounterMap;
	}
	
	public Map<Date, Integer> getVirtualThreadEndCounters() {
		return this.endVTCounterMap;
	}
	
	public void incrementVirtualThreadStart(JFRDescriptor jfrDescriptor, Instant endTime) {
		if (!jfrDescriptor.hasThreadDumpEvents() || startAllTDsCovered)
			return;
		
		if (startVTNextTdInstant == null) {
			// initialize
			startVTCounterMap = new HashMap<>();
			startTdIterator = jfrDescriptor.getThreadDumpEvents().iterator();
			startVTNextTdInstant = jfrDescriptor.getThreadDumpEvents().iterator().next().getEndTime();
		}		

		if (endTime.isBefore(startVTNextTdInstant)) {
			startVTCounter++;
		}
		else {
			// store the counter
			startVTCounterMap.put(Date.from(startVTNextTdInstant), startVTCounter);
			
			// move to the next slot
			while(startTdIterator.hasNext()) {
				startVTNextTdInstant = startTdIterator.next().getEndTime();
				
				if (endTime.isAfter(startVTNextTdInstant)){
					// store zero and jump to next slot slot
					startVTCounterMap.put(Date.from(startVTNextTdInstant), 0);
					continue;
				}
				else {
					startVTCounter = 1;
					return;
				}
			}
			// last slot
			startAllTDsCovered = true;
		}
	}
	
	public void incrementVirtualThreadEnd(JFRDescriptor jfrDescriptor, Instant endTime) {
		if (!jfrDescriptor.hasThreadDumpEvents() || endAllTDsCovered)
			return;
		
		if (endVTNextTdInstant == null) {
			// initialize
			endVTCounterMap = new HashMap<>();
			endTdIterator = jfrDescriptor.getThreadDumpEvents().iterator();
			endVTNextTdInstant = jfrDescriptor.getThreadDumpEvents().iterator().next().getEndTime();
		}		

		if (endTime.isBefore(endVTNextTdInstant)) {
			endVTCounter++;
		}
		else {
			// store the counter
			endVTCounterMap.put(Date.from(endVTNextTdInstant), endVTCounter);
			
			// move to the next slot
			while(endTdIterator.hasNext()) {
				endVTNextTdInstant = endTdIterator.next().getEndTime();
				
				if (endTime.isAfter(endVTNextTdInstant)){
					// store zero and jump to next slot
					endVTCounterMap.put(Date.from(endVTNextTdInstant), 0);
					continue;
				}
				else {
					// adequate slot, start new counter
					endVTCounter = 1;
					return;
				}
			}
			// last slot
			endAllTDsCovered = true;
		}
	}
}
