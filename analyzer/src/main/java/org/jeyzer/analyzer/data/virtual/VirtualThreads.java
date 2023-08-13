package org.jeyzer.analyzer.data.virtual;

import org.jeyzer.analyzer.math.FormulaHelper;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2023 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */


public class VirtualThreads {
	
	private Integer created = null;

	private Integer terminated = null;
	
	private Integer diff = null;

	// if JFR events : approximative, as it depends on where we start
	// if TCMD : real number
	private Integer active = null;
	
	private Integer mounted = null;
	
	private Double mountedCpuUsagePercent = (double)-1; // percentage

	public void setCreatedCount(Integer vtCreatedCount) {
		this.created = vtCreatedCount;
	}
	
	public Integer getCreatedCount() {
		return this.created;
	}
	
	public boolean hasCreatedCount() {
		return this.created != null;
	}

	public void setTerminatedCount(Integer vtTerminatedCount) {
		this.terminated = vtTerminatedCount;
	}
	
	public Integer getTerminatedCount() {
		return this.terminated;
	}
	
	public boolean hasTerminatedCount() {
		return this.terminated != null;
	}

	public void setActiveCount(Integer active) {
		this.active = active;
	}
	
	public Integer getActiveCount() {
		return this.active;
	}

	public boolean hasActiveCount() {
		return this.active != null;
	}	
	
	public Integer getMountedCount() {
		return mounted;
	}

	public void setMountedCount(Integer mounted) {
		this.mounted = mounted;
	}
	
	public boolean hasMountedCount() {
		return this.mounted != null;
	}
	
	public Double getMountedCpuUsagePercent() {
		return mountedCpuUsagePercent;
	}

	public Integer getDiff() {
		return this.diff;
	}

	public boolean hasDiff() {
		return this.diff != null;
	}
	
	public void updateDiffAndActiveCounterData(VirtualThreads prev) {
		if (hasCreatedCount() && hasTerminatedCount()) {
			diff = created - terminated;
			if (prev == null || !prev.hasActiveCount())
				active = diff > 0 ? diff : 0; // first one, positive
			else {
				active = prev.getActiveCount() + diff;
				if (active < 0)
					active = 0;
			}
		}
	}

	public void updateMountedData(int mountedCount, int cpuCores) {
		this.mounted = mountedCount;
		
		if (this.mounted != null && cpuCores > 0)
			this.mountedCpuUsagePercent = FormulaHelper.percent(mountedCount, cpuCores);
	}
	
}
