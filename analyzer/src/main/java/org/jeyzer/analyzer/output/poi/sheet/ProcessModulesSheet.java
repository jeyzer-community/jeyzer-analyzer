package org.jeyzer.analyzer.output.poi.sheet;

import static org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture.GraphType.ACTION_GRAPH_SINGLE;

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
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_TOP_BAR_JEYZER_TITLE;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jeyzer.analyzer.config.report.ConfigProcessModulesSheet;
import org.jeyzer.analyzer.data.module.ProcessModule;
import org.jeyzer.analyzer.data.module.ProcessModules;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture;
import org.jeyzer.analyzer.output.graph.picture.ModuleGraphGenerator;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.util.AnalyzerHelper;

public class ProcessModulesSheet extends JeyzerSheet {
	
	private ConfigProcessModulesSheet sheetCfg;
	
	public ProcessModulesSheet(ConfigProcessModulesSheet processModulesSheetConfig,
			JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = processModulesSheetConfig;
	}

	@Override
	public void display() throws JzrException {
    	int linePos = 1;
    	int rowPos=0;
    	Sheet sheet = createSheet(this.sheetCfg);
		
		ProcessModules processModules = this.session.getProcessModules();
    	
		rowPos = displayHeader(sheet, linePos, rowPos);
    	linePos++;
    	
    	linePos = displayProcessModules(sheet, linePos, processModules);
		
		sheet.setAutoFilter(new CellRangeAddress(1, linePos-1, 0, rowPos - 1));
		sheet.createFreezePane(3, 2); // left and top
		
    	addTopBar(sheet, rowPos);
    	addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 0);
    	
    	displayGraph(sheet, linePos, rowPos, processModules);
    	
    	close(this.sheetCfg);
	}
	
	private int displayProcessModules(Sheet sheet, int linePos, ProcessModules modules) {
		for (ProcessModule module : modules.getProcessModules()) {
			displayProcessModule(sheet, linePos++, module);
			this.itemsCount++;
		}
		
		return linePos;
	}
	
	private void displayProcessModule(Sheet sheet, int linePos, ProcessModule module) {
		int rowPos = 0;
		Row row = sheet.createRow(linePos);
		
		// module name
		addCell(row, rowPos++, module.getName(), STYLE_CELL_CENTERED_WRAPPED);
    	
		// module version
		if (module.hasNoVersion())
			addEmptyCell(row, rowPos++);
		else
			addCell(row, rowPos++, module.getVersion(), STYLE_CELL_CENTERED_WRAPPED);
		
		// release
		if (module.isSnapshot())
			addCell(row, rowPos++, "No", STYLE_CELL_ORANGE_BACKGROUND_CENTERED);
		else {
			if (module.hasNoVersion())
				addCell(row, rowPos++, "?", STYLE_CELL_GREY_CENTERED);
			else
				addCell(row, rowPos++, "Yes", STYLE_CELL_GREEN_BACKGROUND_CENTERED);
		}
		
		// open
		addCell(row, rowPos++, module.isOpen() ? "true" : "false", STYLE_CELL_CENTERED_WRAPPED);
		
		// automatic
		addCell(row, rowPos++, module.isAutomatic() ? "true" : "false", STYLE_CELL_CENTERED_WRAPPED);
		
		// JEYZ-96 : secure the cell display (exports could be huge)
		
		// requires
		addCell(row, rowPos++, secureCellDisplayValue(AnalyzerHelper.getListAsString(module.getRequires())), STYLE_CELL_CENTERED_WRAPPED);
		
		// exports
		addCell(row, rowPos++, secureCellDisplayValue(AnalyzerHelper.getListAsString(module.getExports())), STYLE_CELL_SMALL_TEXT_WRAPPED);
    	
		// uses
		addCell(row, rowPos++, secureCellDisplayValue(AnalyzerHelper.getListAsString(module.getUses())), STYLE_CELL_SMALL_TEXT_WRAPPED);
		
		// provides
		addCell(row, rowPos++, secureCellDisplayValue(AnalyzerHelper.getListAsString(module.getProvides())), STYLE_CELL_SMALL_TEXT_WRAPPED);
		
		// class loader
		addCell(row, rowPos++, module.getClassLoader() != null ? module.getClassLoader() : "", STYLE_CELL_CENTERED_WRAPPED);
	}

	private int displayHeader(Sheet sheet, int linePos, int rowPos) {
    	// Header
    	Row row = sheet.createRow(linePos++);
    	
    	sheet.setColumnWidth(rowPos, 25*256);
    	addHeaderCell(row, rowPos++, "Module name");

    	sheet.setColumnWidth(rowPos, 20*256);
    	addHeaderCell(row, rowPos++, "Module version");
    	
    	sheet.setColumnWidth(rowPos, 14*256);
    	addHeaderCell(row, rowPos++, "Release");
    	
    	sheet.setColumnWidth(rowPos, 14*256);
    	addHeaderCell(row, rowPos++, "Open");
    	
    	sheet.setColumnWidth(rowPos, 14*256);
    	addHeaderCell(row, rowPos++, "Automatic");
    	
    	sheet.setColumnWidth(rowPos, 50*256);
    	addHeaderCell(row, rowPos++, "Requires");
    	
    	sheet.setColumnWidth(rowPos, 75*256);
    	addHeaderCell(row, rowPos++, "Exports");
    	
    	sheet.setColumnWidth(rowPos, 50*256);
    	addHeaderCell(row, rowPos++, "Uses");
    	
    	sheet.setColumnWidth(rowPos, 50*256);
    	addHeaderCell(row, rowPos++, "Provides");
    	
    	sheet.setColumnWidth(rowPos, 50*256);
    	addHeaderCell(row, rowPos++, "Class loader");
    	
		return rowPos;
	}
	
	private void displayGraph(Sheet sheet, int linePos, int rowPos, ProcessModules modules) throws JzrException {
		if (!this.sheetCfg.isModuleGraphDisplayed())
			return;
		
		linePos += 10;
		rowPos += 10;
		
		// add module graph
		ModuleGraphGenerator graphGenerator =  new ModuleGraphGenerator(
					this.displayContext.getSetupManager().getGraphSetupMgr(),
					this.sheetCfg.getModuleGraphCfg(),
					this.sheetCfg.getName(),
					this.displayContext.getGraphPictureRepository());
			
		ExcelGraphPicture picture = graphGenerator.generateGraphPicture(this.sheetCfg.getName(), modules.getProcessModules());
			
		if (picture != null){
			picture.setParentLinkCell(getValidCell(getValidRow(sheet, 0), 1));
			addGraphPicture(sheet, this.sheetCfg.getName(), picture, linePos, rowPos, ACTION_GRAPH_SINGLE);
			this.graphItemsCount++;
				
			clearGraphPicture(picture);
		}
	}
}
