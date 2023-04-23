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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jeyzer.analyzer.config.ConfigState;
import org.jeyzer.analyzer.config.analysis.ConfigPreFilter;
import org.jeyzer.analyzer.config.analysis.ConfigPreFilter.ConfigAutoStrategy;
import org.jeyzer.analyzer.config.analysis.ConfigPreFilter.ConfigFilterKeep;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.session.JzrSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;



public class AnalysisPreFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(AnalysisPreFilter.class);

	private static final int MAX_STACK_SIZE = 10000;
	
	private ConfigFilterKeep keepFilter;
	private int stackMinimumSize;
	
	public AnalysisPreFilter(ConfigPreFilter configPreFilter, JzrSession session) {
		this.keepFilter = configPreFilter.getFilterKeepCfg();
		this.stackMinimumSize = isAutoStackSizeInterestStrategy(configPreFilter, session.getConfigurationState()) ? 
				computeMinimumSize(configPreFilter.getAutoStrategyCfg(), session) : configPreFilter.getMinSize();
		logger.info("Pre-filtering strategy is : " + configPreFilter.getStackSizeInterestStrategy().toString().toLowerCase() + " with minimum stack size : " + this.stackMinimumSize);
	}

	public int getStackMinimumSize() {
		return this.stackMinimumSize;
	}
	
	public ConfigFilterKeep getConfigFilterKeep() {
		return this.keepFilter;
	}
	
	private boolean isAutoStackSizeInterestStrategy(ConfigPreFilter configPreFilter, ConfigState configState) {
		if (configPreFilter.isAutoStackSizeInterestStrategy())
			return true;
		
		if (configPreFilter.isKnownStackSizeInterestStrategy())
			return false;
		
		// always make it automatic if profile is generic
		return configState.isGeneric();
	}

	private int computeMinimumSize(ConfigAutoStrategy strategy, JzrSession session) {
		
		// Check if the volume of snapshots is sufficient for applying the auto strategy.
		// Taken sample snapshots must represent at least 5% of the recording.
		// If not sufficient we apply a low default limit
		if (FormulaHelper.percentRound(strategy.getSamplesLimit(), session.getDumps().size()) > strategy.getRepresentativeSamplePercentage()) {
			logger.info("Pre-filtering auto strategy cannot be applied due to recording smallness. Default size will be used : " + strategy.getDefaultSize());
			return strategy.getDefaultSize();
		}
		
		// Get a list of random small dumps, ordered by stack size
		List<ThreadDump> dumps = new ArrayList<>(session.getDumps());
		Collections.shuffle(dumps);
		Set<ThreadDump> candidates = new TreeSet<>(new Comparator<ThreadDump>() {
		        @Override
		        public int compare(ThreadDump td1, ThreadDump td2) {
	                return td1.size() < td2.size() ? -1 : 1; // must not be equal otherwise item is skipped by TreeSet contract
		        }
	        });
		
		candidates.addAll(dumps);
		
		// collect the stacks in the sample set
		int sampleCount = 0;
		Multimap<Integer, ThreadStack> waitStacksMultiSet = LinkedHashMultimap.create();
		Multimap<Integer, ThreadStack> runningStacksMultiSet = LinkedHashMultimap.create();
		for (ThreadDump dump : candidates) {
			for(ThreadStack stack : dump.getThreads()) {
				// Stacks with size 0 do exist : those are JVM internal threads
				if (stack.getDepthLength() == 0)
					continue;
				if (stack.getState().isWaiting() || stack.getState().isTimedWaiting())
					waitStacksMultiSet.put(stack.getDepthLength(), stack);
				else if (strategy.isStopIfActiveFound() && stack.getState().isRunning())
					runningStacksMultiSet.put(stack.getDepthLength(), stack);
			}
			
			if (sampleCount == strategy.getSamplesLimit())
				break;
			sampleCount++;
		}

		float cumulatedPercent = 0;
		int stacksTotal = waitStacksMultiSet.values().size();
		if (stacksTotal == 0)
			return 1; // take everything
		
		// parse the list until we reach the percentage limit or if we have a stop on active stacks
		for(int size=1; size < MAX_STACK_SIZE; size++) {

			if (strategy.isStopIfActiveFound() && !runningStacksMultiSet.get(size).isEmpty())
				// We choose to get wait threads along with the running ones, at least of same size
				return size;

			Collection<ThreadStack> stacks = waitStacksMultiSet.get(size);
			if (stacks.isEmpty())
				continue;

			float increase = ((float)stacks.size() / stacksTotal) * 100f;
			cumulatedPercent += increase;
			
			if (cumulatedPercent > strategy.getPercentage())
				//  based on last volume increment, decide if we filter more
				return increase > 10 ? size+1 : size;
		}
		
		return MAX_STACK_SIZE;
	}
}
