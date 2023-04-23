package org.jeyzer.analyzer.data.stack;

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

public class StackText{
	
	private int depth = 0;
	private int maxLength = 0;
	private StringBuilder stack = new StringBuilder(2000);
	
	public void appendLine(String line){
		stack.append(line + "\n");
		depth++;
		maxLength = line.length() > maxLength ? line.length() : maxLength;
	}
	
	public String getText(){
		return this.stack.toString(); 
	} 
	
	public int getDepth(){
		return this.depth;
	}
	
	public int getMaxlength(){
		return this.maxLength;
	}
}
