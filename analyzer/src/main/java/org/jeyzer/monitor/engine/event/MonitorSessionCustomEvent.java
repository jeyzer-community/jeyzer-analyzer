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




import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;

public interface MonitorSessionCustomEvent {
	
	public void updateContext(ThreadDump dump, ConfigMonitorThreshold thresholdCfg);	

}
