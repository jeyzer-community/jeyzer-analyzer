package org.jeyzer.analyzer.data;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2021 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.util.Date;
import java.util.Set;

public abstract class AbstractAction implements Action {
	
	private int id;
	
	// equal to timestamp of the thread dumps that precede and follow the current action
	private Date minStartDate;   // action has started at least after this date 
	private Date maxEndDate;     // action has ended at least before this date 
	
	public AbstractAction(int id) {
		this.id = id;
	}	
	
	@Override
	public int getId(){
		return this.id;
	}
	
	@Override
	public String getStrId(){
		return Integer.toString(this.id);
	}

	@Override
	public Date getMaxEndDate() {
		return maxEndDate;
	}

	@Override
	public Date getMinStartDate() {
		return minStartDate;
	}

	@Override
	public void setMinStartDate(Date minStartDate) {
		this.minStartDate = minStartDate;
	}

	@Override
	public void setMaxEndDate(Date maxEndDate) {
		this.maxEndDate = maxEndDate;
	}
	
	/**
	 * Get duration in seconds
	 */
	@Override
	public long getMinDuration(){
		return (this.getEndDate().getTime()-this.getStartDate().getTime())/1000;
	}	

	@Override
	public long getMaxDuration(){
		return (this.getMaxEndDate().getTime()-this.getMinStartDate().getTime())/1000;
	}
	
	@Override
	public String getCompositeFunction(){
		StringBuilder b = new StringBuilder(30);
		Set<String> keys = getDistinctFunctionTags();
		boolean start = true;
		
		for (String key : keys){
			b.append(start? "" : " - ");
			b.append(key);
			start = false;
		}
		
		return b.toString();
	}
	
	@Override
	public String getCompositeOperation(){
		StringBuilder b = new StringBuilder(30);
		Set<String> keys = getDistinctOperationTags();
		boolean start = true;
		
		for (String key : keys){
			b.append(start? "" : " - ");
			b.append(key);
			start = false;
		}
		
		return b.toString();
	}
	
	@Override
	public String getCompositeContentionType(){
		StringBuilder b = new StringBuilder(30);
		Set<String> keys = getDistinctContentionTypeTags();
		boolean start = true;
		
		for (String key : keys){
			b.append(start? "" : " - ");
			b.append(key);
			start = false;
		}
		
		return b.toString();
	}
}
