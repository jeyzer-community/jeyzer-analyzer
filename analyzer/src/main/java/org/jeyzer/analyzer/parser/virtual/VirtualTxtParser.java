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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStackImpl;
import org.jeyzer.analyzer.data.stack.ThreadState;
import org.jeyzer.analyzer.error.JzrLineParsingException;
import org.jeyzer.analyzer.parser.StackContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualTxtParser extends VirtualDumpParser {

	protected static final String EMPTY_STRING = "";
	public static final String STACK_HEADER_START_TAG = "#";
	public static final String STACK_LINE_START_TAG = "      ";
	public static final String VIRTUAL_TAG = "virtual";
		
	private static final Logger logger = LoggerFactory.getLogger(VirtualTxtParser.class);		
	
	public void parseVirtualThreadDump(File file, Date date, ThreadDump dump) throws FileNotFoundException, JzrLineParsingException {
		logger.info("Reading thread dump file : {}", file.getName());

		String line = null;
		List<String> threadLines = new ArrayList<>();
		List<String> ownedLocks = new ArrayList<>();
		List<String> biasedLocks = new ArrayList<>(0); // unlikely
		String lockedOn = EMPTY_STRING;
		String lockedOnClassName = EMPTY_STRING;
		int threadLinePos = 0;
		int lineCount = 0;
		boolean stackFound = false;
		ThreadStack stack = null;
		Map<String, Object> dumpContext = new HashMap<>();

		try (
				FileReader fr = new FileReader(file);
				BufferedReader reader = new BufferedReader(fr);
			)
		{
			while ((line = reader.readLine()) != null) {
				if (line.length() == 0) {
					if (stackFound) {
						// time to flush if thread stack different than 1 line
						if (isRecordableStack(threadLines)) {
							StackContext context = new StackContext(
									threadLines,
									threadLinePos, 
									file.getPath(), 
									ownedLocks, 
									biasedLocks,
									dump.getTimestamp(),
									lockedOn, 
									lockedOnClassName,
									dumpContext
									);
							stack = parseThreadStack(context);
							if (stack.hasUniqueInstance())
								dump.addStack(stack); // add any virtual only once
						}
						threadLines.clear();
						ownedLocks.clear();
						biasedLocks.clear();
						lineCount++;
						stackFound = false;
						lockedOn = EMPTY_STRING;
						lockedOnClassName = EMPTY_STRING;
						continue;
					} else {
						lineCount++;
						continue;
					}
				}

				if (line.startsWith(STACK_HEADER_START_TAG)) {
					// #24 "" virtual
					// or
					// #1044 "Read-Updater"
					threadLines.add(line);
					threadLinePos = lineCount;
					lineCount++;
					stackFound = true;
					continue;
				}

				if (line.startsWith(STACK_LINE_START_TAG)) {
					//    java.base/jdk.internal.misc.Unsafe.park(Native Method)
				    //    java.base/java.util.concurrent.locks.LockSupport.park(LockSupport.java:371)
					threadLines.add(line);
					lineCount++;
					continue;
				}
					
				lineCount++;
			}

		} catch (FileNotFoundException ex) {
			logger.error("Failed to open {}", file.getAbsolutePath(), ex);
			throw ex;
		} catch (Exception e) {
			logger.error("Failed to parse thread dump {}", file.getName(), e);
			throw new JzrLineParsingException(e, file.getName(), line, lineCount);
		}
	}
	
	public ThreadStack parseThreadStack(StackContext context) throws ParseException {
		// header section
		String header = context.threadLines.get(0).intern();
		String name = parseName(header);
		String id = parseID(header);
		boolean virtual = header.contains(VIRTUAL_TAG);

		// stack section - store it as intern to save memory, only for native
		List<String> codeLines;
		if (virtual) {
			codeLines = new ArrayList<>(context.threadLines.size()-1);
			codeLines.addAll(context.threadLines.subList(1,context.threadLines.size()));
		}else {
			codeLines = internCodeLines(context);
		}
		
		// Detect the virtual thread carriers based on code (no other way here)
		//  Same for the unmounted threads
		ThreadState state = ThreadState.UNKNOWN;
		if (codeLines.size() >= 2) {
			if (isCarrierThread(codeLines))
				state = ThreadState.CARRYING_VIRTUAL_THREAD;
			else if (isUnmountedVirtualThread(codeLines))
				state = ThreadState.UNMOUNTED_VIRTUAL_THREAD;
		}

		if (virtual)
			// perform aggegration
			// Name and header have no meaning
			return vtBuilder.lookup(
					id, 
					state, 
					context.filePos, 
					context.fileName, 
					context.timestamp, 
					codeLines
					);
		else 
			return new ThreadStackImpl(
					header, 
					name, 
					id, 
					state, 
					context.filePos, 
					context.fileName, 
					context.timestamp, 
					codeLines,
					!state.isCarryingVirtualThread()  // we exclude carrier threads from working threads
					);
	}
	
	protected String parseID(String header) {
		int pos = header.indexOf(' ');
		return header.substring(1, pos);
	}
	
	protected String parseName(String header){
		String name;
		int pos = header.indexOf('\"');
		int pos2 = header.indexOf('\"', pos);
		
		if (header.length() > 2000)
			// Exotic case where thread name is larger than 2000 chars : the closing " is then missing
			// Example : "thread name[...]-501187129.p#109 prio=5 
			pos = pos == -1 ? 1999 : pos;
		
		name = header.substring(pos, pos2); // can be empty
		if (name.isEmpty()) {
			// take the id as name
			pos = header.indexOf(' ');
			name = header.substring(0, pos);
		}
		
		return name.intern();
	}
	
	protected List<String> internCodeLines(StackContext context) {
		List<String> codeLines = new ArrayList<>(context.threadLines.size()-1);
		for (String lineToIntern : context.threadLines.subList(1,context.threadLines.size())){
			codeLines.add(lineToIntern.intern());
		}
		return codeLines;
	}
	
	protected boolean isRecordableStack(List<String> threadLines) {
		return (threadLines.size() > 1 && !threadLines.get(1).startsWith("\""));
	}
}
