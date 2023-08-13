package org.jeyzer.analyzer.output.poi.rule.header;

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

import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeader;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeaders;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeadersSets;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.session.JzrSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class HeaderBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(HeaderBuilder.class);
	
	private static final HeaderBuilder builder = new HeaderBuilder();
	
	private HeaderBuilder(){
	}
	
	public static HeaderBuilder newInstance(){
		return builder;
	}

	public List<Header> buildHeaders(ConfigSheetHeadersSets headerConfigSets, SheetDisplayContext displayContext, JzrSession session) {
		List<Header> rules = new ArrayList<>();
		Header rule;

		ConfigSheetHeaders headerConfigs= headerConfigSets.getHeaderConfigs(session.getFormatShortName());
		
		if (headerConfigs == null)
			return rules; // no header rules for that format
		
	    for (ConfigSheetHeader headerCfg : headerConfigs.getHeaderConfigs()){
	    	rule = null;

	    	if (RecordingSnapshotCaptureTimeRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new RecordingSnapshotCaptureTimeRule(headerCfg, displayContext);
	    	
	    	else if (ThreadCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ThreadCounterRule(headerCfg, displayContext);

	    	else if (NativeThreadCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new NativeThreadCounterRule(headerCfg, displayContext);
	    	
	    	else if ((session.areVirtualThreadVariationCountersAvailable() || session.hasVirtualThreads()) && VirtualThreadCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new VirtualThreadCounterRule(headerCfg, displayContext);
	    	
	    	else if (PoolThreadCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new PoolThreadCounterRule(headerCfg, displayContext);

	    	else if (PoolThreadActionCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new PoolThreadActionCounterRule(headerCfg, displayContext);
	    	
	    	else if (PoolThreadActionPercentRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new PoolThreadActionPercentRule(headerCfg, displayContext);
	    	
	    	else if (ProcessCPURule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ProcessCPURule(headerCfg, displayContext);

	    	else if (SystemCPURule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new SystemCPURule(headerCfg, displayContext);
	    	
	    	else if (ComputedProcessCPURule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ComputedProcessCPURule(headerCfg, displayContext);
	    	
	    	else if (ApplicativeCpuActivityRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ApplicativeCpuActivityRule(headerCfg, displayContext);

	    	else if (UsedPhysicalSystemMemoryRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new UsedPhysicalSystemMemoryRule(headerCfg, displayContext);
	    	
	    	else if (UsedPhysicalSystemMemoryPercentRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new UsedPhysicalSystemMemoryPercentRule(headerCfg, displayContext);
	    	
	    	else if (ApplicativeMemoryActivityRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ApplicativeMemoryActivityRule(headerCfg, displayContext);
	    	
	    	else if (ComputedProcessMemoryRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ComputedProcessMemoryRule(headerCfg, displayContext);

	    	else if (MemoryPoolHeapRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new MemoryPoolHeapRule(headerCfg, displayContext);	    	

	    	else if (MemoryPoolHeapPercentRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new MemoryPoolHeapPercentRule(headerCfg, displayContext);	    	
	    	
	    	else if (MemoryPoolGenericRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new MemoryPoolGenericRule(headerCfg, displayContext);

	    	else if (MemoryPoolGenericPercentRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new MemoryPoolGenericPercentRule(headerCfg, displayContext);

	    	else if (RecordingSnapshotFileNameRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new RecordingSnapshotFileNameRule(headerCfg, displayContext);
	    	
	    	else if (ProcessUpTimeRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ProcessUpTimeRule(headerCfg, displayContext);
	    	
	    	else if (GarbageCollectorCollectionCountRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GarbageCollectorCollectionCountRule(headerCfg, displayContext);
	    	
	    	else if (GarbageCollectorCollectionTimeRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GarbageCollectorCollectionTimeRule(headerCfg, displayContext);

	    	else if (GarbageCollectorCollectionTimePercentRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GarbageCollectorCollectionTimePercentRule(headerCfg, displayContext);

	    	else if (GarbageCollectorHealthIndicationRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GarbageCollectorHealthIndicationRule(headerCfg, displayContext);
	    	
	    	else if (GarbageCollectorMemoryReleasedPercentRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GarbageCollectorMemoryReleasedPercentRule(headerCfg, displayContext);
	    	
	    	else if (GarbageCollectorMemoryReleasedRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GarbageCollectorMemoryReleasedRule(headerCfg, displayContext);
	    	
	    	else if (GarbageCollectorMemoryUsedAfterGCPercentRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GarbageCollectorMemoryUsedAfterGCPercentRule(headerCfg, displayContext);
	    	
	    	else if (GarbageCollectorMemoryUsedAfterGCRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GarbageCollectorMemoryUsedAfterGCRule(headerCfg, displayContext);
	    	
	    	else if (GarbageCollectorNameRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GarbageCollectorNameRule(headerCfg, displayContext);
	    	
	    	else if (SectionDelimiterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName())) {
	    		// special case for virtual threads..
	    		String title = (String)headerCfg.getValue(SectionDelimiterRule.TITLE_FIELD);
	    		if (title.equalsIgnoreCase("Virtual threads") && session.hasVirtualThreadPresence())
	    			rule = new SectionDelimiterRule(headerCfg, displayContext);
	    		else
	    			rule = new SectionDelimiterRule(headerCfg, displayContext);
	    	}

	    	else if (GarbageCollectorMemoryPoolRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GarbageCollectorMemoryPoolRule(headerCfg, displayContext);	    	

	    	else if (GarbageCollectorMemoryUsedBeforeGCRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GarbageCollectorMemoryUsedBeforeGCRule(headerCfg, displayContext);
	    	
	    	else if (GarbageCollectorMemoryUsedBeforeGCPercentRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GarbageCollectorMemoryUsedBeforeGCPercentRule(headerCfg, displayContext);

	    	else if (GarbageCollectorMemoryMaxRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new GarbageCollectorMemoryMaxRule(headerCfg, displayContext);
	    	
	    	else if (DeadlockCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new DeadlockCounterRule(headerCfg, displayContext);

	    	else if (LockedThreadCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new LockedThreadCounterRule(headerCfg, displayContext);

	    	else if (SuspendedCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new SuspendedCounterRule(headerCfg, displayContext);
	    	
	    	else if (ActionCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ActionCounterRule(headerCfg, displayContext);
	    	
	    	else if (BiasedLockCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new BiasedLockCounterRule(headerCfg, displayContext);
	    	
	    	else if (ObjectPendingFinalizationCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new ObjectPendingFinalizationCounterRule(headerCfg, displayContext);
	    	
	    	else if (FrozenCodeStateThreadCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new FrozenCodeStateThreadCounterRule(headerCfg, displayContext);

	    	else if (JeyzerMXContextParamStringRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new JeyzerMXContextParamStringRule(headerCfg, displayContext);
	    	
	    	else if (JeyzerMXContextParamNumberRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new JeyzerMXContextParamNumberRule(headerCfg, displayContext);

	    	else if (JeyzerMXAllContextParamsRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new JeyzerMXAllContextParamsRule(headerCfg, displayContext);
	    	
	    	else if (MXBeanParamStringRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new MXBeanParamStringRule(headerCfg, displayContext);
	    	
	    	else if (MXBeanParamNumberRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new MXBeanParamNumberRule(headerCfg, displayContext);

	    	else if (CPURunnableThreadCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new CPURunnableThreadCounterRule(headerCfg, displayContext);

	    	else if (TendencyFunctionRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new TendencyFunctionRule(headerCfg, displayContext);
	    	
	    	else if (TendencyOperationRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new TendencyOperationRule(headerCfg, displayContext);

	    	else if (TendencyFunctionOperationRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new TendencyFunctionOperationRule(headerCfg, displayContext);

	    	else if (TendencyExecutorRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new TendencyExecutorRule(headerCfg, displayContext);
	    	
	    	else if (TendencyContentionTypeRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new TendencyContentionTypeRule(headerCfg, displayContext);
	    	
	    	else if (DiskSpaceUsedRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new DiskSpaceUsedRule(headerCfg, displayContext);

	    	else if (DiskSpaceUsedPercentRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new DiskSpaceUsedPercentRule(headerCfg, displayContext);
	    	
	    	else if (DiskSpaceFreeRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new DiskSpaceFreeRule(headerCfg, displayContext);
	    	
	    	else if (DiskSpaceFreePercentRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new DiskSpaceFreePercentRule(headerCfg, displayContext);
	    	
	    	else if (DiskWriteTimeRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new DiskWriteTimeRule(headerCfg, displayContext);
	    	
	    	else if (DiskWriteSizeRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new DiskWriteSizeRule(headerCfg, displayContext);
	    	
	    	else if (DiskWriteSpeedRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new DiskWriteSpeedRule(headerCfg, displayContext);
	    	
	    	else if (FileDescriptorUsagePercentRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new FileDescriptorUsagePercentRule(headerCfg, displayContext);
	    	
	    	else if (OpenFileDescriptorCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new OpenFileDescriptorCounterRule(headerCfg, displayContext);
	    	
	    	else if (session.areVirtualThreadVariationCountersAvailable() && VirtualThreadCreatedCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new VirtualThreadCreatedCounterRule(headerCfg, displayContext);
	    	
	    	else if (session.areVirtualThreadVariationCountersAvailable() && VirtualThreadTerminatedCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new VirtualThreadTerminatedCounterRule(headerCfg, displayContext);
	    	
	    	else if (session.areVirtualThreadVariationCountersAvailable() && VirtualThreadDiffRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new VirtualThreadDiffRule(headerCfg, displayContext);
	    	
	    	else if (session.hasVirtualThreadPresence() && VirtualThreadMountedCounterRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new VirtualThreadMountedCounterRule(headerCfg, displayContext);

	    	else if (session.hasVirtualThreadPresence() && VirtualThreadMountedCPUUsagePercentRule.RULE_NAME.equalsIgnoreCase(headerCfg.getName()))
    			rule = new VirtualThreadMountedCPUUsagePercentRule(headerCfg, displayContext);
	    	
	    	else
	    		logger.warn("Could not instanciate header rule for configuration node : {}", headerCfg.getName());
	    	
	    	if (rule != null)
	    		rules.add(rule);
	    }
		
		return rules;
	}	

}
