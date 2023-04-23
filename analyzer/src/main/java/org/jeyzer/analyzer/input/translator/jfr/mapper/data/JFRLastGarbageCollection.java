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

public class JFRLastGarbageCollection {

	private int gcCId = 0;

	private long heapUsedBeforeGC = 0;
	private long heapUsedAfterGC = 0;
	private long heapMaxBeforeGC = 0;
	private long heapMaxAfterGC = 0;

	private long oldCommittedAfterGC = 0;
	private long oldCommittedBeforeGC = 0;
	private long edenCommittedAfterGC = 0;
	private long edenCommittedBeforeGC = 0;
	
	private long heapG1GCstartTime = 0;
	private long edenUsedBeforeGC = 0;
	private long edenUsedAfterGC = 0;
	private long edenMaxBeforeGC = 0;
	private long edenMaxAfterGC = 0;

	private long oldUsedBeforeGC = 0;
	private long oldUsedAfterGC = 0;
	private long oldMaxBeforeGC = 0;
	private long oldMaxAfterGC = 0;
	
	private long duration = 0;
	private long startTime = 0;
	private long endTime = 0;

	public void setHeapUsedBeforeGC(long heapUsed) {
		this.heapUsedBeforeGC = heapUsed;
	}

	public void setHeapUsedAfterGC(long heapUsed) {
		this.heapUsedAfterGC = heapUsed;
	}

	public void setHeapMaxBeforeGC(long heapMax) {
		this.heapMaxBeforeGC = heapMax;
	}

	public void setHeapMaxAfterGC(long heapMax) {
		this.heapMaxAfterGC = heapMax;
	}
	
	public void setOldCommittedAfterGC(long value) {
		this.oldCommittedAfterGC = value;
	}
	
	public void setOldCommittedBeforeGC(long value) {
		this.oldCommittedBeforeGC = value;
	}
	
	public void setEdenCommittedAfterGC(long value) {
		this.edenCommittedAfterGC = value;
	}
	
	public void setEdenCommittedBeforeGC(long value) {
		this.edenCommittedBeforeGC = value;
	}

	public void setHeapG1GCStartTime(int startTime) {
		this.heapG1GCstartTime = startTime;
	}

	public void setEdenUsedBeforeGC(long edenUsed) {
		this.edenUsedBeforeGC = edenUsed;
	}

	public void setEdenUsedAfterGC(long edenUsed) {
		this.edenUsedAfterGC = edenUsed;
	}

	public void setEdenMaxBeforeGC(long edenMax) {
		this.edenMaxBeforeGC = edenMax;
	}

	public void setEdenMaxAfterGC(long edenMax) {
		this.edenMaxAfterGC = edenMax;
	}

	public void updateOldFigures() {
		this.oldUsedBeforeGC = this.heapUsedBeforeGC - this.edenUsedBeforeGC;
		this.oldMaxBeforeGC = this.heapMaxBeforeGC - this.edenMaxBeforeGC;
		this.oldUsedAfterGC = this.heapUsedAfterGC - this.edenUsedAfterGC;
		this.oldMaxAfterGC = this.heapMaxAfterGC - this.edenMaxAfterGC;
	}

	public long getHeapUsedBeforeGC() {
		return heapUsedBeforeGC;
	}

	public long getHeapUsedAfterGC() {
		return heapUsedAfterGC;
	}

	public long getHeapMaxBeforeGC() {
		return heapMaxBeforeGC;
	}

	public long getHeapMaxAfterGC() {
		return heapMaxAfterGC;
	}

	public long getOldCommittedAfterGC() {
		return oldCommittedAfterGC;
	}

	public long getOldCommittedBeforeGC() {
		return oldCommittedBeforeGC;
	}
	
	public long getEdenCommittedAfterGC() {
		return edenCommittedAfterGC;
	}

	public long getEdenCommittedBeforeGC() {
		return edenCommittedBeforeGC;
	}

	public long getEdenUsedBeforeGC() {
		return edenUsedBeforeGC;
	}

	public long getEdenUsedAfterGC() {
		return edenUsedAfterGC;
	}

	public long getEdenMaxBeforeGC() {
		return edenMaxBeforeGC;
	}

	public long getEdenMaxAfterGC() {
		return edenMaxAfterGC;
	}

	public long getOldUsedBeforeGC() {
		return oldUsedBeforeGC;
	}

	public long getOldUsedAfterGC() {
		return oldUsedAfterGC;
	}

	public long getOldMaxBeforeGC() {
		return oldMaxBeforeGC;
	}

	public long getOldMaxAfterGC() {
		return oldMaxAfterGC;
	}

	public long getHeapG1GCStartTime() {
		return heapG1GCstartTime;
	}

	public void setGCId(int id) {
		this.gcCId = id;
	}
	
	public int getGCId() {
		return this.gcCId;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public void setStartTime(long time) {
		this.startTime = time;
	}
	
	public void setEndTime(long time) {
		this.endTime = time;
	}

	public long getDuration() {
		return duration;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setOldUsedBeforeGC(long value) {
		this.oldUsedBeforeGC = value;
	}
	
	public void setOldUsedAfterGC(long value) {
		this.oldUsedAfterGC = value;
	}

	public void setOldMaxBeforeGC(long value) {
		this.oldMaxBeforeGC = value;
	}
	
	public void setOldMaxAfterGC(long value) {
		this.oldMaxAfterGC = value;
	}
}
