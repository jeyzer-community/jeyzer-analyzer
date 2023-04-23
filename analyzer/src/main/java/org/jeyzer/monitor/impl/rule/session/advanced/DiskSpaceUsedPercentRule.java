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

import org.jeyzer.analyzer.data.DiskSpaceInfo;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigParamMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.condition.session.ValueSessionProvider;
import org.jeyzer.monitor.impl.event.session.advanced.DiskSpaceUsedPercentEvent;

public class DiskSpaceUsedPercentRule extends MonitorSessionRule implements ValueSessionProvider{

	public static final String RULE_NAME = "Disk space used percent";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect high usage of disk space under a particular partition or directory, based on the percentage of used disk space there.";
	
	private String diskSpaceName;
	private String displayName;
	
	public DiskSpaceUsedPercentRule(ConfigMonitorRule def)
			throws JzrInitializationException {
		super(def, 
				RULE_NAME + " : " + ((ConfigParamMonitorRule)def).getDisplayName(), 
				((ConfigParamMonitorRule)def).getDisplayName() + " " + RULE_NAME.toLowerCase() + " is greater or equal to (value).");
		ConfigParamMonitorRule mxDef = (ConfigParamMonitorRule) def;
		this.diskSpaceName = mxDef.getParamName();
		this.displayName = mxDef.getDisplayName();
	}

	@Override
	public MonitorSessionEvent createSessionEvent(MonitorEventInfo info, ThreadDump td){
		return new DiskSpaceUsedPercentEvent( 
				this.diskSpaceName,
				this.displayName,
				info, 
				td);
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
	public boolean matchValue(ThreadDump dump, long value) {
		DiskSpaceInfo info = dump.getDiskSpaces().getDiskSpace(diskSpaceName);
		if (info == null)
			return false;
		return info.getUsedSpacePercent() >= value;
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
