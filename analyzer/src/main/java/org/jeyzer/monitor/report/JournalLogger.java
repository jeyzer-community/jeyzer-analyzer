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


public class JournalLogger extends MonitorLogger {

	public JournalLogger(MonitorLoggerDefinition def, String node) {
		super(def, node);
	}

	@Override
	public void printHeader(JzrMonitorSession session, BufferedWriter out, List<MonitorEvent> events)  throws IOException{
		out.write("********************************************************************************************\n");
		out.write("JZR report generation : " + MonitorHelper.formatDate(new Date()) + "\n");
		out.write("Analysis start        : " + MonitorHelper.formatDate(session.getStartDate()) + "\n");
		out.write("Node                  : " + this.node + "\n");
		out.write("Number of events      : " + events.size() + "\n");
	}

	@Override
	public void printEvents(JzrMonitorSession session, BufferedWriter out, List<MonitorEvent> events) throws IOException {
		for (MonitorEvent event : events){
			out.write(event.dump());
		}
	}

	@Override
	public void printFooter(JzrMonitorSession session, BufferedWriter out, List<MonitorEvent> events)  throws IOException{
		out.write("Analysis ends   : " + MonitorHelper.formatDate(session.getEndDate()) + "\n");
		out.write("********************************************************************************************\n");
	}
	
	@Override
	public boolean isAppend() {
		return true;
	}

}
