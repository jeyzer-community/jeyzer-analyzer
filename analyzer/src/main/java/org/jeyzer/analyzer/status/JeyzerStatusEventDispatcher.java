package org.jeyzer.analyzer.status;

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







import java.util.ArrayList;
import java.util.List;

import org.jeyzer.analyzer.status.JeyzerStatusEvent.STATE;

public class JeyzerStatusEventDispatcher {

	private float progression = 0.0f;
	private long lastEventTimestamp = 0;
	private List<JeyzerStatusListener> listeners = new ArrayList<>();
	
	public void registerListener(JeyzerStatusListener listener){
		if (listener != null)
			listeners.add(listener);
	}
	
	public void fireStatusEvent(JeyzerStatusEvent.STATE status){
    	progression = JeyzerStatusEvent.getProgress(status, progression);
		if (!isDispatchRequired())
			return;
    	
    	String display = JeyzerStatusEvent.getDisplayText(status);
		for (JeyzerStatusListener listener : listeners){
			listener.updateStatus(display, progression);
		}
	}
	
	public void fireReportStatusEvent(String reportName){
		progression = JeyzerStatusEvent.getProgress(STATE.REPORT_GENERATION, progression);
		if (!isDispatchRequired())
			return;
		
		String display = "Generating report : " + reportName + "..."; 
		for (JeyzerStatusListener listener : listeners){
			listener.updateStatus(display, progression);
		}
	}
	
	public boolean isDispatchRequired(){
		long current = System.currentTimeMillis();
		if ((current - lastEventTimestamp)>2000){
			// dispatch event if last event was sent 2 sec ago or more
			// note that UI event spawns one thread for each event, so let's optimize
			lastEventTimestamp = current;
			return true;
		}
		return false;
	}
	
}
