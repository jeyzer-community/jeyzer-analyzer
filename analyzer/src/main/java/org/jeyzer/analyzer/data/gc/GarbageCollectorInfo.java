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



import static org.jeyzer.analyzer.math.FormulaHelper.DOUBLE_TO_LONG_ONE;





import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.setup.MemoryPoolSetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Manages several memory pools : related values (used before, after..) get computed
public class GarbageCollectorInfo extends AbstractGarbageCollectorMemoryPool{

	public static final char GARBAGE_COLLECTOR_SEPARATOR = ':';
	public static final String GARBAGE_COLLECTOR_POOL_MEMORY_USED = "usage:used";
	public static final String GARBAGE_COLLECTOR_POOL_MEMORY_MAX = "usage:max";

	public static final String SERIAL_OLD_GC = "Serial Old";
	public static final String SERIAL_YOUNG_GC = "Def New";
	
	private static final Logger logger = LoggerFactory.getLogger(GarbageCollectorInfo.class);
	
	private String name;
	
	private MemoryPoolSetupManager memPoolSetupManager; // not nice
	
	private long totalCollectionCount = -1;
	private long totalCollectionTime = -1;
	
	// computed
	private long collectionCount = -1;
	private long collectionTime = -1;
	private double collectionTimePercent = -1;
	
	// last gc info
	long lastGCId  = -1;
	long lastGCDuration = -1;
	long lastGCStartTime = -1; // time since process startup in ms
	long lastGCEndTime = -1;  // time since process startup in ms
	
	private Map<String, Long> beforeMemoryPools = new HashMap<>();
	private Map<String, Long> afterMemoryPools = new HashMap<>();
	
	// computed
	private Map<String, GarbageCollectorMemoryPool> memoryPools = new HashMap<>();
	private GarbageCollectorMemoryPool youngMemoryPool;
	private GarbageCollectorMemoryPool oldMemoryPool;
	
	public GarbageCollectorInfo(String name, MemoryPoolSetupManager memPoolSetupManager){
		super(false, false);
		this.name = name;
		this.memPoolSetupManager = memPoolSetupManager;
		this.maxMemory = 0;
		this.usedMemoryBefore = 0;
		this.usedMemoryAfter = 0;
	}
	
	public String getName() {
		return name;
	}

	public long getTotalCollectionCount() {
		return totalCollectionCount;
	}
	public void setTotalCollectionCount(long collectionCount) {
		this.totalCollectionCount = collectionCount;
	}
	
	public long getCollectionCount() {
		return collectionCount;
	}
	
	public long getTotalCollectionTime() {
		return totalCollectionTime;
	}
	public void setTotalCollectionTime(long collectionTime) {
		this.totalCollectionTime = collectionTime;
	}

	public long getCollectionTime() {
		return collectionTime;
	}
	
	public double getCollectionTimePercent() {
		return collectionTimePercent;
	}	
	
	public long getLastGCId() {
		return lastGCId;
	}

	public void setLastGCId(long lastGCId) {
		this.lastGCId = lastGCId;
	}

	public long getLastGCDuration() {
		return lastGCDuration;
	}

	public void setLastGCDuration(long lastGCDuration) {
		this.lastGCDuration = lastGCDuration;
	}

	public long getLastGCStartTime() {
		return lastGCStartTime;
	}

	public void setLastGCStartTime(long lastGCStartTime) {
		this.lastGCStartTime = lastGCStartTime;
	}
	
	public long getLastGCEndTime() {
		return lastGCEndTime;
	}

	public void setLastGCEndTime(long lastGCEndTime) {
		this.lastGCEndTime = lastGCEndTime;
	}
	

	public void addBeforeMemoryPoolEntry(String category, Long value){
		beforeMemoryPools.put(category, value);
	}
	
	public Long getBeforeMemoryPoolValue(String category){
		Long value;
		value = beforeMemoryPools.get(category);
		if (value == null)
			return (long)-1;
		else
			return value;
	}
	
	public void addAfterMemoryPoolEntry(String category, Long value){
		afterMemoryPools.put(category, value);
	}
	
