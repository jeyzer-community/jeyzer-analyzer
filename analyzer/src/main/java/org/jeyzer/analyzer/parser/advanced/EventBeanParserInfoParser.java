package org.jeyzer.analyzer.parser.advanced;

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




import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.event.ExternalEvent;
import org.jeyzer.analyzer.data.event.JzrPublisherEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBeanParserInfoParser {

	private static final Logger logger = LoggerFactory.getLogger(EventBeanParserInfoParser.class);
	
	public static final String JEYZER_APP_EVENT = "Jz app-evt=";
	public static final String JEYZER_PUB_EVENT = "Jz pub-evt=";
	public static final String JEYZER_EVENT_SEPARATOR = "::";
	
	public void parseApplicationEvent(ThreadDump dump, String line) {
		// Jz app-evt=Demo features::Features::::JZR_FEA_10::Ping pong task started::description::I::7::A::JZR_FEA_101573022200380::Ping pong task started : hit 1::1573022200380::1573022200380::19::100::true::ticket
		String eventLine = line.substring(JEYZER_APP_EVENT.length());
		
		int posStartParam = 0;
		int posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String source = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String service = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String type = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String code = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String name = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String narrative = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String level = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String sublevel = eventLine.substring(posStartParam, posEndParam);
		
		// Jz app-evt=Demo features::Features::::JZR_FEA_10::Ping pong task started::description::I::7::A::JZR_FEA_101573022200380::Ping pong task started : hit 1::1573022200380::1573022200380::19::100::true::ticket
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String scope = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String id = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String message = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String start = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String end = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String threadId = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String trust = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String oneshot = posEndParam != -1 ? eventLine.substring(posStartParam, posEndParam) : eventLine.substring(posStartParam);
		
		// ticket is introduced in v2.1
		String ticket = null;
		if (posEndParam != -1) {
			posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
			ticket = eventLine.substring(posStartParam);
		}
		
		ExternalEvent event = new ExternalEvent(
				source,
				service,
				type,
				code,
				name,
				narrative,
				level,
				sublevel,
				scope,
				id,
				message,
				ticket,
				start,
				end,
				threadId,
				trust,
				oneshot,
				dump.getTimestamp()
				);
		
		if (event.isValid())
			dump.addExternalEvent(event);
		else
			logger.warn("Invalid event for event id : " + id);
	}

	public void parsePublisherEvent(ThreadDump dump, String line) {
		// pub-evt=JZR_PUB_002::Jeyzer Publisher is active : applicative events and data are collected. Set the jeyzer.publisher.active system property to false to disable it. Requires applicative restart.::I::7::1574587975492
		String eventLine = line.substring(JEYZER_PUB_EVENT.length());
		
		int posStartParam = 0;
		int posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String code = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String name = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String level = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		posEndParam = eventLine.indexOf(JEYZER_EVENT_SEPARATOR, posStartParam);
		String sublevel = eventLine.substring(posStartParam, posEndParam);
		
		posStartParam = posEndParam + JEYZER_EVENT_SEPARATOR.length();
		String start = eventLine.substring(posStartParam);
		
		JzrPublisherEvent event = new JzrPublisherEvent(
				code,
				name,
				level,
				sublevel,
				start,
				dump.getTimestamp()
				);
		
		if (event.isValid())
			dump.addPublisherEvent(event);
		else
			logger.warn("Invalid publisher event for event name : " + name);
	}
}
