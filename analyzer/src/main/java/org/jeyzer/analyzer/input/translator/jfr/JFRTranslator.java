package org.jeyzer.analyzer.input.translator.jfr;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.jeyzer.analyzer.config.translator.ConfigTranslator;
import org.jeyzer.analyzer.config.translator.jfr.ConfigJFRDecompression;
import org.jeyzer.analyzer.error.JzrTranslatorException;
import org.jeyzer.analyzer.error.JzrTranslatorJFRInvalidVersionException;
import org.jeyzer.analyzer.error.JzrTranslatorMultipleJFRFilesException;
import org.jeyzer.analyzer.input.translator.TranslateData;
import org.jeyzer.analyzer.input.translator.Translator;
import org.jeyzer.analyzer.input.translator.jfr.mapper.JZRRecordingMapper;
import org.jeyzer.analyzer.input.translator.jfr.reader.JFRDescriptor;
import org.jeyzer.analyzer.input.translator.jfr.reader.JFRReader;
import org.jeyzer.analyzer.parser.io.SnapshotFileNameFilter;
import org.jeyzer.analyzer.status.JeyzerStatusEvent;
import org.jeyzer.analyzer.status.JeyzerStatusEvent.STATE;
import org.jeyzer.analyzer.util.JFRHelper;
import org.jeyzer.analyzer.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.jfr.consumer.RecordingFile;

public class JFRTranslator implements Translator {

	private static final Logger logger = LoggerFactory.getLogger(JFRTranslator.class);
	
	public static final String NAME = "jfr";
	private static final short PRIORITY = 100;
	
	private List<File> jzrSnapshots;
	
	private ConfigJFRDecompression jfrCfg;
	
	public JFRTranslator(ConfigJFRDecompression cfg) {
		this.jfrCfg = cfg;
	}
	
	@Override
	public boolean accept(TranslateData input) throws JzrTranslatorException {
		if (input.getDirectory() == null)
			return false;

		if (input.getDirectory().isDirectory()){
			if (JFRHelper.detectJFRFile(input.getTDs())){
				if (input.getTDs().length != 1) {
					logger.error("Multiple files submitted for one JFR analysis");
					throw new JzrTranslatorMultipleJFRFilesException("JFR analysis applies only on one JFR file. Please submit the JFR file alone (zipped or not).");
				}
			}
			else {
				return false;
			}
		}

		if (!this.jfrCfg.isEnabled()) {
			logger.error("JFR analysis not supported on the JDK used by the Jeyzer Analyzer");
			throw new JzrTranslatorException("JFR analysis not supported on the JDK used by the Jeyzer Analyzer. Please ask your administrator to run it on Java 11+.");
		}
		
		return true;
	}

	@Override
	public TranslateData translate(TranslateData input, SnapshotFileNameFilter filter, Date sinceDate) throws JzrTranslatorException {
		if (input.getDirectory().isDirectory()) {
			// take the zip content as JFR
			input = new TranslateData(
					input.getTDs(),
					input.getProcessCard(),
					input.getProcessJarPaths(),
					input.getProcessModules(),
					input.getJVMFlags(),
					input.getTDs()[0]
					);
		}
		
		logger.info("Loading the JFR recording : {}", input.getDirectory().getName());
		
		validateJFRFile(input);

		JFRReader reader = new JFRReader(this.jfrCfg);
		JFRDescriptor descriptor = reader.load(input.getDirectory().getAbsolutePath());

		logger.info("Recording JFR file uncompressed in directory : " + SystemHelper.sanitizePathSeparators(this.jfrCfg.getOuputDirectory()));

		JZRRecordingMapper recordingMapper = new JZRRecordingMapper(this.jfrCfg);

		File processCardFile = recordingMapper.generateProcessCard(descriptor);
		File processJarPathsFile = recordingMapper.generateProcessJarPaths(descriptor);
		File processModulesFile = recordingMapper.generateProcessModules(descriptor);
		File jvmFlagFile = recordingMapper.generateJVMFlags(descriptor);
		
		this.jzrSnapshots = recordingMapper.generateJZRSnapshots(descriptor);

		return new TranslateData(
				jzrSnapshots.toArray(new File[jzrSnapshots.size()]),
				processCardFile,
				processJarPathsFile,
				processModulesFile,
				jvmFlagFile,
				new File(this.jfrCfg.getOuputDirectory())
				);
	}

	private RecordingFile validateJFRFile(TranslateData input) throws JzrTranslatorException {
		String jfrPath = input.getDirectory().getAbsolutePath();
		if (!JFRHelper.isJFRFile(jfrPath)){
			logger.error("Failed to read the JFR recording. File must have extension jfr. Provided file is : " + input.getDirectory().getName());
			throw new JzrTranslatorException("Failed to read the JFR recording. File extension must be .jfr");
		}
		
		try {
			Path path =  Paths.get(jfrPath);
			return new RecordingFile(path);			
		}
		catch(Exception ex) {
			logger.error("Failed to read the JFR recording. JFR file is invalid.", ex);
			if (ex.getMessage().startsWith("File version")) {
				String message = "Submitted JFR recording version is 0.9 which is not supported by the Jeyzer Analyzer. "
						+ "If your application is running under Java 8, please migrate to the latest OpenJDK 8 to get a JFR 1.0 recording. "
						+ "Under Java 7, use the Jeyzer Recorder agent to get a JZR recording.";
				throw new JzrTranslatorJFRInvalidVersionException(message);
			}
			else if (ex.getMessage().startsWith("Not a Flight Recorder file")) {
				String message = "Submitted JFR recording cannot be parsed by the Open JDK. "
						+ "If your application is running under Java 8, please migrate to the latest OpenJDK 8 to get a JFR 1.0 recording. "
						+ "Under Java 7, use the Jeyzer Recorder agent to get a JZR recording.";
				throw new JzrTranslatorJFRInvalidVersionException(message);
			}
			else {
				throw new JzrTranslatorException("Failed to read the JFR recording. JFR file is invalid : make sure it has been generated with the latest OpenJDK 8 or Java 11.");				
			}
		}
		catch(java.lang.NoClassDefFoundError error) {
			logger.error("Attempting to read a JFR recording on a Jeyzer running on Java 8.", error);
			throw new JzrTranslatorException("The Jeyzer installation is running under Java 8 and cannot threfore analyze JFR files. Please install it under Java 11+ to allow it.");
		}
	}

	@Override
	public STATE getStatusEventState() {
		return JeyzerStatusEvent.STATE.JFR_READING;
	}

	@Override
	public short getPriority() {
		return PRIORITY;
	}

	@Override
	public ConfigTranslator getConfiguration() {
		return this.jfrCfg;
	}

	@Override
	public boolean isEnabled() {
		return this.jfrCfg.isEnabled();
	}

	@Override
	public void close() {
		if (jfrCfg.isEnabled() && !jfrCfg.areTranslatedFilesKept() && jzrSnapshots != null){
			logger.info("Cleaning up the JFR uncompressed recording directory : " + jfrCfg.getOuputDirectory());
			cleanJFRDirectory(jzrSnapshots);
		}
		if (jzrSnapshots != null) {
			jzrSnapshots.clear();
			jzrSnapshots = null;
		}
	}
	
	public void cleanJFRDirectory(List<File> unzippedFiles) {
		// delete the intermediary files
		for (File file : unzippedFiles) {
			if (file.exists() && !file.delete())
				logger.warn("Failed to delete the JFR uncompressed file : " + file.getPath());
		}
		unzippedFiles.clear();
	}
}
