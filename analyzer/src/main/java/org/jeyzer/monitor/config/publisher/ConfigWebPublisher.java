package org.jeyzer.monitor.config.publisher;

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







import java.io.File;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.w3c.dom.Element;

public class ConfigWebPublisher extends ConfigPublisher{
	
	public static final String NAME = "web";
	
	public static final String JEYZER_MONITOR_WEB_ENABLED = "web_enabled";
	private static final String JZRM_WEB_URL = "url";
	private static final String JZRM_WEB_DEPLOY = "deploy";

	private boolean webEnabled;
	private String url;
	private String deploy;
	
	public ConfigWebPublisher(Element node) throws Exception, JzrInitializationException {
		super(NAME, node);
		this.webEnabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(node, JEYZER_MONITOR_WEB_ENABLED));
		this.url = ConfigUtil.getAttributeValue(node, JZRM_WEB_URL);
		this.deploy = ConfigUtil.getAttributeValue(node, JZRM_WEB_DEPLOY);
		if (this.webEnabled){
			validateDeployDirectory();
			validateUrl();
		}


	}

	private void validateUrl() throws JzrInitializationException {
		if (!(this.url.startsWith("http://") || this.url.startsWith("https://")))
			throw new JzrInitializationException(
					"Failed to initialize the monitor web publisher : "
					+ "base url " + url + " is not valid.");
	}

	private void validateDeployDirectory() throws JzrInitializationException {
		File directory = new File(deploy);

		// Normally the Jeyzer installer does it, but under Docker/Linux, it did not. 
		// So create it in last case
		if (!directory.exists() || !directory.isDirectory()) {
			if (!directory.mkdirs())
				throw new JzrInitializationException(
				  "Failed to create the monitor web publisher deployment directory : " + deploy);
		}
	}

	@Override
	public boolean isEnabled() {
		return webEnabled;
	}

	public String getUrl() {
		return url;
	}

	public String getDeploy() {
		return deploy;
	}

}
