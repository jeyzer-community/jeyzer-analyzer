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
import org.jeyzer.analyzer.data.stack.ThreadStack.DAEMON;
import org.jeyzer.analyzer.error.JzrLineParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;

public class JRockitParser extends ThreadDumpParser {

	// format name
	public static final String FORMAT_NAME = "JRockit Mission Control";
	public static final String FORMAT_SHORT_NAME = "JRMC";

	public static final String INDICATOR_UPPER_CASE_THREAD_DUMP = "===== FULL THREAD DUMP ===============";
	public static final String INDICATOR_ORACLE_JROCKIT = "Oracle JRockit";

	// date as header
	public static final String DATE_FORMAT = "MMM d HH:mm:ss yyyy";
	
	private static final Logger logger = LoggerFactory.getLogger(JRockitParser.class);

	// Stack content
	public static final String DAEMON_TAG = "daemon";
	public static final String PRIO_TAG = "prio=";
	public static final String ID_TAG = "tid=";

	// dump content
	public static final String STACK_HEADER_START_TAG = "\"";
	public static final String STACK_LINE_START_TAG = "    at";
	public static final String WAITING_ON_LOCK_TAG = "    -- Blocked trying to get lock: ";
	public static final char   WAITING_ON_LOCK_MIDDLE_TAG = '@';
	public static final char   WAITING_ON_LOCK_END_TAG = '[';
	
	public static final String LOCK_OWNER_TAG = "    ^-- Holding lock: ";
	public static final char   LOCK_TAG_MIDDLE = '@';
	public static final char   LOCK_TAG_END = '[';

	public static final String LOCK_ELIMINATED = "[biased lock]";
	
	// end of file
	public static final String DUMP_END_MARKER = "Blocked lock chains";

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
		List<String> biasedLocks = new ArrayList<>(0);
		Multimap<String,ThreadStack> stacks = ArrayListMultimap.create(); // Thread name is not unique so multi map is used. Reason : unfortunately, deadlock detection refers to thread name
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
			// First 6-7 lines --> ignore
			/*
			 * 17625:
			 * <empty line>
			 * ===== FULL THREAD DUMP ===============
			 * Fri Feb 12 13:59:23 2021
			 * Oracle JRockit(R) R28.3.20-12-175519-1.6.0_211-20181010-0440-linux-x86_64
			 * <empty line>
			 */
			
			// Variant with extra date as first line
			/*
			 * 2021-11-15_10_50_04
			 * 17625:
			 * <empty line>
			 * ===== FULL THREAD DUMP ===============
			 * Mon Nov 15 10:50:04 2021
			 * Oracle JRockit(R) R28.2.5-20-152429-1.6.0_37-20120927-1915-linux-x86_64
			 * <empty line>
			 */
			
			line = reader.readLine(); // pid
			line = reader.readLine();
			line = reader.readLine();
			line = reader.readLine(); // line 4
			line = reader.readLine(); // line 5
			line = reader.readLine(); // line 6
			lineCount = lineCount + 6;

