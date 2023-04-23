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

public class NotSupportedFunction extends HeaderFunction {

	public static final String RULE_NAME = "not_supported_function";
	public static final String DISPLAY_NAME = "";

	public NotSupportedFunction(HeaderFunction next) {
		super(next);
	}
	
	@Override
	protected void accept(Double value, Cell cell) {
		// do nothing
	}

	@Override
	protected void displayValue(Workbook workbook, Cell cell, AbstractNumericDisplayHeaderRule header, CellStyles cellStyles) {
		// do nothing
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
