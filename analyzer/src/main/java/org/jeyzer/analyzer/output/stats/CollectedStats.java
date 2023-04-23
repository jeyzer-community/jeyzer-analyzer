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




import org.jeyzer.analyzer.math.FormulaHelper;

public class CollectedStats implements Stats{

	private int actionCount = 0;
	private int stackCount = 0;
	
	private int actionTotal;
	private int stackTotal;
	
	public CollectedStats(int actionTotal, int stackTotal){
		this.actionTotal = actionTotal;
		this.stackTotal = stackTotal;
	}
	
	public CollectedStats(CollectedStats stats){
		this.actionTotal = stats.actionTotal;
		this.stackTotal = stats.stackTotal;
	}
	
	@Override
	public void hitAction(){
		actionCount++;
	}
	
	@Override
	public void hitStack(){
		stackCount++;
	}
	
	@Override
	public int getActionPercentage(){
		return (int)Math.round(FormulaHelper.percent(actionCount, actionTotal));
	}
	
	@Override
	public int getStackPercentage(){
		return (int)Math.round(FormulaHelper.percent(stackCount, stackTotal));
	}	

	@Override
	public int getActionCount(){
		return actionCount;
	}

	@Override
	public int getStackCount(){
		return stackCount;
	}
	
}
