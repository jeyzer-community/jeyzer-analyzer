package org.jeyzer.analyzer.input.translator.jfr.reader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jeyzer.analyzer.config.translator.jfr.ConfigJFRDecompression;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.error.JzrTranslatorException;
import org.jeyzer.analyzer.error.JzrTranslatorJFRThreadDumpEventNotFoundException;
import org.jeyzer.analyzer.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.jfr.EventType;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;

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


public class JFRReader {
	
	private static final Logger logger = LoggerFactory.getLogger(JFRReader.class);

	public static final String JFR_JDK_BOOLEANFLAG 						= "jdk.BooleanFlag";
	public static final String JFR_JDK_BOOLEANFLAGCHANGED				= "jdk.BooleanFlagChanged";
	private static final String JFR_JDK_CPUINFORMATION 					= "jdk.CPUInformation";
	private static final String JFR_JDK_CPULOAD 						= "jdk.CPULoad";
	public static final String JFR_JDK_DOUBLEFLAG 						= "jdk.DoubleFlag";
	public static final String JFR_JDK_DOUBLEFLAGCHANGED				= "jdk.DoubleFlagChanged";
	private static final String JFR_JDK_GARBAGECOLLECTION 				= "jdk.GarbageCollection";
	public  static final String JFR_JDK_GCCONFIGURATION 				= "jdk.GCConfiguration";
	public  static final String JFR_JDK_GCHEAPSUMMARY 					= "jdk.GCHeapSummary";
	public  static final String JFR_JDK_G1HEAPSUMMARY 					= "jdk.G1HeapSummary";
	private static final String JFR_JDK_INITIALENVIRONMENTVARIABLE 		= "jdk.InitialEnvironmentVariable";
	private static final String JFR_JDK_INITIALSYSTEMPROPERTY 			= "jdk.InitialSystemProperty";
	public static final String JFR_JDK_INTFLAG				 			= "jdk.IntFlag"; 
	public static final String JFR_JDK_INTFLAGCHANGED		 			= "jdk.IntFlagChanged";
	private static final String JFR_JDK_JVMINFORMATION 					= "jdk.JVMInformation";
	public static final String JFR_JDK_LONGFLAG				 			= "jdk.LongFlag"; 
	public static final String JFR_JDK_LONGFLAGCHANGED		 			= "jdk.LongFlagChanged"; 
	private static final String JFR_JDK_MODULEEXPORT 					= "jdk.ModuleExport";
	private static final String JFR_JDK_MODULEREQUIRE 					= "jdk.ModuleRequire";
	private static final String JFR_JDK_OLDGARBAGECOLLECTION 			= "jdk.OldGarbageCollection";
	private static final String JFR_JDK_OSINFORMATION 					= "jdk.OSInformation";
	private static final String JFR_JDK_PHYSICALMEMORY 					= "jdk.PhysicalMemory";
	public  static final String JFR_JDK_PSHEAPSUMMARY					= "jdk.PSHeapSummary";
	public static final String JFR_JDK_STRINGFLAG				 		= "jdk.StringFlag";
	public static final String JFR_JDK_STRINGFLAGCHANGED		 		= "jdk.StringFlagChanged";
	private static final String JFR_JDK_THREADALLOCATIONSTATISTICS 		= "jdk.ThreadAllocationStatistics";
	private static final String JFR_JDK_THREADCPULOAD 					= "jdk.ThreadCPULoad";
	private static final String JFR_JDK_THREADDUMP 						= "jdk.ThreadDump";
	private static final String JFR_JDK_THREADEND 						= "jdk.ThreadEnd";
	public static final String JFR_JDK_UNSIGNEDINTFLAG				 	= "jdk.UnsignedIntFlag";
	public static final String JFR_JDK_UNSIGNEDINTFLAGCHANGED		 	= "jdk.UnsignedIntFlagChanged";
	public static final String JFR_JDK_UNSIGNEDLONGFLAG				 	= "jdk.UnsignedLongFlag";
	public static final String JFR_JDK_UNSIGNEDLONGFLAGCHANGED		 	= "jdk.UnsignedLongFlagChanged";
	public static final String JFR_JDK_VIRTUALTHREADSTART		 		= "jdk.VirtualThreadStart";
	public static final String JFR_JDK_VIRTUALTHREADEND				 	= "jdk.VirtualThreadEnd";
	public static final String JFR_JDK_VIRTUALTHREADSUBMITFAILED		= "jdk.VirtualThreadSubmitFailed";
	public static final String JFR_JDK_VIRTUALTHREADPINNED				= "jdk.VirtualThreadPinned";
	private static final String JFR_JDK_YOUNGGARBAGECOLLECTION 			= "jdk.YoungGarbageCollection";
	
	private static final String LOG_JFR_NO_CONTAINS 					= "The JFR file contains no ";
	
