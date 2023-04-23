package org.jeyzer.analyzer.config.translator;

import java.util.ArrayList;
import java.util.List;

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




public class ConfigTranslator {

	protected String type;
	protected boolean keepTranslatedFiles = false;
	protected boolean enabled = false;
	protected String outputDir;
	protected String configFilePath;
	protected boolean abortOnError;
	protected List<String> supportedInputFileExtensions = new ArrayList<>();

	public ConfigTranslator(String type, boolean enabled, String outputDir, String configFilePath, boolean keepTranslatedFiles, boolean abortOnError) {
		this.type = type;
		this.enabled = enabled;
		this.outputDir = outputDir;
		this.configFilePath = configFilePath;
		this.keepTranslatedFiles = keepTranslatedFiles;
		this.abortOnError = abortOnError;
	}
	
	public ConfigTranslator(String type, String configFilePath, boolean abortOnError) {
		this.type = type;
		this.configFilePath = configFilePath;
		this.abortOnError = abortOnError;
	}

	public boolean areTranslatedFilesKept() {
		return keepTranslatedFiles;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public String getOuputDirectory() {
		return outputDir;
	}
	
	public boolean isAbortOnError(){
		return abortOnError;
	}
	
	public String getType(){
		return type;
	}
	
	public List<String> getSupportedInputFileExtensions(){
		return this.supportedInputFileExtensions;
	}
}
