package org.jeyzer.analyzer.output.poi.rule.monitor.cell;

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




import static org.jeyzer.monitor.engine.event.MonitorEvent.PARAM_THREAD_VALUE_INDEX;

import java.util.Iterator;
import java.util.List;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.util.CellReference;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.output.poi.CellRefRepository;
import org.jeyzer.analyzer.output.poi.context.SequenceSheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.cell.AbstractCellDisplayRule;
import org.jeyzer.monitor.engine.event.MonitorEvent;
import org.jeyzer.monitor.engine.event.MonitorSessionEvent;
import org.jeyzer.monitor.engine.event.MonitorTaskEvent;

public class EventLinkRule extends AbstractCellDisplayRule implements MonitorDisplayRule {

	public static final String RULE_NAME = "event_link";	
	
	public EventLinkRule(ConfigDisplay displayCfg, SequenceSheetDisplayContext context) {
		super(displayCfg, context);
	}

	@Override
	public boolean apply(MonitorEvent event, List<Cell> cells) {
		Cell cell;
		
		// cell value
		for (int j=0; j< cells.size(); j++){
			cell = cells.get(j);
			
			if (j % 10 == 0){
				setLink(event, cell);
				continue;
			}
		}
		
		return true;
	}

	private void setLink(MonitorEvent event, Cell cell) {
		CellRefRepository cellRefRepository = this.context.getCellRefRepository();
		
		// link the first available
		Iterator<String> sheetTypeIter = cellRefRepository.getSheetTypes().iterator();
		
		if (!sheetTypeIter.hasNext())
			return;
		
		String sheetType = sheetTypeIter.next();
		
   		if (event instanceof MonitorTaskEvent){
   			CellReference cellRef = cellRefRepository.getCellRef(
   					sheetType, 
   					event.getPrintableParameters().get(PARAM_THREAD_VALUE_INDEX), 
   					event.getStartDate());
   			if (cellRef!=null)
   				addDocumentHyperLink(cell, cellRef.formatAsString());
   		}
   		else if(event instanceof MonitorSessionEvent){
   			CellReference cellRef = cellRefRepository.getRefColumn(
   					sheetType, 
   					event.getStartDate());
   			if (cellRef!=null)
   				addDocumentHyperLink(cell, cellRef.formatAsString());   			
   		}
	}

   	private void addDocumentHyperLink(Cell cell, String docref){
   		CreationHelper factory = this.context.getWorkbook().getCreationHelper();
   	    Hyperlink link = factory.createHyperlink(HyperlinkType.DOCUMENT);
   	    link.setAddress(docref);
   	    cell.setHyperlink(link);
   	}	   
   			
	@Override
	public boolean hasStats() {
		return false;
	}

	@Override
	public String getName() {
		return RULE_NAME;
	}

}
