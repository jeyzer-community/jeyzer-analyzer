package org.jeyzer.analyzer.output;

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







import java.text.SimpleDateFormat;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.jeyzer.analyzer.session.JzrSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReportHelper {

	private static final Logger logger = LoggerFactory.getLogger(ReportHelper.class);
	
	public static final String REPORT_DIR = "/report";
	
	private static final String START_DATE_FORMAT = "yyyy-MM-dd---HH-mm-ss";
	private static final String END_DATE_FORMAT   = "HH-mm-ss";
	private static final String JEYZER_SUFFIX     = "-jzr";

	private ReportHelper(){
	}
	
	public static String getFilePath(JzrSession session, String outputDir, String fileNamePrefix, String extension){
		return outputDir + "/" + ReportHelper.getFileName(session, fileNamePrefix, extension);
	}

	public static String getFileName(JzrSession session, String fileNamePrefix, String extension){
		// start date
		SimpleDateFormat sdf = new SimpleDateFormat(START_DATE_FORMAT);
		String startDate = sdf.format(session.getStartDate());
		
		// end date time
		sdf = new SimpleDateFormat(END_DATE_FORMAT);
		String endTime = sdf.format(session.getEndDate());
		
		return fileNamePrefix + "--" + startDate+ "---" + endTime + JEYZER_SUFFIX + extension;
	}
	
	public static String getPrintableDuration(long time){
		String duration = "";
		Duration dt;
		try {
			dt = DatatypeFactory.newInstance().newDuration(time);
			if (dt.getMinutes() == 0) 
				duration = dt.getSeconds() + " s";
			if (dt.getMinutes() > 0)
				duration = dt.getMinutes() + " mn " + duration;
			if (dt.getHours() > 0)
				duration = dt.getHours() + " h "+ duration; // add the hours if available
			if (dt.getDays() > 0)
				duration = dt.getDays() + " d "+ duration; // add the days if available
		} catch (DatatypeConfigurationException e) {
			logger.error("Failed to print duration", e);
		}
		return duration;
	}
}
