package org.jeyzer.monitor;

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







import java.util.List;

import org.jeyzer.analyzer.error.JzrMonitorInitializationException;
import org.jeyzer.monitor.JeyzerMonitor.MonitorResult;
import org.jeyzer.monitor.engine.event.info.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JeyzerNagiosActiveMonitor {

	public static final Logger consoleLogger = LoggerFactory.getLogger(JeyzerNagiosActiveMonitor.class);
	public static final Logger fileLogger = LoggerFactory.getLogger(JeyzerMonitor.class);
	
	// nagios codes
	private static final int INFO_CODE = 0;
	private static final int WARNING_CODE = 1;
	private static final int CRITICAL_CODE = 2;
	private static final int ERROR_CODE = 2;
	
	private JeyzerNagiosActiveMonitor(){
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JeyzerMonitor.MonitorResult result = null;
		
		JeyzerMonitor monitor = new JeyzerMonitor();
		try {
			monitor.init();
		} catch (JzrMonitorInitializationException ex) {
			fileLogger.error("Failed to initialize Jeyzer Monitor.", ex);
			consoleLogger.error("Failed to initialize Jeyzer Monitor. Error is : "
					+ ex.getMessage());
			exit(Level.ERROR);
		}
		
		if (!monitor.isOneshotMonitoring()){
			consoleLogger.error("Failed to initialize Jeyzer Monitor : oneshot monitoring is not enabled. Please set the scanning period with -1 value.");
			exit(Level.ERROR);
		}
		
		try{
			result = monitor.monitor();
			
			// The only permitted trace for Nagios
			printNagiosTrace(result);
		}
		catch(Exception ex){
			fileLogger.error("Failed to monitor the application.", ex);
			consoleLogger.error("Failed to monitor the application : " 
					+ monitor.getApplicationType() 
					+ "-" 
					+ monitor.getNodeName() 
					+ ". Error is : "
					+ ex.getMessage());
			exit(Level.ERROR);
		}
		
		exit(result.getTopCategory());
	}

	private static void printNagiosTrace(MonitorResult result) {
		if (!result.isEventDetected())
			consoleLogger.info("Jeyzer monitoring completed. No detected events.");
		
		String url = getBestWebUrl(result.getWebPaths());
		if (url != null)
			consoleLogger.info("Events detected. Check Jeyzer monitoring events here : " + url);
		else
			consoleLogger.info("Jeyzer monitoring completed. No event report generated : please configure the web generation.");
	}

	private static String getBestWebUrl(List<String> urls) {
		for (String url : urls){
			if (url.endsWith(".html"))
				return url;
		}
		
		for (String url : urls){
			if (url.endsWith(".xlsx"))
				return url;
		}
		
		for (String url : urls){
			if (url.endsWith(".csv"))
				return url;
		}
		
		for (String url : urls){
			if (url.endsWith(".log"))
				return url;
		}
		
		return null;
	}

	private static void exit(Level categoryStatus) {
		int code;
		switch(categoryStatus){
		    // Nagios codes
			case INFO:
				code = INFO_CODE;
				break;
			case WARNING:
				code = WARNING_CODE;
				break;
			case CRITICAL:
				code = CRITICAL_CODE;
				break;
			case ERROR:
			case UNKNOWN:
			default:
				code = ERROR_CODE;
		}
		System.exit(code);
	}	
	
}
