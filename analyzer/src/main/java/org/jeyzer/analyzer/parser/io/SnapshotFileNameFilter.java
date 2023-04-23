package org.jeyzer.analyzer.parser.io;

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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jeyzer.analyzer.config.analysis.ConfigFilePattern;
import org.jeyzer.analyzer.util.AnalyzerHelper;

public class SnapshotFileNameFilter implements FilenameFilter {

	private Map<String, SnapshotFileNamePattern> successPatterns = new HashMap<>();
	
	private List<SnapshotFileNamePattern> filePatterns = new ArrayList<>();
	
	public SnapshotFileNameFilter(List<ConfigFilePattern> confPatterns){
		SnapshotFileNamePatternBuilder builder = SnapshotFileNamePatternBuilder.newInstance();
		this.filePatterns = builder.buildPatterns(confPatterns);
	}
	
	@Override
	public boolean accept(File dir, String name) {
		if (successPatterns.containsKey(name))
			return true;
		
		// exclude the static files : those have *.txt suffix which are conflicting 
		//   when *.txt is also a snapshot file name pattern
		if (AnalyzerHelper.isRecordingStaticFile(new File(name)))
			return false;
		
		for (SnapshotFileNamePattern pattern : filePatterns){
			if (pattern.accept(name)){
				successPatterns.put(name, pattern);
				return true;
			}
		}
		return false;
	}

	public SnapshotFileNamePattern getFilePattern(String filename) {
		SnapshotFileNamePattern successPattern = successPatterns.get(filename);
		if (successPattern != null)
			return successPattern;
		
		for (SnapshotFileNamePattern pattern : filePatterns){
			if (pattern.accept(filename)){
				successPatterns.put(filename, pattern);
				return pattern;
			}
		}
		return null;
	}
	
	public Set<String> getSupportedFileFormats(){
		Set<String> supportedFileFormats = new TreeSet<>();
		for (SnapshotFileNamePattern filePattern : filePatterns)
			supportedFileFormats.add(filePattern.getDescriptor());
		return supportedFileFormats;
	}
}
