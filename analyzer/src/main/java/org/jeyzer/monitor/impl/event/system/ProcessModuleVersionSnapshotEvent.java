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

import org.jeyzer.analyzer.data.module.ProcessModule;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class ProcessModuleVersionSnapshotEvent extends MonitorSystemEvent {
	
	private List<ProcessModule> modules;
	
	public ProcessModuleVersionSnapshotEvent(List<ProcessModule> modules, MonitorEventInfo info) {
		super("Process module version snapshot", info);
		this.modules = modules;
	}
	
	@Override
	public void updateContext(JzrSession session) {
		// nothing to store
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Snapshot Java modules");
		params.add(buildSnapshotModuleList());
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Snapshot Java modules : " + buildSnapshotModuleList());
	}

	private String buildSnapshotModuleList() {
		boolean start = true;
		StringBuilder builder = new StringBuilder();
		for (ProcessModule module : this.modules) {
			if (!start)
				builder.append(" , ");
			builder.append(module.getName() + " " + module.getVersion());
			start = false;
		}
		return builder.toString();
	}
}
