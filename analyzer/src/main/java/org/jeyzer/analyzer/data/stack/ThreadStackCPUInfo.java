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




import static org.jeyzer.analyzer.util.SystemHelper.CR;

import org.jeyzer.analyzer.math.FormulaHelper;




public class ThreadStackCPUInfo {

	private long timestamp = -1; // ms
	
	private long cpuCumulTime = -1; // nanosec
	private long userCumulTime = -1; // nanosec
	private long systemCumulTime = -1; // nanosec

	private long cpuTime = 0; // nanosec
	private long userTime = 0; // nanosec
	private long systemTime = 0; // nanosec

	private double  cpuUsage = 0;     // percentage
	private double userUsage = 0;        // percentage
	private double systemUsage = 0;      // percentage
	
	private double applicativeActivityUsage = 0; // percentage

	public ThreadStackCPUInfo(long cpuTime, long userTime, long timestamp) {
		this.cpuCumulTime = cpuTime;
		this.userCumulTime = userTime;
		this.timestamp = timestamp;
		this.systemCumulTime = cpuTime + userTime == -2 ? -1 : cpuTime - userTime;
	}

	public long getCpuTime() {
		return cpuTime;
	}

	public long getCumulCpuTime() {
		return cpuCumulTime;
	}

	public long getUserTime() {
		return userTime;
	}
	
	public long getCumulUserTime() {
		return userCumulTime;
	}
	
	public long getSystemTime() {
		return systemTime;
	}
	
	public long getCumulSystemTime() {
		return systemCumulTime;
	}	
	
	public boolean isCpuTimeAvailable() {
		return !(cpuCumulTime == -1);
	}	 

	public double getCpuUsage() {
		return cpuUsage;
	}
	
	public double getSystemUsage(){
		return this.systemUsage;
	}

	public double getUserUsage(){
		return this.userUsage;
	}

	// Percentage = thread cpu time / process cpu peak time * 100
	public double getApplicativeActivityUsage(){
		return this.applicativeActivityUsage;
	}	
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public long updateCPUData(ThreadStack previousStack, long timeSlice){
		long diff = 0;
		long prevCpuCumulTime = 0;
		
		// not set
		if (cpuCumulTime == -1){
//			this.cpuTime = 0;
//			this.cpuUsage = 0;
			return 0;
		}
		
		if (previousStack != null){
			
			if (previousStack.getCpuInfo() == null){
//				this.cpuTime = 0;
//				this.cpuUsage = 0;
				return 0;
			}
			
			prevCpuCumulTime = previousStack.getCpuInfo().getCumulCpuTime();
			
			// no way to take into account current cpu time
			if (prevCpuCumulTime == -1){
//				this.cpuTime = 0;
//				this.cpuUsage = 0;
				return 0;
			}
		}

		// Notes : 
		// - if no previous stack, thread is new one which means cpuTime = cpuCumulTime
		// - if missing dump period in between previous stack and current one, compute it anyway to consider out of memory cases
		// - difference can be in rare cases negative !! 
		//   Negative cases : thread re-created with same name, JVM implementation specifics, cpu count cycling 
		diff = cpuCumulTime - prevCpuCumulTime;  
		
		if (diff < 0){
//			this.cpuTime = 0;
//			this.cpuUsage = 0;
			return 0;
		}
		
		this.cpuTime = diff;
		this.cpuUsage = FormulaHelper.percent(this.cpuTime, timeSlice);
		
		return this.cpuTime; 
	}

	public long updateSystemData(ThreadStack previousStack, long timeSlice){
		long diff = 0;
		long prevSystemCumulTime = 0;

		if (systemCumulTime == -1){
//			this.systemTime = 0;
//			this.systemUsage = 0;
			return 0;	
		}		

		if (previousStack != null){
			
			if (previousStack.getCpuInfo() == null){
//				this.cpuTime = 0;
//				this.cpuUsage = 0;
				return 0;
			}
			
			prevSystemCumulTime = previousStack.getCpuInfo().getCumulSystemTime();
		
			if (prevSystemCumulTime == -1){
//				this.systemTime = 0;
//				this.systemUsage = 0;
				return 0;	
			}
			
		}
		
		diff = this.systemCumulTime - prevSystemCumulTime;
		
		// difference can be in rare cases negative !! 
		if (diff < 0){
//			this.systemTime = 0;
//			this.systemUsage = 0;
			return 0;	
		}
		
		this.systemTime = diff;
		this.systemUsage = FormulaHelper.percent(this.systemTime, timeSlice);
		
		return this.systemTime; 
	}	

	public long updateUserData(ThreadStack previousStack, long timeSlice){
		long diff = 0;
		long prevUserCumulTime = 0;
		
		if (userCumulTime == -1){
//			this.userTime = 0;
//			this.userUsage = 0;
			return 0;	
		}
		
		if (previousStack != null){
		
			if (previousStack.getCpuInfo() == null){
//				this.userTime = 0;
//				this.userUsage = 0;
				return 0;
			}
			
			prevUserCumulTime = previousStack.getCpuInfo().getCumulUserTime();
			
			if (prevUserCumulTime == -1){
//				this.userTime = 0;
//				this.userUsage = 0;
				return 0;	
			}
		}
		
		
		diff = this.userCumulTime - prevUserCumulTime;
		
		// difference can be in rare cases negative !! 
		if (diff < 0){
//			this.userTime = 0;
//			this.userUsage = 0;
			return 0;
		}
		
		this.userTime = diff;
		this.userUsage = FormulaHelper.percent(this.userTime, timeSlice);
		
		return this.userTime; 
	}
	
	public void updateApplicativeActivityUsage(long cpuTimePeak, double timeRatio){
		// optimize
		if (this.cpuTime == 0)
			this.applicativeActivityUsage= 0;
		
		if (cpuTimePeak != 0){
			long adjustedCpuTime = (long) (cpuTime * timeRatio); // acceptable precision loss
			this.applicativeActivityUsage = FormulaHelper.percent(adjustedCpuTime, cpuTimePeak);
		}
	}	
	
	@Override
	public String toString(){
		StringBuilder b = new StringBuilder(2000);
		
		b.append(" Cumulative time in nanosec" + CR);
		b.append(" - CPU        : " + this.cpuCumulTime + CR);
		b.append(" - System     : " + this.systemCumulTime + CR);
		b.append(" - User       : " + this.userCumulTime  + CR);
		
		return b.toString();
	}
}
