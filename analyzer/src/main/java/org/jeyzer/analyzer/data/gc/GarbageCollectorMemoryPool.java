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




import org.jeyzer.analyzer.math.FormulaHelper;

public class GarbageCollectorMemoryPool extends AbstractGarbageCollectorMemoryPool{
	
	public static final String G1_EDEN_SPACE_POOL = "G1 Eden Space";
	
	private String name;
	
	public GarbageCollectorMemoryPool(String name, boolean isFresh, boolean maxOptional){
		super(isFresh, maxOptional);
		this.name = name;
		this.maxMemory = -1;
		this.usedMemoryBefore = -1;
		this.usedMemoryAfter = -1;		
	}
	
	public String getName() {
		return name;
	}

	public void setMaxMemory(Long maxMemory) {
		if (maxMemory != null)
			this.maxMemory = maxMemory;
	}

	public void setUsedMemoryBefore(Long usedMemoryBefore) {
		if (usedMemoryBefore != null)		
			this.usedMemoryBefore = usedMemoryBefore;
	}

	public void setUsedMemoryAfter(Long usedMemoryAfter) {
		if (usedMemoryAfter != null)		
			this.usedMemoryAfter = usedMemoryAfter;
	}

	public void updateGarbageCollectorMemoryPool() {
		if (!isValid())
			return; // can happen if GC not yet started : considered as NA case

		this.usedMemoryAfterPercent = FormulaHelper.percent(this.usedMemoryAfter, this.maxMemory);
		
		this.usedMemoryBeforePercent = FormulaHelper.percent(this.usedMemoryBefore, this.maxMemory);
		
		// Take into account the released memory only if fresh event
		if (isFresh)
			this.releasedMemory = this.usedMemoryBefore - this.usedMemoryAfter;
		else
			this.releasedMemory = 0;
		
		// Take into account the released memory only if fresh event
		if (isFresh){
			this.releasedMemoryPercent = FormulaHelper.percent(this.releasedMemory, this.maxMemory);
		}
		else
			this.releasedMemoryPercent = 0;
		
		
		// compute the GC health indication
		// ex : 90.15  -->  very bad. GC released only small amount of memory
		// ex : 5.90  -->  very good. GC released high amount of memory (90% of the used memory)
		// ex : 5.05  -->  GC didn't work so much (5% of the used memory) and pool is currently almost not used
		// if Negative : memory has increased in the pool. Set to 0.
		if (this.usedMemoryBefore != 0  && this.releasedMemory>0){
			this.healthMemPoolIndication = (double) this.releasedMemory / this.usedMemoryBefore;
			if (this.healthMemPoolIndication == 1)
				this.healthMemPoolIndication = 0.99; // special case if all memory released  
		}
		else
			this.healthMemPoolIndication = 0;
		this.healthMemPoolIndication = Math.round(this.usedMemoryAfterPercent) + this.healthMemPoolIndication;  
	}
	
}
