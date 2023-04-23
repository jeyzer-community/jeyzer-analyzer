package org.jeyzer.analyzer.input.translator.jfr.mapper;

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

import static org.jeyzer.analyzer.input.translator.jfr.reader.JFRDescriptor.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.jeyzer.analyzer.config.translator.jfr.ConfigJFRDecompression;
import org.jeyzer.analyzer.data.TimeZoneInfo.TimeZoneOrigin;
import org.jeyzer.analyzer.error.JzrTranslatorException;
import org.jeyzer.analyzer.input.translator.jfr.mapper.data.JFRGarbageCollection;
import org.jeyzer.analyzer.input.translator.jfr.mapper.data.JFRLastGarbageCollection;
import org.jeyzer.analyzer.input.translator.jfr.mapper.data.JFRThreadDump;
import org.jeyzer.analyzer.input.translator.jfr.reader.JFRDescriptor;
import org.jeyzer.analyzer.input.translator.jfr.reader.JFRReader;
import org.jeyzer.analyzer.util.TimeZoneInfoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedObject;
import jdk.jfr.consumer.RecordedThread;

public class JZRSnapshotMapper {

	private static final Logger logger = LoggerFactory.getLogger(JZRSnapshotMapper.class);

	private static final String JZR_FILE_JZR_PREFIX = "snap-";
	private static final String JZR_FILE_JZR_EXTENSION = ".jzr";
	// Do not use 'z' as it is not reliable.
	private static final String JZR_FILE_DATE_FORMAT = "yyyy-MM-dd---HH-mm-ss-SSS-";

	private static final String EDEN_SPACE_FIELD = "edenSpace";
	private static final String FROM_SPACE_FIELD = "fromSpace";
	private static final String TO_SPACE_FIELD = "toSpace";
	private static final String OLD_OBJECT_SPACE_FIELD = "oldObjectSpace";
	private static final String YOUNG_SPACE_FIELD = "youngSpace";
	private static final String OLD_SPACE_FIELD = "oldSpace";

	private ConfigJFRDecompression jfrCfg;
	private List<JFRThreadDump> dumps = new ArrayList<>();
	private long jvmStartTime; // -1 if not set

	private Comparator<RecordedEvent> eventTimeComparator = (RecordedEvent event1, RecordedEvent event2) -> {
		Date date1 = new Date(event1.getStartTime().toEpochMilli());
		Date date2 = new Date(event2.getStartTime().toEpochMilli());
		return date1.compareTo(date2);
	};

	public JZRSnapshotMapper(long jvmStartTime, ConfigJFRDecompression jfrCfg) {
		this.jvmStartTime = jvmStartTime;
		this.jfrCfg = jfrCfg;
	}

	public List<File> generateThreadDumps(JFRDescriptor descriptor, String outputDir) throws JzrTranslatorException {
		for (RecordedEvent tdEvent : descriptor.getThreadDumpEvents()) {
			JFRThreadDump dump = new JFRThreadDump(tdEvent);
			dumps.add(dump);
		}

		// sort by time
		dumps.sort((JFRThreadDump td1, JFRThreadDump td2) -> td1.getDate().compareTo(td2.getDate()));

		addThreadDumpHeaders(descriptor);

		return writeSnapshots(descriptor, outputDir);
	}

	private void addThreadDumpHeaders(JFRDescriptor descriptor) {
		// Event reference : https://bestsolution-at.github.io/jfr-doc/openjdk-15.html
		enrichCPULoad(descriptor);
		enrichSystemMemory(descriptor);
		enrichProcessUpTime();
		enrichGarbageCollection(descriptor);
		enrichThreadFigures(descriptor);
	}

