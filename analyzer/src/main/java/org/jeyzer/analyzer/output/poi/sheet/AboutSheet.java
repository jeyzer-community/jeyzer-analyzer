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
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_BACKGROUND_FRAME;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_INNER_FRAME;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_JEYZER_ABOUT_TITLE;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_JEYZER_TITLE;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_MAIN_FRAME;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_SECTION_ABOUT_CATEGORY;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeyzer.analyzer.config.ConfigAnalyzer;
import org.jeyzer.analyzer.config.report.ConfigAboutSheet;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.output.poi.context.DisplayContext.MenuItemsContext;
import org.jeyzer.analyzer.output.poi.style.CellFonts;
import org.jeyzer.analyzer.session.JzrSession;

public class AboutSheet extends JeyzerSheet {
	
	public static final String SHEET_NAME = "About";
	public static final String SHEET_TITLE = "About     Jeyzer";
	
	private static final String URL_HOME				= "https://jeyzer.org";
	private static final String URL_DOWNLOAD 		  	= "https://jeyzer.org/download";
	private static final String URL_DOCUMENTATION      	= "https://jeyzer.org/docs/jzr-report";
	private static final String URL_COMMUNITY 		  	= "https://jeyzer.org/contribute-and-share";
	private static final String URL_SERVICES 		  	= "https://jeyzer.org/services";
	
	private static final String EMAIL_INFO			  	= "contact@jeyzer.org";
	
	
	private static final int ICON_COLUMN_INDEX = 2;
	private static final int ICON_ROW_INDEX = 1;
	
	private enum FieldStatus { 
		NORMAL(STYLE_NAV_ABOUT_FIELD_VALUE), 
		WARNING(STYLE_ABOUT_FIELD_WARNING_VALUE), 
		ERROR(STYLE_ABOUT_FIELD_ERROR_VALUE);
		
		private String style;
		
		FieldStatus(String style){
			this.style = style;
		}
		
		String getStyle(){
			return this.style;
		}
	}
	
	private ConfigAboutSheet sheetCfg;
	
	public AboutSheet(ConfigAboutSheet sheetCfg, JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = sheetCfg;
	}
	
	@Override
	public void display() {
    	Sheet sheet = createSheet(this.sheetCfg, SHEET_NAME);
    	prepareSheet(sheet);
    	sheet.protectSheet(this.displayContext.getMagicWord());
    	this.itemsCount = MenuItemsContext.NO_ITEMS;
		
    	sheet.setColumnWidth(0, 3*256);
    	sheet.setColumnWidth(1, 6*256);
    	sheet.setColumnWidth(2, 25*256);
    	sheet.setColumnWidth(3, 100*256);
    	sheet.setColumnWidth(4, 255*256); // max
    	
    	Row row0 = sheet.createRow(0);
    	row0.setHeightInPoints(21);
    	row0.setRowStyle(getThemeStyle(STYLE_THEME_MAIN_FRAME));

    	displayAboutTitle(sheet);
	
		int linePos=2;
		linePos = displayJeyzerSection(sheet, linePos);
		linePos = displayBottomSection(sheet, linePos);
		
		addMenuLink(sheet, sheetCfg, STYLE_THEME_JEYZER_TITLE, 0, 1);
		
		close(this.sheetCfg);
	}

	private void displayAboutTitle(Sheet sheet) {
    	// page title
    	Row row1 = sheet.createRow(1);
    	row1.setHeightInPoints(70);
    	
    	Cell cell = row1.createCell(0);
    	cell.setCellStyle(getThemeStyle(STYLE_THEME_MAIN_FRAME));
    	
    	cell = row1.createCell(1);
    	cell.setCellStyle(getThemeStyle(STYLE_THEME_INNER_FRAME));
    	
    	XSSFRichTextString richText = new XSSFRichTextString(SHEET_TITLE); 
    	XSSFFont highCellFont = ((XSSFWorkbook)workbook).createFont();
        highCellFont.setFontHeightInPoints((short)36);
        highCellFont.setFontName(CellFonts.FONT_MYRIAD_PRO);
        highCellFont.setColor(getThemeTitleColor());
        XSSFFont smallCellFont = ((XSSFWorkbook)workbook).createFont();
        smallCellFont.setFontHeightInPoints((short)26);
        smallCellFont.setColor(getThemeTitleColor());
        smallCellFont.setFontName(CellFonts.FONT_MYRIAD_PRO);
    	richText.applyFont(0, 1, highCellFont);
    	richText.applyFont(1, 5, smallCellFont);
    	richText.applyFont(5, SHEET_TITLE.length(), highCellFont);
    	
    	cell = addCell(row1, 2, richText);
    	cell.setCellStyle(getThemeStyle(STYLE_THEME_JEYZER_ABOUT_TITLE));
		
    	addPicture(
    			sheet, 
    			this.displayContext.getSetupManager().getIconFilePath(),
    			-1,
    			ICON_COLUMN_INDEX,
    			ICON_ROW_INDEX, 
    			((double)0.8),
    			0.7,
    			85,
    			20
    			);
    	
    	cell = row1.createCell(3);
    	cell.setCellStyle(getThemeStyle(STYLE_THEME_INNER_FRAME));
    	
    	cell = row1.createCell(4);
    	cell.setCellStyle(getThemeStyle(STYLE_THEME_INNER_FRAME));
	}
	
