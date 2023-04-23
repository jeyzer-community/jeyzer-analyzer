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

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeyzer.analyzer.config.report.ConfigXLSX;
import org.jeyzer.analyzer.output.poi.theme.BlueTheme;
import org.jeyzer.analyzer.output.poi.theme.VinciTheme;

public class ThemeCellStyles {
	
	private String theme;	

	private Map<String, CellStyle> themeStyles;	
	
	public ThemeCellStyles(ConfigXLSX config, XSSFWorkbook wb){
		theme = config.getTheme();
		themeStyles = createThemeStyles(config, wb);		
	}
	
	public CellStyle getThemeStyle(String name){
		return themeStyles.get(name);
	}

	public XSSFColor getThemeTitleColor() {
        if (VinciTheme.NAME.equals(theme))
        	return VinciTheme.THEME_TITLE_COLOR;
        else
        	return BlueTheme.THEME_TITLE_COLOR;
	}

	public void close(){
		themeStyles.clear();
		themeStyles = null;
	}
	
	private Map<String, CellStyle> createThemeStyles(ConfigXLSX config, XSSFWorkbook wb) {
        Map<String, CellStyle> styles = new HashMap<>();
        CreationHelper createHelper = wb.getCreationHelper();
        if (VinciTheme.NAME.equals(config.getTheme()))
        	return new VinciTheme().createThemeStyles(styles, wb, createHelper);
        else
        	return new BlueTheme().createThemeStyles(styles, wb, createHelper);
	}
	
}
