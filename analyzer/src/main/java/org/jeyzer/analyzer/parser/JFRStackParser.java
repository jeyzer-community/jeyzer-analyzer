package org.jeyzer.analyzer.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStackImpl;
import org.jeyzer.analyzer.data.stack.ThreadState;
import org.jeyzer.analyzer.data.stack.ThreadStack.DAEMON;
import org.jeyzer.analyzer.error.JzrParsingException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.parser.advanced.DumpBeanInfoParser;
import org.jeyzer.analyzer.setup.JzrSetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Floats;
import com.google.common.primitives.Longs;

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



public class JFRStackParser extends JstackParser {

	// format name
	public static final String FORMAT_NAME = "JFR Recording";
	public static final String FORMAT_SHORT_NAME = "JFR Recording";

	public static final String FIRST_LINE = "JFR Recording";
	
	private static final Logger logger = LoggerFactory.getLogger(JFRStackParser.class);

	private static final String JZ_PREFIX = "\tJz>\t";
	
	private static final String JZT_PREFIX = "\tJzt>\t";
	private static final String JZT_MEMORY_PREFIX = JZT_PREFIX + "memory\t";
	
	private static final String CPU_TAG = "cpu=";
	private static final char SIMPLE_ID_TAG = '#';
	
	private DumpBeanInfoParser dumpBeanInfoParser;     // important : must be state less
	private boolean virtualThreadVariationCountersUsed;
	
	public JFRStackParser(JzrSetupManager setupMgr) {
		super();
		this.dumpBeanInfoParser = new DumpBeanInfoParser(setupMgr);
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
	public boolean isCPUMeasurementUsed(){
		return true;
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
		return false;
	}
	
	@Override
	public boolean isProcessUpTimeMeasurementUsed() {
		return true;
	}
	
	@Override
	public boolean areVirtualThreadVariationCountersUsed() {
		return virtualThreadVariationCountersUsed;
	}
	
	@Override
	protected int parseHeader(ThreadDump dump, BufferedReader reader, File file, Map<String, Object> dumpContext) throws IOException, JzrParsingException {
		int lineCount = 0;

		// JFR Recording
		// Jz>	system cpu	0.0035288865
		// Jz>	system free memory	16686727168
		// Jz>	system total memory	34081480704
		// ...
		// <empty line>

		// Note : 2021-03-03 11:44:55 is ignored. JZR file name is processed instead
		
		String line = reader.readLine();
		lineCount++;

		line = reader.readLine();
		lineCount++;
		while(line!=null && line.startsWith(JZ_PREFIX)){
			dumpBeanInfoParser.parse(dump, line.substring(JZ_PREFIX.length()));
			lineCount++;
			line = reader.readLine();
		}
		
		if (dump.getVirtualThreads().hasCreatedCount() && dump.getVirtualThreads().hasTerminatedCount())
			virtualThreadVariationCountersUsed = true;

		lineCount = parseThreadMemoryFigures(dumpContext, reader, lineCount);
		
		// 2021-03-03 11:44:55
		// Full thread dump Java HotSpot(TM) 64-Bit Server VM (11.0.6+8-LTS mixed mode):
		// <empty line>
		line = reader.readLine();
		line = reader.readLine();
		line = reader.readLine();
		lineCount += 3;
		
		return lineCount;
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
		
		// CPU thread
		long cpu = parseCPU(header);
		
		// Memory 
		String simpleId = parseSimpleID(header);
		Long memory = -1L;
		if (simpleId != null) {
			@SuppressWarnings("unchecked")
			Map<String,Long> threadMemory = (Map<String, Long>)context.any.get("memory");
			memory = threadMemory.getOrDefault(simpleId, 0L);
		}
		
		return new ThreadStackImpl(header, name, id, state, suspended,
				context.filePos, context.fileName, context.timestamp, codeLines, 
				context.lockedOn, context.lockedOnClassName, context.ownedLocks, 
				context.biasedLocks, daemon, priority,
				cpu, memory
				);
	}
	
	private String parseSimpleID(String header) {
		int posStart = header.indexOf(SIMPLE_ID_TAG) + 1;
		
		if (posStart <= 0) {
			logger.warn("Could not parse simple thread id (#<x>) on header : " + header);
			return null;
		}
		int posEnd = header.indexOf(' ', posStart);
		return header.substring(posStart, posEnd);
	}

	private long parseCPU(String header) {
		int posStart = header.indexOf(CPU_TAG) + CPU_TAG.length();
		int posEnd = header.indexOf("ms", posStart);
		
		// Openjdk 8 does not contain cpu information on thread header
		if (posStart == CPU_TAG.length()-1)
			return -1;

		Float value = Floats.tryParse(header.substring(posStart, posEnd)); // convert to ns
		if (value == null){
			logger.error(
					"Failed to parse cpu : \"{}\" on header \"{}\". Defaulting to prio -1",
					header.substring(posStart, posEnd), header);
			return -1;
		}

		return FormulaHelper.convertToNanoseconds(value);
	}

	private int parseThreadMemoryFigures(Map<String, Object> dumpContext, BufferedReader reader, int lineCount) throws IOException {
		Map<String,Long> memories = new HashMap<>();
		
		String line = reader.readLine();
		lineCount++;
		
		while(line!=null && line.startsWith(JZT_MEMORY_PREFIX)){
			String couple = line.substring(JZT_MEMORY_PREFIX.length());
			int pos = couple.indexOf('\t');
			if (pos>0) {
				String threadId = couple.substring(0, pos);
				String value = couple.substring(pos+1);
				Long memory = Longs.tryParse(value);
				if (memory != null)
					memories.put(threadId, memory);
			}
			lineCount++;
			line = reader.readLine();
		}
		dumpContext.put("memory", memories);

		return lineCount;
	}

	@Override
	protected boolean isRecordableStack(List<String> threadLines) {
		// Eliminate this JFR exotic case :
		//
		// "G1 Young RemSet Sampling" os_prio=2 cpu=0.00ms elapsed=352.12s tid=0x000001ec58613800 nid=0x56ac runnable  
		// "VM Periodic Task Thread" os_prio=2 cpu=46.88ms elapsed=351.20s tid=0x000001ec74860800 nid=0x1628 waiting on condition
		//
		return threadLines.size() > 1 && !threadLines.get(1).startsWith(STACK_HEADER_START_TAG);
	}
}
