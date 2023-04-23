package org.jeyzer.analyzer.output.poi.rule.header.function;

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
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.jeyzer.analyzer.output.poi.CellColor;
import org.jeyzer.analyzer.output.poi.rule.header.AbstractNumericDisplayHeaderRule;
import org.jeyzer.analyzer.output.poi.rule.header.Header;
import org.jeyzer.analyzer.output.poi.style.CellStyles;

public abstract class HeaderFunction {
	
	public enum HeaderFunctionType { AVERAGE, MIN, MAX, CUMULATIVE, VARIANCE, STANDARD_DEVIATION }
	
	private HeaderFunction next; // can be null
	
	public HeaderFunction(HeaderFunction next){
		this.next = next;
	}

	public void apply(Double value, Cell cell){
		if (value == null)
			return; // nothing to do with it
		
		accept(value, cell);
		
		if (next != null)
			next.apply(value, cell);
	}
	
	public void apply(Long value, Cell cell){
		if (value == null)
			return; // nothing to do with it
		
		apply(value.doubleValue(), cell);
	}
	
	public void display(Workbook workbook, Iterator<Cell> cellIter, Header header, CellStyles cellStyles){
		if (!cellIter.hasNext())
			return; // unexpected
		
		if (!(header instanceof AbstractNumericDisplayHeaderRule))
			return;
		
		AbstractNumericDisplayHeaderRule source = (AbstractNumericDisplayHeaderRule) header;
		
		Cell cell = cellIter.next();
		if (cell != null)
			displayValue(workbook, cell, source, cellStyles);
		
		if (next != null)
			next.display(workbook, cellIter, header, cellStyles);
	}
	
	public int size(){
		if (next != null)
			return next.size() + 1;
		else
			return 1;
	}
	
	public abstract String getName();
	
	public abstract String getDisplayName();
	
	protected abstract void accept(Double value, Cell cell);
	
	protected abstract void displayValue(Workbook workbook, Cell cell, AbstractNumericDisplayHeaderRule header, CellStyles cellStyles);

	protected void applyThreashold(Workbook workbook, Cell cell, double value, AbstractNumericDisplayHeaderRule header, CellStyles cellStyles){
		String colorName = header.getFunctionColor(getName());
		Integer threshold = header.getFunctionThreshold(getName());
		if (colorName == null || threshold == null)
			return;
		
		Object color = CellColor.buildColor(colorName);
		
		if (value >= threshold){
			setColorForeground(workbook, cell, color, cellStyles);
		}
	}
	
	private void setColorForeground(Workbook workbook, Cell cell, Object color, CellStyles cellStyles){
		if (color instanceof String){
			setColorForeground(workbook, cell, (String) color, cellStyles);
		}
		else if (color instanceof Color){
			setColorForeground(workbook, cell, (Color) color, cellStyles);
		}
		// else do nothing
	}
	
	private void setColorForeground(Workbook workbook, Cell cell, String color, CellStyles cellStyles){
		CellStyle style;
		
		style = cellStyles.getAmendedStyles().amendStyleWithFillForegroundColorAndFillPattern(
				workbook, 
				cell.getCellStyle(), 
				IndexedColors.valueOf(color).getIndex(),
				FillPatternType.SOLID_FOREGROUND
				);
		
		cell.setCellStyle(style);
	}
	
	private void setColorForeground(Workbook workbook, Cell cell, Color color, CellStyles cellStyles){
		CellStyle style;
		
		style = cellStyles.getAmendedStyles().amendStyleWithFillForegroundColorAndFillPattern(
				workbook, 
				cell.getCellStyle(), 
				new XSSFColor(color, null),
				FillPatternType.SOLID_FOREGROUND
				);
		
		cell.setCellStyle(style);
	}
}
