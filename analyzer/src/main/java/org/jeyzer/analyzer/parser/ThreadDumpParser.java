package org.jeyzer.analyzer.parser;

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






import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.error.JzrMultipleParsingException;
import org.jeyzer.analyzer.error.JzrParsingException;
import org.jeyzer.analyzer.parser.io.ThreadDumpFileDateHelper;
import org.jeyzer.analyzer.parser.io.SnapshotFileNameFilter;
import org.jeyzer.analyzer.util.AnalyzerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class ThreadDumpParser {

	private static final Logger logger = LoggerFactory.getLogger(ThreadDumpParser.class);		
	
	private static final String WEBLO_THREAD_NAME = "ExecuteThread:";

	protected static final String VIRTUAL_THREAD_CARRIER_CODE_SIGNATURE = "java.lang.VirtualThread.runContinuation";
	protected static final String VIRTUAL_THREAD_UNMOUNTED_CODE_SIGNATURE = "java.lang.VirtualThread.yieldContinuation";
	protected static final String VIRTUAL_THREAD_CODE_SIGNATURE = "java.lang.VirtualThread$VThreadContinuation.lambda$new";
	
	protected static final String EMPTY_STRING = "";

	// synchronized access
	protected List<ThreadDump> dumps = Collections.synchronizedList(new ArrayList<ThreadDump>());
	protected Map<String, Exception> parsingErrors = Collections.synchronizedMap(new HashMap<String, Exception>());

	// guarded by class instance
	private static int nextId = 0;

	private static int getNextId() {
		synchronized (ThreadDumpParserWorkerFactory.class) {
			return nextId++;
		}
	}

	public class ThreadDumpParserWorkerFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("Jeyzer-TD-parser #" + getNextId());
			return t;
		}
	}
	
	public static class ThreadDumpParserWorker implements Runnable{

		private ThreadDumpParser parser;
		private File file;
		private Date fileDate;
		
		public ThreadDumpParserWorker(ThreadDumpParser parser, File file, Date date){
			this.parser = parser;
			this.file = file;
			this.fileDate = date;
		}
		
		@Override
		public void run() {
			parser.parseThreadDump(file, fileDate);
		}
		
	}
	
	public abstract void parseThreadDump(File file, Date date);	
	
	public abstract ThreadStack parseThreadStack(StackContext context)  throws ParseException;
	
	public abstract String getFormatName();
	
	public abstract String getFormatShortName();
	
	public abstract boolean isLockIdUsed();

	public abstract boolean isCPUMeasurementUsed();
	
	public abstract boolean isMemoryMeasurementUsed();
	
	public abstract boolean isProcessUpTimeMeasurementUsed();
	
	public abstract boolean isDiskWriteTimeMeasurementUsed();
	
	public abstract boolean isGarbageCollectionMeasurementUsed();
	
	public abstract boolean isOpenFileDescriptorMeasurementUsed();
	
	public abstract boolean isDeadlockUsed();
	
	public abstract boolean isLockCycleDetectionUsed();
	
	public abstract boolean isBiasedLockUsed();
	
	public abstract boolean isSuspendedUsed();
	
	public abstract boolean isJeyzerMXUsed();
	
	// Parser compatible with JDK 17+, able at least to parse carrier threads
	public abstract boolean hasVirtualThreadSupport();
	
	public abstract boolean areVirtualThreadVariationCountersUsed();
	
	public abstract boolean hasVirtualThreadStackSupport();
	
	/**
	 * Parse thread name
	 * 
	 * Thread name : "http-443-5"
	 */
	protected String parseName(String header){
		String name;
		int pos = header.indexOf('\"',1);
		
		if (header.length() > 2000)
			// Exotic case where thread name is larger than 2000 chars : the closing " is then missing
			// Example : "thread name[...]-501187129.p#109 prio=5 
			pos = pos == -1 ? 1999 : pos;
		
		name = header.substring(1, pos);
		
		// Weblogic thread name : "[ACTIVE] ExecuteThread: '0' for queue: 'weblogic.kernel.Default (self-tuning)'"
		// keep ExecuteThread: '0'
		pos = name.indexOf(WEBLO_THREAD_NAME,1);
		if (pos != -1){
			int endPos = name.indexOf("for queue", pos + WEBLO_THREAD_NAME.length()) - 1;
			name = name.substring(pos, endPos);
		}
		
		return name.intern();
	}
	
	public List<ThreadDump> parseThreadDumpFiles(File[] files, SnapshotFileNameFilter filter, Date sinceDate) throws JzrParsingException
	{
		List<ThreadDump> result = new ArrayList<>();
		long startTime = 0;

		if (logger.isDebugEnabled())
			startTime = System.currentTimeMillis();
		
		try{
			int cpuCount = Runtime.getRuntime().availableProcessors();
			ExecutorService executor = Executors.newFixedThreadPool(cpuCount, 
					new ThreadDumpParserWorkerFactory());
			
			// empty the lists
			dumps.clear();
			parsingErrors.clear();
			
			List<File> filteredFiles = filterAndValidateFiles(filter, files, sinceDate);
			
			for (File file : filteredFiles){
				Date date = ThreadDumpFileDateHelper.getFileDate(filter, file);
				
				// process in parallel the thread dump parsing
				Runnable worker = new ThreadDumpParserWorker(this, file, date);
				executor.execute(worker);
			}
			executor.shutdown();
			
			// wait for thread termination
			while (!executor.isTerminated()){
			}
			
			if (!parsingErrors.isEmpty()){
				// Log all parsing errors
				Set<Entry<String, Exception>> entries = parsingErrors.entrySet();
				Iterator<Entry<String, Exception>> iter = entries.iterator();
				JzrMultipleParsingException ex = new JzrMultipleParsingException();
				
				while(iter.hasNext()){
					Entry<String, Exception> entry = iter.next();
					logger.warn("Parsing failed for file : " + entry.getKey() + ". Error is : " + entry.getValue().getMessage());
					ex.addException(entry.getValue());
				}
				
				throw ex;
			}

			result.addAll(dumps);
			
		}finally{
			// release resources
			dumps.clear();
			parsingErrors.clear();
		}
		
		if (logger.isDebugEnabled()){
			long endTime = System.currentTimeMillis();
			logger.debug("Parsing took {} ms", endTime-startTime);
		}
		
		return result;
	}

	private List<File> filterAndValidateFiles(SnapshotFileNameFilter filter, File[] files, Date sinceDate) throws JzrParsingException {
		List<File> filteredFiles = new ArrayList<>();
		Map<Date, File> filesPerDate = new HashMap<>();
		
		for (int i=0; i < files.length; i++){
			File file = files[i];
			
			// skip files before sinceDate
			Date date = ThreadDumpFileDateHelper.getFileDate(filter, file);
			if (date.compareTo(sinceDate)<=0)
				continue;
			
			// ignore empty files, will be processed as missing thread dumps
			if (file.length() == 0){
				logger.warn("Thread dump file is empty : {}", file.getName());
				continue;
			}
			
			// detect file duplicates, only for recording snapshots
			if (!AnalyzerHelper.isRecordingStaticFile(file)) {
				if (filesPerDate.containsKey(date)){
					File duplicate = filesPerDate.get(date);
					throw new JzrParsingException("Invalid thread dump file set : 2 files have the same time stamp.\n" 
							+ "File 1 : " + duplicate.getName()
							+ "\nFile 2 : " + file.getName());
				}
				filesPerDate.put(date, file);
			}
			
			filteredFiles.add(file);
		}
		
		return filteredFiles;
	}
}
