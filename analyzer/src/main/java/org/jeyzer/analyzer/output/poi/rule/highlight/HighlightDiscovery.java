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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jeyzer.analyzer.data.stack.ThreadStack;

public class HighlightDiscovery extends Highlight{

	public static final int HIGHLIGHT_PRIORITY = 3;
	
	public static final String DISCOVERY_LABEL = "d";
	
	private Pattern regex;
	private List<String> names = new ArrayList<>();
	
	public HighlightDiscovery(Object color, String pattern){
		super(color);
		this.regex = Pattern.compile(pattern);
	}

	@Override
	public String getName() {
		StringBuilder name = new StringBuilder();
		for (String s : names)
		{
			name.append(s);
			name.append("\t");
		}
		return name.toString();
	}
	
	@Override
	public int getPriority(){
		return HIGHLIGHT_PRIORITY;
	}
	
	@Override
	public boolean match(String value){
		if (ThreadStack.FUNC_TO_BE_IDENTIFIED.equals(value) || ThreadStack.OPER_TO_BE_IDENTIFIED.equals(value))
			return false; // ignore

		Matcher matcher = regex.matcher(value);
		boolean found = matcher.find();
		if (found && !names.contains(value))
			names.add(value);
		
		return found;
	}
	
	@Override
	public List<HighlightLegendElement> getLegendElements(){
		List<HighlightLegendElement> legendElements = new ArrayList<>(names.size());		
		
		for (String name : names){
			legendElements.add(new HighlightLegendElement(name, DISCOVERY_LABEL));
		}
		
		return legendElements;
	}
	
}
