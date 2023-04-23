package org.jeyzer.monitor.engine.rule;

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







import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.threshold.MonitorThreshold;

public abstract class MonitorRule {

	private static final AtomicInteger idCounter = new AtomicInteger(1);
	
	private final int id;
	private final String source;
	private final String group;
	private final String name;
	private final String ref;
	private final String conditionDescription;
	private final String narrative;  // can be null
	private final String ticket;  // can be null, since v2.1
	
	private List<String> stickerRefs; // null for analyzer rules
	protected boolean enabled;
	protected boolean dynamic;

	public MonitorRule(String ruleName, String ruleRef, String conditionDescription, String narrative, String ticket, String ruleGroup, String source, boolean dynamic) throws JzrInitializationException {
		this.name = ruleName;
		this.ref = ruleRef;
		this.group = ruleGroup;
		this.source = source;
		this.conditionDescription = conditionDescription;
		this.narrative= narrative;
		this.ticket = ticket;
		this.id = getNextRuleId();
		this.enabled = true;
		this.dynamic = dynamic;
	}
	
	public MonitorRule(ConfigMonitorRule def, String ruleName, String conditionDescription) throws JzrInitializationException {
		this(ruleName, 
				def.getRef(),
				conditionDescription,
				def.getNarrative(),
				def.getTicket(),
				def.getGroup(),
				def.getSource(),
				def.isDynamic());
		this.stickerRefs = def.getStickerRefs();
		validateThresholds(def.getConfigMonitorThresholds());
	}

	public abstract boolean isAdvancedMonitoringBased(); // like CPU, memory, GC..
	
	public abstract List<MonitorThreshold> getThresholds();
	
	public abstract SubLevel getDefaultSubLevel();
	
	public abstract String getDefaultNarrative();
	
	public void disable(){
		enabled = false;
	}
		
	public String getName(){
		return name;
	}
	
	public String getRef(){
		return ref;
	}
	
	public String getGroup(){
		return group;
	}
	
	public String getSource(){
		return source;
	}
	
	public boolean isDynamic(){
		return dynamic;
	}
	
	public String getConditionDescription(){
		return conditionDescription;
	}
	
	public String getNarrative() {
		return narrative != null ? narrative : getDefaultNarrative();
	}
	
	public String getTicket() {
		return ticket;
	}

	public List<String> getStickerRefs(){
		return stickerRefs; // can be null
	}	
	
	public boolean isEnabled(){
		return enabled;
	}
	
	public int getId(){
		return id;
	}	
	
	protected void validateThresholds(List<ConfigMonitorThreshold> thresholds) throws JzrInitializationException {
		List<String> supportedThresholds = getSupportedThresholdTypes();
		
		if (supportedThresholds == null || supportedThresholds.isEmpty())
			throw new JzrInitializationException("Invalid rule " + this.getName() + " / " + this.getRef() + " : supported thresholds are missing");
		
		for (ConfigMonitorThreshold th : thresholds){
			if (!supportedThresholds.contains(th.getName()))
				throw new JzrInitializationException("Rule " + this.getName() + " / " + this.getRef() + " is not compatible with threshold : " + th.getName());
		}
	}
	
	protected abstract List<String> getSupportedThresholdTypes();
	
	private static int getNextRuleId() {
		return idCounter.incrementAndGet();
	}	
	
	public static class MonitorRuleComparable implements Comparator<MonitorRule>{
		 
	    @Override
	    public int compare(MonitorRule r1, MonitorRule r2) {
	        return (r1.getId()>r2.getId() ? 1 : (r1.getId()==r2.getId() ? 0 : -1));
	    }
	}
	
	public static class MonitorRuleRefComparable implements Comparator<MonitorRule>{
		 
	    @Override
	    public int compare(MonitorRule r1, MonitorRule r2) {
	        return r1.getRef().compareTo(r2.getRef());
	    }
	}
}
