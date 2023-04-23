package org.jeyzer.analyzer.data.action;

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionGraphRoot {
	
	public static final String EXECUTOR_ACTION_SEPARATOR =" @@ "; 
	
	private static final Logger logger = LoggerFactory.getLogger(ActionGraphRoot.class);

	private Map<String, ActionGraphSection> actionGraphSectionRoots = new HashMap<>();  
	
	private final boolean atbiIncluded;
	private final boolean statesIncluded;
	
	public ActionGraphRoot(boolean atbiIncluded, boolean statesIncluded) {
		this.atbiIncluded = atbiIncluded;
		this.statesIncluded = statesIncluded;
	}

	public Set<Map.Entry<String, ActionGraphSection>> getActionGraphSectionRoots(){
		List<Map.Entry<String, ActionGraphSection>> entries = new ArrayList<>(this.actionGraphSectionRoots.entrySet());

		// sort by stack count, higher first
		Collections.sort(entries,new ActionGraphRootComparable());
		
		Map<String, ActionGraphSection> result = new LinkedHashMap<String, ActionGraphSection>();
	    for (Iterator<Map.Entry<String, ActionGraphSection>> it = entries.iterator(); it.hasNext();) {
	    	Map.Entry<String, ActionGraphSection> entry = it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }
		
		return result.entrySet();
	}

	public void addAction(ThreadAction action){
		String key;

		if (!atbiIncluded && ThreadStack.FUNC_TO_BE_IDENTIFIED.equals(action.getPrincipalCompositeFunction()))
			return; // Ignore ATBI : do not include it in profiling
		
		// @TODO : getPrincipalCompositeFunction() may not be unique, replace
		key = action.getExecutor()  
			+ EXECUTOR_ACTION_SEPARATOR 
			+ action.getPrincipalCompositeFunction();
		
		ActionGraphRootSection section = (ActionGraphRootSection)actionGraphSectionRoots.get(key);
		if (section != null){
			section.updateActionCount();
			for (int i=0; i<action.size(); i++){
				ThreadStack stack = action.getThreadStack(i);
				if (!section.acceptNewStack(stack))
					// should never happen. there is always a common root code line. 
					// Also, one of the children must accept it at some point
					logger.error("Failed to integrate stack into any graph section. Stack is : \n{}", stack.toString());
			}
		}
		else{
			// this is new action to process : create new root section
			ThreadStack stack = action.getThreadStack(0);
			section = new ActionGraphRootSection(stack, action, statesIncluded);
			
			for (int i=1; i<action.size(); i++){
				stack = action.getThreadStack(i);
				if (!section.acceptNewStack(stack))
					// should never happen. there is always a common root code line
					logger.error("Failed to integrate stack into new root action graph. Stack is : \n{}", stack.toString());
			}
			
			actionGraphSectionRoots.put(key, section);
		}
	}

	public static class ActionGraphRootComparable implements Comparator<Map.Entry<String, ActionGraphSection>>{
		 
	    @Override
	    public int compare(Map.Entry<String, ActionGraphSection> e1, Map.Entry<String, ActionGraphSection> e2) {
	    	ActionGraphSection s1 = e1.getValue();
	    	ActionGraphSection s2 = e2.getValue();
	        return s1.getStackCount()<s2.getStackCount() ? 1 : s1.getStackCount()==s2.getStackCount() ? 0 : -1;
	    }
	}
	
}
