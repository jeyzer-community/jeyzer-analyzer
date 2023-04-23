package org.jeyzer.analyzer.data.tag;

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







public class ThreadStateTag extends Tag{

	public static final String DISPLAY_NAME = "Thread state";	
	
	public enum THREAD_STATE { 
		RUNNING, 
		DEADLOCK, 
		LOCKED,
		WAITING,
		TIMED_WAITING,
		UNKNOWN; 
	
		@Override
		  public String toString() {
		    switch(this) {
		      case RUNNING: return "Running";
		      case DEADLOCK: return "Deadlock";
		      case LOCKED: return "Locked";
		      case WAITING: return "Waiting";
		      case TIMED_WAITING: return "TimedWaiting";
		      default: return "Unknown";
		    }
		  }	
	}

	private THREAD_STATE state;
	
	public ThreadStateTag(THREAD_STATE value) {
		super(value.toString());
		state = value;
	}
	
	@Override
	public String getTypeName() {
		return DISPLAY_NAME;
	}

	public THREAD_STATE getState() {
		return state;
	}
	
}
