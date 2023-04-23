package org.jeyzer.analyzer.data.gc;

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







public abstract class AbstractGarbageCollectorMemoryPool {

	protected long maxMemory; // This is the max memory AFTER garbage collection
	protected long usedMemoryBefore;
	protected long usedMemoryAfter;

	protected boolean isFresh = false;
	protected boolean maxOptional = false;
	
	// computed 
	protected double usedMemoryAfterPercent = -1;
	protected double usedMemoryBeforePercent = -1;
	protected long releasedMemory = -1;
	protected double releasedMemoryPercent = -1;

	protected double healthMemPoolIndication = -1;

	public AbstractGarbageCollectorMemoryPool(boolean fresh, boolean maxOptional){
		this.isFresh = fresh;
		this.maxOptional = maxOptional;
	}

	public long getMaxMemory() {
		return maxMemory;
	}

	public long getUsedMemoryBefore() {
		return usedMemoryBefore;
	}

	public long getUsedMemoryAfter() {
		return usedMemoryAfter;
	}

	public double getUsedMemoryAfterPercent() {
		return usedMemoryAfterPercent;
	}

	public double getUsedMemoryBeforePercent() {
		return usedMemoryBeforePercent;
	}
	
	public long getReleasedMemory() {
		return releasedMemory;
	}

	public double getReleasedMemoryPercent() {
		return releasedMemoryPercent;
	}

	public double getHealthMemPoolIndication() {
		return healthMemPoolIndication;
	}

	public boolean isValid() {
		return !(this.usedMemoryAfter == -1 
				|| this.usedMemoryBefore == -1
				|| ( !this.maxOptional && this.maxMemory == -1 ));
	}

	public boolean isFresh() {
		return isFresh;
	}

}
