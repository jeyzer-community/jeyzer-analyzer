package org.jeyzer.analyzer.output.poi.context;

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







import org.apache.poi.ss.usermodel.Sheet;

public class SheetDisplayContext extends DisplayContext{
	private Sheet sheet;
    	
	public SheetDisplayContext(DisplayContext context, Sheet sheet){
		super(context);
   		this.sheet = sheet;
   	}
    	
   	public Sheet getSheet() {
		return sheet;
	}
}
