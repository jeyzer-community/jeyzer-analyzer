package org.jeyzer.service.repository;

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
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jeyzer.analyzer.config.repository.ConfigRepositoryCache;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryCache {

	private static final Logger logger = LoggerFactory.getLogger(RepositoryCache.class);
	
	private boolean enabled;
	private String rootPath;
	private Map<String, RepositoryCacheEntry> cache;
	private Duration ttl;

	public RepositoryCache(ConfigRepositoryCache cfg) throws JzrInitializationException {
		this.enabled = cfg.isCachingEnabled();
		if (this.enabled){
			this.rootPath = cfg.getCacheDirectoryRoot();
			try {
				SystemHelper.createDirectory(this.rootPath);
			} catch (JzrException ex) {
				throw new JzrInitializationException("Failed to create the repository cache root directory. Please make sure path is valid.", ex);
			}
			this.cache = new ConcurrentHashMap<String, RepositoryCacheEntry>();
			this.ttl = cfg.getCacheTimeToLive();
		}
	}
	
	public String getPath(String remotePath){
		if (!this.enabled)
			return null;
		
		if (!SystemHelper.isRemoteProtocol(remotePath)){
			logger.warn("Trying to cache or get a local repository file from the remote repository cache. File is : " + remotePath);
			return remotePath; // not remote path
		}
		
		if (!this.cache.containsKey(remotePath)){
			return addEntry(remotePath);
		}
		
		RepositoryCacheEntry entry = this.cache.get(remotePath);
		
		// refresh the entry if too old
		if (entry.getLastRefresh().getTime() - (new Date()).getTime() > ttl.toMillis())
			refresh(entry, remotePath);
		
		return entry.getCachedPath();
	}
	
	private void refresh(RepositoryCacheEntry entry, String remotePath) {
		if (cacheRemoteFile(remotePath) != null)
			entry.refresh();
		else
			logger.warn("Failed to refresh the cache entry with remote file. Will keep serving the current local cached file for now.");
	}

	private String cacheRemoteFile(String remotePath) {
		String cachePathSuffix = stripProtocol(remotePath);
		if (cachePathSuffix == null)
			return null;
		
		String localPath = rootPath + "/" + cachePathSuffix.replace(":","-");
		try {
			File localFile = new File(localPath);
			SystemHelper.createDirectory(localFile.getParent());
			SystemHelper.downloadFile(remotePath, localFile.getAbsolutePath());
		} catch (Exception ex) {
			logger.warn("Failed to cache remote file : " + remotePath + " locally as : " + SystemHelper.sanitizePathSeparators(localPath) + ". Reason : " + ex.getMessage());
			return null;
		}
		return localPath;
	}

	private String addEntry(String remotePath) {
		String cachedLocalPath = cacheRemoteFile(remotePath);
		if (cachedLocalPath != null){
			RepositoryCacheEntry entry = new RepositoryCacheEntry(cachedLocalPath);
			cache.put(remotePath, entry);
			return entry.getCachedPath();
		}
		return remotePath;
	}

	private String stripProtocol(String remotePath) {
		String suffix = null;
		
		if (remotePath.startsWith(SystemHelper.HTTP_PREFIX))
			suffix = remotePath.substring(SystemHelper.HTTP_PREFIX.length());
		else if (remotePath.startsWith(SystemHelper.HTTPS_PREFIX))
			suffix = remotePath.substring(SystemHelper.HTTPS_PREFIX.length());
		else if (remotePath.startsWith(SystemHelper.FTP_PREFIX))
			suffix = remotePath.substring(SystemHelper.FTP_PREFIX.length());
		else
			logger.warn("Remote protocol not supported on path : " + remotePath);
		
		return suffix;
	}

	static class RepositoryCacheEntry {
		private Date lastRefresh;
		private String cachedPath;
		
		public RepositoryCacheEntry(String cachedPath){
			this.cachedPath = cachedPath;
			this.lastRefresh = new Date();
		}
		
		public Date getLastRefresh() {
			return lastRefresh;
		}
		
		public void refresh() {
			this.lastRefresh = new Date();
		}
		
		public String getCachedPath() {
			return cachedPath;
		}
	}
}
