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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStackImpl;
import org.jeyzer.analyzer.data.stack.ThreadState;
import org.jeyzer.analyzer.data.stack.ThreadStack.DAEMON;
import org.jeyzer.analyzer.error.JzrLineParsingException;
import org.jeyzer.analyzer.error.JzrParsingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;

public class JstackParser extends ThreadDumpParser {

	// format name
	public static final String FORMAT_NAME = "Jstack jdk 1.6+ tool";
	public static final String FORMAT_SHORT_NAME = "Jstack 1.6+";

	public static final String TD_HEADER_PATTERN = "Full thread dump";

	private static final Logger logger = LoggerFactory.getLogger(JstackParser.class);

	// Stack content
	public static final String DAEMON_TAG = "daemon";
	public static final String PRIO_TAG = "prio=";
	public static final String ID_TAG = "tid=";
	public static final String STATE_TAG_START = "   java.lang.Thread.State: ";
	public static final String STATE_TAG_END = " ";
	protected static final String CARRYING_VIRTUAL_THREAD = "   Carrying virtual";
	protected static final String SUSPENDED = "at breakpoint";

	// dump content
	public static final String STACK_HEADER_START_TAG = "\"";
	public static final String STACK_LINE_START_TAG = "\tat";
	public static final String WAITING_ON_LOCK_TAG = "\t- waiting to lock <";
	public static final String WAITING_ON_LOCK_NAME_TAG = "> (a ";
	public static final char   WAITING_ON_LOCK_END_TAG = ')';
	public static final String LOCK_OWNER_TAG = "\t- locked <";
	public static final String LOCK_ELIMNINATED = "\t- eliminated <";
	public static final char   LOCK_TAG_END = '>';

	// end of file
	// Jstack : JNI global references:
	// JFR :    JNI global refs:
	public static final String DUMP_END_MARKER = "JNI global ref";

	// deadlock
	public static final String FOUND_ONE_JAVA_DEADLOCK = "Found one Java-level deadlock:";
	public static final String JAVA_STACK_INFOMATION = "Java stack information";
	
	public static final String WAIT_TAG = "\t- waiting on <";
	