	public Long getAfterMemoryPoolValue(String category){
		Long value;
		value = afterMemoryPools.get(category);
		if (value == null)
			return (long)-1;
		else
			return value;
	}
	
	public GarbageCollectorMemoryPool getGarbageCollectorMemoryPool(String name){
		return this.memoryPools.get(name); // can be null
	}

	public GarbageCollectorMemoryPool getYoungGarbageCollectorMemoryPool(){
		return this.youngMemoryPool;
	}

	public GarbageCollectorMemoryPool getOldGarbageCollectorMemoryPool(){
		return this.oldMemoryPool;
	}
	
	public void updateGarbageCollectorInfo(ThreadDump previous, long sliceTime) {
		if (previous == null){
			// set to NA the computed values
			this.maxMemory = -1;
			this.usedMemoryBefore = -1;
			this.usedMemoryAfter = -1;
			return;
		}
		
		GarbageCollection gc = previous.getGarbageCollection();
		GarbageCollectorInfo prevGCInfo = gc.getGarbageCollectorInfo(this.name);
		if (prevGCInfo == null) // should not be null
			return; 
		
		long prevTotalCount = prevGCInfo.getTotalCollectionCount();
		if (prevTotalCount != -1 && totalCollectionCount != -1){
			if (this.totalCollectionCount - prevTotalCount >= 0)
				this.collectionCount = this.totalCollectionCount - prevTotalCount;
			else
				// handle negative case in case of restart
				this.collectionCount = this.totalCollectionCount;
		}
		
		long prevTotalTime = prevGCInfo.getTotalCollectionTime();
		if (prevTotalTime != -1 && totalCollectionTime != -1){
			if (this.totalCollectionTime - prevTotalTime >= 0)
				this.collectionTime = this.totalCollectionTime - prevTotalTime;
			else
				// handle negative case in case of restart
				this.collectionTime = this.totalCollectionTime;
		}
		
		if (collectionTime != -1 && sliceTime !=0){
			long sliceTimeMs = sliceTime / 1000000L; // convert to ms
			collectionTimePercent = FormulaHelper.percent(collectionTime, sliceTimeMs);
		}
		
		buildMemoryPools(previous);
		
		updateMemoryPools();
		
		updateGlobalMemoryPoolFigures();
	}

	private void buildMemoryPools(ThreadDump previous) {
		// GarbageCollectorMemoryPool should have :
		//  - before and after used memory 
        //  - max memory  (on before or after memory pool)
		
		for (String category : this.beforeMemoryPools.keySet()){
			
			String poolName = parsePoolName(category);
			if (poolName == null)
				continue; //ignore invalid entry
			
			// create pool if required
			GarbageCollectorMemoryPool memoryPool = memoryPools.get(poolName);
			if (memoryPool == null){
				// is it fresh GC data ?
				boolean fresh = computeFresh(previous.getGarbageCollection());
				boolean maxOptional = GarbageCollectorMemoryPool.G1_EDEN_SPACE_POOL.equalsIgnoreCase(poolName);
				memoryPool = new GarbageCollectorMemoryPool(poolName, fresh, maxOptional);
				memoryPools.put(memoryPool.getName(), memoryPool);
				if (this.memPoolSetupManager.isOldMemoryPool(poolName))
					this.oldMemoryPool = memoryPool;
				else if (this.memPoolSetupManager.isYoungMemoryPool(poolName))
					this.youngMemoryPool = memoryPool;
			}
			
			// extract the field name
			if (category.contains(GARBAGE_COLLECTOR_POOL_MEMORY_USED)){
				Long memUsedBefore = beforeMemoryPools.get(category);
				memoryPool.setUsedMemoryBefore(memUsedBefore);  
				Long memUsedAfter = afterMemoryPools.get(category);
				memoryPool.setUsedMemoryAfter(memUsedAfter);  // can be null
			}
			else if (category.contains(GARBAGE_COLLECTOR_POOL_MEMORY_MAX)){
				Long memMax = afterMemoryPools.get(category);
				memoryPool.setMaxMemory(memMax);
			}
		}
	}
	
