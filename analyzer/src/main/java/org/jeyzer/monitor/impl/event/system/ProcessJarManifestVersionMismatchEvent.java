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



import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.data.jar.ProcessJarVersion;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;

public class ProcessJarManifestVersionMismatchEvent extends MonitorSystemEvent {
	
	private Map<ProcessJarVersion, Map<String, String>> mismatchVersions;
	
	public ProcessJarManifestVersionMismatchEvent(Map<ProcessJarVersion, Map<String, String>> mismatchVersionsPerJar, MonitorEventInfo info) {
		super("Process jar manifest version mismatch", info);
		this.mismatchVersions = mismatchVersionsPerJar;
	}
	
	@Override
	public void updateContext(JzrSession session) {
		// nothing to store
	}

	@Override
	public void addPrintableExtraParameters(List<String> params) {
		params.add("Mismatch versions");
		params.add(buildMismatchVersionList());
	}

	@Override
	protected void dumpExtraParameters(StringBuilder msg) {
		msg.append("Mismatch versions : " + buildMismatchVersionList());
	}

	private String buildMismatchVersionList() {
		boolean start = true;
		StringBuilder builder = new StringBuilder();
		for (ProcessJarVersion jar : this.mismatchVersions.keySet()) {
			if (!start)
				builder.append(" , ");
			builder.append(jar.getJarFileName() + "  : ");
			Iterator<Map.Entry<String, String>> iter = this.mismatchVersions.get(jar).entrySet().iterator();
			boolean startEntry = true;
			while (iter.hasNext()) {
				if (!startEntry)
					builder.append(" , ");
				Map.Entry<String, String> entry = iter.next();
				builder.append(entry.getKey());
				builder.append("=");
				builder.append(entry.getValue());
				startEntry = false;
			}
			start = false;
		}
		return builder.toString();
	}
}
