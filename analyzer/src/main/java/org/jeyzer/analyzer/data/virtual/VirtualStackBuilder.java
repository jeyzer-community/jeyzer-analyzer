package org.jeyzer.analyzer.data.virtual;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2023 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadState;

public class VirtualStackBuilder {

	// Must be synchronized between parsing threads
	protected Map<String, VirtualThreadStackImpl> vtAggregations = Collections.synchronizedMap(new HashMap<String, VirtualThreadStackImpl>());
	protected Map<String, String> transversalIds = Collections.synchronizedMap(new HashMap<String, String>());	
	
	// guarded by class instance
	private static int nextId = 0;

	private static String getNextId() {
		synchronized (VirtualStackBuilder.class) {
			return "VTG #" + Integer.toString(nextId++);
		}
	}	
	
	public ThreadStack lookup(String id, ThreadState state, int filePos, String fileName, Date timestamp, List<String> codeLines) {
		String stackKey = Integer.toString(hashCodeLines(codeLines));
		String crossId = transversalIds.computeIfAbsent(stackKey, x-> getNextId());
		
		String key = timestamp.toString() + stackKey;
		
		VirtualThreadStackImpl vt = vtAggregations.computeIfAbsent(
				key, 
				x-> new VirtualThreadStackImpl(
						crossId, 
						state, 
						filePos, 
						fileName, 
						timestamp, 
						codeLines));
		
		vt.integrateStack(id);
		
		return vt;
	}
	
	private int hashCodeLines(List<String> codeLines) {
		  int hash = 6;
		  int index = 0;
		  
		  for (String line : codeLines){
			  hash += 32 * line.hashCode();
			  index++;
			  if (index > 10)
				  break;
		  }
		  
		  return hash;
	}
}
