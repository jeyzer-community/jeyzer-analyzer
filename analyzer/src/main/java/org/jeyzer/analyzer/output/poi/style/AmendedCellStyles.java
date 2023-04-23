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

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

public class AmendedCellStyles {

	private Map<CellStylePattern, CellStyle> amendedStyles = new HashMap<>(30);	

	public CellStyle amendStyleWithAlignment(Workbook wb, CellStyle seedStyle, HorizontalAlignment align) {
		CellStyle resultStyle;
		
		CellStylePattern pattern = new CellStylePattern(seedStyle);
		pattern.setAlignment(align);
		
		resultStyle = this.amendedStyles.get(pattern);
		
		if (resultStyle != null)
			return resultStyle;
		
		// Clone the seed style, amend it and cache it 
		resultStyle = wb.createCellStyle();
		resultStyle.cloneStyleFrom(seedStyle);
		resultStyle.setAlignment(align);
		
		this.amendedStyles.put(pattern, resultStyle);
		
		return resultStyle;
	}

	public CellStyle amendStyleWithDataFormatAndAlignment(Workbook wb, CellStyle seedStyle, short dataFormat, HorizontalAlignment align) {
		CellStyle resultStyle;
		
		CellStylePattern pattern = new CellStylePattern(seedStyle);
		pattern.setDataFormat(dataFormat);
		pattern.setAlignment(align);
		
		resultStyle = this.amendedStyles.get(pattern);
		
		if (resultStyle != null)
			return resultStyle;
		
		// Clone the seed style, amend it and cache it 
		resultStyle = wb.createCellStyle();
		resultStyle.cloneStyleFrom(seedStyle);
		resultStyle.setDataFormat(dataFormat);
		resultStyle.setAlignment(align);
		
		this.amendedStyles.put(pattern, resultStyle);
		
		return resultStyle;
	}

	public CellStyle amendStyleWithFillForegroundColorAndFillPattern(Workbook wb, CellStyle seedStyle, short color, FillPatternType solidForeground) {
		CellStyle resultStyle;
		
		CellStylePattern pattern = new CellStylePattern(seedStyle);
		pattern.setFillForegroundColor(color);
		pattern.setFillPattern(solidForeground);
		
		resultStyle = this.amendedStyles.get(pattern);
		
		if (resultStyle != null)
			return resultStyle;
		
		// Clone the seed style, amend it and cache it 
		resultStyle = wb.createCellStyle();
		resultStyle.cloneStyleFrom(seedStyle);
		resultStyle.setFillForegroundColor(color);
		resultStyle.setFillPattern(solidForeground);
		
		this.amendedStyles.put(pattern, resultStyle);
		
		return resultStyle;
	}

	public CellStyle amendStyleWithFillForegroundColorAndFillPattern(Workbook wb, CellStyle seedStyle, XSSFColor color, FillPatternType solidForeground) {
		XSSFCellStyle resultStyle;
		
		CellStylePattern pattern = new CellStylePattern(seedStyle);
		pattern.setFillForegroundColorRGB(color);
		pattern.setFillPattern(solidForeground);
		
		resultStyle = (XSSFCellStyle)this.amendedStyles.get(pattern);
		
		if (resultStyle != null)
			return resultStyle;
		
		// Clone the seed style, amend it and cache it 
		resultStyle = (XSSFCellStyle)wb.createCellStyle();
		resultStyle.cloneStyleFrom(seedStyle);
		resultStyle.setFillForegroundColor(color);
		resultStyle.setFillPattern(solidForeground);
		
		this.amendedStyles.put(pattern, resultStyle);
		
		return resultStyle;
	}

