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



import org.apache.poi.common.usermodel.HyperlinkType;





import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.jeyzer.analyzer.output.poi.rule.header.AbstractNumericDisplayHeaderRule;
import org.jeyzer.analyzer.output.poi.style.CellStyles;

public class MinFunction extends HeaderFunction {

	public static final String RULE_NAME = "min";
	public static final String DISPLAY_NAME = "Min";

	private Double min = null;
	private Cell targetCell;
	
	public MinFunction(HeaderFunction next) {
		super(next);
	}
	
	@Override
	protected void accept(Double value, Cell cell) {
		if (this.min == null || value < this.min){
			this.min = value;
			this.targetCell = cell;
		}
	}
	
	@Override
	protected void displayValue(Workbook workbook, Cell cell, AbstractNumericDisplayHeaderRule header, CellStyles cellStyles) {
		if (this.min == null)
			return;
		
		cell.setCellValue(this.min);
		
		applyThreashold(workbook, cell, this.min, header, cellStyles);
		
		CellReference cellref = new CellReference(this.targetCell.getRowIndex(), this.targetCell.getColumnIndex());
        CreationHelper factory = workbook.getCreationHelper();
        Hyperlink link = factory.createHyperlink(HyperlinkType.DOCUMENT);
        link.setAddress(cellref.formatAsString());
        cell.setHyperlink(link);
	}
	
	@Override
	public String getName() {
		return RULE_NAME;
	}

	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

}
