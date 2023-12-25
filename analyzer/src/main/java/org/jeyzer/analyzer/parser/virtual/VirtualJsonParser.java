package org.jeyzer.analyzer.parser.virtual;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2023 Jeyzer
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStackImpl;
import org.jeyzer.analyzer.data.stack.ThreadState;
import org.jeyzer.analyzer.error.JzrParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;

public class VirtualJsonParser extends VirtualDumpParser {

	public static final String MARKER_THREAD_DUMP = "threadDump";
	public static final String MARKER_CONTAINERS = "threadContainers";
	
	public static final String MARKER_CONTAINER = "container";
	public static final String MARKER_THREADS = "threads";
	
	public static final String MARKER_TID = "tid";
	public static final String MARKER_NAME = "name";
	public static final String MARKER_STACK = "stack";
	
	private static final Logger logger = LoggerFactory.getLogger(VirtualJsonParser.class);	
	
	@Override
	public void parseVirtualThreadDump(File file, Date date, ThreadDump dump) throws FileNotFoundException, JzrParsingException {
		logger.info("Reading json thread dump file : {}", file.getName());
		
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
			logger.error("Failed to open {}", file.getAbsolutePath(), ex);
			throw ex;
		} catch (Exception e) {
			logger.error("Invalid json thread dump {}", file.getName(), e);
			throw new JzrParsingException(file.getName(), e);
		}
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

	private List<String> readCodeLines(JsonReader reader) throws IOException {
		List<String> codeLines = new ArrayList<>();
		
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
			if (isCarrierThread(codeLines))
				state = ThreadState.CARRYING_VIRTUAL_THREAD;
			else if (isUnmountedVirtualThread(codeLines))
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
		if (threadName.isEmpty() && isUnmountedVirtualThread(codeLines))
			return true;
		// working virtual thread
		return codeLines.size() > 2 && codeLines.get(codeLines.size()-3).contains(VIRTUAL_THREAD_CODE_SIGNATURE);
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
	
	private List<String> internCodeLines(List<String> lines) {
		List<String> codeLines = new ArrayList<>(lines.size());
		for (String lineToIntern : lines){
			codeLines.add(lineToIntern.intern());
		}
		return codeLines;
	}
}
