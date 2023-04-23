package org.jeyzer.monitor.config.engine;

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
import java.util.List;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.util.MonitorHelper;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigMonitorRule {

	public static final String JEYZER_SOURCE = "Jeyzer Analyzer";
	
	public static final String JZRM_RULE = "rule";
	public static final String JZRM_NAME = "name";
	private static final String JZRM_REF = "ref";
	private static final String JZRM_EXTRA_INFO = "extra_info";
	private static final String JZRM_TICKET = "ticket";
	private static final String JZRM_STICKERS = "stickers";
	
	private static final String JZRM_NARRATIVE = "narrative";
	
	private static final String JZRM_THRESHOLDS = "thresholds";
	private static final String JZRM_THRESHOLD = "threshold";
	
	private final String source;
	private final String group;
	private final String name;
	private final String ref;
	private final String extraInfo; // can be null
	private final String narrative; // can be null
	private final String ticket; // can be null
	private final List<ConfigMonitorThreshold> thresholds;
	private final List<String> stickerRefs;
	
	private final boolean dynamic;

	/**
	 * Constructor for Jeyzer rules
	 * @param dynamic 
	 */
	public ConfigMonitorRule(Element ruleNode, String group, List<String> groupStickerRefs, boolean dynamic) throws JzrInitializationException {
		this.source = JEYZER_SOURCE;
		this.group = group.intern();
		this.name = loadName(ruleNode);
		this.ref= loadRef(ruleNode);
		String value = ConfigUtil.getAttributeValue(ruleNode,JZRM_EXTRA_INFO);
		extraInfo = !value.isEmpty()? value : null;
		this.narrative = loadNarrative(ruleNode);
		this.ticket = loadTicket(ruleNode);
		this.stickerRefs = loadStickerRefs(ruleNode);
		this.stickerRefs.addAll(groupStickerRefs);
		this.thresholds = loadThresholds(ruleNode);
		this.dynamic = dynamic;
	}

	/**
	 * Constructor for External rules (see applicative and publisher events). 
	 * Applicative rules have only 1 threshold since event code (= rule ref) has by definition only 1 level
	 */
	public ConfigMonitorRule(ConfigMonitorThreshold threshold, String source, String group, String name, String ref, String narrative, String ticket) {
		this.source = source;
		this.group = group;
		this.name = name;
		this.ref= ref;
		this.extraInfo = null;
		this.narrative = narrative;
		this.ticket= ticket;
		this.stickerRefs = null;
		this.thresholds = new ArrayList<ConfigMonitorThreshold>(1);
		this.thresholds.add(threshold);
		this.dynamic = false;
	}
	
	private String loadName(Element ruleNode) throws JzrInitializationException {
		String name = ConfigUtil.getAttributeValue(ruleNode,JZRM_NAME);
		if (name.isEmpty())
			throw new JzrInitializationException("Monitoring rule is defined without name.");
		return name;
	}

	private String loadRef(Element ruleNode) throws JzrInitializationException {
		String ref = ConfigUtil.getAttributeValue(ruleNode,JZRM_REF);
		if (ref.isEmpty())
			throw new JzrInitializationException("Monitoring rule is defined without ref.");
		return ref;
	}
	
	// v2.1
	private String loadTicket(Element ruleNode) {
		String value = ConfigUtil.getAttributeValue(ruleNode,JZRM_TICKET);
		return value.isEmpty() ? null : value;
	}

	public static List<String> loadStickerRefs(Element ruleNode) {
		String value = ConfigUtil.getAttributeValue(ruleNode,JZRM_STICKERS);
		return MonitorHelper.parseStrings(value);
	}

	private List<ConfigMonitorThreshold> loadThresholds(Element ruleNode) throws JzrInitializationException {
		List<ConfigMonitorThreshold> thresholds = new ArrayList<>();
		Element thresholdsNode = ConfigUtil.getFirstChildNode(ruleNode, JZRM_THRESHOLDS);
		
		if (thresholdsNode == null)
			throw new JzrInitializationException("Monitoring Rule " + this.getRef() + " / " + this.getName() + " is defined without any threshold.");
		
		NodeList nodes = thresholdsNode.getElementsByTagName(JZRM_THRESHOLD);
		
		if (nodes.getLength() >= 10)
			throw new JzrInitializationException("Monitor rule " + this.getRef() + " / " + this.getName() + " must have less than 10 thresholds.");
		
		for (int i=0; i<nodes.getLength(); i++){
			Element thresholdNode = (Element)nodes.item(i);
			
			try {
				ConfigMonitorThreshold threshold = loadThreshold(thresholdNode, Integer.toString(i+1));
				thresholds.add(threshold);
			}catch (JzrInitializationException ex) {
				throw new JzrInitializationException("Failed to load threshold for rule " + this.getRef() + " / " + this.getName() + " due to : " + ex.getMessage(), ex);
			}
		}
		
		if (thresholds.isEmpty())
			throw new JzrInitializationException("Monitoring Rule " + this.getRef() + " / " + this.getName() + " is defined without any valid threshold.");
		
		return thresholds;
	}

	private ConfigMonitorThreshold loadThreshold(Element thresholdNode, String thresholdRef) throws JzrInitializationException {
		String type = ConfigUtil.getAttributeValue(thresholdNode,ConfigMonitorThreshold.JZRM_TYPE); 
		
		if (ConfigMonitorThreshold.isValidTaskType(type)){
			return new ConfigMonitorTaskThreshold(thresholdNode, type, thresholdRef);
		}
		else if (ConfigMonitorThreshold.isValidSessionType(type) 
				|| ConfigMonitorThreshold.isValidSystemType(type)){
			return new ConfigMonitorThreshold(thresholdNode, type, thresholdRef);
		}		
		else{
			throw new JzrInitializationException("Invalid monitoring rule type : " + type);
		}
	}
	
	private String loadNarrative(Element ruleNode) {
		Element narrativeNode = ConfigUtil.getFirstChildNode(ruleNode, JZRM_NARRATIVE);
		return ConfigUtil.getNodeText(narrativeNode);
	}
	
	public List<ConfigMonitorThreshold> getConfigMonitorThresholds(){
		return this.thresholds;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getRef(){
		return this.ref;
	}

	public String getGroup(){
		return this.group;
	}
	
	public String getSource(){
		return this.source;
	}
	
	// can be null
	public String getExtraInfo(){
		return this.extraInfo;
	}
	
	public List<String> getStickerRefs(){
		return this.stickerRefs;
	}

	// can be null
	public String getNarrative() {
		return this.narrative;
	}

	// can be null
	public String getTicket() {
		return ticket;
	}

	public boolean isDynamic() {
		return this.dynamic;
	}
}
