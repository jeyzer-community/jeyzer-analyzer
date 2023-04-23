package org.jeyzer.analyzer.input.translator.obfuscation;

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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jeyzer.analyzer.config.ConfigUtil;
import org.jeyzer.analyzer.config.translator.ConfigTranslator;
import org.jeyzer.analyzer.config.translator.obfuscation.ConfigDeobfuscation;
import org.jeyzer.analyzer.error.JzrDeobfuscationException;
import org.jeyzer.analyzer.error.JzrDeobfuscationPluginException;
import org.jeyzer.analyzer.error.JzrException;
import org.jeyzer.analyzer.error.JzrInitializationException;
import org.jeyzer.analyzer.error.JzrTranslatorException;
import org.jeyzer.analyzer.input.translator.TranslateData;
import org.jeyzer.analyzer.input.translator.Translator;
import org.jeyzer.analyzer.input.translator.worker.TDTranslator;
import org.jeyzer.analyzer.input.translator.worker.TDTranslatorWorker;
import org.jeyzer.analyzer.input.translator.worker.TDTranslatorWorkerFactory;
import org.jeyzer.analyzer.parser.io.ThreadDumpFileDateComparator;
import org.jeyzer.analyzer.parser.io.ThreadDumpFileDateHelper;
import org.jeyzer.analyzer.parser.io.SnapshotFileNameFilter;
import org.jeyzer.analyzer.status.JeyzerStatusEvent;
import org.jeyzer.analyzer.status.JeyzerStatusEvent.STATE;
import org.jeyzer.analyzer.util.SystemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DeobfuscationTranslator implements Translator, TDTranslator{

	private static final Logger logger = LoggerFactory.getLogger(DeobfuscationTranslator.class);	

	public static final String NAME = "obfuscation";
	private static final short PRIORITY = 500;
	
	protected List<File> deobfuscatedFiles = Collections.synchronizedList(new ArrayList<File>());
	protected Map<String, Throwable> deobsErrors = Collections.synchronizedMap(new HashMap<String, Throwable>());
	protected String id;
	protected short refNumber;
	protected File deobsDir;
	protected String config;
	protected boolean remoteAccess;
	protected boolean abortOnError;
	protected boolean filesKept;
	
	private ConfigTranslator cfg; // local configuration
	
	protected DeobfuscationTranslator(String id, short refNumber, File deobsDir, String config, boolean abortOnError, boolean filesKept) throws JzrInitializationException {
		this.id = id;
		this.deobsDir = deobsDir;
		this.abortOnError = abortOnError;
		this.filesKept = filesKept;
		this.cfg = new ConfigTranslator(ConfigDeobfuscation.TYPE_NAME, true, deobsDir.getAbsolutePath(), config, filesKept, abortOnError);
		
		// config file
		if (ConfigUtil.isValidURI(config))
			this.config = downloadFile(config);
		else
			this.config = config;
		
		checkDeobfuscatorLibrary();
		checkConfigurationFile();
	}

	@Override
	public TranslateData translate(TranslateData inputData, SnapshotFileNameFilter filter, Date sinceDate) throws JzrTranslatorException {
		String configPath = this.config != null ? "using configuration file " + this.config : ""; 
		logger.info("Executing obfuscation translator {} of type {} {}", this.getId(), this.getType(), SystemHelper.sanitizePathSeparators(configPath));
						
		// create the output directory
		try {
			SystemHelper.createDirectory(this.deobsDir.getAbsolutePath());
		} catch (JzrException ex) {
			throw new JzrTranslatorException("Failed to create the directory : " + this.deobsDir.getAbsolutePath()); 
		}
		
		long startTime = 0;
		if (logger.isDebugEnabled())
			startTime = System.currentTimeMillis();

		deobfuscateTDFiles(inputData.getTDs(), filter, sinceDate);
		
		if (logger.isDebugEnabled()){
			long endTime = System.currentTimeMillis();
			logger.debug("Decryption took {} ms", endTime-startTime);
		}
		
		// sort files by date (date provided by the filter)
		this.deobfuscatedFiles.sort(new ThreadDumpFileDateComparator(filter));
		
		TranslateData outputData = new TranslateData(
				this.deobfuscatedFiles.toArray(new File[this.deobfuscatedFiles.size()]),
				inputData.getProcessCard(),
				inputData.getProcessJarPaths(),
				inputData.getProcessModules(),
				inputData.getJVMFlags(),
				this.deobsDir
				);
		
		return outputData;
	}
	
	private void deobfuscateTDFiles(File[] files, SnapshotFileNameFilter filter, Date sinceDate) throws JzrTranslatorException{
		try{			
			int cpuCount = Runtime.getRuntime().availableProcessors();
			ExecutorService executor = Executors.newFixedThreadPool(cpuCount, 
					new TDTranslatorWorkerFactory(NAME));
			
			// empty the lists
			deobsErrors.clear();
			
			for (int i=0; i < files.length; i++){
				File file = files[i];
				
				// skip files before sinceDate
				Date date = ThreadDumpFileDateHelper.getFileDate(filter, file);
				if (date.compareTo(sinceDate)<=0)
					continue;
				
				// ignore empty files, will be processed as missing thread dumps
				if (file.length() == 0){
					logger.warn("Snapshot deobfuscated file is empty : {}", file.getName());
					continue;
				}
				
				// process in parallel the thread dump deobfuscation
				Runnable worker = new TDTranslatorWorker(this, file);
				executor.execute(worker);
			}
			executor.shutdown();
			
			// wait for thread termination
			while (!executor.isTerminated()){}
			
			if (remoteAccess){
				// delete the local temp mapping file
				if (!new File(this.config).delete())
					logger.warn("Failed to delete the local mapping file : " + SystemHelper.sanitizePathSeparators(this.config));
				remoteAccess = false;
			}
			
			if (!deobsErrors.isEmpty()){
				// todo : improve the error handling. Return all deobfuscations errors ?
				
				// Log all deobfuscations errors
				Set<Entry<String, Throwable>> entries = deobsErrors.entrySet();
				Iterator<Entry<String, Throwable>> iter = entries.iterator();
				while(iter.hasNext()){
					Entry<String, Throwable> entry = iter.next();
					logger.warn("Deobfuscation failed for file : " + entry.getKey() + " with deobfuscator " + this.id + ". Error is :" + entry.getValue().getMessage());
				}
				
				// for now return just the first one
				// And <BR> is super crap. Any workaround ? \n doesn't work and not printed
				Map.Entry<String, Throwable> error = deobsErrors.entrySet().iterator().next();
				if (abortOnError)
					throw new JzrDeobfuscationException("Deobfuscation of file " 
						+ error.getKey() 
						+ " failed with deobfuscator " 
						+ this.id 
						+ ".<BR>Error is : " 
						+ error.getValue().getMessage(), 
						error.getValue()); 
			}
			
		}finally{
			// release resources
			deobsErrors.clear();
		}		
	}

	@Override
	public STATE getStatusEventState() {
		return JeyzerStatusEvent.STATE.DEOBFUSCATION;
	}

	@Override
	public short getPriority() {
		return (short) (PRIORITY + refNumber);
	}

	@Override
	public ConfigTranslator getConfiguration() {
		return this.cfg;
	}

	@Override
	public boolean isEnabled() {
		return this.cfg.isEnabled();
	}

	@Override
	public void close() {
		if (!filesKept && deobfuscatedFiles != null){
			logger.info("Cleaning up the deobfuscated snapshot directory : " + this.deobsDir);
			for (File f : deobfuscatedFiles){
				if (f.exists() && !f.delete()) // multiple deobfuscator translators refer to the same set of files  
					logger.warn("Failed to delete deobfuscated file : " + f.getName());
			}
			deobfuscatedFiles = null;
		}
	}

	@Override
	public void translateTDFile(File obfuscatedFile) {
		logger.info("Deobfuscating snapshot file : {}", obfuscatedFile.getName());
		
		// Temp file is required to handle multiple deobfuscations
		File deobfuscatedFile = new File(getDeobfuscatedFileName(obfuscatedFile) + ".tmp");

		// call deobfuscation plugin
		try {
			deobfuscateFile(obfuscatedFile, deobfuscatedFile);
		} catch (Exception ex) {
			logger.error("Failed to deobfuscate file : ", obfuscatedFile.getName(), ex);
			this.deobsErrors.put(obfuscatedFile.getName(), ex);
			if (deobfuscatedFile.exists())
				if (!deobfuscatedFile.delete())
					logger.warn("Failed to delete deobfuscated file : " + deobfuscatedFile.getName());
			return;
		}
		
		// rename to final target file
		File targetFile = new File(getDeobfuscatedFileName(obfuscatedFile));
		// remove if already exists.
		if (targetFile.exists())
			if (!targetFile.delete())
				logger.warn("Failed to delete previous deobfuscated file : " + targetFile.getName());
		if (!deobfuscatedFile.renameTo(targetFile))
			logger.warn("Failed to rename the deobfuscated file " + deobfuscatedFile.getName() + " as " + targetFile.getName());
		if (!targetFile.setLastModified(obfuscatedFile.lastModified()))
			logger.warn("Failed to change the last modified timestamp on the final deobfuscated file " + targetFile.getName());
		
		deobfuscatedFiles.add(targetFile);	
	}
	
	public abstract String getType();
	
	public abstract String getName();
	
	public abstract String getClassName();
	
	public String getId(){
		return id;
	}
	
	protected abstract void deobfuscateFile(File inputFile, File outputFile) throws JzrDeobfuscationPluginException;

	protected abstract void checkConfigurationFile() throws JzrInitializationException;
	
	private void checkDeobfuscatorLibrary() throws JzrInitializationException {
		try {
			Class.forName(this.getClassName());
		} catch(java.lang.ClassNotFoundException ex) {
			logger.warn("Failed to load the " + this.getName() + " obfuscator library. Make sure it is available in the classpath");
			throw new JzrInitializationException("Failed to load the " + this.getName() + " obfuscator library. Make sure it is available in the classpath", ex);
		}
	}

	private String downloadFile(String url) throws JzrInitializationException {
		// web url
		try {
			remoteAccess = true;
			File localMapperFile = File.createTempFile(getName() + "-mapping-" + Thread.currentThread().getName(), ".txt");
			SystemHelper.downloadFile(url, localMapperFile.getAbsolutePath());
			return localMapperFile.getAbsolutePath();
		} catch (Exception ex) {
			throw new JzrInitializationException("Obfuscator " + this.getType() + " configuration plugin file not found : " + this.config);
		}
	}
	
	private String getDeobfuscatedFileName(File stackTraceFile){
		return this.deobsDir + File.separator + stackTraceFile.getName();
	}

}
