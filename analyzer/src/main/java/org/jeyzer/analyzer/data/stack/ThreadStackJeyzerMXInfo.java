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







import java.util.HashMap;
import java.util.Map;

public class ThreadStackJeyzerMXInfo {

	private String id;
	private String user;
	private String functionPrincipal;
	private Map<String, String> contextParams = new HashMap<>();
	private String jzrId;      // unique id
	private long startTime;   // ms
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getFunctionPrincipal() {
		return functionPrincipal;
	}
	
	public void setFunctionPrincipal(String functionPrincipal) {
		this.functionPrincipal = functionPrincipal;
	}
	
	public String getJzrId() {
		return jzrId;
	}
	
	public void setJzrId(String jzrId) {
		this.jzrId = jzrId;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	public void addContextParam(String key, String value){
		contextParams.put(key, value);
	}

	public String getContextParam(String key){
		return contextParams.get(key);
	}
	
	public Map<String, String> getAllContextParams(){
		return contextParams;
	}
}
