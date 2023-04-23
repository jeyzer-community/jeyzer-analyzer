package org.jeyzer.monitor.impl.rule.session;

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
import java.util.Date;
import java.util.List;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.ValueSessionProvider;
import org.jeyzer.monitor.impl.event.session.HiatusTimeEvent;

public class HiatusTimeRule extends MonitorSessionRule implements ValueSessionProvider{

	public static final String RULE_NAME = "Hiatus time";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Time difference between 2 recording snapshots is greater or equal to (value) in seconds.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect long recording interruptions. "
			+ "Either the monitored application was stopped or the external recorder was simply not running. "
			+ "Prefer using the Jeyzer recorder agent to not have to care about recorder presence/start/stop.";
	
	public HiatusTimeRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
		validateCountAndHiatusTime(def);
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td){
		long endTime = info.getStartDate().getTime() + td.getNextTimeSlice()/1000000L; // Not clean
		info.updateEnd(new Date(endTime));
		return new HiatusTimeEvent(info, td);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SESSION_VALUE, THRESHOLD_GLOBAL_VALUE);
	}

	@Override
	public boolean matchValue(ThreadDump dump, long value) {
		return FormulaHelper.convertToSeconds(dump.getNextTimeSlice()) >= value;
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_VERY_LOW;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
	
	private void validateCountAndHiatusTime(ConfigMonitorRule def) throws JzrInitializationException {
		for (ConfigMonitorThreshold threshold : def.getConfigMonitorThresholds()){
			if (threshold.getCount() != 1)
				throw new JzrInitializationException("Invalid rule " + this.getName() + " / " + this.getRef() + " : count is not set to 1.");
			if (threshold.getValue() < 60)
				throw new JzrInitializationException("Invalid rule " + this.getName() + " / " + this.getRef() + " : value must be greater or equal to 60 sec.");
		}
	}
}
