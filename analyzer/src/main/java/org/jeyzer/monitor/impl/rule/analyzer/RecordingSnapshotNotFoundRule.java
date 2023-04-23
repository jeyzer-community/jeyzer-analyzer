package org.jeyzer.monitor.impl.rule.analyzer;

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




import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrNoThreadDumpFileFound;
import org.jeyzer.analyzer.session.JzrMonitorErrorSession;
import org.jeyzer.analyzer.session.JzrMonitorSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorAnalyzerThreshold;
import org.jeyzer.monitor.engine.event.MonitorAnalyzerEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorAnalyzerRule;
import org.jeyzer.monitor.engine.rule.condition.analyzer.SignalAnalyzerProvider;
import org.jeyzer.monitor.impl.event.analyzer.RecordingSnapshotNotFoundEvent;

public class RecordingSnapshotNotFoundRule extends MonitorAnalyzerRule implements SignalAnalyzerProvider{

	public static final String RULE_NAME = "Recording snapshots not found";
	public static final String RULE_REF = "JZR-CORE-001";
	
	public static final String RULE_MESSAGE = "The monitored application (or the external recorder) is probably not started. Check its status.";
	public static final String RULE_CONDITION_DESCRIPTION = "Recording snapshot is not found.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " is an internal rule to detect the absence of recording snapshot. "
			+ "When it occurs, it means that either the recording generation is not active or the monitored application is not started.";
	
	public static final String THRESHOLD_REF = "01";
	
	public RecordingSnapshotNotFoundRule() throws JzrInitializationException {
		super(RULE_NAME, RULE_REF, RULE_CONDITION_DESCRIPTION, RULE_NARRATIVE, new ConfigMonitorAnalyzerThreshold(RULE_MESSAGE, THRESHOLD_REF));
	}

	@Override
	public boolean matchSignal(JzrMonitorSession session) {
		if (session.getDumps().isEmpty() && session instanceof JzrMonitorErrorSession){
			JzrMonitorErrorSession errorSession = (JzrMonitorErrorSession) session;
			return errorSession.getException() instanceof JzrNoThreadDumpFileFound;
		}
		else{
			return false;
		}
	}

	@Override
	public MonitorAnalyzerEvent createAnalyzerEvent(MonitorEventInfo info) {
		return new RecordingSnapshotNotFoundEvent(info);
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
