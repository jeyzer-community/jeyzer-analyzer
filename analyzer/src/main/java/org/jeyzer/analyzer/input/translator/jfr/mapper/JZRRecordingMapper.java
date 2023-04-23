package org.jeyzer.analyzer.input.translator.jfr.mapper;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jeyzer.analyzer.config.translator.jfr.ConfigJFRDecompression;
import org.jeyzer.analyzer.data.ProcessCard;
import org.jeyzer.analyzer.data.flags.JVMFlags;
import org.jeyzer.analyzer.data.module.ProcessModules;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.error.JzrTranslatorException;
import org.jeyzer.analyzer.input.translator.jfr.reader.JFRDescriptor;
import org.jeyzer.analyzer.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JZRRecordingMapper {
	
	private static final Logger logger = LoggerFactory.getLogger(JZRRecordingMapper.class);

	private ConfigJFRDecompression jfrCfg;
	private long jvmStartTime; // Epoch time in ms
	
	public JZRRecordingMapper(ConfigJFRDecompression jfrCfg) throws JzrTranslatorException {
		this.jfrCfg = jfrCfg;
		
		try {
			SystemHelper.createDirectory(this.jfrCfg.getOuputDirectory());
		} catch (JzrException ex) {
			throw new JzrTranslatorException("Failed to create the JFR output directory : " + this.jfrCfg.getOuputDirectory()); 
		}
	}

	public List<File> generateJZRSnapshots(JFRDescriptor descriptor) throws JzrTranslatorException {
		JZRSnapshotMapper mapper = new JZRSnapshotMapper(this.jvmStartTime, this.jfrCfg); 
		return mapper.generateThreadDumps(descriptor, this.jfrCfg.getOuputDirectory());
	}

	public File generateProcessCard(JFRDescriptor descriptor) throws JzrTranslatorException {
		
		File processCardFile = new File(this.jfrCfg.getOuputDirectory() + File.separator + ProcessCard.PROCESS_CARD_FILE_NAME);

		try (
				FileOutputStream fos = new FileOutputStream(processCardFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				PrintWriter writer = new PrintWriter(osw);
			)
		{
			List<String> entries = new ArrayList<>();
			logger.info("Uncompressing JFR file - creating process card file : {}", processCardFile.getAbsolutePath());
			// Event reference : https://bestsolution-at.github.io/jfr-doc/openjdk-15.html
			JZRProcessCardMapper processCardMapper = new JZRProcessCardMapper();
			processCardMapper.mapInitialSystemProperties(descriptor, entries);
			this.jvmStartTime = processCardMapper.mapJVMInfo(descriptor, entries);
			processCardMapper.mapOSInfo(descriptor, entries);
			processCardMapper.mapInitialEnvironmentVariable(descriptor, entries);
			processCardMapper.mapCPUInfo(descriptor, entries);
			Collections.sort(entries);
			for (String entry : entries)
				writer.write(entry + System.lineSeparator());
			writer.flush();
		} catch (IOException ex) {
			throw new JzrTranslatorException("Failed to write the JZR process card file.", ex);
		}
		
		if (this.jvmStartTime == -1)
			logger.warn("JZR recording is missing the JVMInfo \"jvmStartTime\". The Java Flight Recorder profile should be configured to capture it.");
		
		return processCardFile;
	}
	
	public File generateJVMFlags(JFRDescriptor descriptor) throws JzrTranslatorException {
		
		File jvmFlagsFile = new File(this.jfrCfg.getOuputDirectory() + File.separator + JVMFlags.JVM_FLAGS_FILE_NAME);

		try (
				FileOutputStream fos = new FileOutputStream(jvmFlagsFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				PrintWriter writer = new PrintWriter(osw);
			)
		{
			List<String> entries = new ArrayList<>();
			logger.info("Uncompressing JFR file - creating jvm flags file : {}", jvmFlagsFile.getAbsolutePath());
			// Event reference : https://bestsolution-at.github.io/jfr-doc/openjdk-15.html
			JZRJVMFlagMapper jvmFlagsMapper = new JZRJVMFlagMapper();
			jvmFlagsMapper.mapFlags(descriptor, entries);
			Collections.sort(entries);
			for (String entry : entries)
				writer.write(entry + System.lineSeparator());
			writer.flush();
		} catch (IOException ex) {
			throw new JzrTranslatorException("Failed to write the JZR jvm flags file.", ex);
		}
		
		return jvmFlagsFile;
	}

	public File generateProcessModules(JFRDescriptor descriptor) throws JzrTranslatorException {
		File processModuleFile = new File(this.jfrCfg.getOuputDirectory() + File.separator + ProcessModules.PROCESS_MODULES_FILE_NAME);

		try (
				FileOutputStream fos = new FileOutputStream(processModuleFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				PrintWriter writer = new PrintWriter(osw);
			)
		{
			logger.info("Uncompressing JFR file - creating process modules file : {}", processModuleFile.getAbsolutePath());
			JZRProcessModuleMapper moduleMapper = new JZRProcessModuleMapper();
			moduleMapper.mapModules(descriptor, writer);
			writer.flush();
		} catch (IOException ex) {
			throw new JzrTranslatorException("Failed to write the JZR process module file.", ex);
		}
		
		return processModuleFile;
	}

	public File generateProcessJarPaths(JFRDescriptor descriptor) {
		// Not supported
		//  JFR exposes only the modules (with jar path inside). Not enough
		//  JFR does not expose the Manifest version attributes
		return null;
	}
}
