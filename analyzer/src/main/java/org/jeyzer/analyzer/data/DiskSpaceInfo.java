package org.jeyzer.analyzer.data;

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



public class DiskSpaceInfo {
	
	public static final long NOT_AVAILABLE = -1;
	
	private String name;
	private String directory;
	
	// Space in bytes
	private long usedSpace = NOT_AVAILABLE;
	private long totalSpace = NOT_AVAILABLE;
	private long freeSpace = NOT_AVAILABLE;
	
	public DiskSpaceInfo(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public long getUsedSpace() {
		return usedSpace;
	}
	
	public boolean hasUsedSpace() {
		return usedSpace > NOT_AVAILABLE;
	}

	public long getTotalSpace() {
		return totalSpace;
	}
	
	public boolean hasTotalSpace() {
		return totalSpace > NOT_AVAILABLE;
	}

	public long getFreeSpace() {
		return freeSpace;
	}
	
	public boolean hasFreeSpace() {
		return freeSpace > NOT_AVAILABLE;
	}

	public void setUsedSpace(long usedSpace) {
		this.usedSpace = usedSpace;
	}

	public void setTotalSpace(long totalSpace) {
		this.totalSpace = totalSpace;
	}

	public void setFreeSpace(long freeSpace) {
		this.freeSpace = freeSpace;
	}

	public double getUsedSpacePercent() {
		if (!hasTotalSpace() || !hasFreeSpace() || !hasUsedSpace())
			return NOT_AVAILABLE;

		return FormulaHelper.percent(this.usedSpace, this.totalSpace);
	}
	
	public double getFreeSpacePercent() {
		if (!hasTotalSpace() || !hasFreeSpace() || !hasUsedSpace())
			return NOT_AVAILABLE;

		return FormulaHelper.percent(this.freeSpace, this.totalSpace);
	}
}
