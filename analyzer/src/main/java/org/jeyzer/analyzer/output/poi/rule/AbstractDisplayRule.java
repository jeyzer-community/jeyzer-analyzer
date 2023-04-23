package org.jeyzer.analyzer.output.poi.rule;

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



import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;





import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.output.poi.CellColor;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.highlight.HighLightBuilder;
import org.jeyzer.analyzer.output.poi.rule.highlight.Highlight;
import org.jeyzer.analyzer.output.poi.rule.highlight.Highlight.HighlightLegendElement;
import org.jeyzer.analyzer.output.poi.style.AmendedCellStyles;
import org.jeyzer.analyzer.output.poi.style.CellFonts;


public abstract class AbstractDisplayRule {
    	
	public static final String COLOR_FIELD = "color";
	public static final String STATS_FIELD = "stats";
	
	protected static final String NOT_AVAILABLE = "NA";
	protected static final short NO_SIZE_CHANGE = -1;	
	protected static final String COLOR_NOT_AVAILABLE = "GREY_25_PERCENT";
	
	protected Object color = null; // string or color RGB
	protected SheetDisplayContext context;
	protected List<Highlight> highlights = new ArrayList<>();
	
	public AbstractDisplayRule(ConfigDisplay displayCfg, SheetDisplayContext context){
		this.color = CellColor.buildColor((String)displayCfg.getValue(COLOR_FIELD));
		
		this.highlights = HighLightBuilder.newInstance().buildHighLights(displayCfg.getHighlights());

		this.context = context;
	}

	public abstract String getName(); 
	
	protected void setValue(Cell cell, String value){
		setValue(cell, value, true);
	}
	
	protected void setValue(Cell cell, String value, boolean delimiter){
		String prevValue;
		String cellValue;
		
		// update existing cell if any
		prevValue = cell.getStringCellValue();
		if (delimiter && !prevValue.isEmpty())
			cellValue = prevValue + "/" + value;
		else
			cellValue = prevValue + " " + value;
		
		cell.setCellValue(cellValue);
	}
	
	protected void appendSmallValue(Cell cell, String value){
		String prevValue;
		RichTextString richValue = null;
		
		prevValue = cell.getStringCellValue();
		if (prevValue!= null && !prevValue.isEmpty()) {
			richValue = createRichText(prevValue, value);
		}
		else {
			RichTextString prevRichValue = cell.getRichStringCellValue();
			if (prevRichValue!= null) {
				prevValue = prevRichValue.getString();
				richValue = createRichText(prevValue, value);
			}
		}
		
		if (richValue != null)
			cell.setCellValue(richValue);
	}
	
	private RichTextString createRichText(String text, String smallText){
        String finalText = text + "/" + smallText;
        CreationHelper factory = this.context.getWorkbook().getCreationHelper();
        RichTextString richText = factory.createRichTextString(finalText);
        richText.applyFont(0, text.length(), getFont(CellFonts.FONT_11));
        richText.applyFont(text.length() + 1, finalText.length(), getFont(CellFonts.FONT_9));
        return richText;
	}
	
	protected void setColorForeground(Cell cell){
		setColorForeground(cell, this.color);
	}
	
	protected Font getFont(String fontName){
		return this.context.getCellStyles().getCellFonts().getFont(fontName);
	}
	
	protected AmendedCellStyles getAmendedStyles(){
		return this.context.getCellStyles().getAmendedStyles();
	}
	
	protected CellStyle getStyle(String name){
		return this.context.getCellStyles().getDefaultStyles().getStyle(name);
	}
	
	protected CellStyle getThemeStyle(String name){
		return this.context.getCellStyles().getThemeStyles().getThemeStyle(name);
	}		
	
	protected void setColorForeground(Cell cell, Object color){
		if (color instanceof String){
			setColorForeground(cell, (String) color);
		}
		else if (color instanceof Color){
			setColorForeground(cell, (Color) color);
		}
		// else do nothing
	}
	
	private void setColorForeground(Cell cell, String color){
		CellStyle style;
		
		style = getAmendedStyles().amendStyleWithFillForegroundColorAndFillPattern(
				this.context.getWorkbook(), 
				cell.getCellStyle(), 
				IndexedColors.valueOf(color).getIndex(),
				FillPatternType.SOLID_FOREGROUND
				);
		
		cell.setCellStyle(style);
	}
	
	private void setColorForeground(Cell cell, Color color){
		CellStyle style;
		
		style = getAmendedStyles().amendStyleWithFillForegroundColorAndFillPattern(
				this.context.getWorkbook(), 
				cell.getCellStyle(), 
				new XSSFColor(color),
				FillPatternType.SOLID_FOREGROUND
				);
		
		cell.setCellStyle(style);
	}
	
    protected void setBorders(Cell cell, Object color, boolean left, boolean right, boolean top, boolean bottom){
		if (color instanceof String){
			setBorders(cell, (String)color, left, right, top, bottom);
		}
		else if (color instanceof Color){
			setBorders(cell, (Color)color, left, right, top, bottom);
		}
		// else do nothing
    }
	
