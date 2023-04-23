package org.jeyzer.analyzer.output.poi.context;

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







import org.apache.poi.ss.usermodel.Sheet;
import org.jeyzer.analyzer.output.stats.CollectedStats;

public class SequenceSheetDisplayContext extends SheetDisplayContext{
	private CollectedStats statsModel;
	private int tdPeriod;
	private boolean statsSheetSupport;
    
	public SequenceSheetDisplayContext(DisplayContext context, Sheet sheet, CollectedStats statsModel, int tdPeriod){
		super(context, sheet);
   		this.statsModel = statsModel;
   		this.statsSheetSupport = true;
   		this.tdPeriod = tdPeriod;
   	}
	
	public SequenceSheetDisplayContext(DisplayContext context, Sheet sheet, int tdPeriod){
		super(context, sheet);
   		this.tdPeriod = tdPeriod;
   		this.statsSheetSupport = false;
   	}
	
	public boolean hasStatsSupport() {
		return statsSheetSupport;
	}
    
   	public Object createStats() {
		return new CollectedStats(statsModel);
	}
   	
   	public int getThreadDumpPeriod() {
		return tdPeriod;
	}
}
