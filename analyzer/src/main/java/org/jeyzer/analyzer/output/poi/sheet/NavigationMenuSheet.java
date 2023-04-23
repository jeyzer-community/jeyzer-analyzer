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
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FontUnderline;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeyzer.analyzer.config.report.ConfigNavigationMenuSheet;
import org.jeyzer.analyzer.config.report.ConfigSheet;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.output.poi.context.DisplayContext.MenuItemsContext;
import org.jeyzer.analyzer.output.poi.style.CellFonts;
import org.jeyzer.analyzer.session.JzrSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavigationMenuSheet extends JeyzerSheet{
	
	protected static final Logger logger = LoggerFactory.getLogger(NavigationMenuSheet.class);
	
	public static final String SHEET_NAME = "Menu";
	
	private static final String HELPER_NOTICE = "This JZR report has been generated with an application agnostic profile with standard shared profiles (Java, Log4j..).\n"
			+ "If you need free assistance, please send us an email with the current report at jzr-support@jeyzer.org";
	
	private static final List<String> helperProfiles = Arrays.asList("Portal", "Profile-Tuning", "Discovery");
	
	private static final int PICTURE_ROW_SIZE = 10;
	private static final int LOGO_COLUMN_INDEX = 5;
	private static final int LOGO_ROW_INDEX = 2;
	private static final int ICON_COLUMN_INDEX = 2;
	private static final int ICON_ROW_INDEX = 1;
	
	private static final String[] QUOTES = {
		"A new monitoring method",
		"System parallelization failure leads often to paralyzation success",
		"Where there is shouting, there is no true knowledge",
		"The noblest pleasure is the joy of understanding",
		"All our knowledge has its origin in our perceptions",
		"Every instrument requires to be made by experience",
		"As a day well spent procures a happy sleep, so a life well employed procures a happy death",
		"The entire method consists in the order and arrangement of the things to which the mind's eye must turn so that we can discover some truth",
		"And in a way I'm yearning to be done with all this measuring of truth",
		"It is sometimes difficult to avoid the impression that there is a sort of foreknowledge of the coming series of events.",
		"A fact acquires its true and full value only through the idea which is developed from it.",
		"Nothing shocks me. I'm a scientist.",
		"The important thing in science is not so much to obtain new facts as to discover new ways of thinking about them.",
		"I was not predicting the future, I was trying to prevent it.",
		"The future is like a corridor into which we can see only by the light coming from behind.",
		"The best way to predict the future is to invent it.",
		"A problem never exists in isolation; it is surrounded by other problems in space and time.",
		"Leave no stone unturned.",
		"The art of simplicity is a puzzle of complexity.",
		"Questions don't change the truth. But they give it motion.",
		"Once you eliminate the impossible, whatever remains, no matter how improbable, must be the truth.",
		"Believe those who seek the truth, doubt those who find it; doubt all, but do not doubt yourself.",
		"To see what is in front of one's nose needs a constant struggle.",
		"Tell the truth, then run.",
		"My major preoccupation is the question, 'What is reality?'",
		"The crisis of today is the joke of tomorrow.",
		"Without data, you're just another person with an opinion.",
		"It's not enough to do your best; you must know what to do and then do your best.",
		"If you define the problem correctly, you almost have the solution.",
		"What is simple is false. What is complicated is unusable.",
		"Better to prevent than to cure",
		"Don't struggle in vain for things that do not matter",
		"What is clear and accurately expressed is wise",
		"Making the same mistake twice is not a trait of wise man",
		"Do not make any conclusions before the end",
		"Ignorance is the root and stem for all evil"
		};
	
	private static final String[] QUOTE_AUTHOR = {
		"Jeyzer Team",
		"Jeyzer Team",
		"Leonardo Da Vinci",
		"Leonardo Da Vinci",
		"Leonardo Da Vinci",
		"Leonardo Da Vinci",
		"Leonardo Da Vinci",
		"Ren� Descartes",
		"Nick Cave",
		"Carl Jung",
		"Justus Von Liebig",
		"Harrison Ford",
		"Sir William Bragg",
		"Ray Bradbury",
		"Edward Weyer Jr",
		"Alan Kay",
		"Russell L. Ackoff",
		"Euripides",
		"Douglas Horton",
		"Giannina Braschi",
		"Sherlock Holmes",
		"Andr� Gide",
		"George Orwell",
		"Yugoslavian proverb",
		"Philip K. Dick",
		"H. G. Wells",
		"W. Edwards Deming",
		"W. Edwards Deming",
		"Steve Jobs",
		"Paul Valery",
		"Socrate",
		"Aeschylus",
		"Euripides",
		"Menander",
		"Solon",
		"Plato"
		};
	
	private ConfigNavigationMenuSheet sheetCfg;
	
	public NavigationMenuSheet(ConfigNavigationMenuSheet sheetCfg, JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = sheetCfg;
	}

	@Override
	public void display() {
		Sheet sheet = createSheet(this.sheetCfg, SHEET_NAME);
    	prepareSheet(sheet);
    	sheet.protectSheet(this.displayContext.getMagicWord());
    	
    	sheet.setColumnWidth(0, 3*256);
    	sheet.setColumnWidth(1, 6*256);
    	sheet.setColumnWidth(2, 25*256);
    	sheet.setColumnWidth(3, 100*256);
    	sheet.setColumnWidth(4, 7*256);
    	sheet.setColumnWidth(5, 255*256); // max
    	
    	Row row0 = sheet.createRow(0);
    	row0.setHeightInPoints(21);
    	row0.setRowStyle(getThemeStyle(STYLE_THEME_MAIN_FRAME));
    	
    	// page title
    	Row row1 = sheet.createRow(1);
    	row1.setHeightInPoints(70);
    	
    	Cell cell = row1.createCell(0);
    	cell.setCellStyle(getThemeStyle(STYLE_THEME_MAIN_FRAME));
    	
    	cell = addCell(row1, 1, "JZR report");
    	cell.setCellStyle(getThemeStyle(STYLE_THEME_JZR_REPORT_TITLE));
    	cell.setAsActiveCell();
    	
    	cell = addCell(row1, 2, "    Jeyzer");
    	cell.setCellStyle(getThemeStyle(STYLE_THEME_JEYZER_MAIN_TITLE));
		
    	cell = row1.createCell(3);
    	cell.setCellStyle(getThemeStyle(STYLE_THEME_INNER_FRAME));
    	
    	cell = row1.createCell(4);
    	cell.setCellStyle(getThemeStyle(STYLE_THEME_INNER_FRAME_ITEMS));
    	cell.setCellValue("Items");
    	
    	cell = row1.createCell(5);
    	cell.setCellStyle(getThemeStyle(STYLE_THEME_INNER_FRAME));
		cell.setCellValue(getRandomQuote());

		int i=1;
		int adjust=0;
		if (helperProfiles.contains(this.session.getApplicationType())) {
			i++;
			// add jzr-support notice
	    	Row row2 = sheet.createRow(i);
	    	row2.setHeightInPoints(31.8f);
	    	cell = row2.createCell(0);
	    	cell.setCellStyle(getThemeStyle(STYLE_THEME_MAIN_FRAME));
	    	cell = row2.createCell(1);
	    	cell.setCellStyle(getThemeStyle(STYLE_THEME_INNER_FRAME));
	    	cell = row2.createCell(2);
	    	cell.setCellStyle(getStyle(STYLE_NAV_WARNING_TITLE));
	    	cell.setCellValue("Need help ?");
	    	
	    	XSSFRichTextString richText = new XSSFRichTextString(HELPER_NOTICE); 
	    	XSSFFont normalCellFont = ((XSSFWorkbook)workbook).createFont();
	        normalCellFont.setFontHeightInPoints((short)11);
	        normalCellFont.setFontName(CellFonts.FONT_CALIBRI);
	        normalCellFont.setColor(IndexedColors.WHITE.getIndex());
	        XSSFFont emailCellFont = ((XSSFWorkbook)workbook).createFont();
	        emailCellFont.setFontHeightInPoints((short)11);
	        emailCellFont.setColor(IndexedColors.BLUE.getIndex());
	        emailCellFont.setFontName(CellFonts.FONT_CALIBRI);
	        emailCellFont.setBold(true);
	        emailCellFont.setUnderline(FontUnderline.SINGLE);
	    	richText.applyFont(0, HELPER_NOTICE.length()-23, normalCellFont);
	    	richText.applyFont(HELPER_NOTICE.length()-22, HELPER_NOTICE.length(), emailCellFont);
	    	cell = addCell(row2, 3, richText);
	    	cell.setCellStyle(getStyle(STYLE_NAV_WARNING_TITLE));
	    	
	    	cell = row2.createCell(4);
	    	cell.setCellStyle(getStyle(STYLE_NAV_WARNING_TITLE));
	    	cell = row2.createCell(5);
	    	cell.setCellStyle(getThemeStyle(STYLE_THEME_INNER_FRAME));
	    	adjust = 1;
		}
		
		int nbSheets = 0;
		for (ConfigSheet sheetEntry : this.sheetCfg.getConfigNavigationMenuSheet()){
			
			if (sheetEntry.equals((ConfigSheet)sheetCfg) || !sheetEntry.isEnabled())
				continue; // do not process ourselves and ignore non-displayable sheet
			
			i++;
			nbSheets++;
			int col = 0;
			Row row = sheet.createRow(i);
			row.setHeightInPoints(35);
			
			cell = row.createCell(col++);
			cell.setCellStyle(getThemeStyle(STYLE_THEME_MAIN_FRAME));
			cell = row.createCell(col++);
			cell.setCellStyle(getThemeStyle(STYLE_THEME_INNER_FRAME));
			
			CellReference cellref = new CellReference(sheetEntry.getName(), 0, 0, true, true);
			
			// sheet name
			cell = row.createCell(col++);
			cell.setCellValue(sheetEntry.getName());
			cell.setCellStyle(getStyle(STYLE_NAV_ABOUT_FIELD_NAME));
			addDocumentHyperLink(cell, cellref.formatAsString());
			
			// description
			cell = addCell(row, col++, sheetEntry.getDescription());
			cell.setCellStyle(getStyle(STYLE_NAV_ABOUT_FIELD_VALUE));
			
			// items
			col = displayItemsCount(row, col, sheetEntry.getName(), cellref);
			
			cell = row.createCell(col++);
	    	cell.setCellStyle(getThemeStyle(STYLE_THEME_BACKGROUND_FRAME));
		}

		int yCellFactor = (nbSheets >= PICTURE_ROW_SIZE)? PICTURE_ROW_SIZE : nbSheets;
    	addPicture(
    			sheet, 
    			this.displayContext.getSetupManager().getLogoFilePath(),
    			-1,
    			LOGO_COLUMN_INDEX, 
    			LOGO_ROW_INDEX + adjust, 
    			((double)1/65.87)*yCellFactor, // (1/65 * number of sheets) of the large cell (4th one)
    			yCellFactor  // number of sheets
    			);
    	
    	addPicture(
    			sheet, 
    			this.displayContext.getSetupManager().getIconFilePath(),
    			-1,
    			ICON_COLUMN_INDEX,
    			ICON_ROW_INDEX, 
    			((double)10/35),
    			0.7,
    			5,
    			20
    			);
		
		// Bottom row. Set it very large
		i++;
		Row rowBottom = sheet.createRow(i);
		rowBottom.setHeightInPoints(300);
		cell = rowBottom.createCell(0);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_MAIN_FRAME));
		cell = rowBottom.createCell(1);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_INNER_FRAME));
		cell = rowBottom.createCell(2);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_BACKGROUND_FRAME));
		cell = rowBottom.createCell(3);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_BACKGROUND_FRAME));
		cell = rowBottom.createCell(4);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_BACKGROUND_FRAME));
		cell = rowBottom.createCell(5);
		cell.setCellStyle(getThemeStyle(STYLE_THEME_BACKGROUND_FRAME));
	}
	
	private int displayItemsCount(Row row, int col, String sheetName, CellReference cellref) {
		long itemsCount = MenuItemsContext.ITEMS_NA;
		long graphItemsCount = 0;
		
		MenuItemsContext menuItemsCtx = this.displayContext.getMenuItems().get(sheetName);
		boolean criticalAlert = false;
		if (menuItemsCtx != null){
			itemsCount = menuItemsCtx.getItemsCount();
			graphItemsCount = menuItemsCtx.getGraphItemsCount();
			criticalAlert = menuItemsCtx.hasCriticalItems();
		}
		
		Cell cell;
		if (itemsCount > 0 && graphItemsCount == 0)
			cell = addCell(row, col++, itemsCount, cellref, criticalAlert ? STYLE_NAV_FIELD_ITEM_CRITICAL_VALUE: STYLE_NAV_FIELD_ITEM_VALUE);
		else if (itemsCount > 0 && graphItemsCount > 0)
			cell = addCell(row, col++, itemsCount + "\n(" + graphItemsCount + ")", cellref, criticalAlert ? STYLE_NAV_FIELD_ITEM_CRITICAL_VALUE: STYLE_NAV_FIELD_ITEM_VALUE);
		else if (itemsCount == 0)
			cell = addCell(row, col++, itemsCount, cellref, STYLE_NAV_FIELD_ITEM_ZERO_VALUE);
		else if (itemsCount == MenuItemsContext.NO_ITEMS)
			cell = addEmptyCell(row, col++, STYLE_NAV_FIELD_ITEM_VALUE);
		else
			cell = addCell(row, col++, "NA", cellref, STYLE_NAV_FIELD_ITEM_VALUE);
		
		if (criticalAlert)
			setColorForeground(cell, menuItemsCtx.getCriticalColor());
		
		return col;
	}

	@Override
	protected Logger getLogger(){
		return logger;
	}
	
	private XSSFRichTextString getRandomQuote() {
		Random random = new Random();
		int result = random.nextInt(QUOTES.length);
		String value = QUOTES[result] + "    " + QUOTE_AUTHOR[result];
		
    	XSSFRichTextString richText = new XSSFRichTextString(value); 
    	
        Font cellFont = workbook.createFont();
        cellFont.setFontHeightInPoints((short)11);

        Font smallCellFont = workbook.createFont();
        smallCellFont.setFontHeightInPoints((short)8);
        
    	richText.applyFont(0, QUOTES[result].length() + 4, cellFont);
    	richText.applyFont(QUOTES[result].length() + 4, value.length(), smallCellFont);
		
		return richText;
	}
}
