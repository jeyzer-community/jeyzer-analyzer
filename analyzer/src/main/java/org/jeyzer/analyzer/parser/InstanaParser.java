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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class InstanaParser extends ThreadDumpParser {

	// format name
	public static final String FORMAT_NAME = "Instana";
	public static final String FORMAT_SHORT_NAME = "Instana";

	private static final Logger logger = LoggerFactory.getLogger(InstanaParser.class);

	// Stack content
	public static final String ID_TAG = "id=";
	public static final String STATE_TAG_START = "state=";
	public static final String STATE_TAG_END = " ";

	// dump content
	public static final String STACK_LINE_START_TAG = "    at ";
	public static final String WAITING_ON_LOCK_TAG = "    - waiting to lock <";
	public static final String WAITING_ON_LOCK_NAME_TAG = "> (a ";
	public static final String LOCK_OWNER_TAG = "      - locked ";
	public static final char   LOCK_OWNER_TAG_START = '@';
	public static final char   WAITING_ON_LOCK_END_TAG = ')';
	public static final char   LOCK_TAG_END = '>';
	
	public static final String WAIT_TAG = "    - waiting on <";
	
	public static final String HEXA_PREFIX = "0x";

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
		List<String> biasedLocks = new ArrayList<>(0); // unlikely
		Multimap<String,ThreadStack> stacks = ArrayListMultimap.create(); // Thread name is not unique so multi map is used.
		String lockedOn = EMPTY_STRING;
		String lockedOnClassName = EMPTY_STRING;
		ThreadDump dump = new ThreadDump(file, date);
		int threadLinePos = 0;
		int lineCount = 0;
		boolean stackFound = false;
		ThreadStack stack = null;

		try (
				FileReader fr = new FileReader(file);
				BufferedReader reader = new BufferedReader(fr);
			)
		{
			// no header parsing, no capture time

			while ((line = reader.readLine()) != null) {
				if (line.length() == 0) {
					if (stackFound) {
						if (isRecordableStack(threadLines)) {
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

				if (!line.startsWith(" ")) {
					// task-scheduler-8 id=87 state=WAITING
					threadLines.add(line);
					threadLinePos = lineCount;
					lineCount++;
					stackFound = true;
					continue;
				}

				if (line.startsWith(STACK_LINE_START_TAG)) {
					// at <class name with full package>.<method>(<class name>.java:<line>)
					threadLines.add(line);
					lineCount++;
					continue;
				}

				if (line.startsWith(WAIT_TAG)) {
					// - waiting on <0x000000078d356b28> (a <class name with full package>)
					// not exploited, skip it (this is standard wait() monitor)
					lineCount++;
					continue;
				}

				if (line.startsWith(LOCK_OWNER_TAG)) {
					// - locked java.util.Collections$UnmodifiableSet@76455d9b
					int pos = line.indexOf(LOCK_OWNER_TAG_START)+1;
					String hexavalue = HEXA_PREFIX + line.substring(pos);
					String lockName = buildLockName(hexavalue);
					ownedLocks.add(lockName);
					lineCount++;
					continue;
				}
				
				if (line.startsWith(WAITING_ON_LOCK_TAG)) {
					// - waiting to lock <0x000000078d43ab00> (a <class name with full package>)
					int pos = WAITING_ON_LOCK_TAG.length();
					int endPos = line.indexOf(LOCK_TAG_END, pos);
					String hexavalue = line.substring(pos, endPos);
					lockedOn = buildLockName(hexavalue);
					
					pos = endPos + WAITING_ON_LOCK_NAME_TAG.length();
					endPos = line.indexOf(WAITING_ON_LOCK_END_TAG, pos);
					lockedOnClassName = line.substring(pos, endPos);

					lineCount++;
					continue;
				}
					
				// Ignore :
				// - parking to wait for <0x00000007f0df1370> (a
				// java.util.concurrent.SynchronousQueue$TransferStack)
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

	protected boolean isRecordableStack(List<String> threadLines) {
		// JEYZ-29 Jeyzer Analyzer failed to parse consecutive thread names (jstack case)
		return (threadLines.size() > 1 && !threadLines.get(1).startsWith("\""));
	}

	@Override
	public ThreadStack parseThreadStack(StackContext context) throws ParseException {
		// header section
		String header = context.threadLines.get(0).intern();
		String name = parseName(header);
		String id = parseID(header);		
		ThreadState state = StateParser.parseState(header, STATE_TAG_START, STATE_TAG_END);
				
		// stack section - store it as intern to save memory
		List<String> codeLines = internCodeLines(context);

		return new ThreadStackImpl(header, name, id, state, false,
				context.filePos, context.fileName, context.timestamp, codeLines, 
				context.lockedOn, context.lockedOnClassName, context.ownedLocks, false);
	}

	protected List<String> internCodeLines(StackContext context) {
		List<String> codeLines = new ArrayList<>(context.threadLines.size()-1);
		for (String lineToIntern : context.threadLines.subList(1,context.threadLines.size())){
			codeLines.add(lineToIntern.intern());
		}
		return codeLines;
	}

	protected String parseID(String header) {
		int posStart = header.indexOf(ID_TAG) + ID_TAG.length();
		int posEnd = header.indexOf(' ', posStart);
		return header.substring(posStart, posEnd);
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
	protected String parseName(String header){
		String name;

		int pos = header.indexOf(ID_TAG)-1;
		name = header.substring(0,pos);
		
		return name.intern();
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
}
