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







public enum ThreadState {

	// Standard Java states (obtained in hung mode)
	NEW("New"),
	RUNNABLE("Runnable"), 
	TIMED_WAITING("Timed waiting"),
	WAITING("Waiting"),
	BLOCKED("Blocked"),
	TERMINATED("Terminated state"),
	
	// Internal Java states (obtained in hung mode)
	// NEW, BLOCKED as well as :
	IN_NATIVE("In native"), 						// Running in native code.
	IN_NATIVE_TRANS("In native transition"), 		// Corresponding transition state
	IN_VM("In virtual Machine"), 					// Running in VM.
	IN_VM_TRANS("In virtual Machine transition"), 	// Corresponding transition state.
	IN_JAVA("In Java"),			 					// Running in Java or in stub code.
	SYS_BLOCKED("Sys blocked"),			 			// Virtual. Used to differentiate from BLOCKED. Less meaning full.
	SYS_BLOCKED_TRANS("Blocked transition");	 	// Corresponding transition state.	
	
	private String name;
	
	private ThreadState(String name){
		this.name = name;
	}

	public String getDislayName(){
		return this.name;
	}
	
	public boolean isBlocked(){
		return BLOCKED.equals(this);
	}
	
	public boolean isWaiting(){
		return WAITING.equals(this);
	}

	public boolean isTimedWaiting(){
		return TIMED_WAITING.equals(this);
	}	
	
	public boolean isNew(){
		return NEW.equals(this);
	}
	
	public boolean isTerminated(){
		return TERMINATED.equals(this);
	}
	
	public boolean isRunning(){
		if (RUNNABLE.equals(this))
			return true;
		
		// everything else
		return !isWaiting() 
				&& !isTimedWaiting()
				&& !isBlocked()
				&& !isNew()
				&& !isTerminated();
	}

}
