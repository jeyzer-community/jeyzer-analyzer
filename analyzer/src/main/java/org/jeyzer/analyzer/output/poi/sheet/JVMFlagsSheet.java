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



import static org.jeyzer.analyzer.output.poi.theme.AbstractTheme.STYLE_THEME_TOP_BAR_JEYZER_TITLE;
import static org.jeyzer.analyzer.input.translator.jfr.reader.JFRReader.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.jeyzer.analyzer.config.report.ConfigJVMFlagsSheet;
import org.jeyzer.analyzer.data.flags.JVMFlag;
import org.jeyzer.analyzer.data.flags.JVMFlags;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.poi.context.DisplayContext;
import org.jeyzer.analyzer.output.poi.rule.highlight.HighLightBuilder;
import org.jeyzer.analyzer.output.poi.rule.highlight.Highlight;
import org.jeyzer.analyzer.parser.JFRStackParser;
import org.jeyzer.analyzer.session.JzrSession;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilter;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFilterColumn;

public class JVMFlagsSheet extends JeyzerSheet {
	
	private static final short ORIGIN_COLUMN_INDEX = 3;
	
	private List<String> sizeExceptions = new ArrayList<>();
	
	private ConfigJVMFlagsSheet sheetCfg;
	
	protected List<Highlight> highlights = new ArrayList<>();
	boolean jfrRecord = false; // some fields are only available in JFR
	
	public JVMFlagsSheet(ConfigJVMFlagsSheet jvmFlagsSheetConfig,
			JzrSession session, DisplayContext displayContext) {
		super(session, displayContext);
		this.sheetCfg = jvmFlagsSheetConfig;
		this.highlights = HighLightBuilder.newInstance().buildHighLights(sheetCfg.getOriginHighlights().getHighlights());
		this.jfrRecord = JFRStackParser.FORMAT_SHORT_NAME.equals(session.getFormatShortName());
		fillSizeExceptions();
	}

	@Override
	public void display() {
    	int linePos = 1;
    	int rowPos=0;
    	
    	Sheet sheet = createSheet(this.sheetCfg);
    	
    	rowPos = displayHeaders(sheet, linePos, rowPos);
    	linePos++;
    	
    	linePos = displayFlags(sheet, linePos);
    	
    	sheet.setAutoFilter(new CellRangeAddress(1, linePos - 1, 0, rowPos - 1));
    	filterValues(sheet);
    		
		sheet.createFreezePane(1, 2); // left and top
		
    	addTopBar(sheet, rowPos);
    	addMenuLink(sheet, sheetCfg, STYLE_THEME_TOP_BAR_JEYZER_TITLE, 0, 0);
    	
    	close(this.sheetCfg);
	}

	private void filterValues(Sheet sheet) {
    	if (!this.sheetCfg.areDefaultFlagsAutoFiltered())
    		return;
		
		List<String> values = new ArrayList<>();
		values.add("Command line");
		values.add("Ergonomic");
		values.add("Management");
		// Other values could exist..

		final CTAutoFilter sheetFilter = ((XSSFSheet)sheet).getCTWorksheet().getAutoFilter();
		final CTFilterColumn filterColumn = sheetFilter.addNewFilterColumn();
		filterColumn.setColId(ORIGIN_COLUMN_INDEX);
		final CTFilter filter = filterColumn.addNewFilters().insertNewFilter(1);
		for (String value : values)
			filter.setVal(value); // The filter seems to take only the last value. Works anyway in Excel until a validation will come up.

		// Apply the filter by hiding the rows:
		for (final Row row : sheet) {
			for (final Cell c : row) {
				// Could also filter against the content of the values list
				if (c.getColumnIndex() == ORIGIN_COLUMN_INDEX && c.getStringCellValue().equals("Default")) {
					final XSSFRow r1 = (XSSFRow) c.getRow();
					if (r1.getRowNum() > 1) { // skip header
						r1.getCTRow().setHidden(true);
					}
				}
			}
		}
	}

	private int displayFlags(Sheet sheet, int linePos) {
    	JVMFlags jvmFlags = this.session.getJVMFlags();
    	if (jvmFlags == null)
    		return linePos;

    	for (JVMFlag jvmFlag : jvmFlags.getJVMFlags()) {
    		displayJVMFlag(jvmFlag, sheet, linePos++);
    		itemsCount++;
    	}
    	
		return linePos;
	}

	private int displayHeaders(Sheet sheet, int linePos, int rowPos) {
    	// Header
    	Row row = sheet.createRow(linePos);

    	sheet.setColumnWidth(rowPos, 46*256);
    	addHeaderCell(row, rowPos++, this.jfrRecord ? "Flag" : "Diagnostic flag");
    	
    	sheet.setColumnWidth(rowPos, 20*256);
    	addHeaderCell(row, rowPos++, "Value");    	
    	
    	if (this.jfrRecord) {
        	sheet.setColumnWidth(rowPos, 20*256);
        	addHeaderCell(row, rowPos++, "Old value");
    	}

    	sheet.setColumnWidth(rowPos, 13*256);
    	addHeaderCell(row, rowPos++, "Origin");
    	
    	if (this.jfrRecord) {
        	sheet.setColumnWidth(rowPos, 17*256);
        	addHeaderCell(row, rowPos++, "Change date");    	
        	
        	sheet.setColumnWidth(rowPos, 20*256);
        	addHeaderCell(row, rowPos++, "Type");    		
    	}
    	
		return rowPos;
	}

