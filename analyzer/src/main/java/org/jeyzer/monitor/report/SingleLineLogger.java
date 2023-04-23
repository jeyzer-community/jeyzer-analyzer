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
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.jeyzer.monitor.util.MonitorHelper;


public class SingleLineLogger extends MonitorLogger {

	public SingleLineLogger(MonitorLoggerDefinition def, String node){
		super(def, node);
	}
	
	@Override
	public void printHeader(JzrMonitorSession session, BufferedWriter out, List<MonitorEvent> events) throws IOException{
		// do nothing
	}
	
	@Override
	public void printEvents(JzrMonitorSession session, BufferedWriter out, List<MonitorEvent> events) throws IOException{
		for (MonitorEvent event : events){
			printEvent(out, event);
		}
	}
	
	@Override
	public void printFooter(JzrMonitorSession session, BufferedWriter out, List<MonitorEvent> events) throws IOException{
		// do nothing
	}
	
	@Override
	public boolean isAppend() {
		return true;
	}
	
	protected void printEvent(BufferedWriter out, MonitorEvent event) throws IOException{
		StringBuilder line = new StringBuilder(500);

		// spec :
		// <TOOL_Log_date_time_utc> \t <node> \t EVENT : Long Lock \t JZR-STD-001 \t [scope enum value] \t ERROR|INFO|WARNING|SEVERE \t [1-10] : \t on THREAD ID #\t 2724 \t START DATE :Thu Sep 26 14:35:02 CEST 2013 SEEN FOR : 0 hours 15 minutes 11 seconds \t # OF LOCKED THREADS : 23 \t RECOMMENDATION : A Java lock has been detected. Either a dead lock or a thread waiting endlessly for a database operation result. Please collect the thread dumps for Support and restart the application.
	
		// timestamp
		line.append(MonitorHelper.formatDate(new Date()));
		line.append(this.getFieldSeparator());
		
		// node
		line.append(this.node);
		line.append(this.getFieldSeparator());
		
		List<String> params = event.getPrintableParameters();
		
		// handle "EVENT : <event> <REF> <SCOPE> <LEVEL> <SUB LEVEL>"
		line.append(params.get(0));
		line.append(this.getFieldPresenter());
		line.append(params.get(1));
		line.append(this.getFieldSeparator());
		line.append(params.get(3)); // ref
		line.append(this.getFieldSeparator());
		line.append(params.get(4)); // scope
		line.append(this.getFieldSeparator());
		line.append(params.get(5)); // level
		line.append(this.getFieldSeparator());
		line.append(params.get(6)); // sub level
		line.append(this.getFieldSeparator());
		
		int count = 2;
		for (String param : params.subList(7, params.size())){
			line.append(param.replace("\n", " ")); // remove carriage returns for Excel.
			line.append((count % 2 == 0)?
					this.getFieldPresenter():
				    this.getFieldSeparator()
				    );
			count++;
		}
		line.append("\n");
		
		out.write(line.toString());
	}
	
	protected String getFieldSeparator(){
		return "  ";
	}

	protected String getFieldPresenter(){
		return " : ";
	}	
	
}
