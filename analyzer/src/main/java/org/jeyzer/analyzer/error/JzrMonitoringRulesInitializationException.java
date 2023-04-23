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


import java.util.Map;

public class JzrMonitoringRulesInitializationException extends JzrInitializationException {

	private static final long serialVersionUID = -3364798112679730893L;

	private Map<String, Exception> errors;
			
	public JzrMonitoringRulesInitializationException(Map<String, Exception> errors) {
		super();
		this.errors = errors;
	}

	@Override
	public String getMessage(){
		return "\nMonitoring rules creation failed : \n" + getMessages();
	}
	
	public Map<String, Exception> getRuleInitializationErrors(){
		return this.errors;
	}
	
	private String getMessages(){
		StringBuilder msg = new StringBuilder();
		for (String ref : this.errors.keySet())
			msg.append("Creation of rule : " + ref + " failed with error : " + this.errors.get(ref).getMessage() + " \n");
		return msg.toString();
	}
}
