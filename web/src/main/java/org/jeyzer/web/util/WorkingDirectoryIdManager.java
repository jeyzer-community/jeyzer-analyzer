package org.jeyzer.web.util;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Web
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WorkingDirectoryIdManager {

    // thread safe
    private Map<String, ConcurrentLinkedQueue<Integer>> workingDirectories;	
	
	public WorkingDirectoryIdManager(Set<String> profiles, int maxAnalysisInstances){
		workingDirectories = new HashMap<>(profiles.size());

		List<Integer> values = new ArrayList<>(maxAnalysisInstances);
		for (int i=1; i<maxAnalysisInstances+1; i++){
			values.add(i);
		}
		
		for (String profile : profiles){
			ConcurrentLinkedQueue<Integer> numbers = new ConcurrentLinkedQueue<>(values);
			workingDirectories.put(profile, numbers);
		}
	}
	
	public Integer pollId(String profile){
		ConcurrentLinkedQueue<Integer> numbers = this.workingDirectories.get(profile);
		return numbers.poll();
	}
	
	public void releaseId(String profile, Integer id){
		ConcurrentLinkedQueue<Integer> numbers = this.workingDirectories.get(profile);
		if (numbers != null)
			numbers.offer(id);
	}
}
