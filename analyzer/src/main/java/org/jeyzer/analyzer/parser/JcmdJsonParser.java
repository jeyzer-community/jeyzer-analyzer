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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStackImpl;
import org.jeyzer.analyzer.data.stack.ThreadState;
import org.jeyzer.analyzer.data.virtual.VirtualStackBuilder;
import org.jeyzer.analyzer.error.JzrParsingException;
import org.jeyzer.analyzer.setup.JzrSetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;


public class JcmdJsonParser extends ThreadDumpParser {

	// format name
	public static final String FORMAT_NAME = "Jcmd JSON tool";
	public static final String FORMAT_SHORT_NAME = "Jcmd JSON";

	private static final Logger logger = LoggerFactory.getLogger(JcmdJsonParser.class);

	protected static final String SECOND_LINE = "\"threadDump\": {";
	public static final String FOURTH_LINE = "\"time\": \"";
	
	// Markers
	public static final String MARKER_THREAD_DUMP = "threadDump";
	public static final String MARKER_CONTAINERS = "threadContainers";
	
	public static final String MARKER_CONTAINER = "container";
	public static final String MARKER_THREADS = "threads";
	
	public static final String MARKER_TID = "tid";
	public static final String MARKER_NAME = "name";
	public static final String MARKER_STACK = "stack";
	
	// Must not be updated
	public static final List<String> EMPTY_OWNED_LOCKS = new ArrayList<>();
	
	// 2023-07-28T11:33:35.254146100Z
	public static final String DATE_FORMAT = "yyyy-MM-ddHH:mm:ss";
	
	private VirtualStackBuilder vtBuilder = new VirtualStackBuilder();	
	
	public JcmdJsonParser(JzrSetupManager setupMgr) {
	}

	@Override
	public String getFormatName() {
		return FORMAT_NAME;
	}

	@Override
	public String getFormatShortName() {
		return FORMAT_SHORT_NAME;
	}
	
	@Override
	public void parseThreadDump(File file, Date date) {
		logger.info("Reading json thread dump file : {}", file.getName());

		boolean success = true;
		ThreadDump dump = new ThreadDump(file, date);
		
		try (
				FileReader fr = new FileReader(file);
				JsonReader reader = new JsonReader(fr))
		{
			reader.beginObject(); //  ThreadDump object
			
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (MARKER_THREAD_DUMP.equals(name)) {
					reader.beginObject();
					readThreadDump(reader, dump);
					reader.endObject();
				}
				else {
					// Should not happen
					logger.warn("Json parser - Unknown thread attribute : " + name);
					reader.skipValue();
				}				
			}
			
			reader.endObject(); //  ThreadDump object

		} catch (FileNotFoundException ex) {
			success = false;
			logger.error("Failed to open {}", file.getAbsolutePath(), ex);
			this.parsingErrors.put(file.getName(), ex);
		} catch (Exception e) {
			success = false;
			logger.error("Invalid json thread dump {}", file.getName(), e);
			JzrParsingException ex = new JzrParsingException(file.getName(), e);
			this.parsingErrors.put(file.getName(), ex);
		}

