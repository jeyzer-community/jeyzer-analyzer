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

import org.jeyzer.analyzer.data.jar.ProcessJarVersion;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class ProcessJarVersionAbsenceEvent extends MonitorSystemEvent {
	
	private List<ProcessJarVersion> jarVersions;
	
	public ProcessJarVersionAbsenceEvent(List<ProcessJarVersion> jarVersions, MonitorEventInfo info) {
		super("Process jar version absence", info);
		this.jarVersions = jarVersions;
	}
	
	@Override
	public void updateContext(JzrSession session) {
		// nothing to store
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Non versionned libraries");
		params.add(buildNonVersionnedLibraryList());
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Non versionned libraries : " + buildNonVersionnedLibraryList());
	}

	private String buildNonVersionnedLibraryList() {
		boolean start = true;
		StringBuilder builder = new StringBuilder();
		for (ProcessJarVersion jarVersion : this.jarVersions) {
			if (!start)
				builder.append(" , ");
			builder.append(jarVersion.getJarName());
			start = false;
		}
		return builder.toString();
	}
}
