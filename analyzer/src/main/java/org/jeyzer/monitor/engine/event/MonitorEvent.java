package org.jeyzer.monitor.engine.event;

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







import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.util.MonitorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class MonitorEvent {

	private static final Logger logger = LoggerFactory.getLogger(MonitorEvent.class);
	
	// Printable attributes
	public static final String PRINT_EVENT = "EVENT";
	public static final String PRINT_REF = "REF";
	public static final String PRINT_SCOPE = "SCOPE";
	public static final String PRINT_LEVEL = "LEVEL";
	public static final String PRINT_SUB_LEVEL = "SUB LEVEL";
	public static final String PRINT_START_DATE = "START DATE";
	public static final String PRINT_END_DATE = "END DATE";
	public static final String PRINT_DURATION = "DURATION";
	public static final String PRINT_RECOMMENDATION = "RECOMMENDATION";
	public static final String PRINT_COUNT = "COUNT";
	public static final String PRINT_THREAD = "THREAD";
	public static final String PRINT_ACTION = "ACTION";
	public static final String PRINT_NA = "Not Applicable";
	public static final String PRINT_NOT_IMPLEMENTED = "Not Yet Implemented";
	public static final String PRINT_OTHER_INFO = "OTHER INFO";

	// Attribute indexes
	public static final int PARAM_EVENT_FIELD_INDEX = 0;
	public static final int PARAM_EVENT_VALUE_INDEX = 1;
	public static final int PARAM_REF_FIELD_INDEX = 2;
	public static final int PARAM_REF_VALUE_INDEX = 3;
	public static final int PARAM_SCOPE_VALUE_INDEX = 4;
	public static final int PARAM_LEVEL_VALUE_INDEX = 5;
	public static final int PARAM_SUB_LEVEL_VALUE_INDEX = 6;
	public static final int PARAM_ACTION_FIELD_INDEX = 7;
	public static final int PARAM_ACTION_VALUE_INDEX = 8;
	public static final int PARAM_THREAD_FIELD_INDEX = 9;
	public static final int PARAM_THREAD_VALUE_INDEX = 10;
	public static final int PARAM_START_DATE_FIELD_INDEX = 11;
	public static final int PARAM_START_DATE_VALUE_INDEX = 12;
	public static final int PARAM_END_DATE_FIELD_INDEX = 13;
	public static final int PARAM_END_DATE_VALUE_INDEX = 14;
	public static final int PARAM_DURATION_FIELD_INDEX = 15;
	public static final int PARAM_DURATION_VALUE_INDEX = 16;
	public static final int PARAM_RECOMMENDATION_FIELD_INDEX = 17;
	public static final int PARAM_RECOMMENDATION_VALUE_INDEX = 18;
	public static final int PARAM_COUNT_FIELD_INDEX = 19;
	public static final int PARAM_COUNT_VALUE_INDEX = 20;	
	
	public static class MonitorEventComparable implements Comparator<MonitorEvent>{
		 
	    @Override
	    public int compare(MonitorEvent e1, MonitorEvent e2) {
	    	Date d1 = e1 instanceof MonitorApplicativeEvent ? ((MonitorApplicativeEvent)e1).getApplicativeStartDate() : e1.getStartDate();
	    	Date d2 = e2 instanceof MonitorApplicativeEvent ? ((MonitorApplicativeEvent)e2).getApplicativeStartDate() : e2.getStartDate();
	    	int result = d1.compareTo(d2);
	    	if (result != 0)
	    		return result;
	    	
	    	// date equals : put applicative events first
	    	if (e1 instanceof MonitorApplicativeEvent && !(e2 instanceof MonitorApplicativeEvent))
	    		return -1;
	    	else if (!(e1 instanceof MonitorApplicativeEvent) && e2 instanceof MonitorApplicativeEvent)
	    		return 1;
	    	
	    	// date equals : put critical first
	    	if (e1.getLevel().isMoreCritical(e2.getLevel())>0)
	    		return -1;
	    	if (e1.getLevel().isMoreCritical(e2.getLevel())<0)
	    		return 1;
	    	
	    	if (!e1.getName().equals(e2.getName()))
	    		return e1.getName().compareTo(e2.getName());

	    	if (e1.getSubLevel().value() > e2.getSubLevel().value())
	    		return -1;
	    	if (e1.getSubLevel().value() < e2.getSubLevel().value())
	    		return 1;
	    	
	    	return 0;
	    }
	}
	
	public static class MonitorEventGroupComparable implements Comparator<MonitorEvent>{
		 
	    @Override
	    public int compare(MonitorEvent e1, MonitorEvent e2) {
	    	if (e1.getLevel().isMoreCritical(e2.getLevel())>0)
	    		return -1;
	    	if (e1.getLevel().isMoreCritical(e2.getLevel())<0)
	    		return 1;

	    	if (!e1.getName().equals(e2.getName()))
	    		return e1.getName().compareTo(e2.getName());

	    	if (e1.getSubLevel().value() > e2.getSubLevel().value())
	    		return -1;
	    	if (e1.getSubLevel().value() < e2.getSubLevel().value())
	    		return 1;
	    	
		    // equals
	    	Date d1 = e1 instanceof MonitorApplicativeEvent ? ((MonitorApplicativeEvent)e1).getApplicativeStartDate() : e1.getStartDate();
	    	Date d2 = e2 instanceof MonitorApplicativeEvent ? ((MonitorApplicativeEvent)e2).getApplicativeStartDate() : e2.getStartDate();
	    	return d1.compareTo(d2);
	    }
	}
	
	/**
	 * 
	 * Monitor Event
	 *
	 */
	protected final String name;
	protected final MonitorEventInfo info;
	
	protected boolean elected = false;  // start as elligible
	protected boolean inProgress = true;  // start as in progress
	protected boolean triggered = false;  // start as not rule triggered
	protected boolean published = false;  // start as not communicated
	protected int count;
	
	public MonitorEvent(String eventName, MonitorEventInfo info){
		this.name = eventName;
		this.info = info;
		this.count = 1;
	}
	
	public String getId(){
		return this.info.getId();
	}

	public String dump() {
		StringBuilder msg = new StringBuilder(400);
		dumpHeader(msg, this.name);
		dumpExtraParameters(msg);
		dumpFooter(msg);
		return msg.toString();
	}
	
	protected abstract void dumpExtraParameters(StringBuilder msg);
	
	public abstract List<String> getPrintableParameters();
	
	public abstract void addPrintableExtraParameters(List<String> params);
	
	public String getName(){
		return this.name;
	}

	// can be null
	public abstract String getNameExtraInfo();
	
	public Scope getScope(){
		return this.info.getScope();
	}
	
	public void elect(){
		this.elected = true;
	}
	
	public boolean isElected(){
		return this.elected;
	}
	
	public void setProgressStatus(Date endSession){
		this.inProgress = this.info.getEndDate().equals(endSession);
	}
	
	public boolean isInProgress(){
		return inProgress;
	}	

	public void actionsTriggered() {
		this.triggered = true;
	}
	
	public boolean hasTriggeredActions() {
		return triggered;
	}
	
	public void publish() {
		this.published = true;
	}
	
	public boolean isPublished() {
		return published;
	}

	public String getRef(){
		return info.getRef();
	}	
	
	public Date getStartDate(){
		return this.info.getStartDate();
	}	
	
	public Date getEndDate(){
		return this.info.getEndDate();
	}
	
	public String getMessage(){
		return info.getMessage();
	}
	
	public Level getLevel(){
		return this.info.getLevel();
	}
	
	public SubLevel getSubLevel(){
		return this.info.getSubLevel();
	}
	
	public String getRank(){
		return this.info.getRank();
	}
	
	public String getTicket(){
		return info.getTicket();
	}
	
	public boolean hasTicket(){
		return info.hasTicket();
	}
	
	public void updateCount(){
		this.count++;
	}
	
	public void updateEnd(Date date) {
		this.info.updateEnd(date);
	}
	
	public String getPrintableDuration(){
		return getPrintableDuration(this.info.getEndDate().getTime() - this.info.getStartDate().getTime());
	}
	
	public String getPrintableDuration(long time){
		if (time == 0)
			return "Instantaneous";
		try {
			Duration dt = DatatypeFactory.newInstance().newDuration(time);
			return dt.getHours() + " hours "+ dt.getMinutes() + " minutes " + dt.getSeconds() + " seconds";
		} catch (DatatypeConfigurationException e) {
			logger.error("Failed to print duration", e);
			return "Not available";
		}	
	}	
	
	public String getPrintableEndDate(){
		return this.isInProgress() ? "IN PROGRESS" : getPrintableDate(this.info.getEndDate());
	}
	
	public String getPrintableDate(Date date){
		return MonitorHelper.formatDate(date);
	}
	
	/**
	 * Duration in seconds
	 */
	public long getDuration(){
		return this.info.getEndDate().equals(this.info.getStartDate())?  1 : (this.info.getEndDate().getTime()-this.info.getStartDate().getTime())/1000;  // let'say that smallest action takes 1 sec..
	}
	
	public long getCount(){
		return this.count;
	}
	
	protected List<String> getPrintableParameters(String action, String thread, String endDate, String duration){

		List<String> params = new ArrayList<>();
		params.add(PRINT_EVENT);
		params.add(this.name);
		params.add(PRINT_REF);
		params.add(this.info.getRef());
		params.add(this.info.getScope().toString());
		params.add(this.info.getLevel().toString());
		params.add(this.info.getSubLevel().toString());
		params.add(PRINT_ACTION);
		params.add(action);
		params.add(PRINT_THREAD);
		params.add(thread);
		params.add(PRINT_START_DATE);
		params.add(getPrintableDate(this.info.getStartDate()));
		params.add(PRINT_END_DATE);
		params.add(endDate);
		params.add(PRINT_DURATION);
		params.add(duration);
		params.add(PRINT_RECOMMENDATION);
		params.add(this.info.getMessage());
		params.add(PRINT_COUNT);
		params.add(Integer.toString(this.count));
		
		return params;
	}
	
	protected void dumpHeader(StringBuilder msg, String event) {
		msg.append("===============================\n");
		msg.append("EVENT : " + event + "\n");
		msg.append("REF : " + this.info.getRef() + "\n");
		msg.append("LEVEL : " + this.info.getLevel() + "\n");
		msg.append("SUB LEVEL : " + this.info.getSubLevel() + "\n");
		msg.append("COUNT : " + this.count + "\n");
		msg.append("START DATE :" + getPrintableDate(this.info.getStartDate()) + "\n");
		msg.append("END DATE :" + getPrintableDate(this.info.getEndDate()) + "\n");
		msg.append("DURATION : " + getPrintableDuration() + "\n");
	}
	
	protected void dumpFooter(StringBuilder msg) {
		msg.append("RECOMMENDATION : " + this.info.getMessage() + "\n");
		msg.append("===============================\n");
	}

	public boolean equalsIgnoringCategory(MonitorEvent candidate) {
		if (! (this.name.equals(candidate.getName())
					&& this.info.getStartDate().equals(candidate.getStartDate())
					&& this.count == candidate.getCount()
					&& this.info.getRef().equals(candidate.getRef())))
			return false;
		if (getNameExtraInfo() == null && candidate.getNameExtraInfo() == null)
			return true;
		if (getNameExtraInfo() == null && candidate.getNameExtraInfo() != null)
			return false;
		if (getNameExtraInfo() != null && candidate.getNameExtraInfo() == null)
			return false;
		return getNameExtraInfo().equals(candidate.getNameExtraInfo());
	}
	
}
