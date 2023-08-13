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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStackImpl;
import org.jeyzer.analyzer.data.stack.ThreadState;
import org.jeyzer.analyzer.error.JzrLineParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Longs;

public class TDASimpleParser extends ThreadDumpParser {

	// format name
	public static final String FORMAT_NAME = "TDA Simple parser";
	public static final String FORMAT_SHORT_NAME = "TDA";

	private static final Logger logger = LoggerFactory.getLogger(TDASimpleParser.class);

	// Stack content
	public static final String HEADER_START = "\"";
	public static final String ID_TAG_START = " Id="; // space ahead important
	public static final String ID_TAG_END = " ";
	public static final String STATE_TAG_END = " ";
	public static final String WAITING_ON_LOCK_TAG = " owned by ";
	public static final String BLOCKED_ON_TAG = "BLOCKED on ";
	public static final String BLOCKED_ON_END_TAG = "@";

	// dump content
	public static final String STACK_LINE_START_TAG = "        at ";
	public static final String STACK_LINE_START_TAG_WITH_TAB = "\tat ";

	public TDASimpleParser() {
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
		logger.info("Reading thread dump file : {}", file.getName());

		String line = null;
		boolean success = true;
		List<String> threadLines = new ArrayList<>();
		List<String> ownedLocks = new ArrayList<>();
		List<String> biasedLocks = null; // not supported
		String lockedOn = EMPTY_STRING;
		String lockedOnClassName = EMPTY_STRING;
		ThreadDump dump = new ThreadDump(file, date);
		int lineCount = 0;
		int threadLinePos = 0;
		boolean stackFound = false;
		ThreadStack stack = null;

		try (
				FileReader fr = new FileReader(file);
				BufferedReader reader = new BufferedReader(fr);
			)
		{
			while ((line = reader.readLine()) != null) {

				if (line.length() == 0) {
					if (stackFound) {
						// time to flush if thread stack different than 1 line
						// entries
						if (threadLines.size() > 1) {
							StackContext context = new StackContext(
									threadLines,
									threadLinePos, 
									file.getPath(), 
									ownedLocks, 
									biasedLocks,
									dump.getTimestamp(),
									lockedOn, 
									lockedOnClassName
									);
							stack = parseThreadStack(context);
							dump.addStack(stack);
						}
						threadLines.clear();
						ownedLocks.clear();
						lineCount++;
						stackFound = false;
						continue;
					} else {
						lineCount++;
						continue;
					}
				}

				if (line.startsWith(HEADER_START)) {
					// "Timer-9" Id=150 TIMED_WAITING on java.util.TaskQueue@515ee03d
					threadLines.add(line);
					threadLinePos = lineCount;
					lineCount++;
					stackFound = true;
					continue;
				}

				if (line.startsWith(STACK_LINE_START_TAG) || line.startsWith(STACK_LINE_START_TAG_WITH_TAB)) {
					// at org.apache.catalina.startup.Bootstrap.start(Bootstrap.java:288)
					threadLines.add(line);
					lineCount++;
					continue;
				}

				lineCount++;
			}

		} catch (FileNotFoundException ex) {
			success = false;
			logger.error("Failed to open {}", file.getAbsolutePath(), ex);
			this.parsingErrors.put(file.getName(), ex);
		} catch (Exception e) {
			success = false;
			logger.error("Failed to parse thread dump {}.", file.getName(), e);
			JzrLineParsingException ex = new JzrLineParsingException(e, file.getName(), line, lineCount);
			this.parsingErrors.put(file.getName(), ex);
		}

		if (success)
			dumps.add(dump);
	}

	@Override
	public ThreadStack parseThreadStack(StackContext context) throws ParseException {
		// header section
		String header = context.threadLines.get(0).intern();
		String name = parseName(header);
		String id = parseID(header);

		ThreadState state = StateParser.parseTDAState(header);
		String lockedOn = parseLockedOn(header);
		String lockedOnClassName = parseLockClassName(header);

		List<String> codeLines = new ArrayList<>(context.threadLines.size()-1);
		for (String lineToIntern : context.threadLines.subList(1,context.threadLines.size())){
			codeLines.add(lineToIntern.intern());
		}

		return new ThreadStackImpl(header, name, id, state, false,
				context.filePos, context.fileName, context.timestamp, codeLines, 
				lockedOn, lockedOnClassName, context.ownedLocks, false);
	}

	private String parseID(String header) throws ParseException {
		// Id=45
		int posStart = header.indexOf(ID_TAG_START) + ID_TAG_START.length();
		int posEnd = header.indexOf(ID_TAG_END, posStart);
		String value = header.substring(posStart, posEnd);
		
		Long id = Longs.tryParse(value);
		if (id == null){
			logger.error("Thread Id parsing failed.");
			throw new ParseException("Failed to parse thread id : \""
					+ header.substring(posStart) + "\" on header \"" + header
					+ "\".", -1);
		}

		return id.toString().intern();
	}

	private String parseLockedOn(String line) {
		// BLOCKED on org.apache.log4j.Logger@4f56a0fa owned by "[ACTIVE] ExecuteThread: '19' for queue: 'weblogic.kernel.Default (self-tuning)'" Id=2199
		int posStart = line.indexOf(WAITING_ON_LOCK_TAG);
		if (posStart != -1)
			return line.substring(posStart + WAITING_ON_LOCK_TAG.length()).intern();
		
		return "";
	}
	
	private String parseLockClassName(String line) {
		// BLOCKED on org.apache.log4j.Logger@4f56a0fa owned by: <other-thread-name>
		int posStart = line.indexOf(BLOCKED_ON_TAG);
		int posEnd = line.indexOf(BLOCKED_ON_END_TAG);
		if (posStart != -1 && posEnd != -1)
			return line.substring(posStart + BLOCKED_ON_TAG.length(), posEnd).intern();
		else
			return EMPTY_STRING;
	}

	@Override
	public boolean isLockIdUsed() {
		return false; // thread name is used instead
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
		return false;
	}
	
	@Override
	public boolean areVirtualThreadVariationCountersUsed() {
		return false;
	}
	
	@Override
	public boolean hasVirtualThreadStackSupport() {
		return false;
	}
}
