package org.jeyzer.monitor.impl.rule;

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

import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrMonitoringRulesInitializationException;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigMonitorRules;
import org.jeyzer.monitor.engine.rule.MonitorAnalyzerRule;
import org.jeyzer.monitor.engine.rule.MonitorRule;
import org.jeyzer.monitor.engine.rule.MonitorSessionRule;
import org.jeyzer.monitor.engine.rule.MonitorSystemRule;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.impl.rule.analyzer.RecordingSnapshotNotFoundRule;
import org.jeyzer.monitor.impl.rule.analyzer.ToolErrorRule;
import org.jeyzer.monitor.impl.rule.session.AbsentThreadsRule;
import org.jeyzer.monitor.impl.rule.session.ActiveNamedThreadLimitRule;
import org.jeyzer.monitor.impl.rule.session.ActiveThreadLimitRule;
import org.jeyzer.monitor.impl.rule.session.ContentionTypeParallelContentionRule;
import org.jeyzer.monitor.impl.rule.session.DeadlockRule;
import org.jeyzer.monitor.impl.rule.session.ExecutionPatternRule;
import org.jeyzer.monitor.impl.rule.session.FunctionAndOperationParallelContentionRule;
import org.jeyzer.monitor.impl.rule.session.FunctionParallelContentionRule;
import org.jeyzer.monitor.impl.rule.session.GlobalThreadLeakRule;
import org.jeyzer.monitor.impl.rule.session.GlobalThreadLimitRule;
import org.jeyzer.monitor.impl.rule.session.GlobalVirtualThreadLimitRule;
import org.jeyzer.monitor.impl.rule.session.HiatusTimeRule;
import org.jeyzer.monitor.impl.rule.session.LocksContentionRule;
import org.jeyzer.monitor.impl.rule.session.MissingThreadDumpRule;
import org.jeyzer.monitor.impl.rule.session.MultiDistinctFunctionContentionRule;
import org.jeyzer.monitor.impl.rule.session.NamedThreadLeakRule;
import org.jeyzer.monitor.impl.rule.session.NamedThreadLimitRule;
import org.jeyzer.monitor.impl.rule.session.OperationParallelContentionRule;
import org.jeyzer.monitor.impl.rule.session.SuspendedThreadsRule;
import org.jeyzer.monitor.impl.rule.session.advanced.CPUPercentProcessRule;
import org.jeyzer.monitor.impl.rule.session.advanced.CPUPercentSystemRule;
import org.jeyzer.monitor.impl.rule.session.advanced.CPURunnableVsCPUCapacityRule;
import org.jeyzer.monitor.impl.rule.session.advanced.DiskSpaceFreePercentRule;
import org.jeyzer.monitor.impl.rule.session.advanced.DiskSpaceFreeRule;
import org.jeyzer.monitor.impl.rule.session.advanced.DiskSpaceUsedPercentRule;
import org.jeyzer.monitor.impl.rule.session.advanced.DiskSpaceUsedRule;
import org.jeyzer.monitor.impl.rule.session.advanced.DiskWriteSpeedRule;
import org.jeyzer.monitor.impl.rule.session.advanced.DiskWriteTimeRule;
import org.jeyzer.monitor.impl.rule.session.advanced.DownTimeProcessRule;
import org.jeyzer.monitor.impl.rule.session.advanced.GarbageCollectionFailingToReleaseMemoryRule;
import org.jeyzer.monitor.impl.rule.session.advanced.GarbageCollectionTimeRule;
import org.jeyzer.monitor.impl.rule.session.advanced.GarbageOldCollectionCountRule;
import org.jeyzer.monitor.impl.rule.session.advanced.JeyzerMXContextParamNumberRule;
import org.jeyzer.monitor.impl.rule.session.advanced.JeyzerMXContextParamPatternRule;
import org.jeyzer.monitor.impl.rule.session.advanced.MXBeanParamNumberRule;
import org.jeyzer.monitor.impl.rule.session.advanced.MXBeanParamPatternRule;
import org.jeyzer.monitor.impl.rule.session.advanced.MemoryPercentProcessRule;
import org.jeyzer.monitor.impl.rule.session.advanced.MemoryPercentSystemRule;
import org.jeyzer.monitor.impl.rule.session.advanced.OpenFileDescriptorNumberRule;
import org.jeyzer.monitor.impl.rule.session.advanced.OpenFileDescriptorPercentRule;
import org.jeyzer.monitor.impl.rule.session.advanced.RecordingSnapshotCaptureTimeRule;
import org.jeyzer.monitor.impl.rule.session.advanced.RestartProcessRule;
import org.jeyzer.monitor.impl.rule.session.advanced.UpTimeProcessRule;
import org.jeyzer.monitor.impl.rule.session.advanced.VirtualThreadsCPUPercentRule;
import org.jeyzer.monitor.impl.rule.system.ContentionTypeGlobalPercentRule;
import org.jeyzer.monitor.impl.rule.system.ContentionTypeInPrincipalPercentRule;
import org.jeyzer.monitor.impl.rule.system.ContentionTypePresenceRule;
import org.jeyzer.monitor.impl.rule.system.DiskSpaceTotalRule;
import org.jeyzer.monitor.impl.rule.system.ExecutorPresenceRule;
import org.jeyzer.monitor.impl.rule.system.FunctionGlobalPercentRule;
import org.jeyzer.monitor.impl.rule.system.FunctionInPrincipalPercentRule;
import org.jeyzer.monitor.impl.rule.system.FunctionPresenceRule;
import org.jeyzer.monitor.impl.rule.system.GarbageCollectorNameRule;
import org.jeyzer.monitor.impl.rule.system.OperationGlobalPercentRule;
import org.jeyzer.monitor.impl.rule.system.OperationInPrincipalPercentRule;
import org.jeyzer.monitor.impl.rule.system.OperationPresenceRule;
import org.jeyzer.monitor.impl.rule.system.ProcessCardPropertyAbsenceRule;
import org.jeyzer.monitor.impl.rule.system.ProcessCardPropertyNumberRule;
import org.jeyzer.monitor.impl.rule.system.ProcessCardPropertyPatternRule;
import org.jeyzer.monitor.impl.rule.system.ProcessCommandLineMaxHeapMemoryRule;
import org.jeyzer.monitor.impl.rule.system.ProcessCommandLineParameterAbsenceRule;
import org.jeyzer.monitor.impl.rule.system.ProcessCommandLineParameterPatternRule;
import org.jeyzer.monitor.impl.rule.system.ProcessCommandLinePropertyNumberRule;
import org.jeyzer.monitor.impl.rule.system.ProcessCommandLinePropertyPatternRule;
import org.jeyzer.monitor.impl.rule.system.ProcessJarManifestVersionMismatchRule;
import org.jeyzer.monitor.impl.rule.system.ProcessJarVersionAbsenceRule;
import org.jeyzer.monitor.impl.rule.system.ProcessJarMultipleVersionsRule;
import org.jeyzer.monitor.impl.rule.system.ProcessJarNameAbsenceRule;
import org.jeyzer.monitor.impl.rule.system.ProcessJarNameRule;
import org.jeyzer.monitor.impl.rule.system.ProcessJarVersionRule;
import org.jeyzer.monitor.impl.rule.system.ProcessJarVersionSnapshotRule;
import org.jeyzer.monitor.impl.rule.system.ProcessModuleNameAbsenceRule;
import org.jeyzer.monitor.impl.rule.system.ProcessModuleNameRule;
import org.jeyzer.monitor.impl.rule.system.ProcessModuleVersionAbsenceRule;
import org.jeyzer.monitor.impl.rule.system.ProcessModuleVersionRule;
import org.jeyzer.monitor.impl.rule.system.ProcessModuleVersionSnapshotRule;
import org.jeyzer.monitor.impl.rule.system.QuietActivityRule;
import org.jeyzer.monitor.impl.rule.system.RecordingSizeRule;
import org.jeyzer.monitor.impl.rule.system.SharedProfileRule;
import org.jeyzer.monitor.impl.rule.system.StickerMatchRule;
import org.jeyzer.monitor.impl.rule.system.VirtualThreadPresenceRule;
import org.jeyzer.monitor.impl.rule.task.ContentionTypeTaskRule;
import org.jeyzer.monitor.impl.rule.task.ExecutionPatternTaskRule;
import org.jeyzer.monitor.impl.rule.task.FrozenStacksRule;
import org.jeyzer.monitor.impl.rule.task.FunctionTaskRule;
import org.jeyzer.monitor.impl.rule.task.LockerTaskRule;
import org.jeyzer.monitor.impl.rule.task.LongRunningTaskRule;
import org.jeyzer.monitor.impl.rule.task.OperationTaskRule;
import org.jeyzer.monitor.impl.rule.task.StackOverflowRule;
import org.jeyzer.monitor.impl.rule.task.advanced.CPUPercentTaskRule;
import org.jeyzer.monitor.impl.rule.task.advanced.JeyzerMXContextParamNumberTaskRule;
import org.jeyzer.monitor.impl.rule.task.advanced.JeyzerMXContextParamPatternTaskRule;
import org.jeyzer.monitor.impl.rule.task.advanced.MemoryPercentTaskRule;
import org.jeyzer.service.location.JzrLocationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorRuleBuilder {
	
	public static final Logger logger = LoggerFactory.getLogger(MonitorRuleBuilder.class);
	
	public enum RuleScope{
		TASK, SESSION, SYSTEM; 
	}
	
	// required for the dynamic rules
	private ConfigMonitorRules ruleDefs;
	
	private List<MonitorTaskRule> taskRules = new LinkedList<>();
	private List<MonitorSessionRule> sessionRules = new LinkedList<>();
	private List<MonitorSystemRule> systemRules = new LinkedList<>();
	private List<MonitorAnalyzerRule> analyzerRules = new LinkedList<>();
	
	// Monitoring constructor 
	public MonitorRuleBuilder(ConfigMonitorRules ruleDefs) throws JzrInitializationException {
		this.ruleDefs = ruleDefs;
		
		// Unlike the dynamic rules, we do not tolerate errors on rule loading
		initTaskRules(false);
		initSessionRules(false);
		initSystemRules(false);
		
		if (taskRules.isEmpty() && sessionRules.isEmpty() && systemRules.isEmpty()){
			throw new JzrInitializationException("No monitoring rules defined.");
		}
		
		buildAnalyzerRules();
	}
	
	// Analysis constructor
	public MonitorRuleBuilder(ConfigMonitorRules ruleDefs, RuleScope scope) throws JzrInitializationException {
		this.ruleDefs = ruleDefs;
		
		// Unlike the dynamic rules, we do not tolerate errors on rule loading
		switch (scope) {
			case TASK:
				initTaskRules(false);
				break;
			case SESSION:
				initSessionRules(false);
				break;
			case SYSTEM:
				initSystemRules(false);
				break;
		}
	}
	
	public void loadDynamicRules(JzrSession session, JzrLocationResolver resolver) {
		if (!ruleDefs.getDynamicRulesLoadingCfg().isDynamicLoadingActive())
			return;
		
		logger.info("Loading the dynamic monitoring rules");
		List<String> paths = resolver.resolveDynamicMonitorLocations(
				session.getProcessJars(),
				session.getProcessModules(),
				ruleDefs.getDynamicRulesLoadingCfg().isDeclaredRepositoryOnly()
				);
		ruleDefs.loadDynamicRules(paths, resolver);
		
		// add the dynamic rules to the existing sets of rules
		// rule loading errors are accepted
		try {
			initTaskRules(true);
		} catch (JzrMonitoringRulesInitializationException e) {
			logger.warn("Failed to load the following task dynamic monitoring rules : ");
			logger.warn(e.getMessage());
		}
		
		try {
			initSessionRules(true);
		} catch (JzrMonitoringRulesInitializationException e) {
			logger.warn("Failed to load the following session dynamic monitoring rules : ");
			logger.warn(e.getMessage());
		}
		
		try {
			initSystemRules(true);
		} catch (JzrMonitoringRulesInitializationException e) {
			logger.warn("Failed to load the following system dynamic monitoring rules : ");
			logger.warn(e.getMessage());
		}
	}

	public List<MonitorTaskRule> getTaskRules() {
		return taskRules;
	}

	public List<MonitorSessionRule> getSessionRules() {
		return sessionRules;
	}
	
	public List<MonitorSystemRule> getSystemRules() {
		return systemRules;
	}
	
	public List<MonitorAnalyzerRule> getAnalyzerRules() {
		return analyzerRules;
	}

	public List<MonitorRule> getAllRules() {
		List<MonitorRule> rules = new ArrayList<>();
		
		rules.addAll(analyzerRules);
		rules.addAll(systemRules);
		rules.addAll(sessionRules);
		rules.addAll(taskRules);
		
		return rules;
	}

	public void loadDynamicRules(JzrSession session, JzrLocationResolver resolver, RuleScope scope) {
		if (!ruleDefs.getDynamicRulesLoadingCfg().isDynamicLoadingActive())
			return;
		
		logger.info("Loading the dynamic monitoring rules");
		List<String> paths = resolver.resolveDynamicMonitorLocations(
				session.getProcessJars(),
				session.getProcessModules(),
				ruleDefs.getDynamicRulesLoadingCfg().isDeclaredRepositoryOnly()
				);
		ruleDefs.loadDynamicRules(paths, resolver);
		
		// add the dynamic rules to the existing sets of rules
		// rule loading errors are accepted
		switch (scope) {
		case TASK:
			try {
				initTaskRules(true);
			} catch (JzrMonitoringRulesInitializationException e) {
				logger.warn("Failed to load the following task dynamic monitoring rules : ");
				logger.warn(e.getMessage());
			}
			break;
		case SESSION:
			try {
				initSessionRules(true);
			} catch (JzrMonitoringRulesInitializationException e) {
				logger.warn("Failed to load the following session dynamic monitoring rules : ");
				logger.warn(e.getMessage());
			}
			break;
		case SYSTEM:
			try {
				initSystemRules(true);
			} catch (JzrMonitoringRulesInitializationException e) {
				logger.warn("Failed to load the following system dynamic monitoring rules : ");
				logger.warn(e.getMessage());
			}
			break;
	}
	}
	
	private void initSystemRules(boolean dynamic) throws JzrMonitoringRulesInitializationException {
		Map<String, Exception> errors = new HashMap<>();
		for (ConfigMonitorRule def : ruleDefs.getRules(dynamic)){
			String name = def.getName();
			MonitorSystemRule rule = null;
			try {
				if (ProcessCardPropertyPatternRule.RULE_NAME.equals(name))
					rule = new ProcessCardPropertyPatternRule(def);
				else if (ProcessCardPropertyNumberRule.RULE_NAME.equals(name))
					rule = new ProcessCardPropertyNumberRule(def);
				else if (ProcessCardPropertyAbsenceRule.RULE_NAME.equals(name))
					rule = new ProcessCardPropertyAbsenceRule(def);
				else if (ProcessCommandLinePropertyNumberRule.RULE_NAME.equals(name))
					rule = new ProcessCommandLinePropertyNumberRule(def);
				else if (ProcessCommandLinePropertyPatternRule.RULE_NAME.equals(name))
					rule = new ProcessCommandLinePropertyPatternRule(def);
				else if (ProcessCommandLineParameterPatternRule.RULE_NAME.equals(name))
					rule = new ProcessCommandLineParameterPatternRule(def);
				else if (ProcessCommandLineParameterAbsenceRule.RULE_NAME.equals(name))
					rule = new ProcessCommandLineParameterAbsenceRule(def);
				else if (ProcessCommandLineMaxHeapMemoryRule.RULE_NAME.equals(name))
					rule = new ProcessCommandLineMaxHeapMemoryRule(def);
				else if (ContentionTypeGlobalPercentRule.RULE_NAME.equals(name))
					rule = new ContentionTypeGlobalPercentRule(def);
				else if (OperationGlobalPercentRule.RULE_NAME.equals(name))
					rule = new OperationGlobalPercentRule(def);
				else if (FunctionGlobalPercentRule.RULE_NAME.equals(name))
					rule = new FunctionGlobalPercentRule(def);
				else if (ContentionTypeInPrincipalPercentRule.RULE_NAME.equals(name))
					rule = new ContentionTypeInPrincipalPercentRule(def);
				else if (OperationInPrincipalPercentRule.RULE_NAME.equals(name))
					rule = new OperationInPrincipalPercentRule(def);
				else if (FunctionInPrincipalPercentRule.RULE_NAME.equals(name))
					rule = new FunctionInPrincipalPercentRule(def);
				else if (FunctionPresenceRule.RULE_NAME.equals(name))
					rule = new FunctionPresenceRule(def);
				else if (ExecutorPresenceRule.RULE_NAME.equals(name))
					rule = new ExecutorPresenceRule(def);
				else if (OperationPresenceRule.RULE_NAME.equals(name))
					rule = new OperationPresenceRule(def);
				else if (ContentionTypePresenceRule.RULE_NAME.equals(name))
					rule = new ContentionTypePresenceRule(def);
				else if (DiskSpaceTotalRule.RULE_NAME.equals(name))
					rule = new DiskSpaceTotalRule(def);
				else if (StickerMatchRule.RULE_NAME.equals(name))
					rule = new StickerMatchRule(def);
				else if (ProcessJarVersionRule.RULE_NAME.equals(name))
					rule = new ProcessJarVersionRule(def);
				else if (ProcessJarVersionSnapshotRule.RULE_NAME.equals(name))
					rule = new ProcessJarVersionSnapshotRule(def);
				else if (ProcessJarVersionAbsenceRule.RULE_NAME.equals(name))
					rule = new ProcessJarVersionAbsenceRule(def);
				else if (ProcessJarMultipleVersionsRule.RULE_NAME.equals(name))
					rule = new ProcessJarMultipleVersionsRule(def);
				else if (ProcessJarNameRule.RULE_NAME.equals(name))
					rule = new ProcessJarNameRule(def);
				else if (ProcessJarNameAbsenceRule.RULE_NAME.equals(name))
					rule = new ProcessJarNameAbsenceRule(def);
				else if (ProcessJarManifestVersionMismatchRule.RULE_NAME.equals(name))
					rule = new ProcessJarManifestVersionMismatchRule(def);
				else if (ProcessModuleVersionRule.RULE_NAME.equals(name))
					rule = new ProcessModuleVersionRule(def);
				else if (ProcessModuleVersionSnapshotRule.RULE_NAME.equals(name))
					rule = new ProcessModuleVersionSnapshotRule(def);
				else if (ProcessModuleVersionAbsenceRule.RULE_NAME.equals(name))
					rule = new ProcessModuleVersionAbsenceRule(def);
				else if (ProcessModuleNameRule.RULE_NAME.equals(name))
					rule = new ProcessModuleNameRule(def);
				else if (ProcessModuleNameAbsenceRule.RULE_NAME.equals(name))
					rule = new ProcessModuleNameAbsenceRule(def);
				else if (RecordingSizeRule.RULE_NAME.equals(name))
					rule = new RecordingSizeRule(def);
				else if (GarbageCollectorNameRule.RULE_NAME.equals(name))
					rule = new GarbageCollectorNameRule(def);
				else if (QuietActivityRule.RULE_NAME.equals(name))
					rule = new QuietActivityRule(def);
				else if (SharedProfileRule.RULE_NAME.equals(name))
					rule = new SharedProfileRule(def);
				else if (VirtualThreadPresenceRule.RULE_NAME.equals(name))
					rule = new VirtualThreadPresenceRule(def);				
				
				if (rule != null)
					systemRules.add(rule);
			} catch (Exception ex) {
				errors.put(def.getRef(), ex);
			}
		}
		if (!errors.isEmpty())
			throw new JzrMonitoringRulesInitializationException(errors);
	}

	private void initSessionRules(boolean dynamic) throws JzrMonitoringRulesInitializationException {
		Map<String, Exception> errors = new HashMap<>();
		for (ConfigMonitorRule def : ruleDefs.getRules(dynamic)){
			MonitorSessionRule rule = null;
			String name = def.getName();
			try {
				if (GlobalThreadLimitRule.RULE_NAME.equals(name))
					rule = new GlobalThreadLimitRule(def);
				if (GlobalVirtualThreadLimitRule.RULE_NAME.equals(name))
					rule = new GlobalVirtualThreadLimitRule(def);
				else if (NamedThreadLimitRule.RULE_NAME.equals(name))
					rule = new NamedThreadLimitRule(def);
				else if (GlobalThreadLeakRule.RULE_NAME.equals(name))
					rule = new GlobalThreadLeakRule(def);
				else if (NamedThreadLeakRule.RULE_NAME.equals(name))
					rule = new NamedThreadLeakRule(def);
				else if (ActiveThreadLimitRule.RULE_NAME.equals(name))
					rule = new ActiveThreadLimitRule(def);
				else if (CPUPercentProcessRule.RULE_NAME.equals(name))
					rule = new CPUPercentProcessRule(def);
				else if (CPUPercentSystemRule.RULE_NAME.equals(name))
					rule = new CPUPercentSystemRule(def);
				else if (MemoryPercentProcessRule.RULE_NAME.equals(name))
					rule = new MemoryPercentProcessRule(def);
				else if (MemoryPercentSystemRule.RULE_NAME.equals(name))
					rule = new MemoryPercentSystemRule(def);
				else if (GarbageCollectionTimeRule.RULE_NAME.equals(name))
					rule = new GarbageCollectionTimeRule(def);
				else if (GarbageOldCollectionCountRule.RULE_NAME.equals(name))
					rule = new GarbageOldCollectionCountRule(def);
				else if (JeyzerMXContextParamNumberRule.RULE_NAME.equals(name))
					rule = new JeyzerMXContextParamNumberRule(def);
				else if (JeyzerMXContextParamPatternRule.RULE_NAME.equals(name))
					rule = new JeyzerMXContextParamPatternRule(def);
				else if (ExecutionPatternRule.RULE_NAME.equals(name))
					rule = new ExecutionPatternRule(def);
				else if (MissingThreadDumpRule.RULE_NAME.equals(name))
					rule = new MissingThreadDumpRule(def);
				else if (HiatusTimeRule.RULE_NAME.equals(name))
					rule = new HiatusTimeRule(def);
				else if (DeadlockRule.RULE_NAME.equals(name))
					rule = new DeadlockRule(def);
				else if (SuspendedThreadsRule.RULE_NAME.equals(name))
					rule = new SuspendedThreadsRule(def);
				else if (GarbageCollectionFailingToReleaseMemoryRule.RULE_NAME.equals(name))
					rule = new GarbageCollectionFailingToReleaseMemoryRule(def);
				else if (ActiveNamedThreadLimitRule.RULE_NAME.equals(name))
					rule = new ActiveNamedThreadLimitRule(def);
				else if (UpTimeProcessRule.RULE_NAME.equals(name))
					rule = new UpTimeProcessRule(def);
				else if (RestartProcessRule.RULE_NAME.equals(name))
					rule = new RestartProcessRule(def);
				else if (DownTimeProcessRule.RULE_NAME.equals(name))
					rule = new DownTimeProcessRule(def);
				else if (RecordingSnapshotCaptureTimeRule.RULE_NAME.equals(name))
					rule = new RecordingSnapshotCaptureTimeRule(def);
				else if (MXBeanParamNumberRule.RULE_NAME.equals(name))
					rule = new MXBeanParamNumberRule(def);
				else if (MXBeanParamPatternRule.RULE_NAME.equals(name))
					rule = new MXBeanParamPatternRule(def);
				else if (FunctionParallelContentionRule.RULE_NAME.equals(name))
					rule = new FunctionParallelContentionRule(def);
				else if (OperationParallelContentionRule.RULE_NAME.equals(name))
					rule = new OperationParallelContentionRule(def);
				else if (FunctionAndOperationParallelContentionRule.RULE_NAME.equals(name))
					rule = new FunctionAndOperationParallelContentionRule(def);
				else if (ContentionTypeParallelContentionRule.RULE_NAME.equals(name))
					rule = new ContentionTypeParallelContentionRule(def);
				else if (LocksContentionRule.RULE_NAME.equals(name))
					rule = new LocksContentionRule(def);
				else if (AbsentThreadsRule.RULE_NAME.equals(name))
					rule = new AbsentThreadsRule(def);
				else if (CPURunnableVsCPUCapacityRule.RULE_NAME.equals(name))
					rule = new CPURunnableVsCPUCapacityRule(def);
				else if (MultiDistinctFunctionContentionRule.RULE_NAME.equals(name))
					rule = new MultiDistinctFunctionContentionRule(def);
				else if (DiskSpaceFreePercentRule.RULE_NAME.equals(name))
					rule = new DiskSpaceFreePercentRule(def);
				else if (DiskSpaceFreeRule.RULE_NAME.equals(name))
					rule = new DiskSpaceFreeRule(def);
				else if (DiskSpaceUsedPercentRule.RULE_NAME.equals(name))
					rule = new DiskSpaceUsedPercentRule(def);
				else if (DiskSpaceUsedRule.RULE_NAME.equals(name))
					rule = new DiskSpaceUsedRule(def);
				else if (DiskWriteSpeedRule.RULE_NAME.equals(name))
					rule = new DiskWriteSpeedRule(def);
				else if (DiskWriteTimeRule.RULE_NAME.equals(name))
					rule = new DiskWriteTimeRule(def);
				else if (OpenFileDescriptorNumberRule.RULE_NAME.equals(name))
					rule = new OpenFileDescriptorNumberRule(def);
				else if (OpenFileDescriptorPercentRule.RULE_NAME.equals(name))
					rule = new OpenFileDescriptorPercentRule(def);
				else if (VirtualThreadsCPUPercentRule.RULE_NAME.equals(name))
					rule = new VirtualThreadsCPUPercentRule(def);
				
				if (rule != null)
					sessionRules.add(rule);
			} catch (Exception ex) {
				errors.put(def.getRef(), ex);
			}
		}
		if (!errors.isEmpty())
			throw new JzrMonitoringRulesInitializationException(errors);
	}

	private void initTaskRules(boolean dynamic) throws JzrMonitoringRulesInitializationException {
		Map<String, Exception> errors = new HashMap<>();
		for (ConfigMonitorRule def : ruleDefs.getRules(dynamic)){
			String name = def.getName();
			MonitorTaskRule rule = null;
			try {
				if (FrozenStacksRule.RULE_NAME.equals(name))
					rule = new FrozenStacksRule(def);
				else if (ExecutionPatternTaskRule.RULE_NAME.equals(name))
					rule = new ExecutionPatternTaskRule(def);
				else if (LockerTaskRule.RULE_NAME.equals(name))
					rule = new LockerTaskRule(def);
				else if (LongRunningTaskRule.RULE_NAME.equals(name))
					rule = new LongRunningTaskRule(def);
				else if (StackOverflowRule.RULE_NAME.equals(name))
					rule = new StackOverflowRule(def);
				else if (JeyzerMXContextParamNumberTaskRule.RULE_NAME.equals(name))
					rule = new JeyzerMXContextParamNumberTaskRule(def);
				else if (JeyzerMXContextParamPatternTaskRule.RULE_NAME.equals(name))
					rule = new JeyzerMXContextParamPatternTaskRule(def);
				else if (CPUPercentTaskRule.RULE_NAME.equals(name))
					rule = new CPUPercentTaskRule(def);
				else if (MemoryPercentTaskRule.RULE_NAME.equals(name))
					rule = new MemoryPercentTaskRule(def);
				else if (ContentionTypeTaskRule.RULE_NAME.equals(name))
					rule = new ContentionTypeTaskRule(def);
				else if (OperationTaskRule.RULE_NAME.equals(name))
					rule = new OperationTaskRule(def);
				else if (FunctionTaskRule.RULE_NAME.equals(name))
					rule = new FunctionTaskRule(def);
				
				if (rule != null)
					taskRules.add(rule);
			} catch (Exception ex) {
				errors.put(def.getRef(), ex);
			}
		}
		if (!errors.isEmpty())
			throw new JzrMonitoringRulesInitializationException(errors);
	}

	private void buildAnalyzerRules() throws JzrInitializationException {
		analyzerRules.add(new ToolErrorRule());
		analyzerRules.add(new RecordingSnapshotNotFoundRule());
	}
}
