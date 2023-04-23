package org.jeyzer.profile.master;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jeyzer.analyzer.config.analysis.ConfigFilePattern;
import org.jeyzer.analyzer.data.TimeZoneInfo;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Web
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

public class MasterProfile {

	private String type;
	private String target;
	private String analysisFilePath;
	private boolean discoveryModeEnabled;
	private JzrProfileSecurity security;
	private List<ConfigFilePattern> filePatterns = new ArrayList<>(6);
	private TimeZoneInfo defaultRecordingTimeZone;
	private TimeZoneInfo defaultReportTimeZone;
	private List<String> redirectPatterns;

	public MasterProfile(String type, String analysisFilePath, boolean discoveryModeEnabled, List<String> redirectPatterns, List<ConfigFilePattern> filePatterns, JzrProfileSecurity security, TimeZoneInfo defaultRecordingTimeZone, TimeZoneInfo defaultReportTimeZone) {
		this.type = type; // display name
		this.analysisFilePath = analysisFilePath;
		this.target = new File(analysisFilePath).getParentFile().getName();     // used for the path with JEYZER_TARGET_PROFILE
		this.discoveryModeEnabled = discoveryModeEnabled;
		this.filePatterns = filePatterns;
		this.security = security;
		this.defaultRecordingTimeZone = defaultRecordingTimeZone;
		this.defaultReportTimeZone = defaultReportTimeZone;
		this.redirectPatterns = redirectPatterns;
	}

	public String getType() {
		return type;
	}
	
	public String getTarget() {
		return target;
	}

	public String getAnalysisFilePath() {
		return analysisFilePath;
	}

	public boolean isDiscoveryModeEnabled() {
		return discoveryModeEnabled;
	}
	
	public List<ConfigFilePattern> getFilePatterns() {
		return filePatterns;
	}

	public JzrProfileSecurity getSecurity() {
		return security;
	}

	public TimeZoneInfo getDefaultRecordingTimeZone() {
		return defaultRecordingTimeZone;
	}

	public TimeZoneInfo getDefaultReportTimeZone() {
		return defaultReportTimeZone;
	}
	
	public List<String> getRedirectionPatterns() {
		return redirectPatterns;
	}
}
