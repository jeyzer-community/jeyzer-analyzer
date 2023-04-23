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
import java.io.IOException;
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

/**
 * Jstack hung format parser (jstack -F option)
 *
 */
public class JstackHungParser extends ThreadDumpParser {

	// format name
	public static final String FORMAT_NAME = "Jstack jdk 1.6+ tool Hung mode";
	public static final String FORMAT_SHORT_NAME = "Jstack 1.6+ Hung";

	public static final String FIRST_OR_FIFTH_LINE = "Deadlock Detection:";
	
	public static final String FOUND_TOTAL_TAG = "Found a total of";
	public static final String NO_DEADLOCKS_FOUND_TAG = "No deadlocks found.";	

	public static final String FOUND_ONE_JAVA_DEADLOCK = "Found one Java-level deadlock:";
	
	private static final Logger logger = LoggerFactory.getLogger(JstackHungParser.class);

	// Stack content
	public static final String THREAD_TAG = "Thread ";
	public static final String STATE_TAG_START = "state = ";
	public static final String STATE_TAG_END = ")";

	// dump content
	public static final String STACK_LINE_START_TAG = " - ";
	public static final String INTERPRATED_FRAME_TAG = "(Interpreted";
	public static final String COMPILED_FRAME_TAG = "(Compiled";

