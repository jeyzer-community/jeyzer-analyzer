package org.jeyzer.analyzer.config.repository;

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




import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigRepository {
	
	private static final String JZRA_ID = "id";
	private static final String JZRA_REMOTE = "remote";
	private static final String JZRA_LOCAL = "local";
	private static final String JZRA_LOCAL_FIRST = "local_first";
	
	private String id;
	private String remote;
	private String local;
	private boolean preferLocal = false;
	
	public ConfigRepository(Element repoNode) throws JzrInitializationException {
		id = ConfigUtil.getAttributeValue(repoNode, JZRA_ID); // resolve any variable
		remote = ConfigUtil.getAttributeValue(repoNode, JZRA_REMOTE); // resolve any variable
		local = ConfigUtil.getAttributeValue(repoNode, JZRA_LOCAL); // resolve any variable
		preferLocal = Boolean.parseBoolean(ConfigUtil.getAttributeValue(repoNode, JZRA_LOCAL_FIRST));
		
		validate();
	}

	private void validate() throws JzrInitializationException {
		if (preferLocal && (local == null || local.isEmpty()))
			throw new JzrInitializationException("Repository local attribute for repo id " + id + " must be set when " + JZRA_LOCAL_FIRST + " is enabled. Please review the repository configuration.");
		
		if (!preferLocal && (remote == null || remote.isEmpty()))
			throw new JzrInitializationException("Repository remote attribute for repo id " + id + "must be set when " + JZRA_LOCAL_FIRST + " is disabled. Please review the repository configuration.");
	}

	public String getId() {
		return id;
	}

	public String getRemote() {
		return remote;
	}

	public String getLocal() {
		return local;
	}

	public boolean isPreferLocal() {
		return preferLocal;
	}
	
	public String getPrefered(){
		return isPreferLocal() ? local : remote;
	}
	
	public String getFallback(){
		return isPreferLocal() ? remote : local;
	}
}
