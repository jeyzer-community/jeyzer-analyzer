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

import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

import com.google.common.collect.Multimap;

public class ProcessJarMultipleVersionsEvent extends MonitorSystemEvent {
	
	private Multimap<String, String> duplicates;
	
	public ProcessJarMultipleVersionsEvent(Multimap<String, String> duplicates, MonitorEventInfo info) {
		super("Process jar version duplicates", info);
		this.duplicates = duplicates;
	}
	
	@Override
	public void updateContext(JzrSession session) {
		// nothing to store
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Duplicate libraries");
		params.add(buildDuplicateLibraryList());
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Duplicate libraries : " + buildDuplicateLibraryList());
	}

	private String buildDuplicateLibraryList() {
		boolean start = true;
		StringBuilder builder = new StringBuilder();
		for (String jarName : this.duplicates.keySet()) {
			if (!start)
				builder.append(" , ");
			builder.append(jarName + "  : ");
			for (String jarVersion : this.duplicates.get(jarName)) {
				builder.append(jarVersion);
				builder.append(" ");
			}
			start = false;
		}
		return builder.toString();
	}
}
