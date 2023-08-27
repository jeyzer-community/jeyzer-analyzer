package org.jeyzer.monitor.impl.rule.task;

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


import static org.jeyzer.monitor.config.engine.ConfigMonitorThreshold.THRESHOLD_STACK_SIGNAL_WITH_CONTEXT;
import static org.jeyzer.monitor.impl.event.task.VirtualThreadLeakTaskEvent.CONTEXT_KEY_TID_LEAK_SUSPECTS;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.analyzer.data.virtual.VirtualThreadStackImpl;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.monitor.config.engine.ConfigMonitorRule;
import org.jeyzer.monitor.config.engine.ConfigMonitorThreshold;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;
import org.jeyzer.monitor.engine.event.info.MonitorEventInfo;
import org.jeyzer.monitor.engine.event.info.SubLevel;
import org.jeyzer.monitor.engine.rule.MonitorTaskRule;
import org.jeyzer.monitor.engine.rule.condition.task.SignalWithContextTaskProvider;
import org.jeyzer.monitor.impl.event.task.VirtualThreadLeakTaskEvent;

public class VirtualThreadLeakRule extends MonitorTaskRule implements SignalWithContextTaskProvider{

	public static final String RULE_NAME = "Virtual thread leak";
	
	public static final String CONTEXT_KEY_VERIF_INDEX = "Index";
	public static final String CONTEXT_KEY_PREVIOUS_TIDS = "PrevTids";
	public static final String CONTEXT_KEY_PREVIOUS_TID_INTERSECTIONS = "PrevTidIntersections";
	
	public static final int VERIF_INDEX_STAGE = 4;
	
	public static final String RULE_CONDITION_DESCRIPTION = "Virtual thread stacks are present for long time.";
	public static final String RULE_NARRATIVE = "The " + RULE_NAME + " rule permits to detect unmounted virtual threads that seem frozen. "
			+ "Those threads are either potential leaks (if the event is still in progress) or reflecting a very slow interaction. ";
	
	public VirtualThreadLeakRule(ConfigMonitorRule def) throws JzrInitializationException {
		super(def, RULE_NAME, RULE_CONDITION_DESCRIPTION);
		checkThresholds(def);
	}

	@Override
	protected List<String> getSupportedThresholdTypes(){
		return Arrays.asList(THRESHOLD_STACK_SIGNAL_WITH_CONTEXT);
	}

	@Override
	public boolean isAdvancedMonitoringBased() {
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matchSignalWithContext(ThreadStack stack, Map<String, Object> context) {
		if (!stack.isVirtual() || !stack.getState().isUnmountedVirtualThread())
			return false;
		
		if (context == null)
			return true;  // context will be set afterwards

		VirtualThreadStackImpl vt = (VirtualThreadStackImpl) stack;
		
		// Update the verif index
		Integer index = (Integer)context.computeIfAbsent(CONTEXT_KEY_VERIF_INDEX, x-> Integer.valueOf(0));
		context.put(CONTEXT_KEY_VERIF_INDEX, Integer.valueOf(++index));
		
		if (index == 1) {
			// initialize the context
			context.put(CONTEXT_KEY_PREVIOUS_TIDS, vt.getVirtualThreadIds());
		}
		
		if (index % VERIF_INDEX_STAGE != 0)
			return true;
		
		// Time to verify the unmounted virtual stack content
		List<String> prevIds = (List<String>)context.get(CONTEXT_KEY_PREVIOUS_TIDS);
		List<String> currentIds = vt.getVirtualThreadIds();
		
		// Get the intersection
		Set<String> currentIntersections = currentIds.stream().filter(prevIds::contains).collect(Collectors.toSet());
		
		// Completely different ids on each side : no leak (*)
		if (currentIntersections.isEmpty())
			return false;
		
		// Let's check if the ids were in the previous intersection list
		Set<String> prevIntersections = (Set<String>)context.get(CONTEXT_KEY_PREVIOUS_TID_INTERSECTIONS);
		if (prevIntersections == null) {
			// first time
			context.put(CONTEXT_KEY_PREVIOUS_TIDS, currentIds);
			context.put(CONTEXT_KEY_PREVIOUS_TID_INTERSECTIONS, currentIntersections);
			return true;
		}
		
		Set<String> leakSuspects = currentIntersections.stream().filter(prevIntersections::contains).collect(Collectors.toSet());
		
		// Completely different ids on each intersection side : no leak (*)
		if (leakSuspects.isEmpty())
			return false;
		
		// The leak suspect list is intended to evolve over time
		// (*) Event could be elected and end if no more suspects are found : in such case the (last seen) suspects are good candidates for slowness issue (instead of real leak)
		context.put(CONTEXT_KEY_TID_LEAK_SUSPECTS, leakSuspects);
		context.put(CONTEXT_KEY_PREVIOUS_TIDS, currentIds);
		context.put(CONTEXT_KEY_PREVIOUS_TID_INTERSECTIONS, currentIntersections);
		return true;
	}

	@Override
	public boolean matchBeginSignalWithContext(ThreadStack stack, Map<String, Object> context) {
		return context == null;   // no history, so this is the beginning
	}

	@Override
	public MonitorTaskEvent createTaskEvent(MonitorEventInfo info, ThreadAction action, ThreadStackHandler stack) {
		return new VirtualThreadLeakTaskEvent(
				info,
				action,
				stack
				);
	}
	
	@Override
	public SubLevel getDefaultSubLevel() {
		return SubLevel.DEFAULT_HIGH;
	}
	
	@Override
	public String getDefaultNarrative() {
		return RULE_NARRATIVE;
	}
	
	private void checkThresholds(ConfigMonitorRule def) throws JzrInitializationException {
		for (ConfigMonitorThreshold threshold : def.getConfigMonitorThresholds()) {
			if (!threshold.isTimeBound()) {
				// for now we can only handle counts. Time would require to access the session period.
				if (threshold.getCount() < VERIF_INDEX_STAGE)
					throw new JzrInitializationException("Rule " + RULE_NAME + " threshold count (" + threshold.getCount()+ ") is invalid. It must be higher than " + VERIF_INDEX_STAGE * 2);
			}
		}
	}
}