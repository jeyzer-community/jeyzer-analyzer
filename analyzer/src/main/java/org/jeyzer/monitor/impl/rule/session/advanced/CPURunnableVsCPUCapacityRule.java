package org.jeyzer.monitor.impl.rule.session.advanced;

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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_GLOBAL_SIGNAL;
import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SESSION_SIGNAL;




import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.config.ConfigThreadLocal;
import org.jeyzer.analyzer.data.ProcessCard;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.SignalSessionProvider;
import org.jeyzer.monitor.impl.event.session.advanced.CPURunnableVsCPUCapacityEvent;

import com.google.common.primitives.Ints;

public class CPURunnableVsCPUCapacityRule extends MonitorSessionRule implements SignalSessionProvider{

	public static final String RULE_NAME = "CPU Runnable vs CPU capacity";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Number of CPU Runnable threads is greater or equal to the number of CPUs of the system. ";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect high applicative CPU usage. "
			+ "It means that system is not CPU sized correctly to handle the applicative load. "
			+ "Alternative is to optimize the application. "
			+ "The number of system CPUs is obtained from the jzr.ext.process.available.processors process card property. "
			+ "CPU runnable threads represent the active threads having non-blocking contention types (or pre-defined running contention types. See Jeyzer setup).";
	
	public CPURunnableVsCPUCapacityRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}

	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_GLOBAL_SIGNAL, THRESHOLD_SESSION_SIGNAL);
	}

	@Override
	public boolean matchSignal(ThreadDump dump) {
		int systemCPUs = getAvailableProcessors();
		
		if (systemCPUs == -1)
			return false;
		
		// On high number of CPUs, raise event if 70% of CPUs used
		return systemCPUs>=4 ? dump.getCPURunnableThreadsCount() >= systemCPUs*0.7 : dump.getCPURunnableThreadsCount() >= systemCPUs;
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new CPURunnableVsCPUCapacityEvent(info);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_MEDIUM;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
	
	private int getAvailableProcessors() {
		String availableProcessors = ConfigThreadLocal.get(ProcessCard.AVAILABLE_PROCESSORS);
		if (availableProcessors != null && !availableProcessors.isEmpty()) {
			Integer parsedValue = Ints.tryParse(availableProcessors);
			if (parsedValue != null)
				return parsedValue;
		}
		
		// Try JFR data
		availableProcessors = ConfigThreadLocal.get(ProcessCard.JFR_AVAILABLE_PROCESSORS);
		if (availableProcessors != null && !availableProcessors.isEmpty()) {
			Integer parsedValue = Ints.tryParse(availableProcessors);
			if (parsedValue != null)
				return parsedValue;	
		}
		
		return -1;
	}
}
