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
import java.util.Collection;
import java.util.List;

import org.jeyzer.analyzer.data.jar.ProcessJarVersion;
import org.jeyzer.analyzer.data.jar.ProcessJars;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.SignalSystemProvider;
import org.jeyzer.monitor.impl.event.system.ProcessJarMultipleVersionsEvent;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multisets;

public class ProcessJarMultipleVersionsRule extends MonitorSystemRule implements SignalSystemProvider{

	public static final String RULE_NAME = "Process jar multiple versions";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " checks the presence of process jars with multiple versions.\n "
			+ "If the jar files can be accessed from the monitored application (depending on the class loading design), it can be dangerous as the wrong jar file may be loaded.\n "
			+ "Under an application server, this could be a false positive as the loaded jar files can be isolated at web app level.";
	
	private Multimap<String, String> duplicates = ArrayListMultimap.create();
	
	public ProcessJarMultipleVersionsRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, "Process jars are present under multiple versions.");
	}
	
	@Override
	public boolean matchSignal(JzrSession session) {
		Multimap<String, String> candidates = ArrayListMultimap.create();
		ProcessJars processJars = session.getProcessJars();
		if (processJars == null)
			return false;
		
		for (ProcessJarVersion jarVersion : processJars.getJarVersions()) {
			candidates.put(jarVersion.getJarName(), jarVersion.getJarVersion() != null ? jarVersion.getJarVersion() : "none");
		}
		for (String jarName : Multisets.copyHighestCountFirst(candidates.keys()).elementSet()){
			Collection<String> jarVersions = candidates.get(jarName);
			if (jarVersions.size() > 1) {
				for (String jarVersion : jarVersions)
					duplicates.put(jarName, jarVersion);
			}
		}
		
		return !duplicates.isEmpty();
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
		return new ProcessJarMultipleVersionsEvent(this.duplicates, info);
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
