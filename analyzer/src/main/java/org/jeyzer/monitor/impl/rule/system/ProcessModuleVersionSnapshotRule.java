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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jeyzer.analyzer.data.module.ProcessModule;
import org.jeyzer.analyzer.data.module.ProcessModules;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.SignalSystemProvider;
import org.jeyzer.monitor.impl.event.system.ProcessModuleVersionSnapshotEvent;

public class ProcessModuleVersionSnapshotRule extends MonitorSystemRule implements SignalSystemProvider{

	public static final String RULE_NAME = "Process module version snapshot";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule checks if the Java modules contain non official versions (snapshot, alpha, beta, build..)."
			+ "This rule should be typically used in production environments and therefore depend on a related sticker.";
	
	private List<ProcessModule> modules = new ArrayList<>();
	
	public ProcessModuleVersionSnapshotRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, "Java modules contain versions with alphabetic tags.");
	}
	
	@Override
	public boolean matchSignal(JzrSession session) {
		ProcessModules processModules = session.getProcessModules();
		if (processModules == null)
			return false;
		
		for (ProcessModule module : processModules.getProcessModules()) {
			if (module.isSnapshot())
				modules.add(module);
		}
		return !modules.isEmpty();
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}

	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SYSTEM_SIGNAL);
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new ProcessModuleVersionSnapshotEvent(this.modules, info);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_HIGH;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
}
