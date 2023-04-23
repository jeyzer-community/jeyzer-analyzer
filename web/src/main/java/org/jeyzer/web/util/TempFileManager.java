package org.jeyzer.web.util;

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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.jeyzer.web.error.JzrWebException;
import org.jeyzer.web.error.JzrWebUploadedRecordingExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TempFileManager {

	private static final Logger logger = LoggerFactory.getLogger(TempFileManager.class);	

	public class TempFileManagerThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setName("Temp TD Manager");
			return t;
		}
	}
	
	public static final String JZRW_JEYZER_DIRECTORY = "Jeyzer-tds";
	public static final String TEMP_ID_SEPARATOR = "-";
	
	private String tempDirectory;
	private long maxRetentionTime; // in ms
	private ScheduledExecutorService executor;
	
	// guarded by tempZips
	private Map<String, TempFileEntry> tempZips = new HashMap<>();
	
	public TempFileManager(String dir, long maxRetentionTime) {
		this.tempDirectory = dir != null ? dir : System.getProperty("java.io.tmpdir") + File.separator + JZRW_JEYZER_DIRECTORY;
		this.maxRetentionTime = maxRetentionTime;
		
		File tmpDir = new File(tempDirectory);
		if (!tmpDir.exists())
			tmpDir.mkdirs();
		
		// start the cleanup thread (delayed)
		TempFileCleanupTask cleaningTask = new TempFileCleanupTask(tempZips);
		executor = Executors.newSingleThreadScheduledExecutor(
				new TempFileManagerThreadFactory());
		executor.scheduleWithFixedDelay(cleaningTask, 
				60, 
				60, 
				TimeUnit.SECONDS);
	}
	
	public String getTempDirectory() {
		return tempDirectory;
	}
	
	public String addZipFile(String fileName, InputStream inputStream){
		long timestamp = System.currentTimeMillis();
		String id = timestamp + TEMP_ID_SEPARATOR + fileName;
		
		// copy as temporary file, prefixed with time stamp in order to be unique
		String targetFileName = getTempDirectory() + File.separatorChar + id;
		try {
			Files.copy(inputStream, Paths.get(targetFileName));
		} catch (IOException e) {
			logger.error("Failed to upload file " + fileName + " on disk.", e);
		}
		
		TempFileEntry entry = new TempFileEntry(fileName, timestamp);
		
		synchronized(tempZips){
			tempZips.put(id, entry);
		}
		
		return id;
	}
	
	public String moveZipFile(String tempZipFileId, String targetPath, String requestId) throws JzrWebException{
		TempFileEntry entry;
		
		synchronized(tempZips){
			if (!tempZips.containsKey(tempZipFileId)){
				String zipFileName = getOriginalZipFileName(tempZipFileId);
				throw new JzrWebUploadedRecordingExpiredException("Uploaded recording has expired while waiting for analysis. Please reload it (" + zipFileName + "). Expiration timeout set to : " + maxRetentionTime/1000L + " seconds.");
			}
			entry = tempZips.remove(tempZipFileId);
		}
		
		// copy the recording file
		String targetFileName = requestId + "-" + entry.getFileName();
		File source = new File(tempDirectory + File.separatorChar + tempZipFileId);
		File target = new File(targetPath + File.separatorChar + targetFileName);
		
		if (logger.isDebugEnabled())
			logger.debug("Moving the file " + source.getAbsolutePath() + " to directory " + targetPath);		
		try {
			com.google.common.io.Files.move(source, target);
		} catch (IOException e) {
			throw new JzrWebException("Failed to move file " + targetFileName + " to directory " + targetPath, e);
		}
		
		return targetFileName;
	}
	
	public String getOriginalZipFileName(String tempZipFileId){
		// remove time stamp prefix
		return tempZipFileId.substring(tempZipFileId.indexOf(TEMP_ID_SEPARATOR)+1);
	}
	
	public void shutdown() {
		this.executor.shutdown();
		
		// remove all temp files
		for (String id : tempZips.keySet()){
			File file = new File(getTempDirectory() + File.separatorChar + id);
			if (file.exists() && file.isFile()){
				if (logger.isInfoEnabled())
					logger.info("Shutdown cleaning : deleting the temporary recording file : " + file.getAbsolutePath());						
				if (!file.delete())
					logger.warn("Shutdown cleaning : failed to delete the temporary recording file : " + file.getAbsolutePath());
			}
		}
		tempZips.clear();
	}

	public final class TempFileCleanupTask implements Runnable {
		Map<String, TempFileEntry> tempZips;
		
		public TempFileCleanupTask(Map<String, TempFileEntry> tempZips) {
			this.tempZips = tempZips;
		}

		@Override
		public void run() {
			try{
				synchronized(tempZips){
					cleanupTempZips();
				}
			}catch(Exception ex){
				logger.error("Failed to clean the Jeyzer temp directory.", ex);
			}
		}

		private void cleanupTempZips() {
			long currentTime = System.currentTimeMillis();
			
			if (logger.isDebugEnabled())
				logger.debug("Cleaning up the Jeyzer temp directory.");
			
			List<String> candidates = new ArrayList<>();
			
			for (String id : tempZips.keySet()){
				TempFileEntry entry = tempZips.get(id);
				if (currentTime - entry.getInsertionTime() > maxRetentionTime){
					File file = new File(getTempDirectory() + File.separatorChar + id);
					if (file.exists() && file.isFile()){
						if (logger.isInfoEnabled())
							logger.info("Deleting the temporary recording file : " + file.getAbsolutePath());
						if (!file.delete())
							logger.warn("Failed to delete the temporary recording file : " + file.getAbsolutePath());
						candidates.add(id);
					}
				}
			}
			
			for (String id : candidates){
				tempZips.remove(id);
			}
		}

	}
	
	public static final class TempFileEntry{
		private String fileName;
		private long insertionTime;
		
		public TempFileEntry(String fileName, long insertionTime){
			this.fileName = fileName;
			this.insertionTime = insertionTime;
		}

		public String getFileName() {
			return fileName;
		}

		public long getInsertionTime() {
			return insertionTime;
		}
		
	}	
}
