package org.jeyzer.web.config;

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


import javax.servlet.ServletConfig;

import com.google.common.primitives.Ints;

public class ConfigUtil {
	
	public static final int BYTES_IN_1_MB = 1048586;
	
	private ConfigUtil() {}
	
	public static boolean loadBoolAttribute(final ServletConfig servletConfig, String variableName, String servletParamName){
		String value = System.getenv(variableName);
		if (value == null){
			value = servletConfig.getInitParameter(servletParamName);
		}
		return Boolean.parseBoolean(value);
	}
	
	public static Integer loadIntAttribute(final ServletConfig servletConfig, String envVariableName, String servletParamName) {
		String paramValue = System.getenv(envVariableName);
		
		if (paramValue == null){
			paramValue = servletConfig.getInitParameter(servletParamName);
			// Special case where weird unrecognized char ('?') is added at the end for the init parameters declared through annotations
			if (paramValue.charAt(paramValue.length()-1) == 8236)
				// remove the last char to prevent from null pointer then
				paramValue = paramValue.substring(0, paramValue.length()-1);
		}
		
		return Ints.tryParse(paramValue.trim());
	}
	
	public static String loadStringAttribute(final ServletConfig servletConfig, String envVariableName, String servletParamName) {
		String paramValue = System.getenv(envVariableName);
		if (paramValue == null){
			paramValue = servletConfig.getInitParameter(servletParamName);
		}
		return paramValue;
	}
}
