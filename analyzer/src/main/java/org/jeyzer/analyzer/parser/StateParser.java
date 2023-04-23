package org.jeyzer.analyzer.parser;

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



import java.text.ParseException;

import org.jeyzer.analyzer.data.stack.ThreadState;

public final class StateParser {

	// Thread state
	public static final String NEW_STATE = "NEW";	
	public static final String RUNNABLE_STATE = "RUNNABLE";
	public static final String TIMED_WAITING_STATE = "TIMED_WAITING";
	public static final String WAITING_STATE = "WAITING";
	public static final String BLOCKED_STATE = "BLOCKED";
	public static final String TERMINATED_STATE = "TERMINATED";
	
	// Thread state in hung mode  : NEW, BLOCKED as well as :
	public static final String IN_NATIVE_STATE = "IN_NATIVE"; 				// Running in native code.
	public static final String IN_NATIVE_STATE_TRANS = "IN_NATIVE_TRANS"; 	// Corresponding transition state
	public static final String IN_VM_STATE = "IN_VM"; 						// Running in VM.
	public static final String IN_VM_TRANS_STATE = "IN_VM_TRANS"; 			// Corresponding transition state.
	public static final String IN_JAVA_STATE = "IN_JAVA"; 					// Running in Java or in stub code.
	public static final String SYS_BLOCKED_STATE = "SYS_BLOCKED"; 			// Virtual. Used to differentiate from BLOCKED. Less meaning full.
	public static final String SYS_BLOCKED_TRANS_STATE = "BLOCKED_TRANS"; 	// Corresponding transition state.	
	
	// JRockit Mission Control thread state
	public static final String JRMC_STATE_PARKED = ", parked,";
	public static final String JRMC_STATE_SLEEPING = ", sleeping,";
	public static final String JRMC_STATE_WAITING = ", waiting,";
	public static final String JRMC_STATE_BLOCKED = ", blocked,";
	// Others : [STANDBY], [STUCK]
	public static final String JRMC_WEBLO_ACTIVE = "[ACTIVE]";
	public static final String JRMC_WEBLO_FALLBACK_FOR_QUEUE = "for queue:";
	
	// IBM thread states
	private static final String STATE_R = "R";		// Running or runnable thread
	private static final String STATE_CW = "CW";	// Thread waiting on a condition variable
	private static final String STATE_MW = "MW";	// Thread waiting on a monitor lock. MUxSemWait
	private static final String STATE_MS = "MS";	// Thread suspended waiting on a monitor lock
	private static final String STATE_P = "P";		// Timed waiting state (?)
	private static final String STATE_B = "B";		// Blocked
	private static final String STATE_S = "S";		// Suspended thread	
	
	private StateParser(){
	}
	
	public static ThreadState parseState(String header, String startTag, String endTag) throws ParseException{
		String value = parseHeader(header, startTag, endTag);

		if (RUNNABLE_STATE.equals(value))
			return ThreadState.RUNNABLE;
		else if (WAITING_STATE.equals(value))
			return ThreadState.WAITING;
		else if (BLOCKED_STATE.equals(value))
			return ThreadState.BLOCKED;
		else if (TIMED_WAITING_STATE.equals(value))
			return ThreadState.TIMED_WAITING;
		else if (NEW_STATE.equals(value))
			return ThreadState.NEW;
		else if (TERMINATED_STATE.equals(value))
			return ThreadState.TERMINATED;
		else
			throw new ParseException("Thread state not recognized : " + value
					+ " on header : " + header, -1);
	}
	
	public static ThreadState parseTDAState(String header) throws ParseException{
		// "Agent Heartbeat" Id=6 TIMED_WAITING
		if (header.contains(RUNNABLE_STATE))
			return ThreadState.RUNNABLE;
		else if (header.contains(WAITING_STATE))
			return ThreadState.WAITING;
		else if (header.contains(BLOCKED_STATE))
			return ThreadState.BLOCKED;
		else if (header.contains(TIMED_WAITING_STATE))
			return ThreadState.TIMED_WAITING;
		else if (header.contains(NEW_STATE))
			return ThreadState.NEW;
		else if (header.contains(TERMINATED_STATE))
			return ThreadState.TERMINATED;
		else
			throw new ParseException("Thread state not recognized on header : " + header, -1);
	}
	
