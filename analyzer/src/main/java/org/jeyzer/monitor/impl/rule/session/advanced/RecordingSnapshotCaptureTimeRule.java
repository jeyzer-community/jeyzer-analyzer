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
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.ValueSessionProvider;
import org.jeyzer.monitor.impl.event.session.advanced.RecordingSnapshotCaptureTimeEvent;


public class RecordingSnapshotCaptureTimeRule extends MonitorSessionRule implements ValueSessionProvider{

	public static final String RULE_NAME = "Recording snapshot capture time";
	
	public static final String RULE_CONDITION_DESCRIPTION = "Recording snapshot capture time in ms is greater or equal to (value).";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect long snapshot recording times. "
			+ "This usually reflects some high process activity and is therefore good extra process activity indicator.";
	
	public RecordingSnapshotCaptureTimeRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
	}

	@Override
	public boolean matchValue(ThreadDump dump, long value) {
		return dump.getCaptureTime() >= value;  // capture time is ms
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td) {
		return new RecordingSnapshotCaptureTimeEvent(info, td);
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
		return SubLevel.DEFAULT_LOW;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
}