	private void enrichThreadFigures(JFRDescriptor descriptor) {
		Map<Long, List<RecordedEvent>> memoryEvents = sortThreadEvents(
				JFR_FIELD_THREAD,
				descriptor.getThreadAllocationStatisticsEvents());
//		Map<Long,List<RecordedEvent>> cpuEvents = sortThreadEvents(
//		        JFR_FIELD_EVENT_THREAD, 
//		        descriptor.getThreadCPULoadEvents());
		Map<Long, List<RecordedEvent>> endEvents = sortThreadEvents(
				JFR_FIELD_EVENT_THREAD,
				descriptor.getThreadEndEvents()
				);

		for (JFRThreadDump dump : this.dumps) {
			for (Long threadId : memoryEvents.keySet()) {
				List<RecordedEvent> threadEvents = memoryEvents.get(threadId);
				List<RecordedEvent> threadEndEvents = endEvents.get(threadId); // can be null

				RecordedEvent event = getClosestEvent(dump, threadEvents); // can be null
				RecordedEvent endEvent = getClosestEvent(dump, threadEndEvents); // can be null

				if (event != null && (endEvent == null || endEvent.getStartTime().isBefore(event.getStartTime()))) {
					long memoryAllocated = event.getLong("allocated");
					dump.addThreadMemoryAllocation(threadId, memoryAllocated);
				}
			}

//			for (Long threadId : cpuEvents.keySet()) {
//				List<RecordedEvent> threadEvents = cpuEvents.get(threadId);
//				List<RecordedEvent> threadEndEvents = endEvents.get(threadId);  // can be null
//
//				RecordedEvent event = getClosestEvent(dump, threadEvents); // can be null
//				RecordedEvent endEvent = getClosestEvent(dump, threadEndEvents);  // can be null
//				
//				if (event != null && (endEvent == null || endEvent.getStartTime().isBefore(event.getStartTime()))) {
//					float usrCPU = event.getFloat("user"); 
//					dump.addThreadUsrCPULoad(threadId, usrCPU);
//					float sysCPU = event.getFloat("system");
//					dump.addThreadSysCPULoad(threadId, sysCPU);
//				}
//			}
		}
	}

	private Map<Long, List<RecordedEvent>> sortThreadEvents(String threadFieldName, List<RecordedEvent> allEvents) {
		Map<Long, List<RecordedEvent>> events = new HashMap<>();

		for (RecordedEvent event : allEvents) {
			RecordedThread thread = event.getThread(threadFieldName);
			if (thread == null)
				continue; // JEYZ-53 Thread may be null
			
			List<RecordedEvent> threadEvents = events.computeIfAbsent(thread.getJavaThreadId(),
					x -> new ArrayList<RecordedEvent>());
			threadEvents.add(event);
		}

		for (List<RecordedEvent> threadEvents : events.values())
			threadEvents.sort(eventTimeComparator);

		return events;
	}

	private void enrichProcessUpTime() {
		for (JFRThreadDump dump : this.dumps) {
			dump.addProcessUpTime(this.jvmStartTime != -1 ? (dump.getDate().getTime() - this.jvmStartTime): -1L);			
		}
	}

	private void enrichSystemMemory(JFRDescriptor descriptor) {
		List<RecordedEvent> events = descriptor.getPhysicalMemoryEvents();
		EventBrowseContext context = new EventBrowseContext();
		events.sort(eventTimeComparator);
		for (JFRThreadDump dump : this.dumps) {
			RecordedEvent event = getClosestEvent(dump, events, context);

			// System free memory
			long value = event != null ? event.getLong("usedSize") : -1;
			dump.addSystemFreeMemory(value);

			// System total memory
			value = event != null ? event.getLong("totalSize") : -1;
			dump.addSystemTotalMemory(value);
		}
	}

	private void enrichCPULoad(JFRDescriptor descriptor) {
		List<RecordedEvent> events = descriptor.getCPULoadEvents();
		if (events.isEmpty()) {
			logger.warn("JFR CPU Load is empty.");
			return;
		}
		EventBrowseContext context = new EventBrowseContext();
		events.sort(eventTimeComparator);
		for (JFRThreadDump dump : this.dumps) {
			RecordedEvent event = getClosestEvent(dump, events, context);

			// System CPU (value between 0 and 1)
			float value = event != null ? event.getFloat("machineTotal") : -1;
			dump.addSystemCPU(value);

			// Process CPU : let's take jvmUser (value between 0 and 1)
			// PS : JvmSystem is also available but lower than jvmUser
			// jvmUser corresponds to JZR recording values.
			// See also
			// https://docs.oracle.com/javase/9/docs/api/com/sun/management/OperatingSystemMXBean.html#getProcessCpuLoad--
			value = event != null ? event.getFloat("jvmUser") : -1;
			dump.addProcessCPU(value);
		}
	}

