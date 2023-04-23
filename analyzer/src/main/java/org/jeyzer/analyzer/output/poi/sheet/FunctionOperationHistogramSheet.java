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



import static org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture.GraphType.ACTION_GRAPH_SINGLE;
import static org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture.GraphType.CONTENTION_GRAPH_SINGLE;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.*;





import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jeyzer.analyzer.config.report.ConfigFunctionOperationHistogramSheet;
import org.jeyzer.analyzer.data.stack.ThreadStack.ThreadStackHandler;
import org.jeyzer.analyzer.data.tag.Tag;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture;
import org.jeyzer.analyzer.output.graph.picture.GlobalContentionTypeGraphGenerator;
import org.jeyzer.analyzer.output.graph.picture.GlobalStackGraphGenerator;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.session.JzrSession;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class FunctionOperationHistogramSheet extends JeyzerSheet{

	private ConfigFunctionOperationHistogramSheet sheetCfg;
	
	public FunctionOperationHistogramSheet(ConfigFunctionOperationHistogramSheet config, JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = config;
	}

	@Override
	public void display() throws JzrException {
		
    	int linePos = 1;
    	Sheet sheet = createSheet(this.sheetCfg);
    	
    	// Headers
    	int rowPos = displayHeaders(sheet, linePos);

    	sheet.createFreezePane(0, 2);
    	
    	Multiset<Tag> tags = session.getFunctionSet();
    	tags.addAll(session.getOperationSet());
    	tags.addAll(session.getContentionTypeSet());
    	this.itemsCount = tags.elementSet().size();

    	// total number of active stacks
    	int globalActionsStackSize = session.getActionsStackSize();
    	
    	for (Tag tag : Multisets.copyHighestCountFirst(tags).elementSet()) {
   			linePos++;
   			displayTag(tag, tags.count(tag), globalActionsStackSize, sheet, linePos);
    	}

    	sheet.setAutoFilter(new CellRangeAddress(1, linePos, 0, rowPos-1));
    	
    	addTopBar(sheet, rowPos);
    	addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 0);
    	
    	// add global graphs
    	displayGraphs(sheet, linePos, rowPos);
    	
    	close(this.sheetCfg);
	}

	private int displayHeaders(Sheet sheet, int linePos) {
    	Row row = sheet.createRow(linePos);
    	int rowPos=0;
    	
    	sheet.setColumnWidth(rowPos, 50*256);
    	Cell cell = addHeaderCell(row, rowPos++, "Functions");
    	wrapText(cell);

    	sheet.setColumnWidth(rowPos, 27*256);
    	cell = addHeaderCell(row, rowPos++, "Count");
    	wrapText(cell);
        
    	sheet.setColumnWidth(rowPos, 27*256);
    	cell = addHeaderCell(row, rowPos++, "Active stack\r\nglobal appearance %");
    	wrapText(cell);

    	sheet.setColumnWidth(rowPos, 16*256);
    	cell = addHeaderCell(row, rowPos++, "Type");
    	wrapText(cell);
    	
    	return rowPos;
	}

	private void displayTag(Tag tag, int tagCount, int globalActionsStackSize, Sheet sheet, int linePos) {
    	Row row = sheet.createRow(linePos);
    	int rowPos=0;
       
        addCell(row, rowPos++, tag.getName(), getCellStyleReference(linePos));
    	addCell(row, rowPos++, tagCount, getCellStyleNumberReference(linePos));
    	addCell(row, rowPos++, FormulaHelper.percentRound(tagCount, globalActionsStackSize), getCellStyleNumberReference(linePos));
    	addCell(row, rowPos++, tag.getTypeName(), getCellStyleReference(linePos));
	}

	private void displayGraphs(Sheet sheet, int linePos, int rowPos) throws JzrException {
		if (!this.sheetCfg.isFunctionGraphDisplayed() && !this.sheetCfg.isContentionGraphDisplayed())
			return;
		
		linePos += 10;
		rowPos += 10;
		int contentionRowPos = rowPos;
		int contentionLinePos = linePos;
		
		// add function graph
		if (this.sheetCfg.isFunctionGraphDisplayed()){
			GlobalStackGraphGenerator graphGenerator =  new GlobalStackGraphGenerator(
					this.displayContext.getSetupManager().getGraphSetupMgr(),
					this.sheetCfg.getFunctionGraphCfg(),
					this.sheetCfg.getName(),
					this.displayContext.getGraphPictureRepository());
			
			Multiset<ThreadStackHandler> stacks = session.getStackSet();
			for (ThreadStackHandler stack : stacks.elementSet()){
				graphGenerator.addStack(stack, stacks.count(stack));
			}
			
			ExcelGraphPicture picture = graphGenerator.generateGraphPicture();
			
			if (picture != null){
				picture.setParentLinkCell(getValidCell(getValidRow(sheet, 0), 1));
				addGraphPicture(sheet, this.sheetCfg.getName(), picture, linePos, rowPos, ACTION_GRAPH_SINGLE);
				this.graphItemsCount++;
				
				clearGraphPicture(picture);
				
				contentionRowPos += this.sheetCfg.getFunctionGraphCfg().getExcelResolution().getWidth() + 4;  // 2 for the borders, 2 for the separation
			}
		}
		
		// add contention type graph, display on the right
		if (this.sheetCfg.isContentionGraphDisplayed()){
			GlobalContentionTypeGraphGenerator contentionGraphGenerator =  new GlobalContentionTypeGraphGenerator(
					this.displayContext.getSetupManager().getGraphSetupMgr(),
					this.sheetCfg.getContentionGraphCfg(),
					this.sheetCfg.getName(),
					this.displayContext.getGraphPictureRepository());
			
			Multiset<Tag> principalTags = session.getPrincipalContentionTypeSet();
			for (Tag tag : principalTags.elementSet()){
				contentionGraphGenerator.addContentionType(tag, principalTags.count(tag), principalTags.size());
			}
			
			ExcelGraphPicture picture = contentionGraphGenerator.generateGraphPicture();
			
			if (picture != null){
				int linkPos = this.sheetCfg.isFunctionGraphDisplayed() ? 2 : 1;
				picture.setParentLinkCell(getValidCell(getValidRow(sheet, 0), linkPos));
				addGraphPicture(sheet, this.sheetCfg.getName(), picture, contentionLinePos, contentionRowPos, CONTENTION_GRAPH_SINGLE);
				this.graphItemsCount++;
				
				clearGraphPicture(picture);
			}
		}

	}
	
}
