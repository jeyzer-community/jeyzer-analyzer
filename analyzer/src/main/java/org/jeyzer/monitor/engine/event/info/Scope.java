package org.jeyzer.monitor.engine.event.info;

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




/**
 * 
 * Event scope
 *
 */
public enum Scope{
	STACK("Stack"),
	ACTION("Action"), 
	SESSION("Session"),
	GLOBAL("Global"),
	SYSTEM("System"),
	ANALYZER("Analyzer");

	private String label;
	
    private Scope(String label){
    	this.label = label;
    }
    
    @Override
	public String toString(){
		return this.label;
	}
}
