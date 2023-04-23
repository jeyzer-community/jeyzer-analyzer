package org.jeyzer.monitor.engine.rule.threshold;

import java.util.Comparator;

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







import java.util.Date;
import java.util.Map;

import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.jeyzer.monitor.engine.event.info.Level;
import org.jeyzer.monitor.engine.event.info.Scope;
import org.jeyzer.monitor.engine.event.info.SubLevel;

public abstract class MonitorThreshold {

	public static final float ACCEPTED_ERROR_DIFF = 1.3F; // 30%
	protected static final String SEPARATOR = " - ";
	
	protected ConfigMonitorThreshold cfg;
	protected SubLevel subLevel;
	protected Scope scope;
	protected short trustFactor;
	protected MatchType matchType;
	protected int hitCount = 0;
	
	public MonitorThreshold(ConfigMonitorThreshold thCfg, Scope scope, MatchType matchType, SubLevel defaultSubLevel) {
		this.cfg = thCfg;
		this.scope = scope;
		this.matchType = matchType;
		this.subLevel = SubLevel.NOT_SET.equals(thCfg.getSubLevel()) ?  defaultSubLevel : thCfg.getSubLevel();
		this.trustFactor = thCfg.getTrustFactor();
	}

	public String getRef(){
		return this.cfg.getRef();
	}
	
	public Level getLevel(){
		return this.cfg.getLevel();
	}
	
	public SubLevel getSubLevel(){
		return this.subLevel;
	}
	
	public short getTrustFactor(){
		return this.trustFactor;
	}
	
	public boolean isFullyTrustable(){
		return this.trustFactor == 100;
	}
	
	public Map<String, String> getCustomParameters(){
		return this.cfg.getCustomParameters();
	}
	
	public Scope getScope(){
		return this.scope;
	}
	
	public MatchType getMatchType(){
		return this.matchType;
	}
	
	public int getCount() {
		return this.cfg.getCount();
	}
	
	public long getTime() {
		return this.cfg.getTime();
	}
	
	public boolean isPercentageBased(){
		return MatchType.SIGNAL_PERCENTAGE.equals(this.matchType)
				|| MatchType.VALUE_PERCENTAGE.equals(this.matchType)
				|| MatchType.PATTERN_PERCENTAGE.equals(this.matchType);
	}
	
	public String getMessage(){
		return this.cfg.getMessage();
	}
	
	public int getHit(){
		return hitCount;
	}
	
	public abstract String getDisplayCondition();
	
	protected boolean timeElection(MonitorEvent event) {
		if (this.cfg.isTimeBound())
			return event.getDuration() >= this.cfg.getTime();
		else
			// count bound
			return event.getCount() >= this.cfg.getCount();
	}
	
	protected void hit(){
		hitCount++;
	}
	
	// can be overridden
	protected String buildEventId(String ruleRef, String eventId, Level category){
		return ruleRef  
				+ (eventId != null? SEPARATOR + eventId + SEPARATOR : SEPARATOR)
				+ matchType + SEPARATOR   // permits to distinguish threshold variants (ex : action pattern percentage) from original ones.
				+ scope.name() + SEPARATOR
				+ category.name();
	}
	
	protected boolean isAdjacentEvent(MonitorEvent event, Date now, int period){
		return event.getEndDate().getTime() > (now.getTime()-(long)(1000 * period * ACCEPTED_ERROR_DIFF));
	}
	
	/**
	 * 
	 * Threshold match type
	 *
	 */
	public enum MatchType{
		EXCEPTION("Exception"),
		DIFF("Diff"),
		PATTERN("Pattern"), 
		SIGNAL("Signal"),
		VALUE("Value"),
		CUSTOM("Custom"),
		CUSTOM_WITH_CONTEXT("Custom with context"),
		PATTERN_PERCENTAGE("Pattern %"),
		VALUE_PERCENTAGE("Value %"),
		SIGNAL_PERCENTAGE("Signal %"),
		APPLICATIVE("Applicative"),
		PUBLISHER("Publisher");

		private String label;
		
	    private MatchType(String label){
	    	this.label = label;
	    }
	    
	    @Override
		public String toString(){
			return this.label;
		}
	}
	
	public static class MonitorThresholdComparable implements Comparator<MonitorThreshold>{
		 
	    @Override
	    public int compare(MonitorThreshold t1, MonitorThreshold t2) {
	        return t1.getRef().compareTo(t2.getRef());
	    }
	}
}
