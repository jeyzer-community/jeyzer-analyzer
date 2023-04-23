package org.jeyzer.monitor.config.publisher.jira;

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

public class ConfigJiraSetup {

	private static final String JIRA_SETUP_SERVER = "server";
	private static final String JIRA_SETUP_SERVER_URL = "url";
	private static final String JIRA_SETUP_CLIENT = "client";
	private static final String JIRA_SETUP_CLIENT_TYPE = "type";
	private static final String JIRA_SETUP_CLIENT_DEBUG = "debug";
	private static final String JIRA_SETUP_CONNECTIVITY = "connectivity";
	private static final String JIRA_SETUP_CONNECTIVITY_AUTH_KEY = "auth_key";
	
	public static final String JIRA_IMPL_CLOUD_V3 = "jira_cloud_v3";
	public static final String JIRA_IMPL_CLOUD_V2 = "jira_cloud_v2";
	
	private String url;
	private String implType;
	private String authKey;
	private boolean debug;
	
	public ConfigJiraSetup(Element node) throws JzrInitializationException {
		if (node == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA setup configuration is missing.");
		
		loadServerConfiguration(node);
		loadClientConfiguration(node);
	}
	
	public String getServerUrl() {
		return url;
	}

	public String getImplType() {
		return implType;
	}
	
	public boolean isDebugEnabled() {
		return debug;
	}

	public String getAuthKey() {
		return authKey;
	}

	private void loadClientConfiguration(Element node) throws JzrInitializationException {
		if (node == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA setup node is missing.");
		
		Element clientNode = ConfigUtil.getFirstChildNode(node, JIRA_SETUP_CLIENT);
		if (clientNode == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA client configuration is missing.");

		debug = Boolean.parseBoolean(ConfigUtil.getAttributeValue(clientNode, JIRA_SETUP_CLIENT_DEBUG));
		loadImplType(clientNode);
		loadAuthKey(clientNode);
	}

	private void loadAuthKey(Element clientNode) throws JzrInitializationException {
		Element connectNode = ConfigUtil.getFirstChildNode(clientNode, JIRA_SETUP_CONNECTIVITY);
		if (connectNode == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA connectivity configuration is missing.");
		
		authKey = ConfigUtil.getAttributeValue(connectNode, JIRA_SETUP_CONNECTIVITY_AUTH_KEY);
		if (authKey.isEmpty())
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA client authentication key is missing.");
	}

	private void loadImplType(Element clientNode) throws JzrInitializationException {
		implType = ConfigUtil.getAttributeValue(clientNode, JIRA_SETUP_CLIENT_TYPE);
		if (implType.isEmpty())
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA client implementation type is missing.");
		
		// for now, only the JIRA cloud is supported
		if (!JIRA_IMPL_CLOUD_V3.equals(implType) && !JIRA_IMPL_CLOUD_V2.equals(implType))
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA client implementation type is invalid : " + implType);
	}

	private void loadServerConfiguration(Element node) throws JzrInitializationException {
		Element serverNode = ConfigUtil.getFirstChildNode(node, JIRA_SETUP_SERVER);
		if (serverNode == null)
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : JIRA server configuration is missing.");
		
		this.url = ConfigUtil.getAttributeValue(serverNode, JIRA_SETUP_SERVER_URL);
		validateUrl();
	}

	private void validateUrl() throws JzrInitializationException {
		if (!(this.url.startsWith("http://") || this.url.startsWith("https://")))
			throw new JzrInitializationException(
					"Failed to initialize the monitor jira publisher : "
					+ "JIRA server url " + url + " is not valid.");
	}

	public Object getCloudAPIVersion() {
		return JIRA_IMPL_CLOUD_V3.equals(implType) ? "3": "2";
	}
}
