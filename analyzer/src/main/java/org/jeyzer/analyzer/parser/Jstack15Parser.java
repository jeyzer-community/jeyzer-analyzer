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

public class Jstack15Parser extends ThreadDumpParser {

	// format name
	public static final String FORMAT_NAME = "Jstack jdk 1.5 tool";
	public static final String FORMAT_SHORT_NAME = "Jstack 1.5";

	// Seen on JDK 1.5 : "Full thread dump Java HotSpot" (original test)
	// Seen on JDK 11 :  "Full thread dump OpenJDK 64-Bit Server VM (11.0.9.1+1-LTS mixed mode, sharing)"
	//  It was probably JDK 11 connected on Java process running under JDK 5
	public static final String TD_HEADER_PATTERN = "Full thread dump";

	private static final Logger logger = LoggerFactory.getLogger(Jstack15Parser.class);

	// Stack content
	public static final String ID_TAG = "Thread t@";
	public static final String STATE_TAG_START = "   java.lang.Thread.State: ";
	public static final String STATE_TAG_END = " ";
	public static final String WAITING_ON_LOCK_TAG = " owned by: ";
	public static final String WAITING_ON_LOCK_TAG_VARIANT = " owned by ";
	public static final String BLOCKED_ON_TAG = "BLOCKED on ";
	public static final String BLOCKED_ON_END_TAG = "@";

	// dump content
	public static final String STACK_LINE_START_TAG = "        at ";
	public static final String STACK_LINE_START_TAG_WITH_TAB = "\tat ";
	public static final String STACK_LINE_WAIT_TO_LOCK_TAG = "        - waiting to lock";

	public Jstack15Parser() {
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
		long captureTime = -1;
		int lineCount = 0;
		int threadLinePos = 0;
		boolean stackFound = false;
		ThreadStack stack = null;

		try (
				FileReader fr = new FileReader(file);
				BufferedReader reader = new BufferedReader(fr);
			)
		{
			
			// First 4 lines --> ignore
			/*
			 * <empty line> 
			 * 2012-11-29 10:15:46 
			 * Full thread dump Java HotSpot(TM) Server VM (1.5.0_22-b03 mixed mode): 
			 * <empty line>
			 */
			
			// Variant #1 Jstack
			/*
			 * 2021-02-26 21:26:51
			 * Full thread dump OpenJDK 64-Bit Server VM (25.71-b10 mixed mode):
			 * <empty line>
			 */
			
			// Variant with JZR header : extract capture time
			/*
			 * Full Java thread dump from Jstack : C:\Dev\programs\Java\jdk1.8.0_25\bin\jstack -l 6160
			 * 	J#>	capture time	594
			 * <empty line>
			 * <empty line> 
			 * 2012-11-29 10:15:46 
			 * Full thread dump Java HotSpot(TM) Server VM (1.5.0_22-b03 mixed mode): 
			 * <empty line>
			 */
			
			String line1 = reader.readLine();
			String line2 = reader.readLine();
			
			if (JstackHelper.detectTDGHeader(line1)){
				captureTime = JstackHelper.parseCaptureTime(line2);
				reader.readLine(); // line 3  (empty)
				reader.readLine(); // line 4  (empty)
				reader.readLine(); // line 5  
				String line6 = reader.readLine(); // line 6
				JstackHelper.checkEmptyThreadDumpVariant(file, line6);
				reader.readLine(); // line 7   (empty)
				lineCount = lineCount + 7;
			}
			else{
				String line3 = reader.readLine(); // line 3, empty on Variant #1
				boolean variant = line2.startsWith(TD_HEADER_PATTERN);
				if (!variant) {
					// Standard Jstack
					JstackHelper.checkEmptyThreadDumpVariant(file, line3);
					reader.readLine(); // line 4   (empty)
					lineCount = lineCount + 4;
				}
				else {
					// Variant #1
					lineCount = lineCount + 3;
				}
			}
			
			dump.setCaptureTime(captureTime);

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

				if (line.startsWith("\"")) {
					// "main" - Thread t@1
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
				
				if (line.startsWith(STACK_LINE_WAIT_TO_LOCK_TAG)) {
					//  - waiting to lock <1b37558e> (a java.lang.Object) owned by "myTaskExecutor-16" t@175
					int posStart = line.indexOf(WAITING_ON_LOCK_TAG_VARIANT);
					if (posStart != -1)
						lockedOn = line.substring(posStart + WAITING_ON_LOCK_TAG_VARIANT.length()).intern();
				}

				if (line.startsWith(STATE_TAG_START)) {
					// java.lang.Thread.State: RUNNABLE
					// java.lang.Thread.State: WAITING on
					// org.tcp.DecoderThread@20f237
					// java.lang.Thread.State: BLOCKED on
					// org.tcp.RRQueuePolicy@1542167 owned by: other-thread-0
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

		String stateLine = context.threadLines.get(1);
		ThreadState state = StateParser.parseState(stateLine, STATE_TAG_START, STATE_TAG_END);
		String lockedOn = parseLockedOn(stateLine, context.lockedOn);
		String lockedOnClassName = parseLockClassName(stateLine);

		// Jstack extra info --> not available on jdk 1.5
		// Daemon and priority info are so missing
		
		List<String> codeLines = new ArrayList<>(context.threadLines.size()-2);
		for (String lineToIntern : context.threadLines.subList(2,context.threadLines.size())){
			codeLines.add(lineToIntern.intern());
		}

		return new ThreadStackImpl(header, name, id, state, false,
				context.filePos, context.fileName, context.timestamp, codeLines, 
				lockedOn, lockedOnClassName, context.ownedLocks, false);
	}

	private String parseID(String header) throws ParseException {
		// Thread t@<id>
		int posStart = header.indexOf(ID_TAG) + ID_TAG.length();
		String value = header.substring(posStart);
		
		Long id = Longs.tryParse(value);
		if (id == null){
			logger.error("Hexadecimal value conversion failed.");
			throw new ParseException("Failed to parse thread id : \""
					+ header.substring(posStart) + "\" on header \"" + header
					+ "\".", -1);
		}

		return id.toString().intern();
	}

	private String parseLockedOn(String line, String stackLockedOn) {
		if (!stackLockedOn.isEmpty())
			return stackLockedOn; // provided in the stack
		
		// java.lang.Thread.State: BLOCKED on org.tcp.RRQueuePolicy@e9ce15 owned
		// by: <other-thread-name>
		int posStart = line.indexOf(WAITING_ON_LOCK_TAG);
		if (posStart != -1)
			return line.substring(posStart + WAITING_ON_LOCK_TAG.length()).intern();
		
		return "";
	}
	
	private String parseLockClassName(String line) {
		// java.lang.Thread.State: BLOCKED on org.tcp.RRQueuePolicy@e9ce15 owned
		// by: <other-thread-name>
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
}
