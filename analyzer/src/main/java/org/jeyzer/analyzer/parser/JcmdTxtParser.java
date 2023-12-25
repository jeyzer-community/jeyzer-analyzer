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


import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.Date;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.error.JzrParsingException;
import org.jeyzer.analyzer.parser.virtual.VirtualTxtParser;
import org.jeyzer.analyzer.setup.JzrSetupManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JcmdTxtParser extends ThreadDumpParser {

	// format name
	public static final String FORMAT_NAME = "Jcmd txt";
	public static final String FORMAT_SHORT_NAME = "Jcmd txt";

	// 2023-07-28T11:33:35.254146100Z
	public static final String DATE_FORMAT = "yyyy-MM-ddHH:mm:ss";	
	
	private static final Logger logger = LoggerFactory.getLogger(JcmdTxtParser.class);
	
	private VirtualTxtParser txtParser = new VirtualTxtParser();
	
	public JcmdTxtParser(JzrSetupManager setupMgr) {
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

		ThreadDump dump = new ThreadDump(file, date);

		try {
			txtParser.parseVirtualThreadDump(file, date, dump);
		} catch (JzrParsingException | FileNotFoundException ex) {
			this.parsingErrors.put(file.getName(), ex);
			return;
		}

		dumps.add(dump);		
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
	
	@Override
	public boolean areVirtualThreadVariationCountersUsed() {
		return false;
	}
	
	@Override
	public boolean hasVirtualThreadStackSupport() {
		return true;
	}
}