	private void enrichGarbageCollection(JFRDescriptor descriptor) {
		if (!descriptor.hasGCValidData())
			return;
		
		String oldGCName = extractGCName(descriptor, "oldCollector");
		String youngGCName = extractGCName(descriptor, "youngCollector");
		if (oldGCName == null || youngGCName == null) {
			logger.warn("JFR GC data cannot be processed : the old or young GC name is missing.");
			descriptor.invalidateGCData();
			return;
		}
		
		for (JFRThreadDump dump : this.dumps) {
			JFRGarbageCollection gc = new JFRGarbageCollection(oldGCName, youngGCName);
			dump.setJFRGarbageCollection(gc);
		}

		// Set the old and young GC
		identifyLatestGCs(descriptor);

		// Heap
		enrichLastGCFigures(descriptor);

		// Young GC
		setCumulativeYoungGCs(descriptor);

		// Old GC
		setCumulativeOldGCs(descriptor);

		// Peak figures (limited to used space)
		JFRGarbageCollection gc = new JFRGarbageCollection(oldGCName, youngGCName);
		if (gc.isG1GarbageCollector())
			enrichG1PeakFigures(descriptor);
		else if (gc.isPSGarbageCollector())
			enrichPSPeakFigures(descriptor);
		else if (gc.isSerialGarbageCollector())
			enrichSerialPeakFigures();
		else
			logger.warn("Skipping the GC peak figure enrichment : GC not supported.");
	}

	private void enrichSerialPeakFigures() {
		for (JFRThreadDump dump : this.dumps) {
			JFRGarbageCollection gc = dump.getGarbageCollection();
			gc.setOldUsedPeak(-1L);
			gc.setEdenUsedPeak(-1L);
		}
	}

	private void setCumulativeOldGCs(JFRDescriptor descriptor) {
		List<RecordedEvent> events = descriptor.getOldGarbageCollectionEvents();
		if (events.isEmpty())
			return;
		events.sort(eventTimeComparator);

		int eventPos = 0;
		long duration = 0;
		boolean lastEventReached = false;
		RecordedEvent event = events.get(eventPos);
		for (JFRThreadDump dump : this.dumps) {
			while (event.getStartTime().isBefore(dump.getInstant()) && !lastEventReached) {
				duration += event.getDuration().toMillis();
				if (eventPos + 1 == events.size()) {
					lastEventReached = true;
					break;
				}
				eventPos++;
				event = events.get(eventPos);
			}
			JFRGarbageCollection gc = dump.getGarbageCollection();
			gc.setOldGCTime(duration);
			gc.setOldGCCount(eventPos);
		}
	}

	private void setCumulativeYoungGCs(JFRDescriptor descriptor) {
		List<RecordedEvent> events = descriptor.getYoungGarbageCollectionEvents();
		if (events.isEmpty())
			return;
		events.sort(eventTimeComparator);

		int eventPos = 0;
		long duration = 0;
		boolean lastEventReached = false;
		RecordedEvent event = events.get(eventPos);
		for (JFRThreadDump dump : this.dumps) {
			while (isEventBefore(event, dump) && !lastEventReached) {
				duration += event.getDuration().toMillis();
				if (eventPos + 1 == events.size()) {
					lastEventReached = true;
					break;
				}
				eventPos++;
				event = events.get(eventPos);
			}
			JFRGarbageCollection gc = dump.getGarbageCollection();
			gc.setYoungGCTime(duration);
			gc.setYoungGCCount(eventPos);
		}
	}

