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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.config.ConfigDynamicLoading;
import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.data.location.JzrResourceLocation;
import org.jeyzer.analyzer.data.location.MultipleJzrResourceLocation;
import org.jeyzer.analyzer.data.location.SingleJzrResourceLocation;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.monitor.config.applicative.ConfigMonitorApplicativeRuleManager;
import org.jeyzer.monitor.impl.rule.session.advanced.DiskSpaceFreePercentRule;
import org.jeyzer.monitor.impl.rule.session.advanced.DiskSpaceFreeRule;
import org.jeyzer.monitor.impl.rule.session.advanced.DiskSpaceUsedPercentRule;
import org.jeyzer.monitor.impl.rule.session.advanced.DiskSpaceUsedRule;
import org.jeyzer.monitor.impl.rule.session.advanced.DiskWriteSpeedRule;
import org.jeyzer.monitor.impl.rule.session.advanced.DiskWriteTimeRule;
import org.jeyzer.monitor.impl.rule.session.advanced.JeyzerMXContextParamNumberRule;
import org.jeyzer.monitor.impl.rule.session.advanced.JeyzerMXContextParamPatternRule;
import org.jeyzer.monitor.impl.rule.session.advanced.MXBeanParamNumberRule;
import org.jeyzer.monitor.impl.rule.session.advanced.MXBeanParamPatternRule;
import org.jeyzer.monitor.impl.rule.system.ContentionTypeGlobalPercentRule;
import org.jeyzer.monitor.impl.rule.system.ContentionTypeInPrincipalPercentRule;
import org.jeyzer.monitor.impl.rule.system.ContentionTypePresenceRule;
import org.jeyzer.monitor.impl.rule.system.DiskSpaceTotalRule;
import org.jeyzer.monitor.impl.rule.system.FunctionGlobalPercentRule;
import org.jeyzer.monitor.impl.rule.system.FunctionInPrincipalPercentRule;
import org.jeyzer.monitor.impl.rule.system.FunctionPresenceRule;
import org.jeyzer.monitor.impl.rule.system.OperationGlobalPercentRule;
import org.jeyzer.monitor.impl.rule.system.OperationInPrincipalPercentRule;
import org.jeyzer.monitor.impl.rule.system.OperationPresenceRule;
import org.jeyzer.monitor.impl.rule.system.ProcessCardPropertyAbsenceRule;
import org.jeyzer.monitor.impl.rule.system.ProcessCardPropertyNumberRule;
import org.jeyzer.monitor.impl.rule.system.ProcessCardPropertyPatternRule;
import org.jeyzer.monitor.impl.rule.system.ProcessCommandLineParameterAbsenceRule;
import org.jeyzer.monitor.impl.rule.system.ProcessCommandLineParameterPatternRule;
import org.jeyzer.monitor.impl.rule.system.ProcessCommandLinePropertyNumberRule;
import org.jeyzer.monitor.impl.rule.system.ProcessCommandLinePropertyPatternRule;
import org.jeyzer.monitor.impl.rule.system.QuietActivityRule;
import org.jeyzer.monitor.impl.rule.system.RecordingSizeRule;
import org.jeyzer.monitor.impl.rule.system.SharedProfileRule;
import org.jeyzer.monitor.impl.rule.task.advanced.JeyzerMXContextParamNumberTaskRule;
import org.jeyzer.monitor.impl.rule.task.advanced.JeyzerMXContextParamPatternTaskRule;
import org.jeyzer.service.location.JzrLocationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfigMonitorRules {

	private static final Logger logger = LoggerFactory.getLogger(ConfigMonitorRules.class);
	
	public static final String JZRM_RULES = "rules";
	private static final String JZRM_GROUP_NAME = "group_name";
	private static final String JZRM_RULE_SET = "rule_set";
	private static final String JZRM_FILE = "file";
	private static final String JZRM_RULE_SETS = "rule_sets";
	private static final String JZRM_FILES = "files";
	private static final String JZRM_DYNAMIC_RULE_SETS = "dynamic_rule_sets";
	
	private static final String JZRM_JEYZER_PUB_RULES = "jeyzer_publisher_rules";
	
	private static final String DEFAULT_GROUP_NAME = "None";
	
	private final List<ConfigMonitorRule> rules = new LinkedList<>();
	private String id = ""; // set of rule group names
	
	private ConfigMonitorApplicativeRuleManager applicativeRulesManager;
	private boolean publisherRulesAllowed;
	
	private ConfigDynamicLoading dynamicRulesLoadingCfg;
	private List<String> stickerRefs; // initial stickers that will be applied on the dynamic rules

	private Map<String, ConfigMonitorRule> rulesRefs = new HashMap<>();
	
	public ConfigMonitorRules(Element rulesNode, JzrLocationResolver pathResolver) throws JzrInitializationException{
		String group = loadRuleGroupName(rulesNode);
		this.dynamicRulesLoadingCfg = new ConfigDynamicLoading(ConfigUtil.getFirstChildNode(rulesNode, JZRM_DYNAMIC_RULE_SETS));
		this.stickerRefs = ConfigMonitorRule.loadStickerRefs(rulesNode);
		loadRules(rulesNode, group, stickerRefs, pathResolver, false);
		prepareApplicativeAndPublisherRules(rulesNode);
	}
	
	public List<ConfigMonitorRule> getRules(boolean dynamic) {
		List<ConfigMonitorRule> filteredRules = new ArrayList<ConfigMonitorRule>();
		for (ConfigMonitorRule rule : this.rules) {
			if (dynamic) {
				if (rule.isDynamic())
					filteredRules.add(rule);
			}
			else {
				if (!rule.isDynamic())
					filteredRules.add(rule);
			}
		}
		return filteredRules;
	}
	
	public ConfigDynamicLoading getDynamicRulesLoadingCfg() {
		return dynamicRulesLoadingCfg;
	}
	
	public String getId(){
		return this.id;
	}
	
	public ConfigMonitorApplicativeRuleManager getApplicativeRuleManager() {
		return applicativeRulesManager;
	}
	
	public boolean isPublisherRulesAllowed() {
		return publisherRulesAllowed;
	}

	public void loadDynamicRules(List<String> paths, JzrLocationResolver jzrLocationResolver) {
		if (!dynamicRulesLoadingCfg.isDynamicLoadingActive())
			return;
		
		List<String> stickerLoadedRefs = new ArrayList<>(this.stickerRefs); // initialize it with the parent stickers
		for (String path : paths){
			// load the rules configuration file
			Element monitorRulesNode;
			try {
				monitorRulesNode = loadRulesConfigurationFile(path);
				String group = loadRuleGroupName(monitorRulesNode);
				stickerLoadedRefs.addAll(ConfigMonitorRule.loadStickerRefs(monitorRulesNode));
				loadRules(monitorRulesNode, group, stickerRefs, jzrLocationResolver, true); // from here, it can be recursive
			} catch (JzrInitializationException e) {
				logger.warn("Failed to load dynamically the monitoring rules from file : " + (SystemHelper.isRemoteProtocol(path) ? path :  SystemHelper.sanitizePathSeparators(path)));
				continue;
			}
		}
	}

	private void prepareApplicativeAndPublisherRules(Element rulesNode) throws JzrInitializationException {
		this.applicativeRulesManager= new ConfigMonitorApplicativeRuleManager(rulesNode);
		this.id += applicativeRulesManager.getId();
		
		this.publisherRulesAllowed = ConfigUtil.getFirstChildNode(rulesNode, JZRM_JEYZER_PUB_RULES) != null;
		if (this.publisherRulesAllowed)
			this.id += "publisher-rules";
	}

	private String loadRuleGroupName(Element rulesNode) {
		String group = ConfigUtil.getAttributeValue(rulesNode, JZRM_GROUP_NAME);
		if (group == null || group.isEmpty())
			group = DEFAULT_GROUP_NAME;
		id += group;
		return group;
	}

	private void loadRules(Element rulesNode, String group, List<String> stickerRefs, JzrLocationResolver pathResolver, boolean dynamic) throws JzrInitializationException {
		NodeList rulesNodes = rulesNode.getElementsByTagName(ConfigMonitorRule.JZRM_RULE);


		for (int i=0; i<rulesNodes.getLength(); i++){
			Element ruleNode = (Element)rulesNodes.item(i);
			ConfigMonitorRule ruleCfg;
						
			String name = ConfigUtil.getAttributeValue(ruleNode,ConfigMonitorRule.JZRM_NAME);
			if (JeyzerMXContextParamNumberTaskRule.RULE_NAME.equals(name)
					|| JeyzerMXContextParamPatternTaskRule.RULE_NAME.equals(name)
					|| JeyzerMXContextParamNumberRule.RULE_NAME.equals(name)
					|| JeyzerMXContextParamPatternRule.RULE_NAME.equals(name)
					|| MXBeanParamNumberRule.RULE_NAME.equals(name)
					|| MXBeanParamPatternRule.RULE_NAME.equals(name)
					|| ProcessCardPropertyPatternRule.RULE_NAME.equals(name)
					|| ProcessCardPropertyNumberRule.RULE_NAME.equals(name)
					|| ProcessCardPropertyAbsenceRule.RULE_NAME.equals(name)
					|| ContentionTypeGlobalPercentRule.RULE_NAME.equals(name)
					|| OperationGlobalPercentRule.RULE_NAME.equals(name)
					|| FunctionGlobalPercentRule.RULE_NAME.equals(name)
					|| OperationPresenceRule.RULE_NAME.equals(name)
					|| ContentionTypePresenceRule.RULE_NAME.equals(name)
					|| FunctionPresenceRule.RULE_NAME.equals(name)
					|| ProcessCommandLinePropertyNumberRule.RULE_NAME.equals(name)
					|| ProcessCommandLinePropertyPatternRule.RULE_NAME.equals(name)
					|| ProcessCommandLineParameterPatternRule.RULE_NAME.equals(name)
					|| ProcessCommandLineParameterAbsenceRule.RULE_NAME.equals(name)
					|| DiskSpaceUsedPercentRule.RULE_NAME.equals(name)
					|| DiskSpaceUsedRule.RULE_NAME.equals(name)
					|| DiskSpaceFreeRule.RULE_NAME.equals(name)
					|| DiskSpaceFreePercentRule.RULE_NAME.equals(name)
					|| DiskSpaceTotalRule.RULE_NAME.equals(name)
					|| DiskWriteSpeedRule.RULE_NAME.equals(name)
					|| DiskWriteTimeRule.RULE_NAME.equals(name)
					|| RecordingSizeRule.RULE_NAME.equals(name)
					|| QuietActivityRule.RULE_NAME.equals(name)
					|| SharedProfileRule.RULE_NAME.equals(name)){
				ruleCfg = new ConfigParamMonitorRule(ruleNode, group, stickerRefs, dynamic);
			}else if (OperationInPrincipalPercentRule.RULE_NAME.equals(name)
					|| FunctionInPrincipalPercentRule.RULE_NAME.equals(name)
					|| ContentionTypeInPrincipalPercentRule.RULE_NAME.equals(name)){
				ruleCfg = new ConfigPrincipalMonitorRule(ruleNode, group, stickerRefs, dynamic);
			}else{
				ruleCfg = new ConfigMonitorRule(ruleNode, group, stickerRefs, dynamic);
			}
			
			if (dynamic && this.rulesRefs.containsKey(ruleCfg.getRef()))
				continue; // do not reload the same rule in dynamic mode on each monitoring iteration

			// rule ref must be unique
			validateRuleRef(ruleCfg);
			
			rules.add(ruleCfg);
		}
		
		loadRuleSets(rulesNode, pathResolver, stickerRefs, dynamic);
	}

	private void validateRuleRef(ConfigMonitorRule ruleCfg) throws JzrInitializationException {
		if (this.rulesRefs.containsKey(ruleCfg.getRef())){
			ConfigMonitorRule otherCfg = rulesRefs.get(ruleCfg.getRef());
			throw new JzrInitializationException("Failed to load the monitoring rules : rule ref must be unique." 
				+ " Current rule : " + ruleCfg.getGroup() + "/" + ruleCfg.getName()
				+ " Other rule : " + otherCfg.getGroup() + "/" + otherCfg.getName()
				+ " Duplicate ref : " + ruleCfg.getRef());
		}
		rulesRefs.put(ruleCfg.getRef(), ruleCfg);
	}

	private void loadRuleSets(Element rulesNode, JzrLocationResolver pathResolver, List<String> stickerRefs, boolean dynamic) throws JzrInitializationException {
		// resolve profile paths
		List<String> paths = null;
		try{
			List<JzrResourceLocation> ruleSetLocations = loadRuleSetLocations(rulesNode);
			if (ruleSetLocations.isEmpty())
				return; // no rules set
			paths = pathResolver.resolveMonitorLocations(ruleSetLocations);
		}
		catch(Exception ex){
			throw new JzrInitializationException("Monitoring rules configuration loading failed. Failed to resolve the monitoring rule locations.", ex);
		}
		
		for (String path : paths){
			// load rules configuration file
			Element monitorRulesNode = loadRulesConfigurationFile(path);
			String group = loadRuleGroupName(monitorRulesNode);
			stickerRefs.addAll(ConfigMonitorRule.loadStickerRefs(monitorRulesNode));
			loadRules(monitorRulesNode, group, stickerRefs, pathResolver, dynamic); // from here, it can be recursive
		}
	}

	private List<JzrResourceLocation> loadRuleSetLocations(Element rulesNode) {
		List<JzrResourceLocation> ruleSetLocations = new ArrayList<>(3);
		NodeList nodeList = rulesNode.getChildNodes();
		
		for(int j=0; j<nodeList.getLength(); j++){
			Node node = nodeList.item(j);
			if (node.getNodeType() == Node.ELEMENT_NODE 
					&& JZRM_RULE_SET.equals(((Element)node).getTagName())){
				Element ruleSetNode = (Element)node;
				String location = ConfigUtil.getAttributeValue(ruleSetNode,JZRM_FILE);
				if (location != null && !location.isEmpty())
					ruleSetLocations.add(new SingleJzrResourceLocation(location));
			}
			else if (node.getNodeType() == Node.ELEMENT_NODE 
					&& JZRM_RULE_SETS.equals(((Element)node).getTagName())){
				Element ruleSetsNode = (Element)node;
				String location = ConfigUtil.getAttributeValue(ruleSetsNode,JZRM_FILES);
				if (location != null && !location.isEmpty())
					ruleSetLocations.add(new MultipleJzrResourceLocation(location));
			}
		}

		return ruleSetLocations;
	}

	private Element loadRulesConfigurationFile(String path) throws JzrInitializationException {
		Document doc;
		
		logger.info("Loading monitoring rules from file : " + (SystemHelper.isRemoteProtocol(path) ? path :  SystemHelper.sanitizePathSeparators(path)));
		
		try {
			doc = ConfigUtil.loadXMLFile(path);
			if (doc == null){
				logger.error("Failed to open the monitoring rules configuration resource using path : " + path);
				throw new JzrInitializationException("Failed to open the monitoring rules configuration resource using path : " + path);
			}
		} catch (Exception e) {
			logger.error("Failed to open the monitoring rules configuration resource using path : " + path, e);
			throw new JzrInitializationException("Failed to open the monitoring rules configuration resource using path : " + path, e);
		}
		
		NodeList ruleNodes = doc.getElementsByTagName(JZRM_RULES);
		if (ruleNodes == null){
			logger.error("Monitoring rules configuration " + path + " is invalid.");
			throw new JzrInitializationException("Monitoring rules configuration " + path + " is invalid.");
		}
		return (Element)ruleNodes.item(0);
	}
}
