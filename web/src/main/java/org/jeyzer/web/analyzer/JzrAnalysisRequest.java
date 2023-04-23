package org.jeyzer.web.analyzer;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Web
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */


import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.jeyzer.analyzer.util.SystemHelper;

public class JzrAnalysisRequest {
	
	private static final AtomicLong nextId = new AtomicLong(100);

	private final String id;
	private boolean redirect = false;
	
	private String profile;
	private String prevProfile = null; // used in redirect 

	
	private String tempZipFileId;
	private String nodeName;
	private boolean recordingTimeZoneUserSpecified;
	private String recordingTimeZoneId;
	private boolean reportTimeZoneUserSpecified;
	private String reportTimeZoneId;
	private double period;
	private List<JzrDiscoveryItem> discoveryItems;
	private String desc;
	private String email;
	private String password;

	private String tdDir;
	private String prevTdDir = null;
	
	private Map<String, String> remoteAddresses;
	
	/**
	 * Standard constructor
	 */
	public JzrAnalysisRequest() {
		this.id = generateId();
	}
	
	/**
	 * Master profile redirection constructor
	 * Copy constructor
	 * 
	 * @param req the original request
	 */
	public JzrAnalysisRequest(JzrAnalysisRequest req, String redirectProfile) {
		this.id = req.getId(); // keep same id
		this.profile = redirectProfile;
		this.prevProfile = req.getProfile();
		this.redirect = true;
		this.tempZipFileId = req.getTempZipFileId(); // useless after redirect
		
		this.tdDir = null; // will be  rebuilt
		this.prevTdDir = req.getTDDirectory();
		
		// copy
		this.nodeName = req.getNodeName();
		this.recordingTimeZoneUserSpecified = req.isRecordingTimeZoneUserSpecified();
		this.recordingTimeZoneId = req.recordingTimeZoneId;
		this.reportTimeZoneUserSpecified = req.isReportTimeZoneUserSpecified();
		this.reportTimeZoneId = req.getReportTimeZoneId();
		this.period = req.getPeriod();
		this.discoveryItems = req.getDiscoveryItems();
		this.desc = req.getDescription();
		this.email = req.getEmail();
		this.password = req.getPassword();
	}

	public String getId() {
		return id;
	}
	
	public boolean isProfileRedirect() {
		return redirect;
	}
	
	public String getProfile() {
		return profile;
	}
	
	public String getPreviousProfile() {
		return prevProfile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getTempZipFileId() {
		return tempZipFileId;
	}

	public void setTempZipFileId(String tempZipFileId) {
		this.tempZipFileId = tempZipFileId;
	}
	
	public String getNodeName() {
		return nodeName;
	}
	
	public void setNodeName(String nodeName) {
		this.nodeName = SystemHelper.sanitizePathElement(nodeName).trim();
	}

	public double getPeriod() {
		return period;
	}
	
	public String getISOPeriod() {
		return period + "s";
	}
	
	public void setPeriod(double period) {
		this.period = period;
	}
	
	public void setRecordingTimeZoneUserSpecified(boolean value) {
		this.recordingTimeZoneUserSpecified = value;
	}
	
	public Boolean isRecordingTimeZoneUserSpecified() {
		return this.recordingTimeZoneUserSpecified;
	}

	public String getRecordingTimeZoneId() {
		return this.recordingTimeZoneId;
	}
	
	public void setReportTimeZoneUserSpecified(boolean value) {
		this.reportTimeZoneUserSpecified = value;
	}
	
	public Boolean isReportTimeZoneUserSpecified() {
		return this.reportTimeZoneUserSpecified;
	}
	
	public void setRecordingTimeZoneId(String recordingTimeZoneId) {
		this.recordingTimeZoneId = recordingTimeZoneId;
	}
	
	public String getReportTimeZoneId() {
		return this.reportTimeZoneId;
	}
	
	public void setReportTimeZoneId(String reportTimeZoneId) {
		this.reportTimeZoneId = reportTimeZoneId;
	}

	public List<JzrDiscoveryItem> getDiscoveryItems() {
		return discoveryItems;
	}
	
	public void setDiscoveryItems(List<JzrDiscoveryItem> discoveryItems) {
		this.discoveryItems = discoveryItems;
	}

	public String getDescription() {
		return desc;
	}
	
	public void setDescription(String desc) {
		this.desc = desc;
	}

	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email.trim();
	}

	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setTDDirectory(String tdDir) {
		this.tdDir= tdDir;
	}
	
	public String getTDDirectory() {
		return this.tdDir; 
	}
	
	public String getPreviousTDDirectory() {
		return this.prevTdDir; 
	}
	
	public void setClientAddresses(Map<String, String> map) {
		this.remoteAddresses = map;
	}
	
	public Map<String, String> getClientAddresses() {
		return this.remoteAddresses;
	}
	
	private String generateId() {
		String timestamp = Long.toString(System.currentTimeMillis());
		// Remove the 1st (century) and last 3 ms digits
		String reducedTimestamp = timestamp.substring(1, timestamp.length() - 3);
		Long value = Long.valueOf(reducedTimestamp);
		return Long.toHexString(value) + "-" + nextId.incrementAndGet();
	}
}