	private int displayJeyzerSection(Sheet sheet, int linePos) {
		displayCategoryHeader(sheet, "Jeyzer", "", "", "", linePos++);
		
		addField(sheet, "Version", System.getProperty(ConfigAnalyzer.JEYZER_ANALYZER_VERSION) + " - Vinci Edition", linePos++);
		Cell cell = addField(sheet, "Documentation", "JZR reports", linePos++);
		this.addDocumentHyperLinkURL(cell, URL_DOCUMENTATION);
		cell = addField(sheet, "Download", "Latest Jeyzer distribution", linePos++);
		this.addDocumentHyperLinkURL(cell, URL_DOWNLOAD);
//		addField(sheet, "Blog", "https://jeyzer.org/blog", linePos++); 
		cell = addField(sheet, "Community", "Jeyzer community", linePos++);
		this.addDocumentHyperLinkURL(cell, URL_COMMUNITY);
		cell = addField(sheet, "Support", "Jeyzer services", linePos++);
		this.addDocumentHyperLinkURL(cell, URL_SERVICES);
		addField(sheet, "Contact", EMAIL_INFO, linePos++);
		addField(sheet, "Powered by", "Apache POI\nVaadin\nGraphStream", linePos++);
		cell = addField(sheet, "Copyright", "(c) Copyright Jeyzer 2020-2023.  All rights reserved.", linePos++);
		this.addDocumentHyperLinkURL(cell, URL_HOME);

		return linePos;
	}

	private void displayCategoryHeader(Sheet sheet, String name2, String name3, String name4, String name5, int rowLine) {
		int col = 0;
		Row row = sheet.createRow(rowLine);
    	short height = 600;
    	row.setHeight(height);
    	
		Cell cell = row.createCell(col++);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_MAIN_FRAME));
		cell = row.createCell(col++);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_INNER_FRAME));
    	addHeaderCell(row, col++, name2, STYLE_THEME_SECTION_ABOUT_CATEGORY);
    	addHeaderCell(row, col++, name3, STYLE_THEME_SECTION_ABOUT_CATEGORY);
    	addHeaderCell(row, col++, name4, STYLE_THEME_BACKGROUND_FRAME);
	}

	private Cell addField(Sheet sheet, String name, String value, int i) {
		return addField(sheet, name, value, i, FieldStatus.NORMAL);
	}
	
	private Cell addField(Sheet sheet, String name, String value, int i, FieldStatus status) {
		int col = 0;
		Row row = sheet.createRow(i);
		row.setHeightInPoints(35);
		
		Cell cell = row.createCell(col++);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_MAIN_FRAME));
		cell = row.createCell(col++);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_INNER_FRAME));
			
		cell = addCell(row, col++, name);
		cell.setCellStyle(getStyle(STYLE_NAV_ABOUT_FIELD_NAME));
			
		Cell cellValue = addCell(row, col++, value);
		cellValue.setCellStyle(getStyle(status.getStyle()));
			
		cell = row.createCell(col++);
    	cell.setCellStyle(getThemeStyle(STYLE_THEME_BACKGROUND_FRAME));
    	
    	return cellValue;
	}
	
	private int displayBottomSection(Sheet sheet, int linePos) {
		// Bottom row. Set it very large
		Row rowBottom = sheet.createRow(linePos);
		rowBottom.setHeightInPoints(300);
		
		Cell cell = rowBottom.createCell(0);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_MAIN_FRAME));
		cell = rowBottom.createCell(1);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_INNER_FRAME));
		cell = rowBottom.createCell(2);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_BACKGROUND_FRAME));
		cell = rowBottom.createCell(3);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_BACKGROUND_FRAME));
		cell = rowBottom.createCell(4);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_BACKGROUND_FRAME));

		return linePos;
	}
	
}
