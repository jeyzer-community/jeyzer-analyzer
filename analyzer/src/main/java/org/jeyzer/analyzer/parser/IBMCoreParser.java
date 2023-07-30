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

import com.google.common.primitives.Ints;

public class IBMCoreParser extends ThreadDumpParser {

	// format name
	public static final String FORMAT_NAME = "IBM core dump";
	public static final String FORMAT_SHORT_NAME = "IBM core dump";

	public static final String FOURTH_LINE = "1TISIGINFO";

	public static final String STACK_SECTION_END_TAG = "3XMTHREADINFO3           Native callstack:";
	public static final String STACK_SECTION_START_TAG = "3XMTHREADINFO "; // Space
																			// is
																			// important
																			// to
																			// distinguish
																			// it
																			// from
																			// 3XMTHREADINFO1,
																			// 3XMTHREADINFO2..
	private static final String STACK_LINE_DEAMON = "3XMJAVALTHREAD";
	// native thread id
	private static final String STACK_LINE_START_TAG = "4XESTACKTRACE";

	// stack info content
	private static final String PRIO_TAG = "prio=";
	private static final String DAEMON_TAG = "isDaemon:";
	private static final String STATE_TAG_START = "state:";
	private static final String STATE_TAG_END = ",";
	private static final String ID_TAG = "getId:";

	private static final String START_CODE_LINE_START_TAG = "4XESTACKTRACE            ";

	private static final Logger logger = LoggerFactory.getLogger(IBMCoreParser.class);

