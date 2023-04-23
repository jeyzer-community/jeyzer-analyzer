package org.jeyzer.analyzer.output.poi.style;

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







import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FontUnderline;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class CellFonts {

	public static final String FONT_SYMBOL = "Symbol";
	public static final String FONT_CALIBRI = "Calibri";
	public static final String FONT_YU_GOTHIC_UI_LIGHT = "Yu Gothic UI Light";
	public static final String FONT_COOPER_BLACK = "Cooper Black";
	public static final String FONT_MYRIAD_PRO = "Myriad Pro";
	
	// cached fonts
	public static final String FONT_6 = "FONT_6";
	public static final String FONT_8 = "FONT_8";
	public static final String FONT_9 = "FONT_9";
	public static final String FONT_9_BOLD_ITALIC = "FONT_9_BOLD_ITALIC";
	public static final String FONT_10 = "FONT_10";
	public static final String FONT_11 = "FONT_11";
	public static final String FONT_12 = "FONT_12";
	public static final String FONT_12_BOLD = "FONT_12_BOLD";
	public static final String FONT_SYMBOL_9 = "FONT_SYMBOL_9";
	public static final String FONT_DOUBLE_UNDERLINE_SYMBOL_9 = "FONT_DOUBLE_UNDERLINE_SYMBOL_9";
	public static final String FONT_SYMBOL_10 = "FONT_SYMBOL_10";
	public static final String FONT_SYMBOL_11 = "FONT_SYMBOL_11";
	public static final String FONT_SYMBOL_BOLD_10 = "FONT_SYMBOL_BOLD_10";
	public static final String FONT_SYMBOL_BOLD_11 = "FONT_SYMBOL_BOLD_11";
	public static final String FONT_LINK = "FONT_LINK";

	public static Font getUnderlinedFont(Workbook wb, Font oldFont, byte underlineType) {
		
		// fetch same font - but with underline - from the workbook font cache
		Font newFont = wb.findFont(oldFont.getBold(), oldFont.getColor(), oldFont.getFontHeight(), oldFont.getFontName(), oldFont.getItalic(), oldFont.getStrikeout(), oldFont.getTypeOffset(), underlineType);
	
		if (newFont == null){
			// create a new one
			newFont = copyFont(wb, oldFont);
			newFont.setUnderline(underlineType);
		}
		
		return newFont;
	}
	
	public static Font copyFont(Workbook wb, Font oldFont){
		Font newFont = wb.createFont();
		
		// perform font copy
        newFont.setBold(oldFont.getBold());
        newFont.setColor(oldFont.getColor());
        newFont.setFontHeight(oldFont.getFontHeight());
        newFont.setFontName(oldFont.getFontName());
        newFont.setItalic(oldFont.getItalic());
        newFont.setStrikeout(oldFont.getStrikeout());
        newFont.setTypeOffset(oldFont.getTypeOffset());
        newFont.setCharSet(oldFont.getCharSet());
        newFont.setUnderline(oldFont.getUnderline());
        
        return newFont;
	}
	
	public CellFonts(XSSFWorkbook wb){
		this.constantFonts = createFonts(wb);
	}
	
	public XSSFFont getFont(String name){
		return this.constantFonts.get(name);
	}
	
	private Map<String, XSSFFont> constantFonts = new HashMap<>();
	
	private Map<String, XSSFFont> createFonts(XSSFWorkbook wb){
		Map<String, XSSFFont> fonts = new HashMap<>();
		XSSFFont font;

		font = wb.createFont();
		font.setFontHeightInPoints((short)6);
		fonts.put(FONT_6, font);
		
		font = wb.createFont();
		font.setFontHeightInPoints((short)8);
		fonts.put(FONT_8, font);

		font = wb.createFont();
		font.setFontHeightInPoints((short)9);
		fonts.put(FONT_9, font);
		
		font = wb.createFont();
		font.setBold(true);
		font.setItalic(true);
		font.setFontHeightInPoints((short)9);
		fonts.put(FONT_9_BOLD_ITALIC, font);
		
		font = wb.createFont();
		font.setFontHeightInPoints((short)10);
		fonts.put(FONT_10, font);

		font = wb.createFont();
		font.setFontHeightInPoints((short)11);
		fonts.put(FONT_11, font);
		
		font = wb.createFont();
		font.setFontHeightInPoints((short)12);
		fonts.put(FONT_12, font);
		
		font = wb.createFont();
		font.setFontHeightInPoints((short)12);
		font.setBold(true);
		fonts.put(FONT_12_BOLD, font);
		
		font = wb.createFont();
		font.setFontHeightInPoints((short)9);
		font.setFontName(FONT_SYMBOL);
        fonts.put(FONT_SYMBOL_9, font);

		font = wb.createFont();
		font.setFontHeightInPoints((short)9);
		font.setFontName(FONT_SYMBOL);
		font.setUnderline(FontUnderline.DOUBLE);
        fonts.put(FONT_DOUBLE_UNDERLINE_SYMBOL_9, font);
        
		font = wb.createFont();
		font.setFontHeightInPoints((short)10);
		font.setFontName(FONT_SYMBOL);
        fonts.put(FONT_SYMBOL_10, font);

		font = wb.createFont();
		font.setFontHeightInPoints((short)10);
		font.setFontName(FONT_SYMBOL);
		font.setBold(true);
        fonts.put(FONT_SYMBOL_BOLD_10, font);
        
		font = wb.createFont();
		font.setFontHeightInPoints((short)11);
		font.setFontName(FONT_SYMBOL);
        fonts.put(FONT_SYMBOL_11, font);
        
		font = wb.createFont();
		font.setFontHeightInPoints((short)11);
		font.setFontName(FONT_SYMBOL);
		font.setBold(true);
        fonts.put(FONT_SYMBOL_BOLD_11, font);
        
        font = wb.createFont();
        font.setUnderline(Font.U_SINGLE);
        font.setColor(IndexedColors.BLUE.getIndex());
        fonts.put(FONT_LINK, font);
        
		return fonts;
	}
	
}
