package org.jeyzer.analyzer.output;

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



import java.util.Date;

import org.jeyzer.analyzer.data.TimeZoneInfo;
import org.jeyzer.monitor.engine.event.info.Level;





public class ReportDescriptor {

	private String reportFilePath;
	private String reportFileName;
	
	// can be null
	private String musicFilePath;
	private String musicFileName;

	// can be null
	private String ufoStackFilePath;
	private String ufoStackFileName;
	
	private Date startTime;
	private Date endTime;
	private Level level;
	private String applicationType;
	private String applicationId;
	private TimeZoneInfo timeZoneInfo;
	
	// used at least by the Jeyzer Web
	private boolean productionReady;
	
	public void setReportFilePath(String reportFilePath) {
		this.reportFilePath = reportFilePath;
	}

	public void setReportFileName(String reportFileName) {
		this.reportFileName = reportFileName;
	}
	
	public String getReportPath(){
		return this.reportFilePath;
	}

	public String getReportName(){
		return this.reportFileName;
	}
	

	public String getMusicFilePath() {
		return musicFilePath;
	}

	public void setMusicFilePath(String musicFilePath) {
		this.musicFilePath = musicFilePath;
	}

	public String getMusicFileName() {
		return musicFileName;
	}

	public void setMusicFileName(String musicFileName) {
		this.musicFileName = musicFileName;
	}
	
	public String getUfoStackFilePath() {
		return ufoStackFilePath;
	}

	public void setUfoStackFilePath(String ufoFilePath) {
		this.ufoStackFilePath = ufoFilePath;
	}

	public String getUfoStackFileName() {
		return ufoStackFileName;
	}

	public void setUfoStackFileName(String ufoFileName) {
		this.ufoStackFileName = ufoFileName;
	}

	public void setStartTime(Date start) {
		this.startTime= start;
	}
	
	public Date getStartTime() {
		return startTime;
	}

	public void setEndTime(Date end) {
		this.endTime = end;
	}	

	public Date getEndTime() {
		return endTime;
	}

	public void setLevel(Level level) {
		this.level = level;
	}
	
	public Level getLevel() {
		return level;
	}

	public void setApplicationType(String applicationType) {
		// profile
		this.applicationType = applicationType;
	}
	
	public String getApplicationType() {
		return applicationType;
	}
	
	public void setApplicationId(String applicationId) {
		// profile + context in web
		this.applicationId = applicationId;
	}
	
	public String getApplicationId() {
		return applicationId;
	}
	
	public TimeZoneInfo getTimeZoneInfo() {
		return timeZoneInfo;
	}
	
	public void setTimeZoneInfo(TimeZoneInfo timeZoneInfo) {
		this.timeZoneInfo = timeZoneInfo;
	}
	
	public void setAnalysisProductionReady(boolean productionReady) {
		this.productionReady = productionReady;
	}
	
	public boolean isAnalysisProductionReady() {
		return this.productionReady;
	}
}
