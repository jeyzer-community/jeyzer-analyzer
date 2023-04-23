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
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.ValueSessionProvider;
import org.jeyzer.monitor.impl.event.session.advanced.DiskWriteSpeedEvent;

public class DiskWriteSpeedRule extends MonitorSessionRule implements ValueSessionProvider {

	public static final String RULE_NAME = "Disk write speed";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Recording write speed in Kb/sec is lower or equal to (value).";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect low disk writing speed."
			+ "Measured speed applies on the recording writing, and therefore on the related storage system and disk controler.";

	public DiskWriteSpeedRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
	}

	@Override
	public boolean matchValue(ThreadDump dump, long value) {
		long size = dump.getWriteSize();
		long time = dump.getWriteTime();
		
		if (size == -1 || time == -1 || time == 0)
			return false;
		
		// do calculate in Kb / sec
		long speed = FormulaHelper.convertToKb((size / time) * 1000L);
		
		return speed <= value;
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new DiskWriteSpeedEvent(info, td);
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
		return SubLevel.DEFAULT_MEDIUM;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
}
