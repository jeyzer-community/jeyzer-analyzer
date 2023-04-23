package org.jeyzer.analyzer.output.poi.theme;

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







import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeyzer.analyzer.output.poi.style.CellFonts;

public class BlueTheme extends AbstractTheme{
	
	// Blue theme style
	public static final String NAME = "blue";
	
	public static final XSSFColor THEME_TITLE_COLOR = new XSSFColor(new java.awt.Color(48,84,150), null);
	
	@Override
	public Map<String, CellStyle> createThemeStyles(Map<String, CellStyle> styles, XSSFWorkbook wb, CreationHelper createHelper){

		// Main border
    	XSSFCellStyle xssfstyle = wb.createCellStyle();
    	xssfstyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
    	xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);		
    	styles.put(STYLE_THEME_MAIN_FRAME, xssfstyle);
    	
        // Inner border
        XSSFFont font = wb.createFont();
        font.setColor(THEME_TITLE_COLOR);
        short heigth = 11;
        font.setFontHeightInPoints(heigth);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended
        font.setFontName(CellFonts.FONT_CALIBRI);
        font.setItalic(true);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(220, 230, 241), wb.getStylesSource().getIndexedColors()));  // light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_INNER_FRAME, xssfstyle);

        // Inner border items
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(220, 230, 241), wb.getStylesSource().getIndexedColors()));  // light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_INNER_FRAME_ITEMS, xssfstyle);
        
        // Jeyzer main title
        font = wb.createFont();
        font.setColor(THEME_TITLE_COLOR);
        font.setFontHeightInPoints((short)36);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended, Yu Gothic UI Light
        font.setFontName(CellFonts.FONT_COOPER_BLACK);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(220, 230, 241), wb.getStylesSource().getIndexedColors()));  // light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_JEYZER_MAIN_TITLE, xssfstyle);

        // JZR report title
        font = wb.createFont();
        font.setColor(THEME_TITLE_COLOR);
        font.setFontHeightInPoints((short)9);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended, Yu Gothic UI Light
        font.setFontName(CellFonts.FONT_COOPER_BLACK);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(220, 230, 241), wb.getStylesSource().getIndexedColors()));  // light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        xssfstyle.setRotation((short)90);
        styles.put(STYLE_THEME_JZR_REPORT_TITLE, xssfstyle);
        
        // Jeyzer about title
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(220, 230, 241), wb.getStylesSource().getIndexedColors()));  // light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_THEME_JEYZER_ABOUT_TITLE, xssfstyle);
        
        // background frame
    	xssfstyle = wb.createCellStyle();
    	xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(242, 242, 242), wb.getStylesSource().getIndexedColors()));  // light blue
    	xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);		
    	styles.put(STYLE_THEME_BACKGROUND_FRAME, xssfstyle);
        
        // Jeyzer title
        font = wb.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short)11);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended
        font.setFontName(CellFonts.FONT_COOPER_BLACK);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());  // purple
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_JEYZER_TITLE, xssfstyle);
    	
        font = wb.createFont();
        font.setColor(new XSSFColor(new java.awt.Color(166,166,166), wb.getStylesSource().getIndexedColors()));
        font.setBold(true);
        font.setFontHeightInPoints((short)18);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(220, 230, 241), wb.getStylesSource().getIndexedColors()));  // light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_SESSION_TITLE, xssfstyle);

        font = wb.createFont();
        font.setColor(new XSSFColor(new java.awt.Color(102,102,153), wb.getStylesSource().getIndexedColors()));  // blue
        font.setBold(true);
        font.setFontHeightInPoints((short)14);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(220, 230, 241), wb.getStylesSource().getIndexedColors()));  // light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_SESSION_HEADER, xssfstyle);
        
        font = wb.createFont();
        font.setColor(new XSSFColor(new java.awt.Color(102,102,153), wb.getStylesSource().getIndexedColors()));  // blue
        font.setBold(true);
        font.setFontHeightInPoints((short)11);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(220, 230, 241), wb.getStylesSource().getIndexedColors()));  // light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_SESSION, xssfstyle);
        
        // Top border style
        font = wb.createFont();
        font.setFontHeightInPoints((short)11);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setBold(true);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(220, 230, 241), wb.getStylesSource().getIndexedColors()));  // light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_TOP_BAR, xssfstyle);

        // Top border style legend
        font = wb.createFont();
        font.setFontHeightInPoints((short)8);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setFontName(CellFonts.FONT_CALIBRI);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(220, 230, 241), wb.getStylesSource().getIndexedColors()));  // light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_TOP_BAR_JEYZER_LEGEND, xssfstyle);
        
        // Top border style Jeyzer title
        font = wb.createFont();
        font.setFontHeightInPoints((short)9);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setFontName(CellFonts.FONT_COOPER_BLACK);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(220, 230, 241), wb.getStylesSource().getIndexedColors()));  // light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_TOP_BAR_JEYZER_TITLE, xssfstyle);
        
        // Top border style small
        font = wb.createFont();
        font.setFontHeightInPoints((short)9);
        font.setColor(IndexedColors.BLACK.getIndex());
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(220, 230, 241), wb.getStylesSource().getIndexedColors()));  // light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_TOP_BAR_SMALL, xssfstyle);

        // Top border graph picture link
        font = wb.createFont();
        font.setFontHeightInPoints((short)11);
        font.setFontName(CellFonts.FONT_SYMBOL);
        font.setColor(IndexedColors.BLACK.getIndex());
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(220, 230, 241), wb.getStylesSource().getIndexedColors()));  // light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_TOP_BAR_SYMBOL, xssfstyle);
        
        // Header style
        font = wb.createFont();
        font.setBold(true);
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_HEADER, xssfstyle);
        styles.put(STYLE_THEME_HEADER2, xssfstyle);
        styles.put(STYLE_THEME_HEADER_ROW, xssfstyle);
        
        // Header style NA
        font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short)8);
        font.setColor(new XSSFColor(new java.awt.Color(117, 113, 113), wb.getStylesSource().getIndexedColors()));
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_HEADER_ROW_NA, xssfstyle);
        
        // Header date style
        font = wb.createFont();
        font.setBold(true);
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        xssfstyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm:ss"));
        styles.put(STYLE_THEME_HEADER_DATE_ROW, xssfstyle);
        
        // Header style left alignment
        font = wb.createFont();
        font.setBold(true);
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_HEADER_ALIGN_LEFT, xssfstyle);
        
        // Header style small
        font = wb.createFont();
        font.setFontHeightInPoints((short)9);
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_HEADER_SMALL, xssfstyle);

        // Header time style
        font = wb.createFont();
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_HEADER_TIME, xssfstyle);

        // Header time action style
        font = wb.createFont();
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(221, 221, 255), wb.getStylesSource().getIndexedColors())); // very light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_HEADER_TIME_ACTION, xssfstyle);
        
        // Header time action link style
        font = wb.createFont();
        font.setBold(true);
        font.setBold(true);
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(221, 221, 255), wb.getStylesSource().getIndexedColors())); // very light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_HEADER_TIME_ACTION_START, xssfstyle); 
        
        // Header style very small rotated
        font = wb.createFont();
        font.setFontHeightInPoints((short)7);
        font.setBold(true);
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        xssfstyle.setRotation((short)45);
        styles.put(STYLE_THEME_HEADER_VERY_SMALL_ROTATED, xssfstyle);
        styles.put(STYLE_THEME_HEADER2_VERY_SMALL_ROTATED, xssfstyle);  // same

        font = wb.createFont();
        font.setBold(true);
        font.setItalic(true);
        font.setFontHeightInPoints((short)9);
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(221, 221, 255), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_DYNAMIC_HEADER, xssfstyle);

        font = wb.createFont();
        font.setBold(true);
        font.setItalic(true);
        font.setFontHeightInPoints((short)9);
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(221, 221, 255), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        xssfstyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0"));
        styles.put(STYLE_THEME_DYNAMIC_HEADER_NUMBER, xssfstyle);

        font = wb.createFont();
        font.setBold(true);
        font.setItalic(true);
        font.setFontHeightInPoints((short)9);
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(221, 221, 255), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        xssfstyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.0"));
        styles.put(STYLE_THEME_DYNAMIC_HEADER_NUMBER_1_DECIMAL, xssfstyle);
        
        // Section header style
        font = wb.createFont();
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setFontHeightInPoints((short)14);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setFont(font);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setBorderTop(BorderStyle.THIN);
        xssfstyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(196, 227, 252), wb.getStylesSource().getIndexedColors()));  // blue light
        styles.put(STYLE_THEME_SECTION_HEADER, xssfstyle);

        // Section header style bold
        font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setFontHeightInPoints((short)16);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setFont(font);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setBorderTop(BorderStyle.THIN);
        xssfstyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(196, 227, 252), wb.getStylesSource().getIndexedColors()));  // blue light
        styles.put(STYLE_THEME_SECTION_HEADER_BOLD, xssfstyle);

        // Section about style category
        font = wb.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short)18);
        font.setBold(true);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setFont(font);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(196, 227, 252), wb.getStylesSource().getIndexedColors()));  // blue light
        styles.put(STYLE_THEME_SECTION_ABOUT_CATEGORY, xssfstyle);
        
    	return styles;
	}
}
