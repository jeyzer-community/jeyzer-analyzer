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



import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_GLOBAL_VALUE;
import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_SESSION_VALUE;




import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigParamMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.ValueSessionProvider;
import org.jeyzer.monitor.impl.event.session.advanced.JeyzerMXContextParamNumberEvent;
import org.jeyzer.monitor.util.Operator;

import com.google.common.primitives.Longs;

public class JeyzerMXContextParamNumberRule extends MonitorSessionRule implements ValueSessionProvider {

	public static final String RULE_NAME = "Jeyzer MX context parameter number";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect limit exceeds upon an applicative numeric figure. "
			+ "The data is originally published by the application through the Jeyzer publisher API. "
			+ "It is usually used to emit applicative threshold alerts.";
	
	private String paramName;
	private String paramDisplay;
	private Operator operator;
	
	public JeyzerMXContextParamNumberRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + ": " + ((ConfigParamMonitorRule)def).getDisplayName(), 
				Operator.getEventDescription((ConfigParamMonitorRule) def));
		ConfigParamMonitorRule mxDef = (ConfigParamMonitorRule) def;
		this.paramName = mxDef.getParamName();
		this.paramDisplay = mxDef.getDisplayName();
		this.operator = mxDef.getOperator();
	}

	@Override
	public boolean matchValue(ThreadDump dump, long value) {
		String longValue = dump.getJeyzerMXContextParams().get(paramName);
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
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new JeyzerMXContextParamNumberEvent(
				this.paramDisplay, 
				info, 
				this.paramName,
				this.operator
			);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SESSION_VALUE, THRESHOLD_GLOBAL_VALUE);
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