			while ((line = reader.readLine()) != null) {
				if (line.length() == 0) {
					if (stackFound) {
						// time to flush if thread stack different than 1 line
						// entries as for example :
						// "(GC Worker Thread 4)" id=? idx=0x1c tid=17632 prio=5 alive, daemon
						// "(Code Optimization Thread 1)" id=5 idx=0x38 tid=17639 prio=5 alive, native_waiting, daemon
						// "VM JFR Buffer Thread" id=10 idx=0x4c tid=17644 prio=5 alive, in native, daemon
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
							stacks.put(stack.getName(), stack);
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
					// "Timer-0" id=14 idx=0x54 tid=17653 prio=5 alive, waiting, native_blocked, daemon
					// "ExecuteThread: '0' for queue: 'weblogic.socket.Muxer'" id=27 idx=0x7c tid=17665 prio=5 alive, blocked, native_blocked, daemon
					threadLines.add(line);
					threadLinePos = lineCount;
					lineCount++;
					stackFound = true;
					continue;
				}

				if (line.startsWith(STACK_LINE_START_TAG)) {
					// at weblogic/socket/EPollSocketMuxer.processSockets(EPollSocketMuxer.java:153)
					threadLines.add(line);
					lineCount++;
					continue;
				}
				
				if (line.startsWith(WAITING_ON_LOCK_TAG)) {
					// -- Blocked trying to get lock: java/lang/String@0x86e1d518[fat lock]
					int middlePos = line.indexOf(WAITING_ON_LOCK_MIDDLE_TAG, WAITING_ON_LOCK_TAG.length());
					lockedOnClassName = line.substring(WAITING_ON_LOCK_TAG.length(), middlePos).replace('/', '.');
					
					int endPos = line.indexOf(WAITING_ON_LOCK_END_TAG, middlePos);
					String hexavalue = line.substring(middlePos + 1, endPos);
					lockedOn = buildLockName(hexavalue);
					
					lineCount++;
					continue;
				}

				if (line.startsWith(LOCK_OWNER_TAG)) {
					if (line.endsWith(LOCK_ELIMINATED)) {
						// ^-- Holding lock: java/net/SocksSocketImpl@0x8554ed28[biased lock]
						int endPos = line.indexOf(LOCK_TAG_END, LOCK_OWNER_TAG.length());
						String biasedLockName = line.substring(LOCK_OWNER_TAG.length(), endPos);
						biasedLocks.add(biasedLockName.replace('/', '.').intern());
					}
					else {
						// ^-- Holding lock: java/lang/String@0x86e1d518[fat lock]
						int middlePos = line.indexOf(LOCK_TAG_MIDDLE, LOCK_OWNER_TAG.length());
						int endPos = line.indexOf(LOCK_TAG_END, middlePos);
						String hexavalue = line.substring(middlePos + 1, endPos);
						String lockName = buildLockName(hexavalue);
						ownedLocks.add(lockName);
					}
					lineCount++;
					continue;
				}
				
				if (line.startsWith(DUMP_END_MARKER)){
					lineCount++;
					break; // nothing else to parse
				}
					
				// Ignore :
				// -- Waiting for notification on: 
				// -- end of trace
				// ^-- Lock released while waiting: 
				lineCount++;
			}

		} catch (FileNotFoundException ex) {
			success = false;
			logger.error("Failed to open {}", file.getAbsolutePath(), ex);
			this.parsingErrors.put(file.getName(), ex);
		} catch (Exception e) {
			success = false;
			logger.error("Failed to parse thread dump {}", file.getName(), e);
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
		ThreadState state = StateParser.parseJRockitState(context.threadLines.get(0));
		
		DAEMON daemon = parseDaemon(header);
		int priority = parsePriority(header);
		
		// stack section - store it as intern to save memory 
		List<String> codeLines = new ArrayList<>(context.threadLines.size()-1);
		for (String lineToIntern : context.threadLines.subList(1,context.threadLines.size()))
			codeLines.add(lineToIntern.replace('/', '.').intern());

		return new ThreadStackImpl(header, name, id, state, false,
				context.filePos, context.fileName, context.timestamp, codeLines, 
				context.lockedOn, context.lockedOnClassName, context.ownedLocks, context.biasedLocks, false,
				daemon, priority);
	}

	private String parseID(String header) {
		int posStart = header.indexOf(ID_TAG) + ID_TAG.length();
		int posEnd = header.indexOf(' ', posStart);
		return header.substring(posStart, posEnd);
	}

	private DAEMON parseDaemon(String header) {
		return (header.contains(DAEMON_TAG)) ? DAEMON.TRUE : DAEMON.FALSE;
	}

	private int parsePriority(String header) {
		int posStart = header.indexOf(PRIO_TAG) + PRIO_TAG.length();
		int posEnd = header.indexOf(' ', posStart);

		Integer priority = Ints.tryParse(header.substring(posStart, posEnd));
		if (priority == null){
			logger.error(
					"Failed to parse priority : \"{}\" on header \"{}\". Defaulting to prio -1",
					header.substring(posStart, posEnd), header);
			return ThreadStack.PRIORITY_NOT_AVAILABLE;
		}

		return priority;
	}

	private String buildLockName(String hexavalue) {
		String lockName;
		try {
			long lockId = Long.parseLong(hexavalue.substring(2), 16);
			lockName = Long.toString(lockId);
		} catch (NumberFormatException ex) {
			// keep the hexa value
			// failing case example : 0xfffffffeebd36fb8
			lockName = hexavalue;
		}
		return lockName.intern();
	}

	@Override
	public boolean isLockIdUsed() {
		return true;
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
		return true;
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
}