	public static ThreadState parseJRockitState(String header) {
		// "JFR request timer" id=16 idx=0x5c tid=17655 prio=5 alive, waiting, native_blocked, daemon
		// extract waiting
		
		// Must be performed in this order :
		if (header.contains(JRMC_STATE_BLOCKED))
			return ThreadState.BLOCKED;
		else if (header.contains(JRMC_STATE_PARKED))
			return ThreadState.WAITING;
		else if (header.contains(JRMC_STATE_WAITING))
			return ThreadState.WAITING;
		else if (header.contains(JRMC_WEBLO_ACTIVE)) // Can start with or without "
			return ThreadState.RUNNABLE;
		else if (header.contains(JRMC_STATE_SLEEPING))
			return ThreadState.TIMED_WAITING;
		else if (header.contains(JRMC_WEBLO_FALLBACK_FOR_QUEUE))
			return ThreadState.WAITING;
		else
			// "Thread-16" id=40 idx=0xa4 tid=17684 prio=5 alive, in native, daemon
			//     at jrockit/net/SocketNativeIO.readBytesPinned(Ljava/io/FileDescriptor;[BIII)I(Native Method)
			return ThreadState.WAITING; // last fallback
	}

	public static ThreadState parseIBMState(String header, String startTag, String endTag) throws ParseException{
		// 3XMTHREADINFO "VMTransport" J9VMThread:0x0000000001E88A00,
		// j9thread_t:0x00002AAAC27754C0, java/lang/Thread:0x000000009422DB98,
		// state:P, prio=5
		String value = parseHeader(header, startTag, endTag);

		if (STATE_CW.equals(value)) 
			return ThreadState.WAITING;
		else if (STATE_MW.equals(value)) 
			return ThreadState.WAITING;
		else if (STATE_MS.equals(value)) 
			return ThreadState.WAITING;
		else if (STATE_R.equals(value))
			return ThreadState.RUNNABLE;
		else if (STATE_B.equals(value))
			return ThreadState.BLOCKED;
		else if (STATE_P.equals(value))
			return ThreadState.TIMED_WAITING;
		else if (STATE_S.equals(value)) 
			return ThreadState.TIMED_WAITING;
		else
			throw new ParseException("Thread state not recognized : " + value
					+ " on header : " + header, -1);
	}

	public static ThreadState parseHungState(String header, String startTag, String endTag) throws ParseException{
		String value = parseHeader(header, startTag, endTag);
		
		if (IN_NATIVE_STATE.equals(value))
			return ThreadState.IN_NATIVE;
		else if (IN_NATIVE_STATE_TRANS.equals(value))
			return ThreadState.IN_NATIVE_TRANS;
		else if (IN_VM_STATE.equals(value))
			return ThreadState.IN_VM;
		else if (IN_VM_TRANS_STATE.equals(value))
			return ThreadState.IN_VM_TRANS;
		else if (IN_JAVA_STATE.equals(value))
			return ThreadState.IN_JAVA;
		else if (NEW_STATE.equals(value))
			return ThreadState.NEW;
		else if (SYS_BLOCKED_TRANS_STATE.equals(value))
			return ThreadState.SYS_BLOCKED_TRANS;
		else if (BLOCKED_STATE.equals(value))
			// special handling of BLOCKED state as BLOCKED can apply as well for wait. Therefore less meaning full.
			return ThreadState.SYS_BLOCKED;
		else
			throw new ParseException("Thread state not recognized : " + value
					+ " on header : " + header, -1);
	}
	
	private static String parseHeader(String header, String startTag, String endTag) {
		int posStart = header.indexOf(startTag) + startTag.length();
		int posEnd = header.indexOf(endTag, posStart);
		if (posEnd == -1)
			posEnd = header.length();
		return header.substring(posStart, posEnd);
	}	
}
