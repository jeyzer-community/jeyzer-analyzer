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
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.ProcessCommandLine;
import org.jeyzer.analyzer.data.ProcessCommandLine.CommandLineProperty;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigParamMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.ValueSystemProvider;
import org.jeyzer.monitor.impl.event.system.ProcessCommandLinePropertyNumberEvent;
import org.jeyzer.monitor.util.Operator;

import com.google.common.primitives.Longs;

public class ProcessCommandLinePropertyNumberRule extends MonitorSystemRule implements ValueSystemProvider{

	public static final String RULE_NAME = "Process command line property number";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to check that a process command line parameter numeric value is not breaking any predefined limit. "
			+ "Rule covers the applicative parameters and the system properties (specified with -D prefix). "
			+ "Process command line parameter must contain the equal sign.";
	
	private Pattern paramNamePattern;
	private String paramDisplay;
	private Operator operator;
	
	public ProcessCommandLinePropertyNumberRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + ((ConfigParamMonitorRule)def).getDisplayName(), 
				Operator.getEventDescription((ConfigParamMonitorRule) def));
		ConfigParamMonitorRule mxDef = (ConfigParamMonitorRule) def;
		this.paramNamePattern = Pattern.compile(mxDef.getParamName());
		this.paramDisplay = mxDef.getDisplayName();
		this.operator = mxDef.getOperator();
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
		
		CommandLineProperty property = commandLine.getValue(paramNamePattern);
		if (property == null)
			return false;
		
		String longValue = property.getValue();
		if (longValue == null || longValue.isEmpty())
			return false;
		
		Long paramLongValue = Longs.tryParse(longValue);
		if (paramLongValue == null)
			return false;
		
		switch(operator){
		case LOWER_OR_EQUAL:
			return paramLongValue <= value;
		default:
			return paramLongValue >= value;
		}
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new ProcessCommandLinePropertyNumberEvent(
				this.paramDisplay,
				info,
				this.paramNamePattern,
				this.operator
			);
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
