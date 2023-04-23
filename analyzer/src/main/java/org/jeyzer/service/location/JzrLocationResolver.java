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




import java.util.ArrayList;
import java.util.List;

import org.jeyzer.analyzer.data.jar.ProcessJarVersion;
import org.jeyzer.analyzer.data.jar.ProcessJars;
import org.jeyzer.analyzer.data.location.JzrResourceLocation;
import org.jeyzer.analyzer.data.location.MultipleJzrResourceLocation;
import org.jeyzer.analyzer.data.location.SingleJzrResourceLocation;
import org.jeyzer.analyzer.data.module.ProcessModule;
import org.jeyzer.analyzer.data.module.ProcessModules;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrServiceException;
import org.jeyzer.analyzer.setup.JzrSetupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JzrLocationResolver {
	
	private static final Logger logger = LoggerFactory.getLogger(JzrLocationResolver.class);
	
	private static final String UNMANAGED_MODULE_JAVA = "java.";
	private static final String UNMANAGED_MODULE_JDK = "jdk.";
	
	
	public enum LocationType { 
		PATTERN_SET ("_patterns.xml"), 
		MONITOR_RULE_SET("_rules.xml"),
		STICKER_SET("_stickers.xml");
		
		private String sufffix;
		
		private LocationType(String suffix) {
			this.sufffix = suffix;
		}
		
		public String getSuffix() {
			return this.sufffix;
		}
	}
	
	private RepositoryLocationResolver repoResolver;
	
	public JzrLocationResolver(JzrSetupManager setupMgr, DependencyResolver dependencyResolver) throws JzrInitializationException {
		repoResolver = new RepositoryLocationResolver(setupMgr.getRepositorySetupManager(), dependencyResolver);
	}

	public List<String> resolvePatternsLocations(List<JzrResourceLocation> locations) throws JzrServiceException{
		List<String> paths = new ArrayList<>();
		
		for (JzrResourceLocation location : locations){
			if (location instanceof SingleJzrResourceLocation)
				resolveLocation((SingleJzrResourceLocation)location, paths, LocationType.PATTERN_SET);
			if (location instanceof MultipleJzrResourceLocation)
				resolveLocations((MultipleJzrResourceLocation)location, paths, LocationType.PATTERN_SET);
		}
		
		if (paths.isEmpty())
			throw new JzrServiceException("Analysis pattern set configuration loading failed. No valid pattern set location found.");
		
		return paths;
	}
	
	public List<String> resolveDynamicPatternsLocations(ProcessJars processJars, ProcessModules processModules, boolean declaredRepoOnly){
		List<String> paths = new ArrayList<>();
		
		if (processJars != null) {
			for (ProcessJarVersion jarVersion : processJars.getJarVersions()){
				List<String> repoIds = listRepositoryIds(jarVersion, declaredRepoOnly);
				resolveDynamicLocations(jarVersion, repoIds, paths, LocationType.PATTERN_SET);
			}
		}
		else {
			logger.warn("Dynamic loading of patterns is not possible using process jar paths as the JZR recording does not contain such data.");			
		}
		
		if (processModules != null) {
			List<String> repoIds = new ArrayList<>();
			repoIds.addAll(this.repoResolver.getRepositoryIds());
			for (ProcessModule module : processModules.getProcessModules())
				if (isJeyzerManagedModule(module)) // optimization
					resolveDynamicLocations(module, repoIds, paths, LocationType.PATTERN_SET);
		}
		// no warning as JDK 8 could be used (no modules).
		
		return paths;
	}

	public List<String> resolveDynamicStickersLocations(ProcessJars processJars, ProcessModules processModules, boolean declaredRepoOnly){
		List<String> paths = new ArrayList<>();
		
		if (processJars != null) {
			for (ProcessJarVersion jarVersion : processJars.getJarVersions()){
				List<String> repoIds = listRepositoryIds(jarVersion, declaredRepoOnly);
				resolveDynamicLocations(jarVersion, repoIds, paths, LocationType.STICKER_SET);
			}
		}
		else {
			logger.warn("Dynamic loading of stickers is not possible using process jar paths as the JZR recording does not contain such data");
		}
					
		if (processModules != null) {
			List<String> repoIds = new ArrayList<>();
			repoIds.addAll(this.repoResolver.getRepositoryIds());
			for (ProcessModule module : processModules.getProcessModules())
				if (isJeyzerManagedModule(module)) // optimization
					resolveDynamicLocations(module, repoIds, paths, LocationType.STICKER_SET);
		}
		// no warning as JDK 8 could be used (no modules).
		
		return paths;
	}

	public List<String> resolveDynamicMonitorLocations(ProcessJars processJars, ProcessModules processModules, boolean declaredRepoOnly) {
		List<String> paths = new ArrayList<>();
		
		if (processJars != null) {
			for (ProcessJarVersion jarVersion : processJars.getJarVersions()){
				List<String> repoIds = listRepositoryIds(jarVersion, declaredRepoOnly);
				resolveDynamicLocations(jarVersion, repoIds, paths, LocationType.MONITOR_RULE_SET);
			}
		}
		else {
			logger.warn("Dynamic loading of monitoring rules is not possible using process jar paths as the JZR recording does not contain such data");
		}
		
		if (processModules != null) {
			List<String> repoIds = new ArrayList<>();
			repoIds.addAll(this.repoResolver.getRepositoryIds());
			for (ProcessModule module : processModules.getProcessModules())
				if (isJeyzerManagedModule(module)) // optimization
					resolveDynamicLocations(module, repoIds, paths, LocationType.PATTERN_SET);
		}
		// no warning as JDK 8 could be used (no modules).
		
		return paths;
	}

	public List<String> resolveMonitorLocations(List<JzrResourceLocation> locations) throws JzrServiceException{
		List<String> paths = new ArrayList<>();
		
		for (JzrResourceLocation location : locations){
			if (location instanceof SingleJzrResourceLocation)
				resolveLocation((SingleJzrResourceLocation)location, paths, LocationType.MONITOR_RULE_SET);
			if (location instanceof MultipleJzrResourceLocation)
				resolveLocations((MultipleJzrResourceLocation)location, paths, LocationType.MONITOR_RULE_SET);
		}
		
		if (paths.isEmpty())
			throw new JzrServiceException("Monitor rule set configuration loading failed. No valid rule set location found.");
		
		return paths;
	}
	
	public List<String> resolveStickerLocations(List<JzrResourceLocation> locations) throws JzrServiceException{
		List<String> paths = new ArrayList<>();
		
		for (JzrResourceLocation location : locations){
			if (location instanceof SingleJzrResourceLocation)
				resolveLocation((SingleJzrResourceLocation)location, paths, LocationType.STICKER_SET);
			if (location instanceof MultipleJzrResourceLocation)
				resolveLocations((MultipleJzrResourceLocation)location, paths, LocationType.STICKER_SET);
		}
		
		if (paths.isEmpty())
			throw new JzrServiceException("Stickers set configuration loading failed. No valid sticker set location found.");
		
		return paths;
	}

	private void resolveLocation(SingleJzrResourceLocation location, List<String> paths, LocationType type) throws JzrServiceException {
		String path;
		if (location.getPath().startsWith(RepositoryLocationResolver.REPO_PREFIX))
			path = repoResolver.resolveLocation(location.getPath(), location.getVersion(), type);
		else{
			path = location.getPath();
		}

		if (path != null && !paths.contains(path.intern())) // prevent to get path added from multiple sources (jar, module..)
			paths.add(path.intern());
	}
	
	private boolean isJeyzerManagedModule(ProcessModule module) {
		return !(module.getName().startsWith(UNMANAGED_MODULE_JAVA) || module.getName().startsWith(UNMANAGED_MODULE_JDK));
	}

	private void resolveLocations(MultipleJzrResourceLocation location, List<String> paths, LocationType type) throws JzrServiceException {
		paths.addAll(repoResolver.resolveLocations(location.getLocations(), type));
	}
	
	private void resolveDynamicLocations(ProcessJarVersion jarVersion, List<String> repoIds, List<String> paths, LocationType type) {
		// add every path found, meaning if defined several times it will generate duplicate pattern warnings in logs. 
		// Patterns, rules, stickers should be defined only once for a given jar or jar version : profile fixing is left to the profile administrator.
		for (String repoId : repoIds) {
			String path;
			// with version
			if (!jarVersion.hasNoVersion()) {
				// add also the version directory
				path = RepositoryLocationResolver.REPO_PREFIX + repoId + "/" + jarVersion.getJarName();
				resolveDynamicLocation(path,jarVersion.getJarVersion(), paths, type);
			}
			// without version
			path = RepositoryLocationResolver.REPO_PREFIX + repoId + "/" + jarVersion.getJarName();
			resolveDynamicLocation(path, null, paths, type);
		}
	}
	
	private void resolveDynamicLocations(ProcessModule module, List<String> repoIds, List<String> paths, LocationType type) {
		// add every path found, meaning if defined several times it will generate duplicate pattern warnings in logs. 
		// Patterns, rules, stickers should be defined only once for a given module or module version : profile fixing is left to the profile administrator.
		for (String repoId : repoIds) {
			String path;
			// with version
			if (!module.hasNoVersion()) {
				// add also the version directory
				path = RepositoryLocationResolver.REPO_PREFIX + repoId + "/" + module.getName();
				resolveDynamicLocation(path,module.getVersion(), paths, type);
			}
			// without version
			path = RepositoryLocationResolver.REPO_PREFIX + repoId + "/" + module.getName();
			resolveDynamicLocation(path, null, paths, type);
		}
	}
	
	private void resolveDynamicLocation(String path, String version, List<String> paths, LocationType type) {
		SingleJzrResourceLocation location = new SingleJzrResourceLocation(path, version);
		try {
			resolveLocation(location, paths, type);
		} catch (JzrServiceException e) {
			// Acceptable to not get the requested profile resource in dynamic mode, especially when scanning
			if (logger.isDebugEnabled())
				logger.debug("Dynamic profile loading could not complete : profile resource " + path + " not found on the repository.");
		}
	}

	private List<String> listRepositoryIds(ProcessJarVersion jarVersion, boolean declaredRepoOnly) {
		List<String> repoIds = new ArrayList<>();
		if (declaredRepoOnly) {
			String jarRepoId = jarVersion.getJeyzerRepositoryId();
			if (this.repoResolver.contains(jarRepoId))
				repoIds.add(jarVersion.getJeyzerRepositoryId());
		}
		else {
			repoIds.addAll(this.repoResolver.getRepositoryIds());
		}
		return repoIds;
	}
}