	public CellStyle amendStyleWithFont(Workbook wb, CellStyle seedStyle, Font newFont) {
		CellStyle resultStyle;
		
		CellStylePattern pattern = new CellStylePattern(seedStyle);
		pattern.setFontIndex(newFont.getIndexAsInt());
		
		resultStyle = this.amendedStyles.get(pattern);
		
		if (resultStyle != null)
			return resultStyle;
		
		// Clone the seed style, amend it and cache it 
		resultStyle = wb.createCellStyle();
		resultStyle.cloneStyleFrom(seedStyle);
		resultStyle.setFont(newFont);
		
		this.amendedStyles.put(pattern, resultStyle);
		
		return resultStyle;
	}

	public CellStyle amendStyleWithFontAndAlignment(Workbook wb, CellStyle seedStyle, Font newFont, HorizontalAlignment align) {
		CellStyle resultStyle;
		
		CellStylePattern pattern = new CellStylePattern(seedStyle);
		pattern.setFontIndex(newFont.getIndexAsInt());
		pattern.setAlignment(align);
		
		resultStyle = this.amendedStyles.get(pattern);
		
		if (resultStyle != null)
			return resultStyle;
		
		// Clone the seed style, amend it and cache it 
		resultStyle = wb.createCellStyle();
		resultStyle.cloneStyleFrom(seedStyle);
		resultStyle.setFont(newFont);
		resultStyle.setAlignment(align);
		
		this.amendedStyles.put(pattern, resultStyle);
		
		return resultStyle;
	}
	
	public CellStyle amendStyleWithFontAndAlignmentAndVerticalAlignment(Workbook wb, CellStyle seedStyle, Font newFont, HorizontalAlignment align, VerticalAlignment verticalAlign) {
		CellStyle resultStyle;
		
		CellStylePattern pattern = new CellStylePattern(seedStyle);
		pattern.setFontIndex(newFont.getIndexAsInt());
		pattern.setAlignment(align);
		
		resultStyle = this.amendedStyles.get(pattern);
		
		if (resultStyle != null)
			return resultStyle;
		
		// Clone the seed style, amend it and cache it 
		resultStyle = wb.createCellStyle();
		resultStyle.cloneStyleFrom(seedStyle);
		resultStyle.setFont(newFont);
		resultStyle.setAlignment(align);
		resultStyle.setVerticalAlignment(verticalAlign);
		
		this.amendedStyles.put(pattern, resultStyle);
		
		return resultStyle;
	}

	public CellStyle amendStyleWithMediumBordersOfColor(Workbook wb, CellStyle seedStyle, short color, boolean bottom, boolean left, boolean right, boolean top){
		CellStyle resultStyle;
		
		CellStylePattern pattern = new CellStylePattern(seedStyle);
		
		if (left){
			pattern.setBorderLeft(BorderStyle.MEDIUM);
			pattern.setLeftBorderColor(color);
		}
		if (right){
			pattern.setBorderRight(BorderStyle.MEDIUM);
			pattern.setRightBorderColor(color);
		}
		if (top){
			pattern.setBorderTop(BorderStyle.MEDIUM);
			pattern.setTopBorderColor(color);
		}
		if (bottom){
			pattern.setBorderBottom(BorderStyle.MEDIUM);
			pattern.setBottomBorderColor(color);
		}
		
		resultStyle = this.amendedStyles.get(pattern);
		
		if (resultStyle != null)
			return resultStyle;
		
		// Clone the seed style, amend it and cache it 
		resultStyle = wb.createCellStyle();
		resultStyle.cloneStyleFrom(seedStyle);
	
		if (left){
			resultStyle.setBorderLeft(BorderStyle.MEDIUM);
			resultStyle.setLeftBorderColor(color);
		}
		if (right){
			resultStyle.setBorderRight(BorderStyle.MEDIUM);
			resultStyle.setRightBorderColor(color);
		}
		if (top){
			resultStyle.setBorderTop(BorderStyle.MEDIUM);
			resultStyle.setTopBorderColor(color);
		}
		if (bottom){
			resultStyle.setBorderBottom(BorderStyle.MEDIUM);
			resultStyle.setBottomBorderColor(color);
		}		
		
		this.amendedStyles.put(pattern, resultStyle);
		
		return resultStyle;
	}

