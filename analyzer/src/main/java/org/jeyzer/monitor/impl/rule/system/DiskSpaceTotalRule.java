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

import org.jeyzer.analyzer.data.DiskSpaceInfo;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigParamMonitorRule;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.condition.system.ValueSystemProvider;
import org.jeyzer.monitor.impl.event.system.DiskSpaceTotalEvent;

public class DiskSpaceTotalRule extends MonitorSystemRule implements ValueSystemProvider{

	public static final String RULE_NAME = "Disk space total";
	
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect insufficient disk space sizing. "
			+ "Disk space total applies to the partition hosting the target directory, for example a logging directory. "
			+ "It means that the storage system size should be increased.";
	
	private String diskSpaceName;
	private String diskSpaceDisplay;
	
	public DiskSpaceTotalRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, 
				RULE_NAME + " : " + ((ConfigParamMonitorRule)def).getDisplayName(), 
				((ConfigParamMonitorRule)def).getDisplayName() + " " + RULE_NAME.toLowerCase() + " is lower or equal to (value) Gb.");
		ConfigParamMonitorRule mxDef = (ConfigParamMonitorRule) def;
		this.diskSpaceName = mxDef.getParamName();
		this.diskSpaceDisplay = mxDef.getDisplayName();
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return true;
	}
	
	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_SYSTEM_VALUE);
	}

	@Override
	public boolean matchValue(JzrSession session, long value) {
		for (ThreadDump td : session.getDumps()){
			DiskSpaceInfo info = td.getDiskSpaces().getDiskSpace(diskSpaceName);
			if (info == null)
				continue;
			long total = FormulaHelper.convertToGb(info.getTotalSpace());
			if (total > DiskSpaceInfo.NOT_AVAILABLE && total < value)
				return true;
		}
		return false;
	}

	@Override
	public MonitorSystemEvent createSystemEvent(MonitorEventInfo info) {
		return new DiskSpaceTotalEvent(
				this.diskSpaceName,
				this.diskSpaceDisplay,
				info
			);
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
