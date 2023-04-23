package org.jeyzer.analyzer.input.translator.jfr.mapper.data;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JFRGarbageCollection {

	private static final Logger logger = LoggerFactory.getLogger(JFRGarbageCollection.class);
	
	public static final String JFR_AFTER_GC = "After GC";
	public static final String JFR_BEFORE_GC = "Before GC";

	public static final String JFR_G1_YOUNG_NAME = "G1New";
	private static final String JZR_GC_G1_YOUNG_NAME = "G1 Young Generation";
	private static final String JZR_G1_POOL_YOUNG_NAME = "G1 Eden Space";
	
	public static final String JFR_G1_OLD_NAME = "G1Old";
	private static final String JZR_FC_G1_OLD_NAME = "G1 Old Generation";
	private static final String JZR_G1_POOL_OLD_NAME = "G1 Old Gen";

	public static final String JFR_PS_YOUNG_NAME = "ParallelScavenge";
	private static final String JZR_GC_PS_YOUNG_NAME = "PS Scavenge";
	private static final String JZR_PS_POOL_YOUNG_NAME = "PS Eden Space";
	
	public static final String JFR_PS_OLD_NAME = "ParallelOld";
	private static final String JZR_GC_PS_OLD_NAME = "PS MarkSweep";
	private static final String JZR_PS_POOL_OLD_NAME = "PS Old Gen";

	// Mapping is surprisingly for the serial GC option :
	public static final String JFR_SERIAL_YOUNG_NAME = "DefNew";
	private static final String JZR_GC_SERIAL_YOUNG_NAME = "Copy";
	private static final String JZR_SERIAL_POOL_YOUNG_NAME = "Eden Space";
		
	public static final String JFR_SERIAL_OLD_NAME = "SerialOld";
	private static final String JZR_GC_SERIAL_OLD_NAME = "MarkSweepCompact";
	private static final String JZR_SERIAL_POOL_OLD_NAME = "Tenured Gen";
	
	private static final String JFR_G1_FULL_NAME = "G1Full";

	protected static final List<String> OLD_GC_TYPES = new ArrayList<>();
	static {
		OLD_GC_TYPES.add(JFR_G1_OLD_NAME);
		OLD_GC_TYPES.add(JFR_G1_FULL_NAME);
		OLD_GC_TYPES.add(JFR_PS_OLD_NAME);
		OLD_GC_TYPES.add(JFR_SERIAL_OLD_NAME);
	}
	
	public static List<String> getOldGCTypes(){
		return Collections.unmodifiableList(OLD_GC_TYPES);
	}

	protected static final List<String> NEW_GC_TYPES = new ArrayList<>();
	static {
		NEW_GC_TYPES.add(JFR_G1_YOUNG_NAME);
		NEW_GC_TYPES.add(JFR_PS_YOUNG_NAME);
		NEW_GC_TYPES.add(JFR_SERIAL_YOUNG_NAME);
	}
	
	public static  List<String> getNewGCTypes(){
		return Collections.unmodifiableList(NEW_GC_TYPES);
	}
	
	private static final Map<String, GCDescriptor> DESCRIPTORS = new HashMap<>(); 
	static {
		DESCRIPTORS.put(JFR_G1_YOUNG_NAME, new GCDescriptor(JFR_G1_YOUNG_NAME, JZR_GC_G1_YOUNG_NAME, JZR_G1_POOL_YOUNG_NAME));
		DESCRIPTORS.put(JFR_G1_OLD_NAME, new GCDescriptor(JFR_G1_OLD_NAME, JZR_FC_G1_OLD_NAME, JZR_G1_POOL_OLD_NAME));
		DESCRIPTORS.put(JFR_PS_YOUNG_NAME, new GCDescriptor(JFR_PS_YOUNG_NAME, JZR_GC_PS_YOUNG_NAME, JZR_PS_POOL_YOUNG_NAME));
		DESCRIPTORS.put(JFR_PS_OLD_NAME, new GCDescriptor(JFR_PS_OLD_NAME, JZR_GC_PS_OLD_NAME, JZR_PS_POOL_OLD_NAME));
		DESCRIPTORS.put(JFR_SERIAL_YOUNG_NAME, new GCDescriptor(JFR_SERIAL_YOUNG_NAME, JZR_GC_SERIAL_YOUNG_NAME, JZR_SERIAL_POOL_YOUNG_NAME));
		DESCRIPTORS.put(JFR_SERIAL_OLD_NAME, new GCDescriptor(JFR_SERIAL_OLD_NAME, JZR_GC_SERIAL_OLD_NAME, JZR_SERIAL_POOL_OLD_NAME));
	}

	private GCDescriptor oldDescriptor;
	private long oldCount = 0;
	private long oldTime = 0;

	private GCDescriptor youngDescriptor;
	private long youngCount = 0;
	private long youngTime = 0;

	private long usedPeakOld = 0;
	private long usedPeakEden = 0;
	
	private JFRLastGarbageCollection newLastGC = new JFRLastGarbageCollection();
	private JFRLastGarbageCollection oldLastGC = new JFRLastGarbageCollection();

	public JFRGarbageCollection(String oldGCName, String youngGCName) {
		if (DESCRIPTORS.containsKey(oldGCName)) {
			oldDescriptor = DESCRIPTORS.get(oldGCName);			
		}
		else {
			logger.warn("Old garbage collector name unknown. Please ask to implement it : " + oldGCName);
			oldDescriptor = new GCDescriptor(oldGCName);
		}
			
		if (DESCRIPTORS.containsKey(youngGCName)) {
			youngDescriptor = DESCRIPTORS.get(youngGCName);			
		}
		else {
			logger.warn("Young garbage collector name unknown. Please ask to implement it : " + youngGCName);
			youngDescriptor = new GCDescriptor(youngGCName);
		}
	}
	
	public boolean isG1GarbageCollector() {
		return JFR_G1_YOUNG_NAME.equals(this.youngDescriptor.jfrName);
	}
	
	public boolean isPSGarbageCollector() {
		return JFR_PS_YOUNG_NAME.equals(this.youngDescriptor.jfrName);
	}
	
	public boolean isSerialGarbageCollector() {
		return JFR_SERIAL_YOUNG_NAME.equals(this.youngDescriptor.jfrName);
	}

	public long getOldGCCount() {
		return oldCount;
	}

	public void setOldGCCount(long oldCount) {
		this.oldCount = oldCount;
	}

	public long getOldGCTime() {
		return oldTime;
	}

	public void setOldGCTime(long oldTime) {
		this.oldTime = oldTime;
	}

	public long getYoungGCCount() {
		return youngCount;
	}

	public void setYoungGCCount(long youngCount) {
		this.youngCount = youngCount;
	}

	public long getYoungGCTime() {
		return youngTime;
	}

	public void setYoungGCTime(long youngTime) {
		this.youngTime = youngTime;
	}
	
	public JFRLastGarbageCollection getNewLastGC() {
		return newLastGC;
	}

	public JFRLastGarbageCollection getOldLastGC() {
		return oldLastGC;
	}
	
	public GCDescriptor getOldDescriptor() {
		return oldDescriptor;
	}

	public GCDescriptor getYoungDescriptor() {
		return youngDescriptor;
	}

	public static class GCDescriptor{
		private String jfrName;
		private String jzrGCName;
		private String jzrPoolName;
		
		public GCDescriptor(String jfrName, String jzrGCName, String jzrPoolName) {
			this.jfrName = jfrName;
			this.jzrGCName = jzrGCName;
			this.jzrPoolName = jzrPoolName;
		}
		
		// Unknown mapping case
		public GCDescriptor(String jfrName) {
			this.jfrName = jfrName;
			this.jzrGCName = jfrName;
			this.jzrPoolName = jfrName;
		}

		public String getJfrName() {
			return jfrName;
		}

		public String getJzrGCName() {
			return jzrGCName;
		}

		public String getJzrPoolName() {
			return jzrPoolName;
		}
	}
	
	public JFRLastGarbageCollection getLatestGC() {
		if (this.newLastGC == null && this.oldLastGC == null)
			return null;
		if (this.newLastGC == null)
			return this.oldLastGC;
		if (this.oldLastGC == null)
			return this.newLastGC;
		return this.newLastGC.getStartTime() > this.oldLastGC.getStartTime() ? this.newLastGC : this.oldLastGC;  
	}

	public void setOldUsedPeak(long usedPeakOld) {
		if (usedPeakOld > this.usedPeakOld || usedPeakOld == -1L)
			this.usedPeakOld = usedPeakOld;
	}

	public void setEdenUsedPeak(long usedPeakEden) {
		if (usedPeakEden > this.usedPeakEden || usedPeakOld == -1L)
			this.usedPeakEden = usedPeakEden;
	}

	public long getUsedPeakOld() {
		return usedPeakOld;
	}

	public long getUsedPeakEden() {
		return usedPeakEden;
	}
}
