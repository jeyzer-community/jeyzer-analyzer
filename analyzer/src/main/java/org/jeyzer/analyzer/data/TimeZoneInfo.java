package org.jeyzer.analyzer.data;

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


import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeZoneInfo {

	private static final Logger logger = LoggerFactory.getLogger(TimeZoneInfo.class);
	
	private static final String TIME_ZONE_ORIGIN_CUSTOM_MARKER = "c";
	private static final String TIME_ZONE_ORIGIN_PROCESS_MARKER = "p";
	private static final String TIME_ZONE_ORIGIN_JZR_MARKER = "jzr";
	private static final String TIME_ZONE_ORIGIN_JYZ_MARKER = "jyz";
	private static final String TIME_ZONE_ORIGIN_JFR_MARKER = "jfr";
	private static final String TIME_ZONE_ORIGIN_TDG_MARKER = "tdg";

	private static final String TIME_ZONE_ORIGIN_CUSTOM_DISPLAY = "CUSTOM";
	private static final String TIME_ZONE_ORIGIN_PROCESS_DISPLAY = "PROCESS";
	private static final String TIME_ZONE_ORIGIN_JFR_DISPLAY = "JFR";
	private static final String TIME_ZONE_ORIGIN_TDG_DISPLAY = "TDG";
	private static final String TIME_ZONE_ORIGIN_FILE_DISPLAY = "FILE NAME";
	private static final String TIME_ZONE_ORIGIN_USER_SELECTED = "USER SELECTED";
	private static final String TIME_ZONE_ORIGIN_PROFILE = "PROFILE";
	private static final String TIME_ZONE_ORIGIN_UNKNOWN_DISPLAY = "UNKNOWN";
	
//	private static Map<Long, String> abbreviations = new HashMap<>(24);
//	
//	static {
//		abbreviations.put(0L, "UTC");
//		abbreviations.put(3600000L, "CET");   	// +1
//		abbreviations.put(7200000L, "EET");		// +2 
//		abbreviations.put(10800000L, "EAT");	// +3 East Africa Time
//		abbreviations.put(14400000L, "GMT+4");	// +4
//		abbreviations.put(18000000L, "GMT+5");	// +5
//		abbreviations.put(21600000L, "GMT+6");	// +6
//		abbreviations.put(25200000L, "GMT+7");	// +7
//		abbreviations.put(28800000L, "CTT");	// +8  China Taiwan Time
//		abbreviations.put(32400000L, "JST");	// +9  Japan Standard Time
//		abbreviations.put(36000000L, "AET");	// +10 Australia Eastern Time
//		abbreviations.put(39600000L, "SST");	// +11 Solomon Standard Time 
//		abbreviations.put(43200000L, "NST");	// +12 New Zealand Standard Time
//		abbreviations.put(-3600000L, "GMT-1");  // -1
//		abbreviations.put(-7200000L, "GMT-2");	// -2 
//		abbreviations.put(-10800000L, "BET");	// -3  Brazil Eastern Time
//		abbreviations.put(-14400000L, "GMT-4");	// -4
//		abbreviations.put(-18000000L, "EST");	// -5  Eastern Standard Time
//		abbreviations.put(-21600000L, "CST");	// -6  Central Standard Time
//		abbreviations.put(-25200000L, "MST");	// -7  Mountain Standard Time
//		abbreviations.put(-28800000L, "PST");	// -8  Pacific Standard Time
//		abbreviations.put(-32400000L, "AST");	// -9  Alaska Standard Time
//		abbreviations.put(-36000000L, "HST");	// -10 Hawaii Standard Time
//		abbreviations.put(-39600000L, "GMT-11"); // -11  
//	}
	
	// Origin possible values
	public enum TimeZoneOrigin { CUSTOM, PROCESS, JZR, JFR, FILE, USER_SELECTED, PROFILE, UNKNOWN }

	private TimeZoneOrigin origin;
	private TimeZone zone;
	private String zoneCode;  // CST, UTC, etc..

	public TimeZoneInfo(){
		this.origin = TimeZoneOrigin.UNKNOWN;
		this.zone = new GregorianCalendar().getTimeZone();
		this.zoneCode = "UNKNOWN";
	}
	
	public TimeZoneInfo(TimeZoneOrigin origin, String zoneCode){
		this.origin = origin;
		this.zone = TimeZone.getTimeZone(zoneCode);
		this.zoneCode = zoneCode;
	}
	
	public TimeZoneInfo(TimeZone timeZone, String zoneCode) {
		this.origin = TimeZoneOrigin.FILE;
		this.zone = timeZone;
		this.zoneCode = zoneCode;
	}
	
	public TimeZoneInfo(String origin, TimeZone zone, String zoneCode){
		this.origin = parseOrigin(origin);
		this.zone = zone;
		this.zoneCode = zoneCode;
	}
	
	public String getZoneAbbreviation(){
		// Need a map as java doesn't provide such abbreviation access method
		// return abbreviations.get((long)zone.getRawOffset());
		return zoneCode;
	}

	public TimeZoneOrigin getOrigin() {
		return origin;
	}
	
	public String getOriginAbbreviation() {
		switch (this.origin){
		case CUSTOM:
			return TIME_ZONE_ORIGIN_CUSTOM_MARKER;
		case PROCESS:
			return TIME_ZONE_ORIGIN_PROCESS_MARKER;
		case JZR:
			return TIME_ZONE_ORIGIN_JZR_MARKER;
		case JFR:
			return TIME_ZONE_ORIGIN_JFR_MARKER;
		default:
			return null; 
		}
	}
	
	public String getDisplayOrigin() {
		switch (this.origin){
			case CUSTOM:
				return TIME_ZONE_ORIGIN_CUSTOM_DISPLAY;
			case PROCESS:
				return TIME_ZONE_ORIGIN_PROCESS_DISPLAY;
			case JZR:
				return TIME_ZONE_ORIGIN_TDG_DISPLAY;
			case JFR:
				return TIME_ZONE_ORIGIN_JFR_DISPLAY;
			case FILE:
				return TIME_ZONE_ORIGIN_FILE_DISPLAY;
			case USER_SELECTED:
				return TIME_ZONE_ORIGIN_USER_SELECTED;
			case PROFILE:
				return TIME_ZONE_ORIGIN_PROFILE;
			default:
				return TIME_ZONE_ORIGIN_UNKNOWN_DISPLAY;
		}
	}

	public TimeZone getZone() {
		return zone;
	}
	
	public boolean isUnknown(){
		return this.origin.equals(TimeZoneOrigin.UNKNOWN);
	}
	
	private TimeZoneOrigin parseOrigin(String origin) {
		if (TIME_ZONE_ORIGIN_CUSTOM_MARKER.equals(origin))
			return TimeZoneOrigin.CUSTOM;
		else if (TIME_ZONE_ORIGIN_PROCESS_MARKER.equals(origin))
			return TimeZoneOrigin.PROCESS;
		else if (TIME_ZONE_ORIGIN_JZR_MARKER.equals(origin) || TIME_ZONE_ORIGIN_JYZ_MARKER.equals(origin) || TIME_ZONE_ORIGIN_TDG_MARKER.equals(origin))
			return TimeZoneOrigin.JZR;
		else if (TIME_ZONE_ORIGIN_JFR_MARKER.equals(origin))
			return TimeZoneOrigin.JFR;
		else
			logger.warn("Failed to parse time zone origin : {}", origin);
		
		return null;
	}
	
}
