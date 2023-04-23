package org.jeyzer.analyzer.data.stack;

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




import static org.jeyzer.analyzer.math.FormulaHelper.*;
import static org.jeyzer.analyzer.util.SystemHelper.CR;

import org.jeyzer.analyzer.data.memory.MemoryPools;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadStackMemoryInfo {

	private static final Logger logger = LoggerFactory.getLogger(ThreadStackMemoryInfo.class);	
	
	private long allocatedCumulMemory = -1; // bytes
	private long allocatedMemory = -1; // bytes

	// computed
	private double applicativeActivityUsage = 0; // percentage
	private double heapAllocationPercent = -1; // percentage
	
	public ThreadStackMemoryInfo(long allocatedMemory) {
		this.allocatedCumulMemory = allocatedMemory;
	}
	
	public long getAllocatedCumulMemory() {
		return allocatedCumulMemory;
	}

	public long getAllocatedMemory() {
		return allocatedMemory;
	}
	
	// Percentage = thread allocated memory / process memory peak * 100
	public double getApplicativeActivityUsage() {
		return applicativeActivityUsage;
	}	

	// Percentage = thread allocated memory / heap size
	public double getHeapAllocationPercentage() {
		return heapAllocationPercent;
	}
	
	public long updateMemoryData(MemoryPools memoryPools, ThreadStack previousStack, long totalMemory) {
		long prevCumulMemory = 0;
		
		if (allocatedCumulMemory == -1){
			this.allocatedMemory = 0;
			this.heapAllocationPercent = computeHeapAllocationPercent(memoryPools, allocatedMemory);  // 0 or -1
			return 0;
		}
	
		if (previousStack != null){
			if (previousStack.getMemoryInfo() == null){
				this.allocatedMemory = 0; 
				this.heapAllocationPercent = computeHeapAllocationPercent(memoryPools, allocatedMemory);  // 0 or -1
				return 0;
			}
			
			prevCumulMemory = previousStack.getMemoryInfo().getAllocatedCumulMemory();

			if (prevCumulMemory == -1){
				this.allocatedMemory = 0;
				this.heapAllocationPercent = computeHeapAllocationPercent(memoryPools, allocatedMemory);  // 0 or -1
				return 0;
			}
			
		}
		
		long diff = allocatedCumulMemory - prevCumulMemory;
		
		// Crazy memory case seen on Windows jdk1.8.0_25:
		//     "JMX server connection timeout 60" Id=60 in RUNNABLE
		//     cpu time=0	user time=0	memory=646550224756295880   !!!
		// so check against the max memory available with some good error range (x10)
		if (totalMemory !=-1 && diff > totalMemory * 10){
			logger.warn(
					"Invalid memory value reported by the JVM: {} Mb. (Total system memory : {} Mb). Ignoring the value.",
					convertToMb(allocatedCumulMemory), 
					convertToMb(totalMemory));
			this.allocatedMemory = 0;
			this.heapAllocationPercent = computeHeapAllocationPercent(memoryPools, allocatedMemory); // 0 or -1
			return 0;
		}
		
		// difference can be in rare cases negative !! 
		if (diff < 0){
			this.heapAllocationPercent = computeHeapAllocationPercent(memoryPools, 0);  // 0 or -1
			return 0;
		}
		
		this.allocatedMemory = diff;
		this.heapAllocationPercent = computeHeapAllocationPercent(memoryPools, diff);  // some percentage 
		
		return this.allocatedMemory; 
	}
	

	private double computeHeapAllocationPercent(MemoryPools memoryPools, long memory) {
		Long oldGenMax = memoryPools.getOldGenMaxValue();
		Long youngSpaceMax = memoryPools.getYoungSpaceMaxValue();
		boolean youngOptional = memoryPools.isYoungSpaceMaxOptional();
		
		if (oldGenMax != null && oldGenMax !=-1){
			if (youngOptional)
				return FormulaHelper.percent(memory, oldGenMax);
			else if (youngSpaceMax != null && youngSpaceMax != -1)
				return FormulaHelper.percent(memory, youngSpaceMax + oldGenMax);
		}
		
		return -1;
	}

	public void updateApplicativeActivityUsage(long memoryPeak, double timeRatio) {
		// optimize
		if (this.allocatedMemory == 0)
			this.applicativeActivityUsage= 0;
		
		if (memoryPeak != 0){
			long adjustedMemory = (long) (allocatedMemory * timeRatio); // acceptable precision loss 
			this.applicativeActivityUsage = FormulaHelper.percent(adjustedMemory, memoryPeak);
		}
	}

	@Override
	public String toString(){	
		StringBuilder b = new StringBuilder(2000);
		b.append("-  Memory    : " + this.allocatedCumulMemory + " bytes" + CR);
		return b.toString();
	}
	
}
