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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SYSTEM_SIGNAL;




import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.ProcessCard;
import org.jeyzer.analyzer.data.ProcessCommandLine;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigParamMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.SignalSystemProvider;
import org.jeyzer.monitor.impl.event.system.ProcessCommandLineParameterAbsenceEvent;

public class ProcessCommandLineParameterAbsenceRule extends MonitorSystemRule implements SignalSystemProvider{

	public static final String RULE_NAME = "Process command line parameter absence";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect the absence of a Java command line parameter. "
			+ "Rule covers the applicative parameters and the system properties (specified with -D prefix).";
	
	private Pattern paramNamePattern;
	private String paramDisplay;
	
	public ProcessCommandLineParameterAbsenceRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + ((ConfigParamMonitorRule)def).getDisplayName(), 
				((ConfigParamMonitorRule)def).getDisplayName() + " property is not found for the (pattern) regex property name.");
		ConfigParamMonitorRule mxDef = (ConfigParamMonitorRule) def;
		this.paramNamePattern = Pattern.compile(mxDef.getParamName());
		this.paramDisplay = mxDef.getDisplayName();
	}
	
	@Override
	public boolean matchSignal(JzrSession session) {
		ProcessCard card = session.getProcessCard();
		if  (card == null)
			return false; // not available
		
		ProcessCommandLine commandLine = session.getProcessCommandLine();
		if  (commandLine == null)
			return false; // should not happen
		
		return commandLine.getParameter(paramNamePattern) == null;
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}

	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SYSTEM_SIGNAL);
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new ProcessCommandLineParameterAbsenceEvent(this.paramDisplay, info);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_VERY_LOW;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
}
