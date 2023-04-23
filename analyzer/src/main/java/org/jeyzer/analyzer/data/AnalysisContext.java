package org.jeyzer.analyzer.data;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2021 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import org.jeyzer.profile.master.MasterRepository;

public class AnalysisContext {
	
	private String origin;
	private MasterRepository masterRepository = null; // can be null
	private String redirectFrom = null; // can be null
	
	public AnalysisContext(String origin) {
		this.origin = origin;
	}
	
	public AnalysisContext(String origin, MasterRepository masterRepository) {
		this.origin = origin;
		this.masterRepository = masterRepository;
	}
	
	public AnalysisContext(String origin, MasterRepository masterRepository, String redirectFrom) {
		this.origin = origin;
		this.masterRepository = masterRepository;
		this.redirectFrom = redirectFrom;
	}
	
	public MasterRepository getMasterRepository() {
		return masterRepository;
	}
	public String getOrigin() {
		return origin;
	}
	public boolean isRedirect() {
		return redirectFrom != null;
	}
	public String getRedirectFrom() {
		return redirectFrom;
	}
}
