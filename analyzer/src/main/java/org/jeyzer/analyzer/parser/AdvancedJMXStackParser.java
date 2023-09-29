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
import org.jeyzer.analyzer.parser.advanced.DumpBeanInfoParser;
import org.jeyzer.analyzer.parser.advanced.ThreadBeanInfoParser;
import org.jeyzer.analyzer.parser.advanced.ThreadBeanInfoParser.ThreadBeanInfo;
import org.jeyzer.analyzer.setup.JzrSetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedJMXStackParser extends ThreadDumpParser {

	// format name
	public static final String FORMAT_NAME = "Jeyzer Recorder - Advanced JMX";
	public static final String FORMAT_SHORT_NAME = "Advanced JMX";

	public static final String FIRST_LINE = "Full Advanced Java thread dump";

	// Dump content
	public static final String STACK_LINE_START_TAG = "    at";
	public static final String LOCK_OWNER_TAG = "      - locked";
	public static final String LOCKED_SYNC_TAG = "Locked synchronizers: count =";
	private static final String DETECTED_DEAD_LOCK_TAG = "     deadlock participant";
	
	// Stack content
	private static final String ID_TAG = "Id=";
	private static final String STATE_TAG_START = " in ";
	private static final String STATE_TAG_END = " ";
	private static final String WAITING_ON_LOCK_TAG = "on lock=";
	private static final char WAITING_ON_LOCK_CLASS_NAME_TAG = '@';
	private static final String SUSPENDED = "(suspended)";
	
	private static final String JZ_PREFIX = "\tJz>\t";
	private static final String JH_PREFIX = "\tJ#>\t";
	
	private static final Logger logger = LoggerFactory.getLogger(AdvancedJMXStackParser.class);

	private ThreadBeanInfoParser threadBeanInfoParser; // important : must be state less
	private DumpBeanInfoParser dumpBeanInfoParser;     // important : must be state less
	
	public AdvancedJMXStackParser(JzrSetupManager setupMgr) {
		threadBeanInfoParser = new ThreadBeanInfoParser();
		dumpBeanInfoParser = new DumpBeanInfoParser(setupMgr);
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
		logger.info("Reading snapshot file : {}", file.getName());

		String line = null;

		boolean success = true;
		List<String> threadLines = new ArrayList<>();
		List<String> ownedLocks = new ArrayList<>();
		List<String> biasedLocks =  null; // not supported
		String threadLock = "";
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
			// First line --> ignore - "Full Advanced Java thread dump with locks info"
			reader.readLine();
			lineCount++;

			line = reader.readLine();
			while(line!=null && (line.startsWith(JZ_PREFIX) || line.startsWith(JH_PREFIX))){
				dumpBeanInfoParser.parse(dump, line.substring(JZ_PREFIX.length()));
				lineCount++;
				line = reader.readLine();
			}
			
			if (line == null)
				throw new JzrLineParsingException("Premature end of file", file.getName(), line, lineCount);
			
			lineCount++;
			
			while ((line = reader.readLine()) != null) {

				if (line.length() == 0) {
					if (stackFound) {
						// time to flush
						StackContext context = new StackContext(
								threadLines,
								threadLinePos, 
								file.getPath(), 
								ownedLocks, 
								biasedLocks,
								dump.getTimestamp()
								);
						stack = parseThreadStack(context);
						dump.addStack(stack);
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

				if (line.startsWith("\"")) {
					threadLines.add(line);
					threadLinePos = lineCount;
					lineCount++;
					stackFound = true;
					continue;
				}
				
				if (line.startsWith(STACK_LINE_START_TAG)
						|| line.startsWith(JZ_PREFIX)
						|| line.startsWith(JH_PREFIX)
						|| line.startsWith(DETECTED_DEAD_LOCK_TAG)) {
					threadLines.add(line);
					lineCount++;
					continue;
				}
				
				if (line.startsWith(LOCK_OWNER_TAG)) {
					int pos = LOCK_OWNER_TAG.length() + 1;
					String lockName = line.substring(pos);
					ownedLocks.add(lockName.intern());
					lineCount++;
					continue;
				}

				// Locked synchronizers not always available
				if (line.startsWith(LOCKED_SYNC_TAG)) {
					int lockCount;
					threadLock = line;

					int posStart = threadLock.indexOf(LOCKED_SYNC_TAG)
							+ LOCKED_SYNC_TAG.length() + 1;
					int posEnd = threadLock.length();
					lockCount = Integer.parseInt(threadLock.substring(posStart,
							posEnd));

					if (stack != null)
						stack.setLockCount(lockCount);
					lineCount++;
					continue;
				}

				lineCount++;

				/**
				 * Sample with lock and cpu:
				 * 
					"ContainerBackgroundProcessor[StandardEngine[Catalina]]" Id=19 in TIMED_WAITING
	 					cpu time=2193093	 user time=218171
    					at java.lang.Thread.sleep(Native Method)
    					at org.apache.catalina.core.ContainerBase$ContainerBackgroundProcessor.run(ContainerBase.java:1345)
    					at java.lang.Thread.run(Unknown Source)

    					Locked synchronizers: count = 0
				 */
			}

		} catch (FileNotFoundException ex) {
			success = false;
			logger.error("Failed to open {}", file.getAbsolutePath(), ex);
			this.parsingErrors.put(file.getName(), ex);
		} catch (JzrLineParsingException e) {
			success = false;
			this.parsingErrors.put(file.getName(), e);
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
		boolean deadlock = false;
		int linePos = 0;
		
		// header section
		String header = context.threadLines.get(linePos++).intern();
		String name = parseName(header);
		String id = parseId(header);
		ThreadState state = StateParser.parseState(header, STATE_TAG_START, STATE_TAG_END);
		
		// suspended
		boolean suspended = header.indexOf(SUSPENDED) != -1;

		// locked on info is on the header (with Jstack it's inside the stack)
		String lockedOn = parseLockName(header);
		String lockedOnClassName = parseLockClassName(lockedOn);
		
		String line = context.threadLines.get(linePos);
		
		if (line.startsWith(DETECTED_DEAD_LOCK_TAG)){
			deadlock = true;
			linePos++;
			line = context.threadLines.get(linePos);
		}
		
		ThreadBeanInfo info = threadBeanInfoParser.createThreadBeanInfo();
		while(line.startsWith(JZ_PREFIX) || line.startsWith(JH_PREFIX)){
			threadBeanInfoParser.parse(line.substring(JZ_PREFIX.length()), info);
			linePos++;
			if (linePos==context.threadLines.size())
				break;
			line = context.threadLines.get(linePos);
		}
		
		// stack section - take the intern to optimize the memory
		List<String> codeLines = new ArrayList<>(context.threadLines.size()-linePos);
		for (String lineToIntern : context.threadLines.subList(linePos,context.threadLines.size())){
			codeLines.add(lineToIntern.intern());
		}
		
		if (codeLines.size() >= 2 && isCarrierThread(codeLines))
			state = ThreadState.CARRYING_VIRTUAL_THREAD;

		return new ThreadStackImpl(header, name, id, state, suspended,
				context.filePos, context.fileName, context.timestamp, codeLines, 
				lockedOn, lockedOnClassName, context.ownedLocks, deadlock, 
				info.getCpuTime(),
				info.getUserTime(),
				info.getMemory(),
				info.getThreadStackJeyzerMXInfo()
				);
	}

	/**
	 * Parsing
	 */

	private String parseId(String header) {
		int posStart = header.indexOf(ID_TAG) + ID_TAG.length();
		int posEnd = header.indexOf(' ', posStart);
		return header.substring(posStart, posEnd).intern();
	}

	private String parseLockName(String header) {
		String lockName;

		int posStart = header.indexOf(WAITING_ON_LOCK_TAG);
		if (posStart != -1)
			lockName = header.substring(posStart + WAITING_ON_LOCK_TAG.length());
		else
			lockName = EMPTY_STRING;

		return lockName.intern();
	}
	
	private String parseLockClassName(String lockedOn) {
		String lockClassName;

		// org.jeyzer.demo.features.l@62e0c462
		int posEnd = lockedOn.indexOf(WAITING_ON_LOCK_CLASS_NAME_TAG);
		if (posEnd != -1)
			lockClassName = lockedOn.substring(0, posEnd);
		else
			lockClassName = EMPTY_STRING;

		return lockClassName.intern();
	}

	@Override
	public boolean isLockIdUsed() {
		return true;
	}
	
	@Override
	public boolean isCPUMeasurementUsed(){
		return true;
	}
	
	@Override
	public boolean isDiskWriteTimeMeasurementUsed(){
		return false;
	}

	@Override
	public boolean isMemoryMeasurementUsed(){
		return true;
	}

	@Override
	public boolean isGarbageCollectionMeasurementUsed() {
		return true;
	}
	
	@Override
	public boolean isOpenFileDescriptorMeasurementUsed() {
		return true;
	}

	@Override
	public boolean isDeadlockUsed(){
		return true;
	}
	
	@Override
	public boolean isLockCycleDetectionUsed(){
		return false; // cycles are not distinguished through ThreadMXBean through the findDeadlockedThreads method
	}
	
	@Override
	public boolean isBiasedLockUsed(){
		return false;
	}

	@Override
	public boolean isJeyzerMXUsed(){
		return true;
	}

	@Override
	public boolean isProcessUpTimeMeasurementUsed() {
		return true;
	}

	@Override
	public boolean isSuspendedUsed() {
		return true;
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
		return false;
	}
}
