package org.jeyzer.analyzer.setup;

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




import java.util.Map;

import org.jeyzer.analyzer.config.repository.ConfigRepository;
import org.jeyzer.analyzer.config.repository.ConfigRepositoryCache;
import org.jeyzer.analyzer.config.setup.ConfigRepositorySetup;

public class RepositorySetupManager {
	
	private ConfigRepositoryCache cacheCfg;
	private Map<String, ConfigRepository> repositoryCfgs;

	public RepositorySetupManager(ConfigRepositorySetup repositorySetupConfig) {
		cacheCfg = repositorySetupConfig.getRepositoryCacheConfig();
		repositoryCfgs = repositorySetupConfig.getRepositoryConfigs();
	}

	public ConfigRepositoryCache getCacheCfg() {
		return cacheCfg;
	}

	public Map<String, ConfigRepository> getRepositoryCfgs() {
		return repositoryCfgs;
	}

}
