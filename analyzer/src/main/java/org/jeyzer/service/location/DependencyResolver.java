package org.jeyzer.service.location;

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




import java.util.Collection;

import com.google.common.collect.Multimap;

public class DependencyResolver {

	public static final String DEPS_TAG = "@@";	
	
	private Multimap<String, String> dependencies;
	
	public DependencyResolver(Multimap<String, String> dependencies) {
		this.dependencies = dependencies;
	}

	public Collection<String> resolveDependencies(String locations) {
		int startIndex = locations.indexOf(DependencyResolver.DEPS_TAG) + DependencyResolver.DEPS_TAG.length();
		int endIndex = locations.indexOf(DependencyResolver.DEPS_TAG, startIndex);
		String dependencySet = locations.substring(startIndex, endIndex);
		return this.dependencies.get(dependencySet);
	}
	
}
