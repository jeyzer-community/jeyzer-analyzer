package org.jeyzer.analyzer.output.poi.rule.row;

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







import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.config.report.ConfigDisplay;
import org.jeyzer.analyzer.data.ThreadAction;
import org.jeyzer.analyzer.output.poi.CellText;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;
import org.jeyzer.analyzer.output.poi.rule.AbstractDisplayRule;

public class JeyzerMXAllContextParamsRule extends AbstractDisplayRule implements RowHeader{

	public static final String RULE_NAME = "jzr_mx_all_context_params";

	private static final String DISPLAY_NAME = "display";
	private static final String DISPLAY_VALUE = "Y";
	
	public static final String CELL_LABEL_COMMENT = "List of all applicative context parameters (name value pair)\n associated to the current action.\nObtained through the Jeyzer MX interface.";
	
	private String displayName;
	
	private boolean hasLink;
	
	public JeyzerMXAllContextParamsRule(ConfigDisplay headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
		this.displayName = (String)headerCfg.getValue(DISPLAY_NAME);
		
		this.hasLink = Boolean.parseBoolean((String)headerCfg.getValue(ACTION_LINK_FIELD));
	}

	@Override
	public boolean apply(ThreadAction action, Cell cell) {
		StringBuilder valueBuilder = null; // empty cell
		int depth = 0;
		int maxLength = CellText.COMMENT_COLUMN_CHARS_SIZE;
		
		// cell value
		if (action.getThreadStackJeyzerMXInfo()!=null){
			Map<String, String> params = action.getThreadStackJeyzerMXInfo().getAllContextParams();
			if (!params.isEmpty()){
				valueBuilder = new StringBuilder(50*params.size());
				depth = params.size();
				for (Entry<String, String> entry : params.entrySet()){
					valueBuilder.append(entry.getKey());
					valueBuilder.append(" = ");
					valueBuilder.append(entry.getValue());
					valueBuilder.append("\n");
					
					int length = entry.getKey().length() + entry.getValue().length() + 1;
					if (length > maxLength)
						maxLength = length;
				}
			}
		}
		
		if (valueBuilder != null){
			setValue(cell, DISPLAY_VALUE);
			int commentLength = maxLength / CellText.COMMENT_COLUMN_CHARS_SIZE;
			addComment(cell, valueBuilder.toString(), depth, commentLength);
		}
		
		return true;
	}

	@Override
	public int displayLegend(int line, int pos) {
		return line;
	}

	@Override
	public String getName() {
		return RULE_NAME;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}
	
	@Override
	public String getComment() {
		return CELL_LABEL_COMMENT;
	}

	@Override
	public int getColumnWidth() {
		return 10*256;
	}

	@Override
	public boolean hasActionLink() {
		return hasLink;
	}

	@Override
	public boolean hasLegend() {
		return hasHighlights();
	}
	
}
