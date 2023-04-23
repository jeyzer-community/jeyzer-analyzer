package org.jeyzer.monitor.engine.rule.threshold;

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







import java.util.Map.Entry;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionCustomEvent;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;

public abstract class MonitorSessionCustomThreshold extends MonitorSessionThreshold{

	public MonitorSessionCustomThreshold(ConfigMonitorThreshold thCfg, Scope scope, SubLevel defaultSubLevel) {
		super(thCfg, scope, MatchType.CUSTOM, defaultSubLevel);
	}

	protected void updateContext(MonitorSessionEvent event, ThreadDump dump) {
		if (event instanceof MonitorSessionCustomEvent){
			MonitorSessionCustomEvent customEvent = (MonitorSessionCustomEvent) event;
			customEvent.updateContext(dump, this.cfg);
		}
		else{
			event.updateContext(dump);
		}
	}
	
	
	@Override
	public String getDisplayCondition() {
		StringBuilder params = new StringBuilder();
		for(Entry<String, String> entry : getCustomParameters().entrySet()){
			if (params.length() != 0)
				params.append("\n");
			params.append(entry.getKey());
			params.append(" = ");
			params.append(entry.getValue());
		}
		
		if (this.cfg.getPattern() != null){
			if (params.length() != 0)
				params.append("\n");
			params.append("Pattern = ");
			params.append(this.cfg.getPattern().pattern());
		}
		
		if (this.cfg.getValue() != -1){
			if (params.length() != 0)
				params.append("\n");
			params.append("Value = ");
			params.append(this.cfg.getValue());
		}
		
		return params.toString();
	}

}
