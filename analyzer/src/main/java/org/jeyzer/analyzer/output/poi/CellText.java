package org.jeyzer.analyzer.output.poi;

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



import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeyzer.analyzer.output.poi.style.CellFonts;

public final class CellText {

	private CellText(){}
	
	public static final String FONT_SYMBOL_DOWN_ARROW = "¯";
	public static final String FONT_SYMBOL_UP_ARROW = "­";
	public static final String FONT_SYMBOL_RIGHT_ARROW = "®";
	public static final String FONT_SYMBOL_LEFT_ARROW = "¬";
	public static final String FONT_SYMBOL_CLOVER = "§";
	public static final String FONT_SYMBOL_OMEGA = "W";
	
	// size 22
	private static final String[] FONT_SYMBOLS = {"a", "b", "d", "e", "f", "g", "h", "j", "l", "m", "n", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
	
	public static final int CELL_COMMENT_MAX_SIZE = 33035;	
	public static final int CELL_VALUE_MAX_SIZE = 32767;	
		
	public static final int COMMENT_COLUMN_CHARS_SIZE = 12;
	
	// Stack display
	public static final String TRUNCATED_STACK_MESSAGE = "\n\t[Jeyzer warning : above stack has been truncated. Reason : stack too large to fit in current cell comment]";
	
	// Stack display- POI Excel adds extra chars (?), so assume 300 security buffer
	public static final int STACK_DISPLAY_MAX_SIZE = CellText.CELL_COMMENT_MAX_SIZE - TRUNCATED_STACK_MESSAGE.length() - 300;
	
	public static XSSFRichTextString concatTextAndSymbol(XSSFWorkbook workbook, String text, String symbol){
		XSSFRichTextString richText = new XSSFRichTextString(text);
	    XSSFFont cellFont = workbook.createFont();
	    cellFont.setFontName(CellFonts.FONT_SYMBOL);
	    richText.append(symbol, cellFont);
		return richText;
	}
	
	public static int getFontSymbolCharsLength() {
		return FONT_SYMBOLS.length;
	}
	
	public static String getFontSymbolChar(int index) {
		if (index <0 || index >= FONT_SYMBOLS.length)
			return "Invalid index";
		return FONT_SYMBOLS[index];
	}
}