	public JstackHungParser() {
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
		String lockedOn = EMPTY_STRING; // Not used
		String lockedOnClassName = EMPTY_STRING; // Not used
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
			// Variant Linux :
			/*
			 * Deadlock Detection: 
			 * <empty line> 
			 * No deadlocks found. <OR deadlock block below>
			 * <empty line>
			 */

			// Variant Windows : 
			/*
			 * Attaching to process ID 6804, please wait...
			 * Debugger attached successfully.
			 * Server compiler detected.
			 * JVM version is 25.45-b02
			 * Deadlock Detection:
			 * <empty line> 
			 * No deadlocks found.
			 * <empty line>
			 */
			
			// Variant with JZR header : extract capture time
			/*
			 * Full Java thread dump from Jstack : C:\Dev\programs\Java\jdk1.8.0_25\bin\jstack -F 6160
			 * 	J#>	capture time	594
			 * <empty line>
			 * <jstack output like above>
			 */
			
			// Variant deadlock found block :
			 /*
			  * Found one Java-level deadlock:
			  * =============================
			  * <empty line>
			  * "Philosopher-2": 
			  *   waiting to lock Monitor@0x000000001812df48 (Object@0x0000000090c17c80, a org/rwn/jeyzer/demo/philosopher2/a/c),
			  *   which is held by "Philosopher-3"
			  * "Philosopher-3": 
			  *   waiting to lock Monitor@0x000000001812e628 (Object@0x0000000090c18090, a org/rwn/jeyzer/demo/philosopher2/a/d),
			  *   which is held by "Philosopher-4"
			  * "Philosopher-4": 
			  *   waiting to lock Monitor@0x000000001812e6d8 (Object@0x0000000090a003f8, a org/rwn/jeyzer/demo/philosopher2/a/e),
			  *   which is held by "Philosopher-5"
			  * "Philosopher-5":
			  *   waiting to lock Monitor@0x0000000018131858 (Object@0x0000000090c17ff8, a org/rwn/jeyzer/demo/philosopher2/a/a),
			  *   which is held by "Philosopher-1"
			  * "Philosopher-1": 
			  *   waiting to lock Monitor@0x0000000002514958 (Object@0x0000000090c17e30, a org/rwn/jeyzer/demo/philosopher2/a/b),
			  *   which is held by "Philosopher-2"
			  * <empty line> 
			  * Found a total of 1 deadlock. 
			  */
			
			String line1 = reader.readLine();
			String line2 = reader.readLine();
			
			if (JstackHelper.detectTDGHeader(line1)){
				captureTime = JstackHelper.parseCaptureTime(line2);
			}
			lineCount = lineCount + 2;
			
			dump.setCaptureTime(captureTime);
			
			line = reader.readLine();
			lineCount++;
			JstackHelper.checkEmptyThreadDumpVariant(file, line);
			
			while (!(line.startsWith(FOUND_TOTAL_TAG) 
					|| line.startsWith(NO_DEADLOCKS_FOUND_TAG))) {
				line = reader.readLine();
				lineCount++;
				
				if (line.startsWith(FOUND_ONE_JAVA_DEADLOCK)){
					lineCount = extractDeadLock(dump, reader, lineCount);
				}
				
				JstackHelper.checkEmptyThreadDumpVariant(file, line);
			}
			
			line = reader.readLine();
			lineCount++;

			while ((line = reader.readLine()) != null) {

				if (line.length() == 0) {
					if (stackFound) {
						// time to flush if thread stack different than 1 line
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
						lockedOn = EMPTY_STRING;
						lockedOnClassName = EMPTY_STRING;
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

				if (line.startsWith(THREAD_TAG)) {
					// Thread 23: (state = BLOCKED)
					threadLines.add(line);
					threadLinePos = lineCount;
					lineCount++;
					stackFound = true;
					continue;
				}

				if (line.startsWith(STACK_LINE_START_TAG)) {
					// - org.jeyzer.demo.philosopher2.a.a.b(org.jeyzer.demo.philosopher2.b) @bci=0, line=28 (Interpreted frame)
					threadLines.add(line);
					lineCount++;
					continue;
				}

				lineCount++;

				/**
				 * Sample :
				 * 
				 * Thread 23: (state = BLOCKED)
				 *  - org.jeyzer.demo.philosopher2.a.a.b(org.jeyzer.demo.philosopher2.b) @bci=0, line=28 (Interpreted frame)
 				 *  - org.jeyzer.demo.philosopher2.b.b() @bci=33, line=70 (Interpreted frame)
 				 *  - org.jeyzer.demo.philosopher2.a.e.c(org.jeyzer.demo.philosopher2.b) @bci=19, line=24 (Interpreted frame)
 				 *  - org.jeyzer.demo.philosopher2.a.e.a(org.jeyzer.demo.philosopher2.b) @bci=2, line=13 (Interpreted frame)
 				 *  - org.jeyzer.demo.philosopher2.b.a() @bci=90, line=61 (Interpreted frame)
 				 *  - org.jeyzer.demo.philosopher2.b.run() @bci=29, line=45 (Interpreted frame)
 				 *  - java.lang.Thread.run() @bci=11, line=745 (Interpreted frame)
				 */
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

	private int extractDeadLock(ThreadDump dump, BufferedReader reader, int lineCount) throws IOException {
		StringBuilder deadlockText = new StringBuilder(2000);
		String line;
		
//		Found one Java-level deadlock:
//		=============================
//      <empty line>
//		"Philosopher-9":
//		  waiting to lock Monitor@0x00000000181e9288 (Object@0x0000000093456100, a org/rwn/jeyzer/demo/philosopher2/a/e),
//		  which is held by "Philosopher-10"
//		"Philosopher-10":
//		  waiting to lock Monitor@0x0000000017ef8208 (Object@0x0000000093456128, a org/rwn/jeyzer/demo/philosopher2/a/a),
//		  which is held by "Philosopher-6"
//		"Philosopher-6":
//		  waiting to lock Monitor@0x00000000181e5138 (Object@0x00000000922a4b58, a org/rwn/jeyzer/demo/philosopher2/a/b),
//		  which is held by "Philosopher-7"
//		"Philosopher-7":
//		  waiting to lock Monitor@0x0000000002335c98 (Object@0x00000000922d0a78, a org/rwn/jeyzer/demo/philosopher2/a/c),
//		  which is held by "Philosopher-8"
//		"Philosopher-8":
//		  waiting to lock Monitor@0x0000000017ef9808 (Object@0x00000000934693f0, a org/rwn/jeyzer/demo/philosopher2/a/d),
//		  which is held by "Philosopher-9"
//      <empty line>
	
		reader.readLine();   // =============================
		lineCount++;
		reader.readLine();	 // empty line
		lineCount++;
	
		deadlockText.append(FOUND_ONE_JAVA_DEADLOCK);
		deadlockText.append("\n=============================\n");
		
		line = reader.readLine();
		lineCount++;
		
		while(!line.isEmpty()){
			deadlockText.append(line);
			deadlockText.append("\n");
			line = reader.readLine();
			lineCount++;
		}
		
		deadlockText.append("\n");
		
		dump.addDeadLock(deadlockText.toString());
		
		return lineCount; // reader has just read the empty line
	}

	@Override
	public ThreadStack parseThreadStack(StackContext context) throws ParseException {
		// header section
		String header = context.threadLines.get(0);
		String name = parseName(header);
		String id = parseID(header);
		
		// Thread 23: (state = BLOCKED)
		ThreadState state = StateParser.parseHungState(header, STATE_TAG_START, STATE_TAG_END);

		// stack section - store it as intern to save memory
		List<String> codeLines = new ArrayList<>(context.threadLines.size()-1);
		for (String lineToIntern : context.threadLines.subList(1,context.threadLines.size())){
			codeLines.add(lineToIntern.intern());
		}

		return new ThreadStackImpl(header, name, id, state, false,
				context.filePos, context.fileName, context.timestamp, codeLines, 
				context.lockedOn, context.lockedOnClassName, context.ownedLocks, false);
	}

	private String parseID(String header) {
		int posStart = header.indexOf(THREAD_TAG) + THREAD_TAG.length();
		int posEnd = header.indexOf(':', posStart);
		return header.substring(posStart, posEnd).intern();
	}
	
	/**
	 * Parse thread name
	 * 
	 * Thread name : "Thread 23"
	 */
	@Override
	protected String parseName(String header){
		// Example : 
		//   Thread 23: (state = BLOCKED)
		// Extract "Thread 23"
		int pos = header.indexOf(':',THREAD_TAG.length());
		String name = header.substring(0, pos);
		
		return name.intern();
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
		return true;
	}
	
	@Override
	public boolean isLockCycleDetectionUsed(){
		return true;
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