	private void identifyLatestGCs(JFRDescriptor descriptor) {
		// associate it to Old GC ids
		List<RecordedEvent> events = descriptor.getGarbageCollectionEvents();
		if (events.isEmpty())
			logger.info("The list of JFR Garbage Collection events is empty.");

		EventBrowseContext context = new EventBrowseContext();
		events.sort(eventTimeComparator);
		for (JFRThreadDump dump : this.dumps) {
			JFRGarbageCollection gc = dump.getGarbageCollection();
			RecordedEvent event = getClosestGarbageCollectionEvent(dump, events, context,
					JFRGarbageCollection.getOldGCTypes());
			if (event != null) {
				long duration = event.getDuration().toMillis();
				long start = this.jvmStartTime != -1 ? (event.getStartTime().toEpochMilli() - this.jvmStartTime) : -1L; // translated to ms since JVM start
				long end = this.jvmStartTime != -1 ? (start + duration) : -1L;
				
				JFRLastGarbageCollection lastOld = gc.getOldLastGC();
				lastOld.setGCId(event.getInt(JFR_FIELD_GC_ID));
				lastOld.setDuration(duration);
				lastOld.setStartTime(start);
				lastOld.setEndTime(end);
			}
			context.last = event;
		}

		// associate it to New GC ids
		context.reset();
		for (JFRThreadDump dump : this.dumps) {
			JFRGarbageCollection gc = dump.getGarbageCollection();
			RecordedEvent event = getClosestGarbageCollectionEvent(dump, events, context,
					JFRGarbageCollection.getNewGCTypes());
			if (event != null) {
				long duration = event.getDuration().toMillis();
				long start = this.jvmStartTime != -1 ? (event.getStartTime().toEpochMilli() - this.jvmStartTime) : -1L; // translated to ms since JVM start
				long end = this.jvmStartTime != -1 ? (start + duration) : -1L;

				JFRLastGarbageCollection lastNew = gc.getNewLastGC();
				lastNew.setGCId(event.getInt(JFR_FIELD_GC_ID));
				lastNew.setDuration(duration);
				lastNew.setStartTime(start);
				lastNew.setEndTime(end);
			}
		}
	}

	private void enrichLastGCFigures(JFRDescriptor descriptor) {
		for (JFRThreadDump dump : this.dumps) {
			JFRGarbageCollection gc = dump.getGarbageCollection();
			enrichLastGCFigures(descriptor, gc, gc.getNewLastGC());
			enrichLastGCFigures(descriptor, gc, gc.getOldLastGC());
		}
	}

	private void enrichLastGCFigures(JFRDescriptor descriptor, JFRGarbageCollection gc,
			JFRLastGarbageCollection lastGC) {
		if (gc.isG1GarbageCollector())
			enrichG1LastGCFigures(descriptor, lastGC);
		else if (gc.isPSGarbageCollector())
			enrichPSLastGCFigures(descriptor, lastGC);
		else if (gc.isSerialGarbageCollector())
			enrichSerialLastGCFigures(descriptor, lastGC);
		else
			logger.warn("GC figures extraction skipped as the GC type is not recognized");
	}

	private void enrichSerialLastGCFigures(JFRDescriptor descriptor, JFRLastGarbageCollection lastGC) {
		enrichHeapLastGCFigures(descriptor, lastGC, false); 
		
		// old and young not supported
		lastGC.setEdenUsedBeforeGC(-1L);
		lastGC.setOldUsedBeforeGC(-1L);
		lastGC.setEdenMaxBeforeGC(-1L);
		lastGC.setEdenCommittedBeforeGC(-1L);
		lastGC.setOldMaxBeforeGC(-1L);
		lastGC.setOldCommittedBeforeGC(-1L);
		
		lastGC.setEdenUsedAfterGC(-1L);
		lastGC.setOldUsedAfterGC(-1L);
		lastGC.setEdenMaxAfterGC(-1L);
		lastGC.setEdenCommittedAfterGC(-1L);
		lastGC.setOldMaxAfterGC(-1L);
		lastGC.setOldCommittedAfterGC(-1L);
	}

