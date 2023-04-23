package org.jeyzer.service;

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




import org.jeyzer.analyzer.config.ConfigAnalyzer;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.setup.JzrSetupManager;
import org.jeyzer.service.action.id.ActionIdGenerator;
import org.jeyzer.service.location.DependencyResolver;
import org.jeyzer.service.location.JzrLocationResolver;

public class JzrServiceManager {
	
	private JzrLocationResolver resourcePathResolver;
	private DependencyResolver dependencyResolver;
	private ActionIdGenerator actionIdGenerator;

	public JzrServiceManager(ActionIdGenerator actionIdGenerator, JzrSetupManager setupMgr, ConfigAnalyzer analyzerCfg) throws JzrInitializationException{
		this.actionIdGenerator = actionIdGenerator;
		this.dependencyResolver = new DependencyResolver(analyzerCfg.getDependencies());
		this.resourcePathResolver = new JzrLocationResolver(setupMgr, dependencyResolver);
	}
	
	public JzrLocationResolver getResourcePathResolver() {
		return resourcePathResolver;
	}

	public DependencyResolver getDependencyResolver() {
		return dependencyResolver;
	}

	public ActionIdGenerator getActionIdGenerator() {
		return actionIdGenerator;
	}
}
