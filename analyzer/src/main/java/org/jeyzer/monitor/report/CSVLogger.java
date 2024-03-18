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
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.jeyzer.monitor.util.MonitorHelper;


public class CSVLogger extends MonitorLogger {

	public static final String CSV_SEP = ";";
	
	private boolean emptyFile = true;
	
	public CSVLogger(MonitorLoggerDefinition def, String node) {
		super(def, node);
		
		File test = new File(def.getLogFilePath());
		emptyFile = (!test.exists());
	}	
	
	@Override
	public void printHeader(JzrMonitorSession session, BufferedWriter out, List<MonitorEvent> events) throws IOException {
		
		if (!emptyFile){
			return;
		}
		
		// Excel column headers
		out.write("REPORT TIMESTAMP" + CSV_SEP);
		out.write("NODE" + CSV_SEP);
		out.write(MonitorEvent.PRINT_EVENT + CSV_SEP);
		out.write(MonitorEvent.PRINT_EXT_ID + CSV_SEP);
		out.write(MonitorEvent.PRINT_REF + CSV_SEP);
		out.write(MonitorEvent.PRINT_SCOPE + CSV_SEP);
		out.write(MonitorEvent.PRINT_LEVEL + CSV_SEP);
		out.write(MonitorEvent.PRINT_SUB_LEVEL + CSV_SEP);
		out.write(MonitorEvent.PRINT_ACTION + CSV_SEP);
		out.write(MonitorEvent.PRINT_THREAD + CSV_SEP);
		out.write(MonitorEvent.PRINT_START_DATE + CSV_SEP);
		out.write(MonitorEvent.PRINT_END_DATE + CSV_SEP);
		out.write(MonitorEvent.PRINT_DURATION + CSV_SEP);
		out.write(MonitorEvent.PRINT_RECOMMENDATION + CSV_SEP);
		out.write(MonitorEvent.PRINT_COUNT + CSV_SEP);
		out.write(MonitorEvent.PRINT_OTHER_INFO + CSV_SEP);
		out.write(MonitorEvent.PRINT_OTHER_INFO + CSV_SEP);
		out.write(MonitorEvent.PRINT_OTHER_INFO);
		out.write("\n");
		
		this.emptyFile = false;
	}

	@Override
	public void printEvents(JzrMonitorSession session, BufferedWriter out, List<MonitorEvent> events) throws IOException {
		for (MonitorEvent event : events){
			printEvent(out, event);
		}
	}

	@Override
	public void printFooter(JzrMonitorSession session, BufferedWriter out, List<MonitorEvent> events) throws IOException {
		// do nothing
	}

	protected void printEvent(BufferedWriter out, MonitorEvent event) throws IOException {
		StringBuilder line = new StringBuilder(2000);
	
		// timestamp
		line.append(MonitorHelper.formatDate(new Date()));
		line.append(CSV_SEP);
		
		// node
		line.append(this.node);
		line.append(CSV_SEP);
		
		List<String> params = event.getPrintableParameters();
		
		// handle "<event> <EXT ID> <REF> <SCOPE> <LEVEL> <SUB LEVEL>"
		line.append(params.get(1));
		line.append(CSV_SEP);
		line.append(params.get(3));
		line.append(CSV_SEP);
		line.append(params.get(5));
		line.append(CSV_SEP);
		line.append(params.get(6));
		line.append(CSV_SEP);
		line.append(params.get(7));
		line.append(CSV_SEP);
		line.append(params.get(8));
		line.append(CSV_SEP);
		
		String crappyPrefix = "";
		
		// <ACTION>	<THREAD> <START DATE> <END DATE> <DURATION> <RECOMMENDATION> <COUNT> <OTHER INFO>
		int count = 2;
		// print 1 out of 2 fields
		for (String param : params.subList(9, params.size())){
			if (count % 2 != 0){
				line.append(crappyPrefix + param.replace('\n', ' ')); // remove carriage returns for Excel.
				line.append(CSV_SEP);
			}
			if (count==21 || count==23  || count==25) // super crappy hack 2..
				// crappy hack : keep Field name for Other Info column 
				crappyPrefix = param + " : " ;
			else
				crappyPrefix = "";
				
			count++;
		}
		
		line.append("\n");
		
		out.write(line.toString());
	}

	@Override
	public boolean isAppend() {
		return true;
	}
	
}
