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
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.output.poi.CellText;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;
import org.jeyzer.analyzer.util.AnalyzerHelper;

public class BiasedLockCounterRule extends AbstractNumericDisplayHeaderRule implements Header{

	public static final String RULE_NAME = "biased_lock_counter";
	private static final String DISPLAY_NAME = "Biased locks";
	public static final String CELL_LABEL_COMMENT = "Biased locks count";
	
	private static final Set<HeaderFunctionType> SUPPORTED_FUNCTIONS = EnumSet.of(
			HeaderFunctionType.AVERAGE, 
			HeaderFunctionType.MIN,
			HeaderFunctionType.MAX,
			HeaderFunctionType.CUMULATIVE,
			HeaderFunctionType.VARIANCE,
			HeaderFunctionType.STANDARD_DEVIATION);
	
	public BiasedLockCounterRule(ConfigSheetHeader displayCfg, SheetDisplayContext context) {
		super(displayCfg, context);
	}

	@Override
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function) {
		Cell cell;
		double countValue;
		double prevCountValue = (long) -1;
		Double value = null; // NA
		StringBuilder biasedLockText;
		
		// cell value
		for (int j=0; j< dumps.size(); j++){
			biasedLockText = new StringBuilder();
			cell = cells.get(j);
			countValue = 0;

			for (ThreadStack stack : dumps.get(j).getWorkingThreads()){
				if (stack.hasBiasedLocks()){
					List<String> biasedLocks = stack.getBiasedLocks();
					countValue += stack.getBiasedLocks().size();
					updateBiasedLockText(stack, biasedLocks, biasedLockText);
				}
			}
			
			value = countValue;
			
			setValue(cell, value);
			function.apply(value, cell);

			setValueBasedColorForeground(
					cell,
					countValue,
					prevCountValue,
					false
					);			
			
			String commentText = biasedLockText.toString();
			if (!commentText.isEmpty())
				setBiasedLockText(cell, commentText);
			
			prevCountValue = countValue;
		}
		
		return true;
	}

	private void updateBiasedLockText(ThreadStack stack, List<String> biasedLocks, StringBuilder blText) {
		blText.append("== Detected biased lock(s) in stack \"");
		blText.append(stack.getName());
		blText.append(stack.getID());
		blText.append("\" : ==========================\n");
		blText.append("\n");
		for (String biasedLock : biasedLocks){
			blText.append("-- Biased lock : \n");
			blText.append(biasedLock);
			blText.append("\n");
			blText.append("\n");
		}
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

	private void setBiasedLockText(Cell cell, String biasedLockText) {
		int deep = AnalyzerHelper.countLines(biasedLockText);
		int maxLength = AnalyzerHelper.countParagraphMaxLength(biasedLockText);
		if (maxLength == 0)
			return; // no comment to add
		
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
        
        RichTextString string1 = factory.createRichTextString(biasedLockText);
        cmt.setString(string1);
        cmt.setAuthor("Jeyzer");
		
		cell.setCellComment(cmt);
	}
	
	@Override
	public boolean supportFunction(HeaderFunctionType function) {
		return SUPPORTED_FUNCTIONS.contains(function);
	}
	
}
