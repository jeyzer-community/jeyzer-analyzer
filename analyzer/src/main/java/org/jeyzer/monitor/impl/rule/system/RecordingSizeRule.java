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

import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigParamMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.ValueSystemProvider;
import org.jeyzer.monitor.impl.event.system.RecordingSizeEvent;
import org.jeyzer.monitor.util.Operator;

public class RecordingSizeRule extends MonitorSystemRule implements ValueSystemProvider{

	public static final String RULE_NAME = "Recording size";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to check the size of the recording (meaning the number of recording snapshots) is not breaking any predefined limit. "
			+ "The empty recording case (zero size) is always excluded.";
	
	private Operator operator;
	private String extraInfo;
	
	public RecordingSizeRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME + " : " + def.getExtraInfo(), Operator.getEventDescription((ConfigParamMonitorRule) def));
		ConfigParamMonitorRule mxDef = (ConfigParamMonitorRule) def;
		this.operator = mxDef.getOperator();
		this.extraInfo = def.getExtraInfo();
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
		int size = session.getDumps().size();
		
		switch(operator){
		case LOWER_OR_EQUAL:
			return size <= value && size > 0; // exclude empty recordings
		default:
			return size >= value;
		}
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new RecordingSizeEvent(
				this.extraInfo,
				info
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
