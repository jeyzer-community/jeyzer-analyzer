package org.jeyzer.monitor.report;

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







import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.monitor.JeyzerMonitor;
import org.jeyzer.monitor.engine.event.MonitorEvent;


public abstract class MonitorLogger {

	protected MonitorLoggerDefinition def;
	protected String node;
	
	public MonitorLogger(MonitorLoggerDefinition def, String node){
		this.def = def;
		this.node = node;
	}	

	public void logEvents(JzrMonitorSession session, List<MonitorEvent> events, Map<String, List<String>> publisherPaths){

		if (events.isEmpty()){
			JeyzerMonitor.logger.info("No event to log");
			return;
		}
		
		try (
				FileWriter fstream = new FileWriter(def.getLogFilePath(), isAppend());
				BufferedWriter out = new BufferedWriter(fstream);
			)
		{
			SystemHelper.createDirectory(def.getLogFileDirectory());
			
			printHeader(session, out, events);
			printEvents(session, out, events);
			printFooter(session, out, events);

			addToPublisherPaths(publisherPaths);
		}catch (Exception ex){
			JeyzerMonitor.logger.error("Failed to log events in file : " + def.getLogFilePath(), ex);
		}
	}
	
	private void addToPublisherPaths(Map<String, List<String>> publisherPaths) {
		for (String name : this.def.getPublisherNames()){
			// example : mailer or web
			List<String> docPaths = publisherPaths.computeIfAbsent(name, p -> new ArrayList<String>());
			docPaths.add(def.getLogFilePath());
		}
	}

	public abstract void printHeader(JzrMonitorSession session,BufferedWriter out, List<MonitorEvent> events) throws IOException;
	
	public abstract void printEvents(JzrMonitorSession session,BufferedWriter out, List<MonitorEvent> events) throws IOException;
	
	public abstract void printFooter(JzrMonitorSession session,BufferedWriter out, List<MonitorEvent> events) throws IOException;
	
	public abstract boolean isAppend();
	
	/**
	 * 
	 * Logger configuration
	 *
	 */
	public static class MonitorLoggerDefinition{
		public static final String FORMAT_SINGLE_EXCEL_LINE = "single_excel_line";
		public static final String FORMAT_SINGLE_LINE = "single_line";
		public static final String FORMAT_JOURNAL = "journal";
		public static final String FORMAT_CSV = "csv";
		public static final String FORMAT_HTML = "html";
		
		private String format;
		private String fileName;
		private String outputDir;
		private List<String> publisherNames;
		
		public MonitorLoggerDefinition(String format, String outputDir, String fileName, List<String> publisherNames){
			this.format = format;
			this.fileName = fileName;
			this.publisherNames = publisherNames;
			this.outputDir = outputDir;
		}
		
		public String getLogFilePath(){
			return this.outputDir + "/" + this.fileName;
		}
		
		public String getLogFileDirectory(){
			return this.outputDir;
		}
		
		public String getFormat(){
			return this.format;
		}
		
		public List<String> getPublisherNames(){
			return this.publisherNames;
		}
	}
}
