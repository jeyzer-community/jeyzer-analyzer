package org.jeyzer.analyzer.error;


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

public class JzrMasterProfileRedirectException extends JzrTranslatorException{
	
	private static final long serialVersionUID = -3642861179178917095L;

	private final String targetRedirectProfile;
	
	/**
	 * Target redirect exception
	 * To switch usually from a generic profile to a production one
	 */
	public JzrMasterProfileRedirectException(String redirectedProfile) {
		super();
		this.targetRedirectProfile = redirectedProfile;
	}

	public String getTargetRedirectProfile() {
		return targetRedirectProfile;
	}
}
