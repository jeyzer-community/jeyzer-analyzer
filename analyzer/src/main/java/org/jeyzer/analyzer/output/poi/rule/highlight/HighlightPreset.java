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

import org.jeyzer.analyzer.data.stack.ThreadStack;

public class HighlightPreset extends Highlight{
	
	public static final int HIGHLIGHT_PRIORITY = 1;	
	
	public static final String ATBI_LEGEND = "Action to be identified";
	public static final String OTBI_LEGEND = "Operation to be identified";
	
	private String name;
	
	public HighlightPreset(String name, Object highlightColor) {
		super(highlightColor);
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public int getPriority(){
		return HIGHLIGHT_PRIORITY;
	}	

	@Override
	public boolean match(String value){
		return name.equals(value);
	}
	
	@Override
	public List<HighlightLegendElement> getLegendElements(){
		List<HighlightLegendElement> legendElements = new ArrayList<>(1);

		String label = null;
		if (ThreadStack.FUNC_TO_BE_IDENTIFIED.equals(this.name))
			label = ThreadStack.FUNC_TO_BE_IDENTIFIED;
		else if (ThreadStack.OPER_TO_BE_IDENTIFIED.equals(this.name))
			label = ThreadStack.OPER_TO_BE_IDENTIFIED;
		
		String description;
		if (ThreadStack.FUNC_TO_BE_IDENTIFIED.equals(this.name))
			description= ATBI_LEGEND;
		else if (ThreadStack.OPER_TO_BE_IDENTIFIED.equals(this.name))
			description = OTBI_LEGEND;
		else
			description = name;
		
		legendElements.add(new HighlightLegendElement(description, label));
		
		return legendElements;
	}	
	
}
