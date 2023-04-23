package org.jeyzer.monitor.impl.event.system;

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







import java.util.List;

import org.jeyzer.analyzer.data.DiskSpaceInfo;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class DiskSpaceTotalEvent extends MonitorSystemEvent {

	public static final String EVENT_SUFFIX = " disk space total";
	
	private String diskSpaceName;
	private String displayName;
	
	private long totalDiskSpace = Long.MAX_VALUE;
	
	public DiskSpaceTotalEvent(String diskSpaceName, String displayName, MonitorEventInfo info) {
		super(displayName + EVENT_SUFFIX, info);
		this.diskSpaceName = diskSpaceName;
		this.displayName = displayName;
	}
	
	@Override
	public void updateContext(JzrSession session) {
		for (ThreadDump td : session.getDumps()){
			DiskSpaceInfo info = td.getDiskSpaces().getDiskSpace(diskSpaceName);
			if (info == null || !info.hasTotalSpace())
				continue;
			long total = FormulaHelper.convertToGb(info.getTotalSpace());
			if (total < totalDiskSpace){
				totalDiskSpace = total;
				return;
			}
		}
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Total disk space on " + this.displayName + ": " + this.totalDiskSpace + " Gb\n");
	}
	
	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Total disk space on " + this.displayName);
		params.add(Math.round(this.totalDiskSpace) + " Gb");
	}

}
