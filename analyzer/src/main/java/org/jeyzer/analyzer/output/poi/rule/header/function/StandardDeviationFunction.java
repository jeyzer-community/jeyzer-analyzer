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







import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.output.poi.rule.header.AbstractNumericDisplayHeaderRule;
import org.jeyzer.analyzer.output.poi.style.CellStyles;

public class StandardDeviationFunction extends HeaderFunction {

	public static final String RULE_NAME = "standard_deviation";
	public static final String DISPLAY_NAME = "Std Dev";

	private List<Double> values = new ArrayList<>();
	private Double sum = 0D;
	private int count = 0;
	
	public StandardDeviationFunction(HeaderFunction next) {
		super(next);
	}	
	
	@Override
	protected void accept(Double value, Cell cell) {
		this.values.add(value);
		this.sum += value;
		this.count++;
	}

	@Override
	protected void displayValue(Workbook workbook, Cell cell, AbstractNumericDisplayHeaderRule header, CellStyles cellStyles) {
		if (sum == null || sum == Double.doubleToRawLongBits(0))
			return;
		
		double average = sum / count;
		double deviation = FormulaHelper.calculateStandardDeviation(values, average);
		
		cell.setCellValue(Math.round(deviation));
		
		applyThreashold(workbook, cell, deviation, header, cellStyles);
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
