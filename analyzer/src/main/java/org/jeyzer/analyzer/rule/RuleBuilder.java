package org.jeyzer.analyzer.rule;

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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.config.patterns.ConfigPatterns;
import org.jeyzer.analyzer.rule.pattern.Pattern;
import org.jeyzer.analyzer.rule.stack.DiscoveryFunctionRule;
import org.jeyzer.analyzer.rule.stack.DiscoveryOperationRule;
import org.jeyzer.analyzer.rule.stack.ExcludeRule;
import org.jeyzer.analyzer.rule.stack.ExcludeThreadNameRule;
import org.jeyzer.analyzer.rule.stack.ExecutorRule;
import org.jeyzer.analyzer.rule.stack.ExecutorThreadNameRule;
import org.jeyzer.analyzer.rule.stack.FunctionRule;
import org.jeyzer.analyzer.rule.stack.LockerRule;
import org.jeyzer.analyzer.rule.stack.OperationRule;
import org.jeyzer.analyzer.rule.stack.SizeRule;
import org.jeyzer.analyzer.session.JzrSession;


public class RuleBuilder {

	private static final RuleBuilder builder = new RuleBuilder();
	
	private RuleBuilder(){
	}
	
	public static RuleBuilder newInstance(){
		return builder;
	}
	
	public List<Rule> buildRules(AnalysisPreFilter filter, ConfigPatterns conf, JzrSession session){
		List<Rule> rules = new ArrayList<>();
		
		// build exclude rules
		rules = buildExcludeRules(conf, rules);

		// build exclude thread name rules
		rules = buildExcludeThreadNameRules(conf, rules);

		// build locker rules
		rules = buildLockerRules(conf, rules);
		
		// build function rules
		rules = buildFunctionRules(conf, rules);

		// build discovery function rules
		if (session.isDiscoveryModeEnabled())
			rules = buildDiscoveryFunctionRules(conf, rules);
		
		// build operation rules
		rules = buildOperationRules(conf, rules);
		
		// build discovery operation rules
		if (session.isDiscoveryModeEnabled())
			rules = buildDiscoveryOperationRules(conf, rules);
		
		// build executor rules
		rules = buildExecutorRules(conf, rules);

		// build executor thread name rules
		rules = buildExecutorThreadNameRules(conf, rules);
		
		// build size rules
		rules.add(new SizeRule(filter));
		
		// sort by priority
		Collections.sort(rules, new Rule.RuleComparable() );
		
		return rules;
	}	

	private List<Rule> buildDiscoveryFunctionRules(ConfigPatterns conf, List<Rule> rules) {
		@SuppressWarnings("unchecked")
		Map<String, Pattern> discoveryFunctions = (Map<String, Pattern>)conf.getValue(ConfigPatterns.JZRA_DISCOVERY_FUNCTION);
		
		for (String key : discoveryFunctions.keySet()){
			Rule rule = new DiscoveryFunctionRule(discoveryFunctions.get(key));
			rules.add(rule);
		}
		
		return rules;
	}


	private List<Rule> buildDiscoveryOperationRules(ConfigPatterns conf, List<Rule> rules) {
		@SuppressWarnings("unchecked")
		Map<String, Pattern> discoveryOperations = (Map<String, Pattern>)conf.getValue(ConfigPatterns.JZRA_DISCOVERY_OPERATION);
		
		for (String key : discoveryOperations.keySet()){
			Rule rule = new DiscoveryOperationRule(discoveryOperations.get(key));
			rules.add(rule);
		}
		
		return rules;
	}	
	
	private List<Rule> buildExcludeThreadNameRules(ConfigPatterns conf,
			List<Rule> rules) {
		@SuppressWarnings("unchecked")
		Map<String, Pattern> excludes = (Map<String, Pattern>)conf.getValue(ConfigPatterns.JZRA_EXCLUDE_THREAD_NAME);
		
		for (String key : excludes.keySet()){
			Rule rule = new ExcludeThreadNameRule(excludes.get(key));
			rules.add(rule);
		}
		
		return rules;
	}

	private List<Rule> buildFunctionRules(ConfigPatterns conf, List<Rule> rules){
		
		@SuppressWarnings("unchecked")
		Map<String, Pattern> functions = (Map<String, Pattern>)conf.getValue(ConfigPatterns.JZRA_FUNCTION);
		
		for (String key : functions.keySet()){
			Rule rule = new FunctionRule(functions.get(key));
			rules.add(rule);
		}
		
		return rules;
	}		
	
	private List<Rule> buildExecutorRules(ConfigPatterns conf, List<Rule> rules){
		
		@SuppressWarnings("unchecked")
		Map<String, Pattern> executors = (Map<String, Pattern>)conf.getValue(ConfigPatterns.JZRA_EXECUTOR);
		
		for (String key : executors.keySet()){
			Rule rule = new ExecutorRule(executors.get(key));
			rules.add(rule);
		}
		
		return rules;
	}			


	private List<Rule> buildExecutorThreadNameRules(ConfigPatterns conf, List<Rule> rules) {
		
		@SuppressWarnings("unchecked")
		Map<String, Pattern> executors = (Map<String, Pattern>)conf.getValue(ConfigPatterns.JZRA_EXECUTOR_THREAD_NAME);
		
		for (String key : executors.keySet()){
			Rule rule = new ExecutorThreadNameRule(executors.get(key));
			rules.add(rule);
		}
		
		return rules;
	}	
	
	private List<Rule> buildExcludeRules(ConfigPatterns conf, List<Rule> rules){
		
		@SuppressWarnings("unchecked")
		Map<String, Pattern> excludes = (Map<String, Pattern>)conf.getValue(ConfigPatterns.JZRA_EXCLUDE);
		
		for (String key : excludes.keySet()){
			Rule rule = new ExcludeRule(excludes.get(key));
			rules.add(rule);
		}
		
		return rules;
	}
	
	private List<Rule> buildOperationRules(ConfigPatterns conf, List<Rule> rules){
		
		@SuppressWarnings("unchecked")
		Map<String, Pattern> operations = (Map<String, Pattern>)conf.getValue(ConfigPatterns.JZRA_OPERATION);
		
		for (String key : operations.keySet()){
			Rule rule = new OperationRule(operations.get(key));
			rules.add(rule);
		}
		
		return rules;
	}

	
	private List<Rule> buildLockerRules(ConfigPatterns conf, List<Rule> rules){
		
		@SuppressWarnings("unchecked")
		Map<String, Pattern> lockers = (Map<String, Pattern>)conf.getValue(ConfigPatterns.JZRA_LOCKER);
		
		for (String key : lockers.keySet()){
			Rule rule = new LockerRule(lockers.get(key));
			rules.add(rule);
		}
		
		return rules;
	}
}