	private void enrichPSLastGCFigures(JFRDescriptor descriptor, JFRLastGarbageCollection lastGC) {
		// Heap
		enrichHeapLastGCFigures(descriptor, lastGC, false);

		// Young and Old - Before
		List<RecordedEvent> events = descriptor.getPSHeapSummaryEvents();
		events.sort(eventTimeComparator);

		RecordedEvent event = getEventbyGCId(events, lastGC.getGCId(), JFRGarbageCollection.JFR_BEFORE_GC,
				JFRReader.JFR_JDK_PSHEAPSUMMARY);
		if (event != null) {
			RecordedObject edenSpaceEvent = event.getValue(EDEN_SPACE_FIELD);
			long youngUsed = edenSpaceEvent.getLong(JFR_USED);

			RecordedObject fromSpaceEvent = event.getValue(FROM_SPACE_FIELD);
			youngUsed += fromSpaceEvent.getLong(JFR_USED);

			RecordedObject toSpaceEvent = event.getValue(TO_SPACE_FIELD);
			youngUsed += toSpaceEvent.getLong(JFR_USED);

			lastGC.setEdenUsedBeforeGC(youngUsed);

			RecordedObject oldObjectSpaceEvent = event.getValue(OLD_OBJECT_SPACE_FIELD);
			lastGC.setOldUsedBeforeGC(oldObjectSpaceEvent.getLong(JFR_USED));

			RecordedObject youngSpaceEvent = event.getValue(YOUNG_SPACE_FIELD);
			lastGC.setEdenMaxBeforeGC(youngSpaceEvent.getLong(JFR_RESERVED_SIZE));
			lastGC.setEdenCommittedBeforeGC(youngSpaceEvent.getLong(JFR_COMMITTED_SIZE));

			RecordedObject oldSpaceEvent = event.getValue(OLD_SPACE_FIELD);
			lastGC.setOldMaxBeforeGC(oldSpaceEvent.getLong(JFR_RESERVED_SIZE));
			lastGC.setOldCommittedBeforeGC(oldSpaceEvent.getLong(JFR_COMMITTED_SIZE));
		}

		// Young and Old - After
		events = descriptor.getPSHeapSummaryEvents();
		events.sort(eventTimeComparator);

		event = getEventbyGCId(events, lastGC.getGCId(), JFRGarbageCollection.JFR_AFTER_GC,
				JFRReader.JFR_JDK_PSHEAPSUMMARY);
		if (event != null) {
			RecordedObject edenSpaceEvent = event.getValue(EDEN_SPACE_FIELD);
			long youngUsed = edenSpaceEvent.getLong(JFR_USED);

			RecordedObject fromSpaceEvent = event.getValue(FROM_SPACE_FIELD);
			youngUsed += fromSpaceEvent.getLong(JFR_USED);

			RecordedObject toSpaceEvent = event.getValue(TO_SPACE_FIELD);
			youngUsed += toSpaceEvent.getLong(JFR_USED);

			lastGC.setEdenUsedAfterGC(youngUsed);

			RecordedObject oldObjectSpaceEvent = event.getValue(OLD_OBJECT_SPACE_FIELD);
			lastGC.setOldUsedAfterGC(oldObjectSpaceEvent.getLong(JFR_USED));

			RecordedObject youngSpaceEvent = event.getValue(YOUNG_SPACE_FIELD);
			lastGC.setEdenMaxAfterGC(youngSpaceEvent.getLong(JFR_RESERVED_SIZE));
			lastGC.setEdenCommittedAfterGC(youngSpaceEvent.getLong(JFR_COMMITTED_SIZE));

			RecordedObject oldSpaceEvent = event.getValue(OLD_SPACE_FIELD);
			lastGC.setOldMaxAfterGC(oldSpaceEvent.getLong(JFR_RESERVED_SIZE));
			lastGC.setOldCommittedAfterGC(oldSpaceEvent.getLong(JFR_COMMITTED_SIZE));
		}
	}

