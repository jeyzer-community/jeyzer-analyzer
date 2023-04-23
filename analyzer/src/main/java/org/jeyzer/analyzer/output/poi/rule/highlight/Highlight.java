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







import java.util.Comparator;
import java.util.List;

public abstract class Highlight {

	public static class HighlightLegendElement {

		String description;
		String label;  // label can be null
		
		public HighlightLegendElement(String description, String label){
			this.description = description;
			this.label = label; // label can be null
		}
		
		public String getDescription() {
			return description;
		}

		public String getLabel() {
			return label;
		}
	}
	
	public static class HighlightComparable implements Comparator<Highlight>{
		 
	    @Override
	    public int compare(Highlight h1, Highlight h2) {
	        return h1.getPriority()>h2.getPriority() ? 1 : h1.getPriority()==h2.getPriority() ? 0 : -1;
	    }
	}		
	
	private Object color;  // color index or RGB

	public Highlight(Object color){
		this.color = color;
	}
	
	public Object getColor() {
		return color;
	}
	
	public abstract String getName();
	
	public abstract int getPriority();
	
	public abstract boolean match(String value);
	
	public abstract List<HighlightLegendElement> getLegendElements();

}
