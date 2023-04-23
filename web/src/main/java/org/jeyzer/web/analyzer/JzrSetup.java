package org.jeyzer.web.analyzer;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Web
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletException;

import org.jeyzer.profile.master.MasterProfile;
import org.jeyzer.profile.master.MasterRepository;

public class JzrSetup {
	
	public static final String JZR_DISCOVERY_PROFILE = "Discovery";
	
	private static final String ENV_JZR_MASTER_PROFILES_DIR_ROOTS = "JEYZER_MASTER_PROFILES_DIR_ROOTS";
	private static final String ENV_JZR_DEFAULT_PROFILE = "JEYZER_ANALYZER_DEFAULT_PROFILE";
	private static final String ENV_JZR_GENERATE_PROCESS_MUSIC = "JEYZER_ANALYZER_GENERATE_PROCESS_MUSIC";
	
	private static final String SYSTEM_PROPERTY_OS_NAME = "os.name";  
	private static final String WINDOWS_OS = "Win";
	
	private String defaultProfile;
	private boolean generateMusic;
	private MasterRepository repository;
	
	public JzrSetup() throws ServletException{
		String profileRootPaths;
		
		defaultProfile = System.getenv(ENV_JZR_DEFAULT_PROFILE);
		if (defaultProfile == null)
			defaultProfile = JZR_DISCOVERY_PROFILE;
		
		profileRootPaths = System.getenv(ENV_JZR_MASTER_PROFILES_DIR_ROOTS);
		if (profileRootPaths == null)
			throw new ServletException ("Jeyzer Analyzer initialization failure : " + ENV_JZR_MASTER_PROFILES_DIR_ROOTS + " variable not set.");

		boolean windows = System.getProperty(SYSTEM_PROPERTY_OS_NAME).startsWith(WINDOWS_OS);
		StringTokenizer tokenizer = new StringTokenizer(profileRootPaths, windows ? ";" : ":");
		List<File> rootPaths = new ArrayList<>();
		while(tokenizer.hasMoreTokens())
			rootPaths.add(new File(tokenizer.nextToken()));
		
		this.repository = new MasterRepository(rootPaths);
		if (this.repository.isEmpty())
			throw new ServletException ("Jeyzer Analyzer initialization failure : no valid profile found in directories " + profileRootPaths);
		
		String value = System.getenv(ENV_JZR_GENERATE_PROCESS_MUSIC);
		generateMusic = Boolean.parseBoolean(value);
	}
	
	public Set<String> getProfiles(){
		return repository.getProfileNames();
	}
	
	public MasterProfile getProfile(String name){
		return repository.getProfile(name);
	}
	
	public MasterRepository getMasterRepository(){
		return repository;
	}

	public String getDefaultProfile(){
		return this.defaultProfile;
	}
	
	public boolean isProcessMusicGenerationEnabled(){
		return this.generateMusic;
	}

	public void validate() {
		String defaultProfileName = getDefaultProfile();
		if (defaultProfileName == null)
			throw new RuntimeException("Default profile configuration is invalid. Please contact your administrator.");
		
		MasterProfile defaultprofile = getProfile(defaultProfileName);
		if (defaultprofile == null)
			throw new RuntimeException("Profile configuration is not found for default profile : " + defaultProfileName + ". Please contact your administrator.");
		
		if (getProfiles().isEmpty())
			throw new RuntimeException("Profile list is empty. Please contact your administrator.");
	}
}
