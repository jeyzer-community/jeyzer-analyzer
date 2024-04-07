package org.jeyzer.analyzer.input.translator.compression;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jeyzer.analyzer.config.translator.ConfigTranslator;
import org.jeyzer.analyzer.config.translator.compression.ConfigDecompression;
import org.jeyzer.analyzer.data.ProcessCard;
import org.jeyzer.analyzer.data.flags.JVMFlags;
import org.jeyzer.analyzer.data.jar.ProcessJars;
import org.jeyzer.analyzer.data.module.ProcessModules;
import org.jeyzer.analyzer.error.JzrTranslatorException;
import org.jeyzer.analyzer.error.JzrTranslatorRecordingSnapshotNotFoundException;
import org.jeyzer.analyzer.input.translator.TranslateData;
import org.jeyzer.analyzer.input.translator.Translator;
import org.jeyzer.analyzer.parser.io.ThreadDumpFileDateComparator;
import org.jeyzer.analyzer.parser.io.SnapshotFileNameFilter;
import org.jeyzer.analyzer.status.JeyzerStatusEvent;
import org.jeyzer.analyzer.status.JeyzerStatusEvent.STATE;
import org.jeyzer.analyzer.util.JFRHelper;
import org.jeyzer.analyzer.util.SystemHelper;
import org.jeyzer.analyzer.util.ZipHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompressionTranslator implements Translator {

	private static final Logger logger = LoggerFactory.getLogger(CompressionTranslator.class);		
	
	public static final String NAME = "compression";
	private static final short PRIORITY = 100;
	
	private List<File> uncompressedFiles;
	
	private ConfigDecompression compressCfg;
	
	public CompressionTranslator(ConfigDecompression cfg) {
		this.compressCfg = cfg;
	}

	@Override
	public boolean accept(TranslateData input) throws JzrTranslatorException {
		if (input.getDirectory() == null || input.getDirectory().isDirectory())
			return false;
		
		String zipPathCandidate = input.getDirectory().getAbsolutePath();
		if (JFRHelper.isJFRFile(zipPathCandidate))
			return false;
		
		if (!ZipHelper.isZipFile(zipPathCandidate) && !ZipHelper.isGzipFile(zipPathCandidate)){
			logger.error("Failed to uncompress the recording. File must have extension gz or zip. Provided file is : " + input.getDirectory().getName());
			throw new JzrTranslatorException("Failed to uncompress the recording. File must have extension gz or zip.");
		}

		return true;
	}
	
	@Override
	public TranslateData translate(TranslateData input, SnapshotFileNameFilter filter, Date sinceDate) throws JzrTranslatorException {		
		String zipPathCandidate = input.getDirectory().getAbsolutePath();
		
		// unzip files. In case of failure, files get deleted on the translator closure if configured to do so
		this.uncompressedFiles = ZipHelper.uncompress(zipPathCandidate, this.compressCfg);			

		logger.info("Recording zip file uncompressed in directory : " + SystemHelper.sanitizePathSeparators(this.compressCfg.getOuputDirectory()));
		
		// find the unzipped directory, its tds and its process card (optional)
		File[] uncompressedTds = detectTDs(this.uncompressedFiles, filter);
		File tdDir = new File(uncompressedTds[0].getParent());
		File processCardFile = getFile(this.uncompressedFiles, ProcessCard.PROCESS_CARD_FILE_NAME); // can be null
		File processJarPathsFile = getFile(this.uncompressedFiles, ProcessJars.PROCESS_JAR_PATHS_FILE_NAME); // can be null
		File processModulesFile = getFile(this.uncompressedFiles, ProcessModules.PROCESS_MODULES_FILE_NAME); // can be null
		File jvmFlagsFile = getFile(this.uncompressedFiles, JVMFlags.JVM_FLAGS_FILE_NAME); // can be null

		logger.info("Recording directory detected : " + tdDir);
		
		// sort files by date (date provided by the filter) 
		Arrays.sort(uncompressedTds, new ThreadDumpFileDateComparator(filter));
		
		TranslateData output = new TranslateData(
				uncompressedTds,
				processCardFile,
				processJarPathsFile,
				processModulesFile,
				jvmFlagsFile,
				tdDir
				);
		
		return output;
	}

	private File[] detectTDs(List<File> files, SnapshotFileNameFilter filter) throws JzrTranslatorException {
		if (files.isEmpty())
			throw new JzrTranslatorRecordingSnapshotNotFoundException("Recording is empty.", filter.getSupportedFileFormats());
		
		List<File> tds = new ArrayList<>();
		for (File file : files){
			if (filter.accept(null, file.getName()))
				tds.add(file);
		}
		
		if (tds.isEmpty()) {
			throw new JzrTranslatorRecordingSnapshotNotFoundException("Recording content not recognized :  it does not match the recording file patterns defined in the analysis profile.", filter.getSupportedFileFormats());
		}

		return tds.toArray(new File[tds.size()]);
	}

	@Override
	public STATE getStatusEventState() {
		return JeyzerStatusEvent.STATE.UNZIPPING;
	}

	@Override
	public short getPriority() {
		return PRIORITY;
	}

	@Override
	public ConfigTranslator getConfiguration() {
		return this.compressCfg;
	}

	@Override
	public boolean isEnabled() {
		return this.compressCfg.isEnabled();
	}

	@Override
	public void close() {
		if (compressCfg.isEnabled() && !compressCfg.areTranslatedFilesKept() && uncompressedFiles != null){
			logger.info("Cleaning up the uncompressed recording directory : " + SystemHelper.sanitizePathSeparators(compressCfg.getOuputDirectory()));
			ZipHelper.cleanUncompressedDirectory(uncompressedFiles, null, compressCfg, "");
		}
		if (uncompressedFiles != null) {
			uncompressedFiles.clear();
			uncompressedFiles = null;
		}
	}
	
	private File getFile(List<File> uncompressedFiles, String fileName) {
		File candidate = null;
		
		// Make sure it is provided in the recording
		for (File file : uncompressedFiles) {
			if (file.getName().equals(fileName)) {
				candidate = file;
				break;
			}
		}

		if (candidate != null && candidate.exists() && candidate.isFile())
			return candidate;
		else
			return null;
	}
}
