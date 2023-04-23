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







import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction;
import org.jeyzer.analyzer.output.poi.rule.header.function.HeaderFunction.HeaderFunctionType;


public interface Header {
	
	public boolean apply(List<ThreadDump> dumps, List<Cell> cells, HeaderFunction function);
	
	public int displayLegend(int line, int pos);

	public int displayStats(int line, int pos);
	
	public String getName();
	
	public String getDisplayName();
	
	public String getComment();
	
	public String getStyle();
	
	public boolean supportFunction(HeaderFunctionType function);

}
