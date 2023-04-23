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







import java.awt.Color;

import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFColor;

public class CellColor {

	public static final String RGB_PREFIX = "RGB";
	
	// colors
	// used for graph picture frame
	public static final XSSFColor XSSF_LIGHT_BROWN_COLOR = new XSSFColor(new java.awt.Color(255, 230, 153));
	// used for restart
	public static final XSSFColor XSSF_APPLE_GREEN_COLOR = new XSSFColor(new java.awt.Color(196, 229, 159));
	// used for line separator
	public static final XSSFColor XSSF_LEAF_GREEN_COLOR = new XSSFColor(new java.awt.Color(84, 130, 53));
	// used for event applicative times
	public static final XSSFColor XSSF_LIGHT_GREEN_COLOR = new XSSFColor(new java.awt.Color(210, 252, 208));
	// used for thread dump hiatus
	public static final XSSFColor XSSF_LIGHT_GREY_COLOR = new XSSFColor(new java.awt.Color(243, 243, 241));
	// used for table lines :
	public static final XSSFColor XSSF_WHITE_COLOR = new XSSFColor(new java.awt.Color(255, 255, 255));
	public static final XSSFColor XSSF_ULTRA_LIGHT_GREY_COLOR = new XSSFColor(new java.awt.Color(238, 236, 225));
	// used for chart bottom bars :
	public static final XSSFColor XSSF_GREY_SHADOW_COLOR = new XSSFColor(new java.awt.Color(208, 206, 206));

	private static final HSSFPalette PALETTE = (new HSSFWorkbook()).getCustomPalette();
	
	public static Object buildColor(String color){
		if (color == null)
			// not set
			return null;
		
		if (color.startsWith(RGB_PREFIX)){
			return buildFromRGB(color);
		}
		
		// indexed color
		return color;
	}
	
	public static Color buildRGBColor(Object color){
		if (color == null)
			// not set
			return null;
		
		if (color instanceof Color)
			return (Color)color;

		if (!(color instanceof String))
			return null;
		
		String indexedColor = (String)color;
		
		// String index
		HSSFColor hssfColor = PALETTE.getColor(IndexedColors.valueOf(indexedColor).getIndex());
		short[] triplet = hssfColor.getTriplet();
		return new Color(triplet[0], triplet[1], triplet[2]);
	}
	
	private static Color buildFromRGB(String color) {
		int red, green, blue;
		
		// RGB color
		int posr = color.indexOf('-')+1;
		int posg = color.indexOf('-', posr)+1;
		int posb = color.indexOf('-', posg)+1;
		
		red   = Integer.valueOf(color.substring(posr, posg-1));
		green = Integer.valueOf(color.substring(posg, posb-1));
		blue  = Integer.valueOf(color.substring(posb));
		
		return new Color(red, green, blue);
	}

}
