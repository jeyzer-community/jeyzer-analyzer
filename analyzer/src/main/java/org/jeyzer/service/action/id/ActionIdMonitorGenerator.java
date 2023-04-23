package org.jeyzer.service.action.id;

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







import java.util.HashMap;
import java.util.Map;

import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;

import com.google.common.collect.Multimap;

public class ActionIdMonitorGenerator implements ActionIdGenerator {
	
	private ActionIdAnalyzerGenerator generator = new ActionIdAnalyzerGenerator();
	private boolean analysisInitClose = false;
	private Map<String, Integer> actionIdsPerThreaNameAndId = new HashMap<>();

	@Override
	public int getActionId(ThreadStack stack) {
		if (analysisInitClose)
			return generator.getActionId(stack);

		// Let's find our action id in the cache of previous actions ids 
		// in case action was already there (and spotted by the TD monitor) in the previous monitor session  
		String key = buildKey(stack);
		Integer id = actionIdsPerThreaNameAndId.get(key);
		if (id != null)
			return id.intValue();
		else
			return generator.getActionId(stack);
	}
	
	private String buildKey(ThreadStack stack) {
		String key = stack.getID() + "@" +  stack.getName();
		
		if (stack.getThreadStackJeyzerMXInfo() != null && !stack.getThreadStackJeyzerMXInfo().getJzrId().isEmpty())
			key += "@" + stack.getThreadStackJeyzerMXInfo().getJzrId();
		
		return key;
	}

	@Override
	public void analysisInitClose() {
		analysisInitClose = true;
	}

	public void cacheActionIds(Multimap<String, MonitorTaskEvent> taskEvents) {
		actionIdsPerThreaNameAndId.clear();
		
		for (MonitorEvent event : taskEvents.values()){
			MonitorTaskEvent taskEvent = (MonitorTaskEvent) event;
			
			String key = taskEvent.getThreadId() + "@" +  taskEvent.getThreadName();
			if (taskEvent.getJhId() != null)
				key += "@" + taskEvent.getJhId();
			
			actionIdsPerThreaNameAndId.put(key, taskEvent.getActionId());
		}
		
		analysisInitClose = false;
	}	
	
}
