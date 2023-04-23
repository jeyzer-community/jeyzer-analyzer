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




import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;

public class TendencyContentionTypeRule extends AbstractTendencyRule implements Header {

	public static final String RULE_NAME = "tendency_contention_type";
	
	public static final String CELL_LABEL_COMMENT = "Most frequent contention type.\nContention type must at least appear 3 times within the recording snapshot.";
	
	private static final String DISPLAY_NAME = "Tendency contention type";
	
	public TendencyContentionTypeRule(ConfigDisplay headerCfg, SheetDisplayContext context){
		super(headerCfg, context);
	}

	@Override
	public String getName() {
		return RULE_NAME;
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
	protected String getElementValue(ThreadStack stack) {
		return stack.getPrincipalContentionType().intern();
	}

}
