package org.jeyzer.analyzer.output.poi.rule.header;

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







import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeader;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.output.poi.CellText;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;
import org.jeyzer.analyzer.util.AnalyzerHelper;

public class DeadlockCounterRule extends AbstractNumericDisplayHeaderRule implements Header{

	public static final String RULE_NAME = "deadlock_counter";
	private static final String DISPLAY_NAME = "Deadlocks";
	public static final String CELL_LABEL_COMMENT = "Deadlock cases count";
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX);
	
	public DeadlockCounterRule(ConfigSheetHeader displayCfg, SheetDisplayContext context) {
		super(displayCfg, context);
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		double countValue;
		double prevCountValue = (long) -1;
		Double value = null; // NA
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			cell = cells.get(j);

			countValue = dumps.get(j).getDeadLockTexts().size();
			value = countValue;
			
			setValue(cell, value);
			function.apply(value, cell);

			setValueBasedColorForeground(
					cell,
					countValue,
					prevCountValue,
					false
					);			
			
			if (!dumps.get(j).getDeadLockTexts().isEmpty())
				setDeadlockText(cell, dumps.get(j).getDeadLockTexts());
			
			prevCountValue = countValue;
		}
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		return line;
	}

	@Override
	public int displayStats(int line, int pos) {
		return line;
	}

	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	@Override
	public String getComment() {
		return CELL_LABEL_COMMENT;
	}

	@Override
	public boolean isPercentageBased() {
		return false;
	}

	@Override
	public String getName() {
		return RULE_NAME;
	}
	
	@Override
	public String getStyle(){
		return getNumberStyle();
	}

	private void setDeadlockText(Cell cell, List<String> deadlockTexts) {
		StringBuilder deadlockText = new StringBuilder();
		
		int maxLength = CellText.COMMENT_COLUMN_CHARS_SIZE;
		for (String text : deadlockTexts){
			deadlockText.append(text);
			maxLength = text.length() > maxLength ? text.length() : maxLength;
			deadlockText.append(""); // separator
		}
		
		int deep = AnalyzerHelper.countLines(deadlockText.toString());
		int length = maxLength / CellText.COMMENT_COLUMN_CHARS_SIZE;
		
        Drawing patr = this.context.getSheet().createDrawingPatriarch();
        CreationHelper factory = this.context.getWorkbook().getCreationHelper();
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + length);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + deep);
        Comment cmt = patr.createCellComment(anchor);
        cmt.setVisible(false);
        cmt.setColumn(cell.getColumnIndex()+1);
        cmt.setRow(cell.getRowIndex()+1);
        
        RichTextString string1 = factory.createRichTextString(deadlockText.toString());
        cmt.setString(string1);
        cmt.setAuthor("Jeyzer");
		
		cell.setCellComment(cmt);
	}
	
	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return SUPPORTED_FUNCTIONS.contains(function);
	}
	
}
