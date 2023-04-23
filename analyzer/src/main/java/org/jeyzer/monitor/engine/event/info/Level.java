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
 * Event Level
 *
 */
public enum Level{
	INFO(1, 'I'), 
	WARNING(2, 'W'), 
	ERROR(3, 'E'), // must be used for monitoring tool errors 
	CRITICAL(4, 'C'), 
	UNKNOWN(0, 'U');

    private int code;
    private char capital;
	
    private Level(int code, char capital){
    	this.code = code;
    	this.capital = capital;
    }
    
    @Override
	public String toString(){
		return this.name();
	}
	
	public int getCode(){
		return this.code;
	}
	
	public char getCapital(){
		return this.capital;
	}
	
	public int isMoreCritical(Level c){
		if (this.equals(c))
			return 0;
		if (this.code > c.getCode())
			return 1;
		else 
			return -1;
	}

	public static Level getLevel(String category){
		if (INFO.name().equalsIgnoreCase(category))
			return INFO;

		if (WARNING.name().equalsIgnoreCase(category))
			return WARNING;
		
		if (ERROR.name().equalsIgnoreCase(category))
			return ERROR;
		
		if (CRITICAL.name().equalsIgnoreCase(category))
			return CRITICAL;
		
		return UNKNOWN;
	}
	
	public static Level getLevelFromCapital(char cap){
		if (INFO.capital == cap)
			return INFO;

		if (WARNING.capital == cap)
			return WARNING;
		
		if (ERROR.capital == cap)
			return ERROR;
		
		if (CRITICAL.capital == cap)
			return CRITICAL;
		
		return UNKNOWN;
	}
	
}