	private boolean computeFresh(GarbageCollection prevGC) {
		GarbageCollectorInfo prevGCInfo = prevGC.getGarbageCollectorInfo(this.name);
		if (prevGCInfo == null)
			return true;  // assume fresh
		if (this.lastGCEndTime == -1)
			return false;  
		return prevGCInfo.getLastGCEndTime() != this.lastGCEndTime; // assume fresh if prev == -1
	}

	private void updateMemoryPools() {
		for (GarbageCollectorMemoryPool pool : memoryPools.values()){
			pool.updateGarbageCollectorMemoryPool();
		}
	}

	private void updateGlobalMemoryPoolFigures() {
		// all memory pools
		Collection<GarbageCollectorMemoryPool> memoryPools = this.memoryPools.values();
		
		this.releasedMemory = 0;
		this.isFresh = false;
		
		if (SERIAL_OLD_GC.equalsIgnoreCase(this.name) || SERIAL_YOUNG_GC.equalsIgnoreCase(this.name))
			this.isFresh = true;  // Always set to fresh on this type of GC
		
		for (GarbageCollectorMemoryPool memoryPool : memoryPools){
			if (!memoryPool.isValid()){
				// Figures not usable, reset global ones to -1 and return
				this.maxMemory = -1;
				this.usedMemoryBefore = -1;
				this.usedMemoryAfter = -1;
				
				this.usedMemoryAfterPercent = -1;
				this.usedMemoryBeforePercent = -1;
				this.releasedMemory = -1;
				this.releasedMemoryPercent = -1;
				this.healthMemPoolIndication = -1;
				return;
			}

			if (memoryPool.isFresh())
				this.isFresh = true;  // if one of the pool is fresh, set the compound to fresh			
			
			if (memoryPool.getMaxMemory() != -1) // optional case for G1 Eden Space
				this.maxMemory += memoryPool.getMaxMemory();
			this.usedMemoryBefore += memoryPool.getUsedMemoryBefore();
			this.usedMemoryAfter += memoryPool.getUsedMemoryAfter();

			// Take into account the released memory only if fresh event on the pool
			if (memoryPool.isFresh())
				this.releasedMemory += memoryPool.getUsedMemoryBefore() - memoryPool.getUsedMemoryAfter();
		}
		
		this.usedMemoryAfterPercent = FormulaHelper.percent(this.usedMemoryAfter, this.maxMemory); 
		this.usedMemoryBeforePercent = FormulaHelper.percent(this.usedMemoryBefore, this.maxMemory);
		
		// Take into account the released memory only if fresh event
		this.releasedMemoryPercent = FormulaHelper.percent(this.releasedMemory, this.maxMemory);
		
		// compute the GC health indication
		// ex : 90.15  -->  very bad. GC released only small amount of memory
		// ex : 5.90  -->  very good. GC released high amount of memory (90% of the used memory)
		// ex : 5.05  -->  GC didn't work so much (5% of the used memory) and pool is currently almost not used
		// if Negative : memory has increased in the pool. Set to 0.
		if (this.usedMemoryBefore != 0 && this.releasedMemory>0){
			this.healthMemPoolIndication = (double) this.releasedMemory / this.usedMemoryBefore;
			if (Double.doubleToRawLongBits(this.healthMemPoolIndication) == DOUBLE_TO_LONG_ONE)
				this.healthMemPoolIndication = 0.99; // special case if all memory is released
		}
		else
			this.healthMemPoolIndication = 0;
		this.healthMemPoolIndication = Math.round(this.usedMemoryAfterPercent) + this.healthMemPoolIndication;			
	}

	private String parsePoolName(String category) {
		// ex : PS Old Gen:usage:used
		int index = category.indexOf(GARBAGE_COLLECTOR_SEPARATOR);
		if (index <= 0){
			logger.warn("GC pool memory info line invalid {}", category);
			return null;
		}
		else
			return category.substring(0, index);
	}
	
	public boolean isStarted(){
		return this.totalCollectionCount != 0;
	}
}