	public JstackParser() {
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
		List<String> biasedLocks = new ArrayList<>(0); // unlikely
		Multimap<String,ThreadStack> stacks = ArrayListMultimap.create(); // Thread name is not unique so multi map is used. Reason : unfortunately, deadlock detection refers to thread name
		String lockedOn = EMPTY_STRING;
		String lockedOnClassName = EMPTY_STRING;
		ThreadDump dump = new ThreadDump(file, date);
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
			lineCount = parseHeader(dump, reader, file, dumpContext);

			while ((line = reader.readLine()) != null) {
				if (line.length() == 0) {
					if (stackFound) {
						// time to flush if thread stack different than 1 line
						// entries as for example :
						// "VM Thread" prio=10 tid=0x000000000d5d3000 nid=0x123c
						// runnable
						// "GC task thread#0 (ParallelGC)" prio=6
						// tid=0x0000000002c54800 nid=0x12e8 runnable
						// "GC task thread#1 (ParallelGC)" prio=6
						// tid=0x0000000002c56000 nid=0x1304 runnable
						// "GC task thread#2 (ParallelGC)" prio=6
						// tid=0x0000000002c57800 nid=0x15c4 runnable
						// "GC task thread#3 (ParallelGC)" prio=6
						// tid=0x0000000002c59800 nid=0x1310 runnable
						// "VM Periodic Task Thread" prio=10
						// tid=0x000000000d6bf000 nid=0x17dc waiting on
						// condition
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
					// "transaction-1" daemon prio=6 tid=0x00000000139b7800
					// nid=0x1b70 waiting for monitor entry [0x000000001e22f000]
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

				if (line.startsWith(STATE_TAG_START)) {
					// java.lang.Thread.State: BLOCKED (on object monitor)
					threadLines.add(line);
					lineCount++;
					continue;
				}
				
				if (line.startsWith(CARRYING_VIRTUAL_THREAD)) {
					// Carrying virtual thread #29
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
					// - locked <0x000000078d356b28> (a <class name with full package>)
					int pos = LOCK_OWNER_TAG.length();
					int endPos = line.indexOf(LOCK_TAG_END, pos);
					String hexavalue = line.substring(pos, endPos);
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
				
				if (line.startsWith(LOCK_ELIMNINATED)) {
					// - eliminated <owner is scalar replaced> (a <class name with full package>) at <standard stack line to keep>
					
					// get biased lock name
					String biasedLockName = threadLines.get(threadLines.size()-1); // this is the previous line
					
					// add code line
					int pos = line.indexOf(STACK_LINE_START_TAG, LOCK_ELIMNINATED.length()) + 1; // \tat<space>
					String codeLine = line.substring(pos);
					threadLines.add(codeLine);
					
					// add current line
					biasedLocks.add(biasedLockName + "\n" + codeLine);
					
					lineCount++;
					continue;
				}
				
				if (line.startsWith(DUMP_END_MARKER)){
					lineCount++;
					break; // nothing else to parse
				}
					
				// Ignore :
				// - parking to wait for <0x00000007f0df1370> (a
				// java.util.concurrent.SynchronousQueue$TransferStack)
				lineCount++;

				/**
				 * Sample with deadlock found (end of file content)
				 * Obtained with jstack -l
				 * 
				 *JNI global references: 60
				 * 
				 * 
				 *Found one Java-level deadlock:
				 *=============================
				 *"Philosopher-5":
				 *  waiting to lock monitor 0x0000000018509688 (object 0x0000000092f04058, a org.jeyzer.demo.philosopher2.a.e),
				 *  which is held by "Philosopher-4"
				 *"Philosopher-4":
				 *  waiting to lock monitor 0x00000000029c5548 (object 0x0000000092f04208, a org.jeyzer.demo.philosopher2.a.d),
				 *  which is held by "Philosopher-3"
				 *"Philosopher-3":
				 *  waiting to lock monitor 0x000000001850ab28 (object 0x0000000092f04440, a org.jeyzer.demo.philosopher2.a.c),
				 *  which is held by "Philosopher-2"
				 *"Philosopher-2":
				 *  waiting to lock monitor 0x000000001850ce38 (object 0x0000000092f045c8, a org.jeyzer.demo.philosopher2.a.b),
				 *  which is held by "Philosopher-1"
				 *"Philosopher-1":
				 *  waiting to lock monitor 0x000000001850b8e8 (object 0x0000000092f03ea8, a org.jeyzer.demo.philosopher2.a.a),
				 *  which is held by "Philosopher-5"
				 * 
				 *Java stack information for the threads listed above:
				 *===================================================
				 *"Philosopher-5":
				 * 	at org.jeyzer.demo.philosopher2.a.e.b(SourceFile:28)
				 * 	- waiting to lock <0x0000000092f04058> (a org.jeyzer.demo.philosopher2.a.e)
				 * 	at org.jeyzer.demo.philosopher2.b.b(SourceFile:70)
				 * 	at org.jeyzer.demo.philosopher2.a.a.c(SourceFile:24)
				 * 	at org.jeyzer.demo.philosopher2.a.a.a(SourceFile:13)
				 * 	- locked <0x0000000092f03ea8> (a org.jeyzer.demo.philosopher2.a.a)
				 * 	at org.jeyzer.demo.philosopher2.b.a(SourceFile:61)
				 * 	at org.jeyzer.demo.philosopher2.b.run(SourceFile:45)
				 * 	at java.lang.Thread.run(Thread.java:745)
				 *"Philosopher-4":
				 * 	at org.jeyzer.demo.philosopher2.a.d.b(SourceFile:28)
				 * 	- waiting to lock <0x0000000092f04208> (a org.jeyzer.demo.philosopher2.a.d)
				 * 	at org.jeyzer.demo.philosopher2.b.b(SourceFile:70)
				 * 	at org.jeyzer.demo.philosopher2.a.e.c(SourceFile:24)
				 * 	at org.jeyzer.demo.philosopher2.a.e.a(SourceFile:13)
				 * 	- locked <0x0000000092f04058> (a org.jeyzer.demo.philosopher2.a.e)
				 * 	at org.jeyzer.demo.philosopher2.b.a(SourceFile:61)
				 * 	at org.jeyzer.demo.philosopher2.b.run(SourceFile:45)
				 * 	at java.lang.Thread.run(Thread.java:745)
				 *"Philosopher-3":
				 * 	at org.jeyzer.demo.philosopher2.a.c.b(SourceFile:28)
				 * 	- waiting to lock <0x0000000092f04440> (a org.jeyzer.demo.philosopher2.a.c)
				 * 	at org.jeyzer.demo.philosopher2.b.b(SourceFile:70)
				 * 	at org.jeyzer.demo.philosopher2.a.d.c(SourceFile:24)
				 * 	at org.jeyzer.demo.philosopher2.a.d.a(SourceFile:13)
				 * 	- locked <0x0000000092f04208> (a org.jeyzer.demo.philosopher2.a.d)
				 * 	at org.jeyzer.demo.philosopher2.b.a(SourceFile:61)
				 * 	at org.jeyzer.demo.philosopher2.b.run(SourceFile:45)
				 * 	at java.lang.Thread.run(Thread.java:745)
				 *"Philosopher-2":
				 * 	at org.jeyzer.demo.philosopher2.a.b.b(SourceFile:28)
				 * 	- waiting to lock <0x0000000092f045c8> (a org.jeyzer.demo.philosopher2.a.b)
				 * 	at org.jeyzer.demo.philosopher2.b.b(SourceFile:70)
				 * 	at org.jeyzer.demo.philosopher2.a.c.c(SourceFile:24)
				 * 	at org.jeyzer.demo.philosopher2.a.c.a(SourceFile:13)
				 * 	- locked <0x0000000092f04440> (a org.jeyzer.demo.philosopher2.a.c)
				 * 	at org.jeyzer.demo.philosopher2.b.a(SourceFile:61)
				 * 	at org.jeyzer.demo.philosopher2.b.run(SourceFile:45)
				 * 	at java.lang.Thread.run(Thread.java:745)
				 *"Philosopher-1":
				 * 	at org.jeyzer.demo.philosopher2.a.a.b(SourceFile:28)
				 * 	- waiting to lock <0x0000000092f03ea8> (a org.jeyzer.demo.philosopher2.a.a)
				 * 	at org.jeyzer.demo.philosopher2.b.b(SourceFile:70)
				 * 	at org.jeyzer.demo.philosopher2.a.b.c(SourceFile:24)
				 * 	at org.jeyzer.demo.philosopher2.a.b.a(SourceFile:13)
				 * 	- locked <0x0000000092f045c8> (a org.jeyzer.demo.philosopher2.a.b)
				 * 	at org.jeyzer.demo.philosopher2.b.a(SourceFile:61)
				 * 	at org.jeyzer.demo.philosopher2.b.run(SourceFile:45)
				 * 	at java.lang.Thread.run(Thread.java:745)
				 * 	
				 *Found 1 deadlock.
				*/
				
				/**
				 * The above 
				 * "Found one Java-level deadlock" 
				 * and
				 * * "Java stack information for the threads listed above:" 
				 * blocks are repeated for each deadlock. 
				 * It always end with :
				 * Found <x> deadlock[s]. 
				*/

			}

			// process deadlocks
			while((line = reader.readLine()) != null){
				lineCount++;
				if (line.startsWith(FOUND_ONE_JAVA_DEADLOCK)){
					lineCount = extractDeadLock(dump, stacks, reader, lineCount);
				}
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

	protected int parseHeader(ThreadDump dump, BufferedReader reader, File file, Map<String, Object> dumpContext) throws IOException, JzrParsingException {
		// First 3 lines --> ignore
		/*
		 * 2014-03-18 19:06:00 
		 * Full thread dump Java HotSpot(TM) 64-Bit Server VM (20.4-b02 mixed mode): 
		 * <empty line>
		 */

		// Variant with JZR header : extract capture time
		/*
		 * Full Java thread dump from Jstack : C:\Dev\programs\Java\jdk1.8.0_25\bin\jstack -l 6160
		 * 	J#>	capture time	594
		 * <empty line>
		 * 2015-05-24 09:19:37
		 * Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.40-b25 mixed mode):  
		 * <empty line>
		 */
		
		long captureTime = -1;
		int lineCount = 0;
		
		String line1 = reader.readLine();
		String line2 = reader.readLine();
		
		if (JstackHelper.detectTDGHeader(line1)){
			captureTime = JstackHelper.parseCaptureTime(line2);
			reader.readLine(); // line 3  (empty)
			reader.readLine(); // line 4
			String line5 = reader.readLine(); // line 5
			JstackHelper.checkEmptyThreadDumpVariant(file, line5);
			reader.readLine(); // line 6   (empty)
			lineCount = lineCount + 6;
		}
		else{
			JstackHelper.checkEmptyThreadDumpVariant(file, line2);
			reader.readLine(); // line 3   (empty)
			lineCount = lineCount + 3;
		}
		
		dump.setCaptureTime(captureTime);
		return lineCount;
	}

	protected boolean isRecordableStack(List<String> threadLines) {
		// JEYZ-29 Jeyzer Analyzer failed to parse consecutive thread names (jstack case)
		return (threadLines.size() > 1 && !threadLines.get(1).startsWith("\""));
	}

	private int extractDeadLock(ThreadDump dump, Multimap<String, ThreadStack> stacks, BufferedReader reader, int lineCount) throws IOException {
		StringBuilder deadlockText = new StringBuilder(5000);
		String line;
		
//		Found one Java-level deadlock:
//		=============================
//		"Philosopher-10":
//		  waiting to lock monitor 0x0000000017ef8208 (object 0x0000000093456128, a org.jeyzer.demo.philosopher2.a.a),
//		  which is held by "Philosopher-6"
//		"Philosopher-6":
//		  waiting to lock monitor 0x00000000181e5138 (object 0x00000000922a4b58, a org.jeyzer.demo.philosopher2.a.b),
//		  which is held by "Philosopher-7"
//		"Philosopher-7":
//		  waiting to lock monitor 0x0000000002335c98 (object 0x00000000922d0a78, a org.jeyzer.demo.philosopher2.a.c),
//		  which is held by "Philosopher-8"
//		"Philosopher-8":
//		  waiting to lock monitor 0x0000000017ef9808 (object 0x00000000934693f0, a org.jeyzer.demo.philosopher2.a.d),
//		  which is held by "Philosopher-9"
//		"Philosopher-9":
//		  waiting to lock monitor 0x00000000181e9288 (object 0x0000000093456100, a org.jeyzer.demo.philosopher2.a.e),
//		  which is held by "Philosopher-10"
//		<empty line>
//		Java stack information for the threads listed above:
//		===================================================
//		"Philosopher-10":
//			at org.jeyzer.demo.philosopher2.a.a.b(SourceFile:28)
//			- waiting to lock <0x0000000093456128> (a org.jeyzer.demo.philosopher2.a.a)
//			at org.jeyzer.demo.philosopher2.b.b(SourceFile:70)
//			at org.jeyzer.demo.philosopher2.a.e.c(SourceFile:24)
//			at org.jeyzer.demo.philosopher2.a.e.a(SourceFile:13)
//			- locked <0x0000000093456100> (a org.jeyzer.demo.philosopher2.a.e)
//			at org.jeyzer.demo.philosopher2.b.a(SourceFile:61)
//			at org.jeyzer.demo.philosopher2.b.run(SourceFile:45)
//			at java.lang.Thread.run(Thread.java:745)
//		"Philosopher-6":
//			at org.jeyzer.demo.philosopher2.a.b.b(SourceFile:28)
//			- waiting to lock <0x00000000922a4b58> (a org.jeyzer.demo.philosopher2.a.b)
//			at org.jeyzer.demo.philosopher2.b.b(SourceFile:70)
//			at org.jeyzer.demo.philosopher2.a.a.c(SourceFile:24)
//			at org.jeyzer.demo.philosopher2.a.a.a(SourceFile:13)
//			- locked <0x0000000093456128> (a org.jeyzer.demo.philosopher2.a.a)
//			at org.jeyzer.demo.philosopher2.b.a(SourceFile:61)
//			at org.jeyzer.demo.philosopher2.b.run(SourceFile:45)
//			at java.lang.Thread.run(Thread.java:745)
//		"Philosopher-7":
//			at org.jeyzer.demo.philosopher2.a.c.b(SourceFile:28)
//			- waiting to lock <0x00000000922d0a78> (a org.jeyzer.demo.philosopher2.a.c)
//			at org.jeyzer.demo.philosopher2.b.b(SourceFile:70)
//			at org.jeyzer.demo.philosopher2.a.b.c(SourceFile:24)
//			at org.jeyzer.demo.philosopher2.a.b.a(SourceFile:13)
//			- locked <0x00000000922a4b58> (a org.jeyzer.demo.philosopher2.a.b)
//			at org.jeyzer.demo.philosopher2.b.a(SourceFile:61)
//			at org.jeyzer.demo.philosopher2.b.run(SourceFile:45)
//			at java.lang.Thread.run(Thread.java:745)
//		"Philosopher-8":
//			at org.jeyzer.demo.philosopher2.a.d.b(SourceFile:28)
//			- waiting to lock <0x00000000934693f0> (a org.jeyzer.demo.philosopher2.a.d)
//			at org.jeyzer.demo.philosopher2.b.b(SourceFile:70)
//			at org.jeyzer.demo.philosopher2.a.c.c(SourceFile:24)
//			at org.jeyzer.demo.philosopher2.a.c.a(SourceFile:13)
//			- locked <0x00000000922d0a78> (a org.jeyzer.demo.philosopher2.a.c)
//			at org.jeyzer.demo.philosopher2.b.a(SourceFile:61)
//			at org.jeyzer.demo.philosopher2.b.run(SourceFile:45)
//				at java.lang.Thread.run(Thread.java:745)
//		"Philosopher-9":
//			at org.jeyzer.demo.philosopher2.a.e.b(SourceFile:28)
//			- waiting to lock <0x0000000093456100> (a org.jeyzer.demo.philosopher2.a.e)
//			at org.jeyzer.demo.philosopher2.b.b(SourceFile:70)
//			at org.jeyzer.demo.philosopher2.a.d.c(SourceFile:24)
//			at org.jeyzer.demo.philosopher2.a.d.a(SourceFile:13)
//			- locked <0x00000000934693f0> (a org.jeyzer.demo.philosopher2.a.d)
//			at org.jeyzer.demo.philosopher2.b.a(SourceFile:61)
//			at org.jeyzer.demo.philosopher2.b.run(SourceFile:45)
//			at java.lang.Thread.run(Thread.java:745)
				
		reader.readLine();   // =============================
		lineCount++;
		
		line = reader.readLine();
		lineCount++;
		
		deadlockText.append(FOUND_ONE_JAVA_DEADLOCK);
		deadlockText.append("\n=============================\n");
		
		boolean foundJavaStackInfo = false;
		while(!foundJavaStackInfo && !line.isEmpty()){
			if (line.startsWith(JAVA_STACK_INFOMATION))
				foundJavaStackInfo = true;
			
			if (!foundJavaStackInfo && line.startsWith(STACK_HEADER_START_TAG)){
				int pos = line.indexOf('\"',1);
				String name = line.substring(1, pos);
				Collection<ThreadStack> stackObjects = stacks.get(name);
				if (stackObjects != null){
					for (ThreadStack stack : stackObjects){
						if (stack.isBlocked())
							stack.setInDeadlock(true);
					}
				}else{
					logger.warn("Failed to find thread in deadlock from parsed stacks for the thread : " + name);
				}
			}
			
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
		String header = context.threadLines.get(0).intern();
		String name = parseName(header);
		String id = parseID(header);
		ThreadState state = StateParser.parseState(context.threadLines.get(1), STATE_TAG_START, STATE_TAG_END);

		// suspended
		boolean suspended = header.indexOf(SUSPENDED) != -1;
		
		// Jstack extra info
		DAEMON daemon = parseDaemon(header);
		int priority = parsePriority(header);
		
		// stack section - store it as intern to save memory
		List<String> codeLines = internCodeLines(context);

		return new ThreadStackImpl(header, name, id, state, suspended,
				context.filePos, context.fileName, context.timestamp, codeLines, 
				context.lockedOn, context.lockedOnClassName, context.ownedLocks, context.biasedLocks, false,
				daemon, priority);
	}

	protected List<String> internCodeLines(StackContext context) {
		List<String> codeLines = new ArrayList<>(context.threadLines.size()-2);
		for (String lineToIntern : context.threadLines.subList(2,context.threadLines.size())){
			codeLines.add(lineToIntern.intern());
		}
		return codeLines;
	}

	protected String parseID(String header) throws ParseException {
		int posStart = header.indexOf(ID_TAG) + ID_TAG.length();
		int posEnd = header.indexOf(' ', posStart);
		String hexaValue = header.substring(posStart, posEnd);
		long id;

		try {
			id = Long.parseLong(hexaValue.substring(2), 16);
		} catch (NumberFormatException e) {
			logger.error("Hexadecimal value conversion failed.", e);
			throw new ParseException("Failed to parse thread id : \""
					+ header.substring(posStart, posEnd) + "\" on header \""
					+ header + "\".", -1);
		}

		return Long.toString(id).intern();
	}

	protected DAEMON parseDaemon(String header) {
		return (header.contains(DAEMON_TAG)) ? DAEMON.TRUE : DAEMON.FALSE;
	}

	protected int parsePriority(String header) {
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
		return true;
	}
	
	@Override
	public boolean isLockCycleDetectionUsed(){
		return true;
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
		return true;
	}

	@Override
	public boolean hasVirtualThreadSupport() {
		return true;
	}
}
