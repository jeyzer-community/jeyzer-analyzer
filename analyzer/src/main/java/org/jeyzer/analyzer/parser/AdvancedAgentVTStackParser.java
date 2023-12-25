package org.jeyzer.analyzer.parser;


import java.io.BufferedReader;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.error.JzrLineParsingException;
import org.jeyzer.analyzer.error.JzrParsingException;
import org.jeyzer.analyzer.parser.advanced.DumpBeanInfoParser;
import org.jeyzer.analyzer.parser.virtual.VirtualDumpParser;
import org.jeyzer.analyzer.parser.virtual.VirtualJsonParser;
import org.jeyzer.analyzer.parser.virtual.VirtualTxtParser;
import org.jeyzer.analyzer.setup.JzrSetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdvancedAgentVTStackParser extends ThreadDumpParser {

	private static final String JZR_FILE_JZR_EXTENSION = ".jzr";
	private static final String JZR_FILE_JZR_VT_EXTENSION = ".jzr.vt";
	
	// format name
	public static final String FORMAT_NAME = "Jeyzer Recorder - Advanced VT Agent";
	public static final String FORMAT_SHORT_NAME = "Advanced VT Agent";

	public static final String FIRST_LINE = "Full Agent Advanced VT Java thread dump with locks info";

	private static final String JZ_PREFIX = "\tJz>\t";	
		
	private static final Logger logger = LoggerFactory.getLogger(AdvancedAgentVTStackParser.class);	
	
	private DumpBeanInfoParser dumpBeanInfoParser;     // important : must be state less
	
	private VirtualDumpParser virtualParser = null;
	private Map<Date, ThreadDump> dumpCache = new HashMap<>();
	
	public AdvancedAgentVTStackParser(JzrSetupManager setupMgr) {
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
		ThreadDump dump;
		synchronized(dumpCache) {
			// this method will be called twice for each dump : create only one and cache it for the other
			dump = dumpCache.computeIfAbsent(date, key -> new ThreadDump(file, key));
		}
		
		if (file.getName().endsWith(JZR_FILE_JZR_EXTENSION))
			this.parseBeanFile(file, dump);
		if (file.getName().endsWith(JZR_FILE_JZR_VT_EXTENSION))
			this.parseVTFile(file, date, dump);
	}

	@Override
	public ThreadStack parseThreadStack(StackContext context) throws ParseException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected String parseName(String header){
		throw new UnsupportedOperationException();		
	}	
	
	@Override
	public boolean isLockIdUsed() {
		return false;
	}

	@Override
	public boolean isCPUMeasurementUsed() {
		return false;
	}

	@Override
	public boolean isMemoryMeasurementUsed() {
		return false;
	}

	@Override
	public boolean isProcessUpTimeMeasurementUsed() {
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
	public boolean isDeadlockUsed() {
		return false;
	}

	@Override
	public boolean isLockCycleDetectionUsed() {
		return false;
	}

	@Override
	public boolean isBiasedLockUsed() {
		return false;
	}

	@Override
	public boolean isSuspendedUsed() {
		return false;
	}

	@Override
	public boolean isJeyzerMXUsed() {
		return false;
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
		return true;
	}
	
	@Override
	public boolean isDiskWriteTimeMeasurementUsed(){
		return true;
	}

	public void parseBeanFile(File file, ThreadDump dump) {
		logger.info("Reading snapshot file : {}", file.getName());
		String line = null;
		int lineCount = 0;

		try (
				FileReader fr = new FileReader(file);
				BufferedReader reader = new BufferedReader(fr);
			)
		{
			// First line --> ignore - "Full Advanced Java thread dump with locks info"
			line = reader.readLine();
			lineCount++;

			line = reader.readLine();
			while(line!=null && line.startsWith(JZ_PREFIX)){
				dumpBeanInfoParser.parse(dump, line.substring(JZ_PREFIX.length()));
				lineCount++;
				line = reader.readLine();
			}

		} catch (FileNotFoundException ex) {
			logger.error("Failed to open {}", file.getAbsolutePath(), ex);
			this.parsingErrors.put(file.getName(), ex);
			return;
		} catch (Exception e) {
			logger.error("Failed to parse thread dump {}.", file.getName(), e);
			JzrLineParsingException ex = new JzrLineParsingException(e, file.getName(), line, lineCount);
			this.parsingErrors.put(file.getName(), ex);
			return;
		}
		
		registerDump(dump);
	}	

	private void parseVTFile(File file, Date date, ThreadDump dump) {
		this.virtualParser = getVirtualParser(file);
		if (this.virtualParser == null)
			return;
		
		try {
			this.virtualParser.parseVirtualThreadDump(file, date, dump);
		} catch (JzrParsingException | FileNotFoundException ex) {
			this.parsingErrors.put(file.getName(), ex);
			return;
		}
		
		registerDump(dump);
	}

	private synchronized VirtualDumpParser getVirtualParser(File file) {
		if (this.virtualParser != null)
			return this.virtualParser;
		
		List<String> lines = new ArrayList<>(2);
		
		try (
				FileReader fr = new FileReader(file);
				BufferedReader reader = new BufferedReader(fr);
			)
		{
			// Read first 2 lines
			for (int i = 0; i<2; i++){
				String line = reader.readLine();
				if (line == null)
					break;
				lines.add(line);
			}
		
			// let's try JCMD format : 1st line starts with process id
			if (!lines.isEmpty() && lines.get(0).matches("\\d+(\\.\\d+)?")) {
				logger.info("VT format detected : {}", JcmdTxtParser.FORMAT_SHORT_NAME);
				this.virtualParser = new VirtualTxtParser();
				return this.virtualParser;
			}
		
			// let's try JCMD Json format : 2nd line starts with threadDump
			if (lines.size() >=2 && lines.get(1).contains(JcmdJsonParser.SECOND_LINE)) {
				logger.info("VT format detected : {}", JcmdJsonParser.FORMAT_SHORT_NAME);
				this.virtualParser = new VirtualJsonParser();
				return this.virtualParser;
			}
			
			logger.warn("Unknown VT format detected : {} file will be ignored.", file.getName());
			this.parsingErrors.put(file.getName(), new JzrParsingException("Unknown VT format detected on file : " + file.getName()));
			return null;
		} catch (IOException ex) {
			logger.error("Failed to open {}", file.getAbsolutePath(), ex);
			this.parsingErrors.put(file.getName(), ex);
			return null;
		}
	}

	private void registerDump(ThreadDump dump) {
		synchronized(dumps) {
			if (!dumps.contains(dump))
				dumps.add(dump);			
		}
	}
}
