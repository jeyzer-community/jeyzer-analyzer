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







import java.awt.Color;
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

public class VinciTheme extends AbstractTheme{

	// Vinci theme style
	public static final String NAME = "vinci";
	
	public static final XSSFColor THEME_TITLE_COLOR = new XSSFColor(new java.awt.Color(255, 255, 255), null); // white
	
	@Override
	public Map<String, CellStyle> createThemeStyles(Map<String, CellStyle> styles, XSSFWorkbook wb, CreationHelper createHelper){
    	// Main border
    	XSSFCellStyle xssfstyle = wb.createCellStyle();
    	XSSFFont font = wb.createFont();
        font.setColor(THEME_TITLE_COLOR);
        font.setFontName(CellFonts.FONT_MYRIAD_PRO);
        font.setFontHeightInPoints((short)11);
        xssfstyle.setFont(font);
        xssfstyle.setFillForegroundColor(new XSSFColor(new Color(106,129,0), wb.getStylesSource().getIndexedColors()));
    	xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    	styles.put(STYLE_THEME_MAIN_FRAME, xssfstyle);
    
        // Inner border
        font = wb.createFont();
        font.setColor(THEME_TITLE_COLOR);
        font.setFontHeightInPoints((short)11);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended
        font.setFontName(CellFonts.FONT_CALIBRI);
        font.setItalic(true);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(153, 184, 0), wb.getStylesSource().getIndexedColors()));  // green flash
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_INNER_FRAME, xssfstyle);

        // Inner border items
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(153, 184, 0), wb.getStylesSource().getIndexedColors()));  // green flash
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_INNER_FRAME_ITEMS, xssfstyle);
        
        // Jeyzer title
        font = wb.createFont();
        font.setColor(THEME_TITLE_COLOR);
        font.setFontHeightInPoints((short)36);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended
        font.setFontName(CellFonts.FONT_MYRIAD_PRO);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(153, 184, 0), wb.getStylesSource().getIndexedColors()));  // green flash
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_JEYZER_MAIN_TITLE, xssfstyle);

        // JZR report title
        font = wb.createFont();
        font.setColor(THEME_TITLE_COLOR);
        font.setFontHeightInPoints((short)9);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended
        font.setFontName(CellFonts.FONT_MYRIAD_PRO);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(153, 184, 0), wb.getStylesSource().getIndexedColors()));  // green flash
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        xssfstyle.setRotation((short)90);
        styles.put(STYLE_THEME_JZR_REPORT_TITLE, xssfstyle);
        
        // Jeyzer about title
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(153, 184, 0), wb.getStylesSource().getIndexedColors()));  // green flash
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_THEME_JEYZER_ABOUT_TITLE, xssfstyle);
        
        // background frame
    	xssfstyle = wb.createCellStyle();
    	xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(191, 202, 136), wb.getStylesSource().getIndexedColors()));  // light green
    	xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    	styles.put(STYLE_THEME_BACKGROUND_FRAME, xssfstyle);
        
        // Jeyzer title
        font = wb.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short)11);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended
        font.setFontName(CellFonts.FONT_MYRIAD_PRO);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new Color(106,129,0), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_JEYZER_TITLE, xssfstyle);
    	
        font = wb.createFont();
        font.setColor(new XSSFColor(new java.awt.Color(38,38,38), wb.getStylesSource().getIndexedColors()));
        font.setFontHeightInPoints((short)18);
        font.setBold(true);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(191, 202, 136), wb.getStylesSource().getIndexedColors()));  // light green
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_SESSION_TITLE, xssfstyle);

        font = wb.createFont();
        font.setColor(new XSSFColor(new java.awt.Color(38,38,38), wb.getStylesSource().getIndexedColors())); 
        font.setBold(true);
        font.setFontHeightInPoints((short)14);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(191, 202, 136), wb.getStylesSource().getIndexedColors()));  // light green
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_SESSION_HEADER, xssfstyle);
        
        font = wb.createFont();
        font.setColor(new XSSFColor(new java.awt.Color(38,38,38), wb.getStylesSource().getIndexedColors())); 
        font.setBold(true);
        font.setFontHeightInPoints((short)11);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(191, 202, 136), wb.getStylesSource().getIndexedColors()));  // light green
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_SESSION, xssfstyle);
        
        // Top border style
        font = wb.createFont();
        font.setFontHeightInPoints((short)11);
        font.setColor(new XSSFColor(new java.awt.Color(38, 38, 38), wb.getStylesSource().getIndexedColors()));  // dark
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(188, 199, 135), wb.getStylesSource().getIndexedColors()));  // green
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_TOP_BAR, xssfstyle);
        
        // Top border style legend
        font = wb.createFont();
        font.setFontHeightInPoints((short)8);
        font.setFontName(CellFonts.FONT_CALIBRI);
        font.setColor(new XSSFColor(new java.awt.Color(237, 237, 237), wb.getStylesSource().getIndexedColors()));  // light grey
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(188, 199, 135), wb.getStylesSource().getIndexedColors()));  // green
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_TOP_BAR_JEYZER_LEGEND, xssfstyle);

        // Top border style Jeyzer title
        font = wb.createFont();
        font.setFontHeightInPoints((short)9);
        font.setFontName(CellFonts.FONT_MYRIAD_PRO);
        font.setBold(true);
        font.setColor(new XSSFColor(new java.awt.Color(237, 237, 237), wb.getStylesSource().getIndexedColors()));  // light grey
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(188, 199, 135), wb.getStylesSource().getIndexedColors()));  // green
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_TOP_BAR_JEYZER_TITLE, xssfstyle);
        
        // Top border style small
        font = wb.createFont();
        font.setFontHeightInPoints((short)9);
        font.setColor(new XSSFColor(new java.awt.Color(38, 38, 38), wb.getStylesSource().getIndexedColors()));  // dark
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(188, 199, 135), wb.getStylesSource().getIndexedColors()));  // green
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_TOP_BAR_SMALL, xssfstyle);

        // Top border graph picture link
        font = wb.createFont();
        font.setFontHeightInPoints((short)11);
        font.setColor(new XSSFColor(new java.awt.Color(38, 38, 38), wb.getStylesSource().getIndexedColors()));  // dark
        font.setFontName(CellFonts.FONT_SYMBOL);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(188, 199, 135), wb.getStylesSource().getIndexedColors()));  // green
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_TOP_BAR_SYMBOL, xssfstyle);
        
        // Header style
        font = wb.createFont();
        font.setFontHeightInPoints((short)11);
        font.setColor(new XSSFColor(new java.awt.Color(38, 38, 38), wb.getStylesSource().getIndexedColors()));
        xssfstyle = createSidedBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(170, 184, 100), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        xssfstyle.setWrapText(true);
        styles.put(STYLE_THEME_HEADER, xssfstyle);
        
        // Header time style
        font = wb.createFont();
        font.setFontHeightInPoints((short)11);
        font.setColor(new XSSFColor(new java.awt.Color(38, 38, 38), wb.getStylesSource().getIndexedColors()));
        xssfstyle = createSidedBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(170, 184, 100), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_HEADER_TIME, xssfstyle);
        
        // Header style row
        font = wb.createFont();
        font.setFontHeightInPoints((short)11);
        font.setColor(new XSSFColor(new java.awt.Color(13, 13, 13), wb.getStylesSource().getIndexedColors()));
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(209, 225, 181), wb.getStylesSource().getIndexedColors())); // light green
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_HEADER_ROW, xssfstyle);

        // Header style NA
        font = wb.createFont();
        font.setFontHeightInPoints((short)8);
        font.setColor(new XSSFColor(new java.awt.Color(117, 113, 113), wb.getStylesSource().getIndexedColors()));
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(209, 225, 181), wb.getStylesSource().getIndexedColors())); // light green
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_HEADER_ROW_NA, xssfstyle);
        
        // Header style row
        font = wb.createFont();
        font.setFontHeightInPoints((short)11);
        font.setColor(new XSSFColor(new java.awt.Color(13, 13, 13), wb.getStylesSource().getIndexedColors()));
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(209, 225, 181), wb.getStylesSource().getIndexedColors())); // light green
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);        
        xssfstyle.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy h:mm:ss"));
        styles.put(STYLE_THEME_HEADER_DATE_ROW, xssfstyle);
        
        // Header style left alignment
        font = wb.createFont();
        font.setFontHeightInPoints((short)11);
        font.setColor(new XSSFColor(new java.awt.Color(38, 38, 38), wb.getStylesSource().getIndexedColors()));
        xssfstyle = createSidedBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(170, 184, 100), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_HEADER_ALIGN_LEFT, xssfstyle);
        
        // Header style small
        font = wb.createFont();
        font.setFontHeightInPoints((short)9);
        font.setColor(new XSSFColor(new java.awt.Color(38, 38, 38), wb.getStylesSource().getIndexedColors()));
        xssfstyle = createSidedBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(170, 184, 100), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_HEADER_SMALL, xssfstyle);

        // Header style very small rotated
        font = wb.createFont();
        font.setFontHeightInPoints((short)7);
        font.setColor(new XSSFColor(new java.awt.Color(38, 38, 38), wb.getStylesSource().getIndexedColors()));
        font.setBold(true);
        xssfstyle = createSidedBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(170, 184, 100), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        xssfstyle.setRotation((short)45);
        styles.put(STYLE_THEME_HEADER_VERY_SMALL_ROTATED, xssfstyle);

        // Header action style
        font = wb.createFont();
        font.setColor(new XSSFColor(new java.awt.Color(38, 38, 38), wb.getStylesSource().getIndexedColors()));
        xssfstyle = createSidedBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(169, 208, 142), wb.getStylesSource().getIndexedColors()));  // light green
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_HEADER_TIME_ACTION, xssfstyle);
        
        // Header action link style
        font = wb.createFont();
        font.setColor(new XSSFColor(new java.awt.Color(38, 38, 38), wb.getStylesSource().getIndexedColors()));
        font.setBold(true);
        xssfstyle = createSidedBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(169, 208, 142), wb.getStylesSource().getIndexedColors()));  // light green
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_HEADER_TIME_ACTION_START, xssfstyle);
        
        // Header 2 style
        font = wb.createFont();
        font.setFontHeightInPoints((short)11);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setBold(true);
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(185, 201, 33), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        styles.put(STYLE_THEME_HEADER2, xssfstyle);
        
        // Header 2 style very small rotated
        font = wb.createFont();
        font.setFontHeightInPoints((short)7);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setBold(true);
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(185, 201, 33), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(font);
        xssfstyle.setRotation((short)45);
        styles.put(STYLE_THEME_HEADER2_VERY_SMALL_ROTATED, xssfstyle);
        
        font = wb.createFont();
        font.setBold(true);
        font.setItalic(true);
        font.setFontHeightInPoints((short)9);
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(216, 239, 213), wb.getStylesSource().getIndexedColors()));  // green light
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
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(216, 239, 213), wb.getStylesSource().getIndexedColors()));  // green light
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
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(216, 239, 213), wb.getStylesSource().getIndexedColors()));  // green light
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
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(191, 202, 136), wb.getStylesSource().getIndexedColors()));  // green light
        styles.put(STYLE_THEME_SECTION_HEADER, xssfstyle);
        
        // Section style style bold
        font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setFontHeightInPoints((short)16);
        font.setBold(true);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setFont(font);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setBorderTop(BorderStyle.THIN);
        xssfstyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(191, 202, 136), wb.getStylesSource().getIndexedColors()));  // green light
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
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(191, 202, 136), wb.getStylesSource().getIndexedColors()));  // green light
        styles.put(STYLE_THEME_SECTION_ABOUT_CATEGORY, xssfstyle);
        
    	return styles;
	}
	
	protected XSSFCellStyle createSidedBorderedStyle(XSSFWorkbook wb){
		XSSFCellStyle style = wb.createCellStyle();
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(new XSSFColor(new java.awt.Color(217, 217, 217), wb.getStylesSource().getIndexedColors())); // light grey
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(new XSSFColor(new java.awt.Color(217, 217, 217), wb.getStylesSource().getIndexedColors()));
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        return style;
    }
	
}
