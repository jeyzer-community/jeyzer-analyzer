package org.jeyzer.monitor.util;

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




import org.jeyzer.monitor.config.engine.ConfigParamMonitorRule;

public enum Operator {
	DEFAULT, LOWER_OR_EQUAL, GREATER_OR_EQUAL;	
	
	public static final String OPERATOR_LOWER_OR_EQUAL = "lower_or_equal";
	public static final String OPERATOR_GREATER_OR_EQUAL = "greater_or_equal";
	
	public String getBoundPrefix() {
		if (this.equals(GREATER_OR_EQUAL) || this.equals(DEFAULT))
			return "Max ";
		else
			return "Min ";
	}
	
	public static Operator buildOperator(String operator){
		if (operator == null || operator.isEmpty())
			return DEFAULT;
		else if (OPERATOR_LOWER_OR_EQUAL.equals(operator))
			return LOWER_OR_EQUAL;
		else if (OPERATOR_GREATER_OR_EQUAL.equals(operator))
			return GREATER_OR_EQUAL;
		else
			return DEFAULT;
	}
	
	public static String getEventDescription(ConfigParamMonitorRule def) {
		switch(def.getOperator()){
		case LOWER_OR_EQUAL:
			return def.getDisplayName() + " is lower or equal to (value).";
		case GREATER_OR_EQUAL:
		default:
			return def.getDisplayName() + " is greater or equal to (value).";
		}
	}
}
