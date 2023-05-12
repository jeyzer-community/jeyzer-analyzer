package org.jeyzer.analyzer.input.translator.jfr.mapper;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2021 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.util.List;

import org.jeyzer.analyzer.data.flags.JVMFlag;
import org.jeyzer.analyzer.input.translator.jfr.reader.JFRDescriptor;
import org.jeyzer.analyzer.input.translator.jfr.reader.JFRReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.jfr.consumer.RecordedEvent;

public class JZRJVMFlagMapper {
	
	private static final Logger logger = LoggerFactory.getLogger(JZRJVMFlagMapper.class);
	
	private static final String FIELD_VALUE = "value";
	private static final String FIELD_NEW_VALUE = "newValue";
	private static final String FIELD_OLD_VALUE = "oldValue";
	
	public void mapFlags(JFRDescriptor descriptor, List<String> entries) {
		for (RecordedEvent event : descriptor.getJVMFlagEvents()) {
			String entry = createEntry(event);
			if (!entries.contains(entry))
				entries.add(entry); // keep only the first one in case of multiple occurrences
		}
	}

	private String createEntry(RecordedEvent event) {
		String entry;
		String type = event.getEventType().getName();
		boolean changed = type.endsWith("Changed");
		
		entry = event.getString("name");
		entry += JVMFlag.SEPARATOR;
		entry += type;
		entry += JVMFlag.SEPARATOR;
		entry += getValue(event, type); 
		entry += JVMFlag.SEPARATOR;
		entry += (changed ? getOldValue(event, type) : "");
		entry += JVMFlag.SEPARATOR;
		entry += event.getString("origin");
		entry += JVMFlag.SEPARATOR;	
		entry += (changed ? event.getStartTime().toEpochMilli() : -1); // change date
		
		return entry;
	}

	private String getValue(RecordedEvent event, String type) {
		switch (type) {
			case JFRReader.JFR_JDK_DOUBLEFLAG :
				return Double.toString(event.getDouble(FIELD_VALUE));
			case JFRReader.JFR_JDK_DOUBLEFLAGCHANGED :
				return Double.toString(event.getDouble(FIELD_NEW_VALUE));
			case JFRReader.JFR_JDK_BOOLEANFLAG :
				return Boolean.toString(event.getBoolean(FIELD_VALUE));
			case JFRReader.JFR_JDK_BOOLEANFLAGCHANGED :
				return Boolean.toString(event.getBoolean(FIELD_NEW_VALUE));
			case JFRReader.JFR_JDK_INTFLAG :
			case JFRReader.JFR_JDK_UNSIGNEDINTFLAG :
				return Integer.toString(event.getInt(FIELD_VALUE));
			case JFRReader.JFR_JDK_INTFLAGCHANGED :
			case JFRReader.JFR_JDK_UNSIGNEDINTFLAGCHANGED :
				return Integer.toString(event.getInt(FIELD_NEW_VALUE));
			case JFRReader.JFR_JDK_LONGFLAG :
			case JFRReader.JFR_JDK_UNSIGNEDLONGFLAG :
				return Long.toString(event.getLong(FIELD_VALUE));
			case JFRReader.JFR_JDK_LONGFLAGCHANGED :
			case JFRReader.JFR_JDK_UNSIGNEDLONGFLAGCHANGED :
				return Long.toString(event.getLong(FIELD_NEW_VALUE));
			case JFRReader.JFR_JDK_STRINGFLAG :
				return event.getString(FIELD_VALUE);
			case JFRReader.JFR_JDK_STRINGFLAGCHANGED :
				return event.getString(FIELD_NEW_VALUE);
			default :
				logger.warn("Cannot get JVM flag value. JVM flag type not handled : {}", type); // should not happen
				return "-1";
		}
	}
	
	private String getOldValue(RecordedEvent event, String type) {
		switch (type) {
			// ref : https://bestsolution-at.github.io/jfr-doc/openjdk-15.html
			case JFRReader.JFR_JDK_DOUBLEFLAGCHANGED :
				return Double.toString(event.getDouble(FIELD_OLD_VALUE));
			case JFRReader.JFR_JDK_BOOLEANFLAGCHANGED :
				return Boolean.toString(event.getBoolean(FIELD_OLD_VALUE));
			case JFRReader.JFR_JDK_INTFLAGCHANGED :
			case JFRReader.JFR_JDK_UNSIGNEDINTFLAGCHANGED :
				return Integer.toString(event.getInt(FIELD_OLD_VALUE));
			case JFRReader.JFR_JDK_LONGFLAGCHANGED :
			case JFRReader.JFR_JDK_UNSIGNEDLONGFLAGCHANGED :
				return Long.toString(event.getLong(FIELD_OLD_VALUE));
			case JFRReader.JFR_JDK_STRINGFLAGCHANGED :
				return event.getString(FIELD_OLD_VALUE);
			default : 
				logger.warn("Cannot get JVM flag old value. JVM flag type not handled : {}", type); // should not happen
				return "-1";
		}
	}
}
