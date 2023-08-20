package org.jeyzer.analyzer.data.stack;

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
import java.util.Date;
import java.util.List;


public class ThreadStackImpl extends AbstractThreadStack {
	
	public ThreadStackImpl(String header, String id, String name, ThreadState state, int filePos, String fileName, Date timestamp, 
			List<String> codeLines){
		super(header, id, name, state, filePos, fileName, timestamp, codeLines);
	}
	
	public ThreadStackImpl(String header, String id, String name, ThreadState state, int filePos, String fileName, Date timestamp, 
			List<String> codeLines, boolean ofInterest){
		super(header, id, name, state, filePos, fileName, timestamp, codeLines);
		this.setInterest(ofInterest);
	}
	
	public ThreadStackImpl(String header, String name, String id, ThreadState state, boolean suspended, int filePos, String fileName, Date timestamp, 
							List<String> codeLines, String lockName, String lockClassName, List<String> ownedlocks, boolean deadlock){
		super(header, name, id, state, suspended, filePos, fileName, timestamp, codeLines, lockName, lockClassName, ownedlocks, deadlock);
	}

	public ThreadStackImpl(String header, String name, String id, ThreadState state, boolean suspended, int filePos, String fileName, Date timestamp, 
							List<String> codeLines, String lockName, String lockClassName, List<String> ownedlocks, List<String> biasedLocks, boolean deadlock, 
							DAEMON daemon, int priority){
		super(header, name, id, state, suspended, filePos, fileName, timestamp, codeLines, lockName, lockClassName, ownedlocks, deadlock);
		if (biasedLocks != null)
			this.biasedLocks = new ArrayList<String>(biasedLocks);
		this.daemon = daemon; 
		this.priority = priority;
	}
	
	public ThreadStackImpl(String header, String name, String id, ThreadState state, boolean suspended, 
			int filePos, String fileName, Date timestamp,
			List<String> codeLines, String lockedOn, String lockClassName, List<String> ownedLocks, boolean deadlock,
			long cpuTime, long userTime, long allocatedMemory,
			ThreadStackJeyzerMXInfo jeyzerMXInfo) {
		this(header, name, id, state, suspended, filePos, fileName, timestamp, codeLines, lockedOn, lockClassName, ownedLocks, deadlock);
		
		cpuInfo = new ThreadStackCPUInfo(cpuTime, userTime, timestamp.getTime());
		memoryInfo = new ThreadStackMemoryInfo(allocatedMemory);
		this.jeyzerMXInfo = jeyzerMXInfo; // can be null
	}

	public ThreadStackImpl(String header, String name, String id, ThreadState state, boolean suspended, 
			int filePos, String fileName, Date timestamp,
			List<String> codeLines, String lockedOn, String lockClassName, List<String> ownedLocks, List<String> biasedLocks, DAEMON daemon, int priority,
			long cpuTime, long allocatedMemory) {
		this(header, name, id, state, suspended, filePos, fileName, timestamp, codeLines, lockedOn, lockClassName, ownedLocks, biasedLocks, false, daemon, priority);
		cpuInfo = new ThreadStackCPUInfo(cpuTime, cpuTime, timestamp.getTime()); // cpuTime and usrTime cannot be obtained yet with JFR 
		memoryInfo = new ThreadStackMemoryInfo(allocatedMemory);
	}

	@Override
	public boolean isVirtual() {
		return false;
	}	
	
	@Override
	public int getInstanceCount() {
		return 1;  // always unique
	}

	@Override
	public boolean hasUniqueInstance() {
		return true;  // always unique
	}
}
