package org.jeyzer.analyzer.output.poi.rule.header.function;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeaders;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeadersSets;
import org.jeyzer.analyzer.output.poi.rule.header.Header;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;
import org.jeyzer.analyzer.session.JzrSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;



public class HeaderFunctionBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(HeaderFunctionBuilder.class);
	
	private static final HeaderFunctionBuilder builder = new HeaderFunctionBuilder();
	
	private HeaderFunctionBuilder(){
	}
	
	public static HeaderFunctionBuilder newInstance(){
		return builder;
	}

	public Map<String, HeaderFunction> buildHeaderFunctions(ConfigSheetHeadersSets headerConfigSets, List<Header> headers, JzrSession session) {
		Map<String, HeaderFunction> functionsPerHeader = new HashMap<>();
		HeaderFunction function, next;

		ConfigSheetHeaders headerFunctionConfigs= headerConfigSets.getHeaderConfigs(session.getFormatShortName());
		
		if (headerFunctionConfigs == null)
			return functionsPerHeader; // no function rules for that format
		
		List<String> functionNames = headerFunctionConfigs.getFunctions();
		if (functionNames.isEmpty())
			return functionsPerHeader; // no function rules for that format
		
		List<String> reversed = ImmutableList.copyOf(functionNames).reverse();
		
		for (Header header : headers){
			next = null;
			
			function = null;
		    for (String functionName : reversed){
		    	function = null;

		    	if (AverageFunction.RULE_NAME.equalsIgnoreCase(functionName))
		    		function = header.supportFunction(HeaderFunctionType.AVERAGE) ? new AverageFunction(next) : new NotSupportedFunction(next);
		    	else if (MinFunction.RULE_NAME.equalsIgnoreCase(functionName))
		    		function = header.supportFunction(HeaderFunctionType.MIN) ? new MinFunction(next) : new NotSupportedFunction(next);
			    else if (MaxFunction.RULE_NAME.equalsIgnoreCase(functionName))
			    	function = header.supportFunction(HeaderFunctionType.MAX) ? new MaxFunction(next) : new NotSupportedFunction(next);
			    else if (CumulativeFunction.RULE_NAME.equalsIgnoreCase(functionName))
				    function = header.supportFunction(HeaderFunctionType.CUMULATIVE) ? new CumulativeFunction(next) : new NotSupportedFunction(next);
			    else if (VarianceFunction.RULE_NAME.equalsIgnoreCase(functionName))
				    function = header.supportFunction(HeaderFunctionType.VARIANCE) ? new VarianceFunction(next) : new NotSupportedFunction(next);
				else if (StandardDeviationFunction.RULE_NAME.equalsIgnoreCase(functionName))
				    function = header.supportFunction(HeaderFunctionType.STANDARD_DEVIATION) ? new StandardDeviationFunction(next) : new NotSupportedFunction(next);
		    	else
		    		logger.warn("Could not instanciate header function rule for configuration node : {}", functionName);
		    	
		    	if (function != null)
		    		next = function;
		    }
		    
		    if (function != null)
		    	functionsPerHeader.put(header.getName(), function);
		}
		
		return functionsPerHeader;
	}

	public List<HeaderFunction> buildHeaderModelFunctions(ConfigSheetHeadersSets headerConfigs, List<Header> headers, JzrSession session) {
		List<HeaderFunction> functions = new ArrayList<>();
		
		ConfigSheetHeaders headerFunctionConfigs= headerConfigs.getHeaderConfigs(session.getFormatShortName());
		
		if (headerFunctionConfigs == null)
			return functions; // no function rules for that format
		
		List<String> functionNames = headerFunctionConfigs.getFunctions();
		if (functionNames.isEmpty())
			return functions; // no function rules for that format
		
	    for (String functionName : functionNames){
	    	HeaderFunction function = null;

	    	if (AverageFunction.RULE_NAME.equalsIgnoreCase(functionName))
	    		function = new AverageFunction(null);
	    	else if (MinFunction.RULE_NAME.equalsIgnoreCase(functionName))
	    		function = new MinFunction(null);
		    else if (MaxFunction.RULE_NAME.equalsIgnoreCase(functionName))
		    	function = new MaxFunction(null);
		    else if (CumulativeFunction.RULE_NAME.equalsIgnoreCase(functionName))
		    	function = new CumulativeFunction(null);
		    else if (VarianceFunction.RULE_NAME.equalsIgnoreCase(functionName))
		    	function = new VarianceFunction(null);
		    else if (StandardDeviationFunction.RULE_NAME.equalsIgnoreCase(functionName))
		    	function = new StandardDeviationFunction(null);
	    	else
	    		logger.warn("Could not instanciate header function rule for configuration node : {}", functionName);
		    	
	    	if (function != null){
	    		functions.add(function);
	    	}
		}
		
		return functions;
	}	

}
