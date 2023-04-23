package org.jeyzer.monitor.engine.event;

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

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public abstract class MonitorSessionEvent extends MonitorEvent {

	public MonitorSessionEvent(String eventName, MonitorEventInfo info){
		super(eventName, info);
	}
	
	public abstract void updateContext(ThreadDump dump);
	
	@Override
	public String getNameExtraInfo(){
		return null;
	}
	
	@Override
	public List<String> getPrintableParameters(){

		List<String> params = getPrintableParameters(
				PRINT_NA, 
				PRINT_NA,
				getPrintableEndDate(),
				getPrintableDuration()
				);

		addPrintableExtraParameters(params);
		
		return params;
	}
	
}
