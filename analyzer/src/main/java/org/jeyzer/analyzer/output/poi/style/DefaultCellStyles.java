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



import static org.jeyzer.analyzer.output.poi.CellColor.XSSF_APPLE_GREEN_COLOR;
import static org.jeyzer.analyzer.output.poi.CellColor.XSSF_GREY_SHADOW_COLOR;
import static org.jeyzer.analyzer.output.poi.CellColor.XSSF_LEAF_GREEN_COLOR;
import static org.jeyzer.analyzer.output.poi.CellColor.XSSF_LIGHT_BROWN_COLOR;
import static org.jeyzer.analyzer.output.poi.CellColor.XSSF_LIGHT_GREEN_COLOR;
import static org.jeyzer.analyzer.output.poi.CellColor.XSSF_LIGHT_GREY_COLOR;
import static org.jeyzer.analyzer.output.poi.CellColor.XSSF_ULTRA_LIGHT_GREY_COLOR;
import static org.jeyzer.analyzer.output.poi.CellColor.XSSF_WHITE_COLOR;





import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DefaultCellStyles {
	// styles
	public static final String STYLE_CELL_DEFAULT = "default-poi-cell-style";
	public static final String STYLE_CELL = "cell-normal";
	public static final String STYLE_CELL_CENTERED = "cell-center";
	public static final String STYLE_CELL_GREY_CENTERED = "cell-grey-center";
	public static final String STYLE_CELL_CENTERED_WRAPPED = "cell-center-wrapped";
	public static final String STYLE_CELL_ALIGN_RIGHT = "cell-normal-align-right";
	public static final String STYLE_CELL_CENTERED_SMALL = "cell-centered-small";
	public static final String STYLE_CELL_CENTERED_SMALL_ITALIC = "cell-centered-small-italic";
	public static final String STYLE_CELL_GREY_CENTERED_SMALL_ITALIC = "cell-grey-centered-small-italic";
	public static final String STYLE_CELL_SMALL_TEXT = "cell-small-text";
	public static final String STYLE_CELL_SMALL_TEXT_WRAPPED = "cell-small-text-wrapped";
	public static final String STYLE_CELL_SMALL_TEXT_LEFT_ALIGNED = "cell-small-text-lefy-aligned";
	public static final String STYLE_CELL_VERY_SMALL_TEXT_WRAPPED = "cell-very-small-text-wrapped";
	public static final String STYLE_CELL_GREY = "cell-grey";
	public static final String STYLE_CELL_GREY_SHADOW = "cell-grey-shadow";
	public static final String STYLE_CELL_WHITE = "cell-white";
	public static final String STYLE_CELL_ITALIC = "cell-italic";
	public static final String STYLE_CELL_ITALIC_ALIGN_RIGHT = "cell-italic-align-right";
	public static final String STYLE_CELL_ITALIC_WHITE = "cell-italic-white";
	public static final String STYLE_CELL_GREY_ITALIC = "cell-grey-italic";
	public static final String STYLE_CELL_GREEN_BACKGROUND_CENTERED = "cell-green-background-center";
	public static final String STYLE_CELL_ORANGE_BACKGROUND_CENTERED = "cell-orange-background-center";
	public static final String STYLE_CELL_NUMBER = "cell-number";
	public static final String STYLE_CELL_SMALL_NUMBER = "cell-number-small";
	public static final String STYLE_CELL_NUMBER_WHITE = "cell-number-white";
	public static final String STYLE_CELL_GREY_NUMBER = "cell-grey-number";	
	public static final String STYLE_CELL_DECIMAL_NUMBER = "cell-decimal-number";
	public static final String STYLE_CELL_DECIMAL_2_NUMBER = "cell-decimal-number-2";
	public static final String STYLE_CELL_DATE = "cell-date";
	public static final String STYLE_CELL_DATE_CENTERED = "cell-date-center";
	public static final String STYLE_CELL_GREY_DATE_CENTERED = "cell-grey-date-center";
	public static final String STYLE_CELL_GREEN_DATE_CENTERED = "cell-green-date-center";
	public static final String STYLE_LINK = "link";
	public static final String STYLE_LINK_CENTERED = "link-align-center";
	public static final String STYLE_SESSION_CELL = "cell-session";
	public static final String STYLE_SESSION_CELL_TEXT = "cell-text-session";
	public static final String STYLE_SESSION_WARNING_CELL = "cell-warn-session";
	public static final String STYLE_SESSION_OK_CELL = "cell-ok-session";
	public static final String STYLE_CATEGORY_HEADER = "category-header";
	public static final String STYLE_NAV_ABOUT_FIELD_NAME = "nav-about-field-name";
	public static final String STYLE_NAV_ABOUT_FIELD_VALUE = "nav-about-field-value";
	public static final String STYLE_NAV_FIELD_ITEM_VALUE = "nav-field-item-value";
	public static final String STYLE_NAV_FIELD_ITEM_ZERO_VALUE = "nav-field-item-zero-value";
	public static final String STYLE_NAV_FIELD_ITEM_CRITICAL_VALUE = "nav-field-item-critical-value";
	public static final String STYLE_NAV_ERROR_TITLE = "nav-error-title";
	public static final String STYLE_NAV_WARNING_TITLE = "nav-warning-title";
	public static final String STYLE_ABOUT_FIELD_WARNING_VALUE = "nav-about-field-warning-value";
	public static final String STYLE_ABOUT_FIELD_ERROR_VALUE = "nav-about-field-error-value";
	public static final String STYLE_ERROR = "cell-error";
	public static final String STYLE_WARNING = "cell-warning";
	public static final String STYLE_UFO = "cell-ufo";
	public static final String STYLE_RED_NOTICE = "cell-red-notice";
	public static final String STYLE_RED_NOTICE_CENTERED = "cell-red-notice-centered";
	public static final String STYLE_ORANGE_NOTICE = "cell-red-notice";
	public static final String STYLE_MISSING_TD_HEADER = "header-missing-thread-dump";
	public static final String STYLE_MISSING_TD_CELL = "cell-missing-thread-dump";
	public static final String STYLE_RESTART_HEADER = "header-restart";
	public static final String STYLE_RESTART_COLUMN = "column-restart";
	public static final String STYLE_RESTART_LINE = "line-restart";
	public static final String STYLE_LINE_SEPARATOR = "line-separator";
	
	// Black frame
	public static final String STYLE_FRAME_SIMPLE = "frame-simple";
	
	// Frames
	public static final String STYLE_FRAME_LEFT = "frame-left";
	public static final String STYLE_FRAME_TOP_LEFT = "frame-top-left";
	public static final String STYLE_FRAME_TOP = "frame-top";
	public static final String STYLE_FRAME_TOP_RIGHT = "frame-top-right";
	public static final String STYLE_FRAME_RIGHT = "frame-right";
	public static final String STYLE_FRAME_BOTTOM_RIGHT = "frame-bottom-right";
	public static final String STYLE_FRAME_BOTTOM = "frame-bottom";
	public static final String STYLE_FRAME_BOTTOM_LEFT = "frame-bottom-left";

	// Graph frames
	public static final String STYLE_GRAPH_FRAME_LEFT = "graph-frame-left";
	public static final String STYLE_GRAPH_FRAME_TOP_LEFT = "graph-frame-top-left";
	public static final String STYLE_GRAPH_FRAME_TOP = "graph-frame-top";
	public static final String STYLE_GRAPH_FRAME_TOP_RIGHT = "graph-frame-top-right";
	public static final String STYLE_GRAPH_FRAME_RIGHT = "graph-frame-right";
	public static final String STYLE_GRAPH_FRAME_BOTTOM_RIGHT = "graph-frame-bottom-right";
	public static final String STYLE_GRAPH_FRAME_BOTTOM = "graph-frame-bottom";
	public static final String STYLE_GRAPH_FRAME_BOTTOM_LEFT = "graph-frame-bottom-left";
	public static final String STYLE_GRAPH_FRAME_INSIDE = "graph-frame-inside";
	
	// levels
	public static final String STYLE_CELL_LEVEL_CRITICAL = "cell-critical";
	public static final String STYLE_CELL_LEVEL_ERROR = "cell-red";
	public static final String STYLE_CELL_LEVEL_WARNING = "cell-warning";
	public static final String STYLE_CELL_LEVEL_INFO = "cell-info";
	
	// shading
	public static final String STYLE_CELL_LEVEL_1 = "cell-level-1";
	public static final String STYLE_CELL_WRAPPED_LEVEL_1 = "cell-level-1-wrapped";
	public static final String STYLE_CELL_DATE_LEVEL_1 = "cell-date-level-1";
	public static final String STYLE_CELL_LINK_LEVEL_1 = "cell-link-level-1";
	
	public static final String STYLE_CELL_LEVEL_2 = "cell-level-2";
	public static final String STYLE_CELL_LEVEL_3 = "cell-level-3";
	public static final String STYLE_CELL_LEVEL_4 = "cell-level-4";
	public static final String STYLE_CELL_LEVEL_5 = "cell-level-5";
	public static final String STYLE_CELL_LEVEL_6 = "cell-level-6";
	public static final String STYLE_CELL_LEVEL_7 = "cell-level-7";
	public static final String STYLE_CELL_LEVEL_8 = "cell-level-8";
	
	// mx
	public static final String STYLE_CELL_MX_PARAM_NA = "cell-mx-param-na"; 
	
	// Number formats
	private static final String INTEGER_FORMAT = "#,##0";
	private static final String DECIMAL_1_DIGIT_FORMAT = "#,##0.0";
	private static final String DECIMAL_2_DIGITS_FORMAT = "#,##0.00";
	
	private static final String DATE_TIME_FORMAT = "m/d/yy h:mm:ss";
	
	private Map<String, CellStyle> defaultStyles;
	
	public DefaultCellStyles(XSSFWorkbook wb, CellFonts fonts){
		defaultStyles = createStyles(wb, fonts);
	}
	
	public CellStyle getStyle(String name){
		return this.defaultStyles.get(name);
	}
	
	public void close(){
		defaultStyles.clear();
		defaultStyles = null;
	}
	
    /**
     * create a library of cell styles
     * @param fonts 
     */
    private Map<String, CellStyle> createStyles(XSSFWorkbook wb, CellFonts fonts){
    	
        Map<String, CellStyle> styles = new HashMap<>();

        CreationHelper createHelper = wb.getCreationHelper();  
        
        // Cell style
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setWrapText(false);
        styles.put(STYLE_CELL, style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(false);
        styles.put(STYLE_CELL_CENTERED, style);

        XSSFCellStyle xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(XSSF_ULTRA_LIGHT_GREY_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setWrapText(false);
        styles.put(STYLE_CELL_GREY_CENTERED, xssfstyle);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        styles.put(STYLE_CELL_CENTERED_WRAPPED, style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setWrapText(false);
        styles.put(STYLE_CELL_ALIGN_RIGHT, style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(false);
        style.setFont(fonts.getFont(CellFonts.FONT_9));
        styles.put(STYLE_CELL_CENTERED_SMALL, style);
        
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(false);
        Font cellFont = wb.createFont();
        cellFont.setItalic(true);
        cellFont.setFontHeightInPoints((short)9);
        style.setFont(cellFont);
        styles.put(STYLE_CELL_CENTERED_SMALL_ITALIC, style);

        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(XSSF_ULTRA_LIGHT_GREY_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setWrapText(false);
        cellFont = wb.createFont();
        cellFont.setItalic(true);
        cellFont.setFontHeightInPoints((short)9);
        xssfstyle.setFont(cellFont);
        styles.put(STYLE_CELL_GREY_CENTERED_SMALL_ITALIC, xssfstyle);
        
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setWrapText(false);
        xssfstyle.setFont(fonts.getFont(CellFonts.FONT_8));
        styles.put(STYLE_CELL_SMALL_TEXT, xssfstyle);

        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setWrapText(true);
        xssfstyle.setFont(fonts.getFont(CellFonts.FONT_8));
        styles.put(STYLE_CELL_VERY_SMALL_TEXT_WRAPPED, xssfstyle);
        
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setWrapText(true);
        xssfstyle.setShrinkToFit(true);
        xssfstyle.setFont(fonts.getFont(CellFonts.FONT_9));
        styles.put(STYLE_CELL_SMALL_TEXT_WRAPPED, xssfstyle);

        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFont(fonts.getFont(CellFonts.FONT_9));
        styles.put(STYLE_CELL_SMALL_TEXT_LEFT_ALIGNED, xssfstyle);
        
        // Cell number style
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(createHelper.createDataFormat().getFormat(INTEGER_FORMAT));
        style.setFont(fonts.getFont(CellFonts.FONT_9));
        styles.put(STYLE_CELL_SMALL_NUMBER, style);
        
        // Cell grey style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(XSSF_ULTRA_LIGHT_GREY_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(false);
        styles.put(STYLE_CELL_GREY, xssfstyle);

        // Cell white style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(XSSF_WHITE_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(false);
        styles.put(STYLE_CELL_WHITE, xssfstyle);
        
        // Cell grey number style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(XSSF_ULTRA_LIGHT_GREY_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.RIGHT);
        xssfstyle.setDataFormat(createHelper.createDataFormat().getFormat(INTEGER_FORMAT));
        styles.put(STYLE_CELL_GREY_NUMBER, xssfstyle);

        // Cell white number style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(XSSF_WHITE_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.RIGHT);
        xssfstyle.setDataFormat(createHelper.createDataFormat().getFormat(INTEGER_FORMAT));
        styles.put(STYLE_CELL_NUMBER_WHITE, xssfstyle);

        // Cell grey shadow style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(XSSF_GREY_SHADOW_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setWrapText(true);
        styles.put(STYLE_CELL_GREY_SHADOW, xssfstyle);
        
        // Cell style level 1 - yellow tons
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(XSSF_ULTRA_LIGHT_GREY_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(false);
        styles.put(STYLE_CELL_LEVEL_1, xssfstyle);

        // Cell style level 1 wrapped - yellow tons
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(XSSF_ULTRA_LIGHT_GREY_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(true);
        styles.put(STYLE_CELL_WRAPPED_LEVEL_1, xssfstyle);
        
        // Cell style level 1 date - yellow tons
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(XSSF_ULTRA_LIGHT_GREY_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(false);
        xssfstyle.setDataFormat(createHelper.createDataFormat().getFormat(DATE_TIME_FORMAT));
        styles.put(STYLE_CELL_DATE_LEVEL_1, xssfstyle);
        
        // Cell style level 1 link - yellow tons
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(XSSF_ULTRA_LIGHT_GREY_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(false);
        xssfstyle.setFont(fonts.getFont(CellFonts.FONT_LINK));
        styles.put(STYLE_CELL_LINK_LEVEL_1, xssfstyle);
        
        // Cell style level 2
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(238, 243, 198), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(false);
        styles.put(STYLE_CELL_LEVEL_2, xssfstyle);
        
        // Cell style level 3
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(238, 250, 171), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(false);
        styles.put(STYLE_CELL_LEVEL_3, xssfstyle);
        
        // Cell style level 4
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(238, 255, 155), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(false);
        styles.put(STYLE_CELL_LEVEL_4, xssfstyle);

        // Cell style level 5 - green tons
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(226, 239, 218), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(false);
        styles.put(STYLE_CELL_LEVEL_5, xssfstyle);
        
        // Cell style level 6
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(215, 244, 214), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(false);
        styles.put(STYLE_CELL_LEVEL_6, xssfstyle);
        
        // Cell style level 7
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(209, 249, 214), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(false);
        styles.put(STYLE_CELL_LEVEL_7, xssfstyle);

        // Cell style level 8
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(206, 252, 244), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(false);
        styles.put(STYLE_CELL_LEVEL_8, xssfstyle);
        
        // Cell italic style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(false);
        xssfstyle.setFont(fonts.getFont(CellFonts.FONT_9_BOLD_ITALIC));
        styles.put(STYLE_CELL_ITALIC, xssfstyle);
        
        // Cell italic style align right
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.RIGHT);
        xssfstyle.setWrapText(false);
        xssfstyle.setFont(fonts.getFont(CellFonts.FONT_9_BOLD_ITALIC));
        styles.put(STYLE_CELL_ITALIC_ALIGN_RIGHT, xssfstyle);
        
        // Cell italic grey style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setFillForegroundColor(XSSF_ULTRA_LIGHT_GREY_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setWrapText(false);
        xssfstyle.setFont(fonts.getFont(CellFonts.FONT_9_BOLD_ITALIC));
        styles.put(STYLE_CELL_GREY_ITALIC, xssfstyle);

        // Cell green centered style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setWrapText(false);
        xssfstyle.setFillForegroundColor(new XSSFColor(new Color(158,213,97), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_CELL_GREEN_BACKGROUND_CENTERED, xssfstyle);        

        // Cell orange centered style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setWrapText(false);
        xssfstyle.setFillForegroundColor(new XSSFColor(new Color(255,204,102), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_CELL_ORANGE_BACKGROUND_CENTERED, xssfstyle);
        
        // Cell italic white style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setFillForegroundColor(XSSF_WHITE_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setWrapText(false);
        xssfstyle.setFont(fonts.getFont(CellFonts.FONT_9_BOLD_ITALIC));
        styles.put(STYLE_CELL_ITALIC_WHITE, xssfstyle);
        
        // Cell date style
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setDataFormat(createHelper.createDataFormat().getFormat(DATE_TIME_FORMAT));
        styles.put(STYLE_CELL_DATE, style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setDataFormat(createHelper.createDataFormat().getFormat(DATE_TIME_FORMAT));
        style.setFont(fonts.getFont(CellFonts.FONT_10));
        styles.put(STYLE_CELL_DATE_CENTERED, style);

        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(XSSF_LIGHT_GREY_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setDataFormat(createHelper.createDataFormat().getFormat(DATE_TIME_FORMAT));
        xssfstyle.setFont(fonts.getFont(CellFonts.FONT_10));
        styles.put(STYLE_CELL_GREY_DATE_CENTERED, xssfstyle);

        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(XSSF_LIGHT_GREEN_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setDataFormat(createHelper.createDataFormat().getFormat(DATE_TIME_FORMAT));
        xssfstyle.setFont(fonts.getFont(CellFonts.FONT_10));
        styles.put(STYLE_CELL_GREEN_DATE_CENTERED, xssfstyle);
        
        // Link style
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFont(fonts.getFont(CellFonts.FONT_LINK));
        styles.put(STYLE_LINK, style);

        // Link style        
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font hlinkFont = wb.createFont();
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        style.setFont(hlinkFont);
        styles.put(STYLE_LINK_CENTERED, style);
        
        // Cell number style
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(createHelper.createDataFormat().getFormat(INTEGER_FORMAT));
        styles.put(STYLE_CELL_NUMBER, style);

        // Cell decimal number style
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(createHelper.createDataFormat().getFormat(DECIMAL_1_DIGIT_FORMAT));
        styles.put(STYLE_CELL_DECIMAL_NUMBER, style);

        // Cell decimal number style
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(createHelper.createDataFormat().getFormat(DECIMAL_2_DIGITS_FORMAT));
        styles.put(STYLE_CELL_DECIMAL_2_NUMBER, style);
        
        // Warning cell style
        style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_WARNING, style);

        // Error cell style        
        style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_ERROR, style);
        
        // Red notice cell style        
        style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setWrapText(true);
        styles.put(STYLE_RED_NOTICE, style);
        
        // Red notice cell style centered
        style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        Font noticeFont = wb.createFont();
        noticeFont.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(noticeFont);
        styles.put(STYLE_RED_NOTICE_CENTERED, style);

        // Orange notice cell style        
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(new XSSFColor(new Color(255,192,0), wb.getStylesSource().getIndexedColors())); // orange
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setWrapText(true);
        styles.put(STYLE_ORANGE_NOTICE, style);
        
        // Missing thread dump header style
        xssfstyle = createBorderedStyle(wb);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(XSSF_LIGHT_GREY_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(fonts.getFont(CellFonts.FONT_10));
        xssfstyle.setBorderRight(BorderStyle.THIN);
        xssfstyle.setRightBorderColor(IndexedColors.RED.getIndex());
        xssfstyle.setBorderLeft(BorderStyle.THIN);
        xssfstyle.setLeftBorderColor(IndexedColors.RED.getIndex());
        styles.put(STYLE_MISSING_TD_HEADER, xssfstyle);
        
        // Missing thread dump cell style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(XSSF_LIGHT_GREY_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setBorderRight(BorderStyle.THIN);
        xssfstyle.setRightBorderColor(IndexedColors.RED.getIndex());
        xssfstyle.setBorderLeft(BorderStyle.THIN);
        xssfstyle.setLeftBorderColor(IndexedColors.RED.getIndex());
        styles.put(STYLE_MISSING_TD_CELL, xssfstyle);
                
        // Restart header style
        xssfstyle = createBorderedStyle(wb);
        Font mtdheaderFont = wb.createFont();
        mtdheaderFont.setFontHeightInPoints((short)11);
        mtdheaderFont.setBold(true);
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(XSSF_APPLE_GREEN_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(mtdheaderFont);
        xssfstyle.setBorderRight(BorderStyle.THIN);
        xssfstyle.setRightBorderColor(IndexedColors.RED.getIndex());
        xssfstyle.setBorderLeft(BorderStyle.THIN);
        xssfstyle.setLeftBorderColor(IndexedColors.RED.getIndex());
        styles.put(STYLE_RESTART_HEADER, xssfstyle);
        
        // Restart cell style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(XSSF_APPLE_GREEN_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SPARSE_DOTS);
        xssfstyle.setBorderRight(BorderStyle.THIN);
        xssfstyle.setRightBorderColor(IndexedColors.RED.getIndex());
        xssfstyle.setBorderLeft(BorderStyle.THIN);
        xssfstyle.setLeftBorderColor(IndexedColors.RED.getIndex());
        styles.put(STYLE_RESTART_COLUMN, xssfstyle);

        // Restart line style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(XSSF_APPLE_GREEN_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SPARSE_DOTS);
        xssfstyle.setBorderTop(BorderStyle.THIN);
        xssfstyle.setTopBorderColor(IndexedColors.RED.getIndex());
        xssfstyle.setBorderBottom(BorderStyle.THIN);
        xssfstyle.setBottomBorderColor(IndexedColors.RED.getIndex());
        Font restartFont = wb.createFont();
        restartFont.setFontHeightInPoints((short)11);
        restartFont.setBold(true);
        xssfstyle.setFont(restartFont);
        styles.put(STYLE_RESTART_LINE, xssfstyle);
        
        // Separator line style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(XSSF_LEAF_GREEN_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_LINE_SEPARATOR, xssfstyle);
        
        // Session page cell style
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(false);
        style.setFont(fonts.getFont(CellFonts.FONT_12));
        styles.put(STYLE_SESSION_CELL, style);

        // Session page text wrap cell style
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setFont(fonts.getFont(CellFonts.FONT_12));
        styles.put(STYLE_SESSION_CELL_TEXT, style);
        
        // Session page warning cell style
        Font warnFont = wb.createFont();
        warnFont.setBold(true);
        warnFont.setColor(IndexedColors.ORANGE.getIndex());
        warnFont.setFontHeightInPoints((short)12);
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFont(warnFont);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(false);
        styles.put(STYLE_SESSION_WARNING_CELL, style);

        // Session page normal cell style
        Font okFont = wb.createFont();
        okFont.setBold(true);
        okFont.setColor(IndexedColors.GREEN.getIndex());
        okFont.setFontHeightInPoints((short)12);
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFont(okFont);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(false);
        styles.put(STYLE_SESSION_OK_CELL, style);
        
        // Level header style
        Font categoryFont = wb.createFont();
        categoryFont.setBold(true);
        categoryFont.setColor(IndexedColors.BLACK.getIndex());
        categoryFont.setFontHeightInPoints((short)16);
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setFont(categoryFont);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        styles.put(STYLE_CATEGORY_HEADER, style);
        
        // Nav or About field name style
        XSSFFont titleFont = wb.createFont();
        titleFont.setColor(new XSSFColor(new java.awt.Color(48,84,150), wb.getStylesSource().getIndexedColors()));
        titleFont.setFontHeightInPoints((short)12);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended
        titleFont.setFontName(CellFonts.FONT_CALIBRI);
        titleFont.setBold(true);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(true);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(242, 242, 242), wb.getStylesSource().getIndexedColors()));  // light blue
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfstyle.setFont(titleFont);
        styles.put(STYLE_NAV_ABOUT_FIELD_NAME, xssfstyle);
        
        // Nav or About field value style
        titleFont = wb.createFont();
        titleFont.setColor(new XSSFColor(new java.awt.Color(128, 128, 128), wb.getStylesSource().getIndexedColors()));
        titleFont.setFontHeightInPoints((short)9);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended
        titleFont.setFontName(CellFonts.FONT_CALIBRI);
        titleFont.setBold(true);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(true);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFont(titleFont);
        styles.put(STYLE_NAV_ABOUT_FIELD_VALUE, xssfstyle);

        // Nav or About field value style
        titleFont = wb.createFont();
        titleFont.setColor(new XSSFColor(new java.awt.Color(48, 84, 150), wb.getStylesSource().getIndexedColors()));  // dark blue
        titleFont.setFontHeightInPoints((short)9);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended
        titleFont.setFontName(CellFonts.FONT_CALIBRI);
        titleFont.setBold(true);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(true);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFont(titleFont);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 192, 0), wb.getStylesSource().getIndexedColors()));  // orange
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_ABOUT_FIELD_WARNING_VALUE, xssfstyle);
        
        // Nav or About field value style
        titleFont = wb.createFont();
        titleFont.setColor(new XSSFColor(new java.awt.Color(255, 255, 255), wb.getStylesSource().getIndexedColors())); // white
        titleFont.setFontHeightInPoints((short)9);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended
        titleFont.setFontName(CellFonts.FONT_CALIBRI);
        titleFont.setBold(true);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(true);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFont(titleFont);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 0, 0), wb.getStylesSource().getIndexedColors()));  // red
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_ABOUT_FIELD_ERROR_VALUE, xssfstyle);

        titleFont = wb.createFont();
        titleFont.setColor(new XSSFColor(new java.awt.Color(255, 255, 255), wb.getStylesSource().getIndexedColors())); // white
        titleFont.setFontHeightInPoints((short)18);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended
        titleFont.setFontName(CellFonts.FONT_MYRIAD_PRO);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(true);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFont(titleFont);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 0, 0), wb.getStylesSource().getIndexedColors()));  // red
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_NAV_ERROR_TITLE, xssfstyle);

        titleFont = wb.createFont();
        titleFont.setColor(new XSSFColor(new java.awt.Color(255, 255, 255), wb.getStylesSource().getIndexedColors())); // white
        titleFont.setFontHeightInPoints((short)18);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended
        titleFont.setFontName(CellFonts.FONT_MYRIAD_PRO);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.LEFT);
        xssfstyle.setWrapText(true);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFont(titleFont);
        xssfstyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 192, 0), wb.getStylesSource().getIndexedColors()));  // orange
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_NAV_WARNING_TITLE, xssfstyle);
        
        titleFont = wb.createFont();
        titleFont.setColor(new XSSFColor(new java.awt.Color(128, 128, 128), wb.getStylesSource().getIndexedColors()));
        titleFont.setFontHeightInPoints((short)9);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended
        titleFont.setFontName(CellFonts.FONT_CALIBRI);
        titleFont.setBold(true);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setWrapText(true);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFont(titleFont);
        styles.put(STYLE_NAV_FIELD_ITEM_VALUE, xssfstyle);
        
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setWrapText(true);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFont(titleFont);
        xssfstyle.setFillForegroundColor(XSSF_ULTRA_LIGHT_GREY_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_NAV_FIELD_ITEM_ZERO_VALUE, xssfstyle);
        
        titleFont = wb.createFont();
        titleFont.setColor(new XSSFColor(new java.awt.Color(255, 255, 255), wb.getStylesSource().getIndexedColors()));
        titleFont.setFontHeightInPoints((short)9);
        // Other nice fonts : Baskerville Old Face, Arial Rounded MT Bold, AR DESTINE, OCR A Extended
        titleFont.setFontName(CellFonts.FONT_CALIBRI);
        titleFont.setBold(true);
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setWrapText(true);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setFont(titleFont);
        styles.put(STYLE_NAV_FIELD_ITEM_CRITICAL_VALUE, xssfstyle);
        
        // Frame simple style
        style = createBorderedStyle(wb);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        styles.put(STYLE_FRAME_SIMPLE, style);
        
    	// Frame left style
        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(false);
        style.setFont(fonts.getFont(CellFonts.FONT_12));
        style.setBorderLeft(BorderStyle.DOUBLE);
        styles.put(STYLE_FRAME_LEFT, style);
        
        // Frame top left style
        style = wb.createCellStyle();
        style.setBorderLeft(BorderStyle.DOUBLE);
        style.setBorderTop(BorderStyle.DOUBLE);
        styles.put(STYLE_FRAME_TOP_LEFT, style);
        
        // Frame top style
        style = wb.createCellStyle();
        style.setBorderTop(BorderStyle.DOUBLE);
        styles.put(STYLE_FRAME_TOP, style);
        
        // Frame top right style
        style = wb.createCellStyle();
        style.setBorderRight(BorderStyle.DOUBLE);
        style.setBorderTop(BorderStyle.DOUBLE);
        styles.put(STYLE_FRAME_TOP_RIGHT, style);
        
        // Frame right style
        style = wb.createCellStyle();
        style.setBorderRight(BorderStyle.DOUBLE);
        styles.put(STYLE_FRAME_RIGHT, style);
        
        // Frame bottom right style
        style = wb.createCellStyle();
        style.setBorderRight(BorderStyle.DOUBLE);
        style.setBorderBottom(BorderStyle.DOUBLE);
        styles.put(STYLE_FRAME_BOTTOM_RIGHT, style);
        
        // Frame bottom style
        style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.DOUBLE);
        styles.put(STYLE_FRAME_BOTTOM, style);
        
        // Frame bottom left style
        style = wb.createCellStyle();
        style.setBorderLeft(BorderStyle.DOUBLE);
        style.setBorderBottom(BorderStyle.DOUBLE);
        styles.put(STYLE_FRAME_BOTTOM_LEFT, style);
        
    	// Graph frame left style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setBorderLeft(BorderStyle.THIN);
        xssfstyle.setFillForegroundColor(XSSF_LIGHT_BROWN_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_GRAPH_FRAME_LEFT, xssfstyle);
        
        // Graph frame top left style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setBorderLeft(BorderStyle.THIN);
        xssfstyle.setBorderTop(BorderStyle.THIN);
        xssfstyle.setFillForegroundColor(XSSF_LIGHT_BROWN_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_GRAPH_FRAME_TOP_LEFT, xssfstyle);
        
        // Graph frame top style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setBorderTop(BorderStyle.THIN);
        xssfstyle.setFillForegroundColor(XSSF_LIGHT_BROWN_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_GRAPH_FRAME_TOP, xssfstyle);
        
        // Graph frame top right style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setWrapText(false);
        xssfstyle.setFont(fonts.getFont(CellFonts.FONT_DOUBLE_UNDERLINE_SYMBOL_9));
        xssfstyle.setBorderRight(BorderStyle.THIN);
        xssfstyle.setBorderTop(BorderStyle.THIN);
        xssfstyle.setFillForegroundColor(XSSF_LIGHT_BROWN_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_GRAPH_FRAME_TOP_RIGHT, xssfstyle);
        
        // Graph frame right style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setBorderRight(BorderStyle.THIN);
        xssfstyle.setFillForegroundColor(XSSF_LIGHT_BROWN_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_GRAPH_FRAME_RIGHT, xssfstyle);
        
        // Graph frame bottom right style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.CENTER);
        xssfstyle.setVerticalAlignment(VerticalAlignment.CENTER);
        xssfstyle.setWrapText(false);
        xssfstyle.setFont(fonts.getFont(CellFonts.FONT_DOUBLE_UNDERLINE_SYMBOL_9));
        xssfstyle.setBorderRight(BorderStyle.THIN);
        xssfstyle.setBorderBottom(BorderStyle.THIN);
        xssfstyle.setFillForegroundColor(XSSF_LIGHT_BROWN_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_GRAPH_FRAME_BOTTOM_RIGHT, xssfstyle);
        
        // Graph frame bottom style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setBorderBottom(BorderStyle.THIN);
        xssfstyle.setFillForegroundColor(XSSF_LIGHT_BROWN_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_GRAPH_FRAME_BOTTOM, xssfstyle);
        
        // Graph frame bottom left style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setBorderLeft(BorderStyle.THIN);
        xssfstyle.setBorderBottom(BorderStyle.THIN);
        xssfstyle.setFillForegroundColor(XSSF_LIGHT_BROWN_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_GRAPH_FRAME_BOTTOM_LEFT, xssfstyle);
        
        // Graph frame inside style
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(XSSF_LIGHT_BROWN_COLOR);
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put(STYLE_GRAPH_FRAME_INSIDE, xssfstyle);
        
        // Color levels
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(new XSSFColor(new Color(255,199,206), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styles.put(STYLE_CELL_LEVEL_CRITICAL, xssfstyle);

        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(new XSSFColor(new Color(237,125,49), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styles.put(STYLE_CELL_LEVEL_ERROR, xssfstyle);
		
        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(new XSSFColor(new Color(255,235,156), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styles.put(STYLE_CELL_LEVEL_WARNING, xssfstyle);

        xssfstyle = wb.createCellStyle();
        xssfstyle.setFillForegroundColor(new XSSFColor(new Color(242,242,242), wb.getStylesSource().getIndexedColors()));
        xssfstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styles.put(STYLE_CELL_LEVEL_INFO, xssfstyle);
		
        // Header style NA
        XSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short)8);
        font.setColor(new XSSFColor(new java.awt.Color(117, 113, 113), wb.getStylesSource().getIndexedColors()));
        xssfstyle = wb.createCellStyle();
        xssfstyle.setAlignment(HorizontalAlignment.RIGHT);
        xssfstyle.setFont(font);
        styles.put(STYLE_CELL_MX_PARAM_NA, xssfstyle);
		
        return styles;
    }	
	
	public String getShadedStyle(int level){
		switch(level){
			case 1 : return STYLE_CELL_LEVEL_1;
			case 2 : return STYLE_CELL_LEVEL_2;
			case 3 : return STYLE_CELL_LEVEL_3;
			case 4 : return STYLE_CELL_LEVEL_4;
			case 5 : return STYLE_CELL_LEVEL_5;
			case 6 : return STYLE_CELL_LEVEL_6;
			case 7 : return STYLE_CELL_LEVEL_7;
			case 8 : return STYLE_CELL_LEVEL_8;
			
			// if higher value
			default : return STYLE_CELL_LEVEL_8; 
		}
	}
	
    private XSSFCellStyle createBorderedStyle(XSSFWorkbook wb){
    	XSSFCellStyle style = wb.createCellStyle();
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        return style;
    }	
	
}
