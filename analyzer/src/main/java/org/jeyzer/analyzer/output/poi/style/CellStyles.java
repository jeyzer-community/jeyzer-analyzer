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







import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeyzer.analyzer.config.report.ConfigXLSX;

public class CellStyles {

	private CellFonts fonts;
	
	private DefaultCellStyles defaultStyles;
	private AmendedCellStyles amendedStyles;
	private ThemeCellStyles themeStyles;

	public CellStyles(ConfigXLSX config, XSSFWorkbook wb){
		fonts = new CellFonts(wb);
		defaultStyles = new DefaultCellStyles(wb, fonts);
		amendedStyles = new AmendedCellStyles();
		themeStyles = new ThemeCellStyles(config, wb);
	}

	public void close(){
		defaultStyles.close();
		amendedStyles.close();
		themeStyles.close();		
	}
	
	public DefaultCellStyles getDefaultStyles(){
		return defaultStyles;
	}
	
	public CellFonts getCellFonts(){
		return this.fonts;
	}
	
	public AmendedCellStyles getAmendedStyles(){
		return this.amendedStyles;
	}
	
	public ThemeCellStyles getThemeStyles(){
		return this.themeStyles;
	}	
    
}
