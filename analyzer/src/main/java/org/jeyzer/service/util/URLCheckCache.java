package org.jeyzer.service.util;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.jeyzer.analyzer.util.SystemHelper;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class URLCheckCache {
	
	private boolean enabled;
	private LoadingCache<String, Boolean> cache;

	public URLCheckCache(Duration ttl, boolean enabled) {
		this.enabled = enabled;
		if (this.enabled){
			cache = CacheBuilder.newBuilder()
				       .expireAfterWrite(ttl.toMillis(), TimeUnit.MILLISECONDS)
				       .build(
				               new CacheLoader<String, Boolean>() {
				                 public Boolean load(String key) {
				                   return SystemHelper.doesURLExist(key);
				                 }
				               });
		}
	}
	
	public Boolean getUrlCheckResult(String url){
		if (!this.enabled)
			return SystemHelper.doesURLExist(url);
		
		try {
			return cache.get(url);
		} catch (ExecutionException ex) {
			// redo it
			return SystemHelper.doesURLExist(url);
		}
	}

}