	@Override
	public void parseThreadDump(File file, Date date) {
		logger.info("Reading thread dump file : {}", file.getName());

		String line = null;

		boolean success = true;
		List<String> threadLines = new ArrayList<>();
		List<String> ownedLocks = new ArrayList<>(); // Not used
		List<String> biasedLocks = null; // not supported
		String lockedOn = EMPTY_STRING; // Not used
		String lockedOnClassName = EMPTY_STRING; // Not used
		ThreadDump dump = new ThreadDump(file, date);
		int lineCount = 0;
		int threadLinePos = 0;
		boolean stackFound = false;
		ThreadStack stack = null;

		/*
		 * Stack example :
		 * 
		 * s3XMTHREADINFO "VMTransport" J9VMThread:0x0000000001E88A00,
		 * j9thread_t:0x00002AAAC27754C0, java/lang/Thread:0x000000009422DB98,
		 * state:P, prio=5 3XMJAVALTHREAD (java/lang/Thread getId:0x87D,
		 * isDaemon:true) 3XMTHREADINFO1 (native thread ID:0x4D58, native
		 * priority:0x5, native policy:UNKNOWN) 3XMTHREADINFO2 (native stack
		 * address range from:0x00002AAACA9A2000, to:0x00002AAACA9E3000,
		 * size:0x41000) 3XMTHREADINFO3 Java callstack: 4XESTACKTRACE at
		 * sun/misc/Unsafe.park(Native Method) 4XESTACKTRACE at
		 * java/util/concurrent
		 * /locks/LockSupport.parkNanos(LockSupport.java:222(Compiled Code))
		 * 4XESTACKTRACE at
		 * java/util/concurrent/SynchronousQueue$TransferStack.awaitFulfill
		 * (SynchronousQueue.java:435(Compiled Code)) 4XESTACKTRACE at
		 * java/util/
		 * concurrent/SynchronousQueue$TransferStack.transfer(SynchronousQueue
		 * .java:334(Compiled Code)) 4XESTACKTRACE at
		 * java/util/concurrent/SynchronousQueue
		 * .poll(SynchronousQueue.java:885(Compiled Code)) 4XESTACKTRACE at
		 * java/
		 * util/concurrent/ThreadPoolExecutor.getTask(ThreadPoolExecutor.java
		 * :957(Compiled Code)) 4XESTACKTRACE at
		 * java/util/concurrent/ThreadPoolExecutor$Worker
		 * .run(ThreadPoolExecutor.java:917(Compiled Code)) 4XESTACKTRACE at
		 * java/lang/Thread.run(Thread.java:761(Compiled Code)) 3XMTHREADINFO3
		 * Native callstack: 4XENATIVESTACK (0x00002AAAABCA50E2
		 * [libj9prt24.so+0xf0e2]) 4XENATIVESTACK (0x00002AAAABCAF871
		 * [libj9prt24.so+0x19871]) 4XENATIVESTACK (0x00002AAAABCA516D
		 * [libj9prt24.so+0xf16d]) 4XENATIVESTACK (0x00002AAAABCA527A
		 * [libj9prt24.so+0xf27a]) 4XENATIVESTACK (0x00002AAAABCA4F24
		 * [libj9prt24.so+0xef24]) 4XENATIVESTACK (0x00002AAAABCAF871
		 * [libj9prt24.so+0x19871]) 4XENATIVESTACK (0x00002AAAABCA4F9D
		 * [libj9prt24.so+0xef9d]) 4XENATIVESTACK (0x00002AAAABCA070E
		 * [libj9prt24.so+0xa70e]) 4XENATIVESTACK (0x0000003AE040ECA0
		 * [libpthread.so.0+0xeca0]) 4XENATIVESTACK pthread_cond_timedwait+0x120
		 * (0x0000003AE040B280 [libpthread.so.0+0xb280]) 4XENATIVESTACK
		 * j9thread_park+0x11b (0x00002AAAABA8953B [libj9thr24.so+0x553b])
		 * 4XENATIVESTACK (0x00002AAAB0BA814D [libjclscar_24.so+0x5914d])
		 * 4XENATIVESTACK sun_misc_Unsafe_park+0x6a (0x00002AAAB0BA12FA
		 * [libjclscar_24.so+0x522fa]) NULL
		 */

		try (
				FileReader fr = new FileReader(file);
				BufferedReader reader = new BufferedReader(fr);
			)
		{
			while ((line = reader.readLine()) != null) {

				if (STACK_SECTION_END_TAG.equals(line)) {
					if (stackFound) {
						// time to flush if thread stack different than 1 line
						// entries as for example :
						// 3XMTHREADINFO Anonymous native thread
						// 3XMTHREADINFO1 (native thread ID:0x6761, native
						// priority: 0x0, native policy:UNKNOWN)
						// 3XMTHREADINFO3 Native callstack:
						// 4XENATIVESTACK (0x00002AAAABCA50E2
						// [libj9prt24.so+0xf0e2])
						// 4XENATIVESTACK (0x00002AAAABCAF871
						// [libj9prt24.so+0x19871])
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
						if (threadLines.size() > 2) {
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

				if (line.startsWith(STACK_SECTION_START_TAG)) {
					// 3XMTHREADINFO "VMTransport"
					// J9VMThread:0x0000000001E88A00,
					// j9thread_t:0x00002AAAC27754C0,
					// java/lang/Thread:0x000000009422DB98, state:P, prio=5
					threadLines.add(line);
					threadLinePos = lineCount;
					lineCount++;
					stackFound = true;
					continue;
				}

				if (line.startsWith(STACK_LINE_DEAMON)) {
					// 3XMJAVALTHREAD (java/lang/Thread getId:0x87D,
					// isDaemon:true)
					threadLines.add(line);
					lineCount++;
					continue;
				}

				/*
				 * Native thread id if (line.startsWith(STACK_LINE_THREAD_ID)){
				 * // 3XMTHREADINFO1 (native thread ID:0x4D58, native
				 * priority:0x5, native policy:UNKNOWN) threadLines.add(line);
				 * lineCount++; continue; }
				 */

				if (line.startsWith(STACK_LINE_START_TAG)) {
					// 4XESTACKTRACE at sun/misc/Unsafe.park(Native Method)
					threadLines.add(line);
					lineCount++;
					continue;
				}

				// Ignore everything else
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
		ThreadState state = StateParser.parseIBMState(header, STATE_TAG_START, STATE_TAG_END);
		int priority = parsePriority(header);

		// sub header
		String subheader = context.threadLines.get(1);
		String id = parseID(subheader);
		DAEMON daemon = parseDaemon(subheader);

		// cleanup stack lines
		List<String> codeLines = cleanupStackLines(context.threadLines.subList(2,
				context.threadLines.size()));

		return new ThreadStackImpl(header, name, id, state, false,
				context.filePos, context.fileName, context.timestamp, codeLines, 
				context.lockedOn, context.lockedOnClassName, context.ownedLocks, 
				context.biasedLocks, false, daemon, priority);
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
	public boolean isLockIdUsed() {
		return false;
	}

	/**
	 * Parsing
	 */

	@Override
	protected String parseName(String header) {
		// 3XMTHREADINFO "VMTransport" J9VMThread:0x0000000001E88A00,
		// j9thread_t:0x00002AAAC27754C0, java/lang/Thread:0x000000009422DB98,
		// state:P, prio=5
		int startpos = header.indexOf('\"', 1) + 1;
		int endpos = header.indexOf('\"', startpos + 1);
		String name = header.substring(startpos, endpos);

		return name.intern();
	}

	private int parsePriority(String header) {
		// 3XMTHREADINFO "VMTransport" J9VMThread:0x0000000001E88A00,
		// j9thread_t:0x00002AAAC27754C0, java/lang/Thread:0x000000009422DB98,
		// state:P, prio=5
		int posStart = header.indexOf(PRIO_TAG) + PRIO_TAG.length();
		Integer priority = Ints.tryParse(header.substring(posStart));
		if (priority == null){
			logger.error(
					"Failed to parse priority : \"{}\" on header \"{}\". Defaulting to prio -1",
					header.substring(posStart), header);
			return ThreadStack.PRIORITY_NOT_AVAILABLE;
		}

		return priority;
	}

	private String parseID(String header) {
		// 3XMJAVALTHREAD (java/lang/Thread getId:0x87D, isDaemon:true)
		int posStart = header.indexOf(ID_TAG) + ID_TAG.length();
		int posEnd = header.indexOf(',', posStart);
		return header.substring(posStart, posEnd).intern();
	}

	private DAEMON parseDaemon(String header) throws ParseException {
		// 3XMJAVALTHREAD (java/lang/Thread getId:0x87D, isDaemon:true)
		int posStart = header.indexOf(DAEMON_TAG) + DAEMON_TAG.length();
		int posEnd = header.indexOf(')', posStart);

		String value = header.substring(posStart, posEnd);

		if (Boolean.FALSE.toString().equals(value)) {
			return DAEMON.FALSE;
		} else if (Boolean.TRUE.toString().equals(value)) {
			return DAEMON.TRUE;
		} else {
			throw new ParseException("Thread daemon value recognized : " + value
					+ " on sub header : " + header, -1);
		}
	}

	private List<String> cleanupStackLines(List<String> stackLines) {
		List<String> codeLines = new ArrayList<>();

		for (String line : stackLines) {
			String cleanLine = line.replace(START_CODE_LINE_START_TAG, "");
			// replace "/" with "."
			cleanLine = cleanLine.replace('/', '.');
			codeLines.add(cleanLine.intern());
		}

		return codeLines;
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
}