	private void enrichG1LastGCFigures(JFRDescriptor descriptor, JFRLastGarbageCollection lastGC) {
		// Start from heap
		enrichHeapLastGCFigures(descriptor, lastGC, true);

		// Then process young G1
		List<RecordedEvent> events = descriptor.getG1HeapSummaryEvents();
		events.sort(eventTimeComparator);

		RecordedEvent event = getEventbyGCId(events, lastGC.getGCId(), JFRGarbageCollection.JFR_BEFORE_GC,
				JFRReader.JFR_JDK_G1HEAPSUMMARY);
		if (event != null) {
			lastGC.setEdenUsedBeforeGC(event.getLong(JFR_EDEN_USED_SIZE));
			lastGC.setEdenMaxBeforeGC(event.getLong(JFR_EDEN_TOTAL_SIZE));
		}

		event = getEventbyGCId(events, lastGC.getGCId(), JFRGarbageCollection.JFR_AFTER_GC,
				JFRReader.JFR_JDK_G1HEAPSUMMARY);
		if (event != null) {
			lastGC.setEdenUsedAfterGC(event.getLong(JFR_EDEN_USED_SIZE));
			lastGC.setEdenMaxAfterGC(event.getLong(JFR_EDEN_TOTAL_SIZE));
		}

		// And deduce the Old figures
		lastGC.updateOldFigures();
	}

	private void enrichHeapLastGCFigures(JFRDescriptor descriptor, JFRLastGarbageCollection lastGC, boolean g1Type) {
		List<RecordedEvent> events = descriptor.getGCHeapSummaryEvents();
		events.sort(eventTimeComparator);

		RecordedEvent event = getEventbyGCId(events, lastGC.getGCId(), JFRGarbageCollection.JFR_BEFORE_GC,
				JFRReader.JFR_JDK_GCHEAPSUMMARY);
		if (event != null) {
			lastGC.setHeapUsedBeforeGC(event.getLong(JFR_HEAP_USED));
			RecordedObject heapSpaceEvent = event.getValue(JFR_HEAP_SPACE);
			lastGC.setHeapMaxBeforeGC(heapSpaceEvent.getLong(JFR_RESERVED_SIZE));
			if (g1Type) {
				lastGC.setEdenCommittedBeforeGC(-1);
				lastGC.setOldCommittedBeforeGC(heapSpaceEvent.getLong(JFR_COMMITTED_SIZE));
			}
		}

		event = getEventbyGCId(events, lastGC.getGCId(), JFRGarbageCollection.JFR_AFTER_GC,
				JFRReader.JFR_JDK_GCHEAPSUMMARY);
		if (event != null) {
			lastGC.setHeapUsedAfterGC(event.getLong(JFR_HEAP_USED));
			RecordedObject heapSpaceEvent = event.getValue(JFR_HEAP_SPACE);
			lastGC.setHeapMaxAfterGC(heapSpaceEvent.getLong(JFR_RESERVED_SIZE));
			if (g1Type) {
				lastGC.setEdenCommittedAfterGC(-1);
				lastGC.setOldCommittedAfterGC(heapSpaceEvent.getLong(JFR_COMMITTED_SIZE));
			}
		}
	}

	private void enrichG1PeakFigures(JFRDescriptor descriptor) {
		List<RecordedEvent> heapEvents = descriptor.getGCHeapSummaryEvents();
		if (heapEvents.isEmpty())
			return;
		heapEvents.sort(eventTimeComparator);

		List<RecordedEvent> youngEvents = descriptor.getG1HeapSummaryEvents();
		if (youngEvents.isEmpty())
			return;
		youngEvents.sort(eventTimeComparator);

		enrichG1PeakUsedFigures(heapEvents, youngEvents, JFRGarbageCollection.JFR_BEFORE_GC);
		enrichG1PeakUsedFigures(heapEvents, youngEvents, JFRGarbageCollection.JFR_AFTER_GC);
	}

