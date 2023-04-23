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

public class JMXStackParser extends ThreadDumpParser {
	
	// format name
	public static final String FORMAT_NAME = "Jeyzer Recorder - JMX method";
	public static final String FORMAT_SHORT_NAME = "JMX";

	public static final String FIRST_LINE = "Full Java thread dump";

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

	private static final String JZRA_FIELD_CAPTURE_DURATION = "\tJ#>\tcapture time\t";	
	
	private static final Logger logger = LoggerFactory.getLogger(JMXStackParser.class);

	public JMXStackParser() {
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
			// First line --> ignore - "Full Java thread dump with locks info"
			reader.readLine();
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
				
				if (line.startsWith(JZRA_FIELD_CAPTURE_DURATION)) {
					parseCaptureTime(line, dump);
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

	private void parseCaptureTime(String line, ThreadDump dump) {
		int posStart = JZRA_FIELD_CAPTURE_DURATION.length();

		Long captureTime = Longs.tryParse(line.substring(posStart));
		if (captureTime == null){
			logger.warn("Failed to convert capture time : ", line.substring(posStart));
			captureTime = -1L;
		}
		
		dump.setCaptureTime(captureTime);
	}

	@Override
	public ThreadStack parseThreadStack(StackContext context) throws ParseException {
		boolean deadlock = false;
		int linePos = 0;
		
		// header section
		String header = context.threadLines.get(linePos++).intern();
		String name = parseName(header);
		String id = parseID(header);
		ThreadState state = StateParser.parseState(header, STATE_TAG_START, STATE_TAG_END);
		
		// suspended
		boolean suspended = header.indexOf(SUSPENDED) != -1;

		// locked on info is on the header (with Jstack it's inside the stack)
		String lockedOn = parseLockName(header);
		String lockedOnClassName = parseLockClassName(lockedOn);

		// Case where stack content is empty. Example : 
		// >"Attach Listener" Id=5 in RUNNABLE
		// >
		// >    Locked synchronizers: count = 0
		if (context.threadLines.size()>1){
			String line = context.threadLines.get(linePos);
			if (line.startsWith(DETECTED_DEAD_LOCK_TAG)){
				deadlock = true;
				linePos++;
			}
		}

		// stack section - store it as intern to save memory 
		List<String> codeLines = new ArrayList<>(context.threadLines.size()-linePos);
		for (String lineToIntern : context.threadLines.subList(linePos,context.threadLines.size())){
			codeLines.add(lineToIntern.intern());
		}

		return new ThreadStackImpl(header, name, id, state, suspended,
				context.filePos, context.fileName, context.timestamp, codeLines, 
				lockedOn, lockedOnClassName, context.ownedLocks, deadlock);
	}

	/**
	 * Parsing
	 */

	private String parseID(String header) {
		int posStart = header.indexOf(ID_TAG) + ID_TAG.length();
		int posEnd = header.indexOf(' ', posStart);
		return header.substring(posStart, posEnd).intern();
	}

	private String parseLockName(String header) {
		String lockName;

		int posStart = header.indexOf(WAITING_ON_LOCK_TAG);
		if (posStart != -1)
			lockName = header
					.substring(posStart + WAITING_ON_LOCK_TAG.length());
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
		return true;
	}
}
