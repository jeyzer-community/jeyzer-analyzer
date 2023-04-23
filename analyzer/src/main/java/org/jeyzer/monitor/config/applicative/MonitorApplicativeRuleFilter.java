package org.jeyzer.monitor.config.applicative;

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
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.engine.rule.MonitorRule;
import org.w3c.dom.Element;

public class MonitorApplicativeRuleFilter {
	
	private static final String JZRM_ALL_APP_RULES = "all_applicative_rules";
	
	private static final String JZRM_REF_INCLUDES = "include_ref_patterns";
	private static final String JZRM_REF_EXCLUDES = "exclude_ref_patterns";
	
	private String ruleType;
	private boolean allowed;
	private List<Pattern> refIncludes = new ArrayList<>();
	private List<Pattern> refExcludes = new ArrayList<>();

	public MonitorApplicativeRuleFilter(Element rulesNode, String ruleType) throws JzrInitializationException {
		this.ruleType = ruleType;
		loadAllowed(rulesNode, ruleType);
		
		if (this.allowed) {
			// load ref includes
			loadRefFilters(rulesNode, refIncludes, JZRM_ALL_APP_RULES, JZRM_REF_INCLUDES);
			loadRefFilters(rulesNode, refIncludes, ruleType, JZRM_REF_INCLUDES);
			
			// load ref excludes
			loadRefFilters(rulesNode, refExcludes, JZRM_ALL_APP_RULES, JZRM_REF_EXCLUDES);
			loadRefFilters(rulesNode, refExcludes, ruleType, JZRM_REF_EXCLUDES);
		}
	}
	
	public boolean rulesAllowed() {
		return this.allowed;
	}
	
	public Collection<?> filter(List<? extends MonitorRule> rules) {
		if (!this.allowed)
			return rules;
		
		List<MonitorRule> includeCandidates = new ArrayList<>();
		if (refIncludes.isEmpty())
			includeCandidates.addAll(rules);
		else
			includeCandidates = filterIncludes(rules);

		if (includeCandidates.isEmpty())
			return includeCandidates;

		return filterExcludes(includeCandidates);
	}
	
	private List<MonitorRule> filterExcludes(List<MonitorRule> rules) {
		List<MonitorRule> candidates = new ArrayList<>(rules);

		for (MonitorRule rule : rules) {
			boolean found = false;
			
			for (Pattern pattern : refExcludes) {
				found = pattern.matcher(rule.getRef()).find();
				if (found)
					break;
			}
			
			if (found)
				candidates.remove(rule);
		}
		
		return candidates;
	}

	private List<MonitorRule> filterIncludes(List<? extends MonitorRule> rules) {
		List<MonitorRule> candidates = new ArrayList<MonitorRule>();

		for (MonitorRule rule : rules) {
			boolean found = false;
			
			for (Pattern pattern : refIncludes) {
				found = pattern.matcher(rule.getRef()).find();
				if (found)
					break;
			}
			
			if (found)
				candidates.add(rule);
		}
		
		return candidates;
	}

	private void loadAllowed(Element rulesNode, String ruleType) {
		this.allowed = ConfigUtil.getFirstChildNode(rulesNode, JZRM_ALL_APP_RULES) != null;
		if (!this.allowed)
			this.allowed = ConfigUtil.getFirstChildNode(rulesNode, ruleType) != null;
	}
	
	private void loadRefFilters(Element rulesNode, List<Pattern> refFilters, String nodeName, String refFilterName) throws JzrInitializationException {
		Element node = ConfigUtil.getFirstChildNode(rulesNode, nodeName);
		if (node == null)
			return;
		
		String value = ConfigUtil.getAttributeValue(node, refFilterName);
		if (value.isEmpty())
			return;

		String[] parts = value.trim().split(",");
		for (int i=0; i<parts.length; i++){
			String part = parts[i].trim();
			Pattern pattern = null;
			try{
				pattern = Pattern.compile(part);
			}catch(PatternSyntaxException ex) {
				throw new JzrInitializationException("Invalid applicative rule filter on " + refFilterName + " on node " + nodeName + ". Invalid filter is : " + part);
			}
			refFilters.add(pattern);
		}
	}

	public String getId() {
		if(!this.allowed)
			return ""; // filter is transparent/ignored
		
		String id = this.ruleType;
		
		if (!refIncludes.isEmpty())
			id += refIncludes.toString();
		
		if (!refExcludes.isEmpty())
			id += refExcludes.toString();
		
		return id;
	}
}
