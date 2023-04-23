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




import java.time.Duration;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.w3c.dom.Element;

public class ConfigRepositoryCache {
	
	private static final String JZRA_ENABLED = "enabled";
	private static final String JZRA_DIRECTORY = "directory";
	private static final String JZRA_TIME_TO_LIVE = "time_to_live";
	
	private boolean isCachingEnabled;
	private String cacheDirectoryRoot;
	private Duration cacheTimeToLive; 
	
	public ConfigRepositoryCache(Element cacheNode) {
		cacheDirectoryRoot = ConfigUtil.getAttributeValue(cacheNode, JZRA_DIRECTORY); // resolve any variable
		isCachingEnabled = Boolean.parseBoolean(ConfigUtil.getAttributeValue(cacheNode, JZRA_ENABLED));
		cacheTimeToLive = ConfigUtil.getAttributeDuration(cacheNode, JZRA_TIME_TO_LIVE);
	}

	public boolean isCachingEnabled() {
		return isCachingEnabled;
	}

	public String getCacheDirectoryRoot() {
		return cacheDirectoryRoot;
	}

	public Duration getCacheTimeToLive() {
		return cacheTimeToLive;
	}
}