	private void enrichG1PeakUsedFigures(List<RecordedEvent> heapEvents, List<RecordedEvent> youngEvents, String when) {
		long usedPeakOld = 0; // can be before or after, incremental
		long usedPeakYoung = 0; // can be before or after, incremental

		// Old and young before
		int eventPos = 0;
		boolean lastEventReached = false;
		RecordedEvent event = heapEvents.get(eventPos);
		for (JFRThreadDump dump : this.dumps) {
			long usedHeap = 0;
			long usedYoung = 0;
			while (event.getStartTime().isBefore(dump.getInstant()) && !lastEventReached) {
				usedHeap = event.getLong(JFR_HEAP_USED);
				RecordedEvent youngEvent = getEventbyGCId(youngEvents, event.getInt(JFR_FIELD_GC_ID), when,
						JFRReader.JFR_JDK_G1HEAPSUMMARY);
				if (youngEvent != null) {
					usedYoung = youngEvent.getLong(JFR_EDEN_USED_SIZE);
					if (usedHeap - usedYoung > usedPeakOld)
						usedPeakOld = usedHeap - usedYoung;
					if (usedYoung > usedPeakYoung)
						usedPeakYoung = usedYoung;
				}
				eventPos++;
				if (eventPos == heapEvents.size()) {
					lastEventReached = true;
					break;
				}
				event = heapEvents.get(eventPos);
			}
			JFRGarbageCollection gc = dump.getGarbageCollection();
			gc.setOldUsedPeak(usedPeakOld);
			gc.setEdenUsedPeak(usedPeakYoung);
		}
	}

	private void enrichPSPeakFigures(JFRDescriptor descriptor) {
		List<RecordedEvent> psEvents = descriptor.getPSHeapSummaryEvents();
		if (psEvents.isEmpty())
			return;
		psEvents.sort(eventTimeComparator);

		enrichPSPeakUsedFigures(psEvents);
	}

	private void enrichPSPeakUsedFigures(List<RecordedEvent> psEvents) {
		long usedPeakOld = 0; // can be before or after, incremental
		long usedPeakYoung = 0; // can be before or after, incremental

		// Old and young before
		int eventPos = 0;
		boolean lastEventReached = false;
		RecordedEvent event = psEvents.get(eventPos);
		for (JFRThreadDump dump : this.dumps) {
			while (event.getStartTime().isBefore(dump.getInstant()) && !lastEventReached) {
				RecordedObject edenSpaceEvent = event.getValue(EDEN_SPACE_FIELD);
				long usedYoung = edenSpaceEvent.getLong(JFR_USED);

				RecordedObject fromSpaceEvent = event.getValue(FROM_SPACE_FIELD);
				usedYoung += fromSpaceEvent.getLong(JFR_USED);

				RecordedObject toSpaceEvent = event.getValue(TO_SPACE_FIELD);
				usedYoung += toSpaceEvent.getLong(JFR_USED);

				if (usedYoung > usedPeakYoung)
					usedPeakYoung = usedYoung;

				RecordedObject oldObjectSpaceEvent = event.getValue(OLD_OBJECT_SPACE_FIELD);
				long usedOld = oldObjectSpaceEvent.getLong(JFR_USED);
				if (usedOld > usedPeakOld)
					usedPeakOld = usedOld;

				eventPos++;
				if (eventPos == psEvents.size()) {
					lastEventReached = true;
					break;
				}
				event = psEvents.get(eventPos);
			}
			JFRGarbageCollection gc = dump.getGarbageCollection();
			gc.setOldUsedPeak(usedPeakOld);
			gc.setEdenUsedPeak(usedPeakYoung);
		}
	}

	private String extractGCName(JFRDescriptor descriptor, String type) {
		List<RecordedEvent> events = descriptor.getGCConfigurationEvents();
		if (events.isEmpty())
			return null;
		return events.get(0).getString(type);
	}

	private RecordedEvent getClosestEvent(JFRThreadDump dump, List<RecordedEvent> events, EventBrowseContext context) {
		if (events == null)
			return context.last;

		while (context.pos < events.size()) {
			RecordedEvent event = events.get(context.pos);
			if (isEventBefore(event, dump)) {
				context.last = event;
				context.incrementPosition();
			} else {
				break;
			}
		}

		return context.last;
	}

	private RecordedEvent getClosestEvent(JFRThreadDump dump, List<RecordedEvent> events) {
		if (events == null)
			return null;

		int pos = 0;
		RecordedEvent candidate = null;
		while (pos < events.size()) {
			RecordedEvent event = events.get(pos);
			if (isEventBefore(event, dump)) {
				candidate = event;
				pos++;
			} else {
				break;
			}
		}

		return candidate;
	}

