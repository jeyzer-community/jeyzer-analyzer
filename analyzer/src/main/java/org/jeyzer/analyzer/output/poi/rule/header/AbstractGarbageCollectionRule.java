package org.jeyzer.analyzer.output.poi.rule.header;

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




import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeader;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.data.gc.GarbageCollection;
import org.jeyzer.analyzer.data.gc.GarbageCollectorInfo;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;

public abstract class AbstractGarbageCollectionRule extends AbstractNumericDisplayHeaderRule{

	// possibles values : last or <gc name>
	private static final String DISPLAY_GC = "gc";
	protected static final String DISPLAY_GC_ALL = "all";	
	protected static final String DISPLAY_GC_LAST = "last";
	protected static final String DISPLAY_GC_OLD = "old";
	protected static final String DISPLAY_GC_YOUNG = "young";
	
	private static final String DISPLAY_NAME = "display";
	
	protected String displayName;
	protected String gc;
	
	public AbstractGarbageCollectionRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
		
		this.displayName = (String)headerCfg.getValue(DISPLAY_NAME);
		this.gc = (String)headerCfg.getValue(DISPLAY_GC);
	}
	
	
	protected GarbageCollectorInfo accessGarbageCollectionInfo(ThreadDump threadDump) {
		GarbageCollection garbageCollectionData = threadDump.getGarbageCollection();
		GarbageCollectorInfo gcInfo = null;
		
		if (garbageCollectionData == null)
			return null;
		
		if (DISPLAY_GC_LAST.equals(gc)){
			gcInfo = garbageCollectionData.getMostRecentGarbageCollectorInfo();
		}else if (DISPLAY_GC_OLD.equals(gc)){
			gcInfo = garbageCollectionData.getOldGarbageCollectorInfo();
		}else if (DISPLAY_GC_YOUNG.equals(gc)){
			gcInfo = garbageCollectionData.getYoungGarbageCollectorInfo();
		}else{
			gcInfo = garbageCollectionData.getGarbageCollectorInfo(gc);
		}
		
		return gcInfo; // can be null
	}	
	
}