	public CellStyle amendStyleWithMediumBordersOfColor(Workbook wb, CellStyle seedStyle, XSSFColor color, boolean bottom, boolean left, boolean right, boolean top){
		XSSFCellStyle resultStyle;
		
		CellStylePattern pattern = new CellStylePattern(seedStyle);
		
		if (left){
			pattern.setBorderLeft(BorderStyle.MEDIUM);
			pattern.setLeftBorderColorRGB(color);
		}
		if (right){
			pattern.setBorderRight(BorderStyle.MEDIUM);
			pattern.setRightBorderColorRGB(color);
		}
		if (top){
			pattern.setBorderTop(BorderStyle.MEDIUM);
			pattern.setTopBorderColorRGB(color);
		}
		if (bottom){
			pattern.setBorderBottom(BorderStyle.MEDIUM);
			pattern.setBottomBorderColorRGB(color);
		}
		
		resultStyle = (XSSFCellStyle)this.amendedStyles.get(pattern);
		
		if (resultStyle != null)
			return resultStyle;
		
		// Clone the seed style, amend it and cache it 
		resultStyle = (XSSFCellStyle)wb.createCellStyle();
		resultStyle.cloneStyleFrom(seedStyle);
	
		if (left){
			resultStyle.setBorderLeft(BorderStyle.MEDIUM);
			resultStyle.setLeftBorderColor(color);
		}
		if (right){
			resultStyle.setBorderRight(BorderStyle.MEDIUM);
			resultStyle.setRightBorderColor(color);
		}
		if (top){
			resultStyle.setBorderTop(BorderStyle.MEDIUM);
			resultStyle.setTopBorderColor(color);
		}
		if (bottom){
			resultStyle.setBorderBottom(BorderStyle.MEDIUM);
			resultStyle.setBottomBorderColor(color);
		}		
		
		this.amendedStyles.put(pattern, resultStyle);
		
		return resultStyle;
	}

	public CellStyle amendStyleWithWrapText(Workbook wb, CellStyle seedStyle, boolean wrapText) {
		CellStyle resultStyle;
		
		CellStylePattern pattern = new CellStylePattern(seedStyle);
		pattern.setWrapText(wrapText);
		
		resultStyle = this.amendedStyles.get(pattern);
		
		if (resultStyle != null)
			return resultStyle;
		
		// Clone the seed style, amend it and cache it 
		resultStyle = wb.createCellStyle();
		resultStyle.cloneStyleFrom(seedStyle);
		resultStyle.setWrapText(wrapText);
		
		this.amendedStyles.put(pattern, resultStyle);
		
		return resultStyle;
	}

	public CellStyle amendStyleWithWrapTextAndAlignmentAndVerticalAlignment(Workbook wb, CellStyle seedStyle, boolean wrapText, HorizontalAlignment align, VerticalAlignment verticalAlign) {
		CellStyle resultStyle;
		
		CellStylePattern pattern = new CellStylePattern(seedStyle);
		pattern.setWrapText(wrapText);
		pattern.setAlignment(align);
		pattern.setVerticalAlignment(verticalAlign);
		
		resultStyle = this.amendedStyles.get(pattern);
		
		if (resultStyle != null)
			return resultStyle;
		
		// Clone the seed style, amend it and cache it 
		resultStyle = wb.createCellStyle();
		resultStyle.cloneStyleFrom(seedStyle);
		resultStyle.setWrapText(wrapText);
		resultStyle.setAlignment(align);
		resultStyle.setVerticalAlignment(verticalAlign);
		
		this.amendedStyles.put(pattern, resultStyle);
		
		return resultStyle;
	}

	public void close(){
		amendedStyles.clear();
		amendedStyles = null;
	}
	
}
