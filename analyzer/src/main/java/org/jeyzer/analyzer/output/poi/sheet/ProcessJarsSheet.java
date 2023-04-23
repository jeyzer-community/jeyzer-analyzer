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
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_TOP_BAR_JEYZER_TITLE;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jeyzer.analyzer.config.report.ConfigProcessJarsSheet;
import org.jeyzer.analyzer.data.jar.ProcessJarVersion;
import org.jeyzer.analyzer.data.jar.ProcessJarVersionType;
import org.jeyzer.analyzer.data.jar.ProcessJars;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.session.JzrSession;

public class ProcessJarsSheet extends JeyzerSheet {
	
	private ConfigProcessJarsSheet sheetCfg;
	
	public ProcessJarsSheet(ConfigProcessJarsSheet processJarsSheetConfig,
			JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = processJarsSheetConfig;
	}

	@Override
	public void display() {
    	int linePos = 1;
    	int rowPos=0;
    	Sheet sheet = createSheet(this.sheetCfg);
		
		ProcessJars processJars = this.session.getProcessJars();
    	
		rowPos = displayHeader(sheet, linePos, rowPos);
    	linePos++;
    	
    	linePos = displayProcessJars(sheet, linePos, processJars);
		
		sheet.setAutoFilter(new CellRangeAddress(1, linePos-1, 0, rowPos - 1));
		sheet.createFreezePane(3, 2); // left and top
		
    	addTopBar(sheet, rowPos);
    	addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 0);
    	
    	close(this.sheetCfg);
	}
	
	private int displayProcessJars(Sheet sheet, int linePos, ProcessJars processJars) {
		for (ProcessJarVersion jarVersion : processJars.getJarVersions()) {
			displayProcessJar(sheet, linePos++, jarVersion);
			this.itemsCount++;
		}
		
		return linePos;
	}
	
	private void displayProcessJar(Sheet sheet, int linePos, ProcessJarVersion jarVersion) {
		int rowPos = 0;
		Row row = sheet.createRow(linePos);
		
		// jar name
		addCell(row, rowPos++, jarVersion.getJarName(), STYLE_CELL);
    	
		// jar version
		if (jarVersion.hasNoVersion())
			addEmptyCell(row, rowPos++);
		else
			addCell(row, rowPos++, jarVersion.getJarVersion(), STYLE_CELL);
		
		// release
		if (jarVersion.isSnapshot())
			addCell(row, rowPos++, "No", STYLE_CELL_ORANGE_BACKGROUND_CENTERED);
		else {
			if (jarVersion.hasNoVersion())
				addCell(row, rowPos++, "?", STYLE_CELL_GREY_CENTERED);
			else
				addCell(row, rowPos++, "Yes", STYLE_CELL_GREEN_BACKGROUND_CENTERED);
		}
		
    	for (ProcessJarVersionType type : ProcessJarVersionType.values()) {
    		String version = jarVersion.getJarVersion(type);
    		if (version == null)
    			addEmptyCell(row, rowPos++);
    		else
    			addCell(row, rowPos++, version, STYLE_CELL);
    	}
    	
    	// Jeyzer repository
    	String repoId = jarVersion.getJeyzerRepositoryId();
    	addCell(row, rowPos++, repoId != null ? repoId : "", STYLE_CELL);
		
		// jar path
		addCell(row, rowPos++, jarVersion.getJarPath(), STYLE_CELL);
	}

	private int displayHeader(Sheet sheet, int linePos, int rowPos) {
    	// Header
    	Row row = sheet.createRow(linePos++);
    	
    	sheet.setColumnWidth(rowPos, 25*256);
    	addHeaderCell(row, rowPos++, "Jar name");

    	sheet.setColumnWidth(rowPos, 20*256);
    	addHeaderCell(row, rowPos++, "Jar version");
    	
    	sheet.setColumnWidth(rowPos, 14*256);
    	addHeaderCell(row, rowPos++, "Release");
    	
    	for (ProcessJarVersionType type : ProcessJarVersionType.values()) {
        	sheet.setColumnWidth(rowPos, 20*256);
        	addHeaderCell(row, rowPos++, type.getAttributeName());
    	}
    	
    	sheet.setColumnWidth(rowPos, 20*256);
    	addHeaderCell(row, rowPos++, "Jeyzer Repository");
    	
    	sheet.setColumnWidth(rowPos, 140*256);
    	addHeaderCell(row, rowPos++, "Jar path");
    	
		return rowPos;
	}
}