		if (success)
			dumps.add(dump);
	}

	private void readThreadDump(JsonReader reader, ThreadDump dump) throws JzrParsingException, IOException {
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (MARKER_CONTAINERS.equals(name)) {
				reader.beginArray();
				readContainer(reader, dump);
				reader.endArray();
			}
			else {
				// "processId": "47604",
				// "time": "2023-08-13T06:28:55.329818700Z",
				// "runtimeVersion": "20.0.1+9-29",					
				 reader.skipValue();
			}
		}
	}

	private void readContainer(JsonReader reader, ThreadDump dump) throws IOException, JzrParsingException {
		while (reader.hasNext()) {
			String container = null;
			reader.beginObject();
			while (reader.hasNext()) {

				String name = reader.nextName();
				if (MARKER_THREADS.equals(name)) {
					reader.beginArray();
					readThread(reader, dump, container);
					reader.endArray();
				}
				else if (MARKER_CONTAINER.equals(name)) {
					container = reader.nextString();
				}
				else {
					// "container": "ForkJoinPool.commonPool\/jdk.internal.vm.SharedThreadContainer@4b84481",
					// "parent": "<root>",
					// "owner": null,
					// "threadCount": "11"
					reader.skipValue();
				}
			}
			reader.endObject();
		}
	}

	private void readThread(JsonReader reader, ThreadDump dump, String container) throws IOException, JzrParsingException {
		while (reader.hasNext()) {
			String id = null;
			String threadName = null;
			List<String> codeLines = null;
			
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				if (MARKER_TID.equals(name)) {
					id = reader.nextString();
				}
				else if (MARKER_NAME.equals(name)) {
					threadName = reader.nextString();
				}
				else if (MARKER_STACK.equals(name)) {
					reader.beginArray();
					codeLines = readCodeLines(reader);
					reader.endArray();
				}
				else {
					// Should not happen
					logger.warn("Json parser - Unknown thread attribute : " + name);
					reader.skipValue();
				}
			}
			reader.endObject();
			
			if (id != null && threadName != null && codeLines != null) {
				
				if (!codeLines.isEmpty())
					createThreadStackImpl(dump, id, threadName, codeLines, container);
				else
					if (logger.isDebugEnabled())
						logger.debug("Json parser - thread " + threadName + " has no stack");
			}
			else {
				throw new JzrParsingException("Invalid json content : one of the thread attributes is missing");
			}
		}
	}

	private String buildDefaultThreadName(String container) {
		if (container == null || container.isEmpty())
			return "Unknown";
		
		// do some name cleanup
		// Examples :
		//  ForkJoinPool.commonPool/jdk.internal.vm.SharedThreadContainer@4b84481
		//  java.util.concurrent.ScheduledThreadPoolExecutor@6d03e736/jdk.internal.vm.SharedThreadContainer@6b343fdd
		String name;
		int pos = container.indexOf('/');
		if (pos != -1)
			name = container.substring(0,pos);
		else {
			pos = container.indexOf('@');
			if (pos != -1)
				name = container.substring(0,pos);
			else
				name = container;
		}
		
		// remove any package name
		pos = name.lastIndexOf('.');
		if (pos != -1)
			name = name.substring(pos+1);
		
		return name;
	}

	private List<String> readCodeLines(JsonReader reader) throws IOException {
		List<String> codeLines = new ArrayList<String>();
		
		while (reader.hasNext()) {
			codeLines.add(reader.nextString());
		}
		
		return codeLines;
	}

	private void createThreadStackImpl(ThreadDump dump, String id, String threadName, List<String> codeLines, String container) {
		boolean virtual = detectVirtualThread(threadName, codeLines);			
		
		codeLines = internCodeLines(codeLines);
		
		// Detect the virtual thread carriers based on code (no other way here)
		//  Same for the unmounted threads
		ThreadState state = ThreadState.UNKNOWN;
		if (codeLines.size() >= 2) {
			if (codeLines.get(1).contains(VIRTUAL_THREAD_CARRIER_CODE_SIGNATURE))
				state = ThreadState.CARRYING_VIRTUAL_THREAD;
			else if (codeLines.get(1).contains(VIRTUAL_THREAD_UNMOUNTED_CODE_SIGNATURE))
				state = ThreadState.UNMOUNTED_VIRTUAL_THREAD;
		}
	
		ThreadStack stack;
		if (virtual) {
			// perform aggregration for carrying threads
			// Name and header have no meaning
			stack = vtBuilder.lookup(
							id, 
							state, 
							-1, // Not used, 
							dump.getFilePath(), 
							dump.getTimestamp(), 
							codeLines
						);
		}
		else {
			if (threadName.isEmpty())
				threadName = buildDefaultThreadName(container); // take the container name instead
			String header = threadName + " #" + id;
			stack = new ThreadStackImpl(
							header, 
							id,
							threadName, 
							state,
							-1, // Not used
							dump.getFilePath(),
							dump.getTimestamp(),
							codeLines,
							!state.isCarryingVirtualThread() // we exclude carrier threads from working threads
						);
		}
		
		if (stack.hasUniqueInstance())
			dump.addStack(stack);
	}

	private boolean detectVirtualThread(String threadName, List<String> codeLines) {
		// unmounted virtual thread
		if (threadName.isEmpty() && codeLines.get(1).contains(VIRTUAL_THREAD_UNMOUNTED_CODE_SIGNATURE))
			return true;
		// working virtual thread
		if (codeLines.size() > 2 && codeLines.get(codeLines.size()-3).contains(VIRTUAL_THREAD_CODE_SIGNATURE))
			return true;
		return false;
	}
	
	private List<String> internCodeLines(List<String> lines) {
		List<String> codeLines = new ArrayList<>(lines.size());
		for (String lineToIntern : lines){
			codeLines.add(lineToIntern.intern());
		}
		return codeLines;
	}

	@Override
	public ThreadStack parseThreadStack(StackContext context) throws ParseException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLockIdUsed() {
		return false;
	}

	@Override
	public boolean isCPUMeasurementUsed(){
		return false;
	}
	
	@Override
	public boolean isMemoryMeasurementUsed(){
		return false;
	}
	
	@Override
	public boolean isGarbageCollectionMeasurementUsed() {
		return false;
	}
	
	@Override
	public boolean isOpenFileDescriptorMeasurementUsed() {
		return false;
	}
	
	@Override
	public boolean isDeadlockUsed(){
		return false;
	}
	
	@Override
	public boolean isLockCycleDetectionUsed(){
		return false;
	}
	
	@Override
	public boolean isBiasedLockUsed(){
		return false;
	}
	
	@Override
	public boolean isJeyzerMXUsed(){
		return false;
	}
	
	@Override
	public boolean isProcessUpTimeMeasurementUsed() {
		return false;
	}
	
	@Override
	public boolean isDiskWriteTimeMeasurementUsed(){
		return false;
	}
	
	@Override
	public boolean isSuspendedUsed() {
		return false;
	}

	@Override
	public boolean hasVirtualThreadSupport() {
		return true;
	}
	
	@Override
	public boolean areVirtualThreadVariationCountersUsed() {
		return false;
	}
	
	@Override
	public boolean hasVirtualThreadStackSupport() {
		return true;
	}
}