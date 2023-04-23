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







import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jeyzer.analyzer.data.ThreadDump;
import org.jeyzer.analyzer.math.FormulaHelper;
import org.jeyzer.analyzer.setup.MemoryPoolSetupManager;

public class GarbageCollection {
	
	private long gcTime = 0;
	private long gcCount = 0;
	private long gcTotalCount = 0;
	private double gcTimePercent = 0;
	private GarbageCollectorInfo mostRecentGCInfo = null;
	private GarbageCollectorInfo youngGCInfo = null;
	private GarbageCollectorInfo oldGCInfo = null;
	
	private Map<String, GarbageCollectorInfo> garbageCollectors = new HashMap<>(2);
	
	public GarbageCollectorInfo getGarbageCollectorInfo(String name){
		return garbageCollectors.get(name);
	}
	
	public GarbageCollectorInfo addGarbageCollectorInfo(String name, MemoryPoolSetupManager memPoolSetupManager){
		GarbageCollectorInfo gcInfo = new GarbageCollectorInfo(name, memPoolSetupManager);
		garbageCollectors.put(name, gcInfo); // can be null
		return gcInfo;
	}

	public long getGcTime() {
		return gcTime;
	}

	public long getGcCount() {
		return gcCount;
	}
	
	public long getGcTotalCount() {
		return gcTotalCount;
	}

	public double getGcTimePercent() {
		return gcTimePercent;
	}

	public Collection<GarbageCollectorInfo> getGarbageCollectorInfos(){
		return garbageCollectors.values();
	}

	public void updateGarbageCollectionData(ThreadDump previous, long sliceTime) {
		for (GarbageCollectorInfo gcInfo : garbageCollectors.values()){
			gcInfo.updateGarbageCollectorInfo(previous, sliceTime);

			// update all GC collection count
			if (gcInfo.getCollectionCount() != -1 && gcCount != -1)
				gcCount += gcInfo.getCollectionCount();
			else
				gcCount = -1; // invalidate
			
			if (gcInfo.getTotalCollectionCount() != -1 && gcTotalCount != -1)
				gcTotalCount += gcInfo.getTotalCollectionCount();
			else
				gcTotalCount = -1; // invalidate
			
			// update all GC collection time
			if (gcInfo.getCollectionTime() != -1 && gcTime != -1)
				gcTime += gcInfo.getCollectionTime();
			else
				gcTime = -1; // invalidate
		}
		
		if (gcTime != -1 && sliceTime !=0){
			long sliceTimeMs = sliceTime / 1000000L; // convert to ms
			gcTimePercent = FormulaHelper.percent(gcTime, sliceTimeMs);
		}
		else{
			gcTimePercent = -1;
		}
			
	}

	public GarbageCollectorInfo getMostRecentGarbageCollectorInfo() {
		if (mostRecentGCInfo == null){
			long endTime = 0;
			
			for (GarbageCollectorInfo candidate : this.garbageCollectors.values()){
				if (!candidate.isValid())
					continue;
				if (candidate.getLastGCEndTime() != -1 && candidate.getLastGCEndTime() > endTime){
					mostRecentGCInfo = candidate;
					endTime = candidate.getLastGCEndTime();
				}
			}
		}
		
		return mostRecentGCInfo; // can be null
	}

	public GarbageCollectorInfo getOldGarbageCollectorInfo() {
		return oldGCInfo;
	}
	
	public void setOldGarbageCollectorInfo(GarbageCollectorInfo gcInfo) {
		this.oldGCInfo = gcInfo;
	}

	public GarbageCollectorInfo getYoungGarbageCollectorInfo() {
		return youngGCInfo;
	}
	
	public void setYoungGarbageCollectorInfo(GarbageCollectorInfo gcInfo) {
		this.youngGCInfo = gcInfo;
	}
	
	public boolean isStarted(){
		return this.gcTotalCount != 0;
	}	
	
}
