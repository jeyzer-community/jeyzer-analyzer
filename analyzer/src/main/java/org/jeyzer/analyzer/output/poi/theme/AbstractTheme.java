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
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public abstract class AbstractTheme {
	
	public static final String STYLE_THEME_MAIN_FRAME = "main-frame";
	public static final String STYLE_THEME_INNER_FRAME = "inner-frame";
	public static final String STYLE_THEME_INNER_FRAME_ITEMS = "inner-frame-items";
	public static final String STYLE_THEME_JEYZER_MAIN_TITLE = "jeyzer-main-title";
	public static final String STYLE_THEME_JZR_REPORT_TITLE = "jzr-report-title";
	public static final String STYLE_THEME_JEYZER_ABOUT_TITLE = "jeyzer-about-title";
	public static final String STYLE_THEME_JEYZER_TITLE = "jeyzer-title";
	public static final String STYLE_THEME_BACKGROUND_FRAME = "background-frame";
	public static final String STYLE_THEME_SESSION_TITLE = "session-title";
	public static final String STYLE_THEME_SESSION_HEADER = "session-header";
	public static final String STYLE_THEME_SESSION = "session";
	
	public static final String STYLE_THEME_TOP_BAR_JEYZER_TITLE = "top-bar-jeyzer-title";
	public static final String STYLE_THEME_TOP_BAR_JEYZER_LEGEND = "top-bar-legend";	
	public static final String STYLE_THEME_TOP_BAR = "top-bar";
	public static final String STYLE_THEME_TOP_BAR_SMALL = "top-bar-small";
	public static final String STYLE_THEME_TOP_BAR_SYMBOL = "top-bar-symbol";
	
	public static final String STYLE_THEME_HEADER = "header";
	public static final String STYLE_THEME_HEADER_ALIGN_LEFT = "header-align-left";
	public static final String STYLE_THEME_HEADER_SMALL = "header-small";
	public static final String STYLE_THEME_HEADER_VERY_SMALL_ROTATED = "header-small-rotated";
	public static final String STYLE_THEME_HEADER_ROW = "header-row";
	public static final String STYLE_THEME_HEADER_ROW_NA = "header-row-na";
	public static final String STYLE_THEME_HEADER_DATE_ROW = "header-row-date";
	public static final String STYLE_THEME_HEADER_TIME = "header-time";
	public static final String STYLE_THEME_HEADER_TIME_ACTION = "header-time-action";
	public static final String STYLE_THEME_HEADER_TIME_ACTION_START = "header-time-action-start";

	public static final String STYLE_THEME_DYNAMIC_HEADER = "dynamic-header";
	public static final String STYLE_THEME_DYNAMIC_HEADER_NUMBER = "dynamic-header-number";
	public static final String STYLE_THEME_DYNAMIC_HEADER_NUMBER_1_DECIMAL = "dynamic-header-number-1-decimal";
	
	public static final String STYLE_THEME_HEADER2 = "header2";
	public static final String STYLE_THEME_HEADER2_VERY_SMALL_ROTATED = "header2-small-rotated";
	
	public static final String STYLE_THEME_SECTION_HEADER = "section-header";
	public static final String STYLE_THEME_SECTION_HEADER_BOLD = "section-header-bold";
	
	public static final String STYLE_THEME_SECTION_ABOUT_CATEGORY = "section-about-category";
	
	public abstract Map<String, CellStyle> createThemeStyles(Map<String, CellStyle> styles, XSSFWorkbook wb, CreationHelper createHelper);

	protected XSSFCellStyle createBorderedStyle(XSSFWorkbook wb){
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
