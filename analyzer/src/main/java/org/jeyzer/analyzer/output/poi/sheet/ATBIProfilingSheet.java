package org.jeyzer.analyzer.output.poi.sheet;

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



import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;





import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jeyzer.analyzer.config.report.ConfigATBIProfilingSheet;
import org.jeyzer.analyzer.data.action.ActionGraphSection;
import org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.session.JzrSession;

public class ATBIProfilingSheet extends ActionProfilingSheet {

	private ConfigATBIProfilingSheet sheetCfg;
	
	public ATBIProfilingSheet(ConfigATBIProfilingSheet sheetCfg, JzrSession session, DisplayContext displayContext) {
		super(sheetCfg, session, displayContext);
		this.sheetCfg = sheetCfg;
	}
	
	@Override
	protected int displaySection(Sheet sheet, int firstLine, int linePos, ActionGraphSection section, int actionStackCount, int level, 
			int prevSectionLine, int nextSectionLine, int parentSectionLine, int lastChildSectionLine, 
			Map<String, ExcelGraphPicture> functionGraphPictures, Map<String, ExcelGraphPicture> contentionGraphPictures) {

		// Pre-grouping doesn't work, even if cells are already created
//		int rowEnd = getActionSectionRowEnd(section);
//		sheet.groupRow(firstLine, rowEnd-1);
//		sheet.setRowGroupCollapsed(firstLine, true);
		
		// display code lines first
		boolean atbi = section.isSourceFunctionTagsEmpty() 
				&& section.isSourceOperationTagsEmpty()
				&& section.getSize() >= this.sheetCfg.getSectionSizeThreshold()
				&& section.getStackCount() >= this.sheetCfg.getStackCountThreshold();
		
		if (atbi){
			for (String codeLine : section.getCodeLines())
				displayATBICodeLines(sheet, linePos++, codeLine, section.getStackCount());
		}
		else{
			Iterator<String> functionTag = section.getSourceFunctionTags().iterator();
			Iterator<String> operationTag = section.getSourceOperationTags().iterator();
			Iterator<String> contentionTypeTag = section.getSourceOperationTags().iterator();
			for (String codeLine : section.getCodeLines())
				displayCodeLines(sheet, linePos++, codeLine, functionTag.next(), operationTag.next(), contentionTypeTag.next());
			
		}
		
		// display action line
		displayActionLine(sheet, linePos++, level, section, actionStackCount, 
				prevSectionLine, nextSectionLine, parentSectionLine, lastChildSectionLine, 
				functionGraphPictures, functionGraphPictures);

		// group it (Excel support max 8 levels of grouping)
		if (level<8){
			sheet.groupRow(firstLine, linePos-2);
			sheet.setRowGroupCollapsed(firstLine, true);
		}
		
		return linePos;
	}
	
	private void displayATBICodeLines(Sheet sheet, int pos, String codeLine, int count) {
		int rowPos = 0;
		Row row = sheet.createRow(pos);

		String color;
		if (count <= 5)
			color = "RGB-255-192-0";
		else if (count <= 20)
			color = "RGB-255-102-0";
		else
			color = "RGB-255-0-0";
		
		addEmptyCell(row, rowPos++, color);
		addEmptyCell(row, rowPos++, color);
		addEmptyCell(row, rowPos++, color);
		addEmptyCell(row, rowPos++, color);
		addEmptyCell(row, rowPos++, color);
		addEmptyCell(row, rowPos++, color);
		
		if(this.sheetCfg.isFunctionGraphDisplayed())
			addEmptyCell(row, rowPos++, color);  // graph picture
		
		addCell(row, rowPos++, codeLine, STYLE_CELL, color);
		addEmptyCell(row, rowPos++, STYLE_CELL, color); // action
		addEmptyCell(row, rowPos++, STYLE_CELL, color); // count
		addEmptyCell(row, rowPos++, STYLE_CELL, color); // global %
		addEmptyCell(row, rowPos++, STYLE_CELL, color); // action%
			
    	if (session.isCPUInfoAvailable()){
    		addEmptyCell(row, rowPos++, color);
    		addEmptyCell(row, rowPos++, color);
    	}
	    	
    	if (session.isMemoryInfoAvailable()){
    		addEmptyCell(row, rowPos++, color);
    		addEmptyCell(row, rowPos++, color);
    	}
	    	
		addEmptyCell(row, rowPos++, STYLE_CELL, color);
		addCell(row, rowPos++, "ATBI", STYLE_CELL, color);
		addEmptyCell(row, rowPos++, STYLE_CELL, color);
	}

}