	private ConfigJFRDecompression jfrCfg;
	
	public JFRReader(ConfigJFRDecompression jfrCfg) {
		this.jfrCfg = jfrCfg;
	}

	public JFRDescriptor load(String jfrPath) throws JzrTranslatorException {
		JFRDescriptor jfrDescriptor = new JFRDescriptor();
		
		if (this.jfrCfg.isJFRDataPerTypeDump() || this.jfrCfg.isAllJFRDataDump())
			logger.info("Recording JFR file dump is available in directory : " + SystemHelper.sanitizePathSeparators(this.jfrCfg.getDumpDirectory()));
		
		prepareJFRDirectory();
		
		List<String> eventNames = loadAndDumpEventTypes(jfrPath);
		jfrDescriptor.setLoadedEventTypes(eventNames);
		
		for (String eventTypeName : eventNames)
			loadAndDumpEvents(eventTypeName, jfrPath, jfrDescriptor);
		
		if (this.jfrCfg.isAllJFRDataDump())
			dumpAllEvents(jfrPath);
		
		validateEvents(jfrDescriptor);
		
		return jfrDescriptor;
	}

	private void validateEvents(JFRDescriptor jfrDescriptor) throws JzrTranslatorException {
		// Thread dumps must be there. Blocking otherwise
		if (!jfrDescriptor.hasThreadDumpEvents()) {
			logger.error("JFR file does not contain any thread dump event. Please use a JFR profile with periodic thread dumps (60s for example).");
			throw new JzrTranslatorJFRThreadDumpEventNotFoundException("JFR file does not contain any thread dump event. Please use a JFR profile with periodic thread dumps (60s for example).");
		}
		// GC names must be there
		if (!jfrDescriptor.containsEventType(JFR_JDK_GCCONFIGURATION)) {
			logger.warn("JFR GC data cannot be processed : " + JFRReader.JFR_JDK_GCCONFIGURATION + " events are missing.");
			jfrDescriptor.invalidateGCData();
		}
		
		// List missing events
		if (!jfrDescriptor.containsEventType(JFR_JDK_CPUINFORMATION))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_CPUINFORMATION + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_CPULOAD))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_CPULOAD + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_G1HEAPSUMMARY))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_G1HEAPSUMMARY + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_GARBAGECOLLECTION))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_GARBAGECOLLECTION + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_GCHEAPSUMMARY))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_GCHEAPSUMMARY + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_INITIALENVIRONMENTVARIABLE))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_INITIALENVIRONMENTVARIABLE + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_INITIALSYSTEMPROPERTY))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_INITIALSYSTEMPROPERTY + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_JVMINFORMATION))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_JVMINFORMATION + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_MODULEEXPORT))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_MODULEEXPORT + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_MODULEREQUIRE))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_MODULEREQUIRE + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_OLDGARBAGECOLLECTION))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_OLDGARBAGECOLLECTION + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_OSINFORMATION))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_OSINFORMATION + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_PHYSICALMEMORY))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_PHYSICALMEMORY + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_PSHEAPSUMMARY))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_PSHEAPSUMMARY + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_THREADALLOCATIONSTATISTICS))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_THREADALLOCATIONSTATISTICS + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_THREADEND))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_THREADEND + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_YOUNGGARBAGECOLLECTION))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_YOUNGGARBAGECOLLECTION + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_DOUBLEFLAG))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_DOUBLEFLAG + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_DOUBLEFLAGCHANGED))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_DOUBLEFLAGCHANGED + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_BOOLEANFLAG))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_BOOLEANFLAG + " events.");
		if (!jfrDescriptor.containsEventType(JFR_JDK_BOOLEANFLAGCHANGED))
			logger.info(LOG_JFR_NO_CONTAINS + JFR_JDK_BOOLEANFLAGCHANGED + " events.");
	}

	private void prepareJFRDirectory() throws JzrTranslatorException {
		if (!this.jfrCfg.isJFRDataPerTypeDump() && !this.jfrCfg.isAllJFRDataDump())
			return;
		
		String dumpPath = jfrCfg.getDumpDirectory();
		try {
			SystemHelper.createDirectory(dumpPath);
		} catch (JzrException ex) {
			throw new JzrTranslatorException("Failed to create the JFR dump directory : " + dumpPath); 
		}
		
		File dumpDir = new File(dumpPath);
		File[] dumpFiles = dumpDir.listFiles((File dir, String name) -> name.endsWith("-jfr.txt"));
		
		for (File dumpFile : dumpFiles)
			if (!dumpFile.delete())
				logger.warn("JFR dump directory cleanup - Failed to delete the old JFR dump file : " + dumpFile.getAbsolutePath());
	}

	private void loadAndDumpEvents(String eventTypeName, String jfrPath, JFRDescriptor jfrDescriptor) throws JzrTranslatorException {
		RecordingFile recordingFile = loadJFRFile(jfrPath);
		File eventFile = new File(jfrCfg.getDumpDirectory() + File.separator + eventTypeName + "-jfr.txt");
		
		if (this.jfrCfg.isJFRDataPerTypeDump()) {
			try (
					// output (optional)
					FileOutputStream fos = new FileOutputStream(eventFile);
					OutputStreamWriter osw = new OutputStreamWriter(fos);
					PrintWriter writer = new PrintWriter(osw);
				)
			{
				while (recordingFile.hasMoreEvents()) {
					RecordedEvent event = recordingFile.readEvent();
					if (event.getEventType().getName().equals(eventTypeName)) {
						writer.write(event.toString());
						loadEvent(event, jfrDescriptor);
					}
				}
			} catch (IOException ex) {
				throw new JzrTranslatorException("Failed to iterate over the JFR or write the JFR events.", ex);
			}
		}
		else {
			try {
				while (recordingFile.hasMoreEvents()) {
					RecordedEvent event = recordingFile.readEvent();
					if (event.getEventType().getName().equals(eventTypeName))
						loadEvent(event, jfrDescriptor);
				}				
			} catch (IOException ex) {
				throw new JzrTranslatorException("Failed to iterate over the JFR events.", ex);
			}
		}
	}

	private void loadEvent(RecordedEvent event, JFRDescriptor jfrDescriptor) {
		if (JFR_JDK_THREADDUMP.equals(event.getEventType().getName()))
			jfrDescriptor.addThreadDumpEvent(event);
		else if (JFR_JDK_VIRTUALTHREADSTART.equals(event.getEventType().getName()))
			jfrDescriptor.incrementVirtualThreadStart(jfrDescriptor, event.getStartTime());
		else if (JFR_JDK_VIRTUALTHREADEND.equals(event.getEventType().getName()))
			jfrDescriptor.incrementVirtualThreadEnd(jfrDescriptor, event.getStartTime());
//		else if (JFR_JDK_VIRTUALTHREADSUBMITFAILED.equals(event.getEventType().getName()))
//			logger.info("Virtual thread submit failed detected");
//		else if (JFR_JDK_VIRTUALTHREADPINNED.equals(event.getEventType().getName()))
//			logger.info("Virtual thread pinned detected");
		else if (JFR_JDK_INITIALSYSTEMPROPERTY.equals(event.getEventType().getName()))
			jfrDescriptor.addSystemPropertyEvent(event);
		else if (JFR_JDK_JVMINFORMATION.equals(event.getEventType().getName()))
			jfrDescriptor.addJVMInformation(event);
		else if (JFR_JDK_OSINFORMATION.equals(event.getEventType().getName()))
			jfrDescriptor.addOSInformation(event);
		else if (JFR_JDK_CPUINFORMATION.equals(event.getEventType().getName()))
			jfrDescriptor.addCPUInformation(event);
		else if (JFR_JDK_INITIALENVIRONMENTVARIABLE.equals(event.getEventType().getName()))
			jfrDescriptor.addInitialEnvironmentVariable(event);
		else if (JFR_JDK_MODULEEXPORT.equals(event.getEventType().getName()))
			jfrDescriptor.addModuleExport(event);
		else if (JFR_JDK_MODULEREQUIRE.equals(event.getEventType().getName()))
			jfrDescriptor.addModuleRequire(event);
		else if (JFR_JDK_CPULOAD.equals(event.getEventType().getName()))
			jfrDescriptor.addCPULoad(event);
		else if (JFR_JDK_PHYSICALMEMORY.equals(event.getEventType().getName()))
			jfrDescriptor.addSystemMemory(event);
		else if (JFR_JDK_YOUNGGARBAGECOLLECTION.equals(event.getEventType().getName()))
			jfrDescriptor.addYoungGarbageCollection(event);
		else if (JFR_JDK_OLDGARBAGECOLLECTION.equals(event.getEventType().getName()))
			jfrDescriptor.addOldGarbageCollection(event);
		else if (JFR_JDK_GCCONFIGURATION.equals(event.getEventType().getName()))
			jfrDescriptor.addGCConfiguration(event);
		else if (JFR_JDK_GCHEAPSUMMARY.equals(event.getEventType().getName()))
			jfrDescriptor.addGCHeapSummary(event);
		else if (JFR_JDK_G1HEAPSUMMARY.equals(event.getEventType().getName()))
			jfrDescriptor.addG1HeapSummary(event);
		else if (JFR_JDK_PSHEAPSUMMARY.equals(event.getEventType().getName()))
			jfrDescriptor.addPSHeapSummary(event);
		else if (JFR_JDK_GARBAGECOLLECTION.equals(event.getEventType().getName()))
			jfrDescriptor.addGarbageCollection(event);
		else if (JFR_JDK_THREADALLOCATIONSTATISTICS.equals(event.getEventType().getName()))
			jfrDescriptor.addThreadAllocationStatistics(event);
		else if (JFR_JDK_THREADCPULOAD.equals(event.getEventType().getName()))
			jfrDescriptor.addThreadCPULoad(event);
		else if (JFR_JDK_THREADEND.equals(event.getEventType().getName()))
			jfrDescriptor.addThreadEnd(event);
		else if (JFR_JDK_DOUBLEFLAG.equals(event.getEventType().getName())
				|| JFR_JDK_DOUBLEFLAGCHANGED.equals(event.getEventType().getName())
				|| JFR_JDK_BOOLEANFLAG.equals(event.getEventType().getName())
				|| JFR_JDK_BOOLEANFLAGCHANGED.equals(event.getEventType().getName())
				|| JFR_JDK_LONGFLAG.equals(event.getEventType().getName())
				|| JFR_JDK_LONGFLAGCHANGED.equals(event.getEventType().getName())
				|| JFR_JDK_INTFLAG.equals(event.getEventType().getName())
				|| JFR_JDK_INTFLAGCHANGED.equals(event.getEventType().getName())
				|| JFR_JDK_STRINGFLAG.equals(event.getEventType().getName())
				|| JFR_JDK_STRINGFLAGCHANGED.equals(event.getEventType().getName())
				|| JFR_JDK_UNSIGNEDINTFLAG.equals(event.getEventType().getName())
				|| JFR_JDK_UNSIGNEDINTFLAGCHANGED.equals(event.getEventType().getName())
				|| JFR_JDK_UNSIGNEDLONGFLAG.equals(event.getEventType().getName())
				|| JFR_JDK_UNSIGNEDLONGFLAGCHANGED.equals(event.getEventType().getName()
						)
				)
			jfrDescriptor.addJVMFlag(event);
	}

	private void dumpAllEvents(String jfrPath) throws JzrTranslatorException {
		File dumpFile = new File(this.jfrCfg.getDumpDirectory() + File.separator + "all-events-jfr.txt");
		RecordingFile recordingFile = loadJFRFile(jfrPath);
		try (
				// output
				FileOutputStream fos = new FileOutputStream(dumpFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				PrintWriter writer = new PrintWriter(osw);
			)
		{
			while (recordingFile.hasMoreEvents()) {
				RecordedEvent event = recordingFile.readEvent();
				writer.write(event.toString());
			}
		} catch (IOException ex) {
			throw new JzrTranslatorException("Failed to write into the JFR dump file : " + dumpFile.getAbsolutePath(), ex);
		}
	}

	private List<String> loadAndDumpEventTypes(String jfrPath) throws JzrTranslatorException {
		RecordingFile recordingFile = loadJFRFile(jfrPath);
		
		if (this.jfrCfg.isJFRDataPerTypeDump()) {
			File dumpFile = new File(this.jfrCfg.getDumpDirectory() + File.separator + "eventTypes-jfr.txt");
			try (
					// output
					FileOutputStream fos = new FileOutputStream(dumpFile);
					OutputStreamWriter osw = new OutputStreamWriter(fos);
					PrintWriter writer = new PrintWriter(osw);
				)
			{
					for (EventType eventType : recordingFile.readEventTypes())
						writer.println(eventType.getName());
			} catch (IOException ex) {
				throw new JzrTranslatorException("Failed to write into the JFR dump file : " + dumpFile.getAbsolutePath(), ex);
			}
		}

		// Get only the type names which have events in the recording (optimization)
		List<String> eventTypeNames = new ArrayList<>();
		try {
			while (recordingFile.hasMoreEvents()) {
				RecordedEvent event = recordingFile.readEvent();
				if (!eventTypeNames.contains(event.getEventType().getName())) {
					if (JFR_JDK_THREADDUMP.equals(event.getEventType().getName()))
						// Must be put first to allow cumulative and dependent event processing (cf. virtual thread events)
						eventTypeNames.add(0, event.getEventType().getName());
					else 
						eventTypeNames.add(event.getEventType().getName());					
				}
			}
		} catch (IOException ex) {
			throw new JzrTranslatorException("Failed to iterate over the JFR events.", ex);
		}
		
		return eventTypeNames;
	}
	
	private RecordingFile loadJFRFile(String jfrPath) throws JzrTranslatorException {
		try {
			Path path =  Paths.get(jfrPath);
			return new RecordingFile(path);			
		}
		catch(Exception ex) {
			logger.error("Failed to open the JFR recording.", ex);
			throw new JzrTranslatorException("Failed to open the JFR recording.");
		}
	}
}
