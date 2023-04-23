package org.jeyzer.analyzer.input.translator;

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

public class TranslateData {

	private final File[] tds;
	private final File processCard;
	private final File processJarPaths;
	private final File processModules;
	private final File jvmFlags;
	private final File directory;
	
	public TranslateData(File[] tds, File processCard, File processJarPaths, File processModules, File jvmFlags, File directory) {
		this.tds = tds;
		this.processCard = processCard;
		this.processJarPaths = processJarPaths;
		this.processModules = processModules;
		this.jvmFlags = jvmFlags;
		this.directory = directory;
	}
	
	public File[] getTDs() {
		return tds;
	}

	public File getProcessCard() {
		return processCard;
	}
	
	public File getProcessJarPaths() {
		return processJarPaths;
	}
	
	public File getProcessModules() {
		return processModules;
	}
	
	public File getJVMFlags() {
		return jvmFlags;
	}

	public File getDirectory() {
		return directory;
	}
}
