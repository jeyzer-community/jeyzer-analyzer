package org.jeyzer.analyzer.output.poi.sheet;

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



import static org.jeyzer.analyzer.output.poi.style.CellFonts.FONT_SYMBOL_11;
import static org.jeyzer.analyzer.output.poi.style.CellFonts.FONT_SYMBOL_BOLD_10;
import static org.jeyzer.analyzer.output.poi.style.DefaultCellStyles.*;
import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.*;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.util.CellReference;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jeyzer.analyzer.config.report.ConfigSheet;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture;
import org.jeyzer.analyzer.output.graph.picture.ExcelGraphPicture.GraphType;
import org.jeyzer.analyzer.output.poi.CellColor;
import org.jeyzer.analyzer.output.poi.CellText;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.output.poi.context.DisplayContext.MenuItemsContext;
import org.jeyzer.analyzer.output.poi.rule.highlight.Highlight;
import org.jeyzer.analyzer.output.poi.style.AmendedCellStyles;
import org.jeyzer.analyzer.output.poi.style.CellFonts;
import org.jeyzer.analyzer.output.poi.style.CellStyles;
import org.jeyzer.analyzer.session.JzrSession;
import org.jeyzer.analyzer.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JeyzerSheet {

	protected static final Logger logger = LoggerFactory.getLogger(JeyzerSheet.class);
		
	protected static final String JEYZER = "Jeyzer";
	
	protected static final String NOT_AVAILABLE = "NA";
	
	public static final String TIME_DISPLAY_FORMAT = "HH:mm:ss";
	public static final String DAY_DISPLAY_FORMAT = "dd MMM yyyy";
	public static final String DATE_TIME_DISPLAY_FORMAT = "E MMM dd HH:mm:ss yyyy";
	
	// thread safe as initialized on startup only
	private static final Map<String, SimpleDateFormat> formatters = new HashMap<>(5);
	static {
		formatters.put(TIME_DISPLAY_FORMAT, new SimpleDateFormat(TIME_DISPLAY_FORMAT, Locale.US));
		formatters.put(DAY_DISPLAY_FORMAT, new SimpleDateFormat(DAY_DISPLAY_FORMAT, Locale.US));
		formatters.put(DATE_TIME_DISPLAY_FORMAT, new SimpleDateFormat(DATE_TIME_DISPLAY_FORMAT, Locale.US));
	}
	
	protected DisplayContext displayContext;
	protected CellStyles styles;
	protected Workbook workbook;
	protected JzrSession session;
	
	protected int itemsCount = 0;
	protected int graphItemsCount = 0;
	
	public JeyzerSheet(JzrSession session, DisplayContext displayContext){
		this.displayContext = displayContext;
		this.workbook = displayContext.getWorkbook();
		this.styles = displayContext.getCellStyles();
		this.session = session;
	}
	
	public abstract void display() throws JzrException;
	
	protected void close(ConfigSheet cfgSheet){
		this.displayContext.registerMenuItems(
				cfgSheet.getName(),
				new MenuItemsContext(
						this.itemsCount,
						this.graphItemsCount
						)
				);
	}
	
	// can be overriden
	protected Logger getLogger(){
		return logger;
	}

	protected Cell addCell(Row row, int pos, String text){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellValue(text);
		
		return cell;
	}	
	
	protected Cell addCell(Row row, int pos, XSSFRichTextString text){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellValue(text);
		
		return cell;
	}	
	
	protected Cell addCell(Row row, int pos, XSSFRichTextString text, String style){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellValue(text);
		cell.setCellStyle(getStyle(style));
		
		return cell;
	}
	
	protected Cell addCell(Row row, int pos, String text, String style, String color){
		Cell cell;
		
		cell = addCell(row, pos, text, style);
		setColorForeground(cell, CellColor.buildColor(color));
		
		return cell;
	}	
	
	protected Cell addCell(Row row, int pos, String text, String style){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellValue(text);
		cell.setCellStyle(getStyle(style));
		
		return cell;
	}
	
	protected Cell addEmptyRowHeaderCell(Row row, int pos){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellStyle(this.getThemeStyle(STYLE_THEME_HEADER_ROW));
		
		return cell;
	}
	
	protected Cell addHeaderCell(Row row, int pos, String text){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellValue(text);
		cell.setCellStyle(this.getThemeStyle(STYLE_THEME_HEADER));
		
		return cell;
	}
	
	protected Cell addHeaderCell(Row row, int pos, String text, String style){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellValue(text);
		cell.setCellStyle(this.getThemeStyle(style));
		
		return cell;
	}	
	
	protected Cell addHeaderCell(Row row, int pos, XSSFRichTextString text){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellValue(text);
		cell.setCellStyle(this.getThemeStyle(STYLE_THEME_HEADER));
		
		return cell;
	}

	protected Cell addEmptyCell(Row row, int pos){
		Cell cell;
		
		cell = row.createCell(pos);
		
		return cell;
	}
	
	protected Cell addEmptyCell(Row row, int pos, String style){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellStyle(getStyle(style));
		
		return cell;
	}

	protected Cell addEmptyCell(Row row, int pos, String style, String color){
		Cell cell;
		
		cell = addEmptyCell(row, pos, style);
		setColorForeground(cell, CellColor.buildColor(color));
		
		return cell;
	}
	
	protected Cell addCell(Row row, int pos, Date date, String style){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellValue(date);
		cell.setCellStyle(getStyle(style));
		
		return cell;
	}

	protected Cell addCell(Row row, int pos, long value, String style){
		return addCell(row, pos, value, style, HorizontalAlignment.RIGHT);  // Excel standard
	}
	
	protected Cell addCell(Row row, int pos, long value, String style, HorizontalAlignment alignment){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellStyle(getStyle(style));
		cell.setCellValue((double)value);

		// amend style to align on the right
		CellStyle newStyle;
		newStyle = getAmendedStyles().amendStyleWithAlignment(
				this.workbook,
				cell.getCellStyle(),
				alignment
				);
		
		cell.setCellStyle(newStyle);
		
		return cell;
	}
	
	protected Cell addCell(Row row, int pos, Double value, String style){
		return addCell(row, pos, value, style, HorizontalAlignment.RIGHT); // Excel standard
	}
	
	protected Cell addCell(Row row, int pos, Double value, String style, HorizontalAlignment alignment){
		Cell cell;
		
		cell = row.createCell(pos);
		if (value != null)
			cell.setCellValue(value);  // numeric cell
		else 
			cell.setCellValue(NOT_AVAILABLE);
		cell.setCellStyle(getStyle(style));
		
		CellStyle newStyle;
		CreationHelper createHelper = this.workbook.getCreationHelper();
		newStyle = getAmendedStyles().amendStyleWithDataFormatAndAlignment(
				this.workbook,
				cell.getCellStyle(),
				createHelper.createDataFormat().getFormat("#,##0.0"),
				alignment
				);
		
		cell.setCellStyle(newStyle);
        
		return cell;
	}

	protected Cell addCell(Row row, int pos, long value, CellReference cellref, String style){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellStyle(getStyle(style));
		cell.setCellValue((double)value);
		addDocumentHyperLink(cell, cellref.formatAsString());
		
		return cell;
	}
	
	protected Cell addCell(Row row, int pos, long value, String style, Font font){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellStyle(getStyle(style));
		cell.setCellValue((double)value);

		// amend style to align on the right 
		CellStyle newStyle;
		newStyle = getAmendedStyles().amendStyleWithFontAndAlignment(
				this.workbook,
				cell.getCellStyle(),
				font,
				HorizontalAlignment.RIGHT
				);
		
		cell.setCellStyle(newStyle);
		
		return cell;
	}

	protected Cell addCell(Row row, int pos, String value, String cellref, String style, Font font){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellType(CellType.NUMERIC);
		cell.setCellValue(value);
		
		// add the hyper link
		addDocumentHyperLink(cell, cellref);

		CellStyle newStyle;
		newStyle = getAmendedStyles().amendStyleWithFontAndAlignment(
				this.workbook,
				getStyle(style),
				font,
				HorizontalAlignment.RIGHT
				);
		
		cell.setCellStyle(newStyle);
		
		return cell;
	}
	
	protected Cell addCell(Row row, int pos, String value, String style, Font font){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellType(CellType.STRING);
		cell.setCellStyle(getStyle(style)); // @todo : remove
		cell.setCellValue(value);

		CellStyle newStyle;
		newStyle = getAmendedStyles().amendStyleWithFontAndAlignment(
				this.workbook,
				cell.getCellStyle(),
				font,
				HorizontalAlignment.CENTER
				);
		
		cell.setCellStyle(newStyle);
		
		return cell;
	}
	
	protected Cell addCell(Row row, int pos, String value, String cellref, CellStyle style, Font font){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellType(CellType.NUMERIC);
		cell.setCellValue(value);
		
		// add the hyper link
		addDocumentHyperLink(cell, cellref);

		CellStyle newStyle;
		newStyle = getAmendedStyles().amendStyleWithFontAndAlignmentAndVerticalAlignment(
				this.workbook,
				style,
				font,
				HorizontalAlignment.CENTER,
				VerticalAlignment.CENTER
				);
		
		cell.setCellStyle(newStyle);
		
		return cell;
	}	
	
	protected Cell addCell(Row row, int pos, String value, String cellref, CellStyle style){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellType(CellType.NUMERIC);
		cell.setCellStyle(style);
		cell.setCellValue(value);
		
		// add the hyper link
		addDocumentHyperLink(cell, cellref);
		
		return cell;
	}	

	protected Cell addCell(Row row, int pos, String value, CellReference cellref, String style){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellStyle(getStyle(style));
//		cell.setCellType(CellType.NUMERIC);
		cell.setCellValue(value);
		addDocumentHyperLink(cell, cellref.formatAsString());
		
		return cell;
	}
	
	protected Cell addCell(Row row, int pos, String value, CellReference cellref, String style, Font font){
		Cell cell;
		
		cell = row.createCell(pos);
		cell.setCellStyle(getStyle(style));
		cell.setCellType(CellType.NUMERIC);
		cell.setCellValue(value);

		CellStyle newStyle;
		newStyle = getAmendedStyles().amendStyleWithFontAndAlignment(
				this.workbook,
				cell.getCellStyle(),
				font,
				HorizontalAlignment.CENTER
				);
		
		cell.setCellStyle(newStyle);
		
		addDocumentHyperLink(cell, cellref.formatAsString());
		
		return cell;
	}

    protected Sheet createSheet(ConfigSheet config){
    	return createSheet(config, config.getName());
    }
    
    protected Sheet createSheet(ConfigSheet config, String name){
    	XSSFSheet sheet = ((XSSFWorkbook)workbook).createSheet(name);
    	
    	if (config.getColor() != null)
    		sheet.setTabColor(config.getColor());
    	
    	return sheet;
    }
    
	protected boolean createColumnGroup(Sheet sheet, int start, int end, boolean collapsed) {
		if (!this.displayContext.getSetupManager().isColumnGroupingEbabled())
			return false; // disabled, do nothing
		
		sheet.groupColumn(start, end);
		sheet.setColumnGroupCollapsed(start, collapsed);
		
		return true;
	}    
	
	protected void prepareSheet(Sheet sheet){
		sheet.setAutobreaks(true);
		sheet.setFitToPage(true);
		sheet.setHorizontallyCenter(true);
	}

	protected void addDocumentHyperLink(Cell cell, String docref){
		CreationHelper factory = this.workbook.getCreationHelper();
		Hyperlink link = factory.createHyperlink(HyperlinkType.DOCUMENT);
		link.setAddress(docref);
		cell.setHyperlink(link);
	}
	
	protected void addDocumentHyperLinkURL(Cell cell, String url){
		CreationHelper factory = this.workbook.getCreationHelper();
		Hyperlink link = factory.createHyperlink(HyperlinkType.URL);
		link.setAddress(url);
		cell.setHyperlink(link);
	}
	
	protected void addDocumentHyperLinkEmail(Cell cell, String email){
		CreationHelper factory = this.workbook.getCreationHelper();
		Hyperlink link = factory.createHyperlink(HyperlinkType.EMAIL);
		link.setAddress(email);
		cell.setHyperlink(link);
	}
	
	protected void addComment(Sheet sheet, Cell cell, String comment, int rowDepth, int columnLength){
        Drawing patr = sheet.createDrawingPatriarch();
        CreationHelper factory = this.workbook.getCreationHelper();
        
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + columnLength);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + rowDepth);
        Comment cmt = patr.createCellComment(anchor);
        cmt.setVisible(false);
        RichTextString string1 = factory.createRichTextString(comment);
        string1.applyFont(this.getConstantFont(CellFonts.FONT_9));
        cmt.setString(string1);
        cell.setCellComment(cmt);
	}
	
	protected void setColorHighlight(Cell cell, String value, List<Highlight> highlights){
		for (Highlight hl : highlights){
			if (hl.match(value)){
				setColorForeground(cell,hl.getColor());
				return; // on first match return
			}
		}
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
		CellStyle newStyle;
		IndexedColors indexedColor = IndexedColors.valueOf(color); 

		if (indexedColor == null)
			return;
		
		newStyle = getAmendedStyles().amendStyleWithFillForegroundColorAndFillPattern(
				this.workbook, 
				cell.getCellStyle(), 
				indexedColor.getIndex(),
				FillPatternType.SOLID_FOREGROUND
				);
		
		cell.setCellStyle(newStyle);
	}
	
	private void setColorForeground(Cell cell, Color color){
		setColorForeground(cell, new XSSFColor(color, null));
	}
	
	protected void setColorForeground(Cell cell, XSSFColor color){
		CellStyle newStyle;

		newStyle = getAmendedStyles().amendStyleWithFillForegroundColorAndFillPattern(
				this.workbook, 
				cell.getCellStyle(), 
				color,
				FillPatternType.SOLID_FOREGROUND
				);
		
		cell.setCellStyle(newStyle);
	}
	
    protected String formatDate(Date date, String format) {
    	SimpleDateFormat sdf = formatters.get(format);
		
		if (!session.getDisplayTimeZoneInfo().isUnknown())
			sdf.setTimeZone(session.getDisplayTimeZoneInfo().getZone());  // keep it
		
		return sdf.format(date);
	}
    
    protected String formatRecordingDate(Date date, String format) {
    	SimpleDateFormat sdf = formatters.get(format);
    	SimpleDateFormat sdfRec = (SimpleDateFormat)sdf.clone();
    	
		if (!session.getRecordingTimeZoneInfo().isUnknown())
			sdfRec.setTimeZone(session.getRecordingTimeZoneInfo().getZone());
		
		return sdfRec.format(date);
	}

    protected void wrapText(Cell cell){
		CellStyle newStyle;
		newStyle = getAmendedStyles().amendStyleWithWrapTextAndAlignmentAndVerticalAlignment(
				this.workbook,
				cell.getCellStyle(),
				true,
				HorizontalAlignment.CENTER,
				VerticalAlignment.TOP
				);
		
		cell.setCellStyle(newStyle);
	}
    
    protected Date convertToTimeZone(Date date) {
    	
		if (!session.getDisplayTimeZoneInfo().isUnknown()){
			// the given date is in local time
			long localOffset = new GregorianCalendar().getTimeZone().getRawOffset();
			long localDstOffset = new GregorianCalendar().getTimeZone().getDSTSavings();
			
			// the requested display time zone
			long targetOffset = session.getDisplayTimeZoneInfo().getZone().getRawOffset();
			long targetDstOffset = session.getDisplayTimeZoneInfo().getZone().getDSTSavings();
			
			// align the local time to UTC and then align to the target time, considering also the DST offsets
			return new Date(date.getTime() - localOffset - localDstOffset + targetOffset + targetDstOffset); // align to GMT and then apply target offset
		}
		
		return date;
	}

	
	protected String getCellStyleNumberReference(int count){
		return (count % 2 == 0) ? STYLE_CELL_GREY_NUMBER : STYLE_CELL_NUMBER;
	}

	protected String getCellStyleReference(int count){
		return (count % 2 == 0) ? STYLE_CELL_GREY : STYLE_CELL;
	}
	
	protected String getCellStyleItalicReference(int count){
		return (count % 2 == 0) ? STYLE_CELL_GREY_ITALIC : STYLE_CELL_ITALIC;
	}
	
	protected String getCellStylePlainNumberReference(int count){
		return (count % 2 == 0) ? STYLE_CELL_GREY_NUMBER : STYLE_CELL_NUMBER_WHITE;
	}

	protected String getCellStylePlainReference(int count){
		return (count % 2 == 0) ? STYLE_CELL_GREY : STYLE_CELL_WHITE;
	}
	
	protected String getCellStylePlainItalicReference(int count){
		return (count % 2 == 0) ? STYLE_CELL_GREY_ITALIC : STYLE_CELL_ITALIC_WHITE;
	}
	
	protected CellStyle getStyle(String style){
		return 	this.styles.getDefaultStyles().getStyle(style);
	}
	
	protected String getShadedStyle(int level){
		return 	this.styles.getDefaultStyles().getShadedStyle(level);
	}	
	
	protected Font getConstantFont(String font){
		return 	this.styles.getCellFonts().getFont(font);
	}
	
	protected XSSFFont getConstantXSSFont(String font){
		return 	this.styles.getCellFonts().getFont(font);
	}
	
	protected CellStyle getThemeStyle(String style){
		return 	this.styles.getThemeStyles().getThemeStyle(style);
	}
	
	protected AmendedCellStyles getAmendedStyles(){
		return this.styles.getAmendedStyles();
	}
	
	protected XSSFColor getThemeTitleColor(){
		return 	this.styles.getThemeStyles().getThemeTitleColor();
	}

	protected void addMenuLink(Sheet sheet, ConfigSheet cfgSheet, String style, int linePos, int rowPos){
		Row row = getValidRow(sheet, linePos);
		Cell cell = row.createCell(rowPos);
		cell.setCellStyle(getThemeStyle(style));
		cell.setCellValue(JEYZER);
		
		if (cfgSheet.isNavigationEnabled()){
			CellReference cellref = new CellReference(
					NavigationMenuSheet.SHEET_NAME, 
					1, 2, true, true);  // Refer the Jeyzer cell of the menu sheet
			addDocumentHyperLink(cell, cellref.formatAsString());
		}
 	}	
	
	protected void addTopBar(Sheet sheet, int rowMax) {
    	Row row = sheet.createRow(0);
    	for (int rowPos=0; rowPos<rowMax; rowPos++){
    		Cell cell = row.createCell(rowPos);
    		cell.setCellStyle(getThemeStyle(STYLE_THEME_TOP_BAR));
    	}
	}

	protected void addMainFrame(Sheet sheet, ConfigSheet cfgSheet, int lineMax, int rowMax) {
		int linePos;
		Row row;
    	
    	for (linePos=1; linePos<lineMax; linePos++){
    		row = getValidRow(sheet, linePos);
    		Cell cell = row.createCell(0);
    		cell.setCellStyle(getThemeStyle(STYLE_THEME_MAIN_FRAME));
    	}
    	
		row = sheet.createRow(0);
    	row.setHeightInPoints(15);
    	for (int rowPos=0; rowPos<rowMax; rowPos++){
    		Cell cell = row.createCell(rowPos);
    		cell.setCellStyle(getThemeStyle(STYLE_THEME_MAIN_FRAME));
    	}
    	
    	addMenuLink(sheet, cfgSheet, STYLE_THEME_JEYZER_TITLE, 0, 1);
	}
	
    
    protected Cell getValidCell(Row row, int pos){
	   	Cell cell = row.getCell(pos);
	   	if (cell == null)
	   		cell = row.createCell(pos);
    	return cell;
    }
    
    protected Row getValidRow(Sheet sheet, int pos){
	   	Row row = sheet.getRow(pos);
	   	if (row == null)
	   		row = sheet.createRow(pos);
    	return row;
    }
    
	protected int addPicture(Sheet sheet, String picturePath, int pictureIdx, int colIndex, int rowIndex, double xCellFactor, double yCellFactor) {
	    return addPicture(sheet, picturePath, pictureIdx, colIndex, rowIndex, xCellFactor, yCellFactor, 0, 0);
	}
	
	protected int addPicture(Sheet sheet, String picturePath, int pictureIdx, int colIndex, int rowIndex, double xCellFactor, double yCellFactor, int dx1, int dy1) {
	    if (pictureIdx == -1)
	    	pictureIdx = addPictureToWorkbook(picturePath);

	    CreationHelper helper = workbook.getCreationHelper();

	    // Create the drawing patriarch.  This is the top level container for all shapes. 
	    Drawing drawing = sheet.createDrawingPatriarch();

	    //add a picture shape
	    ClientAnchor anchor = helper.createClientAnchor();
	    //set top-left corner of the picture,
	    //subsequent call of Picture#resize() will operate relative to it
	    anchor.setCol1(colIndex);
	    anchor.setRow1(rowIndex);
	    anchor.setDx1(dx1 * Units.EMU_PER_POINT);
	    anchor.setDy1(dy1 * Units.EMU_PER_POINT);
	    Picture pict = drawing.createPicture(anchor, pictureIdx);

	    // pict.resize(((double)1/65.87)*factor, factor); // relative to size cell.
	    pict.resize(xCellFactor, yCellFactor); // relative to size cell.
	    
	    return pictureIdx;
	}
	
	private int addPictureToWorkbook(String picturePath) {
		int pictureIdx = -2;
		
	    try (
	    	    InputStream is = new FileInputStream(picturePath);
	    	)
	    {
			byte[] bytes = null;
			bytes = IOUtils.toByteArray(is);
			pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
		} catch (IOException e) {
			getLogger().warn("Failed to load the menu image from : " + picturePath, e);
		}
	    
	    return pictureIdx;
	}

	protected int addGraphPicture(Sheet sheet, String name, ExcelGraphPicture graphPicture, int linePos, final int frameRowPos, GraphType type) {
		int graphPictureColumnSize = graphPicture.getExcelWidth();
		int graphPictureRowSize = graphPicture.getExcelHeight();
		
		// add frame
		addGraphFrameBorders(sheet, linePos, linePos + graphPictureRowSize + 1, frameRowPos, frameRowPos + graphPictureColumnSize + 1);
		
		// fill cells in frame
		for(int l=linePos+1; l<linePos + graphPictureRowSize + 1; l++){
			Row cellRowToFill = getValidRow(sheet, l);
			for(int c=frameRowPos+1; c<frameRowPos + graphPictureColumnSize + 1; c++){
				addEmptyCell(cellRowToFill, c, STYLE_GRAPH_FRAME_INSIDE);
			}
		}
		
		// set the child cell link on top right of the frame
		Cell parentCell = graphPicture.getParentLinkCell();
		CellReference parentCellRef = new CellReference(
				name, 
				parentCell.getRowIndex(), 
				parentCell.getColumnIndex(),
				true, 
				true);
		
		// top right cell
		Row childTopCellRow = getValidRow(sheet, linePos);
		Cell childCell = getValidCell(childTopCellRow, (int)(frameRowPos + graphPictureColumnSize + 1));
		childCell.setCellValue(CellText.FONT_SYMBOL_UP_ARROW);
		addDocumentHyperLink(childCell, parentCellRef.formatAsString());
		
		// bottom right cell
		Row childBottomCellRow = getValidRow(sheet, linePos + graphPictureRowSize + 1);
		childCell = getValidCell(childBottomCellRow, (int)(frameRowPos + graphPictureColumnSize + 1));
		childCell.setCellValue(CellText.FONT_SYMBOL_UP_ARROW);
		addDocumentHyperLink(childCell, parentCellRef.formatAsString());
		
		// display picture
		linePos++;
		int index = addPicture(
				sheet, 
				graphPicture.getPicturePath(),
				graphPicture.getIndex(),
				frameRowPos+1,
				linePos,
				graphPictureColumnSize,
				graphPictureRowSize
				);
		if (!graphPicture.hasIndex())
			graphPicture.setIndex(index);
		
		linePos += graphPictureRowSize;
		
		// recreate parent cell and add link to bottom right of the picture 
		CellReference childCellRef = new CellReference(
				name, 
				linePos, 
				childCell.getColumnIndex(),
				true, 
				true);
		switch(type){
			case CONTENTION_GRAPH_SINGLE:
				addCell(parentCell.getRow(), 
						parentCell.getColumnIndex(), 
						CellText.FONT_SYMBOL_OMEGA, 
						childCellRef.formatAsString(), 
						getThemeStyle(STYLE_THEME_TOP_BAR_SYMBOL));
				break;
			case ACTION_GRAPH_SINGLE:
				addCell(parentCell.getRow(), 
						parentCell.getColumnIndex(), 
						CellText.FONT_SYMBOL_CLOVER, 
						childCellRef.formatAsString(),
						getThemeStyle(STYLE_THEME_TOP_BAR_SYMBOL));
				break;
			case CONTENTION_GRAPH:
				addCell(parentCell.getRow(), 
						parentCell.getColumnIndex(), 
						CellText.FONT_SYMBOL_OMEGA, 
						childCellRef.formatAsString(), 
						parentCell.getCellStyle(), 
						getConstantFont(FONT_SYMBOL_BOLD_10));
				break;
			case ACTION_GRAPH:
			default:
				addCell(parentCell.getRow(), 
						parentCell.getColumnIndex(), 
						CellText.FONT_SYMBOL_CLOVER, 
						childCellRef.formatAsString(), 
						parentCell.getCellStyle(), 
						getConstantFont(FONT_SYMBOL_11));
		}

		linePos += 10;
		
		return linePos; 
	}
	
	private void addGraphFrameBorders(Sheet sheet, int startLine, int endLine, int startPos, int endPos){
    	Row row;
    	Cell cell;

    	sheet.setColumnWidth(startPos, 3*256);
    	sheet.setColumnWidth(endPos, 3*256);
    	
    	// top cells
        row = getValidRow(sheet, startLine);
    	for (int i=startPos; i<endPos+1; i++){
    		cell = getValidCell(row, i);
    	   	if (i==startPos)
    	   		cell.setCellStyle(getStyle(STYLE_GRAPH_FRAME_TOP_LEFT));
    	   	else if (i==endPos)
    	   		cell.setCellStyle(getStyle(STYLE_GRAPH_FRAME_TOP_RIGHT));
    	   	else 
    	   		cell.setCellStyle(getStyle(STYLE_GRAPH_FRAME_TOP));
    	}
        
    	// left and right cells
    	for (int j=startLine+1; j<endLine; j++){
    		row = getValidRow(sheet, j);

    	   	cell = getValidCell(row, startPos);
    		cell.setCellStyle(getStyle(STYLE_GRAPH_FRAME_LEFT));

    		cell = getValidCell(row, endPos);
    		cell.setCellStyle(getStyle(STYLE_GRAPH_FRAME_RIGHT));
    	}
    	
    	// bottom cells
    	row = getValidRow(sheet, endLine);
    	for (int i=startPos; i<endPos+1; i++){
    	   	cell = getValidCell(row, i);
    	   	if (i==startPos)
    	   		cell.setCellStyle(getStyle(STYLE_GRAPH_FRAME_BOTTOM_LEFT));
    	   	else if (i==endPos)
    	   		cell.setCellStyle(getStyle(STYLE_GRAPH_FRAME_BOTTOM_RIGHT));
    	   	else 
    	   		cell.setCellStyle(getStyle(STYLE_GRAPH_FRAME_BOTTOM));
    	}
    }	

	protected void clearGraphPictures(Collection<ExcelGraphPicture> graphPictures) {
		if (!this.displayContext.getSetupManager().getGraphSetupMgr().isArchivingEnabled()){
			for (ExcelGraphPicture picture : graphPictures){
				SystemHelper.deleteFile(picture.getPicturePath());
			}
		}
	}
	
	protected void clearGraphPicture(ExcelGraphPicture graphPicture) {
		if (!this.displayContext.getSetupManager().getGraphSetupMgr().isArchivingEnabled()){
			SystemHelper.deleteFile(graphPicture.getPicturePath());
		}
	}
	
	protected void registerActionLink(int actionId, String sheetName, Cell cell) {
		if (!this.displayContext.getSetupManager().isActionLinkEnabled())
			return;
		CellReference cellref = new CellReference(sheetName, cell.getRowIndex(), cell.getColumnIndex(), true, true);
		this.displayContext.getCellRefRepository().addActionRef(Integer.toString(actionId), cellref);
	}
	
	protected String secureCellDisplayValue(String value) {
		return value.length() > CellText.CELL_VALUE_MAX_SIZE ? value.substring(0, CellText.CELL_VALUE_MAX_SIZE - 5) + "[...]" : value;
	}
}
