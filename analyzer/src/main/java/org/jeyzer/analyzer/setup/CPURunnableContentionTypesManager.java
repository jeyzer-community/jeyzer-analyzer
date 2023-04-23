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







import java.util.List;

import org.jeyzer.analyzer.config.setup.ConfigCPURunnableContentionTypesSetup;

public class CPURunnableContentionTypesManager {

	private boolean contentionTypesInclude;
	private List<String> contentionTypesIncludes;
	private List<String> contentionTypesExcludes;
	
	public CPURunnableContentionTypesManager(ConfigCPURunnableContentionTypesSetup setup) {
		contentionTypesInclude = setup.isContentionTypesInclude();
		contentionTypesIncludes = setup.getContentionTypesIncludes();
		contentionTypesExcludes = setup.getContentionTypesExcludes();
	}

	public boolean isCPURunnable(String contentionType) {
		return contentionTypesInclude ?
				contentionTypesIncludes.contains(contentionType) :
				!contentionTypesExcludes.contains(contentionType);
	}
}
