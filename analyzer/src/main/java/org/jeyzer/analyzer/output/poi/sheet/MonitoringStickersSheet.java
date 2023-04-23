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

import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jeyzer.analyzer.config.report.ConfigMonitoringStickersSheet;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.output.poi.context.MonitoringRepository;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.monitor.config.sticker.ConfigProcessJarVersionSticker;
import org.jeyzer.monitor.config.sticker.ConfigProcessModuleVersionSticker;
import org.jeyzer.monitor.config.sticker.ConfigPropertyCardSticker;
import org.jeyzer.monitor.sticker.Sticker;

public class MonitoringStickersSheet extends JeyzerSheet {

	private ConfigMonitoringStickersSheet sheetCfg;
	
	public MonitoringStickersSheet(ConfigMonitoringStickersSheet config, JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = config;
	}

	@Override
	public void display() throws JzrException {
    	int linePos = 1;
    	int rowPos=0;
    	Sheet sheet = createSheet(this.sheetCfg);
    	
    	rowPos = displayHeader(sheet, linePos, rowPos);
    	linePos++;
    	
    	linePos = displayStickers(sheet, linePos);
		
		sheet.setAutoFilter(new CellRangeAddress(1, linePos-1, 0, rowPos - 1));
		sheet.createFreezePane(3, 2); // left and top
		
    	addTopBar(sheet, rowPos);
    	addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 0);
    	
