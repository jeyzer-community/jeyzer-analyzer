package org.jeyzer.monitor.impl.event.session.advanced;

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
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class DiskSpaceFreeEvent extends MonitorSessionEvent {
	
	public static final String EVENT_SUFFIX = " disk space free";
	
	protected long minFreeDiskSpace = Long.MAX_VALUE;
	
	private String diskSpaceName;
	private String displayName;
	
	public DiskSpaceFreeEvent(String diskSpaceName, String displayName, MonitorEventInfo info, ThreadDump dump) {
		super(displayName + EVENT_SUFFIX, info);
		this.diskSpaceName = diskSpaceName;
		this.displayName = displayName;
	}
	
	@Override
	public void updateContext(ThreadDump dump) {
		DiskSpaceInfo info = dump.getDiskSpaces().getDiskSpace(diskSpaceName);
		if (info == null || !info.hasFreeSpace())
			return;
		long free = FormulaHelper.convertToGb(info.getFreeSpace());
		if (free <= minFreeDiskSpace)
			minFreeDiskSpace = free;
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Min disk space free on " + this.displayName);
		params.add(Math.round(this.minFreeDiskSpace) + " Gb");
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Min disk space free on " + this.displayName + ": " + this.minFreeDiskSpace + " Gb\n");
	}
	
}
