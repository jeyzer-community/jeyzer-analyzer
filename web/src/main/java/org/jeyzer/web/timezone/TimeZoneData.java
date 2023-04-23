package org.jeyzer.web.timezone;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Web
 * --
 * Copyright (C) 2020 - 2021 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class TimeZoneData {

	public List<String> getTimeZones() {
		
		// TimeZone.getAvailableIDs() returns too many values. This list is a sub set :
		String[] ids = new String[] { "America/Buenos_Aires", "America/Chicago", "America/Denver", "America/Detroit",
				"America/Los_Angeles", "America/Mexico_City", "America/Montreal", "America/New_York", "America/Toronto",
				"America/Vancouver", "Asia/Bangkok", "Asia/Calcutta", "Asia/Dubai", "Asia/Hong_Kong", "Asia/Istanbul",
				"Asia/Seoul", "Asia/Shanghai", "Asia/Singapore", "Asia/Tokyo", "Australia/Melbourne", "Brazil/East",
				"Brazil/West", "CET", "Canada/Atlantic", "Canada/Central", "Canada/Eastern", "Canada/Mountain",
				"Canada/Newfoundland", "Canada/Pacific", "Canada/Saskatchewan", "Canada/Yukon", "EET", "Etc/GMT",
				"Etc/GMT+0", "Etc/GMT+1", "Etc/GMT+10", "Etc/GMT+11", "Etc/GMT+12", "Etc/GMT+2", "Etc/GMT+3",
				"Etc/GMT+4", "Etc/GMT+5", "Etc/GMT+6", "Etc/GMT+7", "Etc/GMT+8", "Etc/GMT+9", "Etc/GMT-0", "Etc/GMT-1",
				"Etc/GMT-10", "Etc/GMT-11", "Etc/GMT-12", "Etc/GMT-13", "Etc/GMT-14", "Etc/GMT-2", "Etc/GMT-3",
				"Etc/GMT-4", "Etc/GMT-5", "Etc/GMT-6", "Etc/GMT-7", "Etc/GMT-8", "Etc/GMT-9", "Etc/GMT0",
				"Europe/Amsterdam", "Europe/Berlin", "Europe/Brussels", "Europe/Budapest", "Europe/Copenhagen",
				"Europe/Dublin", "Europe/Helsinki", "Europe/Istanbul", "Europe/Kiev", "Europe/Lisbon", "Europe/London",
				"Europe/Madrid", "Europe/Minsk", "Europe/Moscow", "Europe/Oslo", "Europe/Paris", "Europe/Prague",
				"Europe/Rome", "Europe/Sofia", "Europe/Stockholm", "Europe/Vienna", "Europe/Warsaw", "Europe/Zurich",
				"GMT", "Greenwich", "Hongkong", "Iceland", "Israel", "Japan", "Mexico/General",
				"Poland", "Portugal", "Singapore", "Turkey", "US/Alaska", 
				"US/Arizona", "US/Central", "US/East-Indiana", "US/Eastern", "US/Hawaii",
				"US/Michigan", "US/Mountain", "US/Pacific", "UTC", "WET", "EST",
				"HST", "MST", "ACT", "AET", "ART", "AST", "BST", "CAT", "CST", "EAT", "ECT",
				"IST", "JST", "NST", "PST", "SST" };
		
		List<String> idList = Arrays.asList(ids);
		idList.sort(Comparator.comparing(String::toString));
		
		return idList;
	}
}
