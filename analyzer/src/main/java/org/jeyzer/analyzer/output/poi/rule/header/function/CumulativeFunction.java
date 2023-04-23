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







import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeyzer.analyzer.output.poi.rule.header.AbstractNumericDisplayHeaderRule;
import org.jeyzer.analyzer.output.poi.style.CellStyles;

public class CumulativeFunction extends HeaderFunction {

	public static final String RULE_NAME = "cumulative";
	public static final String DISPLAY_NAME = "Cumul";

	private Double sum = 0D;
	
	public CumulativeFunction(HeaderFunction next) {
		super(next);
	}	
	
	@Override
	protected void accept(Double value, Cell cell) {
		this.sum += value;
	}

	@Override
	protected void displayValue(Workbook workbook, Cell cell, AbstractNumericDisplayHeaderRule header, CellStyles cellStyles) {
		if (sum == null || sum == Double.doubleToRawLongBits(0))
			return;
		
		cell.setCellValue(sum);
		
		applyThreashold(workbook, cell, sum, header, cellStyles);
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
