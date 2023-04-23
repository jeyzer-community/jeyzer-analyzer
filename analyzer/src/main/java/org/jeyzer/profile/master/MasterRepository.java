package org.jeyzer.profile.master;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 - 2021 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterRepository {
	
	private static final Logger logger = LoggerFactory.getLogger(MasterRepository.class);

	private static final String ANALYSIS_FILE_SUFFIX = "_analysis.xml";
	
	private Map<String, MasterProfile> profiles = new TreeMap<>();
	
	public MasterRepository(List<File> rootDirs) {		
		for (File rootDir : rootDirs) {
			scanProfiles(rootDir);			
		}
	}
	
	private void scanProfiles(File rootDir) {
		if (!rootDir.exists()){
			logger.warn("Profile root directory not found : {} . Profile root directory ignored.", rootDir.getAbsolutePath());
			return;
		}
		
		logger.info("Loading the master profile repository using the root directory : {}", rootDir);
		
		File[] directories = rootDir.listFiles(file -> file.isDirectory());
		
		// sort the files (required on Linux)
		Arrays.sort(directories);
		
		MasterProfileBuilder profileBuilder = new MasterProfileBuilder();
		for (File profileDir : directories){
			String profileName = profileDir.getName();
			
			File analysisFile = new File(profileDir + File.separator + profileName + ANALYSIS_FILE_SUFFIX);
			MasterProfile profile = profileBuilder.loadProfile(profileName, analysisFile);
			
			if (profile != null)
				this.profiles.put(profile.getType(), profile);
		}
	}
	
	public boolean isEmpty() {
		return this.profiles.isEmpty();
	}
	
	public Set<String> getProfileNames(){
		return this.profiles.keySet();
	}

	public MasterProfile getProfile(String name) {
		return this.profiles.get(name);
	}
	
	public List<MasterProfile> getMasterProfiles(String exclude){
		List<MasterProfile> masterProfiles = new ArrayList<>();
		
		for (Entry<String, MasterProfile> entry : this.profiles.entrySet()) {
			if (!entry.getKey().equals(exclude))
				masterProfiles.add(entry.getValue());
		}
		
		return masterProfiles;
	}
}
