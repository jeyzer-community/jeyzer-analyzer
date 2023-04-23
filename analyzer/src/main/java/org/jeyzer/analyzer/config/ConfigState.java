package org.jeyzer.analyzer.config;

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



/*
 * Analyzer configuration states :
 * 
 * 	 Production :  Well calibrated profiles and monitoring rules. Suitable for production and QA environments
 *                 Required for integration with external systems and email sending
 *                 Jeyzer Web relies on this state to display the criticality level of the generated JZR report
 *                 
 * 	 Generic    :  Agnostic applicative profile
 *                 Generated monitoring events may have no meaning
 *                 
 *   Draft      :  Work in progress profile
 *                 Generated monitoring events may have no meaning at this stage
 *                 Default state if not specified
 *                 
 *   Disabled   :  Profile cannot be loaded and initialization exception is thrown.
 *                 Jeyzer Web will simply ignore it
 *                 Useful when handling large set of profiles through Jeyzer Web 
 *                 Variable should then be used for activating sets of master profiles : state="${XY_APP_PROFILE_GROUP_STATE}"
 */
public enum ConfigState {
	
	PRODUCTION, GENERIC, DRAFT, DISABLED;
	
	public String getDisplayValue() {
		return this.name().toLowerCase();
	}
	
	public boolean isProduction() {
		return PRODUCTION.equals(this);
	}
	
	public boolean isGeneric() {
		return GENERIC.equals(this);
	}
	
	public boolean isDisabled() {
		return DISABLED.equals(this);
	}
}
