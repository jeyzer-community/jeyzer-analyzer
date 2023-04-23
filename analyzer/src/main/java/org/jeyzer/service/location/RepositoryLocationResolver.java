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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jeyzer.analyzer.config.repository.ConfigRepository;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrServiceException;
import org.jeyzer.analyzer.setup.RepositorySetupManager;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.service.location.JzrLocationResolver.LocationType;
import org.jeyzer.service.repository.RepositoryCache;
import org.jeyzer.service.util.URLCheckCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryLocationResolver {
	
	private static final Logger logger = LoggerFactory.getLogger(RepositoryLocationResolver.class);
	
	public static final String REPO_PREFIX = "repo://";
	
	private Map<String, ConfigRepository> repositoryCfgs;
	private DependencyResolver dependencyResolver;
	private RepositoryCache repositoryCache;
	private URLCheckCache repositoryURLCache;
	
	public RepositoryLocationResolver(RepositorySetupManager repoSetupMgr, DependencyResolver dependencyResolver) throws JzrInitializationException {
		this.repositoryCfgs = repoSetupMgr.getRepositoryCfgs();
		this.dependencyResolver = dependencyResolver;
		// caches use the same settings
		this.repositoryCache = new RepositoryCache(repoSetupMgr.getCacheCfg());
		this.repositoryURLCache = new URLCheckCache(repoSetupMgr.getCacheCfg().getCacheTimeToLive(), repoSetupMgr.getCacheCfg().isCachingEnabled());
	}

	public String resolveLocation(String location, String version, LocationType type) throws JzrServiceException {
		validateLocation(location);

		String path = resolvePath(location, version, false, type);
		
		String urlPath = SystemHelper.isRemoteProtocol(path) ? path : SystemHelper.getFilePrefix() + path;

		// test if file exists
		if (!doesUrlExist(urlPath)){
			logger.info("Prefered repository path " + path + " is not accessible. Loading now the fallback path.");
			path = resolvePath(location, version, true, type);
			
			urlPath = SystemHelper.isRemoteProtocol(path) ? path : SystemHelper.getFilePrefix() + path;
			if (!doesUrlExist(urlPath))
				if (LocationType.PATTERN_SET.equals(type))
					throw new JzrServiceException("Fallback repository path " + path + " is not accessible for the repository location : " + location + ". Please review the analysis pattern set configuration.");
				else {
					logger.info("Fallback repository path " + path + " is not accessible. This could be normal if the target profile doesn't contain any monitoring rule.");
					return null;  // monitor rule file not found. Those are optional.
				}
		}

		// get the cached version of the file (or cache it) 
		if (SystemHelper.isRemoteProtocol(urlPath)){
			String localPath = repositoryCache.getPath(path);
			if (localPath != null)
				return localPath;
		}
		
		return path;
	}
	
	private boolean doesUrlExist(String path) {
		if (SystemHelper.isRemoteProtocol(path))
			// cache the result for http, https, ftp calls
			return this.repositoryURLCache.getUrlCheckResult(path);
		
		return SystemHelper.doesURLExist(path);
	}

	public Collection<String> resolveLocations(String locations, LocationType type) throws JzrServiceException {
		List<String> paths = new ArrayList<>();

		if (!locations.endsWith(DependencyResolver.DEPS_TAG))
			throw new JzrServiceException("Failed to resolve the multiple repository locations. Repository url must start with " + RepositoryLocationResolver.REPO_PREFIX + ". Current path is : " + locations);		
		
		// resolve dependencies		
		Collection<String> deps = this.dependencyResolver.resolveDependencies(locations);
		
		String rootLocation = locations.substring(0, locations.indexOf(DependencyResolver.DEPS_TAG));
		
		for (String dependency : deps){
			String path = resolveLocation(rootLocation + dependency, null, type);
			if (path != null) 
				paths.add(path);
		}
		
		return paths;
	}
	
	private void validateLocation(String location) throws JzrServiceException {
		if (!location.startsWith(REPO_PREFIX))
			throw new JzrServiceException("Invalid profile repository url : " + location + ". Repository location must start with the \"repo://\" prefix . Please review the profile configuration.");
		
		if (location.contains(DependencyResolver.DEPS_TAG))
			throw new JzrServiceException("Invalid profile repository url : " + location + ". Dependencies tag " + DependencyResolver.DEPS_TAG + " cannot be used on single repository url. Please review the profile configuration.");
		
		if (location.endsWith("/") || location.endsWith("\\"))
			throw new JzrServiceException("Invalid profile repository url : " + location + ". Dependency must be specified at last. Please review the profile configuration.");

	}

	private String resolvePath(String location, String version, boolean fallback, LocationType type) throws JzrServiceException {
		String repoLocation = resolveRepository(location, fallback);
		String locationSuffix = location.substring(location.indexOf('/', REPO_PREFIX.length()+1));
		
		// update the location suffix
		// /xyz/<profile name>   -->  /xyz/<profile name>/<profile name>_patterns.xml
		// /xyz/<profile name>   -->  /xyz/<profile name>/<profile name>_rules.xml
		String name = location.substring(location.lastIndexOf('/'));
		
		// version      -->  /xyz/<profile name>/<version>/<profile name>_patterns.xml
		// no version   -->  /xyz/<profile name>/<profile name>_patterns.xml
		if (version != null)
			return repoLocation + locationSuffix + "/" + version + name + type.getSuffix();
		else
			return repoLocation + locationSuffix + name + type.getSuffix();
	}

	private String resolveRepository(String location, boolean fallback) throws JzrServiceException {
		// repo://(id to resolve)/
		int repoEnd = location.indexOf('/', REPO_PREFIX.length()+1);
		String repoId = location.substring(REPO_PREFIX.length(), repoEnd);
		
		ConfigRepository repo = this.repositoryCfgs.get(repoId);
		
		if (!fallback && repo == null)
			throw new JzrServiceException("Repository not found for repo id " + repoId + ". Please review the repository configuration.");
		
		return fallback? repo != null ? repo.getFallback() : null : repo.getPrefered();
	}

	public Set<String> getRepositoryIds() {
		return this.repositoryCfgs.keySet();
	}

	public boolean contains(String jarRepoId) {
		return jarRepoId != null ? this.repositoryCfgs.containsKey(jarRepoId) : false;
	}
}
