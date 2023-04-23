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

import org.jeyzer.analyzer.data.stack.ThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadStackJeyzerMXInfo;

public interface Action {

	public int size();
	public int getId();	
	public String getStrId();
	public Date getStartDate();	
	public Date getEndDate();
	public Date getMaxEndDate();
	public Date getMinStartDate();
	public void setMinStartDate(Date minStartDate);
	public void setMaxEndDate(Date maxEndDate);
	public long getCpuTime();
	public double getCpuUsageMax();
	public double getCpuUsage();
	public double getApplicativeCpuActivityUsageMax();
	public long getAllocatedMemory();
	public double getApplicativeMemoryActivityUsageMax();
	public long getMinDuration();
	public long getMaxDuration();
	public String getExecutor();
	public String getThreadId();
	public String getName();
	public int getPriority();
	public ThreadStackJeyzerMXInfo getThreadStackJeyzerMXInfo();
	public Set<String> getDistinctFunctionTags();
	public String getCompositeFunction();
	public Set<String> getDistinctOperationTags();
	public String getCompositeOperation();
	public String getOperationTags(int pos);
	public Set<String> getDistinctContentionTypeTags();
	public String getContentionTypeTags(int pos);
	public String getCompositeContentionType();
	public ThreadStack getThreadStack(int pos);
	public String getPrincipalContentionType(int pos);
	public String getPrincipalOperation(int pos);
	public String getPrincipalFunction(int pos);
	public String getPrincipalCompositeFunction();
	public String getPrincipalCompositeOperation();
	public String getPrincipalCompositeContentionType();
	public String getFunctionTags(int pos);
}
