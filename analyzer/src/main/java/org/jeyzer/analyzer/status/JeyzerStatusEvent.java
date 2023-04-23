package org.jeyzer.analyzer.status;

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



public class JeyzerStatusEvent {

	public enum STATE { INITIALIZING, PROFILE_REDIRECTION, UNZIPPING, JFR_READING, DECRYPTION, DEOBFUSCATION, PARSING, DATA_PROCESSING, REPORT_GENERATION, REPORT_ENCRYPTION}
	
	public static String getDisplayText(STATE state){
		if (STATE.INITIALIZING.equals(state))
			return "Initializing...";
		if (STATE.PROFILE_REDIRECTION.equals(state))
			return "Profile redirection...";
		else if (STATE.UNZIPPING.equals(state))
			return "Unzipping files...";
		else if (STATE.JFR_READING.equals(state))
			return "Reading JFR file...";
		else if (STATE.DECRYPTION.equals(state))
			return "Decrypting files...";
		else if (STATE.DEOBFUSCATION.equals(state))
			return "Deobfuscating files...";
		else if (STATE.PARSING.equals(state))
			return "Parsing files...";
		else if (STATE.DATA_PROCESSING.equals(state))
			return "Analyzing data...";
		else if (STATE.REPORT_ENCRYPTION.equals(state))
			return "Encrypting the report...";
		else
			return "Analysis in progress...";
	}
	
	public static float getProgress(STATE state, float current){
		if (STATE.INITIALIZING.equals(state))
			return current + 0.05f;
		else if (STATE.PROFILE_REDIRECTION.equals(state))
			return current + 0.05f;
		else if (STATE.UNZIPPING.equals(state))
			return current + 0.1f;
		else if (STATE.JFR_READING.equals(state))
			return current + 0.1f;
		else if (STATE.DECRYPTION.equals(state))
			return current + 0.1f;
		else if (STATE.DEOBFUSCATION.equals(state))
			return current + 0.1f;
		else if (STATE.PARSING.equals(state))
			return current + 0.1f;
		else if (STATE.DATA_PROCESSING.equals(state))
			return current + 0.05f;
		else if (STATE.REPORT_GENERATION.equals(state))
			return current + 0.05f;
		else if (STATE.REPORT_ENCRYPTION.equals(state))
			return current + 0.1f;
		else
			return current;
	}
}
