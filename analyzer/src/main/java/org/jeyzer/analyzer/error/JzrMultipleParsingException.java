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







import java.util.ArrayList;
import java.util.List;

public class JzrMultipleParsingException extends JzrParsingException {

	private static final long serialVersionUID = 20292450638234523L;

	private List<Exception> errors = new ArrayList<>();
	
	public JzrMultipleParsingException() {
		super();
	}
	
	public void addException(Exception ex){
		this.errors.add(ex);
	}
	
	@Override
	public String getMessage(){
		return "\nThread dump parsing failed with the following "
				+ getErrorMessageSuffix()
				+ " : "
				+ getMessages();
	}
	
	private String getErrorMessageSuffix() {
		if (errors.size() == 1)
			return "error";
		if (errors.size() <= 5)
			return "errors";
		else
		   return "5 errors " + "(out of " + errors.size() + ")"; 
	}

	private String getMessages(){
		StringBuilder msg = new StringBuilder();
		
		// display only the first 5 errors
		int i=0;
		for (Exception ex : this.errors){
			msg.append("\n   ");
			if (ex instanceof NullPointerException)
				msg.append("Null pointer exception on thread dump parsing");
			else
				msg.append(ex.getMessage());
			i++;
			if (i==5)
				break;
		}
		return msg.toString();
	}
}
