package org.jeyzer.monitor.util;

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



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.jeyzer.monitor.config.ConfigMonitor;
import org.jeyzer.monitor.engine.event.MonitorAnalyzerEvent;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.MonitorSystemEvent;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;


public final class MonitorHelper {

	private static final Logger logger = LoggerFactory.getLogger(MonitorHelper.class);
	
	// Purely for display, in monitored process time zone
	// Important : time zone will be set at session analysis time
	// Make it thread safe. No memory leaking
	private static ThreadLocal<SimpleDateFormat> sdf = ThreadLocal.withInitial(
				() -> new SimpleDateFormat(ConfigMonitor.getDateFormat())
			);
		
	// Display in local time zone
	// Used for the internal monitoring which cannot rely on the Jeyzer session
	// Make it thread safe. No memory leaking
	private static ThreadLocal<SimpleDateFormat> sdfLocal = ThreadLocal.withInitial(
				() -> new SimpleDateFormat(ConfigMonitor.getDateFormat())
			);
	
	private MonitorHelper(){
	}	
	
	public static String formatDate(Date date){
		return sdf.get().format(date); // expected to be in monitored process time zone
	}
	
	public static Date convertToTimeZone(Date date) {
		// given date is in local time
		long localOffset = new GregorianCalendar().getTimeZone().getRawOffset();
		long localDstOffset = new GregorianCalendar().getTimeZone().getDSTSavings();

		// the requested display time zone
		long targetOffset = sdf.get().getTimeZone().getRawOffset();
		long targetDstOffset = sdf.get().getTimeZone().getDSTSavings();
		
		// align the local time to UTC and then align to the target time, considering also the DST offsets
		return new Date(date.getTime() - localOffset - localDstOffset + targetOffset + targetDstOffset);
	}
	
	public static void setMonitoredProcessTimeZone(TimeZone zone){
		if (zone != null)
			sdf.get().setTimeZone(zone);
	}
	
	public static String formatLocalDate(Date date) {
		return sdfLocal.get().format(date);
	}
	
	public static Date parseLocalDate(String date){
		try {
			return sdfLocal.get().parse(date);
		} catch (ParseException e) {
			logger.error("Failed to convert " + date + " into java date.", e);
		}
		
		return null;
	}
	
	public static String getPrintableDuration(long time){
		String duration = "";
		Duration dt;
		try {
			dt = DatatypeFactory.newInstance().newDuration(time);
			if (dt.getMinutes() == 0)
				duration = (dt.getSeconds() > 0 ? dt.getSeconds() : 1) + " s";
			if (dt.getMinutes() > 0)
				duration = dt.getMinutes() + " mn " + duration;
			if (dt.getHours() > 0)
				duration = dt.getHours() + " h "+ duration; // add the hours if available
		} catch (DatatypeConfigurationException e) {
			logger.error("Failed to print duration", e);
		}
		return duration;
	}
	
	public static List<String> parseStrings(String value) {
		List<String> values = new ArrayList<>();
		
		if (value == null || value.trim().isEmpty())
			return values;
		
		String[] parts = value.trim().split(",");
		for (int i=0; i<parts.length; i++){
			String part = parts[i].trim();
			if (!part.isEmpty())
				values.add(part);			
		}
		
		return values;
	}
	
	public static void displayMemoryUsage(){
        int mb = 1024*1024;
        
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
         
        logger.info("Heap utilization statistics :");
         
        //Print used memory
        logger.info(" - Used Memory  : {} Mb", (runtime.totalMemory() - runtime.freeMemory()) / mb);
 
        //Print free memory
        logger.info(" - Free Memory  : {} Mb", runtime.freeMemory() / mb);
         
        //Print total available memory
        logger.info(" - Total Memory : {} Mb", runtime.totalMemory() / mb);
 
        //Print Maximum available memory
        logger.info(" - Max Memory   : {} Mb", runtime.maxMemory() / mb);
	}
	
	public static List<MonitorEvent> buildElectedEventSortedList(Multimap<String, MonitorEvent> events, boolean groupSorting){
		// get elected events
		Collection<MonitorEvent> candidateEvents = events.values();
		List<MonitorEvent> electedEvents = new ArrayList<>();

		Iterator<MonitorEvent> iter = candidateEvents.iterator();
		while (iter.hasNext()){
			MonitorEvent event = iter.next();
			if (event.isElected())
				electedEvents.add(event);
		}
		
		if (groupSorting)
			Collections.sort(electedEvents, new MonitorEvent.MonitorEventGroupComparable());
		else
			Collections.sort(electedEvents, new MonitorEvent.MonitorEventComparable());
		return electedEvents;
	}

