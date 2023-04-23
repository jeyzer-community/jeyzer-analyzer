package org.jeyzer.analyzer.data.jar;

import java.util.Map;
import java.util.TreeMap;

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


import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Comparator;

public class ProcessJarVersion {
	
	public static final String JAR_EXTENSION = ".jar";
		
	private static final Pattern JAR_VERSION_PATTERN = Pattern.compile("\\w+.*-([\\d\\.]+)(-?[\\w\\.-^0-9]+)?\\.jar$");
	private static final String JAR_MANIFEST_JEYZER_REPOSITORY_FIELD = "Jeyzer-Repository";
	
	private String jarPath;
	private String jarFileName;
	private String jarName;
	private boolean snapshot = false;
	private Map<ProcessJarVersionType, String> versions = new TreeMap<ProcessJarVersionType, String>(
		new Comparator<ProcessJarVersionType>(){
            @Override
            public int compare(ProcessJarVersionType t1, ProcessJarVersionType t2) {
                return t1.getPriority() > t2.getPriority() ? -1 : t1.getPriority() == t2.getPriority() ? 0 : 1;
            }
		}
	);
	private String jeyzerRepoId;

	public ProcessJarVersion(String jarEntry) {
		String[] elements = jarEntry.split(";");
		this.jarPath = elements[0];
		extractData(elements[0]);
		loadManifestAttributes(elements);
	}	
	
	public String getJarPath() {
		return jarPath;
	}
	
	public String getJarFileName() {
		return jarFileName;
	}
	
	public String getJarName() {
		return jarName;
	}
	
	public String getJarVersion() {
		if (this.versions.isEmpty())
			return null;
		return this.versions.values().iterator().next(); // first entry
	}
	
	public String getJarVersion(ProcessJarVersionType type) {
		return this.versions.get(type); // can be null
	}
	
	public boolean isSnapshot() {
		return snapshot;
	}
	
	public boolean hasNoVersion() {
		return versions.isEmpty();
	}

	public String getJeyzerRepositoryId() {
		return this.jeyzerRepoId; // can be null
	}
	
	private void extractData(String path) {
		int index = path.lastIndexOf('/');
		if (index == -1) {
			// not expected
			this.jarName = path;
			return;
		}
		
		this.jarFileName = path.substring(index+1);
		index = jarFileName.lastIndexOf('.');
		if (index == -1) {
			// not expected
			this.jarName = path;
			return;
		}
		
		// Tested OK with :
		//  file:/C:/demo-pg/jeyzer-76/recorder/lib/jeyzer-recorder-2.0.1-SNAPSHOT.jar
		//  file:/C:/demo-pg/jeyzer-76/recorder/lib/jeyzer-recorder-3.3.3.jar
		//  file:/C:/demo-pg/jeyzer-76/recorder/lib/mylib-1.0.0-alpha.beta.jar
		//  file:/C:/demo-pg/jeyzer-76/recorder/lib/jeyzer-recorder-3.0.jar
		//  file:/C:/demo-pg/jeyzer-76/recorder/lib/jeyzer-recorder-3.0beta.jar
		//  file:/C:/demo-pg/jeyzer-76/recorder/lib/jeyzer-recorder-3.0beta4.jar
		//  file:/C:/demo-pg/jeyzer-76/recorder/lib/jeyzer-recorder-3-SNAPSHOT.jar

		Matcher matcher = JAR_VERSION_PATTERN.matcher(jarFileName);
		
		if (matcher.matches()) {
			String version;
			if (matcher.group(2) == null) {
				// just the version
				version = matcher.group(1);
			}
			else {
				// version + snapshot
				this.snapshot = true;
				version = matcher.group(1) + matcher.group(2);
			}
			index = jarFileName.indexOf(version);
			this.versions.put(ProcessJarVersionType.JAR_FILE_VERSION, version);
			this.jarName = jarFileName.substring(0, index-1);
		}
		else {
			index = jarFileName.indexOf(JAR_EXTENSION);
			this.jarName = jarFileName.substring(0, index);
		}
	}

	private void loadManifestAttributes(String[] elements) {
		if (elements.length <= 1)
			return;
		
		for (int i=1; i<elements.length; i++)
			loadManifestAttribute(elements[i]);
	}

	private void loadManifestAttribute(String element) {
		if (element == null)
			return;
		
		// Specification-Version=2.6.1
		String[] couple = element.split("=");
		if (couple.length <= 1)
			return;
		
		if (couple[0] == null || couple[1] == null || couple[0].isEmpty() || couple[1].isEmpty())
			return;
		
		// Versions
		ProcessJarVersionType type = ProcessJarVersionType.loadProcessJarManifestVersion(couple[0]);
		if (type != null)
			versions.put(type, couple[1]);
		
		// Jeyzer repository
		if (JAR_MANIFEST_JEYZER_REPOSITORY_FIELD.equalsIgnoreCase(couple[0]))
			this.jeyzerRepoId = couple[1];
	}
}
