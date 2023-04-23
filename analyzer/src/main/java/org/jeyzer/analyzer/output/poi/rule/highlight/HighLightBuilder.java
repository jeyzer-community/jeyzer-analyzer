package org.jeyzer.analyzer.output.poi.rule.highlight;

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
import java.util.Collections;
import java.util.List;

import org.jeyzer.analyzer.config.ConfigAnalyzer;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.output.poi.CellColor;

public class HighLightBuilder {
	
	public static final String COLOR_FIELD = "color";
	public static final String NAME_FIELD = "name";
	public static final String REGEX_FIELD = "regex";
	
	private static final HighLightBuilder builder = new HighLightBuilder();
	
	private HighLightBuilder(){}
	
	public static HighLightBuilder newInstance(){
		return builder;
	}
	
	public List<Highlight> buildHighLights(List<ConfigDisplay> highlightsCfgs){
		List<Highlight> highlights = new ArrayList<>();
		
		if (highlightsCfgs == null)
			return highlights;
		
		for (ConfigDisplay highlightCfg : highlightsCfgs){
			Highlight hl;
			String name = (String)highlightCfg.getValue(NAME_FIELD);
			Object regex = highlightCfg.getValue(REGEX_FIELD);
			Object highlightColor = CellColor.buildColor((String)highlightCfg.getValue(COLOR_FIELD));
			if (regex ==  null){
				hl = new HighlightPreset(name, highlightColor);
			}
			else if (name ==  null){
				if (((String)regex).isEmpty() || ConfigAnalyzer.INVALID_DISCOVERY_REGEX_FIELD.equals(regex)){
					// ignore, means that JEYZER_ANALYZER_DISCOVERY_FUNCTIONS_REGEX_<X> variable was not set or set with empty list
					continue;
				}
				
				hl = new HighlightDiscovery(highlightColor, (String)regex);
			}
			else{
				hl = new HighlightRegex(name, highlightColor, (String)regex);
			}
			highlights.add(hl);
		}
		
		// sort by priority
		Collections.sort(highlights, new Highlight.HighlightComparable());
		
		return highlights;
	}
}