	public static List<MonitorEvent> buildElectedEventSortedListForReport(
			Multimap<String, MonitorTaskEvent> taskEvents,
			Multimap<String, MonitorSessionEvent> sessionEvents,
			Multimap<String, MonitorSystemEvent> systemEvents, 
			boolean groupSorting,
			boolean cleanupDuplicateEvents) {

		Multimap<String, MonitorEvent> events = LinkedListMultimap.create();

		if (cleanupDuplicateEvents){
			// If enabled, it means that several rules of same nature have been defined and are generating therefore duplicate events.
			// It would be preferable to review the rules, which is not always possible when handling various rule sets.
			// Note that, within one rule, the threshold management is already and always preventing the event duplication. 
			filterDuplicateTaskEvents(taskEvents);
			filterDuplicateSessionEvents(sessionEvents);
			filterDuplicateSystemEvents(systemEvents);
		}
		
		for (Entry<String, MonitorTaskEvent> entry : taskEvents.entries()){
			events.put(entry.getKey(), entry.getValue());
		}

		for (Entry<String, MonitorSessionEvent> entry : sessionEvents.entries()){
			events.put(entry.getKey(), entry.getValue());
		}

		for (Entry<String, MonitorSystemEvent> entry : systemEvents.entries()){
			events.put(entry.getKey(), entry.getValue());
		}
		
		return buildElectedEventSortedList(events, groupSorting);
	}

	public static void filterDuplicateSystemEvents(Multimap<String, MonitorSystemEvent> systemEvents) {
		Collection<MonitorEvent> events = new ArrayList<MonitorEvent>(systemEvents.values());
		Collection<MonitorEvent> eventsToRemove = filterDuplicateEvents(events);
		for (MonitorEvent event: eventsToRemove)
			systemEvents.remove(event.getId(), event);
	}

	public static void filterDuplicateTaskEvents(Multimap<String, MonitorTaskEvent> taskEvents) {
		Collection<MonitorEvent> events = new ArrayList<MonitorEvent>(taskEvents.values());
		Collection<MonitorEvent> eventsToRemove = filterDuplicateEvents(events);
		for (MonitorEvent event: eventsToRemove)
			taskEvents.remove(event.getId(), event);
	}

	public static void filterDuplicateSessionEvents(Multimap<String, MonitorSessionEvent> sessionEvents) {
		Collection<MonitorEvent> events = new ArrayList<MonitorEvent>(sessionEvents.values());
		Collection<MonitorEvent> eventsToRemove = filterDuplicateEvents(events);
		for (MonitorEvent event: eventsToRemove)
			sessionEvents.remove(event.getId(), event);
	}
	
	private static Collection<MonitorEvent> filterDuplicateEvents(Collection<MonitorEvent> events) {
		Collection<MonitorEvent> eventsToRemove = new ArrayList<>();
		for (MonitorEvent event: events){
			if (eventsToRemove.contains(event))
				continue;
			for (MonitorEvent candidate: events){
				if (candidate == event || eventsToRemove.contains(candidate))
					continue;
				if (event.equalsIgnoringCategory(candidate))
					if (event.getLevel().isMoreCritical(candidate.getLevel())>0)
						eventsToRemove.add(candidate);
					else
						eventsToRemove.add(event);
			}
		}
		return eventsToRemove;
	}

	public static List<MonitorEvent> buildElectedEventSortedList(
			Multimap<String, MonitorTaskEvent> taskEvents,
			Multimap<String, MonitorSessionEvent> sessionEvents,
			Multimap<String, MonitorSystemEvent> systemEvents, 
			Multimap<String, MonitorAnalyzerEvent> analyzerEvents,
			boolean cleanupDuplicateEvents) {

		Multimap<String, MonitorEvent> events = LinkedListMultimap.create();
		
		if (cleanupDuplicateEvents){
			// If enabled, it means that several rules of same nature have been defined and are generating therefore duplicate events.
			// It would be preferable to review the rules, which is not always possible when handling various rule sets.
			// Note that, within one rule, the threshold management is already and always preventing the event duplication. 
			filterDuplicateTaskEvents(taskEvents);
			filterDuplicateSessionEvents(sessionEvents);
			filterDuplicateSystemEvents(systemEvents);
		}
		
		for (Entry<String, MonitorTaskEvent> entry : taskEvents.entries()){
			events.put(entry.getKey(), entry.getValue());
		}

		for (Entry<String, MonitorSessionEvent> entry : sessionEvents.entries()){
			events.put(entry.getKey(), entry.getValue());
		}
		
		for (Entry<String, MonitorSystemEvent> entry : systemEvents.entries()){
			events.put(entry.getKey(), entry.getValue());
		}
		
		for (Entry<String, MonitorAnalyzerEvent> entry : analyzerEvents.entries()){
			events.put(entry.getKey(), entry.getValue());
		}
		
		return buildElectedEventSortedList(events, false); // time sorting
	}
	
	public static boolean isEventThresholdMatched(List<MonitorEvent> events, Level eventThreshold, boolean reemit) {
		for (MonitorEvent event : events){
			boolean publish = reemit ? true : !event.isPublished(); 
			if (publish && event.getLevel().isMoreCritical(eventThreshold) >= 0)
				return true;
		}

		return false;
	}
	
	public static boolean isEventCategoryMatched(List<MonitorEvent> events, Level level) {
		for (MonitorEvent event : events){
			if (event.getLevel().equals(level))
				return true;
		}

		return false;
	}
}
