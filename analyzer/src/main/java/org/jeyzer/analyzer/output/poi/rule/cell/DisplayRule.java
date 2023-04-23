package org.jeyzer.analyzer.output.poi.rule.cell;

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
import org.jeyzer.analyzer.data.Action;

public interface DisplayRule {
	
	public boolean apply(Action action, List<Cell> cells);
	
	public int displayLegend(int line, int pos);
	public int displayStats(int line, int pos);
	
	public boolean hasLegend();
	public boolean hasStats();
	public boolean hasStatsToDisplay();
	
}
