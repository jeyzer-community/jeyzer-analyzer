package org.jeyzer.analyzer.config.patterns;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.rule.pattern.Pattern;
import org.jeyzer.analyzer.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigAggregatorPatterns implements ConfigPatterns {

	private static final Logger logger = LoggerFactory.getLogger(ConfigAggregatorPatterns.class);	
	
	private List<ConfigSinglePatterns> singlePatternSets = new ArrayList<>(4);
	
	public ConfigAggregatorPatterns(List<String> patternsPaths) throws JzrInitializationException{
		loadConfigurations(patternsPaths);
		
		if (singlePatternSets.isEmpty())
			throw new JzrInitializationException("Analysis patterns configuration loading failed. No valid analysis patterns found.");
	}
	
	@Override
	public Object getValue(String field) {
		boolean merge = false;
		Object value = null;
		
		// for a non-map value, take the first non null value
		// for a map like value, merge all linked hashed maps, keeping the patterns load ordering
		for (ConfigPatterns patternSet : singlePatternSets){
			Object candidate = null;
			candidate = patternSet.getValue(field);
			if (candidate instanceof LinkedHashMap){
				merge = true;
				break;
			}
			value = candidate;
			if (value != null)
				return value; 
		}
		
		if (merge)
			return mergeMaps(field);
		
		if (value == null)
			logger.warn("Analysis pattern configuration value not found for field : " + field);
		
		return value;
	}

	private Map<String, Pattern> mergeMaps(String field){
		Map<String, Pattern> map = new LinkedHashMap<>();
		
		for (ConfigSinglePatterns patternSet : singlePatternSets){
			@SuppressWarnings("unchecked")
			Map<String, Pattern> patternsMap = (LinkedHashMap<String, Pattern>)patternSet.getValue(field);
			
			warnForDuplicates(patternSet, patternsMap, map, field);
			
			if (patternsMap != null)
				map.putAll(patternsMap);
		}
		return map;	
	}
	
	private void warnForDuplicates(ConfigSinglePatterns patternSet, Map<String, Pattern> delta, Map<String, Pattern> global, String field) {
		for (String key : delta.keySet())
			if (global.containsKey(key)) {
				// To prevent the below, multi map should be used, but thinking further, allowing duplicates will confuse the analysis results.
				// It would then mean adding the profile name as a group everywhere. This would complexify the whole stuff.
				//  At last, remember that within the analysis patterns, several patterns (see getPatterns below) can be associated to the same pattern resource. 
				logger.warn("The " + field + " pattern resource named \"" + key + "\" is defined in different analysis patterns. Only the one from lower analysis pattern set \"" + patternSet.getName() + "\" will be retained.  Update the upper analysis pattern set to use different key otherwise you may not get expected analysis results (especially on monitoring rules).");
				logger.warn(" - " + key + " = " + global.get(key).getPatterns() + " will be ignored in the analysis.");
				logger.warn(" - " + key + " = " + delta.get(key).getPatterns() + " will be kept (unless replaced by lower analysis pattern).");
			}
	}

	private void loadConfigurations(List<String> analysisPatternSetPaths) throws JzrInitializationException{
		for (String path : analysisPatternSetPaths){
			logger.info("Loading the analysis patterns from file : " + (SystemHelper.isRemoteProtocol(path) ? path :  SystemHelper.sanitizePathSeparators(path)));
			ConfigSinglePatterns singleProfile = new ConfigSinglePatterns(path, false);
			singlePatternSets.add(singleProfile);
		}
	}
	
	public List<ConfigSinglePatterns> getPatternSets(){
		return this.singlePatternSets;
	}
	
	public ConfigSinglePatterns getMasterPatterns(){
		return this.singlePatternSets.get(0);
	}

	public void loadDynamicPatterns(List<String> paths) {
		for (String path : paths){
			logger.info("Loading dynamically the analysis patterns from file : " + (SystemHelper.isRemoteProtocol(path) ? path :  SystemHelper.sanitizePathSeparators(path)));
			ConfigSinglePatterns singleProfile;
			try {
				singleProfile = new ConfigSinglePatterns(path, true);
			} catch (JzrInitializationException e) {
				logger.warn("Failed to load dynamically the analysis patterns from file : " + (SystemHelper.isRemoteProtocol(path) ? path :  SystemHelper.sanitizePathSeparators(path)));
				continue;
			}
			singlePatternSets.add(singleProfile);
		}
	}
}