	private void displayJVMFlag(JVMFlag jvmFlag, Sheet sheet, int rowLine) {
		Row row = sheet.createRow(rowLine);
		int rowPos = 0;
		Cell cell;
		
		cell = addCell(row, rowPos++, jvmFlag.getName(), getCellStylePlainReference(rowLine));
		setColorHighlight(cell, jvmFlag.getOrigin(), this.highlights);
		
		cell = displayValue(row, rowLine, rowPos++, jvmFlag.getValue(), jvmFlag.getType(), jvmFlag.getName());
		setColorHighlight(cell, jvmFlag.getOrigin(), this.highlights);
		
    	if (this.jfrRecord) {
    		cell = displayOldValue(row, rowLine, rowPos++, jvmFlag);
    		setColorHighlight(cell, jvmFlag.getOrigin(), this.highlights);
    	}
		
		cell = addCell(row, rowPos++, jvmFlag.getOrigin(), getCellStylePlainReference(rowLine));
		setColorHighlight(cell, jvmFlag.getOrigin(), this.highlights);
		
    	if (this.jfrRecord) {
    		cell = displayChangedTime(row, rowLine, rowPos++, jvmFlag);
    		setColorHighlight(cell, jvmFlag.getOrigin(), this.highlights);
    		
    		cell = addCell(row, rowPos++, JVMFlag.FLAG_TYPE_NOT_AVAILABLE.equals(jvmFlag.getType()) ? "Not available" : jvmFlag.getType(), getCellStylePlainReference(rowLine));
    		setColorHighlight(cell, jvmFlag.getOrigin(), this.highlights);    		
    	}
	}

	private Cell displayValue(Row row, int rowLine, int rowPos, String value, String type, String name) {
		switch(type) {
			case JFR_JDK_DOUBLEFLAG :
			case JFR_JDK_DOUBLEFLAGCHANGED :
				return addCell(row, rowPos, Double.valueOf(value), getCellStylePlainReference(rowLine), HorizontalAlignment.LEFT);
			case JFR_JDK_INTFLAG :
			case JFR_JDK_INTFLAGCHANGED :
			case JFR_JDK_LONGFLAG :
			case JFR_JDK_LONGFLAGCHANGED :
			case JFR_JDK_UNSIGNEDINTFLAG :
			case JFR_JDK_UNSIGNEDINTFLAGCHANGED :
			case JFR_JDK_UNSIGNEDLONGFLAG :
			case JFR_JDK_UNSIGNEDLONGFLAGCHANGED :
				if (name.endsWith("TableSize") || !name.endsWith("Size") || sizeExceptions.contains(name))
					return addCell(row, rowPos, Long.valueOf(value), getCellStylePlainReference(rowLine), HorizontalAlignment.LEFT);
				else
					return addCell(row, rowPos, FormulaHelper.humanReadableByteCountBin(Long.valueOf(value)), getCellStylePlainReference(rowLine));
			default :
				// String and boolean or not available (JZR recording case)
				return addCell(row, rowPos, (value == null || value.equalsIgnoreCase("null")) ? "" : value, getCellStylePlainReference(rowLine));
		}
	}

	private Cell displayOldValue(Row row, int rowLine, int rowPos, JVMFlag jvmFlag) {
		if (jvmFlag.isChangedValue()) {
			return displayValue(row, rowLine, rowPos, jvmFlag.getOldValue(), jvmFlag.getType(), jvmFlag.getName());
		}
		else {
			return addEmptyCell(row, rowPos, getCellStylePlainReference(rowLine));
		}
	}

	private Cell displayChangedTime(Row row, int rowLine, int rowPos, JVMFlag jvmFlag) {
		if (jvmFlag.isChangedValue()) {
			return addCell(row, rowPos, formatDate(new Date(jvmFlag.getTime()), DATE_TIME_DISPLAY_FORMAT), getCellStylePlainReference(rowLine));
		}
		else {
			return addEmptyCell(row, rowPos, getCellStylePlainReference(rowLine));
		}
	}
	
	private void fillSizeExceptions() {
		this.sizeExceptions.add("FreqInlineSize");
		this.sizeExceptions.add("G1RSetScanBlockSize");
		this.sizeExceptions.add("G1SATBBufferSize");
		this.sizeExceptions.add("InlineThrowMaxSize");
		this.sizeExceptions.add("MaxBCEAEstimateSize");
		this.sizeExceptions.add("MaxElementPrintSize");
		this.sizeExceptions.add("MaxInlineSize");
		this.sizeExceptions.add("MaxSubklassPrintSize");
		this.sizeExceptions.add("MaxTrivialSize");
		this.sizeExceptions.add("OldPLABSize");
		this.sizeExceptions.add("OptoBlockListSize");
		this.sizeExceptions.add("OptoNodeListSize");
		this.sizeExceptions.add("SharedSymbolTableBucketSize");
		this.sizeExceptions.add("ValueMapInitialSize");
		this.sizeExceptions.add("ValueMapMaxLoopSize");
		this.sizeExceptions.add("WarmCallMaxSize");
	}
}
