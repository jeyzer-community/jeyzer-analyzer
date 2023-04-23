package org.jeyzer.monitor.impl.rule.system;

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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SYSTEM_VALUE;




import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.data.ProcessCommandLine;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.ValueSystemProvider;
import org.jeyzer.monitor.impl.event.system.ProcessCommandLineMaxHeapMemoryEvent;
import org.jeyzer.monitor.util.CommanLineHelper;

public class ProcessCommandLineMaxHeapMemoryRule extends MonitorSystemRule implements ValueSystemProvider{

	public static final String RULE_NAME = "Process command line max heap";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Maximum Java heap size (-Xmx) is lower or equal to (value).";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to check that the Java Xmx is correctly sized. "
			+ "Rule will automatically match if the Xmx parameter is not set."
			+ "It is always recommended to set the Xmx of a Java process for having proper memory usage and be respectful with the hosting system.";
	
	public ProcessCommandLineMaxHeapMemoryRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}

	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SYSTEM_VALUE);
	}

	@Override
	public boolean matchValue(JzrSession session, long value) {
		ProcessCommandLine commandLine = session.getProcessCommandLine();
		if  (commandLine == null)
			return false; // should not happen
		
		String param = commandLine.getParameter(CommanLineHelper.MAX_HEAP_PARAM);
		if (param == null)
			return false;
		
		long maxHeapSize = CommanLineHelper.parseHeapSize(param);
		
		return maxHeapSize < value;
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new ProcessCommandLineMaxHeapMemoryEvent(info);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_LOW;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
}
