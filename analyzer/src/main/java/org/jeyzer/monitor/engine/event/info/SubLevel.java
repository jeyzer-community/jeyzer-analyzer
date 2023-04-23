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
 * Event Sub Level
 *
 */
public enum SubLevel{
	CUSTOM_VERY_HIGH(10),
	CUSTOM_HIGH(9),
	CUSTOM_MEDIUM(8), 
	CUSTOM_LOW(7),
	CUSTOM_VERY_LOW(6),
	DEFAULT_VERY_HIGH(5),
	DEFAULT_HIGH(4),
	DEFAULT_MEDIUM(3), 
	DEFAULT_LOW(2),
	DEFAULT_VERY_LOW(1),
	INVALID(0),
	NOT_SET(-1);

	private int level;
	
    private SubLevel(int value){
    	this.level = value;
    }
    
    @Override
	public String toString(){
		return Integer.toString(this.level);
	}
    
	public int value(){
		return this.level;
	}

	public static SubLevel getSubLevel(String subLevel){
		if (subLevel == null || subLevel.isEmpty())
			return NOT_SET;
		
		if (CUSTOM_VERY_HIGH.toString().equalsIgnoreCase(subLevel))
			return CUSTOM_VERY_HIGH;
	
		if (CUSTOM_HIGH.toString().equalsIgnoreCase(subLevel))
			return CUSTOM_HIGH;
		
		if (CUSTOM_MEDIUM.toString().equalsIgnoreCase(subLevel))
			return CUSTOM_MEDIUM;
		
		if (CUSTOM_LOW.toString().equalsIgnoreCase(subLevel))
			return CUSTOM_LOW;
		
		if (CUSTOM_VERY_LOW.toString().equalsIgnoreCase(subLevel))
			return CUSTOM_VERY_LOW;
		
		return INVALID;
	}
}