    	close(this.sheetCfg);
	}

	private int displayStickers(Sheet sheet, int linePos) {
    	List<Sticker> stickers = buildStickers();
		
		for (Sticker sticker : stickers){
			displaySticker(sheet, linePos++, sticker);
			this.itemsCount++;
		}
		
		return linePos;
	}
	
	private List<Sticker> buildStickers() {
		MonitoringRepository repository = this.displayContext.getMonitoringRepository();		
		return repository.getStickerList(
				this.sheetCfg.getConfigStickers(), 
				this.session,
				this.sheetCfg.getJzrResolver(),
				this.displayContext.getSetupManager().getMonitorSetupManager());
	}

	private void displaySticker(Sheet sheet, int linePos, Sticker sticker) {
		int rowPos = 0;
		Row row = sheet.createRow(linePos);
		
		// Positive match
		displayMatch(row, rowPos++, sticker, false);
		
		// Negative match (! operator)
		displayMatch(row, rowPos++, sticker, true);
		
		// sticker name
		addCell(row, rowPos++, sticker.getConfigSticker().getName(), STYLE_CELL_CENTERED_WRAPPED);
		
		// sticker full name
		addCell(row, rowPos++, sticker.getConfigSticker().getFullName(), STYLE_CELL_CENTERED_WRAPPED);
		
		// sticker group
		addCell(row, rowPos++, sticker.getConfigSticker().getGroup(), STYLE_CELL_CENTERED_WRAPPED);
		
		// sticker type
		addCell(row, rowPos++, sticker.getConfigSticker().getType(), STYLE_CELL_CENTERED_WRAPPED);
		
		// sticker appliance
		addCell(row, rowPos++, (sticker.getConfigSticker().isStrictAppliance() ? "Strict" : "Lazy"), STYLE_CELL_CENTERED_WRAPPED);
		
		// sticker dynamic
		addCell(row, rowPos++, (sticker.getConfigSticker().isDynamic() ? "Dynamic" : "Static"), STYLE_CELL_CENTERED_WRAPPED);
		
		// sticker property (opt)
		displayProperty(row, rowPos++, sticker);
		
		// sticker property (pattern)
		displayPattern(row, rowPos++, sticker);
	}

	private void displayProperty(Row row, int rowPos, Sticker sticker) {
		if (sticker.getConfigSticker() instanceof ConfigPropertyCardSticker){
			ConfigPropertyCardSticker cfg = (ConfigPropertyCardSticker)sticker.getConfigSticker();
			addCell(row, rowPos++, cfg.getProperty(), STYLE_CELL_CENTERED_WRAPPED);
		}
		else if (sticker.getConfigSticker() instanceof ConfigProcessJarVersionSticker){
			ConfigProcessJarVersionSticker cfg = (ConfigProcessJarVersionSticker)sticker.getConfigSticker();
			addCell(row, rowPos++, cfg.getJarName(), STYLE_CELL_CENTERED_WRAPPED);
		}
		else if (sticker.getConfigSticker() instanceof ConfigProcessModuleVersionSticker){
			ConfigProcessModuleVersionSticker cfg = (ConfigProcessModuleVersionSticker)sticker.getConfigSticker();
			addCell(row, rowPos++, cfg.getModuleName(), STYLE_CELL_CENTERED_WRAPPED);
		}
		else
			addEmptyCell(row, rowPos);
	}
	
	private void displayPattern(Row row, int rowPos, Sticker sticker) {
		if (sticker.getConfigSticker() instanceof ConfigPropertyCardSticker){
			ConfigPropertyCardSticker cfg = (ConfigPropertyCardSticker)sticker.getConfigSticker();
			addCell(row, rowPos++, cfg.getPattern().toString(), STYLE_CELL_CENTERED_WRAPPED);
		}
		else if (sticker.getConfigSticker() instanceof ConfigProcessJarVersionSticker){
			ConfigProcessJarVersionSticker cfg = (ConfigProcessJarVersionSticker)sticker.getConfigSticker();
			addCell(row, rowPos++, cfg.getPattern().toString(), STYLE_CELL_CENTERED_WRAPPED);
		}
		else if (sticker.getConfigSticker() instanceof ConfigProcessModuleVersionSticker){
			ConfigProcessModuleVersionSticker cfg = (ConfigProcessModuleVersionSticker)sticker.getConfigSticker();
			addCell(row, rowPos++, cfg.getPattern().toString(), STYLE_CELL_CENTERED_WRAPPED);
		}
		else
			addEmptyCell(row, rowPos);
	}

	private void displayMatch(Row row, int rowPos, Sticker sticker, boolean negative) {
		boolean match = sticker.match(this.session, negative);
		addCell(row, rowPos, 
				match ? "Y" : "N", 
				match ? STYLE_CELL_CENTERED  : STYLE_CELL_GREY_SHADOW);
	}

	private int displayHeader(Sheet sheet, int linePos, int rowPos) {
    	// Header
    	Row row = sheet.createRow(linePos++);

    	sheet.setColumnWidth(rowPos, 8*256);
    	addHeaderCell(row, rowPos++, "Match");
    	
    	sheet.setColumnWidth(rowPos, 8*256);
    	addHeaderCell(row, rowPos++, "Neg Match");    	
    	
    	sheet.setColumnWidth(rowPos, 30*256);
    	addHeaderCell(row, rowPos++, "Sticker name");

    	sheet.setColumnWidth(rowPos, 35*256);
    	addHeaderCell(row, rowPos++, "Full name");
    	
    	sheet.setColumnWidth(rowPos, 25*256);
    	addHeaderCell(row, rowPos++, "Group");    	
    	
    	sheet.setColumnWidth(rowPos, 30*256);
    	addHeaderCell(row, rowPos++, "Type");
    	
    	sheet.setColumnWidth(rowPos, 10*256);
    	addHeaderCell(row, rowPos++, "Appliance");
    	
    	sheet.setColumnWidth(rowPos, 13*256);
    	addHeaderCell(row, rowPos++, "Loading");
    	
    	sheet.setColumnWidth(rowPos, 30*256);
    	addHeaderCell(row, rowPos++, "Property or Jar name");
    	
    	sheet.setColumnWidth(rowPos, 30*256);
    	addHeaderCell(row, rowPos++, "Pattern");
    	    	
		return rowPos;
	}
}
