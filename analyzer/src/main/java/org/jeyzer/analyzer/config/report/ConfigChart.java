package org.jeyzer.analyzer.config.report;

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

import org.jeyzer.analyzer.config.ConfigUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.primitives.Doubles;

public class ConfigChart {

	private static final String SERIE = "serie";
	private static final String HEADER = "header";
	private static final String Y_MAX = "y_axis_max";
	private static final String TITLE = "title";
	
	private List<String> series = new ArrayList<>();
	private Double yAxisMax = null;
	private String chartTitle;  // may be null
	
	public ConfigChart(Element configNode){
		NodeList nodes = configNode.getElementsByTagName(SERIE);
		String header;
		String value;
		
		for (int i=0; i<nodes.getLength(); i++){
			header = ConfigUtil.getAttributeValue((Element)nodes.item(i),HEADER);
			if (header != null && !header.isEmpty())
				this.series.add(header);
		}
		
		value = ConfigUtil.getAttributeValue(configNode,Y_MAX);
		if (value != null && !value.isEmpty())
			yAxisMax = Doubles.tryParse(value); // can be null
		
		value = ConfigUtil.getAttributeValue(configNode,TITLE);
		if (value != null && !value.isEmpty())
			chartTitle = value;
	}
	
	public List<String> getSeries(){
		return this.series;
	}

	public Double getYAxisMax(){
		return this.yAxisMax; // can be null
	}
	
	public String getTitle(){
		return this.chartTitle; // can be null
	}
}
