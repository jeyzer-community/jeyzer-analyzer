package org.jeyzer.analyzer.data.virtual;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jeyzer.analyzer.data.stack.AbstractThreadStack;
import org.jeyzer.analyzer.data.stack.ThreadState;


public class VirtualThreadStackImpl extends AbstractThreadStack {
	
	// Aggregation of ids, and count of vts
	private List<String> vtIds = Collections.synchronizedList(new ArrayList<>());

	public VirtualThreadStackImpl(String id, ThreadState state, int filePos, String fileName, Date timestamp, List<String> codeLines){
		super("Aggregation virtual-thread " + id,
				id,
				id,
				state,
				filePos,
				fileName,
				timestamp,
				codeLines);
	}
	
	public void integrateStack(String id) {
		this.vtIds.add(id);
	}
	
	public List<String> getVirtualThreadIds(){
		return this.vtIds;
	}
	
	@Override
	public int getInstanceCount() {
		return this.vtIds.size();
	}
	
	@Override
	public boolean isVirtual() {
		return true;
	}

	@Override
	public boolean hasUniqueInstance() {
		return this.vtIds.size() == 1;
	}
}
