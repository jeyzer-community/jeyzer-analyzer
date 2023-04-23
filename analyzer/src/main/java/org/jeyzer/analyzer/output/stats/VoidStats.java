package org.jeyzer.analyzer.output.stats;

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







public class VoidStats implements Stats{
	
	public static final VoidStats VOID_STATS = new VoidStats();  

	@Override
	public void hitAction() {
		// do nothing
	}

	@Override
	public void hitStack() {
		// do nothing
	}

	@Override
	public int getActionPercentage() {
		return 0;
	}

	@Override
	public int getStackPercentage() {
		return 0;
	}

	@Override
	public int getActionCount() {
		return 0;
	}

	@Override
	public int getStackCount() {
		return 0;
	}

}
