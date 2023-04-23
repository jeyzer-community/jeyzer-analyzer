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

import java.util.List;
import java.util.stream.Stream;

public class TimeZoneService {

	   private TimeZoneData timeZoneData = new TimeZoneData();

	    public Stream<String> fetch(String filter, int offset, int limit) {
	        return timeZoneData.getTimeZones().stream()
	                .filter(zoneId -> filter == null || zoneId.toLowerCase().startsWith(filter.toLowerCase()))
	                .skip(offset).limit(limit);
	    }

	    public int count(String filter) {
	        return (int) timeZoneData.getTimeZones().stream()
	                .filter(zoneId -> filter == null || zoneId.toLowerCase().startsWith(filter.toLowerCase()))
	                .count();
	    }

	    public Stream<String> fetchPage(String filter, int page, int pageSize) {
	        return timeZoneData.getTimeZones().stream()
	                .filter(zoneId -> filter == null || zoneId.toLowerCase().startsWith(filter.toLowerCase()))
	                .skip((long)page * pageSize).limit(pageSize);
	    }

	    public int count() {
	        return timeZoneData.getTimeZones().size();
	    }

	    public List<String> fetchAll() {
	        return timeZoneData.getTimeZones();
	    }
}