    private void setBorders(Cell cell, String color, boolean left, boolean right, boolean top, boolean bottom){
		CellStyle style;
		
		style = getAmendedStyles().amendStyleWithMediumBordersOfColor(
				this.context.getWorkbook(), 
				cell.getCellStyle(),
				IndexedColors.valueOf(color).getIndex(),
				bottom,
				left,
				right,
				top
				);
		
		cell.setCellStyle(style);
    }

    private void setBorders(Cell cell, Color color, boolean left, boolean right, boolean top, boolean bottom){
		CellStyle style;
		
		style = getAmendedStyles().amendStyleWithMediumBordersOfColor(
				this.context.getWorkbook(), 
				cell.getCellStyle(),
				new XSSFColor(color),
				bottom,
				left,
				right,
				top
				);
		
		cell.setCellStyle(style);
    }
    
    
	protected void setColorHighlight(Cell cell, String value){
		for (Highlight hl : this.highlights){
			if (hl.match(value)){
				setColorForeground(cell,hl.getColor());
				return; // on first match return
			}
		}
	}

    protected void setBorders(Cell cell){
    	CellStyle style;
    	
    	style = cell.getCellStyle();
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());

    }

    protected int displayHighlightLegend(int line, int pos, String prefix){
		Row row;
    	Cell cell;
    	
    	for (Highlight hl : this.highlights){
    		
    		List<HighlightLegendElement> legendElements = hl.getLegendElements();
    		
    		for (HighlightLegendElement legendElement : legendElements){
        		row = getRow(line);
        		
        		// code color + name
        		cell = row.createCell(pos);
        		this.setColorForeground(cell, hl.getColor());
        		setBorders(cell);
        		
        		// label
        		if(legendElement.getLabel() != null)
        			cell.setCellValue(legendElement.getLabel());
        		
        		// description
        		String description = legendElement.getDescription();
        		if (prefix != null)
        			description = prefix + description;
        		cell = row.createCell(pos+1);
        		cell.setCellValue("  " + description);
    			cell.setCellStyle(getStyle(STYLE_CELL_SMALL_TEXT_LEFT_ALIGNED));
    			
        		++line;
    		}
    	}
    	
    	return line;
    }
    
    
    protected int displayHighlightLegend(int line, int pos){
    	return displayHighlightLegend(line, pos, null);
    }
    
    protected boolean hasHighlights(){
    	return !this.highlights.isEmpty();
    }
    
    protected int displayLegend(String description, String label, boolean color, int line, int pos){
		return displayLegend(description, label, color, NO_SIZE_CHANGE, line, pos);
    }
    
    protected int displayLegend(String description, String label, boolean color, short size, int line, int pos){
		Row row;
    	Cell cell;
		
		row = getRow(line);
		
		// code color + name
		cell = row.createCell(pos);
		if (color)
			setColorForeground(cell);
		if(label != null)
			cell.setCellValue(label);
		setBorders(cell);

		// size
		if (size != NO_SIZE_CHANGE)
			changeCellTextSize(cell, size);
		
		// label
		cell = row.createCell(pos+1);
		cell.setCellValue("  " + description);
		cell.setCellStyle(getStyle(STYLE_CELL_SMALL_TEXT_LEFT_ALIGNED));
		
		return ++line;
    }
	
	protected void underlineCellText(Cell cell) {
		CellStyle style;

		Font oldFont = this.context.getWorkbook().getFontAt(cell.getCellStyle().getFontIndex());
		if (Font.U_SINGLE == oldFont.getUnderline())
			return; // already he case. May be not expected (?)

		Font newFont = CellFonts.getUnderlinedFont(this.context.getWorkbook(), oldFont, Font.U_SINGLE);
		
		style = getAmendedStyles().amendStyleWithFont(
				this.context.getWorkbook(), 
				cell.getCellStyle(),
				newFont
				);
		
		cell.setCellStyle(style);
	}	
	

	protected void changeCellTextSize(Cell cell, short size) {
		CellStyle style;

		Font oldFont = this.context.getWorkbook().getFontAt(cell.getCellStyle().getFontIndex());
		Font newFont = CellFonts.copyFont(this.context.getWorkbook(), oldFont);
		newFont.setFontHeightInPoints(size);
		
		style = getAmendedStyles().amendStyleWithFont(
				this.context.getWorkbook(), 
				cell.getCellStyle(),
				newFont
				);
		
		cell.setCellStyle(style);
	}
	
	protected Row getRow(int line){
		Row row = this.context.getSheet().getRow(line);
		if (row == null)
			row = this.context.getSheet().createRow(line);
		return row;
	}
	
	protected void addComment(Cell cell, String comment, int depth, int length){
        Drawing patr = this.context.getSheet().createDrawingPatriarch();
        CreationHelper factory = this.context.getWorkbook().getCreationHelper();
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + length);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + depth);
        Comment cmt = patr.createCellComment(anchor);
        cmt.setVisible(false);
        cmt.setColumn(cell.getColumnIndex()+1);
        cmt.setRow(cell.getRowIndex()+1);
        
        RichTextString string1 = factory.createRichTextString(comment);
        string1.applyFont(getFont(CellFonts.FONT_9));
        cmt.setString(string1);
        cmt.setAuthor("Jeyzer");
        cell.setCellComment(cmt);
	}
}
