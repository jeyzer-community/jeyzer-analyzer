package org.jeyzer.analyzer.data.tag;

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




import org.jeyzer.analyzer.data.stack.ThreadStack;



public class OperationTag extends Tag {
	
	public static final String DISPLAY_NAME = "Operation";

	public static final Tag OTBI_TAG = new OperationTag(ThreadStack.OPER_TO_BE_IDENTIFIED);	
	
	public OperationTag(String name) {
		super(name);
	}
	
	@Override
	public String getTypeName() {
		return DISPLAY_NAME;
	}	

}