	private RecordedEvent getEventbyGCId(List<RecordedEvent> events, int gcId, String when, String eventType) {
		for (RecordedEvent event : events)
			if (event.getInt(JFR_FIELD_GC_ID) == gcId && when.equals(event.getString(JFR_FIELD_WHEN)))
				return event;

		logger.warn(eventType + " event not found for GC id : " + gcId + " and when : " + when);
		return null;
	}

	private RecordedEvent getClosestGarbageCollectionEvent(JFRThreadDump dump, List<RecordedEvent> events,
			EventBrowseContext context, List<String> gcTypes) {
		while (context.pos < events.size()) {
			RecordedEvent event = events.get(context.pos);
			if (isEventBefore(event, dump) && gcTypes.contains(event.getString(JFR_FIELD_NAME))) {
				context.last = event;
				context.incrementPosition();
			} else {
				if (event.getStartTime().isAfter(dump.getInstant()))
					break;
				else
					context.incrementPosition();
			}
		}

		return context.last;
	}

	private boolean isEventBefore(RecordedEvent event, JFRThreadDump dump) {
		return event.getStartTime().isBefore(dump.getInstant()) || event.getStartTime().equals(dump.getInstant());
	}

	private List<File> writeSnapshots(JFRDescriptor descriptor, String outputDir) throws JzrTranslatorException {
		List<File> jzrSnapshots = new ArrayList<>();

		for (JFRThreadDump dump : dumps) {
			File jzrSnapshot = new File(outputDir + File.separator
					+ getTimeStampedFileName(descriptor, JZR_FILE_JZR_PREFIX, dump.getDate(), JZR_FILE_JZR_EXTENSION));
			jzrSnapshots.add(jzrSnapshot);
			try (FileOutputStream fos = new FileOutputStream(jzrSnapshot);
					OutputStreamWriter osw = new OutputStreamWriter(fos);
					PrintWriter writer = new PrintWriter(osw);) {
				logger.info("Uncompressing JFR file - creating JZR recording file : {}", jzrSnapshot.getAbsolutePath());
				writer.write("JFR Recording" + System.lineSeparator());
				dump.write(descriptor, writer);
				writer.flush();
			} catch (IOException ex) {
				throw new JzrTranslatorException("Failed to write the JZR recording file.", ex);
			}
		}

		return jzrSnapshots;
	}

	private String getTimeStampedFileName(JFRDescriptor descriptor, String prefix, Date dateStamp, String extension) {
		// format is thread-dump-<P or JZR or JFR>-<yyyy-MM-dd---HH-mm-ss-SSS-z>.txt
		StringBuilder name = new StringBuilder(prefix);
		TimeZone timeZoneId;

		TimeZoneOrigin origin = jfrCfg.getTimeZoneOrigin();
		
		// Time zone may be available in the JFR recording (process origin) or specified by configuration (custom origin)
		// Otherwise use the JFR recording time zone which is always UTC (http://hirt.se/blog/?p=185)
		if (TimeZoneOrigin.PROCESS.equals(origin) && descriptor.getTimeZoneId() != null && TimeZoneInfoHelper.isValidTimeZone(descriptor.getTimeZoneId())) {
			name.append("p-");
			timeZoneId = TimeZone.getTimeZone(descriptor.getTimeZoneId());
		}
		else if (TimeZoneOrigin.CUSTOM.equals(origin)) {
			name.append("c-");
			timeZoneId = this.jfrCfg.getCustomTimeZone();
		}
		else {
			name.append("jfr-");
			timeZoneId = TimeZone.getTimeZone("UTC");
		}
		name.append(getTimeStamp(dateStamp, JZR_FILE_DATE_FORMAT, timeZoneId));
		name.append(extension);
		return name.toString();
	}

	private String getTimeStamp(Date dateStamp, String format, TimeZone timeZone) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(timeZone);
		String result = sdf.format(dateStamp) + timeZone.getID();
		return result.replace(':', '@').replace('/','$'); // secure the time zone chars
	}

	private static class EventBrowseContext {
		private int pos = 0;
		private RecordedEvent last = null;

		public void reset() {
			this.pos = 0;
			this.last = null;
		}

		public void incrementPosition() {
			this.pos++;
		}
	}
}
