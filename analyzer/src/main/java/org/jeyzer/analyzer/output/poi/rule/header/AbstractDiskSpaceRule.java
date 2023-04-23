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




import org.jeyzer.analyzer.config.report.headers.ConfigSheetHeader;
import org.jeyzer.analyzer.output.poi.context.SheetDisplayContext;

public abstract class AbstractDiskSpaceRule extends AbstractNumericDisplayHeaderRule implements Header{

	private static final String DISK_SPACE_ID = "id";
	private static final String DISK_SPACE_DISPLAY = "display";
	private static final String DISK_SPACE_COMMENT = "comment";
	
	protected static final int NOT_SET = -1;	
	
	protected String id;
	protected String display;
	protected String comment;
	
	public AbstractDiskSpaceRule(ConfigSheetHeader headerCfg, SheetDisplayContext context) {
		super(headerCfg, context);
		this.id = (String)headerCfg.getValue(DISK_SPACE_ID);
		this.display = (String)headerCfg.getValue(DISK_SPACE_DISPLAY);
		this.comment = (String)headerCfg.getValue(DISK_SPACE_COMMENT);
	}
	
	@Override
	public String getDisplayName() {
		return this.display;
	}
	
	@Override
	public String getComment() {
		return this.comment;
	}

}
